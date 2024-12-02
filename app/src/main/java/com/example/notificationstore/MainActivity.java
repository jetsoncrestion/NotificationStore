package com.example.notificationstore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Adapter.NotificationAdapter;
import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseAuth mAuth;
    private ImageView logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private void loadNotificationsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Snapshot children count: " + snapshot.getChildrenCount());
                notificationModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "Snapshot key: " + dataSnapshot.getKey());
                    NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);
                    if (notificationModel != null) {
                        Log.d(TAG, "Loaded notification: " + notificationModel.getAppName());
                        notificationModels.add(notificationModel);
                    }
                }
                notificationAdapter.notifyDataSetChanged();
                Log.d(TAG, "Notifications loaded: " + notificationModels.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load notifications: " + error.getMessage());
            }
        });
    }
}
