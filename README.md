📱 AI-Powered System Control App – Phase 1 (Month 2)

This repository documents the Phase 1 → Month 2 progress of the AI-Powered System Control App.
We implemented notifications, permissions, logging, alerts, scheduling, and app usage monitoring.

📌 Week 1: Notifications & Permissions
🎯 Goal

Build custom notifications.

Handle runtime permissions (Camera, Storage).

Create UI with buttons to toggle theme, submit, request permissions.

🔧 Key Code
NotificationHelper.java
package com.example.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "my_channel";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "App Channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void sendSimpleNotification(Context context, int id, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());
    }
}

MainActivity.java (Week 1 Section)
Button notifyButton = findViewById(R.id.notifyButton);
notifyButton.setOnClickListener(v -> {
    NotificationHelper.sendSimpleNotification(
        this, 1001, "Hello!", "This is your first custom notification."
    );
});

Permissions
String[] permissions = {
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE
};


📜 Manifest

<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

▶️ Testing

Run the app → Click Notify Button → Custom notification shows.

Tap Permission Button → Camera & Storage permissions requested.

📸 Screenshot Placeholder: Notification & Permission UI

📌 Week 2: Logging & Alerts
🎯 Goal

Log app events to a JSON file.

Manage alerts with receivers.

Display logs in RecyclerView Dashboard.

🔧 Key Code
LogManager.java
package com.example.utils;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class LogManager {
    private static final String FILE_NAME = "app_logs.json";
    private Context context;

    public LogManager(Context context) { this.context = context; }

    public void logEvent(LogEvent event) {
        try {
            JSONArray logs = readLogs();
            logs.put(event.toJSON());
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(logs.toString().getBytes());
            fos.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JSONArray readLogs() {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            return new JSONArray(new String(buffer));
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}

LogEvent.java
package com.example.utils;

import org.json.JSONObject;

public class LogEvent {
    private String type, level, source;
    private JSONObject meta;

    public LogEvent(String type, String level, String source, JSONObject meta) {
        this.type = type;
        this.level = level;
        this.source = source;
        this.meta = meta;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", type);
            obj.put("level", level);
            obj.put("source", source);
            obj.put("meta", meta);
        } catch (Exception e) { e.printStackTrace(); }
        return obj;
    }
}

RecyclerView Dashboard
RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
recyclerView.setLayoutManager(new LinearLayoutManager(this));
List<String> items = new ArrayList<>();
items.add("File deleted event logged");
DashboardAdapter adapter = new DashboardAdapter(items);
recyclerView.setAdapter(adapter);

▶️ Testing

Run app → Perform actions → JSON log created.

Open Dashboard → View latest log entries.

📸 Screenshot Placeholder: Log Dashboard UI

📌 Week 3: Scheduling & App Usage Monitoring
🎯 Goal

Schedule daily reminders using AlarmManager.

Track app usage stats via UsageStatsManager.

Display usage in UI.

🔧 Key Code
SchedulerHelper.java
package com.example.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class SchedulerHelper {
    public static void scheduleDailyNotification(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 9 AM
        calendar.set(Calendar.MINUTE, 0);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}

UsageStatsHelper.java
package com.example.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UsageStatsHelper {
    public static String getUsageSummary(Context context) {
        UsageStatsManager usm = (UsageStatsManager) 
                context.getSystemService(Context.USAGE_STATS_SERVICE);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.HOURS.toMillis(1);

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        StringBuilder sb = new StringBuilder();
        if (stats != null) {
            for (UsageStats stat : stats) {
                long totalTime = stat.getTotalTimeInForeground() / 1000;
                if (totalTime > 0) {
                    sb.append(stat.getPackageName()).append(" : ")
                      .append(totalTime).append(" sec\n");
                }
            }
        } else {
            sb.append("No usage data available.\n");
        }
        return sb.toString();
    }
}

MainActivity.java (Week 3 Section)
// Schedule reminder
Intent alarmIntent = new Intent(this, SnoozeActionReceiver.class);
SchedulerHelper.scheduleDailyNotification(this, alarmIntent);

// Show usage stats
String usageReport = UsageStatsHelper.getUsageSummary(this);
Toast.makeText(this, usageReport, Toast.LENGTH_LONG).show();


📜 Manifest

<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions"/>

<receiver android:name=".utils.SnoozeActionReceiver"/>

▶️ Testing

Run the app → Grant Notification + Usage Access permissions.

Wait until scheduled time (9 AM by default, can reduce for testing).

See notification pop up + usage stats in Toast/RecyclerView.

📸 Screenshot Placeholder: Usage Stats UI

✅ Summary of Phase 1 (Month 2)

Week 1: Notifications & Permissions.

Week 2: Logging & Alerts.

Week 3: Scheduling & App Usage Monitoring.

This completes the Phase 1 → Month 2 roadmap 🚀.
Next, we will extend features in Month 3 (Smart Suggestions, Optimization, etc.).
