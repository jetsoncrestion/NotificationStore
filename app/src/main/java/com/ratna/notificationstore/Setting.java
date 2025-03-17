package com.ratna.notificationstore;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

public class Setting extends BaseActivity implements BackupTask.NotificationDataReader  {
    private static final String TAG = "Setting";
    private static final int RC_SIGN_IN = 9001;
    private static final String[] SCOPES = {DriveScopes.DRIVE_FILE};
    private GoogleSignInClient googleSignInClient;
    private Drive driveService;
    private ImageView imageViewBack, imageViewGreater;
    private Switch toggleSwitch, toggleSwitchSecond;
    private String deviceId;
    private TextView textViewAppNameVersion, setLanguageName;
    private CardView cardView3, cardView4, cardView5, cardView6, cardView7, cardView8, cardViewTerms, cardViewPrivacy, cardViewAbout, cardViewShareOurApp, cardViewRateOurApp, cardView15, cardViewHowToUseApp, cardView16, cardView17;
    //private TextView textView4;
    private int thumbOnColor;
    private int thumbOffColor;
    private int trackOnColor;
    private int trackOffColor;
    private File directory;
    private File activeNotificationsFile;
    private File deletedNotificationsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        //textView4 = findViewById(R.id.textView4);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Auto-delete old notifications
        autoDeleteOldNotifications();
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
        cardView15 = findViewById(R.id.cardView15);         //Change App Language
        cardViewRateOurApp = findViewById(R.id.cardViewRateOurApp);
        cardViewShareOurApp = findViewById(R.id.cardViewShareOurApp);
        cardViewTerms = findViewById(R.id.cardViewTerms);
        cardViewPrivacy = findViewById(R.id.cardViewPrivacy);
        cardViewAbout = findViewById(R.id.cardViewAbout);
        textViewAppNameVersion = findViewById(R.id.textViewAppNameVersion);
        setLanguageName = findViewById(R.id.setLanguageName);
        cardViewHowToUseApp = findViewById(R.id.cardViewHowToUseApp);
        cardView16 = findViewById(R.id.cardView16);
        cardView17 = findViewById(R.id.cardView17);
        deviceId = DeviceUtil.getOrGenerateDeviceId(this);

        thumbOnColor = ContextCompat.getColor(this, R.color.switch_thumb_on);
        thumbOffColor = ContextCompat.getColor(this, R.color.switch_thumb_off);
        trackOnColor = ContextCompat.getColor(this, R.color.switch_track_on);
        trackOffColor = ContextCompat.getColor(this, R.color.switch_track_off);

        boolean isHideSystemNotifications = loadPreference("hideSystemNotifications");
        boolean isHideDuplicateNotifications = loadPreference("hideDuplicateNotifications");

        updateVersionNumber();
        updateLanguageTextView();
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

//        textView4.setOnClickListener(v -> {
//            Intent intent = new Intent(Setting.this, SecurityActivity.class);
//            startActivity(intent);
//        });

