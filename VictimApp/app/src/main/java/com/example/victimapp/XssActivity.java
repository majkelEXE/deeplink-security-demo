package com.example.victimapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class XssActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_xss);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WebView webView = findViewById(R.id.webview);
        // VULNERABILITY (WebView XSS): JavaScript is enabled, so an injected onerror
        // handler runs. onJsAlert is overridden so the injected alert() is actually
        // displayed (the base WebChromeClient does not show a dialog on every WebView).
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(XssActivity.this)
                        .setTitle("victim.example")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, (d, w) -> result.confirm())
                        .show();
                return true;
            }
        });

        String name = null;
        if (getIntent().getData() != null) {
            name = getIntent().getData().getQueryParameter("name");
        }
        if (name == null || name.isEmpty()) {
            name = getString(R.string.xss_default_name);
        }

        String html;
        try {
            // VULNERABILITY: the untrusted "name" is injected into the HTML template
            // without any encoding, so markup inside it is parsed and executed.
            html = readAsset("profile.html").replace("{{NAME}}", name);
        } catch (IOException e) {
            Toast.makeText(this, R.string.xss_template_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Local render only; the base URL just gives the page an https-like origin.
        // Uses a reserved .example host so the injected <img src=x> fails DNS fast
        // (avoids the slow mDNS lookup of a .local host before onerror fires).
        webView.loadDataWithBaseURL("https://victim.example/", html, "text/html", "UTF-8", null);
    }

    // ---------------------------------------------------------------------------
    // SAFE variant (for presentation screenshots).
    // To capture the "fixed" state: comment out the vulnerable onCreate above and
    // uncomment this whole block (onCreate + escapeHtml). Keep ONE onCreate.
    // ---------------------------------------------------------------------------
//    @SuppressLint("SetJavaScriptEnabled")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_xss);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        WebView webView = findViewById(R.id.webview);
//        webView.getSettings().setJavaScriptEnabled(true);
//
//        String name = null;
//        if (getIntent().getData() != null) {
//            name = getIntent().getData().getQueryParameter("name");
//        }
//        if (name == null || name.isEmpty()) {
//            name = getString(R.string.xss_default_name);
//        }
//
//        String html;
//        try {
//            // DEFENSE (WebView XSS fixed): HTML-encode the untrusted value before
//            // injecting it, so any markup becomes inert text instead of executing.
//            html = readAsset("profile.html").replace("{{NAME}}", escapeHtml(name));
//        } catch (IOException e) {
//            Toast.makeText(this, R.string.xss_template_error, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        webView.loadDataWithBaseURL("https://victim.example/", html, "text/html", "UTF-8", null);
//    }
//
//    // DEFENSE: minimal HTML entity encoding for untrusted text.
//    private String escapeHtml(String s) {
//        return s.replace("&", "&amp;")
//                .replace("<", "&lt;")
//                .replace(">", "&gt;")
//                .replace("\"", "&quot;")
//                .replace("'", "&#39;");
//    }

    private String readAsset(String fileName) throws IOException {
        try (InputStream is = getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}
