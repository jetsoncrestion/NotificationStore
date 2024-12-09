package com.example.notificationstore;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_notification);

        deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        recyclerView = findViewById(R.id.deleteNotificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deletedNotificationModels = new ArrayList<>();
        notificationAdapter = new DeleteNotificationAdapter(this, deletedNotificationModels, this);
        recyclerView.setAdapter(notificationAdapter);

        loadDeletedNotifications();
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
    }

    @Override
    public void onDeleteNotification(DeleteNotificationModel model, int position) {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications");

        deletedNotificationsRef.child(model.getUniqueKey()).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Notification deleted permanently", Toast.LENGTH_SHORT).show();
            //deletedNotificationModels.remove(position);
            notificationAdapter.notifyItemRemoved(position);
        }).addOnFailureListener(e -> {
            Log.e("DeleteNotification", "Failed to delete notification: " + e.getMessage());
            Toast.makeText(this, "Failed to delete notification", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRestoreNotification(DeleteNotificationModel model, int position) {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications");

        DatabaseReference activeNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("notifications");

        // Move the notification to active notifications
        activeNotificationsRef.push().setValue(model).addOnSuccessListener(aVoid -> {
            // Remove it from the deleted notifications list
            deletedNotificationsRef.child(model.getUniqueKey()).removeValue().addOnSuccessListener(aVoid1 -> {
                Toast.makeText(this, "Notification restored", Toast.LENGTH_SHORT).show();
                notificationAdapter.notifyItemRemoved(position);
            }).addOnFailureListener(e -> {
                Log.e("RestoreNotification", "Failed to remove from deleted: " + e.getMessage());
                Toast.makeText(this, "Failed to remove from deleted notifications", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Log.e("RestoreNotification", "Failed to restore notification: " + e.getMessage());
            Toast.makeText(this, "Failed to restore notification", Toast.LENGTH_SHORT).show();
        });
    }

}
