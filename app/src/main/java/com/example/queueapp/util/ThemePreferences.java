package com.example.queueapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemePreferences {

    private static final String PREFS_NAME = "wequeue_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private static ThemePreferences instance;

    private final SharedPreferences prefs;

    private ThemePreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ThemePreferences getInstance(Context context) {
        if (instance == null) {
            instance = new ThemePreferences(context);
        }
        return instance;
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        AppCompatDelegate.setDefaultNightMode(enabled
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void applySavedTheme(Context context) {
        boolean dark = getInstance(context).isDarkMode();
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
