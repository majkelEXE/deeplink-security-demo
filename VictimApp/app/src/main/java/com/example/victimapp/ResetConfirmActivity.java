package com.example.victimapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class ResetConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_confirm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tokenView = findViewById(R.id.textToken);
        MaterialButton confirmButton = findViewById(R.id.buttonConfirm);
        TextView resultView = findViewById(R.id.textResult);

        // Legitimate target screen for victimapp://reset/confirm?token=...
        // AttackerApp registers the same filter to hijack this link (see AttackerApp).
        String token = null;
        if (getIntent().getData() != null) {
            token = getIntent().getData().getQueryParameter("token");
        }

        if (token == null || token.isEmpty()) {
            tokenView.setText(R.string.reset_no_token);
            confirmButton.setEnabled(false);
        } else {
            tokenView.setText(token);
        }

        confirmButton.setOnClickListener(v -> {
            resultView.setText(R.string.reset_done);
            resultView.setVisibility(View.VISIBLE);
        });
    }
}
