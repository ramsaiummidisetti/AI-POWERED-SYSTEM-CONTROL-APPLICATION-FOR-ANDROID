package com.example.accessibility;

import android.os.Build;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;
import android.view.View;
import android.view.Gravity;
import android.graphics.PixelFormat;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import com.example.ai.SmartSuggestionManager;
import com.example.ai.TransitionTracker;
import com.example.ai.PatternEngine;
import com.example.ai.PredictionDismissReceiver;
import com.example.ai.PredictionOpenReceiver;
import com.example.utils.NotificationHelper;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;

public class UniversalControlService extends AccessibilityService {

    private static UniversalControlService instance;
    private static final String TAG = "UNIVERSAL_CTRL";

    private String currentPackage = "";
    private boolean automationRunning = false;
    private boolean predictionActive = false;
    private Handler automationHandler = new Handler(Looper.getMainLooper());
    private Runnable automationRunnable;
    private int automationScrollCount = 0;
    private static final int MAX_SCROLLS = 3;
    private long lastPredictionTime = 0;
    private static final long PREDICTION_COOLDOWN = 2 * 60 * 1000; // 2 MINUTES
    private static final int PREDICTION_NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "prediction_channel";

    public static UniversalControlService getInstance() {
        return instance;
    }

    public String getCurrentPackage() {
        return currentPackage;
    }

    public void resetPredictionFlag() {
        predictionActive = false;
    }
    // ==========================================================
    // SERVICE CONNECT
    // ==========================================================

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        info.notificationTimeout = 50;

        setServiceInfo(info);

