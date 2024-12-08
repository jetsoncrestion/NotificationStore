package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Adapter.AppAdapter;
import com.example.notificationstore.Model.AppModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity {
    private AppAdapter appAdapter;
    private List<AppModel> appModels;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isAppSelectionDone = preferences.getBoolean("isAppSelectionDone", false);

        boolean isRevisiting = getIntent().getBooleanExtra("isRevisiting", false);

        if (isAppSelectionDone && !isRevisiting) {
            Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        confirmButton = findViewById(R.id.button);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appModels = getInstalledApps();

        HashSet<String> selectedApps = (HashSet<String>) preferences.getStringSet("selectedApps", new HashSet<>());
        for (AppModel appModel : appModels) {
            if (selectedApps.contains(appModel.getPackageName())) {
                appModel.setSelected(true);
            }
        }

        appAdapter = new AppAdapter(appModels, this);
        recyclerView.setAdapter(appAdapter);

        confirmButton.setOnClickListener(v -> {
            List<String> selectedAppsList = appAdapter.getSelectedApps();
            Log.d("SelectedApps", "Selected apps: " + selectedAppsList);

            if (selectedAppsList.isEmpty()) {
                Toast.makeText(AppSelectionActivity.this, "Please select at least one app.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use the existing preferences to save the selected apps and the selection completion flag
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet("selectedApps", new HashSet<>(selectedAppsList));
            editor.putBoolean("isAppSelectionDone", true); // Save that selection is done
            editor.apply();

            // Navigate to the main activity
            Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
            intent.putStringArrayListExtra("selectedApps", new ArrayList<>(selectedAppsList));
            startActivity(intent);
            finish();
        });

    }

    private List<AppModel> getInstalledApps() {
        List<AppModel> apps = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);

        for (ApplicationInfo appInfo : installedApps) {
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {

                Drawable appIcon = appInfo.loadIcon(packageManager);
                apps.add(new AppModel(appName, appInfo.packageName, false, appIcon));
            }
        }
        return apps;
    }
}