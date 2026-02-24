package com.example.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TransitionTracker {
    private static final String TAG = "TRANSITIONS";
    private static final String PREF_NAME = "transition_prefs";
    private static final String KEY_PREFIX = "transition_";
    private static final String KEY_LAST_APP = "last_app";

    private static String lastApp = null;

   public static void recordTransition(Context context, String currentPackage) {

        if (currentPackage == null) return;

        // 🚫 Ignore system / noise packages
        if (isIgnoredPackage(context, currentPackage)) return;

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String lastApp = prefs.getString(KEY_LAST_APP, null);

        // First launch case
        if (lastApp == null) {
            prefs.edit().putString(KEY_LAST_APP, currentPackage).apply();
            return;
        }

        // Same app transition ignore
        if (lastApp.equals(currentPackage)) return;

        // Ignore if last app was noise
        if (isIgnoredPackage(context, lastApp)) {
            prefs.edit().putString(KEY_LAST_APP, currentPackage).apply();
            return;
        }

        String key = KEY_PREFIX + lastApp + "->" + currentPackage;

        int count = prefs.getInt(key, 0);
        count++;

        prefs.edit()
                .putInt(key, count)
                .putString(KEY_LAST_APP, currentPackage)
                .apply();

        Log.e("TRANSITIONS",
                "Recorded: " + lastApp + "->" +
                currentPackage + " Count: " + count);
    }
    public static Map<String, Integer> getAllTransitions(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Map<String, ?> allEntries = prefs.getAll();
        Map<String, Integer> result = new HashMap<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {

            if (entry.getKey().startsWith(KEY_PREFIX)) {

                String cleanKey =
                        entry.getKey().replace(KEY_PREFIX, "");

                result.put(cleanKey, (Integer) entry.getValue());
            }
        }

        return result;
    }
  
    private static boolean isIgnoredPackage(Context context, String pkg) {

        if (pkg == null) return true;

        if (pkg.equals(context.getPackageName())) return true;

        if (pkg.equals("com.android.systemui")) return true;
        if (pkg.contains("launcher")) return true;

        return false;
    }
    public static Map<String, Integer> getStrongTransitions(Context context, int minCount) {

        Map<String, Integer> all = getAllTransitions(context);
        Map<String, Integer> strong = new HashMap<>();

        for (Map.Entry<String, Integer> entry : all.entrySet()) {

            if (entry.getValue() >= minCount) {
                strong.put(entry.getKey(), entry.getValue());
            }
        }

        return strong;
    }
}