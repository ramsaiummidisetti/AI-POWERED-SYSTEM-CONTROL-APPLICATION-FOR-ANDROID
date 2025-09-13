package com.example.utils;


import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.text.format.DateUtils;

import java.util.List;

public class UsageStatsHelper {

    public static String getUsageSummary(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return "UsageStats not available";

        long now = System.currentTimeMillis();
        long start = now - DateUtils.DAY_IN_MILLIS;

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                start,
                now
        );

        if (stats == null || stats.isEmpty()) {
            return "No usage data available (grant permission in settings)";
        }

        StringBuilder sb = new StringBuilder();
        for (UsageStats usage : stats) {
            long totalTime = usage.getTotalTimeInForeground();
            if (totalTime > 0) {
                sb.append(usage.getPackageName())
                  .append(" → ")
                  .append(totalTime / 1000).append(" sec\n");
            }
        }

        return sb.toString();
    }
}
