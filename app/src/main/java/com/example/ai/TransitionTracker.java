package com.example.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TransitionTracker {

    private static final String TAG = "TRANSITIONS";

    private static final String PREF_NAME = "transition_prefs";
    private static final String KEY_PREFIX = "transition_";

    // ==========================================================
    // RECORD TRANSITION
    // ==========================================================

    public static void recordTransition(Context context,
                                        String fromApp,
                                        String toApp) {

        if (fromApp == null || toApp == null) return;

        if (fromApp.equals(toApp)) return;

        if (isIgnoredPackage(context, fromApp)) return;

        if (isIgnoredPackage(context, toApp)) return;

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String key = KEY_PREFIX + fromApp + "->" + toApp;

        int count = prefs.getInt(key, 0) + 1;

        prefs.edit()
                .putInt(key, count)
                .apply();

        Log.e(TAG,
                "Recorded: " +
                        fromApp +
                        " -> " +
                        toApp +
                        " | Count: " +
                        count);
    }

    // ==========================================================
    // GET ALL TRANSITIONS
    // ==========================================================

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

    // ==========================================================
    // STRONG TRANSITIONS
    // ==========================================================

    public static Map<String, Integer> getStrongTransitions(Context context,
                                                            int minCount) {

        Map<String, Integer> all =
                getAllTransitions(context);

        Map<String, Integer> strong =
                new HashMap<>();

        for (Map.Entry<String, Integer> entry : all.entrySet()) {

            if (entry.getValue() >= minCount) {

                strong.put(entry.getKey(),
                        entry.getValue());
            }
        }

        return strong;
    }

    // ==========================================================
    // PREDICT NEXT APP
    // ==========================================================

    public static String predictNextApp(Context context,
                                        String currentApp) {

        Map<String, Integer> transitions =
                getAllTransitions(context);

        int totalFromCurrent = 0;

        // Calculate total outgoing transitions
        for (Map.Entry<String, Integer> entry : transitions.entrySet()) {

            if (entry.getKey().startsWith(currentApp + "->")) {

                totalFromCurrent += entry.getValue();
            }
        }

        if (totalFromCurrent == 0)
            return null;

        double maxConfidence = 0;

        String bestPrediction = null;

        for (Map.Entry<String, Integer> entry : transitions.entrySet()) {

            String key = entry.getKey();

            int count = entry.getValue();

            if (!key.startsWith(currentApp + "->"))
                continue;

            String nextApp =
                    key.substring((currentApp + "->").length());

            double confidence =
                    (double) count / totalFromCurrent;

            if (confidence > maxConfidence) {

                maxConfidence = confidence;

                bestPrediction = nextApp;
            }
        }

        // Suggest only if confidence >= 40%
        if (maxConfidence >= 0.3)
            return bestPrediction;

        return null;
    }

    // ==========================================================
    // REINFORCEMENT LEARNING
    // ==========================================================

    public static void reinforceTransition(Context context,
                                           String fromApp,
                                           String toApp) {

        if (fromApp == null || toApp == null)
            return;

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME,
                        Context.MODE_PRIVATE);

        String key =
                KEY_PREFIX + fromApp + "->" + toApp;

        int count =
                prefs.getInt(key, 0);

        int bonus = 2;

        prefs.edit()
                .putInt(key, count + bonus)
                .apply();

        Log.e(TAG,
                "Reinforced: " +
                        fromApp +
                        " -> " +
                        toApp +
                        " | NewCount: " +
                        (count + bonus));
    }

    // ==========================================================
    // TOTAL TRANSITIONS
    // ==========================================================

    public static int getTotalTransitions(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME,
                        Context.MODE_PRIVATE);

        int total = 0;

        for (String key : prefs.getAll().keySet()) {

            if (key.startsWith(KEY_PREFIX)) {

                total += prefs.getInt(key, 0);
            }
        }

        return total;
    }

    // ==========================================================
    // IGNORE SYSTEM APPS
    // ==========================================================

    private static boolean isIgnoredPackage(Context context,
                                            String pkg) {

        if (pkg == null) return true;

        if (pkg.equals(context.getPackageName()))
            return true;

        if (pkg.equals("com.android.systemui"))
            return true;

        if (pkg.contains("launcher"))
            return true;

        if (pkg.contains("inputmethod"))
            return true;

        return false;
    }
    public static void resetTransitions(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences("transition_prefs",
                        Context.MODE_PRIVATE);

        prefs.edit().clear().apply();

        Log.e("TRANSITIONS", "All transition data RESET");
    }
}