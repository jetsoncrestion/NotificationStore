package com.ratna.notificationstore.Model;

public class CrashInfo {
        public String id;
        public String app_name;
        public String package_name;
        public String redirect;
        public String crash;
        public String message;

        // Default constructor required for calls to DataSnapshot.getValue(CrashInfo.class)
        public CrashInfo() {

        }

        // Constructor to initialize fields
        public CrashInfo(String id, String app_name, String package_name, String redirect, String crash, String message) {
            this.id = id;
            this.app_name = app_name;
            this.package_name = package_name;
            this.redirect = redirect;
            this.crash = crash;
            this.message = message;
        }
}
