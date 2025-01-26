package com.ratna.notificationstore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Model.NotificationModel;
import com.ratna.notificationstore.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        holder.notificationHeading.setText(notificationModel.getNotificationHeading());
        holder.appName.setText(notificationModel.getAppName());
        holder.notificationContent.setText(notificationModel.getNotificationContent());

        long timestamp = notificationModel.getNotificationDateTime();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd / HH:mm", Locale.getDefault()).format(new Date(timestamp));
        holder.notificationDateTime.setText(formattedDate);

        String appIconBase64 = notificationModel.getAppIconBase64();
        if (appIconBase64 != null && !appIconBase64.isEmpty()) {
            Bitmap bitmap = decodeBase64ToBitmap(appIconBase64);
            holder.appIcon.setImageBitmap(bitmap != null ? bitmap : getFallbackIcon());
        } else {
            holder.appIcon.setImageResource(R.drawable.baseline_android_24);
        }

        holder.imageButtonDelete.setOnClickListener(v -> deleteNotification(position));
        holder.itemView.setOnClickListener(v -> {
            String packageName = notificationModel.getPackageName();
            if (packageName != null && !packageName.isEmpty()) {
                openApplication(packageName);
            } else {
                Toast.makeText(context, "Package name is missing", Toast.LENGTH_SHORT).show();
                Log.e("NotificationAdapter", "Missing package name for notification at position: " + position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            showNotificationDetailsDialog(notificationModel);
            return true;
        });
    }

    private void openApplication(String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Unable to open application", Toast.LENGTH_SHORT).show();
                Log.e("NotificationAdapter", "No launch intent available for package: " + packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening application", Toast.LENGTH_SHORT).show();
            Log.e("NotificationAdapter", "Exception while opening package: " + packageName, e);
        }
    }

    private Bitmap getFallbackIcon() {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.baseline_android_24);
    }

    private void deleteNotification(int position) {
        if (notificationModels == null || notificationModels.isEmpty() || position < 0 || position >= notificationModels.size()) {
            Log.e("NotificationAdapter", "Invalid position or notification list is null.");
            return;
        }

        NotificationModel model = notificationModels.get(position);

        // Get deviceId from SharedPreferences
        String deviceId = getDeviceId(context);
        if (deviceId == null) {
            Log.e("NotificationAdapter", "Device ID not found. Cannot delete notification.");
            Toast.makeText(context, "Device not recognized", Toast.LENGTH_SHORT).show();
            return;
        }

        String uniqueKey = model.getUniqueKey();
        if (uniqueKey == null) {
            Log.e("NotificationAdapter", "Unique key is null for notification at position: " + position);
            Toast.makeText(context, "Failed to find item in Firebase", Toast.LENGTH_SHORT).show();
            return;
        }
        notificationModels.remove(position);
        notifyItemRemoved(position);
//        notifyItemRangeChanged(position, notificationModels.size());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("notifications");

        DatabaseReference deletedNotificationsRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("deleted_notifications").child(uniqueKey);
        deletedNotificationsRef.setValue(model).addOnSuccessListener(aVoid -> {

            databaseReference.child(uniqueKey).removeValue().addOnSuccessListener(aVoid1 -> {
                Log.d("NotificationAdapter", "Notification deleted and saved to deleted_notifications successfully.");
                // Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e("NotificationAdapter", "Failed to delete notification from notifications: " + e.getMessage());
                Toast.makeText(context, "Failed to delete notification", Toast.LENGTH_SHORT).show();

                notificationModels.add(position, model);
                notifyItemInserted(position);
//                                notifyItemRangeChanged(position, notificationModels.size());
            });
        }).addOnFailureListener(e -> {
            Log.e("NotificationAdapter", "Failed to save notification to deleted_notifications: " + e.getMessage());
            Toast.makeText(context, "Failed to save deleted notification", Toast.LENGTH_SHORT).show();

            notificationModels.add(position, model);
            notifyItemInserted(position);
            notifyItemRangeChanged(position, notificationModels.size());
        });
    }

    private String getDeviceId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NotificationStorePrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("DeviceID", null);
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    private void showNotificationDetailsDialog(NotificationModel model) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.notification_item, null);

        TextView appName = dialogView.findViewById(R.id.textViewAppName);
        TextView notificationHeading = dialogView.findViewById(R.id.textViewNotificationHeading);
        TextView notificationContent = dialogView.findViewById(R.id.textViewNotificationContent);
        TextView close = dialogView.findViewById(R.id.textViewClose);

        appName.setText(model.getAppName());
        notificationHeading.setText(model.getNotificationHeading());
        notificationContent.setText(model.getNotificationContent());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setView(dialogView).create();

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(60);
                drawable.setColor(Color.WHITE);

                window.setBackgroundDrawable(drawable);
            }
        });

        dialog.show();

        close.setOnClickListener(v -> dialog.dismiss());
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView appName, notificationHeading, notificationContent, notificationDateTime;
        private ImageView appIcon, imageButtonDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            notificationHeading = itemView.findViewById(R.id.notificationHeading);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationDateTime = itemView.findViewById(R.id.notificationDateTime);
            appIcon = itemView.findViewById(R.id.appIcon);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);
            //imageViewMenuActionBar = itemView.findViewById(R.id.imageViewMenuActionBar);
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateData(List<NotificationModel> filteredList) {
        this.notificationModels = filteredList;
        notifyDataSetChanged();
    }

    public interface UniqueKeyCallback {
        void onUniqueKeyRetrieved(String uniqueKey);
    }
}
