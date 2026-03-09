package com.example;

import com.example.accessibility.UniversalControlService;
import java.util.Map;
import android.app.usage.UsageStats;
import android.util.Pair;

import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;

import android.net.Uri;
import android.provider.Settings;
import android.os.Build;
import com.example.utils.SpeechController;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.utils.ContextManager;
import com.example.utils.GestureHandler;

import com.example.utils.AIIntentEngine;

import com.example.voice.WakeWordEngine;
import com.example.voice.WakeWordListener;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Pair;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.LinkedList;
import java.util.Queue;

import android.nfc.NfcAdapter;
import android.widget.TextView;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;
import android.view.animation.AnimationUtils;
import com.google.android.material.appbar.MaterialToolbar;

import com.example.utils.LogEvent;
import com.example.utils.LogManager;
import com.example.utils.LogSyncWorker;
import com.example.utils.NotificationHelper;
import com.example.utils.SchedulerHelper;
import com.example.utils.SmartSuggestions;
import com.example.utils.UsageStatsHelper;
import com.example.utils.NetworkHelper;
import com.example.utils.ReminderReceiver;
import com.example.utils.DashboardAdapter;
import com.example.utils.AlertManager;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

import com.example.utils.IntentParser;
import com.example.utils.CommandOrchestrator;
import com.example.utils.VoiceHelper;
import com.example.utils.VoiceFeedback;

import org.json.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

