package com.example;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.utils.ContextManager;
import com.example.utils.GestureHandler;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.nfc.NfcAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.appcompat.app.AppCompatDelegate;

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

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

import com.example.utils.IntentParser;
import com.example.utils.CommandOrchestrator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean isDark = false;
    private AlertManager alertManager;

    // RecyclerView card data
    private List<String> titles;
    private List<String> details;
    private DashboardAdapter adapter;

    private BluetoothAdapter bluetoothAdapter;

    // private TextToSpeech textToSpeech;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        if (loadThemePreference()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);
     


        contextManager = new ContextManager(this);

        gestureDetector = new GestureDetector(this, new GestureHandler(new GestureHandler.GestureListener() {
            @Override
            public void onSwipeLeft() {
                speak("You swiped left. Showing previous status.");
            }

            @Override
            public void onSwipeRight() {
                speak("You swiped right. Refreshing dashboard.");
                // refreshDashboard();
            }

            @Override
            public void onDoubleTap() {
                String context = contextManager.detectContext();
                speak("Detected context: " + context);
            }
        }));

        // inside onCreate() after setContentView(...)
        Button refreshButton = findViewById(R.id.btn_refresh);
        // refreshButton.setOnClickListener(v -> refreshDashboard());

            // 🎤 Voice button
            Button voiceButton = findViewById(R.id.btn_voice);
            voiceButton.setOnClickListener(v -> {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                try {
                    startActivityForResult(intent, 200);
                } catch (Exception e) {
                    Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
                }
            });
            Button openDashboardButton = findViewById(R.id.btn_open_dashboard);
             openDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DashboardScreenActivity.class);
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
                        "Welcome",
                        "Hello, " + userName + "!",
                        mainIntent,
                        mainIntent);
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ RecyclerView with 2x2 grid
        // RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
        // recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // titles = new ArrayList<>();
        // details = new ArrayList<>();

        // titles.add("App Usage");
        // details.add("Loading...");
        // titles.add("Battery Info");
        // details.add("Loading...");
        // titles.add("Network");
        // details.add("Loading...");
        // titles.add("Bluetooth");
        // details.add(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? "On" : "Off"); // replaced Logs

        // // ✅ Add NFC card
        // titles.add("NFC");
        // NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // if (nfcAdapter == null) {
        //     details.add("Not Supported");
        // } else if (nfcAdapter.isEnabled()) {
        //     details.add("On");
        // } else {
        //     details.add("Off");
        // }

        // // Adapter with click handling
        // adapter = new DashboardAdapter(titles, details, (title, position) -> {

        //     if (title.equals("Bluetooth")) {
        //         toggleBluetooth(); // call a method to toggle
        //     } else if (title.equals("NFC")) {
        //         NfcAdapter nfcAdapterInner = NfcAdapter.getDefaultAdapter(this);
        //         if (nfcAdapterInner != null && !nfcAdapterInner.isEnabled()) {
        //             Toast.makeText(this, "Please enable NFC in settings", Toast.LENGTH_SHORT).show();
        //             startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        //         } else {
        //             Toast.makeText(this, details.get(position), Toast.LENGTH_SHORT).show();
        //         }
        //     } else {
        //         Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
        //     }
        // });

        // recyclerView.setAdapter(adapter);

        // // ✅ Populate cards
        // // Inside your details population
        // try {
        //     String usage = UsageStatsHelper.getUsageSummary(this); // ✅ fixed method name
        //     details.set(0, usage);
        // } catch (Exception e) {
        //     Log.e(TAG, "UsageStats error", e);
        //     details.set(0, "Usage: error");
        // }

        // try {
        //     details.set(1, getBatteryInfo());
        // } catch (Exception e) {
        //     Log.e(TAG, "Battery error", e);
        //     details.set(1, "Battery: error");
        // }

        // try {
        //     String net = NetworkHelper.getNetworkStatus(this);
        //     if (net == null)
        //         net = getNetworkStatusFallback();
        //     details.set(2, net);
        // } catch (Exception e) {
        //     Log.e(TAG, "Network error", e);
        //     details.set(2, "Network: error");
        // }

        // adapter.notifyDataSetChanged();
        // // 🔄 Live Auto-Refresh every 60 seconds
        // autoRefreshRunnable = new Runnable() {
        //     @Override
        //     public void run() {
        //         refreshDashboard(); // your existing method updates cards
        //         autoRefreshHandler.postDelayed(this, 60000); // 60 000 ms = 60 s
        //     }
        // };

        // Start the first auto-refresh
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 60000);

        // ✅ Schedule background work
        WorkManager.getInstance(this).enqueue(new OneTimeWorkRequest.Builder(LogSyncWorker.class).build());

        // ✅ Schedule reminder
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000,
                        pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pendingIntent);
            }
        }

        // ✅ Smart suggestions
        SmartSuggestions.checkStorageAndSuggest(this);
        SmartSuggestions.checkBatteryAndSuggest(this);
    }
    // // ✅ Initialize Text-to-Speech safely
    // private void initTextToSpeech() {
    //     try {
    //         textToSpeech = new TextToSpeech(this, status -> {
    //             if (status == TextToSpeech.SUCCESS) {
    //                 int result = textToSpeech.setLanguage(Locale.ENGLISH);
    //                 textToSpeech.setPitch(1.1f);
    //                 textToSpeech.setSpeechRate(1.0f);

    //                 if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
    //                     Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show();
    //                     installTTSData(); // Auto-install missing data
    //                 } else {
    //                     Log.i("TTS", "Text-to-Speech initialized successfully");
    //                 }
    //             } else {
    //                 Toast.makeText(this, "TTS initialization failed. Installing voice data...", Toast.LENGTH_SHORT).show();
    //                 installTTSData();
    //             }
    //         });
    //     } catch (Exception e) {
    //         Log.e("TTS", "Initialization error: " + e.getMessage());
    //     }
    // }

    // // ✅ Auto-install Google TTS engine if missing
    // private void installTTSData() {
    //     try {
    //         Intent installIntent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    //         startActivity(installIntent);
    //     } catch (Exception e) {
    //         Log.e("TTS", "TTS installation failed: " + e.getMessage());
    //     }
    // }



    // Toggle Bluetooth and update RecyclerView card
    // private void toggleBluetooth() {
    //     if (bluetoothAdapter == null) {
    //         Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
    //         return;
    //     }
    //     // Check runtime permission for Android 12+
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
    //             ContextCompat.checkSelfPermission(this,
    //                     Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

    //         requestPermissions(new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 103);
    //         return;
    //     }

    //     if (bluetoothAdapter.isEnabled()) {
    //         boolean success = bluetoothAdapter.disable();
    //         if (success)
    //             speak("Turning off Bluetooth.");
    //         else
    //             speak("Unable to turn off Bluetooth directly. Please check settings.");
    //     } else {
    //         boolean success = bluetoothAdapter.enable();
    //         if (success)
    //             speak("Turning on Bluetooth.");
    //         else
    //             speak("Unable to turn on Bluetooth directly. Please check settings.");
    //     }

    //     // Delay to allow state change
    //     new android.os.Handler().postDelayed(() -> {
    //         boolean enabled = bluetoothAdapter.isEnabled();
    //         for (int i = 0; i < titles.size(); i++) {
    //             if (titles.get(i).equals("Bluetooth")) {
    //                 details.set(i, enabled ? "On" : "Off");
    //                 adapter.notifyItemChanged(i);
    //                 break;
    //             }
    //         }
    //     }, 1500);
    // }

    // private void openBluetoothSettingsFallback() {
    //     Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
    //     startActivity(intent);
    // }

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

        // Usage Stats
        if (!hasUsageStatsPermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
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
            return "Network: unknown";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (caps == null)
                return "No network";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return "Wi-Fi connected";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return "Mobile data connected";
            return "Other network";
        } else {
            return "Network status requires API >= M";
        }
    }

    // 📌 Usage stats permission check
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // ✅ Refresh all card values dynamically
    // private void refreshDashboard() {
    //     try {
    //         // App Usage
    //         String usage = UsageStatsHelper.getUsageSummary(this);
    //         details.set(0, usage);
    //     } catch (Exception e) {
    //         details.set(0, "Usage: error");
    //     }

    //     // Battery
    //     details.set(1, getBatteryInfo());

    //     // Network
    //     String net = NetworkHelper.getNetworkStatus(this);
    //     if (net == null)
    //         net = getNetworkStatusFallback();
    //     details.set(2, net);

    //     // Bluetooth
    //     if (bluetoothAdapter != null)
    //         details.set(3, bluetoothAdapter.isEnabled() ? "On" : "Off");
    //     else
    //         details.set(3, "Not Supported");

    //     // NFC
    //     NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    //     if (nfcAdapter == null)
    //         details.set(4, "Not Supported");
    //     else if (nfcAdapter.isEnabled())
    //         details.set(4, "On");
    //     else
    //         details.set(4, "Off");

    //     adapter.notifyDataSetChanged();
    //     Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
    // }

    // @Override
    // protected void onResume() {
    //     super.onResume();
    //     refreshDashboard(); // auto-refresh when returning from Settings
    // }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");
        try {
            startActivityForResult(intent, 200);
        } catch (Exception e) {
            Toast.makeText(this, "Speech not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0).toLowerCase(Locale.ROOT);
                IntentParser.ParsedIntent intent = IntentParser.parse(command);
                executeIntent(intent, command);

            }
        }
    }

    private void executeIntent(IntentParser.ParsedIntent intent, String command) {

        switch (intent.target) {
            case "bluetooth":
                speak("Opening Bluetooth settings.");
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;

            case "usage":
                String usage = UsageStatsHelper.getUsageSummary(this);
                speak("Here is your app usage summary.");
                Toast.makeText(this, usage, Toast.LENGTH_LONG).show();
                break;

            case "darkmode":
                isDark = true;
                saveThemePreference(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                speak("Dark mode activated");
                Toast.makeText(this, "Dark mode activated", Toast.LENGTH_SHORT).show();
                break;

            case "lightmode":
                isDark = false;
                saveThemePreference(false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                speak("Light mode activated");
                Toast.makeText(this, "Light mode activated", Toast.LENGTH_SHORT).show();
                break;

            case "time":
                String time = java.text.DateFormat.getTimeInstance().format(new java.util.Date());
                speak("The current time is " + time);
                Toast.makeText(this, "Current time: " + time, Toast.LENGTH_SHORT).show();
                break;

            case "exit":
                speak("Closing the application. Goodbye!");
                finishAffinity();
                break;

            case "alarm":
                try {
                    if (command == null || command.isEmpty()) {
                        speak("I didn’t catch the time. Please say it again like set alarm for 6 AM.");
                        return;
                    }
                    // Example: “set alarm for 6 am”
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{1,2})\\s?(am|pm)")
                            .matcher(command);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        String ampm = matcher.group(2);
                        if (ampm.equalsIgnoreCase("pm") && hour != 12)
                            hour += 12;
                        if (ampm.equalsIgnoreCase("am") && hour == 12)
                            hour = 0;

                        java.util.Calendar calendar = java.util.Calendar.getInstance();
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
                        calendar.set(java.util.Calendar.MINUTE, 0);
                        calendar.set(java.util.Calendar.SECOND, 0);

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        Intent alarmIntent = new Intent(this, ReminderReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alarmIntent,
                                PendingIntent.FLAG_IMMUTABLE);
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                        speak("Alarm set for " + matcher.group(1) + " " + matcher.group(2));
                        Toast.makeText(this, "Alarm set successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        speak("Please say time like 'Set alarm for 6 AM'");
                    }
                } catch (Exception e) {
                    speak("Failed to set alarm.");
                    e.printStackTrace();
                }
                break;

            default:
                speak("Sorry, I didn't understand that command.");
                Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleVoiceCommand(String command) {
        // 1️⃣ Parse the user's spoken text into structured intent
        IntentParser.ParsedIntent parsed = IntentParser.parse(command);

        // 2️⃣ Pass it to the orchestrator for execution
       CommandOrchestrator orchestrator = new CommandOrchestrator(this, null, this);
        orchestrator.execute(parsed);
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

      // Simple speak() replacement without TTS (to prevent app crash)
   // Simple speak() without TTS
    private void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;
        android.util.Log.i("VoiceOutput", text);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


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
        // if (autoRefreshHandler != null && autoRefreshRunnable != null) {
        //     autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        // }
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

}
