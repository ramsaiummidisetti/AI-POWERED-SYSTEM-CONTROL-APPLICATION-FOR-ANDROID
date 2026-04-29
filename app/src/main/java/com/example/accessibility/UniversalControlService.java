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
import com.example.ai.PredictionDismissReceiver;
import com.example.ai.PredictionOpenReceiver;

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

    private long lastSuggestionTime = 0;
    private static final long SUGGESTION_COOLDOWN = 10000; // 10 sec

    private android.speech.tts.TextToSpeech tts;
    private boolean isTtsReady = false;

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
        tts = new android.speech.tts.TextToSpeech(this, status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts.setLanguage(java.util.Locale.US);
                isTtsReady = true;
            }
        });
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
        // DataManager dataManager = new DataManager(this);
        String time = getTimeOfDay();
        String suggestedApp = dataManager.getMostUsedApp(time);

        

            if (suggestedApp != null 
            && !suggestedApp.equals(currentPackage)
            && !suggestedApp.contains("youtube")) {

                long currentTime = System.currentTimeMillis();

                if (currentTime - lastSuggestionTime > SUGGESTION_COOLDOWN && isTtsReady) {

                    lastSuggestionTime = currentTime;

                    String appName = getAppName(suggestedApp);

                    Log.e("AI_AUTO", "Suggesting: " + appName);

                    speak("You usually use " + appName + " now");
                }
            }else {
                Log.e("AI_AUTO", "Cooldown active — skipped");
            }
        
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

            Log.e("AI_AUTO", "Trying click attempt: " + attempts);

            boolean success = clickMatchingVideo(root, lastQuery);

            if (!success) {
                Log.e("AI_AUTO", "Matching failed → fallback");
                success = clickAnyVideoContainer(root);
            }

            if (!success) {
                    retryYouTubeClick(attempts - 1);
                }
        } else {
            Log.e("AI_AUTO", "YouTube not ready → retrying");
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
        Log.e("AI_AUTO", "No titles → fallback");
        return clickFirstAvailable(root);
    }

    query = query.toLowerCase();

    for (AccessibilityNodeInfo node : titles) {

        if (node == null || node.getText() == null) continue;

        String text = node.getText().toString().toLowerCase();

        if (text.contains("shorts") || text.contains("ad")) continue;

        Log.e("YT_DEBUG", "Trying: " + text);

        if (text.contains(query) || calculateMatchScore(text, query) >= 3) {

            if (clickNode(node)) {
                Log.e("YT_DEBUG", "Matched & clicked: " + text);
                return true;
            }
        }
        Log.e("YT_DEBUG", "Titles found: " + titles.size());
    }

    Log.e("AI_AUTO", "No good match → fallback");
    return clickFirstAvailable(root);
}
   private boolean clickNode(AccessibilityNodeInfo node) {

    AccessibilityNodeInfo parent = node;

    for (int i = 0; i < 15 && parent != null; i++) {

        if (parent.isClickable()) {

            boolean result = parent.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK);

            Log.e("YT_DEBUG", "Clicked: " + result);

            return result;
        }

        parent = parent.getParent();
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

    if (root.isClickable()) {

        CharSequence desc = root.getContentDescription();

        if (desc != null) {

            String d = desc.toString().toLowerCase();

            if (d.contains("video") || d.contains("play")) {

                boolean clicked = root.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK);

                Log.e("YT_DEBUG", "Fallback clicked: " + d);

                return clicked;
            }
        }
    }

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
                targetPackage.hashCode(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 🔥 DISMISS INTENT
        Intent dismissIntent = new Intent(this, PredictionDismissReceiver.class);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                this,
                targetPackage.hashCode() + 1,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("AI Suggestion")
                .setContentText("You may want to open " + getAppName(targetPackage))
                .setAutoCancel(true)
                .addAction(0, "Open", openPendingIntent)
                .addAction(0, "Dismiss", dismissPendingIntent)   // 🔥 NEW
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

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            Log.e("YT_DEBUG", "Root is NULL");
            return;
        }

        // 🔥 Try finding video thumbnails
        List<AccessibilityNodeInfo> videoNodes =
                root.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/thumbnail");

        if (videoNodes != null && !videoNodes.isEmpty()) {

            for (AccessibilityNodeInfo node : videoNodes) {

               AccessibilityNodeInfo parent = node;
                int depth = 0;

                while (parent != null && !parent.isClickable() && depth < 10) {
                    parent = parent.getParent();
                    depth++;
                }

                if (parent != null && parent.isClickable()) {

                    boolean clicked = parent.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);

                    Log.e("YT_DEBUG", "Clicked via parent: " + clicked);

                    return;
                }
             }
             clickAnyVideoContainer(root);
        }

        // 🔥 Fallback: search by text
        List<AccessibilityNodeInfo> textNodes =
                root.findAccessibilityNodeInfosByText("views");

        if (textNodes != null && !textNodes.isEmpty()) {

            AccessibilityNodeInfo parent = textNodes.get(0).getParent();

            if (parent != null && parent.isClickable()) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.e("YT_DEBUG", "Clicked via text fallback");
                return;
            }
        }

        Log.e("YT_DEBUG", "No clickable video found");
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
private String getTimeOfDay() {

    int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

    if (hour >= 5 && hour < 12) return "morning";
    else if (hour < 17) return "afternoon";
    else if (hour < 21) return "evening";
    else return "night";
}

private String getAppName(String packageName) {
    try {
        android.content.pm.PackageManager pm = getPackageManager();
        android.content.pm.ApplicationInfo appInfo =
                pm.getApplicationInfo(packageName, 0);
        return pm.getApplicationLabel(appInfo).toString();
    } catch (Exception e) {
        return packageName;
    }
}
private void speak(String text) {

    if (tts != null && isTtsReady) {
        tts.speak(text,
                android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                null,
                null);
    }
   
}
     @Override
    public void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.shutdown();
        }
    }
    public void startYouTubeAutomation() {

    if (automationRunning) {
        Log.e("AI_AUTO", "Already running — skip");
        return;
    }

    automationRunning = true;

    Log.e("AI_AUTO", "STARTING automation");

    retryYouTubeClick(4);

    // 🔥 reset after some time
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        automationRunning = false;
        Log.e("AI_AUTO", "Automation reset");
    }, 15000);
}
}


