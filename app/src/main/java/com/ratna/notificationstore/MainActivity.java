package com.ratna.notificationstore;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Adapter.NotificationAdapter;
import com.ratna.notificationstore.Model.NotificationModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "NotificationStorePrefs";
    private static final String DEVICE_ID_KEY = "DeviceID";

    private Context context;

    private NotificationAdapter notificationAdapter;
    private static List<NotificationModel> notificationModels = new ArrayList<>();
    public static List<NotificationModel> getNotificationModels() {
        return notificationModels;
    }
    private String deviceId;
    TextView textViewNothingFound;
    private ImageView imageViewSetting, imageViewSearch, imageViewFilter, imageViewMenu;
    private SearchView searchView;
    private int thumbOnColor, thumbOffColor, trackOnColor, trackOffColor;
    private TextView textViewFromCalender, textViewUntilCalender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewFromCalender = new TextView(this);
        textViewUntilCalender = new TextView(this);

        FirebaseApp.initializeApp(this);
        imageViewSetting = findViewById(R.id.imageViewSetting);
        searchView = findViewById(R.id.searchView);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        textViewNothingFound = findViewById(R.id.textViewNothingFound);
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
            Context wrapper = new ContextThemeWrapper(MainActivity.this, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(wrapper, imageViewMenu);

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
                if (isChecked){
                    appSpinner.setVisibility(View.VISIBLE);
                } else {
                    appSpinner.setVisibility(View.GONE);
                }
            }));

            toggleSwitchSecond.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                updateSwitchSecondColors(toggleSwitchSecond, isChecked);
                if (isChecked){
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

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

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
        loadNotificationsFromFirebase();
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

        // Parse start and end dates
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
                boolean matchesApp = "All Apps".equals(selectedApp) ||
                        (model.getAppName() != null && model.getAppName().equalsIgnoreCase(selectedApp));

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
                        searchView.setQuery("", false); // Reset query
                    })
                    .start();
            searchView.clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_update_selection) {
            Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
            intent.putExtra("isRevisiting", true);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                boolean matchesQuery = TextUtils.isEmpty(query) ||
                        (model.getAppName() != null && model.getAppName().toLowerCase().contains(query.toLowerCase())) ||
                        (model.getNotificationContent() != null && model.getNotificationContent().toLowerCase().contains(query.toLowerCase())) ||
                        (model.getNotificationHeading() != null && model.getNotificationHeading().toLowerCase().contains(query.toLowerCase()));

                boolean matchesDate = model.getTimeStamp() >= start && model.getTimeStamp() <= end;

                if (matchesQuery && matchesDate) {
                    filteredList.add(model);
                }
            }
        }

        notificationAdapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            textViewNothingFound.setVisibility(View.VISIBLE);
        } else {
            textViewNothingFound.setVisibility(View.GONE);
        }
        Log.d(TAG, "Filtered notifications count: " + filteredList.size());
    }

    private void loadNotificationsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("notifications");

        databaseReference.orderByChild("timestamp").limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationModels.clear();

                if (!snapshot.exists()) {
                    Log.d(TAG, "No notifications found for device: " + deviceId);
                    return;
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);
                    if (notificationModel != null) {
                        notificationModel.setUniqueKey(dataSnapshot.getKey());
                        notificationModels.add(notificationModel);
                    }
                }

                List<NotificationModel> reversedList = new ArrayList<>();
                for (int i = notificationModels.size() - 1; i >= 0; i--) {
                    reversedList.add(notificationModels.get(i));
                }

                notificationModels.clear();
                notificationModels.addAll(reversedList);

                notificationAdapter.notifyDataSetChanged();
                Log.d(TAG, "Notifications loaded: " + notificationModels.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load notifications: " + error.getMessage());
            }
        });
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    showTimePicker(textView, year1, month1, dayOfMonth);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(TextView textView, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String dateTime = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hourOfDay, minuteOfHour);
                    textView.setText(dateTime);
                    Log.d(TAG, "Date and time selected: " + dateTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }
}