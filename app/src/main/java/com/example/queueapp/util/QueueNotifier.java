package com.example.queueapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.queueapp.api.model.QueueModel;

/**
 * Decides when to fire queue notifications by comparing the latest queue state
 * against the last state we already notified about. State is persisted so the
 * same alert is not raised twice (e.g. across repeated polls or app restarts).
 */
public final class QueueNotifier {

    private static final String PREFS_NAME = "wequeue_queue_state";
    private static final String KEY_QUEUE_ID = "last_queue_id";
    private static final String KEY_NOTIFIED_CALLED = "notified_called";
    private static final String KEY_NOTIFIED_ALMOST = "notified_almost";

    /** Notify "almost your turn" once position is at or below this. */
    private static final int ALMOST_THRESHOLD = 3;

    private QueueNotifier() {
    }

    /**
     * Evaluates the given queue (may be null when the user has no active queue)
     * and posts notifications for any newly reached milestone.
     */
    public static void evaluate(Context context, @Nullable QueueModel queue) {
        Context app = context.getApplicationContext();
        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        NotificationPreferences notifPrefs = NotificationPreferences.getInstance(app);

        if (queue == null) {
            // No active queue: clear tracking so a future queue starts fresh.
            prefs.edit().clear().apply();
            return;
        }

        int queueId = queue.getId();
        int trackedId = prefs.getInt(KEY_QUEUE_ID, -1);
        if (queueId != trackedId) {
            // New queue ticket: reset milestone tracking.
            prefs.edit()
                    .putInt(KEY_QUEUE_ID, queueId)
                    .putBoolean(KEY_NOTIFIED_CALLED, false)
                    .putBoolean(KEY_NOTIFIED_ALMOST, false)
                    .apply();
        }

        String status = queue.getStatus() != null ? queue.getStatus().toLowerCase() : "";
        boolean alreadyCalled = prefs.getBoolean(KEY_NOTIFIED_CALLED, false);
        boolean alreadyAlmost = prefs.getBoolean(KEY_NOTIFIED_ALMOST, false);

        boolean isCalled = "called".equals(status) || "serving".equals(status) || "ready".equals(status);

        if (isCalled && !alreadyCalled) {
            if (notifPrefs.shouldNotifyQueueCalled()) {
                NotificationHelper.notifyQueueCalled(app, safeNumber(queue));
            }
            prefs.edit().putBoolean(KEY_NOTIFIED_CALLED, true).apply();
            return;
        }

        boolean isAlmost = !isCalled
                && "waiting".equals(status)
                && queue.getPosition() > 0
                && queue.getPosition() <= ALMOST_THRESHOLD;

        if (isAlmost && !alreadyAlmost) {
            if (notifPrefs.shouldNotifyQueueAlmost()) {
                NotificationHelper.notifyQueueAlmost(app, safeNumber(queue), queue.getPosition());
            }
            prefs.edit().putBoolean(KEY_NOTIFIED_ALMOST, true).apply();
        }
    }

    private static String safeNumber(QueueModel queue) {
        return TextUtils.isEmpty(queue.getQueueNumber()) ? "-" : queue.getQueueNumber();
    }
}
