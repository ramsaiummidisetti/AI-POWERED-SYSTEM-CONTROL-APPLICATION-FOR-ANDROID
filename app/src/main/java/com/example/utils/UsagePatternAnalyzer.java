package com.example.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UsagePatternAnalyzer {

    private static final String TAG = "PATTERN";

    public static List<String> getRecentlyUsedApps(Context context) {

        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        List<String> result = new ArrayList<>();
        if (usm == null)
            return result;

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR, -1); // last 1 hour
        long startTime = cal.getTimeInMillis();

        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        if (stats == null)
            return result;

        for (UsageStats usage : stats) {
            if (usage.getTotalTimeInForeground() > 0) {
                result.add(usage.getPackageName());
                Log.e(TAG, "App used: " + usage.getPackageName());
            }
        }
        return result;
    }
}
