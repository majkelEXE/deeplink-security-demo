package com.example.victimapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // Demo-only credentials for the educational project.
    private static final String DEMO_USERNAME = "student";
    private static final String DEMO_PASSWORD = "demo123";

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        errorText = findViewById(R.id.textError);
        MaterialButton loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = textOf(usernameInput);
        String password = textOf(passwordInput);

        if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
            // Store the session; MainActivity reads it in onResume.
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
            prefs.edit()
                    .putBoolean(MainActivity.KEY_LOGGED_IN, true)
                    .putString(MainActivity.KEY_USERNAME, username)
                    .apply();
            Toast.makeText(this, R.string.toast_logged_in, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            errorText.setText(R.string.login_error);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
