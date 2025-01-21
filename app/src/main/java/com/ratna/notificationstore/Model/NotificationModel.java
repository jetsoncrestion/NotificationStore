package com.ratna.notificationstore.Model;

public class NotificationModel {
    private String uniqueKey;
    private String appName;
    private long timeStamp;
    private String notificationHeading;
    private String notificationContent;
    private long notificationDateTime;
    private String appIconBase64;
    private String imageButtonDelete;
    private String packageName;

    // Default constructor
    public NotificationModel() {
        this.timeStamp = System.currentTimeMillis();
    }

    // Constructor with parameters
    public NotificationModel(String uniqueKey, String appName, long timeStamp, String notificationHeading, String notificationContent, long notificationDateTime, String appIconBase64, String imageButtonDelete, String packageName) {
        this.uniqueKey = uniqueKey;
        this.appName = appName;
        this.timeStamp = timeStamp == 0 ? System.currentTimeMillis() : timeStamp;
        this.notificationHeading = notificationHeading;
        this.notificationContent = notificationContent;
        this.notificationDateTime = notificationDateTime;
        this.appIconBase64 = appIconBase64;
        this.imageButtonDelete = imageButtonDelete;
        this.packageName = packageName;
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
    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setNotificationHeading(String notificationHeading){
        this.notificationHeading = notificationHeading;
    }

    public String getNotificationHeading(){
        return notificationHeading;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
