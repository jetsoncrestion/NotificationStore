package com.example.notificationstore.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Model.AppModel;
import com.example.notificationstore.R;

import java.util.List;
import java.util.stream.Collectors;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private final List<AppModel> appModels;
    private final Context context;

    public AppAdapter(List<AppModel> appModels, Context context) {
        this.appModels = appModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_layout, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppModel app = appModels.get(position);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(app.getAppIcon());

        boolean isChecked = app.isSelected();
        holder.toggleSwitch.setOnCheckedChangeListener(null);
        holder.toggleSwitch.setChecked(app.isSelected());
        holder.updateSwitchColors(isChecked);

        holder.toggleSwitch.setOnCheckedChangeListener((buttonView, isCheckedNew) -> {
            app.setSelected(isCheckedNew);
            holder.updateSwitchColors(isCheckedNew);
        });
    }

    @Override
    public int getItemCount() {
        return appModels.size();
    }

    public List<String> getSelectedApps() {
        // Return package names of selected apps
        return appModels.stream().filter(AppModel::isSelected).map(AppModel::getPackageName).collect(Collectors.toList());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView appName;
        final ImageView appIcon;
        final Switch toggleSwitch;

        private final int thumbOnColor;
        private final int thumbOffColor;
        private final int trackOnColor;
        private final int trackOffColor;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);

            thumbOnColor = ContextCompat.getColor(context, R.color.switch_thumb_on);
            thumbOffColor = ContextCompat.getColor(context, R.color.switch_thumb_off);
            trackOnColor = ContextCompat.getColor(context, R.color.switch_track_on);
            trackOffColor = ContextCompat.getColor(context, R.color.switch_track_off);
        }

        void updateSwitchColors(boolean isChecked) {
            if (isChecked) {
                toggleSwitch.setThumbTintList(ColorStateList.valueOf(thumbOnColor));
                toggleSwitch.setTrackTintList(ColorStateList.valueOf(trackOnColor));
            } else {
                toggleSwitch.setThumbTintList(ColorStateList.valueOf(thumbOffColor));
                toggleSwitch.setTrackTintList(ColorStateList.valueOf(trackOffColor));
            }
        }
    }
}