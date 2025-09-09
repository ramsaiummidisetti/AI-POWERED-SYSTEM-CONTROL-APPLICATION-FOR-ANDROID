package com.example.utils;

import android.app.*;
import android.content.*;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;

public class NotificationHelper {
    public static final String CHANNEL_ID = "smart_alerts";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

    public static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Smart Alerts", CHANNEL_IMPORTANCE);
            channel.setDescription("Alerts from AI System Control App");
            nm.createNotificationChannel(channel);
        }
    }

    public static void sendBasicNotification(Context ctx, int id, String title, String text, Intent openIntent) {
        PendingIntent pi = PendingIntent.getActivity(
                ctx, id, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, b.build());

        // Log notification event
        try {
            JSONObject meta = new JSONObject();
            meta.put("alertType", "battery");
            meta.put("level", 15);

            LogEvent notificationSent = new LogEvent("notification_sent", "warning", "system", meta);
            LogManager logManager = new LogManager(ctx);
            logManager.logEvent(notificationSent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Log prediction event (if applicable)
        try {
            JSONObject metaPrediction = new JSONObject();
            metaPrediction.put("prediction", "battery_low");
            metaPrediction.put("predictedLevel", 12);

            LogEvent prediction = new LogEvent("prediction_made", "warning", "app", metaPrediction);
            LogManager logManager = new LogManager(ctx);
            logManager.logEvent(prediction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendActionNotification(Context ctx, int id,
            String title, String text,
            Intent openIntent, Intent actionIntent) {
        PendingIntent openPi = PendingIntent.getActivity(
                ctx, id, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

        PendingIntent actionPi = PendingIntent.getBroadcast(
                ctx, id + 1000, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(openPi)
                .addAction(android.R.drawable.ic_media_pause, "Snooze", actionPi) // 👈 Snooze button
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, b.build());
    }

}