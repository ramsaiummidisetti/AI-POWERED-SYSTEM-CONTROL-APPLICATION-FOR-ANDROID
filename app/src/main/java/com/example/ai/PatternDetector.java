package com.example.ai;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternDetector {

    private static final String TAG = "PATTERN_DETECTOR";

    public static String detectMostFrequentApp(List<String> apps) {

        if (apps == null || apps.isEmpty())
            return null;

        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String app : apps) {

            frequencyMap.put(app,
                    frequencyMap.getOrDefault(app, 0) + 1);
        }

        String mostUsedApp = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {

            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostUsedApp = entry.getKey();
            }
        }

        Log.e(TAG, "Most used: " + mostUsedApp +
                " Count: " + maxCount);

        return mostUsedApp;
    }
}