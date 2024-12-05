package com.example.notificationstore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        searchView = findViewById(R.id.searchView);
        noItemsTextView = findViewById(R.id.noItemsTextView);

        deviceId = getOrGenerateDeviceId(this);

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

    private String getOrGenerateDeviceId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedDeviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (savedDeviceId == null) {
            // Generate a new UUID
            savedDeviceId = UUID.randomUUID().toString();

            // Save it to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_ID_KEY, savedDeviceId);
            editor.apply();
        }

        return savedDeviceId;
    }
}
