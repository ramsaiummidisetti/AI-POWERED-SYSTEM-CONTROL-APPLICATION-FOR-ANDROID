package com.example.utils;

import android.content.*;
import com.example.MainActivity;
import org.json.JSONObject;

public class AlertManager {
    private Context ctx;
    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver storageReceiver;

    public AlertManager(Context ctx) {
        this.ctx = ctx;
        initReceivers();
    }

    public void sendSmartAlert(Context context, String message, String type) {
        // implementation
    }

    private void initReceivers() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtils.writeLogAsync(ctx, new LogEvent("battery_low", "warning", "system", null));
                Intent open = new Intent(ctx, MainActivity.class);
                NotificationHelper.sendBasicNotification(ctx, 1001, "Battery Low", "Please charge your device.", open);
            }
        };

        storageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtils.writeLogAsync(ctx, new LogEvent("storage_low", "warning", "system", null));
                Intent open = new Intent(ctx, MainActivity.class);
                NotificationHelper.sendBasicNotification(ctx, 1002, "Storage Low",
                        "Free up space to improve performance.", open);

                String taskId = intent.getStringExtra("TASK_ID");
                try {
                    JSONObject meta = new JSONObject();
                    meta.put("taskId", taskId);
                    meta.put("taskName", "reminder_alarm");

                    LogEvent taskExecuted = new LogEvent("task_executed", "info", "system", meta);
                    LogManager logManager = new LogManager(context);
                    logManager.logEvent(taskExecuted);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void register() {
        ctx.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        ctx.registerReceiver(storageReceiver, new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW));
    }

    public void unregister() {
        try {
            ctx.unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {
        }
        try {
            ctx.unregisterReceiver(storageReceiver);
        } catch (Exception ignored) {
        }
    }
}
