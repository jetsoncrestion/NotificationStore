package com.example.notificationstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Model.NotificationModel;
import com.example.notificationstore.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<NotificationModel> notificationModels;

    public NotificationAdapter(Context context, List<NotificationModel> notificationModels) {
        this.context = context;
        this.notificationModels = notificationModels;
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        NotificationModel notificationModel = notificationModels.get(position);
        holder.appName.setText(notificationModel.getAppName());
        holder.notificationContent.setText(notificationModel.getNotificationContent());

        // Format timestamp to human-readable date
        long timestamp = notificationModel.getNotificationDateTime();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(timestamp));
        holder.notificationDateTime.setText(formattedDate);

        // Set the app icon
        if (notificationModel.getAppIcon() != null) {
            holder.appIcon.setImageResource(notificationModel.getAppIcon());
        } else {
            holder.appIcon.setImageResource(R.drawable.user); // Fallback icon
        }
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView appName, notificationContent, notificationDateTime;
        private ImageView appIcon;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationDateTime = itemView.findViewById(R.id.notificationDateTime);
            appIcon = itemView.findViewById(R.id.appIcon);
        }
    }
}
