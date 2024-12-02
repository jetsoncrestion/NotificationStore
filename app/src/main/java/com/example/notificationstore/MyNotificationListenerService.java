package com.example.notificationstore;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
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

        PackageManager pm = getPackageManager();
        String appName;
        Integer appIcon = null;

        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            appName = pm.getApplicationLabel(appInfo).toString();
            appIcon = appInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App not found: " + packageName, e);
            appName = packageName;
        }

        saveNotificationToFirebase(appName, text, timestamp, appIcon);

      //  if (appName == null || title == null || text == null) return;


        // Save notification details to Firebase
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
//        String notificationId = databaseReference.push().getKey();
//        NotificationModel notification = new NotificationModel(appName, text, timestamp, appIcon);
//        if (notificationId != null) {
//            databaseReference.child(notificationId).setValue(notification);
//        }
    }

    private void saveNotificationToFirebase(String appName, String text, long timestamp, Integer appIcon) {
        Log.d(TAG, "Saving notification to Firebase: " + appName + " - " + text);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
        String notificationId = databaseReference.push().getKey();

        NotificationModel notification = new NotificationModel(appName, text, timestamp, appIcon);

        if (notificationId != null) {
            databaseReference.child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved successfully."))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification to Firebase.", e));
        } else {
            Log.e(TAG, "Failed to generate notification ID.");
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if needed
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }
}
