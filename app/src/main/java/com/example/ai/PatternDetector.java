package com.example.ai;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternDetector {

    private static final String TAG = "PATTERN";

    public static String detectRepeatedApp(List<String> apps) {

        Map<String, Integer> freq = new HashMap<>();

        for (String app : apps) {
            freq.put(app, freq.getOrDefault(app, 0) + 1);
        }

        for (String app : freq.keySet()) {
            if (freq.get(app) >= 3) {
                Log.e(TAG, "Pattern detected: " + app);
                return app;
            }
        }
        return null;
    }
}
