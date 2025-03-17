package com.ratna.notificationstore;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HowToUseApp extends AppCompatActivity {
    private ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_how_to_use_app);

        imageViewBack = findViewById(R.id.imageViewBack);

        imageViewBack.setOnClickListener(view -> {
            finish();
        });

    }
}