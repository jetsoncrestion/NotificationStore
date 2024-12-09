package com.example.notificationstore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.fonts.FontFamily;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Adapter.NotificationAdapter;
import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "NotificationStorePrefs";
    private static final String DEVICE_ID_KEY = "DeviceID";

    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationModels;
    private TextView noItemsTextView;
    private SearchView searchView;
    private ImageView imageMenuActionBar;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        searchView = findViewById(R.id.searchView);
        noItemsTextView = findViewById(R.id.noItemsTextView);
        imageMenuActionBar = findViewById(R.id.imageMenuActionBar);

        deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotifications(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotifications(newText);
                return false;
            }
        });

        imageMenuActionBar.setOnClickListener(v -> {
            showPopupMenu(v);
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

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popupMenu.getMenu());

        MenuItem actionUpdateSelection = popupMenu.getMenu().findItem(R.id.action_update_selection);
        MenuItem actionViewDeleted = popupMenu.getMenu().findItem(R.id.action_view_deleted);

        // Apply custom styling
        applyPopupMenuItemStyle(actionUpdateSelection);
        applyPopupMenuItemStyle(actionViewDeleted);

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_update_selection) {
                openAppSelectionActivity();
                return true;
            } else if (item.getItemId() == R.id.action_view_deleted) {
                Intent intent = new Intent(MainActivity.this, DeleteNotificationActivity.class);
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

    private void filterNotifications(String newText) {
        List<NotificationModel> filteredList = new ArrayList<>();

        if (!TextUtils.isEmpty(newText)) {
            for (NotificationModel model : notificationModels) {
                if (model != null &&
                        model.getAppName() != null &&
                        model.getNotificationContent() != null &&
                        (model.getAppName().toLowerCase().contains(newText.toLowerCase()) ||
                                model.getNotificationContent().toLowerCase().contains(newText.toLowerCase()))) {
                    filteredList.add(model);
                }
            }
        } else {
            filteredList.addAll(notificationModels);
        }

        if (filteredList.isEmpty()) {
            findViewById(R.id.noItemsTextView).setVisibility(View.VISIBLE); // Show the message
        } else {
            findViewById(R.id.noItemsTextView).setVisibility(View.GONE); // Hide the message
        }

        notificationAdapter.updateData(filteredList);
    }

    private void loadNotificationsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("notifications");

        databaseReference.orderByChild("timestamp").limitToLast(50).addValueEventListener(new ValueEventListener() {
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

    private void openAppSelectionActivity() {
        // Functionality to open App Selection Activity
        Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
        intent.putExtra("isRevisiting", true);
        startActivity(intent);
        finish();
    }
}
