package com.example.notificationstore;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;

import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
    private static String lastNotificationKey = null; // To track the last notification's unique key
    private static long lastSavedTime = 0; // To track the last saved notification's timestamp
    private static final long MINIMUM_INTERVAL = 5000; // Minimum interval in milliseconds (5 seconds)

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");
        long timestamp = sbn.getPostTime();

        // Log incoming notification details
        Log.d(TAG, "Notification received: " + packageName + " - " + title + " - " + text);

        if (title == null || text == null) {
            Log.e(TAG, "Title or text is null, skipping notification.");
            return;
        }

        if (packageName.equals("com.android.systemui")) {
            Log.d(TAG, "Ignored notification from System UI.");
            return;
        }

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
            appName = packageName; // Fallback to package name if app info is not found
        }

        // Log the values before saving to Firebase
        Log.d(TAG, "Saving notification: " + appName + " - " + text + " - " + timestamp);

        saveNotificationToFirebase(appName, text, timestamp, appIconBase64);
    }

    private void saveNotificationToFirebase(String appName, String text, long timestamp, String appIconBase64) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("notifications");

            String notificationId = String.valueOf(timestamp);  // You can use the timestamp as the unique key

            NotificationModel notification = new NotificationModel();
            notification.setUniqueKey(notificationId);  // Set the unique key (timestamp)
            notification.setAppName(appName);
            notification.setNotificationContent(text);
            notification.setNotificationDateTime(timestamp);
            notification.setAppIconBase64(appIconBase64);

            if (notificationId != null) {
                databaseReference.child(notificationId).setValue(notification)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved successfully under user: " + userId))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification to Firebase.", e));
            } else {
                Log.e(TAG, "Failed to generate notification ID.");
            }
        } else {
            Log.e(TAG, "User not authenticated. Cannot save notifications.");
        }
    }



    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if needed
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }

    private String drawableToBase64(Drawable drawable) {
        if (drawable == null) {
            Log.e(TAG, "Drawable is null. Cannot convert to Base64.");
            return null;
        }

        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            // Handle vector or other drawable types by converting them to a Bitmap
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        // Resize the bitmap to avoid large icons
        bitmap = resizeBitmap(bitmap, 128, 128);

        if (bitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } else {
            Log.e(TAG, "Bitmap is null. Cannot convert to Base64.");
            return null;
        }
    }


    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        float aspectRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        int newWidth = maxWidth;
        int newHeight = maxHeight;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newWidth = (int) (newHeight * aspectRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

}