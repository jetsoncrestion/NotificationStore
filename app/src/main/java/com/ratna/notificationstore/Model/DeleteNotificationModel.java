package com.ratna.notificationstore.Model;

public class DeleteNotificationModel {
    private String uniqueKey;
    private String appName;
    private long timeStamp;
    private String notificationContent;
    private String packageName;
    private String imageBase64;
    private String notificationHeading;
    private long notificationDateTime;
    private String appIconBase64;
    private String imageButtonDelete;


    public DeleteNotificationModel() {
        this.timeStamp = System.currentTimeMillis();
        // Default constructor required for Firebase
    }

    public DeleteNotificationModel(String uniqueKey, String appName, long timeStamp, String notificationContent, String packageName, String imageBase64, String notificationHeading, long notificationDateTime, String appIconBase64, String imageButtonDelete) {
        this.uniqueKey = uniqueKey;
        this.appName = appName;
        this.timeStamp = timeStamp;
        this.notificationContent = notificationContent;
        this.packageName = packageName;
        this.imageBase64 = imageBase64;
        this.notificationHeading = notificationHeading;
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

    public String getPackageName(){
        return packageName;
    }
    public void setPackageName(String packageName){
        this.packageName = packageName;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getNotificationHeading() {
        return notificationHeading;
    }

    public void setNotificationHeading(String notificationHeading) {
        this.notificationHeading = notificationHeading;
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
