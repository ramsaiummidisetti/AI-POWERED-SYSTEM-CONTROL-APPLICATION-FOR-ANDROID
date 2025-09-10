📅 Week 3 – Scheduling & App Usage Monitoring

## 🎯 Goal
- Use **AlarmManager / WorkManager** to schedule tasks in the background.  
- Use **UsageStatsManager** to track app usage and display it in UI.  

---

## 📝 What We Implemented

### 1️⃣ SchedulerHelper – Daily Notifications
File: `com/example/utils/SchedulerHelper.java`

```java
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
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule at 9 AM every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}
2️⃣ UsageStatsHelper – Monitor App Usage
File: com/example/utils/UsageStatsHelper.java

java
Copy code
package com.example.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class UsageStatsHelper {

    public static String getUsageSummary(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.HOURS.toMillis(1); // last 1 hour

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

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
3️⃣ MainActivity.java Updates
java
Copy code
// Schedule daily notification
Intent alarmIntent = new Intent(this, SnoozeActionReceiver.class)
        .putExtra("notification_id", 4001);
SchedulerHelper.scheduleDailyNotification(this, alarmIntent);

// Show app usage stats
String usageReport = UsageStatsHelper.getUsageSummary(this);
Toast.makeText(this, usageReport, Toast.LENGTH_LONG).show();
4️⃣ AndroidManifest.xml Changes
xml
Copy code
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions"/>

<receiver android:name=".utils.SnoozeActionReceiver"/>
▶️ How to Run & Test
Run the app → Grant Notification + Storage + Camera permissions as before.

Grant usage access manually:

Settings → Apps → Special Access → Usage Access → Enable your app.

Test scheduled notification:

Default is 9 AM daily. For quick test, change code to +60000 (1 min).

Usage stats appear as a Toast with the list of apps used in the last hour.

📸 Screenshots
Daily Notification Example

Usage Stats Toast

