package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WelcomeSecond extends AppCompatActivity {
    private TextView textViewSkip;
    private ImageView imageViewArrow;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_WELCOME_SECOND_SCREEN_SHOWN = "welcome_second_screen_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_second);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isWelcomeSecondScreenShown = sharedPreferences.getBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, false);

        if (isWelcomeSecondScreenShown) {
            Intent intent = new Intent(WelcomeSecond.this, WelcomeFourth.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_welcome_second);

            textViewSkip = findViewById(R.id.textViewSkip);
            imageViewArrow = findViewById(R.id.imageViewArrow);

            textViewSkip.setOnClickListener(v -> {
                SharedPreferences .Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, true);
                editor.apply();

                Intent intent = new Intent(WelcomeSecond.this, WelcomeFourth.class);
                startActivity(intent);
                finish();
            });

            imageViewArrow.setOnClickListener(v -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, true);
                editor.apply();

                Intent intent = new Intent(WelcomeSecond.this, WelcomeThird.class);
                startActivity(intent);
                finish();
            });
        }
    }
}