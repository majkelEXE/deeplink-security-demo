package com.example.victimapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountActivity extends AppCompatActivity {

    // VULNERABLE onCreate (active by default).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // VULNERABILITY (Auth Bypass): the protected data is shown with no session check,
        // so a deep link (victimapp://account) reaches it even when the user is logged out.
        // The banner below only visualizes the session state; it does NOT gate access.
        showSessionBanner();
    }

    // ---------------------------------------------------------------------------
    // SAFE variant (for presentation screenshots).
    // To capture the "fixed" state: comment out the vulnerable onCreate above and
    // uncomment this one. Keep exactly ONE onCreate active.
    // Uses the already-imported android.content.Intent.
    // ---------------------------------------------------------------------------
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_account);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        // DEFENSE (Auth Bypass fixed): the protected screen checks the session itself,
//        // so the deep link cannot reach the data. No session -> go to login and finish.
//        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
//        if (!prefs.getBoolean(MainActivity.KEY_LOGGED_IN, false)) {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//            return;
//        }
//        showSessionBanner();
//    }

    private void showSessionBanner() {
        TextView banner = findViewById(R.id.textSessionState);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean(MainActivity.KEY_LOGGED_IN, false);
        if (loggedIn) {
            String username = prefs.getString(MainActivity.KEY_USERNAME, "");
            banner.setText(getString(R.string.account_session_ok, username));
            banner.setBackgroundColor(ContextCompat.getColor(this, R.color.success_light));
            banner.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else {
            banner.setText(R.string.account_session_bypass);
            banner.setBackgroundColor(ContextCompat.getColor(this, R.color.danger_light));
            banner.setTextColor(ContextCompat.getColor(this, R.color.danger));
        }
    }
}
