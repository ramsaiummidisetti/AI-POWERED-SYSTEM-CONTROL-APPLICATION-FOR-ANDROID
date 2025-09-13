package com.example.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UsageStatsHelper {

    public static String getTopUsageSummary(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = context.getPackageManager();

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (12 * 60 * 60 * 1000); // last 12h

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        if (stats == null || stats.isEmpty()) {
            return "No usage data (grant Usage Access in settings)";
        }

        Map<String, Long> usageMap = new HashMap<>();
        for (UsageStats usage : stats) {
            String pkg = usage.getPackageName();
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);

                // Skip system apps
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;

                String appName = pm.getApplicationLabel(appInfo).toString();
                long time = usage.getTotalTimeInForeground();

                usageMap.put(appName, usageMap.getOrDefault(appName, 0L) + time);
            } catch (Exception ignored) {}
        }

        // Sort by usage time
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(usageMap.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        // Build readable string
        StringBuilder sb = new StringBuilder("App Usage (Last 12h):\n");

        int count = 0;
        for (Map.Entry<String, Long> entry : sorted) {
            long millis = entry.getValue();
            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(millis) % 60);

            sb.append(entry.getKey()).append(" → ").append(time).append("\n");

            if (++count >= 5) break; // show top 5
        }

        return sb.toString();
    }
}