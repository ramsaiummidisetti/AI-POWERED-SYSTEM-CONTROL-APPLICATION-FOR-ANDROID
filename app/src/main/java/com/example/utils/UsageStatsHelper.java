package com.example.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class UsageStatsHelper {

    public static String getUsageSummary(Context context) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 12 * 60 * 60 * 1000; // last 12 hours

        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return "UsageStatsManager unavailable";

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        if (stats == null || stats.isEmpty()) {
            return "No usage data (grant permission in Settings)";
        }

        PackageManager pm = context.getPackageManager();
        Map<String, Long> usageMap = new HashMap<>();

        for (UsageStats usage : stats) {
            long time = usage.getTotalTimeInForeground();
            if (time > 0) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(usage.getPackageName(), 0);
                    String appName = pm.getApplicationLabel(appInfo).toString();

                    // ✅ don’t filter system apps (yet)
                    if (usageMap.containsKey(appName)) {
                        usageMap.put(appName, usageMap.get(appName) + time);
                    } else {
                        usageMap.put(appName, time);
                    }

                } catch (PackageManager.NameNotFoundException ignored) {}
            }
        }

        // Sort by usage time (descending)
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(usageMap.entrySet());
        Collections.sort(sorted, (e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));


        StringBuilder sb = new StringBuilder("App Usage (Last 12h):\n");
        for (Map.Entry<String, Long> entry : sorted) {
            sb.append(entry.getKey())
              .append(" → ")
              .append(formatTime(entry.getValue()))
              .append("\n");
        }

        return sb.toString();
    }

    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
    }
}
