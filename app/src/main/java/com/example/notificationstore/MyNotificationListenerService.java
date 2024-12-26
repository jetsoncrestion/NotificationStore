package com.example.notificationstore;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
    private final Set<String> recentNotifications = ConcurrentHashMap.newKeySet();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");
        String subject = sbn.getNotification().extras.getString("android.subText");
        String bigText = sbn.getNotification().extras.getString("android.bigText");

        Bundle extras = sbn.getNotification().extras;
        for (String key : extras.keySet()) {
            Log.d(TAG, "Key: " + key + " Value: " + extras.get(key));
        }

        if (text != null && text.matches("\\d+ new messages")) {
            Log.d(TAG, "Multi-message notification detected: " + text);
            handleMultiMessageNotification(packageName, text);
            return;
        }

        if (packageName.equals("com.google.android.gm")) {
            if (TextUtils.isEmpty(title)) {
                title = subject;
            }
            if (TextUtils.isEmpty(text)) {
                text = bigText;
            }
        }

        if (packageName.equals("com.facebook.katana")) {
            if (TextUtils.isEmpty(title)) {
                title = sbn.getNotification().extras.getString("com.facebook.katana.Title");
            }
            if (TextUtils.isEmpty(text)) {
                text = sbn.getNotification().extras.getString("com.facebook.katana.Text");
            }
        } else if (packageName.equals("com.instagram.android")) {
            if (TextUtils.isEmpty(title)) {
                title = sbn.getNotification().extras.getString("com.instagram.android.Title");
            }
            if (TextUtils.isEmpty(text)) {
                text = sbn.getNotification().extras.getString("com.instagram.android.Text");
            }
        } else if (packageName.equals("com.google.android.youtube")) {
            if (TextUtils.isEmpty(title)) {
                title = sbn.getNotification().extras.getString("android.title.big");
            }
            if (TextUtils.isEmpty(title)) {
                title = sbn.getNotification().extras.getString("android.title");
            }
            if (TextUtils.isEmpty(text)) {
                text = sbn.getNotification().extras.getString("android.text");
            }
            if (TextUtils.isEmpty(text)) {
                text = sbn.getNotification().extras.getString("android.bigText");
            }
        }

        String heading = title;
        long timestamp = sbn.getPostTime();

        Log.d(TAG, "Notification received: " + packageName + " - " + heading + " - " + text);

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(text)) {
            Log.e(TAG, "Title or text is null/empty, skipping notification.");
            return;
        }

        String notificationKey = packageName + "|" + heading + "|" + text + "|" + timestamp;

        if (!recentNotifications.add(notificationKey)) {
            Log.d(TAG, "Duplicate notification detected: " + notificationKey + ", skipping.");
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> recentNotifications.remove(notificationKey), 2000);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean showAllNotifications = preferences.getBoolean("hideSystemNotifications", false);

        if (!showAllNotifications) {
            Set<String> selectedApps = preferences.getStringSet("selectedApps", new HashSet<>());
            if (!selectedApps.contains(packageName)) {
                Log.d(TAG, "Notification from non-selected app: " + packageName + ", skipping.");
                return;
            }
        }

        Set<String> selectedApps = preferences.getStringSet("selectedApps", new HashSet<>());

        Log.d(TAG, "Selected apps: " + selectedApps);

        if (!selectedApps.contains(packageName)) {
            Log.d(TAG, "Notification from non-selected app: " + packageName + ", skipping.");
            return;
        }

        if (packageName.equals("com.android.systemui")) {
            Log.d(TAG, "Ignored notification from System UI.");
            return;
        }

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

        saveNotificationToFirebase(appName, heading, text, timestamp, appIconBase64);
    }

    private void saveNotificationToFirebase(String appName, String heading, String text, long timestamp, String appIconBase64) {
        // Retrieve device ID
        String deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("devices")
                .child(deviceId)
                .child("notifications");
        String notificationId = String.valueOf(timestamp);

        NotificationModel notification = new NotificationModel();
        notification.setUniqueKey(notificationId);
        notification.setAppName(appName);
        notification.setNotificationHeading(heading);
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

    private void handleMultiMessageNotification(String packageName, String messageSummary) {
        Log.d(TAG, "Processing multi-message summary: " + messageSummary);
    }

    private String drawableToBase64(Drawable drawable) {
        int targetWidth = convertToPx(90);
        int targetHeight = convertToPx(90);
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            bitmap = Bitmap.createScaledBitmap(
                    ((BitmapDrawable) drawable).getBitmap(),
                    targetWidth,
                    targetHeight,
                    true
            );
        } else if (drawable instanceof AdaptiveIconDrawable) {
            Bitmap tempBitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(tempBitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            bitmap = Bitmap.createScaledBitmap(tempBitmap, targetWidth, targetHeight, true);
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

    private int convertToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
