package com.example.victimapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    // Simple session store, also read/written by LoginActivity and AccountActivity.
    static final String PREFS = "session";
    static final String KEY_LOGGED_IN = "logged_in";
    static final String KEY_USERNAME = "username";

    private TextView status;
    private MaterialButton sessionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        status = findViewById(R.id.textStatus);
        sessionButton = findViewById(R.id.buttonSession);
        sessionButton.setOnClickListener(v -> onSessionButtonClicked());

        MaterialButton accountButton = findViewById(R.id.buttonAccount);
        accountButton.setOnClickListener(v -> onOpenAccountClicked());

        MaterialButton safeButton = findViewById(R.id.buttonSafe);
        safeButton.setOnClickListener(v -> startActivity(new Intent(this, SafeDemoActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh after returning from LoginActivity or after logout.
        refreshSessionUi();
    }

    private void onSessionButtonClicked() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            // Logged in -> log out by clearing the session.
            prefs.edit().clear().apply();
            refreshSessionUi();
        } else {
            // Logged out -> open the login screen.
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void onOpenAccountClicked() {
        // Safe path: verify the session before opening the protected screen.
        // (AccountActivity itself does NOT check it, which is the demonstrated flaw.)
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            startActivity(new Intent(this, AccountActivity.class));
        } else {
            Toast.makeText(this, R.string.account_requires_login, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void refreshSessionUi() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean(KEY_LOGGED_IN, false);
        if (loggedIn) {
            String username = prefs.getString(KEY_USERNAME, "");
            status.setText(getString(R.string.status_logged_in, username));
            sessionButton.setText(R.string.action_logout);
        } else {
            status.setText(R.string.status_logged_out);
            sessionButton.setText(R.string.action_login);
        }
    }
}