import com.example.utils.UsagePatternAnalyzer;
import com.example.ai.PatternDetector;
import com.example.ai.AutomationSuggester;
import com.example.ai.TaskScriptParser;
import com.example.ai.ScriptEngine;
import com.example.ai.SmartSuggestionManager;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity implements WakeWordListener, VoiceFeedback {

    private BroadcastReceiver predictionReceiver;
    private IntentFilter predictionFilter;
    private WakeWordEngine wakeWordEngine;
    private static final String TAG = "MainActivity";
    private boolean isDark = false;

    private AlertManager alertManager;

    private AIIntentEngine aiIntentEngine;

    // RecyclerView card data
    private List<String> titles;
    private List<String> details;
    private DashboardAdapter adapter;

    private BluetoothAdapter bluetoothAdapter;

    // private TextToSpeech textToSpeech;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private LinearLayout voiceFeedbackContainer;
    private ScrollView voiceScrollView;
    private Queue<String> feedbackQueue = new LinkedList<>();

    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    private SpeechController speechController;

    private TextView tvListening;
    private CommandOrchestrator commandOrchestrator;

    private boolean awaitingConfirmation = false;
    private String lastSuggestedPackage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        commandOrchestrator = new CommandOrchestrator(this, this);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        // 09/02/26
        speechController = new SpeechController(this, new SpeechController.Callback() {

            @Override
            public void onListening() {
                tvListening.setText("🎤 Listening… Speak now");
                tvListening.setVisibility(View.VISIBLE);

            }

            @Override
            public void onPartialText(String text) {
                updateVoiceFeedback("User", text);
            }

            @Override
            public void onFinalText(String text) {
                tvListening.setVisibility(View.GONE);
                updateVoiceFeedback("User", text);
                handleIntent(text.toLowerCase());

            }

            @Override
            public void onError(String message) {
                tvListening.setVisibility(View.GONE);
                updateVoiceFeedback("Assistant", message);

            }
        });

        // 09/02/26 end
        // STEP 1: Enable performance monitoring (Debug only)
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build());

            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build());
        }

        if (loadThemePreference()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);

        predictionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String targetPackage = intent.getStringExtra("target_package");
                if (targetPackage == null)
                    return;
                showPredictionPopup(targetPackage);
            }
        };

        predictionFilter = new IntentFilter("PREDICTION_EVENT");

        // 🔥 Overlay permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textToSpeech = new TextToSpeech(this, status -> {

                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.US);

                    textToSpeech.setSpeechRate(1.0f);
                    textToSpeech.setPitch(1.0f);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                        Log.e("TTS", "Language not supported");
                        ttsReady = false;

                    } else {
                        ttsReady = true;
                        Log.e("TTS", "TTS Initialized Successfully");
                    }

                } else {
                    Log.e("TTS", "TTS Initialization Failed");
                    ttsReady = false;
                }
            });

            tvListening = findViewById(R.id.tvListening);

            wakeWordEngine = new WakeWordEngine(this);
            wakeWordEngine.start();

            try {
                aiIntentEngine = AIIntentEngine.getInstance(this);
                Log.i(TAG, "AIIntentEngine initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize AIIntentEngine", e);
            }

            // ✅ Initialize context manager first
            contextManager = new ContextManager(this);

            // ✅ Initialize gestureDetector before using it
            gestureDetector = new GestureDetector(this, new GestureHandler(new GestureHandler.GestureListener() {
                @Override
                public void onSwipeLeft() {
                    vibrateShort();
                    speak("You swiped left. Showing previous status.");
                }

                @Override
                public void onSwipeRight() {
                    vibrateShort();
                    speak("You swiped right. Refreshing dashboard.");
                }

                @Override
                public void onDoubleTap() {
                    vibrateShort();
                    String context = contextManager.detectContext();
                    speak("Detected context: " + context);
                }
            }));

            VoiceHelper.init(this);
            VoiceHelper.speak(this, "Welcome to Command Titan");

            voiceFeedbackContainer = findViewById(R.id.voiceFeedbackContainer);
            voiceScrollView = findViewById(R.id.voiceScrollView);

            contextManager = new ContextManager(this);

            // 🎤 Voice button
            Button voiceButton = findViewById(R.id.btn_voice);

            voiceButton.setOnClickListener(v -> {

                speechController.startListening();
            });

            FloatingActionButton openDashboardButton = findViewById(R.id.btn_open_dashboard);
            openDashboardButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_pop));
                Intent intent = new Intent(MainActivity.this, DashboardScreenActivity.class);
                startActivity(intent);
            });
            // 🆕 Help Button
            FloatingActionButton openHelpButton = findViewById(R.id.btn_open_help);
            openHelpButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_pop));
                Intent intent = new Intent(MainActivity.this, HelperActivity.class);
                startActivity(intent);
            });
            // Autoameanage Button
            FloatingActionButton manageBtn = findViewById(R.id.btn_manage_automations);

            manageBtn.setOnClickListener(v -> {
                Intent intent = new Intent(
                        MainActivity.this,
                        AutomationManagerActivity.class);
                startActivity(intent);
            });

            // Initialize Bluetooth
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // init AlertManager
            alertManager = new AlertManager(this);

            // ✅ Request all needed permissions on app start
            requestAllPermissions();

            // ✅ Notification channel
            NotificationHelper.createChannel(this);

            // ✅ LogManager demo event
            LogManager logManager = new LogManager(this);
            try {
                JSONObject metaFile = new JSONObject();
                metaFile.put("fileName", "example.txt");
                metaFile.put("fileSize", 1024);
                logManager.logEvent(new LogEvent("file_deleted", "info", "app", metaFile));
            } catch (Exception e) {
                Log.e(TAG, "Log meta creation failed", e);
            }

            // Views
            EditText nameEditText = findViewById(R.id.et_name);
            Button submitCommandButton = findViewById(R.id.btn_submit);

            // ✅ Submit button → open SecondActivity + send notification
            submitCommandButton.setOnClickListener(v -> {
                String userName = nameEditText.getText().toString();
                if (!userName.isEmpty()) {
                    // Move to SecondActivity
                    Intent secondActivityIntent = new Intent(MainActivity.this, SecondActivity.class);
                    secondActivityIntent.putExtra("USER_NAME", userName);
                    startActivity(secondActivityIntent);

                    // Show Notification
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

                    NotificationHelper.sendActionNotification(
                            this, 1001,
                            "",
                            "" + userName + "",
                            mainIntent,
                            mainIntent);
                } else {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                }

            });

            // ✅ Schedule background work
            WorkManager.getInstance(this).enqueue(new OneTimeWorkRequest.Builder(LogSyncWorker.class).build());

            // ✅ Schedule reminder
            // ⭐ SAFE reminder scheduling (works on Android 6 → 15)
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, ReminderReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                // ⭐ NO exact alarm → no crash on Android 12+
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 60000,
                        pendingIntent);
            }

            // ✅ Smart suggestions
            SmartSuggestions.checkStorageAndSuggest(this);
            SmartSuggestions.checkBatteryAndSuggest(this);
            runUsageAnalysisSafely();
        }
    }

    @Override

    protected void onResume() {
        super.onResume();

        if (predictionReceiver != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(predictionReceiver, predictionFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(predictionReceiver, predictionFilter);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(predictionReceiver);
        } catch (Exception ignored) {
        }
    }

    private void runUsageAnalysisSafely() {

        if (!hasUsageStatsPermission()) {
            Log.e("USAGE_DEBUG", "Permission not granted");
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // 1️⃣ Get real foreground usage (UsageEvents based)
            java.util.Map<String, Long> usageMap = UsagePatternAnalyzer.getLastHourUsageTime(this);

            if (usageMap == null || usageMap.isEmpty()) {
                Log.e("AUTONOMY", "No usage data");
                return;
            }

            // 2️⃣ Detect dominant app
            android.util.Pair<String, Long> result = PatternDetector.detectDominantApp(usageMap);

            if (result == null) {
                Log.e("AUTONOMY", "No dominant app detected");
                return;
            }

            String dominantApp = result.first;
            long dominantTime = result.second;

            Log.e("AUTONOMY",
                    "DOMINANT_APP (UsageEvents): " +
                            dominantApp +
                            " Foreground(ms): " +
                            dominantTime);

            // 3️⃣ Send to SmartSuggestionManager
            SmartSuggestionManager.evaluateAndSuggest(
                    this,
                    dominantApp,
                    dominantTime);

        }, 3000); // 3 sec delay

    }

    public boolean tryEnableBluetoothDirectly() {
        if (bluetoothAdapter == null)
            return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return false; // blocked by Android
        return bluetoothAdapter.enable();
    }

    public boolean tryDisableBluetoothDirectly() {
        if (bluetoothAdapter == null)
            return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return false; // blocked by Android
        return bluetoothAdapter.disable();
    }

    // 📌 Ask for all runtime permissions
    private void requestAllPermissions() {
        List<String> needed = new ArrayList<>();

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needed.add(perm);
            }
        }

        if (!needed.isEmpty()) {
            requestPermissions(needed.toArray(new String[0]), 101);
        }

        // Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, 102);
        }
    }

    // 📌 Battery Info
    public String getBatteryInfo() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null)
            return "Battery: unavailable";
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        int percent = (int) ((level / (float) scale) * 100);
        String chargeStatus = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) ? "Charging" : "Not charging";

        return percent + "% - " + chargeStatus;
    }

    // 📌 Network fallback
    public String getNetworkStatusFallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return "Network status unavailable";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (caps == null)
                return "You are currently offline";

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return "You are connected to Wi-Fi";

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return "You are connected to mobile data";

            return "You are connected to another network type";
        } else {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                    return "You are connected to Wi-Fi";
                else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                    return "You are connected to mobile data";
                else
                    return "You are connected to another network";
            } else {
                return "You are currently offline";
            }
        }
    }

    // 📌 Usage stats permission check
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public boolean isBluetoothOn() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void turnOffBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    public void openBluetoothSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }
    // AudioManager audioManager =
    // (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    // audioManager.requestAudioFocus(
    // null,
    // AudioManager.STREAM_MUSIC,
    // AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
    // );

    // Simple speak() replacement without TTS (to prevent app crash)
    // Simple speak() without TTS
    @Override
    public void speak(String text) {

        if (!ttsReady || textToSpeech == null) {
            Log.e("TTS", "TTS not ready");
            return;
        }

        textToSpeech.stop();

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "COMMAND_TITAN_TTS");

        Log.e("TTS", "Speaking: " + text);
    }

    // public void speak(String text) {
    // if (textToSpeech != null) {
    // textToSpeech.speak(text,
    // TextToSpeech.QUEUE_FLUSH,
    // null,
    // null);
    // }
    // }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, permissions[i] + " Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (wakeWordEngine != null) {
            wakeWordEngine.stop();
        }
        if (speechController != null) {
            speechController.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        VoiceHelper.shutdown();
        super.onDestroy();
    }

    private GestureDetector gestureDetector;
    private ContextManager contextManager;
    private Handler autoRefreshHandler = new Handler();
    private Runnable autoRefreshRunnable;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void saveThemePreference(boolean darkMode) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("dark_mode", darkMode).apply();
    }

    private boolean loadThemePreference() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getBoolean("dark_mode", false);
    }

    private void updateVoiceFeedback(String sender, String message) {
        if (voiceFeedbackContainer == null)
            return;

        // Keep only last 5 messages
        if (feedbackQueue.size() >= 5) {
            feedbackQueue.poll();
            voiceFeedbackContainer.removeViewAt(0);
        }

        // Bubble container
        LinearLayout bubbleWrap = new LinearLayout(this);
        bubbleWrap.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        bubbleWrap.setPadding(6, 6, 6, 6);

        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(15f);
        tv.setPadding(16, 12, 16, 12);
        tv.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.75));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 6, 8, 6);

        if (sender.equalsIgnoreCase("User")) {
            bubbleWrap.setGravity(android.view.Gravity.END);
            tv.setBackgroundResource(R.drawable.bg_user_bubble);
            tv.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            bubbleWrap.setGravity(android.view.Gravity.START);
            tv.setBackgroundResource(R.drawable.bg_assistant_bubble);
            tv.setTextColor(getResources().getColor(android.R.color.white));
        }

        bubbleWrap.addView(tv, params);
        feedbackQueue.add(sender + ": " + message);
        voiceFeedbackContainer.addView(bubbleWrap);
        voiceScrollView.post(() -> voiceScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    // Add this helper method below your class methods
    private void vibrateShort() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(50);
            }
        }
    }

    private void handleIntent(String userCommand) {

        if (userCommand == null || userCommand.trim().isEmpty())
            return;

        userCommand = userCommand.toLowerCase().trim();

        Log.e("VOICE_DEBUG", "Voice command received: " + userCommand);

        // =====================================================
        // 🔥 PRIORITY -1 — Suggestion Confirmation Layer
        // =====================================================

        if (awaitingConfirmation) {

            if (userCommand.equals("yes") || userCommand.equals("okay") || userCommand.contains("create")
                    || userCommand.contains("do it")) {

                awaitingConfirmation = false;

                if (lastSuggestedPackage != null) {
                    createPinnedShortcut(lastSuggestedPackage);
                } else {
                    speak("No app selected for shortcut");
                }

                // Later: real shortcut creation logic
                return;
            }

            if (userCommand.equals("no") || userCommand.contains("cancel")) {

                awaitingConfirmation = false;
                speak("Okay, cancelled");
                return;
            }
        }

        // =====================================================
        // 🔥 PRIORITY 0 — CRITICAL RULE-BASED FALLBACK
        // =====================================================

        // Emergency must NEVER depend on ML
        if (userCommand.contains("emergency")) {
            commandOrchestrator.handleIntent("emergency", userCommand);
            return;
        }

        // Installed apps
        if (userCommand.contains("list") && userCommand.contains("app")) {
            commandOrchestrator.handleIntent("list_installed_apps", userCommand);
            return;
        }

        // Battery quick rule
        if (userCommand.contains("battery")) {
            commandOrchestrator.handleIntent("battery_status", userCommand);
            return;
        }

        // Time quick rule
        if (userCommand.contains("time")) {
            commandOrchestrator.handleIntent("get_current_time", userCommand);
            return;
        }

        // =====================================================
        // 🔥 PRIORITY 1 — MULTI STEP SCRIPTING
        // =====================================================

        List<String> actions = TaskScriptParser.parseActions(userCommand);

        if (actions != null && actions.size() > 1) {
            ScriptEngine.execute(commandOrchestrator, actions);
            return;
        }

        // =====================================================
        // 🔥 PRIORITY 2 — DIRECT UI AUTOMATION
        // =====================================================

        if (userCommand.startsWith("click") || userCommand.startsWith("tap") || userCommand.startsWith("type")
                || userCommand.contains("scroll") || userCommand.equals("back") || userCommand.equals("home")
                || userCommand.contains("notification")) {

            UniversalControlService service = UniversalControlService.getInstance();

            if (service != null) {
                service.performAction(userCommand);
            } else {
                speak("Accessibility service not active");
            }

            return;
        }

        // =====================================================
        // 🔥 PRIORITY 3 — ML INTENT ENGINE
        // =====================================================

        if (aiIntentEngine == null) {
            speak("AI engine not ready");
            return;
        }

        String predictedIntent = aiIntentEngine.getIntent(this, userCommand);

        if (predictedIntent == null) {
            speak("Sorry, I did not understand");
            return;
        }

        commandOrchestrator.handleIntent(predictedIntent, userCommand);
    }

    @Override
    public void onWakeWordDetected() {
        runOnUiThread(() -> {
            updateVoiceFeedback("Assistant", "👋 Hey Guru detected");
            speak("Yes?");
            speechController.startListening();

        });
    }

    private void createPinnedShortcut(String packageName) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            speak("Shortcut not supported on this Android version");
            return;
        }

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported()) {

            speak("Pinned shortcuts not supported on this device");
            return;
        }

        try {

            String appName = getPackageManager()
                    .getApplicationLabel(
                            getPackageManager()
                                    .getApplicationInfo(packageName, 0))
                    .toString();

            Intent launchIntent = getPackageManager()
                    .getLaunchIntentForPackage(packageName);

            if (launchIntent == null) {
                speak("App cannot be launched");
                return;
            }

            launchIntent.setAction(Intent.ACTION_VIEW);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(this,
                    "shortcut_" + packageName)
                    .setShortLabel(appName)
                    .setLongLabel("Open " + appName)
                    .setIcon(Icon.createWithResource(
                            this,
                            android.R.drawable.sym_def_app_icon))
                    .setIntent(launchIntent)
                    .build();

            shortcutManager.requestPinShortcut(shortcut, null);

            speak("Shortcut created on home screen");

        } catch (Exception e) {
            speak("Failed to create shortcut");
            Log.e("SHORTCUT", "Error creating shortcut", e);
        }
    }

    public CommandOrchestrator getCommandOrchestrator() {
        return commandOrchestrator;
    }

    private void showPredictionPopup(String targetPackage) {

        PackageManager pm = getPackageManager();
        String appName = targetPackage;

        try {
            appName = pm.getApplicationLabel(
                    pm.getApplicationInfo(targetPackage, 0)).toString();
        } catch (Exception ignored) {
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Smart Routine Suggestion")
                .setMessage("You usually open " + appName +
                        " after this app.\nOpen now?")
                .setPositiveButton("Open", (d, w) -> {

                    Intent launchIntent = pm.getLaunchIntentForPackage(targetPackage);

                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                })
                .setNegativeButton("Not now", null)
                .show();
    }
}
