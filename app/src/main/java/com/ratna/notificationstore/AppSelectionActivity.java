package com.ratna.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Adapter.AppAdapter;
import com.ratna.notificationstore.Model.AppModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity {
    private AppAdapter appAdapter;
    private List<AppModel> appModels;
    private ImageView imageViewBack;
    private CardView cardView9;
    private SearchView searchView;
    private ImageView imageViewSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private boolean isSelectAllEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        imageViewBack = findViewById(R.id.imageViewBack);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        cardView9 = findViewById(R.id.cardView9);
        searchView = findViewById(R.id.searchView);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isAppSelectionDone = preferences.getBoolean("isAppSelectionDone", false);
        boolean isRevisiting = getIntent().getBooleanExtra("isRevisiting", false);
        isSelectAllEnabled = preferences.getBoolean("isSelectAllEnabled", false);

        Switch toggleSwitch = cardView9.findViewById(R.id.toggleSwitch);
        toggleSwitch.setChecked(isSelectAllEnabled);
        updateSwitchColors(toggleSwitch, isSelectAllEnabled);

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (appAdapter == null) {
                Toast.makeText(this, "Please wait for the apps to load.", Toast.LENGTH_SHORT).show();
                toggleSwitch.setChecked(!isChecked); // Revert the state
                return;
            }
            isSelectAllEnabled = isChecked;
            updateSwitchColors(toggleSwitch, isChecked); // Update colors on toggle
            updateSelectAllState(isChecked);
            saveSelectAllState(preferences, isChecked);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                appAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                appAdapter.getFilter().filter(newText);
                return false;
            }
        });

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

        imageViewSearch.setOnClickListener(v -> {
            toggleSearchView(searchView.getVisibility() != View.VISIBLE);
        });

        findViewById(R.id.button).setOnClickListener(v -> {
            List<String> selectedAppsList = appAdapter.getSelectedApps();
            Log.d("SelectedApps", "Selected apps: " + selectedAppsList);

            if (selectedAppsList.isEmpty()) {
                Toast.makeText(AppSelectionActivity.this, "Please select at least one app.", Toast.LENGTH_SHORT).show();
                return;
            }

            new SavePreferencesTask(preferences, selectedAppsList).execute();
            Intent intent = new Intent(AppSelectionActivity.this, AppPermissionActivity.class);
            startActivity(intent);
        });

        loadApps();
    }

    private void updateSwitchColors(Switch toggleSwitch, boolean isChecked) {
        int thumbOnColor = ContextCompat.getColor(this, R.color.switch_thumb_on);
        int thumbOffColor = ContextCompat.getColor(this, R.color.switch_thumb_off);
        int trackOnColor = ContextCompat.getColor(this, R.color.switch_track_on);
        int trackOffColor = ContextCompat.getColor(this, R.color.switch_track_off);

        toggleSwitch.setThumbTintList(ColorStateList.valueOf(isChecked ? thumbOnColor : thumbOffColor));
        toggleSwitch.setTrackTintList(ColorStateList.valueOf(isChecked ? trackOnColor : trackOffColor));
    }

    private void loadApps() {
        progressBar.setVisibility(View.VISIBLE);
        new LoadAppsTask().execute();
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

            new LoadSelectedAppsTask().execute();
        }
    }

    private void updateSelectAllState(boolean isChecked) {
        if (appAdapter == null) {
            return; // Do nothing if appAdapter is not initialized
        }
        for (AppModel appModel : appModels) {
            appModel.setSelected(isChecked);
        }
        appAdapter.notifyDataSetChanged();
    }

    private void saveSelectAllState(SharedPreferences preferences, boolean isChecked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isSelectAllEnabled", isChecked);
        editor.apply();
    }

    private class LoadSelectedAppsTask extends AsyncTask<Void, Void, HashSet<String>> {
        @Override
        protected HashSet<String> doInBackground(Void... voids) {
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            HashSet<String> selectedApps = (HashSet<String>) preferences.getStringSet("selectedApps", new HashSet<>());

            selectedApps.add("com.facebook.katana"); //Facebook
            selectedApps.add("com.google.android.apps.messaging"); //default phone message app
            selectedApps.add("com.android.dialer"); //default phone dialer app
            selectedApps.add("com.instagram.android"); //Instagram
            selectedApps.add("com.whatsapp"); //WhatsApp
            selectedApps.add("np.com.nepalipatro"); //NepaliPatro
            selectedApps.add("com.twitter.android"); //Twitter
            selectedApps.add("com.linkedin.android"); //LinkedIn
            selectedApps.add("com.android.chrome"); //Chrome
            selectedApps.add("com.google.android.googlequicksearchbox"); //Google
            selectedApps.add("com.facebook.orca"); //Messenger

            return selectedApps;
        }

        @Override
        protected void onPostExecute(HashSet<String> selectedApps) {
            super.onPostExecute(selectedApps);
            for (AppModel appModel : appModels) {
                if (selectedApps.contains(appModel.getPackageName())) {
                    appModel.setSelected(true);
                }
            }

            boolean allSelected = !appModels.isEmpty() && appModels.stream().allMatch(AppModel::isSelected);
            isSelectAllEnabled = allSelected;
            Switch toggleSwitch = cardView9.findViewById(R.id.toggleSwitch);
            toggleSwitch.setChecked(allSelected);

            appAdapter = new AppAdapter(appModels, AppSelectionActivity.this);
            recyclerView.setAdapter(appAdapter);
        }
    }

    private void toggleSearchView(boolean show) {
        if (show) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setAlpha(0f);
            searchView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            searchView.requestFocus();
        } else {
            searchView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        searchView.setVisibility(View.GONE);
                        searchView.setQuery("", false);
                    })
                    .start();
            searchView.clearFocus();
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
            Intent intent = new Intent(AppSelectionActivity.this, AppPermissionActivity.class);
            intent.putStringArrayListExtra("selectedApps", new ArrayList<>(selectedAppsList));
            startActivity(intent);
            finish();
        }
    }
}
