package com.ratna.notificationstore;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.appcheck.interop.BuildConfig;

public class AboutActivity extends AppCompatActivity {
    private ImageView imageViewBack, imageViewAbout;
    private TextView appNameVersion, appNameVersionDesp;
    private Button PP, tc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewAbout = findViewById(R.id.imageViewAbout);
        appNameVersion = findViewById(R.id.appNameVersion);
        appNameVersionDesp = findViewById(R.id.appNameVersionDesp);
        PP = findViewById(R.id.PP);
        tc = findViewById(R.id.tc);

        imageViewBack.setOnClickListener(v -> {
            super.onBackPressed();
        });

        PP.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        tc.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, TermsAndConditionsActivity.class);
            startActivity(intent);
        });

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String installedVersion = pInfo.versionName;
            String latestVersion = BuildConfig.VERSION_NAME.trim(); // Fetching version from Gradle and trimming spaces

            appNameVersion.setText("Version: " + installedVersion);

            if (installedVersion.equals(latestVersion)) {
                appNameVersionDesp.setText("The Latest Version is Already Updated");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appNameVersion.setText("Version: N/A");
        }


        imageViewAbout.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
    }
}