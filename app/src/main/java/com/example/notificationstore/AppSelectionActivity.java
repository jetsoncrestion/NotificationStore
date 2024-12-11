package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
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
    private ImageView imageMenuActionBar, imageViewBack;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        imageMenuActionBar = findViewById(R.id.imageMenuActionBar);
        imageViewBack = findViewById(R.id.imageViewBack);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        confirmButton = findViewById(R.id.button);
        new LoadAppsTask().execute();

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
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

        imageMenuActionBar.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.second_menu, popupMenu.getMenu());

        MenuItem actionUpdateSelection = popupMenu.getMenu().findItem(R.id.action_update_selection);
        MenuItem actionViewDeleted = popupMenu.getMenu().findItem(R.id.action_view_deleted);

        // Apply custom styling
        applyPopupMenuItemStyle(actionUpdateSelection);
        applyPopupMenuItemStyle(actionViewDeleted);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_update_selection) {
                openAppSelectionActivity();
                return true;
            } else if (item.getItemId() == R.id.action_view_deleted) {
                Intent intent = new Intent(AppSelectionActivity.this, DeleteNotificationActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void applyPopupMenuItemStyle(MenuItem item) {
        SpannableString styledText = new SpannableString(item.getTitle());

        int textColor = getResources().getColor(R.color.headingText);
        int textSize = 20;  // In sp (scaled pixels)
        Typeface typeface = ResourcesCompat.getFont(this, R.font.roboto);

        styledText.setSpan(new ForegroundColorSpan(textColor), 0, styledText.length(), 0);
        styledText.setSpan(new StyleSpan(Typeface.BOLD), 0, styledText.length(), 0);
        styledText.setSpan(new AbsoluteSizeSpan(textSize, true), 0, styledText.length(), 0);
        //styledText.setSpan(new CustomTypefaceSpan(R.font.roboto, typeface), 0, styledText.length(), 0);

        item.setTitle(styledText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here.
        if (item.getItemId() == R.id.action_update_selection) {
            openAppSelectionActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<AppModel> getInstalledApps() {
        List<AppModel> apps = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);
        String currentAppPackageName = getPackageName();

        for (ApplicationInfo appInfo : installedApps) {
            String appName = packageManager.getApplicationLabel(appInfo).toString();

            if (appInfo.packageName.equals(currentAppPackageName)){
                continue;
            }
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {

                Drawable appIcon = appInfo.loadIcon(packageManager);
                apps.add(new AppModel(appName, appInfo.packageName, false, appIcon));
            }
        }
        return apps;
    }

    private void openAppSelectionActivity() {
        // Functionality to open App Selection Activity
        Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
        intent.putExtra("isRevisiting", true);
        startActivity(intent);
        finish();
    }

    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppModel>> {
        @Override
        protected List<AppModel> doInBackground(Void... voids) {
            return getInstalledApps();
        }

        @Override
        protected void onPostExecute(List<AppModel> apps) {
            super.onPostExecute(apps);
            appModels = apps;

            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            HashSet<String> selectedApps = (HashSet<String>) preferences.getStringSet("selectedApps", new HashSet<>());

            for (AppModel appModel : appModels) {
                if (selectedApps.contains(appModel.getPackageName())) {
                    appModel.setSelected(true);
                }
            }

            appAdapter = new AppAdapter(appModels, AppSelectionActivity.this);
            recyclerView.setAdapter(appAdapter);
        }
    }
}