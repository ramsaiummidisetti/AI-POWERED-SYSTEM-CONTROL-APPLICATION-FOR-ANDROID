package com.example.ai;

import java.util.Map;
import android.app.usage.UsageStats;
import android.util.Pair;

import java.util.List;

public class PatternDetector {

        public static android.util.Pair<String, Long> detectDominantApp(
            java.util.Map<String, Long> usageMap) {

        if (usageMap == null || usageMap.isEmpty())
            return null;

        long totalTime = 0;
        String topApp = null;
        long maxTime = 0;

        final long MIN_ABSOLUTE_TIME = 20000; // 20 sec minimum

        for (java.util.Map.Entry<String, Long> entry : usageMap.entrySet()) {

            String pkg = entry.getKey();
            long time = entry.getValue();

            totalTime += time;

            if (time > maxTime) {
                maxTime = time;
                topApp = pkg;
            }
        }

        if (topApp == null || totalTime == 0)
            return null;

        if (maxTime < MIN_ABSOLUTE_TIME)
            return null;

        double dominanceRatio = (double) maxTime / totalTime;

        android.util.Log.e("AUTONOMY",
                "Top: " + topApp +
                " Time: " + maxTime +
                " Total: " + totalTime +
                " Ratio: " + dominanceRatio);

        if (dominanceRatio < 0.40)
            return null;

        return new android.util.Pair<>(topApp, maxTime);
    }
}