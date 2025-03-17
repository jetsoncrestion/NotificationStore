package com.ratna.notificationstore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Adapter.DeleteNotificationAdapter;
import com.ratna.notificationstore.Model.DeleteNotificationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteNotificationActivity extends AppCompatActivity implements DeleteNotificationAdapter.OnDeleteNotificationListener {
    private static final String TAG = "DeleteNotificationActivity";
    private RecyclerView recyclerView;
    private DeleteNotificationAdapter notificationAdapter;
    private ArrayList<DeleteNotificationModel> deletedNotificationModels;
  //  private ArrayList<DeleteNotificationModel> recentlyDeletedNotifications;
    //private String deviceId;
    private ImageView imageBack, imageViewMenuActionBar;
    private int selectedPosition = -1;
    private File deletedNotificationsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_notification);

        imageBack = findViewById(R.id.imageBack);
        imageViewMenuActionBar = findViewById(R.id.imageViewMenu);
        recyclerView = findViewById(R.id.deleteNotificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deletedNotificationModels = new ArrayList<>();
        notificationAdapter = new DeleteNotificationAdapter(this, deletedNotificationModels, this);
        recyclerView.setAdapter(notificationAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    selectedPosition = rv.getChildAdapterPosition(child); // Store the selected position
                }
                return false;
            }
        });

        imageViewMenuActionBar.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(DeleteNotificationActivity.this, imageViewMenuActionBar);
            popupMenu.inflate(R.menu.main_menu);
            Menu menu = popupMenu.getMenu();
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete_all) {
                    deleteAllNotifications();
                    return true;
                }
                if (itemId == R.id.action_restore_all) {
                    restoreAllNotifications();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        File directory = new File(getFilesDir(), "NotificationStores");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        deletedNotificationsFile = new File(directory, "deleted_notifications.json");
        autoDeleteOldNotifications();
        loadDeletedNotifications();

        imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(DeleteNotificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void autoDeleteOldNotifications() {
        if (!deletedNotificationsFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(deletedNotificationsFile))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            //reader.close();
            String content = jsonBuilder.toString();
            if (content.isEmpty()) return;
            JSONArray jsonArray = new JSONArray(content);
            JSONArray newArray = new JSONArray();
            long currentTime = System.currentTimeMillis();
            long ninetyDaysMillis = 90L * 24 * 60 * 60 * 1000; // 90 days
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                long timeStamp = obj.optLong("timestamp");
                if (currentTime - timeStamp <= ninetyDaysMillis) {
                    newArray.put(obj);
                }
            }
            try (FileWriter writer = new FileWriter(deletedNotificationsFile)) {
                writer.write(newArray.toString());
            }
            Log.d(TAG, "Auto-deletion in deleted file complete. Count: " + newArray.length());
            // writer.close();
        } catch (Exception e) {
            Log.e(TAG, "Error in autoDeleteOldNotifications", e);
        }
    }

    private void loadDeletedNotifications() {
        deletedNotificationModels.clear();
        if (!deletedNotificationsFile.exists()) {
            Toast.makeText(this, "No deleted notifications found", Toast.LENGTH_SHORT).show();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(deletedNotificationsFile))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            //reader.close();
            String content = jsonBuilder.toString();
            if (!content.isEmpty()) {
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    DeleteNotificationModel model = new DeleteNotificationModel();
                    model.setNotificationHeading(obj.optString("notificationHeading"));
                    model.setNotificationContent(obj.optString("notificationContent"));
                    model.setTimeStamp(obj.optLong("timestamp"));
                    model.setAppName(obj.optString("appName"));
                    model.setPackageName(obj.optString("packageName"));
                    model.setAppIconBase64(obj.optString("appIconBase64"));
                    model.setUniqueKey(obj.optString("uniqueKey"));
                    deletedNotificationModels.add(model);
                }
            }
            notificationAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error loading deleted notifications", e);
            Toast.makeText(this, "Error loading deleted notifications", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.second_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (selectedPosition == -1) {
            Toast.makeText(this, "Select a notification first", Toast.LENGTH_SHORT).show();
            return true;
        }

        DeleteNotificationModel model = notificationAdapter.getNotificationAt(selectedPosition);
        int itemId = item.getItemId();
        if (itemId == R.id.action_Delete_selection) {
            onDeleteNotification(model, selectedPosition);
            return true;
        } else if (itemId == R.id.action_restore_selection) {
            onRestoreNotification(model, selectedPosition);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteNotification(DeleteNotificationModel model, int position) {
        if (deletedNotificationModels == null || position < 0 || position >= deletedNotificationModels.size()) {
            Toast.makeText(this, "Invalid notification selected", Toast.LENGTH_SHORT).show();
            return;
        }

        deletedNotificationModels.remove(position);
        notificationAdapter.notifyItemRemoved(position);
        updateDeletedNotificationsFile();
        Toast.makeText(this, "Notification deleted permanently", Toast.LENGTH_SHORT).show();
        selectedPosition = -1;
    }

    @Override
    public void onRestoreNotification(DeleteNotificationModel model, int position) {
        deletedNotificationModels.remove(position);
        notificationAdapter.notifyItemRemoved(position);
        updateDeletedNotificationsFile();
        File directory = new File(getFilesDir(), "NotificationStores");
        File activeNotificationsFile = new File(directory, "notification.json");
        try {
            JSONArray activeArray;
            if (activeNotificationsFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(activeNotificationsFile))) {
                    StringBuilder activeBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        activeBuilder.append(line);
                    }
                    //reader.close();
                    String activeContent = activeBuilder.toString();
                    activeArray = activeContent.isEmpty() ? new JSONArray() : new JSONArray(activeContent);
                }
            } else {
                activeArray = new JSONArray();
            }
            JSONObject obj = new JSONObject();
            obj.put("notificationHeading", model.getNotificationHeading());
            obj.put("notificationContent", model.getNotificationContent());
            obj.put("timestamp", model.getTimeStamp());
            obj.put("appName", model.getAppName());
            obj.put("packageName", model.getPackageName());
            obj.put("appIconBase64", model.getAppIconBase64());
            obj.put("uniqueKey", model.getUniqueKey());
            activeArray.put(obj);
            try (FileWriter writer = new FileWriter(activeNotificationsFile)) {
                writer.write(activeArray.toString());
            }
            // writer.close();
            Toast.makeText(this, "Notification restored", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore notification", e);
            Toast.makeText(this, "Failed to restore notification", Toast.LENGTH_SHORT).show();
        }
        selectedPosition = -1;
    }

    public void restoreAllNotifications() {
        File directory = new File(getFilesDir(), "NotificationStores");
        File activeNotificationsFile = new File(directory, "notification.json");
        if (!deletedNotificationsFile.exists()) {
            Toast.makeText(this, "No notifications to restore", Toast.LENGTH_SHORT).show();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(deletedNotificationsFile))) {
            StringBuilder deletedBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                deletedBuilder.append(line);
            }
            //reader.close();
            String deletedContent = deletedBuilder.toString();
            if (deletedContent.isEmpty()) {
                Toast.makeText(this, "No notifications to restore", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONArray deletedArray = new JSONArray(deletedContent);
            JSONArray activeArray;
            if (activeNotificationsFile.exists()) {
                try (BufferedReader activeReader = new BufferedReader(new FileReader(activeNotificationsFile))) {
                    StringBuilder activeBuilder = new StringBuilder();
                    while ((line = activeReader.readLine()) != null) {
                        activeBuilder.append(line);
                    }
                    //activeReader.close();
                    String activeContent = activeBuilder.toString();
                    activeArray = activeContent.isEmpty() ? new JSONArray() : new JSONArray(activeContent);
                }
            } else {
                activeArray = new JSONArray();
            }
            for (int i = 0; i < deletedArray.length(); i++) {
                JSONObject obj = deletedArray.getJSONObject(i);
                activeArray.put(obj);
            }
            try (FileWriter activeWriter = new FileWriter(activeNotificationsFile)) {
                activeWriter.write(activeArray.toString());
            }
            //activeWriter.close();
            // Clear deleted notifications file
            try (FileWriter deletedWriter = new FileWriter(deletedNotificationsFile)) {
                deletedWriter.write("[]");
            }
            //deletedWriter.close();
            deletedNotificationModels.clear();
            notificationAdapter.notifyDataSetChanged();
            Toast.makeText(this, "All notifications restored", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to restore notifications", Toast.LENGTH_SHORT).show();
        }
    }


    public void deleteAllNotifications() {
        if (deletedNotificationsFile.exists()) {
            try (FileWriter writer = new FileWriter(deletedNotificationsFile)) {
                writer.write("[]");
                //  writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            deletedNotificationModels.clear();
            notificationAdapter.notifyDataSetChanged();
            Toast.makeText(this, "All notifications deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete notifications", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDeletedNotificationsFile() {
        File directory = new File(getFilesDir(), "NotificationStores");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            JSONArray newArray = new JSONArray();
            for (DeleteNotificationModel model : deletedNotificationModels) {
                JSONObject obj = new JSONObject();
                obj.put("notificationHeading", model.getNotificationHeading());
                obj.put("notificationContent", model.getNotificationContent());
                obj.put("timestamp", model.getTimeStamp());
                obj.put("appName", model.getAppName());
                obj.put("packageName", model.getPackageName());
                obj.put("appIconBase64", model.getAppIconBase64());
                obj.put("uniqueKey", model.getUniqueKey());
                newArray.put(obj);
            }
            try (FileWriter writer = new FileWriter(deletedNotificationsFile)) {
                writer.write(newArray.toString());
                //writer.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating deleted notifications file", e);
        }
    }
}
