package com.example.queueapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists the user's notification preferences. These flags gate whether the
 * app is allowed to post a given kind of system notification.
 */
public final class NotificationPreferences {

    private static final String PREFS_NAME = "wequeue_notifications";
    private static final String KEY_MASTER = "notifications_enabled";
    private static final String KEY_QUEUE_CALLED = "notify_queue_called";
    private static final String KEY_QUEUE_ALMOST = "notify_queue_almost";

    private static NotificationPreferences instance;

    private final SharedPreferences prefs;

    private NotificationPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized NotificationPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationPreferences(context);
        }
        return instance;
    }

    public boolean isMasterEnabled() {
        return prefs.getBoolean(KEY_MASTER, true);
    }

    public void setMasterEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MASTER, enabled).apply();
    }

    public boolean isQueueCalledEnabled() {
        return prefs.getBoolean(KEY_QUEUE_CALLED, true);
    }

    public void setQueueCalledEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_QUEUE_CALLED, enabled).apply();
    }

    public boolean isQueueAlmostEnabled() {
        return prefs.getBoolean(KEY_QUEUE_ALMOST, true);
    }

    public void setQueueAlmostEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_QUEUE_ALMOST, enabled).apply();
    }

    /** Master switch AND the specific toggle must be on for "queue called" alerts. */
    public boolean shouldNotifyQueueCalled() {
        return isMasterEnabled() && isQueueCalledEnabled();
    }

    /** Master switch AND the specific toggle must be on for "almost your turn" alerts. */
    public boolean shouldNotifyQueueAlmost() {
        return isMasterEnabled() && isQueueAlmostEnabled();
    }
}
