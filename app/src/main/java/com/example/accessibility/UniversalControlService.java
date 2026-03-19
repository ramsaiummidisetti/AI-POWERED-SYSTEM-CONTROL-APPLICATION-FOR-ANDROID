package com.example.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.ai.TransitionTracker;
import com.example.ai.SmartSuggestionManager;
import com.example.ai.PredictionOpenReceiver;
import com.example.ml.HybridPredictionEngine;

public class UniversalControlService extends AccessibilityService {

    private static UniversalControlService instance;
    private static final String TAG = "UNIVERSAL_CTRL";

    private String currentPackage = "";

    private boolean automationRunning = false;

    private Handler automationHandler = new Handler(Looper.getMainLooper());
    private Runnable automationRunnable;

    private int automationScrollCount = 0;
    private static final int MAX_SCROLLS = 3;

    private long lastPredictionTime = 0;
    private static final long PREDICTION_COOLDOWN = 30000;

    private boolean predictionActive = false;

    private long lastEventTime = 0;
    private static final long EVENT_DEBOUNCE = 800;

    public static UniversalControlService getInstance() {
        return instance;
    }

    public String getCurrentPackage() {
        return currentPackage;
    }

    // ==========================================================
    // SERVICE CONNECT
    // ==========================================================

    @Override
    public void onServiceConnected() {

        super.onServiceConnected();

        instance = this;

        HybridPredictionEngine.initialize(this);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        info.notificationTimeout = 50;

        setServiceInfo(info);

        Log.e(TAG, "Accessibility service connected");
    }

    // ==========================================================
    // ACCESSIBILITY EVENT
    // ==========================================================

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null)
            return;

        long now = System.currentTimeMillis();

        if (now - lastEventTime < EVENT_DEBOUNCE)
            return;

        lastEventTime = now;

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return;

        CharSequence pkg = event.getPackageName();

        if (pkg == null)
            return;

        String newPackage = pkg.toString();

        if (isUtilityApp(newPackage))
            return;

        if (newPackage.equals(currentPackage))
            return;

        String oldPackage = currentPackage;

        if (oldPackage == null || oldPackage.isEmpty()) {

            currentPackage = newPackage;

            Log.e(TAG, "ACTIVE PACKAGE: " + currentPackage);

            return;
        }

        // ==========================================================
        // SMART SUGGESTION CHECK
        // ==========================================================

        long foregroundTime = 20000;

        SmartSuggestionManager.evaluateAndSuggest(
                this,
                newPackage,
                foregroundTime);

        // ==========================================================
        // RECORD TRANSITION
        // ==========================================================

        TransitionTracker.recordTransition(
                this,
                oldPackage,
                newPackage);

        currentPackage = newPackage;

        Log.e(TAG, "ACTIVE PACKAGE: " + currentPackage);

        // ==========================================================
        // HYBRID PREDICTION
        // ==========================================================

        String predicted = null;

        try {

            predicted = HybridPredictionEngine.predictNextApp(
                    this,
                    newPackage);

        } catch (Exception e) {

            Log.e("HYBRID_ENGINE", "Prediction crashed", e);
        }

        if (predicted != null &&
                !predicted.equals(currentPackage) &&
                !predictionActive &&
                now - lastPredictionTime > PREDICTION_COOLDOWN) {

            predictionActive = true;

            lastPredictionTime = now;

            Log.e("HYBRID_ENGINE",
                    "Prediction fired: " + predicted);

            sendPredictionNotification(predicted);
        }

        // ==========================================================
        // AUTOMATION
        // ==========================================================

        if (SmartSuggestionManager.isAutomationActive(currentPackage)) {

            if (automationRunning)
                return;

            automationRunning = true;

            automationScrollCount = 0;

            automationRunnable = new Runnable() {

                @Override
                public void run() {

                    if (!SmartSuggestionManager.isAutomationActive(currentPackage)) {

                        stopAutomation();

                        return;
                    }

                    if (automationScrollCount >= MAX_SCROLLS) {

                        stopAutomation();

                        return;
                    }

                    performAction("scroll down");

                    automationScrollCount++;

                    automationHandler.postDelayed(
                            this,
                            10000);
                }
            };

            automationHandler.postDelayed(
                    automationRunnable,
                    2000);

        } else {

            if (automationRunning)
                stopAutomation();
        }
    }

    @Override
    public void onInterrupt() {

        Log.e(TAG, "Service interrupted");
    }

    // ==========================================================
    // ACTION EXECUTION
    // ==========================================================

    public void performAction(String command) {

        if (currentPackage == null)
            return;

        if (getPackageName().equals(currentPackage))
            return;

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null)
            return;

        command = command.toLowerCase().trim();

        if (command.contains("scroll down")) {

            root.performAction(
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

        } else if (command.contains("scroll up")) {

            root.performAction(
                    AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }

        root.recycle();
    }

    // ==========================================================
    // STOP AUTOMATION
    // ==========================================================

    private void stopAutomation() {

        if (!automationRunning)
            return;

        automationHandler.removeCallbacks(automationRunnable);

        automationRunning = false;

        automationScrollCount = 0;

        Log.e("AUTOMATION", "Automation stopped");

        resetPredictionFlag();
    }

    // ==========================================================
    // PREDICTION NOTIFICATION
    // ==========================================================

    private void sendPredictionNotification(String targetPackage) {

        NotificationManager manager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);

        String channelId = "prediction_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Smart Predictions",
                    NotificationManager.IMPORTANCE_HIGH);

            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(this,
                PredictionOpenReceiver.class);

        openIntent.putExtra("from_app", currentPackage);
        openIntent.putExtra("to_app", targetPackage);

        PendingIntent openPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("Smart Suggestion")
                .setContentText("Open predicted app?")
                .setAutoCancel(true)
                .addAction(0,
                        "Open",
                        openPendingIntent)
                .build();

        manager.notify(1001, notification);

        Log.e("HYBRID_ENGINE",
                "Sending suggestion for: " + targetPackage);
    }

    // ==========================================================
    // UTILITY APP FILTER
    // ==========================================================

    private boolean isUtilityApp(String pkg) {

        if (pkg == null)
            return true;

        if (pkg.equals(getPackageName()))
            return true;

        if (pkg.equals("com.android.systemui"))
            return true;

        if (pkg.contains("launcher"))
            return true;

        if (pkg.contains("settings"))
            return true;

        if (pkg.contains("securitycenter"))
            return true;

        if (pkg.contains("inputmethod"))
            return true;

        return false;
    }

    // ==========================================================
    // RESET PREDICTION FLAG
    // ==========================================================

    public void resetPredictionFlag() {

        predictionActive = false;

        Log.e(TAG, "Prediction flag reset");
    }

    // ==========================================================
    // FORCE STOP AUTOMATION
    // ==========================================================

    public void forceStopAutomation(String appPackage) {

        if (appPackage == null)
            return;

        if (!appPackage.equals(currentPackage))
            return;

        if (automationRunnable != null)
            automationHandler.removeCallbacks(automationRunnable);

        automationRunning = false;

        automationScrollCount = 0;

        Log.e("AUTOMATION",
                "Force stopped for: " + appPackage);
    }
}