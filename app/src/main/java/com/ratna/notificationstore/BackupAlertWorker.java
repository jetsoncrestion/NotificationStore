package com.ratna.notificationstore;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackupAlertWorker extends Worker {
    private static final String LAST_NOTIFICATION_TIME_KEY = "last_notification_time";
    private static final String CHANNEL_ID = "backup_alert_channel";
    private static final String PREFS_NAME = "BackupPrefs";
    private static final int NOTIFICATION_ID = 2;
    private static final long BACKUP_INTERVAL = 15 * 24 * 60 * 60 * 1000L;
    public BackupAlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (shouldShowNotification()) {
            showBackupAlertNotification();
            updateLastNotificationTime();
        }
        return Result.success();
    }

    private boolean shouldShowNotification() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastNotificationTime = prefs.getLong(LAST_NOTIFICATION_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Show notification only if 15 days have passed since the last notification
        return currentTime - lastNotificationTime >= BACKUP_INTERVAL;
    }

    private void updateLastNotificationTime() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_NOTIFICATION_TIME_KEY, System.currentTimeMillis());
        editor.apply();
    }


    private void showBackupAlertNotification() {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Backup Alert Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("show_backup_dialog", true); // Flag to show the backup dialog
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable._92_log_05) // Replace with your notification icon
                .setContentTitle("Backup Alert")
                .setContentText("It's time to back up your notifications!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}

