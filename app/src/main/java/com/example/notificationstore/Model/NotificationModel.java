package com.example.notificationstore.Model;

public class NotificationModel {
    private String appName;
    private String notificationContent;
    private long notificationDateTime;
    private Integer appIcon;

    public NotificationModel(String appName, String notificationContent, long notificationDateTime, Integer appIcon) {
        this.appName = appName;
        this.notificationContent = notificationContent;
        this.notificationDateTime = notificationDateTime;
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    public long getNotificationDateTime() {
        return notificationDateTime;
    }

    public void setNotificationDateTime(long notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }

    public Integer getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Integer appIcon) {
        this.appIcon = appIcon;
    }
}
