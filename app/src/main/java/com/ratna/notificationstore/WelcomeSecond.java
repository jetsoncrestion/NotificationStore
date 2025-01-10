package com.ratna.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeSecond extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_WELCOME_SECOND_SCREEN_SHOWN = "welcome_second_screen_shown";
    private TextView textViewSkip;
    private ImageView imageViewArrowRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_second);
        textViewSkip = findViewById(R.id.textViewSkip);
        imageViewArrowRight = findViewById(R.id.imageViewArrowRight);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isWelcomeSecondScreenShown = sharedPreferences.getBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, false);
        Log.d("WelcomeSecond", "isWelcomeSecondScreenShown: " + isWelcomeSecondScreenShown);

        if (isWelcomeSecondScreenShown) {
            Intent intent = new Intent(WelcomeSecond.this, WelcomeFourth.class);
            startActivity(intent);
            finish();
        } else {
            textViewSkip.setOnClickListener(v -> {
                SharedPreferences .Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, true);
                editor.apply();
                Log.d("WelcomeSecond", "isWelcomeSecondScreenShown updated: " + sharedPreferences.getBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, false));

                Intent intent = new Intent(WelcomeSecond.this, WelcomeFourth.class);
                startActivity(intent);
                finish();
            });

            imageViewArrowRight.setOnClickListener(v -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, true);
                editor.apply();

                Log.d("WelcomeSecond", "isWelcomeSecondScreenShown updated: " + sharedPreferences.getBoolean(KEY_WELCOME_SECOND_SCREEN_SHOWN, false));

                Intent intent = new Intent(WelcomeSecond.this, WelcomeThird.class);
                startActivity(intent);
                finish();
            });
        }
    }
}