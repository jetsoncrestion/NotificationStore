package com.example.notificationstore;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.notificationstore.Model.DeleteNotificationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Setting extends AppCompatActivity {
    private ImageView imageViewBack, imageViewGreater;
    private Switch toggleSwitch, toggleSwitchSecond;
    private String deviceId;
    private CardView cardView3, cardView4, cardView5, cardView6, cardView7, cardView8;

    private int thumbOnColor;
    private int thumbOffColor;
    private int trackOnColor;
    private int trackOffColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewGreater = findViewById(R.id.imageViewGreater);
        toggleSwitch = findViewById(R.id.toggleSwitch);
        toggleSwitchSecond = findViewById(R.id.toggleSwitchSecond);
        cardView3 = findViewById(R.id.cardView3);
        cardView4 = findViewById(R.id.cardView4);
        cardView5 = findViewById(R.id.cardView5);
        cardView6 = findViewById(R.id.cardView6);
        cardView7 = findViewById(R.id.cardView7);
        cardView8 = findViewById(R.id.cardView8);
        deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        thumbOnColor = ContextCompat.getColor(this, R.color.switch_thumb_on);
        thumbOffColor = ContextCompat.getColor(this, R.color.switch_thumb_off);
        trackOnColor = ContextCompat.getColor(this, R.color.switch_track_on);
        trackOffColor = ContextCompat.getColor(this, R.color.switch_track_off);

        boolean isHideSystemNotifications = loadPreference("hideSystemNotifications");
        boolean isHideDuplicateNotifications = loadPreference("hideDuplicateNotifications");

        // Apply the saved states
        toggleSwitch.setChecked(isHideSystemNotifications);
        updateSwitchColors(toggleSwitch, isHideSystemNotifications);

        toggleSwitchSecond.setChecked(isHideDuplicateNotifications);
        updateSwitchSecondColors(toggleSwitchSecond, isHideDuplicateNotifications);

        imageViewBack.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        imageViewGreater.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, AppSelectionActivity.class);
            intent.putExtra("isRevisiting", true);
            startActivity(intent);
        });

        cardView5.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, AppSelectionActivity.class);
            intent.putExtra("isRevisiting", true);
            startActivity(intent);
        });

        cardView8.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, DeleteNotificationActivity.class);
            startActivity(intent);
        });

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(toggleSwitch, isChecked);
            savePreference("hideSystemNotifications", isChecked);
        });

        toggleSwitchSecond.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchSecondColors(toggleSwitchSecond, isChecked);
            savePreference("hideDuplicateNotifications", isChecked);
        });

        cardView7.setOnClickListener(v -> {
            // Create the AlertDialog for deleting all notifications
            new AlertDialog.Builder(Setting.this).setTitle("Delete All Notifications").setMessage("Are you sure you want to delete all notifications?").setPositiveButton("Yes", (dialog, which) -> {
                deleteAllNotifications();
                Toast.makeText(Setting.this, "All notifications deleted", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        cardView8.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, DeleteNotificationActivity.class);
            startActivity(intent);
        });

        autoDeleteOldNotifications();

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(toggleSwitch, isChecked);
            if (isChecked) {
                savePreference("hideSystemNotifications", true);
            } else {
                savePreference("hideSystemNotifications", false);
            }
        });
        toggleSwitchSecond.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchSecondColors(toggleSwitchSecond, isChecked);
            if (isChecked) {
                savePreference("hideDuplicateNotifications", true);
            } else {
                savePreference("hideDuplicateNotifications", false);
            }
        });
    }

    private void deleteAllNotifications() {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications");

        deletedNotificationsRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "All deleted notifications have been cleared", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete all notifications", Toast.LENGTH_SHORT).show();
        });
    }

    private void autoDeleteOldNotifications() {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications");

        deletedNotificationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    DeleteNotificationModel model = snapshot.getValue(DeleteNotificationModel.class);
                    if (model != null && model.getTimeStamp() != 0) {
                        long currentTime = System.currentTimeMillis();
                        long notificationTime = model.getTimeStamp();
                        long timeDifference = currentTime - notificationTime;

                        if (timeDifference > 2592000000L) {
                            deletedNotificationsRef.child(snapshot.getKey()).removeValue();
                            Toast.makeText(this, "Old notifications deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void savePreference(String key, boolean value) {
        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    private boolean loadPreference(String key) {
        return getSharedPreferences("MyPrefs", MODE_PRIVATE).getBoolean(key, false);
    }
}