//package com.ratna.notificationstore;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class WelcomeThird extends AppCompatActivity {
//    private static final String PREFS_NAME = "MyPrefs";
//    private static final String KEY_WELCOME_THIRD_SCREEN_SHOWN = "welcome_third_screen_shown";
//    private Button buttonGetStarted;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.fragment_welcome_third);
//
//        buttonGetStarted = findViewById(R.id.buttonGetStarted);
//
//        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        boolean isWelcomeThirdScreenShown = sharedPreferences.getBoolean(KEY_WELCOME_THIRD_SCREEN_SHOWN, false);
//
//        buttonGetStarted.setOnClickListener(v -> {
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(KEY_WELCOME_THIRD_SCREEN_SHOWN, true);
//            editor.apply();
//            navigateToPermissionEnable();
//        });
//    }
//
//    private void navigateToPermissionEnable() {
//        //Log.d(TAG, "Navigating to Permission Enable Screen");
//        Intent intent = new Intent(WelcomeThird.this, AppSelectionSecond.class);
//        startActivity(intent);
//        finish();
//    }
//}