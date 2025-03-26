package com.ratna.notificationstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.ConditionVariable;
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
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.ratna.notificationstore.Fragments.WelcomeFragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SplashScreen extends AppCompatActivity {
    private Animation animation;
    private static final String TAG = "SplashScreen";
    private boolean isAnimationFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        TextView textView = findViewById(R.id.textView);
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

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
                isAnimationFinished = true;
               proceedToNextActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void proceedToNextActivity() {
        boolean isPinLockEnabled = getSharedPreferences("MyPrefs", MODE_PRIVATE).getBoolean("setPinLock", false);
        if (isPinLockEnabled) {
            String storedPin = getStoredPin(this);
            if (storedPin == null) {
                startActivity(new Intent(this, Welcome.class));
            } else {
                startActivity(new Intent(this, PinLock.class));
            }
        } else {
            startActivity(new Intent(this, Welcome.class));
        }
        finish();
    }

    private String getStoredPin(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "secure_pin_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            return sharedPreferences.getString("PIN", null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}