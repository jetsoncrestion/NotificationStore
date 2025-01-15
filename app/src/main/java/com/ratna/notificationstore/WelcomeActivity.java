package com.ratna.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_WELCOME_SCREEN_SHOWN = "welcome_screen_shown";
    private TextView textViewSkip;
    private ImageView imageViewArrowRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        textViewSkip = findViewById(R.id.textViewSkip);
        imageViewArrowRight = findViewById(R.id.imageViewArrowRight);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isWelcomeScreenShown = sharedPreferences.getBoolean(KEY_WELCOME_SCREEN_SHOWN, false);

        if (isWelcomeScreenShown) {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            textViewSkip.setOnClickListener(v -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_WELCOME_SCREEN_SHOWN, true);
                editor.apply();

                Intent intent = new Intent(WelcomeActivity.this, WelcomeThird.class);
                startActivity(intent);
                finish();
            });

            imageViewArrowRight.setOnClickListener(v -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_WELCOME_SCREEN_SHOWN, true);
                editor.apply();

                Intent intent = new Intent(WelcomeActivity.this, WelcomeSecond.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
