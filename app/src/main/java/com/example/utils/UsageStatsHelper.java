package com.example.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UsageStatsHelper {

    public static String getTopUsageSummary(Context context) {
        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long end = System.currentTimeMillis();
            long start = end - TimeUnit.HOURS.toMillis(24); // last 24 hours
            List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);

            if (stats == null || stats.isEmpty()) {
                return "No usage data. Grant Usage Access.";
            }

            // sort by lastTimeUsed or totalTimeInForeground
            Collections.sort(stats, Comparator.comparingLong(UsageStats::getTotalTimeInForeground));
            // reverse for descending
            Collections.reverse(stats);

            PackageManager pm = context.getPackageManager();
            List<String> lines = new ArrayList<>();
            int count = 0;
            for (UsageStats s : stats) {
                long totalMs = s.getTotalTimeInForeground();
                if (totalMs <= 0) continue;
                String pkg = s.getPackageName();
                String label;
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                    label = pm.getApplicationLabel(ai).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    label = pkg;
                }
                // remove package-like names, keep only label (e.g., "YouTube", "WhatsApp")
                String hhmmss = formatMillis(totalMs);
                lines.add(label + " — " + hhmmss);
                count++;
                if (count >= 5) break;
            }
            if (lines.isEmpty()) return "No apps used in last 24h";
            return String.join("\n", lines);
        } catch (Exception e) {
            Log.e("UsageStatsHelper", "error", e);
            return "UsageStats error";
        }
    }

    private static String formatMillis(long ms) {
        long seconds = ms / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
