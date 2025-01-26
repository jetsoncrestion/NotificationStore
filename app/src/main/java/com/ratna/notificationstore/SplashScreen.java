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
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        TextView textView = findViewById(R.id.textView);
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Log.d(TAG, "Update flow completed successfully!");
                Toast.makeText(this, "App updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Update flow failed! Result code: " + result.getResultCode());
                Toast.makeText(this, "App update failed!", Toast.LENGTH_SHORT).show();
            }
        });
        CheckForAppUpdate();

        animation = AnimationUtils.loadAnimation(this, R.anim.image_animation);
        textView.startAnimation(animation);

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(), new int[]{Color.parseColor("#ffffff"), Color.parseColor("#ffffff")}, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
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
                    Toast.makeText(this, "Immediate update is available.", Toast.LENGTH_SHORT).show();

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                activityResultLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting update flow: " + e.getMessage());
                        Toast.makeText(this, "Failed to start update flow.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Update available but not allowed for immediate update.");
                    Toast.makeText(this, "Update available but not immediate.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "No update available.");
                Toast.makeText(this, "Your app is up to date!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to check for updates: " + e.getMessage());
            Toast.makeText(this, "Error checking for updates.", Toast.LENGTH_SHORT).show();
        });
    }
}