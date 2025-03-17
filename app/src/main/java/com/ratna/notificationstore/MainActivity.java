package com.ratna.notificationstore;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ratna.notificationstore.Adapter.NotificationAdapter;
import com.ratna.notificationstore.Model.NotificationModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "NotificationStorePrefs";
    private static final String DEVICE_ID_KEY = "DeviceID";
    private static final String AUTO_START_PROMPT_SHOWN = "auto_start_prompt_shown";
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private NotificationAdapter notificationAdapter;
    private static List<NotificationModel> notificationModels = new ArrayList<>();

    public static List<NotificationModel> getNotificationModels() {
        return notificationModels;
    }

    private String deviceId;
    private ImageView imageViewSetting, imageViewSearch, imageViewFilter, imageViewMenu;
    private SearchView searchView;
    private AppUpdateManager appUpdateManager;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private boolean isUpdateRequired = false;
    private int thumbOnColor, thumbOffColor, trackOnColor, trackOffColor;
    private TextView textViewFromCalender, textViewUntilCalender;
    private Handler updateCheckHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("app_update_notification");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Log.d(TAG, "FCM Tokens: " + token);
            } else {
                Log.e(TAG, "Failed to get FCM token");
            }
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        rebindNotificationListenerService();
        if (isMIUI() && !prefs.getBoolean(AUTO_START_PROMPT_SHOWN, false)) {
            enableAutostart();
        }

        appUpdateManager = AppUpdateManagerFactory.create(this);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Log.d(TAG, "Update flow completed successfully!");
                Toast.makeText(this, R.string.app_updated_successfully, Toast.LENGTH_SHORT).show();
                clearUpdateNotification();
            } else {
                Log.e(TAG, "Update flow failed! Result code: " + result.getResultCode());
                Toast.makeText(this, R.string.app_update_failed_please_update_the_app_to_continue, Toast.LENGTH_SHORT).show();
                finish(); // Close the app if update fails
            }
        });

        createNotificationChannel();
        //checkForAppUpdate();
        startPeriodicUpdateCheck();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.white));
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshNotifications();
        });

        textViewFromCalender = new TextView(this);
        textViewUntilCalender = new TextView(this);
        imageViewSetting = findViewById(R.id.imageViewSetting);
        searchView = findViewById(R.id.searchView);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        imageViewFilter = findViewById(R.id.imageViewFilter);
        imageViewMenu = findViewById(R.id.imageViewMenu);

        imageViewSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Setting.class);
            startActivity(intent);
        });

        imageViewSearch.setOnClickListener(v -> {
            toggleSearchView(searchView.getVisibility() != View.VISIBLE);
        });

        imageViewMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, imageViewMenu);
            for (NotificationModel model : notificationModels) {
                if (model != null && model.getAppName() != null && !TextUtils.isEmpty(model.getAppName())) {
                    MenuItem menuItem = popupMenu.getMenu().findItem(model.getAppName().hashCode());
                    if (menuItem == null) {
                        popupMenu.getMenu().add(Menu.NONE, model.getAppName().hashCode(), Menu.NONE, model.getAppName()).setVisible(true);
                    }
                }
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                String selectedApp = item.getTitle().toString();
                Intent intent = new Intent(MainActivity.this, FilteredNotificationActivity.class);
                intent.putExtra("selectedApp", selectedApp);
                startActivity(intent);
                return true;
            });

            try {
                Field popupField = PopupMenu.class.getDeclaredField("mPopup");
                popupField.setAccessible(true);
                Object popupHelper = popupField.get(popupMenu);
                if (popupHelper instanceof ListPopupWindow) {
                    ListPopupWindow popupWindow = (ListPopupWindow) popupHelper;
                    popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.background));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popupMenu.show();
        });

        imageViewFilter.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.custom_filter_alert_dialog, null);
            TextView textViewCancel = dialogView.findViewById(R.id.textViewCancel);
            TextView textViewAccept = dialogView.findViewById(R.id.textViewAccept);
            Switch toggleSwitch = dialogView.findViewById(R.id.toggleSwitch);
            Switch toggleSwitchSecond = dialogView.findViewById(R.id.toggleSwitchSecond);
            Spinner appSpinner = dialogView.findViewById(R.id.spinner2);
            TextView textViewFrom = dialogView.findViewById(R.id.textViewFrom);
            TextView textViewUntil = dialogView.findViewById(R.id.textViewUntil);
            TextView textViewFromCalender = dialogView.findViewById(R.id.textViewFromCalender);
            TextView textViewUntilCalender = dialogView.findViewById(R.id.textViewUntilCalender);

            thumbOnColor = ContextCompat.getColor(this, R.color.switch_thumb_on);
            thumbOffColor = ContextCompat.getColor(this, R.color.switch_thumb_off);
            trackOnColor = ContextCompat.getColor(this, R.color.switch_track_on);
            trackOffColor = ContextCompat.getColor(this, R.color.switch_track_off);

            appSpinner.setVisibility(View.GONE);
            textViewFrom.setVisibility(View.GONE);
            textViewUntil.setVisibility(View.GONE);
            textViewFromCalender.setVisibility(View.GONE);
            textViewUntilCalender.setVisibility(View.GONE);

            List<String> appNames = new ArrayList<>();
            appNames.add("All Apps");
            for (NotificationModel model : notificationModels) {
                if (model != null && model.getAppName() != null && !appNames.contains(model.getAppName())) {
                    appNames.add(model.getAppName());
                }
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, appNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            appSpinner.setAdapter(spinnerAdapter);
            Log.d(TAG, "App Names: " + appNames);
            appSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedApp = appSpinner.getSelectedItem().toString();
                    Log.d(TAG, "Selected app: " + selectedApp);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            toggleSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                updateSwitchColors(toggleSwitch, isChecked);
                if (isChecked) {
                    appSpinner.setVisibility(View.VISIBLE);
                } else {
                    appSpinner.setVisibility(View.GONE);
                }
            }));

            toggleSwitchSecond.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                updateSwitchSecondColors(toggleSwitchSecond, isChecked);
                if (isChecked) {
                    textViewFrom.setVisibility(View.VISIBLE);
                    textViewFromCalender.setVisibility(View.VISIBLE);
                    textViewUntil.setVisibility(View.VISIBLE);
                    textViewUntilCalender.setVisibility(View.VISIBLE);
                } else {
                    textViewFrom.setVisibility(View.GONE);
                    textViewFromCalender.setVisibility(View.GONE);
                    textViewUntil.setVisibility(View.GONE);
                    textViewUntilCalender.setVisibility(View.GONE);
                }
            }));

            appSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedApp = appSpinner.getSelectedItem().toString();
                    Log.d(TAG, "Selected app: " + selectedApp);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            ArrayAdapter<String> spinnerAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, appNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            appSpinner.setAdapter(spinnerAdapter);
            textViewFromCalender.setOnClickListener(v1 -> showDatePickerWithTime(textViewFromCalender));
            textViewUntilCalender.setOnClickListener(v1 -> showDatePickerWithTime(textViewUntilCalender));
            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create();
            alertDialog.setOnShowListener(dialogInterface -> {
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.RECTANGLE);
                    drawable.setCornerRadius(60);
                    drawable.setColor(Color.WHITE);
                    window.setBackgroundDrawable(drawable);
                }
            });

            textViewCancel.setOnClickListener(v1 -> alertDialog.dismiss());
            textViewAccept.setOnClickListener(v1 -> {
                String selectedApp = appSpinner.getSelectedItem().toString();
                String startDate = textViewFromCalender.getText().toString();
                String endDate = textViewUntilCalender.getText().toString();
                Log.d(TAG, "Filter accepted: App - " + selectedApp + ", Start Date - " + startDate + ", End Date - " + endDate);
                filterNotificationsByAppAndDate(selectedApp, startDate, endDate);
                alertDialog.dismiss();
            });
            alertDialog.show();
        });

        deviceId = DeviceUtil.getOrGenerateDeviceId(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    String startDate = textViewFromCalender.getText().toString();
                    String endDate = textViewUntilCalender.getText().toString();
                    filterNotifications(query, startDate, endDate);
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String startDate = textViewFromCalender.getText().toString();
                String endDate = textViewUntilCalender.getText().toString();
                filterNotifications(newText, startDate, endDate);
                return true;
            }
        });

        if (!isNotificationListenerEnabled()) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        RecyclerView recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationModels = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationModels);
        recyclerView.setAdapter(notificationAdapter);
        loadNotificationFromLocalStorage();
        autoDeleteOldNotificationsLocal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rebindNotificationListenerService();
    }

    private void enableAutostart() {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                startActivity(intent);
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(AUTO_START_PROMPT_SHOWN, true);
                editor.apply();
            } catch (Exception e) {
                Log.e(TAG, "Failed to open Autostart settings", e);
                Toast.makeText(this, "Please enable Autostart for this app manually ", Toast.LENGTH_LONG).show();
            }
        }
    private void rebindNotificationListenerService() {
        Intent serviceIntent = new Intent(this, MyNotificationListenerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "NotificationListenerService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "NotificationListenerService disconnected");
        }
    };

    private boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    private String getSystemProperty (String key) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            return (String) get.invoke(null, key);
        } catch (Exception e) {
            return null;
        }
    }

    private void refreshNotifications() {
        new Handler().postDelayed(() -> {
            notificationModels.clear();
            loadNotificationFromLocalStorage();
            notificationAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }, 2000);
    }

    private void startPeriodicUpdateCheck() {
        updateCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForAppUpdateBeforeNotification();
                updateCheckHandler.postDelayed(this, TimeUnit.DAYS.toMillis(1));
            }
        }, TimeUnit.DAYS.toMillis(1));
    }

    private void checkForAppUpdateBeforeNotification() {
        Task<AppUpdateInfo> task = appUpdateManager.getAppUpdateInfo();
        task.addOnSuccessListener(appUpdateInfo -> {
           if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
               showUpdateNotification();
           } else {
               Log.d(TAG, "No update available. Skipping notification.");
           }
        }).addOnFailureListener(e -> {
            Log.e(TAG,"Update Check failed: " +e.getMessage());
        });
    }

