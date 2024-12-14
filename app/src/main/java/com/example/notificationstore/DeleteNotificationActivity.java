package com.example.notificationstore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Adapter.DeleteNotificationAdapter;
import com.example.notificationstore.Model.DeleteNotificationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeleteNotificationActivity extends AppCompatActivity implements DeleteNotificationAdapter.OnDeleteNotificationListener {

    private RecyclerView recyclerView;
    private DeleteNotificationAdapter notificationAdapter;
    private List<DeleteNotificationModel> deletedNotificationModels;
    private String deviceId;
    private ImageView imageBack;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_notification);

        imageBack = findViewById(R.id.imageBack);
        deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        recyclerView = findViewById(R.id.deleteNotificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deletedNotificationModels = new ArrayList<>();
        notificationAdapter = new DeleteNotificationAdapter(this, deletedNotificationModels, this);
        recyclerView.setAdapter(notificationAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    selectedPosition = rv.getChildAdapterPosition(child); // Store the selected position
                }
                return false;
            }
        });

        autoDeleteOldNotifications();
        loadDeletedNotifications();
    }

    private void autoDeleteOldNotifications() {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("deleted_notifications");

        deletedNotificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DeleteNotificationModel model = dataSnapshot.getValue(DeleteNotificationModel.class);
                    if (model != null && model.getTimeStamp() != 0) {
                        long currentTime = System.currentTimeMillis();
                        long notificationTime = model.getTimeStamp();
                        long timeDifference = currentTime - notificationTime;

                        if (timeDifference > 2592000000L) {
                            deletedNotificationsRef.child(dataSnapshot.getKey()).removeValue();
                            Log.d("AutoDelete", "Notification older than 30 days deleted");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AutoDelete", "Error checking for old notifications: " + error.getMessage());
            }
        });
    }

    private void loadDeletedNotifications() {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications");

        deletedNotificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deletedNotificationModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DeleteNotificationModel model = dataSnapshot.getValue(DeleteNotificationModel.class);
                    if (model != null) {
                        model.setUniqueKey(dataSnapshot.getKey());
                        deletedNotificationModels.add(model);
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoadDeleted", "Failed to load deleted notifications: " + error.getMessage());
            }
        });

        imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(DeleteNotificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.second_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (selectedPosition == -1) {
            Toast.makeText(this, "Select a notification first", Toast.LENGTH_SHORT).show();
            return true;
        }

        DeleteNotificationModel model = notificationAdapter.getNotificationAt(selectedPosition);

        int itemId = item.getItemId();

        if (itemId == R.id.action_Delete_selection) {
            onDeleteNotification(model, selectedPosition);
            return true;
        } else if (itemId == R.id.action_restore_selection) {
            onRestoreNotification(model, selectedPosition);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDeleteNotification(DeleteNotificationModel model, int position) {
        deletedNotificationModels.remove(position);
        notificationAdapter.notifyItemRemoved(position);

        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("deleted_notifications");

        deletedNotificationsRef.child(model.getUniqueKey()).removeValue().addOnSuccessListener(aVoid -> {
            //Toast.makeText(this, "Notification deleted permanently", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("DeleteNotification", "Failed to delete notification: " + e.getMessage());
            Toast.makeText(this, "Failed to delete notification", Toast.LENGTH_SHORT).show();

            deletedNotificationModels.add(position, model);
            notificationAdapter.notifyItemInserted(position);
        });
    }

    @Override
    public void onRestoreNotification(DeleteNotificationModel model, int position) {
        deletedNotificationModels.remove(position);
        notificationAdapter.notifyItemRemoved(position);

        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("deleted_notifications");

        DatabaseReference activeNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("notifications");

        activeNotificationsRef.push().setValue(model).addOnSuccessListener(aVoid -> {
            deletedNotificationsRef.child(model.getUniqueKey()).removeValue().addOnFailureListener(e -> {
                Log.e("RestoreNotification", "Failed to remove from deleted: " + e.getMessage());
                Toast.makeText(this, "Failed to remove from deleted notifications", Toast.LENGTH_SHORT).show();

                deletedNotificationModels.add(position, model);
                notificationAdapter.notifyItemInserted(position);
            });

            Toast.makeText(this, "Notification restored", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("RestoreNotification", "Failed to restore notification: " + e.getMessage());
            Toast.makeText(this, "Failed to restore notification", Toast.LENGTH_SHORT).show();

            deletedNotificationModels.add(position, model);
            notificationAdapter.notifyItemInserted(position);
        });
    }
}
