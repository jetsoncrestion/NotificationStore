package com.ratna.NotificationStore;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms_and_conditions);
        WebView webView = findViewById(R.id.termsAndConditions);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/terms_and_conditions.html");
    }
}