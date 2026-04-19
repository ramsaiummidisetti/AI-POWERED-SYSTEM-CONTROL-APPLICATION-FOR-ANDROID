package com.example.ai;

import android.content.Context;
import android.content.SharedPreferences;

public class AIMetricsManager {

    private static final String PREF = "ai_metrics";

    private static final String KEY_PREDICTIONS = "predictions";
    private static final String KEY_CORRECT = "correct";
    private static final String KEY_WRONG = "wrong";
    private static final String KEY_LAST_FROM = "last_from";
    private static final String KEY_LAST_PREDICTED = "last_predicted";

    // ==========================================================
    // 🔹 INTERNAL HELPER
    // ==========================================================

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ==========================================================
    // 🔹 PREDICTION COUNTERS
    // ==========================================================

    public static void incrementPrediction(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int count = prefs.getInt(KEY_PREDICTIONS, 0);
        prefs.edit().putInt(KEY_PREDICTIONS, count + 1).apply();
    }

    public static void incrementCorrect(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int count = prefs.getInt(KEY_CORRECT, 0);
        prefs.edit().putInt(KEY_CORRECT, count + 1).apply();
    }

    public static void incrementWrong(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int count = prefs.getInt(KEY_WRONG, 0);
        prefs.edit().putInt(KEY_WRONG, count + 1).apply();
    }

    // ==========================================================
    // 🔹 GETTERS
    // ==========================================================

    public static int getTotalPredictions(Context context) {
        return getPrefs(context).getInt(KEY_PREDICTIONS, 0);
    }

    public static int getCorrectPredictions(Context context) {
        return getPrefs(context).getInt(KEY_CORRECT, 0);
    }

    public static int getWrongPredictions(Context context) {
        return getPrefs(context).getInt(KEY_WRONG, 0);
    }

    // ==========================================================
    // 🔹 ACCURACY
    // ==========================================================

    public static int getAccuracy(Context context) {

        int total = getTotalPredictions(context);
        int correct = getCorrectPredictions(context);

        if (total == 0) return 0;

        return (correct * 100) / total;
    }

    // ==========================================================
    // 🔹 LAST PREDICTION TRACKING
    // ==========================================================

    public static void saveLastPrediction(Context context,
                                          String fromApp,
                                          String predictedApp) {

        if (fromApp == null || predictedApp == null) return;

        SharedPreferences prefs = getPrefs(context);

        prefs.edit()
                .putString(KEY_LAST_FROM, fromApp)
                .putString(KEY_LAST_PREDICTED, predictedApp)
                .apply();
    }

    public static String getLastPredictedApp(Context context) {
        return getPrefs(context).getString(KEY_LAST_PREDICTED, null);
    }

    public static String getLastFromApp(Context context) {
        return getPrefs(context).getString(KEY_LAST_FROM, null);
    }

    // ==========================================================
    // 🔹 RESET (VERY IMPORTANT FOR TESTING)
    // ==========================================================

    public static void resetMetrics(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}