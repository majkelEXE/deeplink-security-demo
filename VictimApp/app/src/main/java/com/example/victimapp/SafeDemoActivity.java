package com.example.victimapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SafeDemoActivity extends AppCompatActivity {

    // Allowlist for the safe redirect check: exactly this host, nothing else.
    private static final String TRUSTED_REDIRECT_HOST = "example.com";

    // Base URL of the demo-site (Vercel); replace after deployment.
    private static final String DEMO_BASE_URL = "https://deeplink-security-demo.vercel.app";

    // Sample untrusted input used by the safe-rendering demo.
    private static final String SAMPLE_PAYLOAD =
            "<img src=x onerror=\"alert(document.getElementById('secret').textContent)\">";

    // Preset URLs for the redirect allowlist demo: one valid, the rest are traps.
    private static final String[] REDIRECT_PRESETS = {
            "https://example.com/konto",
            "https://example.com.evil.com/",
            "https://example.com@evil.com/",
            "https://example.com:8443/",
            "http://example.com/"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_safe_demo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUpSafeAccount();
        setUpSafeRedirect();
        setUpSafeXss();
        setUpSafeAppLink();
    }

    private void setUpSafeAccount() {
        TextView result = findViewById(R.id.textAccountResult);
        MaterialButton button = findViewById(R.id.buttonSafeAccount);
        button.setOnClickListener(v -> {
            // Safe: the protected data is shown only when a session exists.
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
            boolean loggedIn = prefs.getBoolean(MainActivity.KEY_LOGGED_IN, false);
            if (loggedIn) {
                result.setText(R.string.safe_account_ok);
                result.setTextColor(ContextCompat.getColor(this, R.color.success));
            } else {
                result.setText(R.string.safe_account_blocked);
                result.setTextColor(ContextCompat.getColor(this, R.color.danger));
            }
            result.setVisibility(View.VISIBLE);
        });
    }

    private void setUpSafeRedirect() {
        TextView result = findViewById(R.id.textRedirectResult);
        MaterialButton button = findViewById(R.id.buttonSafeRedirect);
        button.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (String url : REDIRECT_PRESETS) {
                String verdict = isAllowedRedirect(url)
                        ? getString(R.string.safe_redirect_allow)
                        : getString(R.string.safe_redirect_block);
                sb.append(verdict).append("  ").append(url).append('\n');
            }
            result.setText(sb.toString().trim());
            result.setVisibility(View.VISIBLE);
        });
    }

    // Strict allowlist check: HTTPS only, exact host, no userinfo, default port or 443.
    // No contains()/endsWith(): those would accept subdomain and suffix traps.
    private boolean isAllowedRedirect(String rawUrl) {
        Uri uri = Uri.parse(rawUrl);
        if (!"https".equals(uri.getScheme())) {
            return false;
        }
        if (uri.getUserInfo() != null) {
            return false;
        }
        if (!TRUSTED_REDIRECT_HOST.equals(uri.getHost())) {
            return false;
        }
        int port = uri.getPort();
        return port == -1 || port == 443;
    }

    private void setUpSafeXss() {
        TextView result = findViewById(R.id.textXssResult);
        MaterialButton button = findViewById(R.id.buttonSafeXss);
        button.setOnClickListener(v -> {
            // Safe: the untrusted payload is shown as plain text, never executed.
            result.setText(SAMPLE_PAYLOAD);
            result.setVisibility(View.VISIBLE);
        });
    }

    private void setUpSafeAppLink() {
        MaterialButton button = findViewById(R.id.buttonSafeApplink);
        button.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DEMO_BASE_URL + "/secure")));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.safe_applink_no_app, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
