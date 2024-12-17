package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.notificationstore.Model.DeleteNotificationModel;
import com.google.android.material.radiobutton.MaterialRadioButton;
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

        TextView textViewAutoSubDeleteNotification = findViewById(R.id.textViewAutoSubDeleteNotification);
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        String deleteOption = sharedPreferences.getString("delete_option", "never_delete");

        switch (deleteOption) {
            case "delete_daily":
                textViewAutoSubDeleteNotification.setText("Auto delete notifications daily.");
                break;
            case "delete_older_than_1_week":
                textViewAutoSubDeleteNotification.setText("Delete notifications older than 1 week.");
                break;
            case "delete_older_than_1_month":
                textViewAutoSubDeleteNotification.setText("Delete notifications older than 1 month.");
                break;
            default:
                textViewAutoSubDeleteNotification.setText("Notifications will not be auto-deleted.");
                break;
        }

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
            new AlertDialog.Builder(Setting.this).setTitle("Delete All Notifications").setMessage("Are you sure you want to delete all notifications?").setPositiveButton("Yes, Delete All", (dialog, which) -> {
                deleteAllNotifications();
                Toast.makeText(Setting.this, "All notifications deleted", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        cardView6.setOnClickListener(v -> {
            showCustomDeleteAlertDialog();
        });

        autoDeleteOldNotifications();
    }

    private void showCustomDeleteAlertDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.auto_delete_alert_dialog, null);

        MaterialRadioButton radioButtonNeverDelete = dialogView.findViewById(R.id.radioButton);
        MaterialRadioButton radioButtonDeleteDaily = dialogView.findViewById(R.id.radioButton2);
        MaterialRadioButton radioButtonDeleteOlderThanOneWeek = dialogView.findViewById(R.id.radioButton3);
        MaterialRadioButton radioButtonDeleteOlderThanOneMonth = dialogView.findViewById(R.id.radioButton4);
        TextView textViewAccept = dialogView.findViewById(R.id.textViewAccept);
        TextView textViewCancel = dialogView.findViewById(R.id.textViewCancel);

        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        String deleteOption = sharedPreferences.getString("delete_option", "never_delete");

        TextView textViewAutoSubDeleteNotification = findViewById(R.id.textViewAutoSubDeleteNotification);

        switch (deleteOption) {
            case "delete_daily":
                radioButtonDeleteDaily.setChecked(true);
                break;
            case "delete_older_than_1_week":
                radioButtonDeleteOlderThanOneWeek.setChecked(true);
                break;
            case "delete_older_than_1_month":
                radioButtonDeleteOlderThanOneMonth.setChecked(true);
                break;
            default:
                radioButtonNeverDelete.setChecked(true);
                break;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(60);
                drawable.setColor(Color.WHITE);

                window.setBackgroundDrawable(drawable);
            }
        });

        textViewCancel.setOnClickListener(v -> dialog.dismiss());

        textViewAccept.setOnClickListener(v -> {
            String selectedOption = "";

            if (radioButtonNeverDelete.isChecked()) {
                selectedOption = "never_delete";
                performNeverDeleteLogic();
                textViewAutoSubDeleteNotification.setText("Notifications will not be auto-deleted.");
            } else if (radioButtonDeleteDaily.isChecked()) {
                selectedOption = "delete_daily";
                performDeleteDailyLogic();
                textViewAutoSubDeleteNotification.setText("Auto delete notifications daily.");
            } else if (radioButtonDeleteOlderThanOneWeek.isChecked()) {
                selectedOption = "delete_older_than_1_week";
                performDeleteOlderThanOneWeekLogic();
                textViewAutoSubDeleteNotification.setText("Delete notifications older than 1 week.");
            } else if (radioButtonDeleteOlderThanOneMonth.isChecked()) {
                selectedOption = "delete_older_than_1_month";
                performDeleteOlderThanOneMonthLogic();
                textViewAutoSubDeleteNotification.setText("Delete notifications older than 1 month.");
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("delete_option", selectedOption);
            editor.apply();

            Toast.makeText(this, "Selected: " + selectedOption, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performNeverDeleteLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("delete_option", "never_delete");
        editor.apply();

        Toast.makeText(this, "Auto-delete is disabled. Notifications will not be deleted automatically.", Toast.LENGTH_SHORT).show();
    }

    private void performDeleteDailyLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("delete_option", "delete_daily");
        editor.apply();
        deleteOldNotifications(86400000L);
    }

    private void performDeleteOlderThanOneWeekLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("delete_option", "delete_older_than_1_week");
        editor.apply();
        deleteOldNotifications(604800000L);
    }

    private void performDeleteOlderThanOneMonthLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("delete_option", "delete_older_than_1_month");
        editor.apply();
        deleteOldNotifications(2592000000L);
    }

    private void deleteOldNotifications(long timeThreshold) {
        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications");

        deletedNotificationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    DeleteNotificationModel model = snapshot.getValue(DeleteNotificationModel.class);
                    if (model != null && model.getTimeStamp() != 0) {
                        long currentTime = System.currentTimeMillis();
                        long notificationTime = model.getTimeStamp();
                        long timeDifference = currentTime - notificationTime;

                        if (timeDifference > timeThreshold) {
                            deletedNotificationsRef.child(snapshot.getKey()).removeValue();
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
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
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        String deleteOption = sharedPreferences.getString("delete_option", "never_delete");

        if ("never_delete".equals(deleteOption)) {
            return;
        }
        long timeThreshold = 2592000000L;
        if ("delete_daily".equals(deleteOption)) {
            timeThreshold = 86400000L;
        } else if ("delete_older_than_1_week".equals(deleteOption)) {
            timeThreshold = 604800000L;
        }

        deleteOldNotifications(timeThreshold);
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