//    private void checkForAppUpdate() {
//        Task<AppUpdateInfo> task = appUpdateManager.getAppUpdateInfo();
//
//        task.addOnSuccessListener(appUpdateInfo -> {
//            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
//                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
//                    isUpdateRequired = true;
//                    startUpdateFlow(appUpdateManager, appUpdateInfo);
//                    showUpdateNotification();
//                } else {
//                    Log.d(TAG, "Immediate update not allowed.");
//                    isUpdateRequired = false;
//                }
//            } else {
//                isUpdateRequired = false;
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Update check failed: " + e.getMessage());
//            isUpdateRequired = false;
//        });
//    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "App Update Channel";
            String description = "Channel for app update notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("app_update_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showUpdateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "app_update_channel")
                .setSmallIcon(R.drawable._92_log_05) // Replace with your notification icon
                .setContentTitle("App Update Available")
                .setContentText("A new version of the app is available. Tap to update.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private void clearUpdateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
    }

    private void startUpdateFlow(AppUpdateManager appUpdateManager, AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
        } catch (Exception e) {
            Log.e(TAG, "Update flow error: " + e.getMessage());
            Toast.makeText(this, "Update failed. Restart the app.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateSwitchColors(Switch toggleSwitch, boolean isChecked) {
        if (isChecked) {
            toggleSwitch.setThumbTintList(ColorStateList.valueOf(thumbOnColor));
            toggleSwitch.setTrackTintList(ColorStateList.valueOf(trackOnColor));
        } else {
            toggleSwitch.setThumbTintList(ColorStateList.valueOf(thumbOffColor));
            toggleSwitch.setTrackTintList(ColorStateList.valueOf(trackOffColor));
        }
    }

    private void updateSwitchSecondColors(Switch toggleSwitchSecond, boolean isChecked) {
        if (isChecked) {
            toggleSwitchSecond.setThumbTintList(ColorStateList.valueOf(thumbOnColor));
            toggleSwitchSecond.setTrackTintList(ColorStateList.valueOf(trackOnColor));
        } else {
            toggleSwitchSecond.setThumbTintList(ColorStateList.valueOf(thumbOffColor));
            toggleSwitchSecond.setTrackTintList(ColorStateList.valueOf(trackOffColor));
        }
    }

    private void filterNotificationsByAppAndDate(String selectedApp, String startDate, String endDate) {
        Log.d(TAG, "filterNotificationsByAppAndDate called with app: " + selectedApp + ", startDate: " + startDate + ", endDate: " + endDate);
        List<NotificationModel> filteredList = new ArrayList<>();
        long start = Long.MIN_VALUE;
        long end = Long.MAX_VALUE;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            if (!TextUtils.isEmpty(startDate)) {
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(dateFormat.parse(startDate));
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                startCalendar.set(Calendar.MINUTE, 0);
                startCalendar.set(Calendar.SECOND, 0);
                startCalendar.set(Calendar.MILLISECOND, 0);
                start = startCalendar.getTimeInMillis();
            }
            if (!TextUtils.isEmpty(endDate)) {
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(dateFormat.parse(endDate));
                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endCalendar.set(Calendar.MINUTE, 59);
                endCalendar.set(Calendar.SECOND, 59);
                endCalendar.set(Calendar.MILLISECOND, 999);
                end = endCalendar.getTimeInMillis();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
        }

        for (NotificationModel model : notificationModels) {
            if (model != null) {
                boolean matchesApp = "All Apps".equals(selectedApp) || (model.getAppName() != null && model.getAppName().equalsIgnoreCase(selectedApp));
                Log.d("Date Filter", "Notification timestamp: " + model.getTimeStamp() + ", Start date: " + startDate + ", End date: " + endDate);
                boolean matchesDate = model.getTimeStamp() >= start && model.getTimeStamp() <= end;
                if (matchesApp && matchesDate) {
                    filteredList.add(model);
                }
            }
        }
        Log.d(TAG, "Filtered notifications count: " + filteredList.size());
        notificationAdapter.updateData(filteredList);
    }

    private void toggleSearchView(boolean show) {
        if (show) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setAlpha(0f);
            searchView.animate().alpha(1f).setDuration(300).start();
            searchView.requestFocus();
        } else {
            searchView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                searchView.setVisibility(View.GONE);
                searchView.setQuery("", false); // Reset query
            }).start();
            searchView.clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void loadNotificationFromLocalStorage() {
        File directory = new File(getFilesDir(), "NotificationStores");
        File file = new File(directory, "notification.json");
        if (!file.exists()) {
            Log.d(TAG, "No notification found in local storage");
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();
            String content = jsonBuilder.toString();
            if (!content.isEmpty()) {
                JSONArray jsonArray = new JSONArray(content);
                notificationModels.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    NotificationModel model = new NotificationModel();
                    model.setAppName(obj.optString("appName"));
                    model.setNotificationHeading(obj.optString("notificationHeading"));
                    model.setNotificationContent(obj.optString("notificationContent"));
                    model.setTimeStamp(obj.optLong("timestamp"));
                    String appIconBase64 = shortenBase64(obj.optString("appIconBase64"));
                    model.setAppIconBase64(appIconBase64);
                    model.setPackageName(obj.optString("packageName"));
                    notificationModels.add(model);
                }
                List<NotificationModel> reversedList = new ArrayList<>();
                for (int i = notificationModels.size() - 1; i >= 0; i--) {
                    reversedList.add(notificationModels.get(i));
                }
                notificationModels.clear();
                notificationModels.addAll(reversedList);
                notificationAdapter.notifyDataSetChanged();
                Log.d(TAG, "Notification loaded form local storage: " + notificationModels.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading notification form local storage: " + e.getMessage());
        }
    }

    private String shortenBase64(String base64String) {
        if (base64String == null || base64String.length() <= 10) {
            return base64String; // Return the original string if it's already short or null
        }
        return base64String.substring(0, 10); // Return the first 10 characters
    }

    private void autoDeleteOldNotificationsLocal() {
        long currentTime = System.currentTimeMillis();
        long ninetyDaysInMillis = 90L * 24 * 60 * 60 * 1000;
        File directory = new File(getFilesDir(), "NotificationStores");
        File file = new File(directory, "notification.json");
        if (!file.exists()) {
            Log.d(TAG, "No notification found in local storage");
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();
            String content = jsonBuilder.toString();
            JSONArray jsonArray = new JSONArray(content);
            JSONArray newArray = new JSONArray();
            List<NotificationModel> updatedModels = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                long timestamp = obj.optLong("timestamp");
                if (timestamp >= currentTime - ninetyDaysInMillis) {
                    newArray.put(obj);
                    NotificationModel model = new NotificationModel();
                    model.setAppName(obj.optString("appName"));
                    model.setNotificationHeading(obj.optString("notificationHeading"));
                    model.setNotificationContent(obj.optString("notificationContent"));
                    model.setTimeStamp(obj.optLong("timestamp"));
                    model.setAppIconBase64(obj.optString("appIconBase64"));
                    model.setPackageName(obj.optString("packageName"));
                    updatedModels.add(model);
                }
            }
            FileWriter writer = new FileWriter(file);
            writer.write(newArray.toString());
            writer.close();
            notificationModels.clear();
            List<NotificationModel> reversedList = new ArrayList<>();
            for (int i = updatedModels.size() - 1; i >= 0; i--) {
                reversedList.add(updatedModels.get(i));
            }
            notificationModels.addAll(reversedList);
            notificationAdapter.notifyDataSetChanged();
            Log.d(TAG, "Auto-deleted old notifications. Updated count: " + notificationModels.size());
        } catch (Exception e) {
            Log.e(TAG, "Error auto-deleting old notifications: " + e.getMessage());
        }
    }

    private boolean isNotificationListenerEnabled() {
        String enabledListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return enabledListeners != null && enabledListeners.contains(getPackageName());
    }

    private void showDatePickerWithTime(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            showTimePicker(textView, year1, month1, dayOfMonth);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(TextView textView, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String dateTime = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hourOfDay, minuteOfHour);
            textView.setText(dateTime);
            Log.d(TAG, "Date and time selected: " + dateTime);
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void filterNotifications(String query, String startDate, String endDate) {
        List<NotificationModel> filteredList = new ArrayList<>();
        long start = Long.MIN_VALUE;
        long end = Long.MAX_VALUE;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            if (!TextUtils.isEmpty(startDate)) {
                start = dateFormat.parse(startDate).getTime();
            }
            if (!TextUtils.isEmpty(endDate)) {
                end = dateFormat.parse(endDate).getTime();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date and time: " + e.getMessage());
        }

        for (NotificationModel model : notificationModels) {
            if (model != null) {
                boolean matchesQuery = TextUtils.isEmpty(query) || (model.getAppName() != null && model.getAppName().toLowerCase().contains(query.toLowerCase())) || (model.getNotificationContent() != null && model.getNotificationContent().toLowerCase().contains(query.toLowerCase())) || (model.getNotificationHeading() != null && model.getNotificationHeading().toLowerCase().contains(query.toLowerCase()));
                boolean matchesDate = model.getTimeStamp() >= start && model.getTimeStamp() <= end;
                if (matchesQuery && matchesDate) {
                    filteredList.add(model);
                }
            }
        }

        notificationAdapter.updateData(filteredList);
        Log.d(TAG, "Filtered notifications count: " + filteredList.size());
    }
}