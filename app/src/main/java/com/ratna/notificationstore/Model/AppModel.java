package com.ratna.notificationstore.Model;

import android.graphics.drawable.Drawable;

public class AppModel {
    private String appName;
    private String packageName;
    private boolean isSelected;
    private Drawable appIcon;

    public AppModel(String appName, String packageName, boolean isSelected, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.isSelected = isSelected;
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}
