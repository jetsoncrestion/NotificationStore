package com.ratna.notificationstore;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class SplashScreen extends AppCompatActivity {
    private Animation animation;
    private static final String TAG = "SplashScreen";
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private boolean isUpdateRequired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        TextView textView = findViewById(R.id.textView);
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        // Register result launcher for update flow completion
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Log.d(TAG, "Update flow completed successfully!");
                Toast.makeText(this, "App updated successfully!", Toast.LENGTH_SHORT).show();
                proceedToNextActivity();
            } else {
                Log.e(TAG, "Update flow failed! Result code: " + result.getResultCode());
                Toast.makeText(this, "App update failed! You cannot proceed without updating.", Toast.LENGTH_SHORT).show();
                finish(); // Close the app if update fails
            }
        });

        // Check for app update
        CheckForAppUpdate();

        // Apply animation
        animation = AnimationUtils.loadAnimation(this, R.anim.image_animation);
        textView.startAnimation(animation);

        // Set text gradient
        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{Color.parseColor("#ffffff"), Color.parseColor("#ffffff")}, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);

        // Handle animation end and launch next activity if no update is required
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isUpdateRequired) {
                    Log.d(TAG, "Update is required, blocking access.");
                } else {
                    proceedToNextActivity();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void CheckForAppUpdate() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            Log.d(TAG, "Update Availability: " + appUpdateInfo.updateAvailability());
            Log.d(TAG, "Is Immediate Update Allowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE));
            Log.d(TAG, "Is Flexible Update Allowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE));

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    Log.d(TAG, "Immediate update is available.");
                    Toast.makeText(this, "Immediate update is available. Please update the app.", Toast.LENGTH_SHORT).show();
                    isUpdateRequired = true; // Block access until update is completed

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                activityResultLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting update flow: " + e.getMessage());
                        Toast.makeText(this, "Failed to start update flow.", Toast.LENGTH_SHORT).show();
                        finish(); // Close the app if there's an error
                    }
                } else {
                    Log.d(TAG, "Update available but not allowed for immediate update.");
                }
            } else {
                Log.d(TAG, "No update available.");
                proceedToNextActivity();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to check for updates: ", e);
            proceedToNextActivity();
        });
    }
}
