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
import android.net.Uri;
import java.util.List;
import com.example.utils.YouTubeApiHelper;
import com.example.database.DataManager;
import com.example.ai.TransitionTracker;
import com.example.ai.SmartSuggestionManager;
import com.example.ai.PredictionOpenReceiver;
import com.example.ai.AIMetricsManager;
import com.example.ml.HybridPredictionEngine;

public class UniversalControlService extends AccessibilityService {

    private DataManager dataManager;
    private static UniversalControlService instance;
    private static final String TAG = "UNIVERSAL_CTRL";

    private String currentPackage = "";
    private String lastQuery = "";


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

    @Override
    public void onServiceConnected() {

        super.onServiceConnected();

        dataManager = new DataManager(this);

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

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) return;

        long now = System.currentTimeMillis();

        if (now - lastEventTime < EVENT_DEBOUNCE) return;
        lastEventTime = now;

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return;

        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;

        String newPackage = pkg.toString();

        if (isUtilityApp(newPackage)) return;
        if (newPackage.equals(currentPackage)) return;

        String oldPackage = currentPackage;

        if (oldPackage == null || oldPackage.isEmpty()) {
            currentPackage = newPackage;
            return;
        }

        // Record transition
        TransitionTracker.recordTransition(this, oldPackage, newPackage);
        dataManager.printContextLogs();
        currentPackage = newPackage;

        if (dataManager != null) {
            dataManager.insertContext(newPackage, "app_open");
        }

        Log.e(TAG, "ACTIVE PACKAGE: " + currentPackage);

        // Prediction
        String predicted = null;
        try {
            predicted = HybridPredictionEngine.predictNextApp(this, newPackage);
        } catch (Exception e) {
            Log.e("HYBRID_ENGINE", "Prediction crashed", e);
        }

