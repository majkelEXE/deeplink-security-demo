package com.example.victimapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class RedirectActivity extends AppCompatActivity {

    private String targetUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_redirect);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView urlView = findViewById(R.id.textUrl);
        MaterialButton openButton = findViewById(R.id.buttonOpen);

        Uri data = getIntent().getData();
        targetUrl = data != null ? data.getQueryParameter("url") : null;

        if (targetUrl == null || targetUrl.isEmpty()) {
            urlView.setText(R.string.redirect_no_url);
            openButton.setEnabled(false);
            return;
        }

        urlView.setText(targetUrl);
        // The button lets the presenter re-trigger the redirect without re-scanning.
        openButton.setOnClickListener(v -> openInBrowser());

        // VULNERABILITY (Open Redirect): the url parameter is forwarded to an external
        // browser with no validation of scheme, host, port or anything else.
        openInBrowser();
    }

    private void openInBrowser() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.redirect_no_app, Toast.LENGTH_SHORT).show();
        }
    }
}
