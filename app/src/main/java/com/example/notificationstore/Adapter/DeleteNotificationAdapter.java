package com.example.notificationstore.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Model.DeleteNotificationModel;
import com.example.notificationstore.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeleteNotificationAdapter extends RecyclerView.Adapter<DeleteNotificationAdapter.ViewHolder> {
    private final Context context;
    private final List<DeleteNotificationModel> deleteNotificationModels;
    private final OnDeleteNotificationListener deleteListener;

    public DeleteNotificationAdapter(Context context, List<DeleteNotificationModel> deleteNotificationModels, OnDeleteNotificationListener deleteListener) {
        this.context = context;
        this.deleteNotificationModels = deleteNotificationModels;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delete_notification, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeleteNotificationModel model = deleteNotificationModels.get(position);
        holder.appName.setText(model.getAppName());
        holder.notificationContent.setText(model.getNotificationContent());

        long timestamp = model.getNotificationDateTime();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        holder.notificationDateTime.setText(formattedDate);

        String appIconBase64 = model.getAppIconBase64();
        Bitmap appIcon = decodeBase64ToBitmap(appIconBase64);
        if (appIcon != null) {
            holder.appIcon.setImageBitmap(appIcon);
        } else {
            holder.appIcon.setImageResource(R.drawable.baseline_android_24);
        }

        holder.imageButtonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteNotification(model, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onNotificationLongPressed(model, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return deleteNotificationModels.size();
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("DeleteNotificationAdapter", "Error decoding Base64 to Bitmap", e);
            return null;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView appName, notificationContent, notificationDateTime;
        private final ImageView appIcon, imageButtonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationDateTime = itemView.findViewById(R.id.notificationDateTime);
            appIcon = itemView.findViewById(R.id.appIcon);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);
        }
    }

    public interface OnDeleteNotificationListener {
        void onDeleteNotification(DeleteNotificationModel model, int position);
        void onRestoreNotification(DeleteNotificationModel model, int position);
        void onNotificationLongPressed(DeleteNotificationModel model, int position);
    }
}
