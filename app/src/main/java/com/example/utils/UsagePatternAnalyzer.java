package com.example.utils;

import android.app.usage.UsageEvents;
import java.util.HashMap;
import java.util.Map;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UsagePatternAnalyzer {

    private static final String TAG = "USAGE_ANALYZER";

    public static java.util.Map<String, Long> getLastHourUsageTime(
        android.content.Context context) {

    android.app.usage.UsageStatsManager usageStatsManager =
            (android.app.usage.UsageStatsManager)
                    context.getSystemService(android.content.Context.USAGE_STATS_SERVICE);

    if (usageStatsManager == null) {
        android.util.Log.e("USAGE_DEBUG", "UsageStatsManager is null");
        return new java.util.HashMap<>();
    }

    long endTime = System.currentTimeMillis();
    long startTime = endTime - (60 * 60 * 1000); // last 1 hour

    android.app.usage.UsageEvents usageEvents =
            usageStatsManager.queryEvents(startTime, endTime);

    java.util.Map<String, Long> appUsageMap = new java.util.HashMap<>();
    java.util.Map<String, Long> foregroundStartMap = new java.util.HashMap<>();

    android.app.usage.UsageEvents.Event event =
            new android.app.usage.UsageEvents.Event();

    while (usageEvents.hasNextEvent()) {

        usageEvents.getNextEvent(event);

        String packageName = event.getPackageName();

        if (packageName == null)
            continue;

        if (packageName.contains("systemui") ||
            packageName.contains("launcher") ||
            packageName.contains("settings") ||
            packageName.contains("securitycenter") ||
            packageName.contains("com.example"))
            continue;

        if (event.getEventType() ==
                android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {

            foregroundStartMap.put(packageName, event.getTimeStamp());

        } else if (event.getEventType() ==
                android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND) {

            Long start = foregroundStartMap.remove(packageName);

            if (start != null) {

                long duration = event.getTimeStamp() - start;

                long current =
                        appUsageMap.getOrDefault(packageName, 0L);

                appUsageMap.put(packageName,
                        current + duration);
            }
        }
    }

    return appUsageMap;
}
}
