package com.ratna.notificationstore;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

public class BackupTask extends AsyncTask<GoogleSignInAccount, Void, String> {
    private static final String TAG = "BackupTask";
    private Context context;
    private Drive driveService;
    private NotificationDataReader notificationDataReader;
    private boolean isRestore;

    public interface NotificationDataReader {
        String readNotificationData();
        void writeNotificationData(String data);
    }

    BackupTask(Context context, Drive driveService, NotificationDataReader notificationDataReader) {
        this.context = context;
        this.driveService = driveService;
        this.notificationDataReader = notificationDataReader;
        this.isRestore = false;
    }
    BackupTask(Context context, Drive driveService, NotificationDataReader notificationDataReader, boolean isRestore) {
        this.context = context;
        this.driveService = driveService;
        this.notificationDataReader = notificationDataReader;
        this.isRestore = isRestore;
    }

    @Override
    protected String doInBackground(GoogleSignInAccount... accounts) {
        GoogleSignInAccount account = accounts[0];
        if (account == null) {
            return "Google account not found. Please sign in again";
        }

        if (isRestore) {
            return restoreData();
        } else {
            return backupData();
        }
    }

    private String backupData() {
        String notificationData = notificationDataReader.readNotificationData();
        if (notificationData == null || notificationData.isEmpty()) {
            return "No data to backup.";
        }
        String backupFileName = "notification_backup.json";
        try {
            String fileId = null;
            Drive.Files.List request = driveService.files().list()
                    .setQ("name='" + backupFileName + "' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id");
            FileList fileList = request.execute();
            if (!fileList.getFiles().isEmpty()) {
                fileId = fileList.getFiles().get(0).getId();
            }
            File fileMetadata = new File();
            fileMetadata.setName(backupFileName);
            fileMetadata.setMimeType("application/json");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(notificationData.getBytes());
            if (fileId != null) {
                driveService.files().update(fileId, fileMetadata, new com.google.api.client.http.ByteArrayContent("application/json", outputStream.toByteArray()))
                        .execute();
                return "Backup updated successfully!";
            } else {
                File file = driveService.files().create(fileMetadata,
                                new com.google.api.client.http.ByteArrayContent("application/json", outputStream.toByteArray()))
                        .setFields("id")
                        .execute();
                return "Backup created successful! File ID: " + file.getId();
            }
        } catch (IOException e) {
            Log.e("GoogleDrive", "Backup failed: " + e.getMessage(), e);
            return "Backup failed. Please try again.";
        }
    }

    private String restoreData() {
        String backupFileName = "notification_backup.json";
        try {
            Drive.Files.List request = driveService.files().list()
                    .setQ("name='" + backupFileName + "' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id)");
            FileList fileList = request.execute();
            if (fileList.getFiles().isEmpty()) {
                return "No backup file found.";
            }
            String fileId = fileList.getFiles().get(0).getId();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                String backupData = outputStream.toString();
                notificationDataReader.writeNotificationData(backupData);
                return "Data restored successfully!";
            } finally {
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Restore Failed: " + e.getMessage(), e);
            return "Restore failed. Please try again.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
//        backupAnimationView.cancelAnimation();
//        backupAnimationView.setVisibility(View.GONE);
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }
}
