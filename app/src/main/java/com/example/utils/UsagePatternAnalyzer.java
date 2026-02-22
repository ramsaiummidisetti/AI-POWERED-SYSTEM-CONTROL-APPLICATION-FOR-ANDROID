package com.example.utils;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UsagePatternAnalyzer {

    private static final String TAG = "USAGE_ANALYZER";

    public static List<String> getLastHourForegroundApps(Context context) {

        List<String> appSequence = new ArrayList<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            Log.e(TAG, "UsageStatsManager is null");
            return appSequence;
        }

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (60 * 60 * 1000); // last 1 hour

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        UsageEvents.Event event = new UsageEvents.Event();

        while (usageEvents.hasNextEvent()) {

            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {

                String packageName = event.getPackageName();

                if (packageName != null &&
                        !packageName.contains("systemui") &&
                        !packageName.contains("launcher") &&
                        !packageName.contains("com.example")) {

                    appSequence.add(packageName);
                    Log.e(TAG, "Foreground: " + packageName);
                }
            }
        }

        return appSequence;
    }
}