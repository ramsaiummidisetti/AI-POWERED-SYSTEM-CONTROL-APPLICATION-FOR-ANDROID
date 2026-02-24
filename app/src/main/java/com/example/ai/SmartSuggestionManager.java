package com.example.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.accessibility.UniversalControlService;
import com.example.MainActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SmartSuggestionManager {

    private static final String TAG = "SUGGESTION";
    private static final boolean DEV_MODE = true;

    private static final long DOMINANT_THRESHOLD =
            DEV_MODE ? 20000 : 600000; // 20 sec dev / 10 min prod

    private static final long GLOBAL_COOLDOWN =
            DEV_MODE ? 60000 : 10 * 60 * 1000; // 1 min dev / 10 min prod

    private static final long EXECUTION_COOLDOWN = DEV_MODE ? 30000 : 2 * 60 * 1000; // 30 SEC DEV / 2 MIN PROD 

    private static long lastSuggestionTime = 0;
    private static final java.util.Map<String, Long> lastExecutionTime = new java.util.HashMap<>();
    private static final Set<String> sessionSuggestedApps = new HashSet<>();
    private static final Set<String> permanentlyRejectedApps = new HashSet<>();
    private static final Set<String> activeAutomations = new HashSet<>();

    private static final String PREF_NAME = "automation_prefs";
    private static final String KEY_AUTOMATIONS = "active_automations";

    private static boolean dialogShowing = false;

    // ==========================================================
    // MAIN ENTRY
    // ==========================================================

    public static void evaluateAndSuggest(
            Context context,
            String appPackage,
            long foregroundTime) {

        if (context == null || appPackage == null)
            return;

        loadAutomations(context);

        // If automation already active → execute directly
        if (activeAutomations.contains(appPackage)) {

            long now = System.currentTimeMillis();
            long lastTime = lastExecutionTime.getOrDefault(appPackage, 0L);

            if (now - lastTime < EXECUTION_COOLDOWN) {
                Log.e("AUTOMATION", "Execution cooldown active for: " + appPackage);
                return;
            }

            lastExecutionTime.put(appPackage, now);

            Log.e("AUTOMATION", "Executing automation for: " + appPackage);

            executeAutomation(context, appPackage);
            return;
        }

        long now = System.currentTimeMillis();

        if (permanentlyRejectedApps.contains(appPackage)) {
            Log.e(TAG, "App permanently rejected in this session");
            return;
        }

        if (foregroundTime < DOMINANT_THRESHOLD) {
            Log.e(TAG, "Below dominant threshold");
            return;
        }

        if (now - lastSuggestionTime < GLOBAL_COOLDOWN) {
            Log.e(TAG, "Cooldown active");
            return;
        }

        if (sessionSuggestedApps.contains(appPackage)) {
            Log.e(TAG, "Already suggested this session");
            return;
        }

        if (dialogShowing) {
            Log.e(TAG, "Dialog already showing");
            return;
        }

        lastSuggestionTime = now;
        sessionSuggestedApps.add(appPackage);

        showSuggestion(context, appPackage);
    }

    // ==========================================================
    // SUGGESTION DIALOG
    // ==========================================================

    private static void showSuggestion(Context context, String appPackage) {

        if (!(context instanceof Activity)) {
            Log.e(TAG, "Context not Activity. Dialog not shown.");
            return;
        }

        Activity activity = (Activity) context;

        activity.runOnUiThread(() -> {

            dialogShowing = true;

            new AlertDialog.Builder(activity)
                    .setTitle("Smart Suggestion")
                    .setMessage("You spent significant time in this app.\nCreate automation?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (d, w) -> {

                        activeAutomations.add(appPackage);
                        saveAutomation(context, appPackage);

                        Log.e("AUTOMATION", "Automation CREATED for: " + appPackage);

                        dialogShowing = false;
                        d.dismiss();
                    })
                    .setNegativeButton("No", (d, w) -> {
                        permanentlyRejectedApps.add(appPackage);
                        dialogShowing = false;d.dismiss();
                    })
                    .show();
        });
    }

    // ==========================================================
    // EXECUTION (STABLE VERSION)
    // ==========================================================

    private static void executeAutomation(Context context, String appPackage) {

        UniversalControlService service =
                UniversalControlService.getInstance();

        if (service == null) {
            Log.e("AUTOMATION", "Accessibility service NULL");
            return;
        }

        // Only execute if target app is foreground
        if (!appPackage.equals(service.getCurrentPackage())) {
            Log.e("AUTOMATION", "Target app not foreground. Skipping execution.");
            return;
        }

        if (!(context instanceof MainActivity)) {
            Log.e("AUTOMATION", "Context not MainActivity");
            return;
        }

        MainActivity activity = (MainActivity) context;

        // Stabilization delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (!appPackage.equals(service.getCurrentPackage())) {
                Log.e("AUTOMATION", "Foreground changed. Cancel execution.");
                return;
            }

            Log.e("AUTOMATION", "Executing automation for: " + appPackage);

            List<String> actions = new ArrayList<>();
            actions.add("scroll");

            ScriptEngine.execute(
                    activity.getCommandOrchestrator(),
                    actions
            );

        }, 1200); // 1.2 sec UI stabilization
    }

    // ==========================================================
    // STORAGE
    // ==========================================================

    private static void saveAutomation(Context context, String appPackage) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Set<String> stored =
                prefs.getStringSet(KEY_AUTOMATIONS, new HashSet<>());

        Set<String> updated = new HashSet<>(stored);
        updated.add(appPackage);

        prefs.edit().putStringSet(KEY_AUTOMATIONS, updated).apply();
    }

    private static void loadAutomations(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Set<String> stored =
                prefs.getStringSet(KEY_AUTOMATIONS, new HashSet<>());

        activeAutomations.clear();
        activeAutomations.addAll(stored);
    }

    // ==========================================================

    public static void resetSession() {
        sessionSuggestedApps.clear();
        permanentlyRejectedApps.clear();
        dialogShowing = false;
    }

    public static boolean isAutomationActive(String appPackage) {
        return activeAutomations.contains(appPackage);
    }
    public static Set<String> getActiveAutomations(Context context) {

        android.content.SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Set<String> stored =
                prefs.getStringSet(KEY_AUTOMATIONS, new HashSet<>());

        return new HashSet<>(stored);
    }
        public static void removeAutomation(Context context, String appPackage) {

        android.content.SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Set<String> stored =
                new HashSet<>(prefs.getStringSet(KEY_AUTOMATIONS, new HashSet<>()));

        stored.remove(appPackage);

        prefs.edit().putStringSet(KEY_AUTOMATIONS, stored).apply();

        activeAutomations.remove(appPackage);

                android.util.Log.e("AUTOMATION",
                        "Automation REMOVED for: " + appPackage);
                        UniversalControlService service =
                UniversalControlService.getInstance();

        if (service != null) {
            service.forceStopAutomation(appPackage);
        }
    }
}