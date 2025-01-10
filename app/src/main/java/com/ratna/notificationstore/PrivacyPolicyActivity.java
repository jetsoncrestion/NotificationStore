package com.ratna.notificationstore;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);

        WebView webView = findViewById(R.id.privacyPolicy);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/privacy_policy.html");
    }
}