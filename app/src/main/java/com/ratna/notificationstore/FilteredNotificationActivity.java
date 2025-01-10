package com.ratna.notificationstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Adapter.NotificationAdapter;
import com.ratna.notificationstore.Model.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class FilteredNotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> filteredNotifications;
    private String selectedApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filtered_notification);

        TextView textViewSelectedAppName = findViewById(R.id.textViewSelectedAppName);
        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        recyclerView = findViewById(R.id.recyclerViewFilteredNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        filteredNotifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, filteredNotifications);
        recyclerView.setAdapter(notificationAdapter);

        imageViewBack.setOnClickListener(v -> {
            Intent intent = new Intent(FilteredNotificationActivity.this, MainActivity.class);
            startActivity(intent);
        });

        selectedApp = getIntent().getStringExtra("selectedApp");

        if (selectedApp != null) {
            textViewSelectedAppName.setText(selectedApp);
            loadFilteredNotifications(selectedApp);
        }
    }

    private void loadFilteredNotifications(String appName) {
        List<NotificationModel> allNotifications = MainActivity.getNotificationModels();

        for (NotificationModel model : allNotifications) {
            if (model != null && model.getAppName() != null && model.getAppName().equals(selectedApp)) {
                if (appName.equals("") || appName.equalsIgnoreCase(model.getAppName())) {
                    filteredNotifications.add(model);
                }
            }
        }
        notificationAdapter.notifyDataSetChanged();
    }
}