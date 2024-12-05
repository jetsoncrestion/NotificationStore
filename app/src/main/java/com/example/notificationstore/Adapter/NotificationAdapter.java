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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Model.NotificationModel;
import com.example.notificationstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        holder.appName.setText(notificationModel.getAppName());
        holder.notificationContent.setText(notificationModel.getNotificationContent());

        long timestamp = notificationModel.getNotificationDateTime();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        holder.notificationDateTime.setText(formattedDate);

        String appIconBase64 = notificationModel.getAppIconBase64();
        if (appIconBase64 != null && !appIconBase64.isEmpty()) {
            Bitmap bitmap = decodeBase64ToBitmap(appIconBase64);
            if (bitmap != null) {
                holder.appIcon.setImageBitmap(bitmap);
            } else {
                Log.e("NotificationAdapter", "Failed to decode app icon Base64. Using fallback icon.");
                holder.appIcon.setImageResource(R.drawable.baseline_android_24);
            }
        } else {
            Log.e("NotificationAdapter", "App icon Base64 is null or empty. Using fallback icon.");
            holder.appIcon.setImageResource(R.drawable.baseline_android_24);
        }

        holder.imageButtonDelete.setOnClickListener(v -> {
            deleteNotification(position);
        });
    }

    private void deleteNotification(int position) {
        if (notificationModels == null || notificationModels.isEmpty() || position < 0 || position >= notificationModels.size()) {
            Log.e("NotificationAdapter", "Invalid position or notification list is null.");
            return;
        }

        NotificationModel model = notificationModels.get(position);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid() == null) {
            Log.e("NotificationAdapter", "User not authenticated. Cannot delete notification.");
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notifications");

        String uniqueKey = model.getUniqueKey();

        Log.d("NotificationAdapter", "Attempting to delete notification with key: " + uniqueKey);

        if (uniqueKey == null) {
            Log.e("NotificationAdapter", "Unique key is null for notification at position: " + position);
            Toast.makeText(context, "Failed to find item in Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(uniqueKey).removeValue().addOnSuccessListener(aVoid -> {

//            notificationModels.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notificationModels.size());
            Log.d("NotificationAdapter", "Notification deleted successfully.");
            Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("NotificationAdapter", "Failed to delete notification: " + e.getMessage());
            Toast.makeText(context, "Failed to delete notification", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView appName, notificationContent, notificationDateTime;
        private ImageView appIcon, imageButtonDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationDateTime = itemView.findViewById(R.id.notificationDateTime);
            appIcon = itemView.findViewById(R.id.appIcon);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);
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
