package com.example.notificationstore.Model;

public class NotificationModel {
    private String appName;
    private String notificationContent;
    private long notificationDateTime;
    private String appIconBase64;

    public NotificationModel(){

    }

    public NotificationModel(String appName, String notificationContent, long notificationDateTime, String appIconBase64) {
        this.appName = appName;
        this.notificationContent = notificationContent;
        this.notificationDateTime = notificationDateTime;
        this.appIconBase64 = appIconBase64;
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

    public String getAppIconBase64() {
        return appIconBase64;
    }

    public void setAppIconBase64(String appIconBase64) {
        this.appIconBase64 = appIconBase64;
    }
}