        Log.e(TAG, "SERVICE CONNECTED");
    }

    // ==========================================================
    // PACKAGE TRACKING
    // ==========================================================
    @Override

    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null)
            return;

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return;

        CharSequence pkg = event.getPackageName();
        if (pkg == null)
            return;

        String newPackage = pkg.toString();

        // Ignore system noise early
        if (newPackage.equals(getPackageName()) ||
                newPackage.equals("com.android.systemui") ||
                newPackage.contains("launcher") ||
                newPackage.contains("minusscreen")) {
            return;
        }
        if (isUtilityApp(newPackage)) {
            return;
        }
        // ==============================
        // 🔮 Pattern Prediction
        // ==============================
        // If package didn't change, ignore
        if (newPackage.equals(currentPackage))
            return;

        String oldPackage = currentPackage;

        // 🚫 Ignore if old app is utility (don't learn/predict from utilities)
        if (isUtilityApp(oldPackage)) {
            currentPackage = newPackage;
            return;
        }

        // 🔮 Predict based on OLD package
        String predicted = PatternEngine.predictNextApp(this, oldPackage);

        long now = System.currentTimeMillis();

        if (predicted != null &&
                now - lastPredictionTime > PREDICTION_COOLDOWN) {

            lastPredictionTime = now;
            sendPredictionNotification(predicted);
        }

        // 📊 Record transition AFTER prediction
        TransitionTracker.recordTransition(this, newPackage);

        // 🔁 Update current
        currentPackage = newPackage;

        Log.e(TAG, "ACTIVE PACKAGE: " + currentPackage);

        // ==============================
        // 🔁 Automation Logic
        // ==============================

        if (SmartSuggestionManager.isAutomationActive(currentPackage)) {

            if (automationRunning)
                return;

            automationRunning = true;
            automationScrollCount = 0;

            Log.e("AUTOMATION",
                    "Starting repeating automation for: " + currentPackage);

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

                    Log.e("AUTOMATION", "Auto scroll executing...");
                    performAction("scroll down");

                    automationScrollCount++;

                    automationHandler.postDelayed(this, 10000);
                }
            };

            automationHandler.postDelayed(automationRunnable, 2000);

        } else {

            if (automationRunning) {
                stopAutomation();
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Service interrupted");
    }

    // ==========================================================
    // MAIN EXECUTION ENTRY
    // ==========================================================

    public void performAction(String command) {

        Log.e("UNIVERSAL_CTRL", "performAction called with: " + command);
        if (currentPackage == null) {
            Log.e(TAG, "No active package");
            return;
        }

        // 🚫 Ignore own app
        if (currentPackage.equals(getPackageName())) {
            Log.e(TAG, "Ignoring own app");
            return;
        }

        // 🚫 Ignore system UI
        if (currentPackage.contains("systemui")) {
            Log.e(TAG, "Ignoring system UI");
            return;
        }

        // 🚫 Ignore launcher
        if (currentPackage.contains("launcher")) {
            Log.e(TAG, "Ignoring launcher");
            return;
        }

        AccessibilityNodeInfo root = getSafeRoot();

        if (root == null) {
            Log.e(TAG, "Root not ready");
            return;
        }

        command = command.toLowerCase().trim();

        Log.e(TAG, "Trying action in package: " + currentPackage);

        if (command.contains("scroll down")) {
            scroll(root, true);
        } else if (command.contains("scroll up")) {
            scroll(root, false);
        } else if (command.startsWith("click ") || command.startsWith("tap ")) {

            String keyword = command
                    .replaceFirst("click ", "")
                    .replaceFirst("tap ", "");

            clickSemantic(root, keyword);
        } else if (command.startsWith("type ")) {

            String text = command.replaceFirst("type ", "");

            typeText(root, text);
        }

        root.recycle();
    }// ==========================================================
     // SAFE ROOT (NO BLOCKING)
     // ==========================================================

    private AccessibilityNodeInfo getSafeRoot() {

        for (int i = 0; i < 5; i++) {

            AccessibilityNodeInfo root = getRootInActiveWindow();

            if (root != null && root.getChildCount() > 0) {
                return root;
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        }

        Log.e(TAG, "Root not ready after wait");
        return null;
    }

    // ==========================================================
    // SCROLL
    // ==========================================================

    private void scroll(AccessibilityNodeInfo root, boolean forward) {

        Log.e(TAG, "Trying scroll in package: " + currentPackage);

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null)
                continue;
            if (!node.isVisibleToUser())
                continue;
            if (!node.isScrollable())
                continue;

            if (!node.getActionList().contains(
                    forward
                            ? AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD
                            : AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)) {
                continue;
            }

            boolean result = node.performAction(
                    forward
                            ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                            : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);

            Log.e(TAG, "Scroll result: " + result);

            recycleAll(nodes);
            return;
        }

        Log.e(TAG, "No scrollable visible node found");
        recycleAll(nodes);
    }

    // ==========================================================
    // CLICK
    // ==========================================================

    private void clickSemantic(AccessibilityNodeInfo root, String keyword) {

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        keyword = keyword.toLowerCase().trim();

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null)
                continue;
            if (!node.isVisibleToUser())
                continue;

            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();

            boolean match = false;

            if (text != null &&
                    text.toString().toLowerCase().contains(keyword)) {
                match = true;
            }

            if (!match && desc != null &&
                    desc.toString().toLowerCase().contains(keyword)) {
                match = true;
            }

            if (match) {

                AccessibilityNodeInfo parent = node;

                while (parent != null) {

                    if (parent.isClickable()) {

                        parent.performAction(
                                AccessibilityNodeInfo.ACTION_CLICK);

                        Log.e(TAG, "Clicked: " + keyword);

                        recycleAll(nodes);
                        return;
                    }

                    parent = parent.getParent();
                }
            }
        }

        recycleAll(nodes);
    }

    // ==========================================================
    // TYPE
    // ==========================================================

    private void typeText(AccessibilityNodeInfo root, String text) {

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null)
                continue;

            if (node.isEditable()) {

                Bundle args = new Bundle();
                args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        text);
                node.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        args);

                Log.e(TAG, "Typed: " + text);

                recycleAll(nodes);
                return;
            }
        }

        recycleAll(nodes);
    }

    // ==========================================================
    // NODE COLLECTION
    // ==========================================================

    private void collectNodes(AccessibilityNodeInfo node,
            List<AccessibilityNodeInfo> list) {

        if (node == null)
            return;

        list.add(node);

        for (int i = 0; i < node.getChildCount(); i++) {
            collectNodes(node.getChild(i), list);
        }
    }

    private void recycleAll(List<AccessibilityNodeInfo> nodes) {

        for (AccessibilityNodeInfo n : nodes) {
            if (n != null)
                n.recycle();
        }
    }

    private void stopAutomation() {

        if (!automationRunning)
            return;

        automationHandler.removeCallbacks(automationRunnable);
        automationRunning = false;
        automationScrollCount = 0;

        Log.e("AUTOMATION", "Automation stopped");
    }

    public void forceStopAutomation(String appPackage) {

        if (!appPackage.equals(currentPackage))
            return;

        if (automationRunnable != null) {
            automationHandler.removeCallbacks(automationRunnable);
        }

        automationRunning = false;

        Log.e("AUTOMATION", "Force stopped for: " + appPackage);
    }

    private void sendPredictionNotification(String targetPackage) {

        PackageManager pm = getPackageManager();
        String appName = targetPackage;

        try {
            appName = pm.getApplicationLabel(
                    pm.getApplicationInfo(targetPackage, 0)).toString();
        } catch (Exception ignored) {
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelId = "prediction_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Smart Predictions",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // ---------------- OPEN ACTION ----------------
        Intent openIntent = new Intent(this, PredictionOpenReceiver.class);

        openIntent.putExtra("from_app", currentPackage);
        openIntent.putExtra("to_app", targetPackage);

        PendingIntent openPendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(), // unique
                openIntent,
                PendingIntent.FLAG_CANCEL_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);
        // ---------------- DISMISS ACTION ----------------
        Intent dismissIntent = new Intent(this, PredictionDismissReceiver.class);
        dismissIntent.putExtra("transition_key",
                currentPackage + "->" + targetPackage);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                this,
                2,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);
        // ---------------- BUILD NOTIFICATION ----------------
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Smart Routine Suggestion")
                .setContentText("Open " + appName + "?")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(0, "Open", openPendingIntent)
                .addAction(0, "Dismiss", dismissPendingIntent)
                .build();
        manager.cancel(1001);
        manager.notify(1001, notification);
    }

    private boolean isUtilityApp(String pkg) {

        if (pkg == null)
            return true;

        if (pkg.contains("settings"))
            return true;
        if (pkg.contains("dialer"))
            return true;
        if (pkg.contains("camera"))
            return true;
        if (pkg.contains("biometric"))
            return true;
        if (pkg.contains("permission"))
            return true;

        return false;
    }

    private void resetPredictionCooldown() {
        predictionActive = false;
    }
}