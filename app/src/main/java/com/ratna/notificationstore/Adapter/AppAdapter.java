package com.ratna.notificationstore.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ratna.notificationstore.Model.AppModel;
import com.ratna.notificationstore.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private final List<AppModel> appModels;
    private final List<AppModel> appModelsFull;
    private final Context context;

    public AppAdapter(List<AppModel> appModels, Context context) {
        this.appModels = appModels;
        this.appModelsFull = new ArrayList<>(appModels);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return appModels.stream().filter(AppModel::isSelected).map(AppModel::getPackageName).collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    public Filter getFilter() {
        return appFilter;
    }

    private final Filter appFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<AppModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(appModelsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (AppModel app : appModelsFull) {
                    if (app.getAppName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(app);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            appModels.clear();
            appModels.addAll((List<AppModel>) results.values);
            notifyDataSetChanged();
        }
    };

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