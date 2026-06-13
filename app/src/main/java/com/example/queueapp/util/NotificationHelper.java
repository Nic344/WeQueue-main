package com.example.queueapp.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.queueapp.MainActivity;
import com.example.queueapp.R;

public final class NotificationHelper {

    public static final String CHANNEL_QUEUE = "queue_updates";

    private static final int ID_QUEUE_CALLED = 1001;
    private static final int ID_QUEUE_ALMOST = 1002;
    private static final int ID_TEST = 1099;

    private NotificationHelper() {
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_QUEUE,
                    context.getString(R.string.notif_channel_queue_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notif_channel_queue_desc));
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void notifyQueueCalled(Context context, String queueNumber) {
        String title = context.getString(R.string.notif_called_title);
        String body = context.getString(R.string.notif_called_body, queueNumber);
        post(context, ID_QUEUE_CALLED, title, body);
    }

    public static void notifyQueueAlmost(Context context, String queueNumber, int position) {
        String title = context.getString(R.string.notif_almost_title);
        String body = context.getString(R.string.notif_almost_body, position, queueNumber);
        post(context, ID_QUEUE_ALMOST, title, body);
    }

    public static void notifyTest(Context context) {
        post(context, ID_TEST,
                context.getString(R.string.notif_test_title),
                context.getString(R.string.notif_test_body));
    }

    private static void post(Context context, int id, String title, String body) {
        if (!hasPermission(context)) {
            return;
        }
        createChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, id, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_QUEUE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        } catch (SecurityException ignored) {

        }
    }
}
