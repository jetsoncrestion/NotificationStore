package com.example.notificationstore.Model;

public class NotificationModel {
    private String uniqueKey;
    private String appName;
    private String notificationContent;
    private long notificationDateTime;
    private String appIconBase64;
    private String imageButtonDelete;

    // Default constructor
    public NotificationModel() {
    }

    // Constructor with parameters
    public NotificationModel(String uniqueKey, String appName, String notificationContent, long notificationDateTime, String appIconBase64, String imageButtonDelete) {
        this.uniqueKey = uniqueKey;
        this.appName = appName;
        this.notificationContent = notificationContent;
        this.notificationDateTime = notificationDateTime;
        this.appIconBase64 = appIconBase64;
        this.imageButtonDelete = imageButtonDelete;
    }

    // Getters and setters
    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
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

    public String getImageButtonDelete() {
        return imageButtonDelete;
    }

    public void setImageButtonDelete(String imageButtonDelete) {
        this.imageButtonDelete = imageButtonDelete;
    }
}
