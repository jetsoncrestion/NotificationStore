package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Adapter.AppAdapter;
import com.example.notificationstore.Model.AppModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppSelectionActivity extends AppCompatActivity {
    private AppAdapter appAdapter;
    private List<AppModel> appModels;
    private ImageView imageViewBack;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExecutorService executorService;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        imageViewBack = findViewById(R.id.imageViewBack);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        executorService = Executors.newSingleThreadExecutor();

        preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isAppSelectionDone = preferences.getBoolean("isAppSelectionDone", false);
        boolean isRevisiting = getIntent().getBooleanExtra("isRevisiting", false);

        if (isAppSelectionDone && !isRevisiting) {
            Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        imageViewBack.setOnClickListener(v -> {
            Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.button).setOnClickListener(v -> {
            List<String> selectedAppsList = appAdapter.getSelectedApps();
            if (selectedAppsList.isEmpty()) {
                Toast.makeText(AppSelectionActivity.this, "Please select at least one app.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet("selectedApps", new HashSet<>(selectedAppsList));
            editor.putBoolean("isAppSelectionDone", true);
            editor.apply();

            Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
            intent.putStringArrayListExtra("selectedApps", new ArrayList<>(selectedAppsList));
            startActivity(intent);
            finish();
        });

        loadApps();
    }

    private void loadApps() {
        progressBar.setVisibility(View.VISIBLE);

        // Check if the installed apps are cached in SharedPreferences
        String cachedApps = preferences.getString("cachedApps", null);
        if (cachedApps != null) {
            // Parse cached data and load it directly
            List<AppModel> cachedAppModels = parseCachedApps(cachedApps);
            updateRecyclerView(cachedAppModels);
        } else {
            // If no cached data, load the apps from scratch
            executorService.submit(() -> {
                List<AppModel> apps = getInstalledApps();
                // Cache the loaded apps
                cacheInstalledApps(apps);
                // Update the UI on the main thread
                new Handler(Looper.getMainLooper()).post(() -> updateRecyclerView(apps));
            });
        }
    }

    private void updateRecyclerView(List<AppModel> apps) {
        progressBar.setVisibility(View.GONE);
        appModels = apps;

        HashSet<String> selectedApps = (HashSet<String>) preferences.getStringSet("selectedApps", new HashSet<>());
        for (AppModel appModel : appModels) {
            if (selectedApps.contains(appModel.getPackageName())) {
                appModel.setSelected(true);
            }
        }

        appAdapter = new AppAdapter(appModels, AppSelectionActivity.this);
        recyclerView.setAdapter(appAdapter);
    }

    private void cacheInstalledApps(List<AppModel> apps) {
        StringBuilder appsStringBuilder = new StringBuilder();
        for (AppModel appModel : apps) {
            appsStringBuilder.append(appModel.getPackageName()).append(",");
        }
        preferences.edit().putString("cachedApps", appsStringBuilder.toString()).apply();
    }

    private List<AppModel> parseCachedApps(String cachedApps) {
        List<AppModel> apps = new ArrayList<>();
        String[] appPackages = cachedApps.split(",");
        PackageManager packageManager = getPackageManager();

        for (String appPackage : appPackages) {
            try {
                // Get application info for the package name
                ApplicationInfo appInfo = packageManager.getApplicationInfo(appPackage, 0);
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                Drawable appIcon = appInfo.loadIcon(packageManager);  // Get the app's icon

                // Create the AppModel with real app name and icon
                apps.add(new AppModel(appName, appPackage, false, appIcon));
            } catch (PackageManager.NameNotFoundException e) {
                // Handle case where the app package is not found
                Log.e("AppSelection", "Package not found: " + appPackage);
            }
        }

        return apps;
    }

    private List<AppModel> getInstalledApps() {
        List<AppModel> apps = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);
        String currentAppPackageName = getPackageName();

        for (ApplicationInfo appInfo : installedApps) {
            String appName = packageManager.getApplicationLabel(appInfo).toString();

            // Filter out the current app and apps that don't have launch intents
            if (appInfo.packageName.equals(currentAppPackageName)) continue;
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) == null) continue;

            Drawable appIcon = appInfo.loadIcon(packageManager);
            apps.add(new AppModel(appName, appInfo.packageName, false, appIcon));
        }
        return apps;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}