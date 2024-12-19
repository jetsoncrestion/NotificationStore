package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private ImageView imageView;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

//        if (isPinSet()) {
//            // If PIN is set, navigate to SecurityActivity for PIN validation
//            Intent intent = new Intent(SplashScreen.this, SecurityActivity.class);
//            startActivity(intent);
//            finish();
//        } else {
//            // If PIN is not set, continue with the splash screen animation
//            setContentView(R.layout.activity_splash_screen);
//            initializeUI();
//        }

        TextView textView = findViewById(R.id.textView);
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        imageView = findViewById(R.id.imageView);
        animation = AnimationUtils.loadAnimation(this, R.anim.image_animation);
        imageView.startAnimation(animation);
        textView.startAnimation(animation);

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{Color.parseColor("#5D3FD3"), Color.parseColor("#0F52BA")},
                null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(SplashScreen.this, AppSelectionActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initializeUI() {
            TextView textView = findViewById(R.id.textView);
            TextPaint paint = textView.getPaint();
            float width = paint.measureText(textView.getText().toString());

            imageView = findViewById(R.id.imageView);
            animation = AnimationUtils.loadAnimation(this, R.anim.image_animation);

            Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                    new int[]{Color.parseColor("#5D3FD3"), Color.parseColor("#0F52BA")},
                    null, Shader.TileMode.CLAMP);
            textView.getPaint().setShader(textShader);
        }

    private boolean isPinSet() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isPinSet", false);
    }


    private void checkPinSetup() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isPinSet = sharedPreferences.getBoolean("isPinSet", false);

        Intent intent;
        if (isPinSet) {
            intent = new Intent(SplashScreen.this, AppSelectionActivity.class);
        } else {
            intent = new Intent(SplashScreen.this, SecurityActivity.class);
        }

        startActivity(intent);
        finish();
    }
}