package com.ratna.NotificationStore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AppPermissionActivity extends AppCompatActivity {
    private Button buttonAllowAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_permission);

        SharedPreferences preferences = getSharedPreferences("NotificationStorePrefs", MODE_PRIVATE);
        boolean permissionGranted = preferences.getBoolean("permission_granted", false);

        if (permissionGranted) {
            navigateToMainActivity();
        } else {
            setFinishOnTouchOutside(false);

            buttonAllowAccess = findViewById(R.id.buttonAllowAccess);
            buttonAllowAccess.setOnClickListener(v -> {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("permission_granted", true);
                editor.apply();

                navigateToMainActivity();
            });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AppPermissionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
