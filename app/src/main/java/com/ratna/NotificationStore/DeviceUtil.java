package com.ratna.NotificationStore;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class DeviceUtil {
    private static final String PREFS_NAME = "NotificationStorePrefs";
    private static final String DEVICE_ID_KEY = "DeviceID";

    public static String getOrGenerateDeviceId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String deviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_ID_KEY, deviceId);
            editor.apply();
        }
        return deviceId;
    }
}
