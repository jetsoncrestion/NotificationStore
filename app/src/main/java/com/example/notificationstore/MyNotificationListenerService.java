package com.example.notificationstore;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyNotificationListenerService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");
        long timestamp = sbn.getPostTime();

        PackageManager pm = getPackageManager();
        String appName = null;
        Integer appIcon = null;

        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            appName = pm.getApplicationLabel(appInfo).toString();

            // Convert Drawable to an integer resource ID (or null if unavailable)
            Drawable iconDrawable = pm.getApplicationIcon(appInfo);
            appIcon = appInfo.icon; // You can store this resource ID
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (appName == null || title == null || text == null) return;


        // Save notification details to Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
        String notificationId = databaseReference.push().getKey();
        NotificationModel notification = new NotificationModel(appName, text, timestamp, appIcon);
        if (notificationId != null) {
            databaseReference.child(notificationId).setValue(notification);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if needed
    }
}
