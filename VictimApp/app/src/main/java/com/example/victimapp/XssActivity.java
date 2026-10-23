package com.example.victimapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
        // VULNERABILITY (WebView XSS): JavaScript is enabled and a WebChromeClient is set,
        // so an injected onerror/alert() handler actually runs and can show dialogs.
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

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
        webView.loadDataWithBaseURL("https://victim.local/", html, "text/html", "UTF-8", null);
    }

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
