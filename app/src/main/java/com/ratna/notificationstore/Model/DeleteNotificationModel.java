package com.ratna.notificationstore.Model;

public class DeleteNotificationModel {
    private String uniqueKey;
    private String appName;
    private long timeStamp;
    private String notificationContent;
    private long notificationDateTime;
    private String appIconBase64;
    private String imageButtonDelete;


    public DeleteNotificationModel() {
        // Default constructor required for Firebase
    }

    public DeleteNotificationModel(String uniqueKey, String appName, long timeStamp, String notificationContent, long notificationDateTime, String appIconBase64, String imageButtonDelete) {
        this.uniqueKey = uniqueKey;
        this.appName = appName;
        this.timeStamp = timeStamp;
        this.notificationContent = notificationContent;
        this.notificationDateTime = notificationDateTime;
        this.appIconBase64 = appIconBase64;
        this.imageButtonDelete = imageButtonDelete;
    }

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
