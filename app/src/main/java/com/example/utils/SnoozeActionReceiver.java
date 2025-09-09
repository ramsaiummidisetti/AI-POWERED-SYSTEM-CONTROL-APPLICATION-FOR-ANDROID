package com.example.utils;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SnoozeActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", -1);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationId != -1 && nm != null) {
            nm.cancel(notificationId);
        }

        // Log snooze action
        LogUtils.writeLogAsync(context, new LogEvent(
                "notification_snoozed", "info", "user", null));
    }
}
