package com.example.queueapp;

import android.app.Application;

import com.example.queueapp.data.AppSession;
import com.example.queueapp.util.ThemePreferences;
import com.google.firebase.FirebaseApp;

public class WeQueueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePreferences.applySavedTheme(this);
        FirebaseApp.initializeApp(this);
        AppSession.getInstance().init(this);
    }
}
