package com.ratna.notificationstore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BaseActivity extends AppCompatActivity {
    private BroadcastReceiver languageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recreate();
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPreferences = newBase.getSharedPreferences("AppSetting", MODE_PRIVATE);
        String languageCode = sharedPreferences.getString("Language", "en");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(languageChangeReceiver, new IntentFilter("LANGUAGE_CHANGE"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(languageChangeReceiver);
        super.onPause();
    }
}
