package com.ratna.NotificationStore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.NotificationStore.Adapter.AppAdapter;
import com.ratna.NotificationStore.Model.AppModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppSelectionSecond extends AppCompatActivity {
    private AppAdapter appAdapter;
    private List<AppModel> appModels;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_selection_second);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isAppSelectionDone = preferences.getBoolean("isAppSelectionDone", false);
        boolean isRevisiting = getIntent().getBooleanExtra("isRevisiting", false);

        if (isAppSelectionDone && !isRevisiting) {
            Intent intent = new Intent(AppSelectionSecond.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        findViewById(R.id.button).setOnClickListener(v -> {
            List<String> selectedAppsList = appAdapter.getSelectedApps();
            Log.d("SelectedApps", "Selected apps: " + selectedAppsList);

            if (selectedAppsList.isEmpty()) {
                Toast.makeText(AppSelectionSecond.this, "Please select at least one app.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AppSelectionSecond.SavePreferencesTask(preferences, selectedAppsList).execute();
            Intent intent = new Intent(AppSelectionSecond.this, AppPermissionActivity.class);
            startActivity(intent);
            finish();
        });
        loadApps();
    }

    private void loadApps() {
        progressBar.setVisibility(View.VISIBLE);
        new AppSelectionSecond.LoadAppsTask().execute();
    }

    private List<AppModel> getInstalledApps() {
        List<AppModel> apps = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);
        String currentAppPackageName = getPackageName();

        for (ApplicationInfo appInfo : installedApps) {
            String appName = packageManager.getApplicationLabel(appInfo).toString();

            if (appInfo.packageName.equals(currentAppPackageName)) {
                continue;
            }
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
                Drawable appIcon = appInfo.loadIcon(packageManager);
                apps.add(new AppModel(appName, appInfo.packageName, false, appIcon));
            }
        }
        apps.sort((app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));
        return apps;
    }

    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppModel>> {
        @Override
        protected List<AppModel> doInBackground(Void... voids) {
            return getInstalledApps();
        }

        @Override
        protected void onPostExecute(List<AppModel> apps) {
            super.onPostExecute(apps);
            progressBar.setVisibility(View.GONE);
            appModels = apps;

            new AppSelectionSecond.LoadSelectedAppsTask().execute();
        }
    }

    private class LoadSelectedAppsTask extends AsyncTask<Void, Void, HashSet<String>> {
        @Override
        protected HashSet<String> doInBackground(Void... voids) {
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            return (HashSet<String>) preferences.getStringSet("selectedApps", new HashSet<>());
        }

        @Override
        protected void onPostExecute(HashSet<String> selectedApps) {
            super.onPostExecute(selectedApps);
            for (AppModel appModel : appModels) {
                if (selectedApps.contains(appModel.getPackageName())) {
                    appModel.setSelected(true);
                }
            }

            appAdapter = new AppAdapter(appModels, AppSelectionSecond.this);
            recyclerView.setAdapter(appAdapter);
        }
    }

    private class SavePreferencesTask extends AsyncTask<Void, Void, Void> {
        private SharedPreferences preferences;
        private List<String> selectedAppsList;

        public SavePreferencesTask(SharedPreferences preferences, List<String> selectedAppsList) {
            this.preferences = preferences;
            this.selectedAppsList = selectedAppsList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet("selectedApps", new HashSet<>(selectedAppsList));
            editor.putBoolean("isAppSelectionDone", true);
            editor.apply();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(AppSelectionSecond.this, AppPermissionActivity.class);
            intent.putStringArrayListExtra("selectedApps", new ArrayList<>(selectedAppsList));
            startActivity(intent);
            finish();
        }
    }
}