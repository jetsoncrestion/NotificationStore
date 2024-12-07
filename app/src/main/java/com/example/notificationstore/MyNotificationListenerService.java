package com.example.notificationstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;

import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
    private static String lastNotificationKey = null;
    private static long lastSavedTime = 0;
    private static final long MINIMUM_INTERVAL = 5000;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");
        long timestamp = sbn.getPostTime();

        Log.d(TAG, "Notification received: " + packageName + " - " + title + " - " + text);

        if (title == null || text == null) {
            Log.e(TAG, "Title or text is null, skipping notification.");
            return;
        }

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Set<String> selectedApps = preferences.getStringSet("selectedApps", new HashSet<>());

        // Log the selected apps to verify
        Log.d(TAG, "Selected apps: " + selectedApps);

        if (!selectedApps.contains(packageName)) {
            Log.d(TAG, "Notification from non-selected app: " + packageName + ", skipping.");
            return;
        }


//        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//        Set<String> selectedApps = preferences.getStringSet("selectedApps", new HashSet<>());
//        Log.d(TAG, "Selected apps from SharedPreferences: " + selectedApps);
//
//        if (!selectedApps.contains(packageName)) {
//            Log.d(TAG, "Notification from non-selected app: " + packageName + ", skipping.");
//            return;
//        }

        if (packageName.equals("com.android.systemui")) {
            Log.d(TAG, "Ignored notification from System UI.");
            return;
        }

        // Deduplication logic
        String currentNotificationKey = packageName + "|" + title + "|" + text;

        if (currentNotificationKey.equals(lastNotificationKey)) {
            Log.d(TAG, "Duplicate notification detected, skipping.");
            return;
        }

        if (timestamp - lastSavedTime < MINIMUM_INTERVAL) {
            Log.d(TAG, "Notification received too soon after the last one, skipping.");
            return;
        }
        lastNotificationKey = currentNotificationKey;
        lastSavedTime = timestamp;

        PackageManager pm = getPackageManager();
        String appName;
        String appIconBase64 = null;

        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            appName = pm.getApplicationLabel(appInfo).toString();
            Drawable appIconDrawable = pm.getApplicationIcon(packageName);
            appIconBase64 = drawableToBase64(appIconDrawable);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App not found: " + packageName, e);
            appName = packageName;
        }

        saveNotificationToFirebase(appName, text, timestamp, appIconBase64);
    }

    private void saveNotificationToFirebase(String appName, String text, long timestamp, String appIconBase64) {
        // Retrieve device ID
        String deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("notifications");

        String notificationId = String.valueOf(timestamp);  // Use timestamp as the unique key

        NotificationModel notification = new NotificationModel();
        notification.setUniqueKey(notificationId);
        notification.setAppName(appName);
        notification.setNotificationContent(text);
        notification.setNotificationDateTime(timestamp);
        notification.setAppIconBase64(appIconBase64);

        if (notificationId != null) {
            databaseReference.child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved successfully for device: " + deviceId))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification to Firebase.", e));
        } else {
            Log.e(TAG, "Failed to generate notification ID.");
        }
    }

    private String drawableToBase64(Drawable drawable) {
        Bitmap bitmap = null;

        // Check if the drawable is BitmapDrawable
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        // If the drawable is AdaptiveIconDrawable, handle it differently
        else if (drawable instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptiveIconDrawable = (AdaptiveIconDrawable) drawable;
            Drawable foregroundDrawable = adaptiveIconDrawable.getForeground();
            if (foregroundDrawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) foregroundDrawable).getBitmap();
            } else {
                // If the foreground is not a BitmapDrawable, we can try to use the background or handle as needed.
                // For simplicity, we'll skip processing if we can't handle it.
                Log.e(TAG, "Unable to convert AdaptiveIconDrawable to Bitmap.");
            }
        } else {
            Log.e(TAG, "Unknown drawable type: " + drawable.getClass().getSimpleName());
        }

        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } else {
            Log.e(TAG, "Drawable could not be converted to Bitmap.");
            return null;
        }
    }


//    private String getOrGenerateDeviceId(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        String deviceId = sharedPreferences.getString("deviceId", null);
//
//        if (deviceId == null) {
//            deviceId = UUID.randomUUID().toString();
//            sharedPreferences.edit().putString("deviceId", deviceId).apply();
//        }
//
//        return deviceId;
//    }
}
