Got it Bittu 👍
Let’s continue from your **Week 2 completed project** into **Phase 1 → Month 2 → Week 3**.

Here’s your guide for **Week 3: Scheduling and App Usage Monitoring** (step-by-step so you can run it on your laptop and test):

---

## 📌 Week 3: Scheduling and App Usage Monitoring

### 🎯 Goal

* Use **AlarmManager / WorkManager** to schedule tasks in background.
* Use **UsageStatsManager** to track app usage and display it in UI.

---

### 📝 What We’ll Do

1. **Schedule a background task**

   * Example: Show a notification every day at a fixed time using `AlarmManager`.
   * Alternative: Use `WorkManager` for periodic tasks (preferred if you want reliability across reboots).

2. **Monitor app usage**

   * Use `UsageStatsManager` to fetch statistics like which apps were opened, how long they were used.
   * Requires **special permission** → `android.permission.PACKAGE_USAGE_STATS`.
   * You’ll guide the user to enable it manually in settings.

3. **Display results in UI**

   * Show the scheduled task firing.
   * Display app usage data in a `TextView` or `RecyclerView`.

---

### 📂 Files to Add in Your Project

👉 Inside `com.example.utils` create 2 new helpers:

#### 1️⃣ **SchedulerHelper.java**

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
```

#### 2️⃣ **UsageStatsHelper.java**

```java
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
```

---

### 🔧 Update `MainActivity.java`

Add inside `onCreate`:

```java
// Schedule daily notification
Intent alarmIntent = new Intent(this, SnoozeActionReceiver.class)
        .putExtra("notification_id", 4001);
SchedulerHelper.scheduleDailyNotification(this, alarmIntent);

// Show app usage stats
String usageReport = UsageStatsHelper.getUsageSummary(this);
Toast.makeText(this, usageReport, Toast.LENGTH_LONG).show();
```

---

### 📜 AndroidManifest.xml Changes

Add permissions:

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions"/>
```

Add receiver for AlarmManager:

```xml
<receiver android:name=".utils.SnoozeActionReceiver"/>
```

---

### ▶️ How to Run & Test

1. **Run the app** → Grant Notification + Storage + Camera permissions as before.
2. **Grant usage access manually**:

   * Go to: **Settings → Apps → Special Access → Usage Access**
   * Enable your app.
3. **Check scheduled notification**:

   * At **9 AM** (or adjust time in `SchedulerHelper`) → a notification appears.
   * For quick test, set time = `Calendar.getInstance().getTimeInMillis() + 60000` (1 min).
4. **Check usage stats**:

   * Toast shows which apps you used in the last 1 hour.
   * Extend to show in `TextView` if needed.

---

👉 Bittu, this completes **Week 3 base setup**.
Do you want me to also **write the Week 3 README file (like Week 1 & 2)** so you can upload it to GitHub directly?
