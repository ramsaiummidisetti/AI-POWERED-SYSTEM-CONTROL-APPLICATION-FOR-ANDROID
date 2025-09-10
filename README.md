📱 AI-Powered System Control App – Phase 1 (Month 2)

This repository contains the Phase 1 → Month 2 progress of the AI-Powered System Control App project.
We covered Week 1, Week 2, and Week 3 development tasks step by step.

📌 Week 1: Notifications & Permissions
🎯 Goal

Implement custom notifications.

Handle runtime permissions (Camera, Storage).

Create basic UI buttons to trigger notifications and permissions.

📝 Implemented

Added NotificationHelper.java to manage notification channels and actions.

Added permission handling with ActivityResultLauncher.

🔧 Code Snippet
// MainActivity.java (Week 1 Part)
NotificationHelper.createChannel(this);

Button notifyButton = findViewById(R.id.notifyButton);
notifyButton.setOnClickListener(v -> {
    NotificationHelper.sendSimpleNotification(
        this,
        1001,
        "Hello!",
        "This is your first custom notification."
    );
});

📜 Manifest
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

▶️ Testing

Run the app → Tap Notify Button → Custom notification appears.

Tap Permission Button → Camera & Storage runtime permission request.

📸 Screenshot Placeholder: Notification demo

📌 Week 2: Logging & Alerts
🎯 Goal

Create a JSON-based logging system.

Implement alert manager for warnings.

Display logs in RecyclerView Dashboard.

📝 Implemented

Added LogManager.java to write JSON logs into app_logs.json.

Added AlertManager.java to register/unregister system alerts.

Created DashboardAdapter.java for RecyclerView UI.

🔧 Code Snippet
// Logging Example
JSONObject metaFile = new JSONObject();
metaFile.put("fileName", "example.txt");
metaFile.put("fileSize", 1024);

LogEvent fileDeleted = new LogEvent("file_deleted", "info", "app", metaFile);
logManager.logEvent(fileDeleted);

// RecyclerView Setup
RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
recyclerView.setLayoutManager(new LinearLayoutManager(this));
List<String> items = new ArrayList<>();
items.add("File deleted event logged");
DashboardAdapter adapter = new DashboardAdapter(items);
recyclerView.setAdapter(adapter);

📜 Manifest
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

▶️ Testing

Run the app → Perform actions → Logs stored in app_logs.json.

Open Dashboard → View latest 100 log entries.

📸 Screenshot Placeholder: Log dashboard view

📌 Week 3: Scheduling & App Usage Monitoring
🎯 Goal

Schedule daily reminders using AlarmManager/WorkManager.

Track app usage via UsageStatsManager.

Display usage in UI + Toast.

📝 Implemented

Added SchedulerHelper.java → schedules daily notification (default 9AM).

Added UsageStatsHelper.java → fetches last 1-hour app usage stats.

Integrated into MainActivity.

🔧 Code Snippets
SchedulerHelper.java
public class SchedulerHelper {
    public static void scheduleDailyNotification(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
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
public class UsageStatsHelper {
    public static String getUsageSummary(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

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
        }
        return sb.toString();
    }
}

MainActivity.java
// Schedule daily notification
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

Run the app → Grant Notification & Usage Access permissions.

Wait for scheduled notification (default 9AM, change for quick test).

Toast/RecyclerView shows app usage stats.

📸 Screenshot Placeholder: Usage stats UI

✅ Summary of Phase 1 (Month 2)

Week 1: Notifications & Permissions.

Week 2: Logging & Alerts.

Week 3: Scheduling & App Usage.

This completes Phase 1 (Month 2) roadmap progress 🚀.
