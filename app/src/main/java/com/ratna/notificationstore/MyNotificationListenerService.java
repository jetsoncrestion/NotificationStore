package com.ratna.notificationstore;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
    private final Set<String> recentNotifications = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationListenerService created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NotificationListenerService destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "App task removed, re-initializing service");
        // Re-initialize the service
        Intent intent = new Intent(this, MyNotificationListenerService.class);
        startService(intent);
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getString("android.text");
        String subject = extras.getString("android.subText");
        String bigText = extras.getString("android.bigText");

        for (String key : extras.keySet()) {
            Log.d(TAG, "Key: " + key + " Value: " + extras.get(key));
        }

        if (text != null && (text.contains("Checking for new messages") || text.matches(".*messages from \\d+ chats.*"))) {
            Log.d(TAG, "Skipping notification with text: " + text);
            return;
        }

        if (text != null && text.matches("\\d+ new messages")) {
            Log.d(TAG, "Multi-message notification detected: " + text);
            handleMultiMessageNotification(packageName, text);
            return;
        }

        if (packageName.equals("com.whatsapp") || packageName.equals("com.whatsapp.w4b")) {
            String textLines = extras.getString("android.textLines");
            if (!TextUtils.isEmpty(textLines)) {
                text = textLines; // Update text to use textLines if available
            }

            if (text != null && (text.contains("image") || text.contains("photo"))) {
                String notificationKey = packageName + "|" + title + "|" + text;

                synchronized (recentNotifications) {
                    if (!recentNotifications.add(notificationKey)) {
                        Log.d(TAG, "Duplicate WhatsApp image notification detected: " + notificationKey + ", skipping.");
                        return; // Skip processing duplicate notifications
                    }

                    // Remove key after 10 seconds to allow fresh notifications later
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        synchronized (recentNotifications) {
                            recentNotifications.remove(notificationKey);
                        }
                    }, 10000); // Adjust delay as needed
                }
            }
        }

        if (text != null && (text.toLowerCase().contains("preparing backup") ||
                text.matches("Uploading: \\d+(\\.\\d+)? (kB|MB|GB) of \\d+(\\.\\d+)? (kB|MB|GB).*"))) {
            Log.d(TAG, "Skipping chat backup notification from: " + packageName + " - " + text);
            return; // Skip further processing
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

        String notificationKey = packageName + "|" + sbn.getId() + "|" + heading + "|" + text + "|" + timestamp;

        synchronized (recentNotifications) {
            if (!recentNotifications.add(notificationKey)) {
                Log.d(TAG, "Duplicate notification detected: " + notificationKey + ", skipping.");
                return;
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            synchronized (recentNotifications) {
                recentNotifications.remove(notificationKey);
            }
        }, 5000);

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
        String appName = packageName;
        String appIconBase64 = null;

        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            appName = pm.getApplicationLabel(appInfo).toString();
            Drawable appIconDrawable = pm.getApplicationIcon(packageName);
            appIconBase64 = shortenBase64(drawableToBase64(appIconDrawable));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App not found: " + packageName, e);
            appName = packageName;
        }

        saveNotificationToInternalStorage(appName, heading, text, timestamp, shortenBase64(appIconBase64), packageName);

        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Ensure it's a new task
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
            // Create a notification with a PendingIntent to open the app
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)  // Use appropriate icon
                    .setContentTitle(appName)
                    .setContentText(text)
                    .setAutoCancel(true)  // Remove notification after it's clicked
                    .setContentIntent(pendingIntent);  // Set the PendingIntent here

            // Get the NotificationManager to issue the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            notificationManager.notify((int) timestamp, builder.build());
        }
    }

    private void saveNotificationToInternalStorage(String appName, String heading, String text, long timestamp, String appIconBase64, String packageName) {
        try{
            appIconBase64 = shortenBase64(appIconBase64);
            File directory = new File(getFilesDir(), "NotificationStores");
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(TAG, "Failed to create directory for notifications: " + directory.getAbsolutePath());
                return;
            }
            File file = new File(directory, "notification.json");
            JSONArray jsonArray;
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();
                jsonArray = new JSONArray(jsonBuilder.toString());
            } else {
                jsonArray = new JSONArray();
            }

                JSONObject notificationObject = new JSONObject();
               notificationObject.put("appName", appName);
               notificationObject.put("notificationHeading", heading);
               notificationObject.put("notificationContent", text);
               notificationObject.put("timestamp", timestamp);
               notificationObject.put("appIconBase64", appIconBase64);
               notificationObject.put("packageName", packageName);
               jsonArray.put(notificationObject);

            FileWriter writer = new FileWriter(file, false);
            writer.write(jsonArray.toString());
            writer.flush();
            writer.close();
                Log.d(TAG, "Notification saved internally: " + notificationObject.toString());
        } catch (Exception e){
            Log.e(TAG, "Failed to save notification internally", e);
        }
    }

    private String shortenBase64(String base64String) {
        if (base64String == null || base64String.length() <= 10) {
            return base64String;
        }
        return base64String.substring(0, 10);
    }

    private void handleMultiMessageNotification(String packageName, String messageSummary) {
        Log.d(TAG, "Processing multi-message summary: " + messageSummary);
    }


    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null, unable to convert to Base64.");
            return null;  // Handle null case gracefully
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (drawable instanceof AdaptiveIconDrawable) {
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
