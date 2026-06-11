package com.example.queueapp;

import android.app.Application;

import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.util.NotificationHelper;
import com.example.queueapp.util.ThemePreferences;

public class WeQueueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePreferences.applySavedTheme(this);
        NotificationHelper.createChannels(this);
        ApiConfig.init(this);
        AppSession.getInstance().init(this);
    }
}