        cardViewHowToUseApp.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, HowToUseApp.class);
            startActivity(intent);
        });

        cardViewRateOurApp.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(Setting.this, "App not found on Play Store.", Toast.LENGTH_SHORT).show();
            }
        });

        cardViewShareOurApp.setOnClickListener(v -> {
            String appPackageName = getPackageName();
            String playStoreLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Check out this amazing Notification Store App: " + playStoreLink;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            try {
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(Setting.this, "No application found to share the app.", Toast.LENGTH_SHORT).show();
            }
        });

        cardView15.setOnClickListener(v -> {
            showLanguageSelectionDialog();
        });
        cardView16.setOnClickListener(view -> {
            startBackupProcess();
        });
        cardView17.setOnClickListener(view -> {
            startRestoreProcess();
        });

        cardViewTerms.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, TermsAndConditionsActivity.class);
            startActivity(intent);
        });

        cardViewPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        cardViewAbout.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, AboutActivity.class);
            startActivity(intent);
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
            new AlertDialog.Builder(Setting.this, R.style.AlertDialogCustom).setTitle("Delete All Notifications").setMessage("Are you sure you want to delete all notifications?").setPositiveButton("Yes, Delete All", (dialog, which) -> {
                deleteAllNotifications();
                Toast.makeText(Setting.this, "All notifications deleted", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        cardView6.setOnClickListener(v -> {
            showCustomDeleteAlertDialog();
        });

        File directory = new File(getFilesDir(), "NotificationStores");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        activeNotificationsFile = new File(directory, "notification.json");
        deletedNotificationsFile = new File(directory, "deleted_notifications.json");
        // Ensure deleted_notifications file exists
        if (!deletedNotificationsFile.exists()) {
            try (FileWriter writer = new FileWriter(deletedNotificationsFile)) {
                writer.write("[]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        autoDeleteOldNotifications();
        Log.d(TAG, "onCreate completed in Setting.");
    }

    @SuppressLint("StaticFieldLeak")
    private void startBackupProcess() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            if (driveService == null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                driveService = new Drive.Builder(new NetHttpTransport(),
                        new GsonFactory(),
                        credential).setApplicationName("Notification Store").build();
            }
            new BackupTask(this, driveService, this).execute(account);
        } else {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    private void startRestoreProcess() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            if (driveService == null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                driveService = new Drive.Builder(new NetHttpTransport(),
                        new GsonFactory(),
                        credential).setApplicationName("Notification Store").build();
            }
            new BackupTask(this, driveService, this, true).execute(account);
        } else {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void writeNotificationData(String data) {
        File directory = new File(getFilesDir(), "NotificationStores");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "notification.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write notification data: " + e.getMessage(), e);
        }
    }

    @Override
    public String readNotificationData() {
        File directory = new File(getFilesDir(), "NotificationStores");
        File file = new File(directory, "notification.json");
        if (!file.exists()) {
            Log.e(TAG, "Notification file does not exists");
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String content = builder.toString();
            if (!content.isEmpty()) {
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    // Shorten the appIconBase64 string to 10 characters
                    String appIconBase64 = obj.optString("appIconBase64");
                    if (!TextUtils.isEmpty(appIconBase64) && appIconBase64.length() > 10) {
                        obj.put("appIconBase64", appIconBase64.substring(0, 10));
                    }
                }
                return builder.toString();
            }
        } catch (Exception e) {
            Log.e("FileReader", "Error reading notification data: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            new BackupTask(this, driveService, this).execute(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void backupToGoogleDrive(GoogleSignInAccount account) {
        if (account == null) {
            Log.e(TAG, "GoogleSignInAccount is null. User is not signed in.");
            Toast.makeText(this, "Google account not found. Please sing in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            driveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("Notification Store")
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Google Drive service: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to initialize Google Drive service.", Toast.LENGTH_SHORT).show();
            return;
        }
        String notificationData = readNotificationData();
        if (notificationData == null || notificationData.isEmpty()) {
            Log.e(TAG, "No notification data found to backup.");
            Toast.makeText(this, "No data to backup.", Toast.LENGTH_SHORT).show();
            return;
        }
        String backupFileName = "notification_backup.json";
        String fileId = null;
        try {
            Drive.Files.List request = driveService.files().list()
                    .setQ("name='" + backupFileName + "' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id");
            com.google.api.services.drive.model.FileList fileList = request.execute();
            if (!fileList.getFiles().isEmpty()) {
                fileId = fileList.getFiles().get(0).getId();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to search for existing backup file: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to search for existing backup file.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(backupFileName);
            fileMetadata.setMimeType("application/json"); // Set the MIME type
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(notificationData.getBytes());
            if (fileId != null) {
                driveService.files().update(fileId, fileMetadata, new com.google.api.client.http.ByteArrayContent("application/json", outputStream.toByteArray())).execute();
                Log.d(TAG, "Backup updated successfully! File ID: " + fileId);
                Toast.makeText(this, "Backup updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                com.google.api.services.drive.model.File file = driveService.files().create(fileMetadata,
                                new com.google.api.client.http.ByteArrayContent("application/json", outputStream.toByteArray()))
                        .setFields("id")
                        .execute();
                Log.d(TAG, "Backup successful! File ID: " + file.getId());
                Toast.makeText(this, "Backup successful! File ID: " + file.getId(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("GoogleDrive", "Backup failed: " + e.getMessage(), e);
            Toast.makeText(this, "Backup failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLanguageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.custom_multi_language_selection, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        TextView textViewAccept = dialogView.findViewById(R.id.textViewAccept);
        TextView textViewCancel = dialogView.findViewById(R.id.textViewCancel);

        AlertDialog dialog = builder.create();

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

        SharedPreferences sharedPreferences = getSharedPreferences("AppSetting", MODE_PRIVATE);
        String selectedLanguageCode = sharedPreferences.getString("Language", "en");
        if (selectedLanguageCode.equals("en")){
            radioGroup.check(R.id.radioButtonEnglish);
        }else if (selectedLanguageCode.equals("ne")){
            radioGroup.check(R.id.radioButtonNepali);
        } else if (selectedLanguageCode.equals("it")) {
            radioGroup.check(R.id.radioButtonItalian);
        } else if (selectedLanguageCode.equals("ko")) {
            radioGroup.check(R.id.radioButtonKorean);
        } else if (selectedLanguageCode.equals("hi")) {
            radioGroup.check(R.id.radioButtonHindi);
        } else if (selectedLanguageCode.equals("pt")) {
            radioGroup.check(R.id.radioButtonPortuguese);
        } else if (selectedLanguageCode.equals("fr")) {
            radioGroup.check(R.id.radioButtonFrench);
        }

        textViewAccept.setOnClickListener(view -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String newLanguageCode = "en";

            if (selectedId == R.id.radioButtonNepali){
                newLanguageCode = "ne";
            } else if (selectedId == R.id.radioButtonItalian){
                newLanguageCode = "it";
            } else if (selectedId == R.id.radioButtonKorean){
                newLanguageCode = "ko";
            } else if (selectedId == R.id.radioButtonHindi) {
                newLanguageCode = "hi";
            } else if (selectedId == R.id.radioButtonPortuguese) {
                newLanguageCode = "pt";
            } else if (selectedId == R.id.radioButtonFrench) {
                newLanguageCode = "fr";
            }

            setLocale(newLanguageCode);
            updateLanguageTextView();
            dialog.dismiss();
        });

        textViewCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.show();
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

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
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
            dialog.dismiss();
        });
        dialog.show();
    }

    private void performNeverDeleteLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("delete_option", "never_delete");
        editor.apply();
    }

    private void performDeleteDailyLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("DeletePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("delete_option", "delete_daily");
        editor.apply();
        deleteOldNotifications(43200000L);
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
        try {
            // Read active notifications
            JSONArray activeArray = new JSONArray();
            if (activeNotificationsFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(activeNotificationsFile))) {
                    StringBuilder activeBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        activeBuilder.append(line);
                    }
                    //reader.close();
                    String activeContent = activeBuilder.toString();
                    if (!activeContent.isEmpty()) {
                        activeArray = new JSONArray(activeContent);
                    }
                }
            }

            // Read deleted notifications (if any)
            JSONArray deletedArray = new JSONArray();
            if (deletedNotificationsFile.exists()) {
                try (BufferedReader readerDel = new BufferedReader(new FileReader(deletedNotificationsFile))) {
                    StringBuilder delBuilder = new StringBuilder();
                    String line;
                    while ((line = readerDel.readLine()) != null) {
                        delBuilder.append(line);
                    }
                    //  readerDel.close();
                    String delContent = delBuilder.toString();
                    if (!delContent.isEmpty()) {
                        deletedArray = new JSONArray(delContent);
                    }
                }
            }

            long currentTime = System.currentTimeMillis();
            JSONArray newActiveArray = new JSONArray();
            // For each notification, check if older than threshold; if yes, move to deleted array.
            for (int i = 0; i < activeArray.length(); i++) {
                JSONObject notif = activeArray.getJSONObject(i);
                if (notif.has("notificationDateTime") && !notif.has("timestamp")) {
                    notif.put("timestamp", notif.getLong("notificationDateTime"));
                }
                long notifTime = notif.optLong("timestamp");
                if (currentTime - notifTime > timeThreshold) {
                    deletedArray.put(notif);
                } else {
                    newActiveArray.put(notif);
                }
            }
            try (FileWriter writer = new FileWriter(activeNotificationsFile)) {
                writer.write(newActiveArray.toString());
            }
            try (FileWriter writer = new FileWriter(deletedNotificationsFile)) {
                writer.write(deletedArray.toString());
            }
            Log.d(TAG, "deletedOldNotifications: Moved" + (activeArray.length() - newActiveArray.length()) + "notifications to deleted file");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete old notifications", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllNotifications() {
       try {
           JSONArray activeArray = new JSONArray();
           if (activeNotificationsFile.exists()) {
               try (BufferedReader reader = new BufferedReader(new FileReader(activeNotificationsFile))) {
                   StringBuilder activeBuilder = new StringBuilder();
                   String line;
                   while ((line = reader.readLine()) != null) {
                       activeBuilder.append(line);
                   }
                   String activeContent = activeBuilder.toString();
                   if (!activeContent.isEmpty()) {
                       activeArray = new JSONArray(activeContent);
                   }
               }
       }
           JSONArray deletedArray = new JSONArray();
           if (deletedNotificationsFile.exists()) {
               try (BufferedReader readerDel = new BufferedReader(new FileReader(deletedNotificationsFile))) {
                   StringBuilder delBuilder = new StringBuilder();
                   String line;
                   while ((line = readerDel.readLine()) != null) {
                       delBuilder.append(line);
                   }
                   String delContent = delBuilder.toString();
                   if (!delContent.isEmpty()) {
                       deletedArray = new JSONArray(delContent);
                   }
               }
           }
           for (int i = 0; i < activeArray.length(); i++) {
               JSONObject notif = activeArray.getJSONObject(i);
               if (!notif.has("timestamp") && notif.has("notificationDateTime")) {
                   notif.put("timestamp", notif.getLong("notificationDateTime"));
               }
               deletedArray.put(notif);
           }
           // Clear active notifications file
           try (FileWriter writer = new FileWriter(activeNotificationsFile)) {
               writer.write("[]");
           }
           // Update deleted notifications file
           try (FileWriter writer = new FileWriter(deletedNotificationsFile)) {
               writer.write(deletedArray.toString());
           }
           Toast.makeText(this, "All notifications moved to Recently Deleted", Toast.LENGTH_SHORT).show();
       } catch (Exception e) {
           e.printStackTrace();
           Toast.makeText(this, "Failed to delete all notifications", Toast.LENGTH_SHORT).show();
       }
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

    private void updateVersionNumber() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            textViewAppNameVersion.setText("Notify: Notification History" + " (" + versionName + ")");
        } catch (Exception e) {
            textViewAppNameVersion.setText("Version is not available");
        }
    }

    public class BaseActivity extends AppCompatActivity {
        @Override
        protected void attachBaseContext(Context newBase) {
            SharedPreferences sharedPreferences = newBase.getSharedPreferences("AppSetting", MODE_PRIVATE);
            String languageCode = sharedPreferences.getString("Language", "en");
            super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode));
        }
    }

    private void setLocale(String languageCode) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSetting", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", languageCode);
        editor.apply();

        LocaleHelper.setLocale(this, languageCode);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isLanguageChange", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void updateLanguageTextView() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSetting", MODE_PRIVATE);
        String languageCode = sharedPreferences.getString("Language", "en");
        String languageDisplay;
        switch (languageCode) {
            case "ne":
                languageDisplay = "नेपाली";
                break;
            case "it":
                languageDisplay = "Italian";
                break;
            case "ko":
                languageDisplay = "한국어";
                break;
            case "fr":
                languageDisplay = "Français";
                break;
            case "hi":
                languageDisplay = "Hindi";
                break;
            case "pt":
                languageDisplay = "Português";
                break;
            default:
                languageDisplay = "English";
        }
        setLanguageName.setText(languageDisplay);
    }
}
