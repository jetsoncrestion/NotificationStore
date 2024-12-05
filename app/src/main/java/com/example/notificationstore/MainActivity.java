package com.example.notificationstore;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationModels;
    private TextView noItemsTextView;
    private SearchView searchView;
    private FirebaseAuth mAuth;
    private ImageView logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchView = findViewById(R.id.searchView);
        noItemsTextView = findViewById(R.id.noItemsTextView);

        FirebaseApp.initializeApp(this);

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
            // If permission is not enabled, prompt the user to enable it
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        logoutButton = findViewById(R.id.imageView2);
        RecyclerView recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationModels = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationModels);
        recyclerView.setAdapter(notificationAdapter);

        mAuth = FirebaseAuth.getInstance();

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("notifications");

            // Order by timestamp to ensure latest notifications come first
            databaseReference.orderByChild("timestamp").limitToLast(50).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    notificationModels.clear();

                    if (!snapshot.exists()) {
                        Log.d(TAG, "No notifications found for user: " + userId);
                        return;
                    }

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);
                        if (notificationModel != null) {
                            // Set the unique key from Firebase
                            notificationModel.setUniqueKey(dataSnapshot.getKey());

                            // Add the notification to the list
                            notificationModels.add(notificationModel);
                        }
                    }

                    // Reverse the list to show the latest notification first
                    List<NotificationModel> reversedList = new ArrayList<>();
                    for (int i = notificationModels.size() - 1; i >= 0; i--) {
                        reversedList.add(notificationModels.get(i));
                    }

                    notificationModels.clear();
                    notificationModels.addAll(reversedList);

                    notificationAdapter.notifyDataSetChanged();
                    Log.d(TAG, "User-specific notifications loaded: " + notificationModels.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load notifications: " + error.getMessage());
                }
            });
        } else {
            Log.e(TAG, "User not authenticated. Cannot load notifications.");
        }
    }

    private boolean isNotificationListenerEnabled() {
        String enabledListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return enabledListeners != null && enabledListeners.contains(getPackageName());
    }
}