        if (predicted != null &&
                !predicted.equals(currentPackage) &&
                !predictionActive &&
                now - lastPredictionTime > PREDICTION_COOLDOWN) {

            predictionActive = true;
            lastPredictionTime = now;

            AIMetricsManager.incrementPrediction(this);

            AIMetricsManager.saveLastPrediction(
                    this,
                    currentPackage,
                    predicted
            );

            sendPredictionNotification(predicted);
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

        if (currentPackage == null) return;

        if (getPackageName().equals(currentPackage)) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        command = command.toLowerCase().trim();

        if (command.contains("scroll down")) {

            root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

        } else if (command.contains("scroll up")) {

            root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);

        } else if (command.contains("play")) {


            String query = extractQuery(command);
            int time = extractTimeInSeconds(command);

            if (query.isEmpty()) {
                query = "trending video";
            }

            // openYouTubeWithTime(query, time);
            playVideoWithTimestamp(command);
        }
        root.recycle();
    }

    // ==========================================================
    // YOUTUBE AUTOMATION
    // ==========================================================

    private void retryYouTubeClick(int attempts) {

        if (attempts <= 0) {
            Log.e("AI_AUTO", "Retry failed");
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            AccessibilityNodeInfo root = getRootInActiveWindow();

            if (root != null &&
                    root.getPackageName() != null &&
                    root.getPackageName().toString().contains("youtube")) {

                boolean success = clickMatchingVideo(root, lastQuery);

                // 🔥 fallback if matching fails
                if (!success) {
                    Log.e("AI_AUTO", "Matching failed → trying container click");
                    success = clickAnyVideoContainer(root);
                }

                if (!success) {
                    retryYouTubeClick(attempts - 1);
                }

            } else {
                retryYouTubeClick(attempts - 1);
            }

        }, 4000);
    }

   private boolean clickMatchingVideo(AccessibilityNodeInfo root, String query) {

        if (root == null) return false;

        List<AccessibilityNodeInfo> titles =
                root.findAccessibilityNodeInfosByViewId(
                        "com.google.android.youtube:id/title");

        if (titles == null || titles.isEmpty()) {
            Log.e("AI_AUTO", "No titles found → fallback");
            return clickFirstAvailable(root);   // 🔥 fallback
        }

        AccessibilityNodeInfo bestNode = null;
        int bestScore = 0;

        query = query.toLowerCase();

        for (AccessibilityNodeInfo node : titles) {

            if (node == null || node.getText() == null) continue;

            String text = node.getText().toString().toLowerCase();

            if (text.contains("shorts") || text.contains("ad")) continue;

            int score = calculateMatchScore(text, query);

            if (score > bestScore) {
                bestScore = score;
                bestNode = node;
            }
        }

        // 🔥 If no good match → fallback
        if (bestNode == null || bestScore < 3) {
            Log.e("AI_AUTO", "No match → fallback click");
            return clickFirstAvailable(root);
        }

        return clickNode(bestNode);
    }
    private boolean clickNode(AccessibilityNodeInfo node) {

        AccessibilityNodeInfo parent = node;
        int depth = 0;

        while (parent != null && !parent.isClickable() && depth < 10) {
            parent = parent.getParent();
            depth++;
        }

        if (parent != null && parent.isClickable()) {

            boolean clicked = parent.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK);

            Log.e("AI_AUTO", "Clicked: " + node.getText());

            return clicked;
        }

        return false;
    }
    private boolean clickFirstAvailable(AccessibilityNodeInfo root) {

            List<AccessibilityNodeInfo> titles =
                    root.findAccessibilityNodeInfosByViewId(
                            "com.google.android.youtube:id/title");

            if (titles == null) return false;

            for (AccessibilityNodeInfo node : titles) {

                if (node == null || node.getText() == null) continue;

                String text = node.getText().toString().toLowerCase();

                if (text.contains("shorts") || text.contains("ad")) continue;

                return clickNode(node);
            }

            return false;
        }
        private int calculateMatchScore(String title, String query) {

            title = title.toLowerCase();
            query = query.toLowerCase();

            int score = 0;

            String[] words = query.split(" ");

            for (String word : words) {

                if (word.length() < 2) continue;

                if (title.contains(word)) {
                    score += 2; // 🔥 stronger weight
                }
            }

            // 🔥 BONUS: full phrase match
            if (title.contains(query)) {
                score += 5;
            }

            // 🔥 PRIORITY KEYWORDS
            if (title.contains("official")) score += 2;
            if (title.contains("trailer")) score += 2;
            if (title.contains("song")) score += 2;

            return score;
        }
    private boolean clickAnyVideoContainer(AccessibilityNodeInfo root) {

        if (root == null) return false;

        // If node is clickable and looks like video item → click
        if (root.isClickable()) {

            CharSequence desc = root.getContentDescription();

            if (desc != null) {
                String d = desc.toString().toLowerCase();

                if (d.contains("video") || d.contains("play")) {

                    boolean clicked = root.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);

                    Log.e("AI_AUTO", "Clicked via container: " + d);

                    return clicked;
                }
            }
        }

        // Recursively scan children
        for (int i = 0; i < root.getChildCount(); i++) {

            AccessibilityNodeInfo child = root.getChild(i);

            if (clickAnyVideoContainer(child)) {
                return true;
            }
        }

        return false;
    }
    // ==========================================================
    // NOTIFICATION
    // ==========================================================

    private void sendPredictionNotification(String targetPackage) {

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelId = "prediction_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Smart Predictions",
                    NotificationManager.IMPORTANCE_HIGH);

            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(this, PredictionOpenReceiver.class);
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
                .addAction(0, "Open", openPendingIntent)
                .build();

        manager.notify(1001, notification);
    }

    private boolean isUtilityApp(String pkg) {

        if (pkg == null) return true;

        if (pkg.equals(getPackageName())) return true;
        if (pkg.equals("com.android.systemui")) return true;

        if (pkg.contains("launcher")) return true;
        if (pkg.contains("settings")) return true;
        if (pkg.contains("inputmethod")) return true;

        return false;
    }
    public void playYouTubeNow() {
        retryYouTubeClick(3);
    }
        // ==========================================================
    // 🔥 REQUIRED METHODS (FIX BUILD ERRORS)
    // ==========================================================

        // Getter for current active package
        public String getCurrentPackage() {
            return currentPackage;
        }

        // Reset prediction flag (used by receivers)
        public void resetPredictionFlag() {
            predictionActive = false;
            Log.e(TAG, "Prediction flag reset");
        }

        // Force stop automation for an app
        public void forceStopAutomation(String packageName) {

            if (packageName == null) return;

            if (packageName.equals(currentPackage)) {

                // Stop any running automation
                automationRunning = false;

                if (automationHandler != null && automationRunnable != null) {
                    automationHandler.removeCallbacks(automationRunnable);
                }

                // Go to home screen
                performGlobalAction(GLOBAL_ACTION_HOME);

                Log.e(TAG, "Force stopped automation for: " + packageName);
            }
        }
        public void setYouTubeQuery(String query) {
            this.lastQuery = query;
        }
        private int extractTimeInSeconds(String command) {

            int seconds = 0;

            command = command.toLowerCase();

            String[] words = command.split(" ");

            for (int i = 0; i < words.length; i++) {

                try {

                    int value = Integer.parseInt(words[i]);

                    // HOURS
                    if (i + 1 < words.length && words[i + 1].contains("hour")) {
                        seconds += value * 3600;
                    }

                    // MINUTES
                    else if (i + 1 < words.length && words[i + 1].contains("minute")) {
                        seconds += value * 60;
                    }

                    // SECONDS
                    else if (i + 1 < words.length && words[i + 1].contains("second")) {
                        seconds += value;
                    }

                } catch (Exception ignored) {}
            }

            return seconds;
        }
        public void openYouTubeWithTime(String videoId, int seconds) {

            String url = "https://www.youtube.com/watch?v=" + videoId;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(url));

            // 🔥 FORCE TIMESTAMP
            intent.putExtra("start_time", seconds);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
private String extractQuery(String command) {

    command = command.toLowerCase();

    // remove play + time part
    command = command.replace("play", "");

    // remove time words
    command = command.replaceAll("\\d+\\s*(hour|hours|minute|minutes|second|seconds)", "");

    return command.trim();
}
private void playVideoWithTimestamp(String command) {

    new Thread(() -> {

        try {

            String query = extractQuery(command);
            int time = extractTimeInSeconds(command);

            Log.e("AI_AUTO", "Query: " + query);
            Log.e("AI_AUTO", "Time: " + time);

            String videoId = YouTubeApiHelper.searchVideoId(query);

            if (videoId == null) {
                Log.e("AI_AUTO", "No video found");
                return;
            }

            String url = "https://www.youtube.com/watch?v="
                    + videoId + "&t=" + time + "s";

            android.content.Intent intent =
                    new android.content.Intent(android.content.Intent.ACTION_VIEW);

            intent.setData(android.net.Uri.parse(url));
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

            Log.e("AI_AUTO", "Playing video with timestamp");

        } catch (Exception e) {
            Log.e("AI_AUTO", "Error in playback", e);
        }

    }).start();
}

}

