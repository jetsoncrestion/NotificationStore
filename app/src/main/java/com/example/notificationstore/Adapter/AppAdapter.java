package com.example.notificationstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppModel app = appModels.get(position);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(app.getAppIcon());
        holder.toggleSwitch.setOnCheckedChangeListener(null);
        holder.toggleSwitch.setChecked(app.isSelected());

        holder.toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> app.setSelected(isChecked));

//        // Bind app data to the ViewHolder
//        holder.appName.setText(app.getAppName());
//        holder.appIcon.setImageDrawable(app.getAppIcon());
//        holder.appCheckbox.setOnCheckedChangeListener(null);
//        holder.appCheckbox.setChecked(app.isSelected());
//
//        // Handle checkbox state changes
//        holder.appCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> app.setSelected(isChecked));
    }

    @Override
    public int getItemCount() {
        return appModels.size();
    }
    public List<String> getSelectedApps() {
        // Return package names of selected apps
        return appModels.stream()
                .filter(AppModel::isSelected)
                .map(AppModel::getPackageName)
                .collect(Collectors.toList());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView appName;
        final ImageView appIcon;
        final SwitchMaterial toggleSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);
        }
    }
}