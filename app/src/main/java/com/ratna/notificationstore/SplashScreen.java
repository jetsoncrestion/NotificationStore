package com.ratna.notificationstore;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashScreen extends AppCompatActivity {
    private Animation animation;
   // private OkHttpClient client = new OkHttpClient();
    private static final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        TextView textView = findViewById(R.id.textView);
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        // Animation for the text
        animation = AnimationUtils.loadAnimation(this, R.anim.image_animation);
        textView.startAnimation(animation);

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{Color.parseColor("#ffffff"), Color.parseColor("#ffffff")},
                null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);

        // Listener to proceed after animation ends
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
               // performGetRequest(); // Call the API check function
                Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

//    private void performGetRequest() {
//        // Use the plain URL directly instead of Base64
//        String url = "https://www.google.com" + "?package_name=" + getPackageName();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "GET request failed: " + e.getMessage());
//                runOnUiThread(() -> setToWelcomeScreen());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    final String responseData = response.body().string();
//                    Log.d(TAG, "GET request successful: " + responseData);
//
//                    try {
//                        // Attempt to parse the response as a JSON object
//                        HashMap<String, Object> vmap = new Gson().fromJson(responseData,
//                                new TypeToken<HashMap<String, Object>>() {
//                                }.getType());
//
//                        // Debugging the response data
//                        Log.d(TAG, "Parsed response: " + vmap);
//
//                        runOnUiThread(() -> {
//                            if (vmap.containsKey("package_name") && vmap.get("package_name") != null) {
//                                // Ensure the 'package_name' matches
//                                if (getPackageName().equals(vmap.get("package_name").toString())) {
//                                    if (vmap.containsKey("crash") && "Yes".equalsIgnoreCase(vmap.get("crash").toString())) {
//                                        showCrashDialog(vmap);
//                                    } else {
//                                        setToWelcomeScreen();
//                                    }
//                                } else {
//                                    Log.e(TAG, "Package name mismatch: expected " + getPackageName() + ", found " + vmap.get("package_name"));
//                                    setToWelcomeScreen();
//                                }
//                            } else {
//                                Log.e(TAG, "'package_name' is missing or null in the response.");
//                                setToWelcomeScreen();
//                            }
//                        });
//                    } catch (Exception e) {
//                        Log.e(TAG, "Error parsing response: " + e.getMessage());
//                        e.printStackTrace();
//                        runOnUiThread(() -> setToWelcomeScreen());
//                    }
//                } else {
//                    Log.e(TAG, "GET request failed: " + response.message());
//                    runOnUiThread(() -> setToWelcomeScreen());
//                }
//            }
//        });
//    }
//
//    private void showCrashDialog(HashMap<String, Object> vmap) {
//        AlertDialog dialog = new AlertDialog.Builder(SplashScreen.this).create();
//        dialog.setTitle("App Notice");
//        dialog.setMessage(vmap.get("message").toString());
//        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Visit", (dialogInterface, which) -> {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(android.net.Uri.parse("https://www.google.com")); // Updated redirect URL
//            startActivity(intent);
//        });
//        dialog.setCancelable(false);
//        dialog.show();
//    }

//    private void setToWelcomeScreen() {
//        Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class);
//        startActivity(intent);
//        finish();
//    }
    }