package com.ratna.notificationstore.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Model.DeleteNotificationModel;
import com.ratna.notificationstore.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public void onBindViewHolder(@NonNull DeleteNotificationAdapter.ViewHolder holder, int position) {
        DeleteNotificationModel model = deleteNotificationModels.get(position);
        holder.notificationHeading.setText(model.getNotificationHeading());
        holder.appName.setText(model.getAppName());
        holder.notificationContent.setText(model.getNotificationContent());

        long timestamp = model.getTimeStamp();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd / hh:mm a", Locale.getDefault()).format(new Date(timestamp));
        holder.notificationDateTime.setText(formattedDate);

        String appIconBase64 = model.getAppIconBase64();
        //Bitmap appIcon = decodeBase64ToBitmap(appIconBase64);
        if (appIconBase64 != null && !appIconBase64.isEmpty()) {
            Bitmap bitmap = decodeBase64ToBitmap(appIconBase64);
            holder.appIcon.setImageBitmap(bitmap != null ? bitmap : getFallbackIcon());
        } else {
            holder.appIcon.setImageResource(R.drawable.baseline_android_24);
        }

        holder.imageButtonMenuActionBar.setOnClickListener(v -> {
            if (deleteListener != null) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.second_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition == RecyclerView.NO_POSITION) {
                        return false;
                    }
                    DeleteNotificationModel currentModel = deleteNotificationModels.get(currentPosition);
                    if (item.getItemId() == R.id.action_Delete_selection) {
                        deleteListener.onDeleteNotification(currentModel, currentPosition);
                        return true;
                    } else if (item.getItemId() == R.id.action_restore_selection) {
                        deleteListener.onRestoreNotification(currentModel, currentPosition);
                        return true;
                    } else {
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return deleteNotificationModels.size();
    }

    public DeleteNotificationModel getNotificationAt(int position) {
        return deleteNotificationModels.get(position);
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

    private Bitmap getFallbackIcon() {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.baseline_android_24);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView appName, notificationHeading, notificationContent, notificationDateTime;
        private final ImageView appIcon, imageButtonMenuActionBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            notificationHeading = itemView.findViewById(R.id.notificationHeading);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationDateTime = itemView.findViewById(R.id.notificationDateTime);
            appIcon = itemView.findViewById(R.id.appIcon);

            imageButtonMenuActionBar = itemView.findViewById(R.id.imageButtonMenuActionBar);
            imageButtonMenuActionBar.setClickable(true);
        }
    }

    public interface OnDeleteNotificationListener {
        void onDeleteNotification(DeleteNotificationModel model, int position);
        void onRestoreNotification(DeleteNotificationModel model, int position);
    }
}
