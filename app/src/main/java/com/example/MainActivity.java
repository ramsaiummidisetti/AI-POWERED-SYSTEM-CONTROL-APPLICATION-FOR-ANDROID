package com.example;

import com.example.R;
import com.example.utils.ReminderReceiver; // Add this import
import com.example.utils.AlertManager;
import com.example.utils.DashboardAdapter;
import com.example.utils.LogEvent;
import com.example.utils.LogManager;
import com.example.utils.LogSyncWorker;
import com.example.utils.NotificationHelper;
import com.example.utils.SchedulerHelper;
import com.example.utils.SmartSuggestions;
import com.example.utils.UsageStatsHelper;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isDark = false;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private AlertManager alertManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize AlertManager with context
        alertManager = new AlertManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS }, 1001);
            }
        }

        // ✅ Setup notifications and alerts
        NotificationHelper.createChannel(this);

        // Declare LogManager once
        LogManager logManager = new LogManager(this);

        JSONObject metaFile = new JSONObject();
        try {
            metaFile.put("fileName", "example.txt");
            metaFile.put("fileSize", 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogEvent fileDeleted = new LogEvent("file_deleted", "info", "app", metaFile);
        logManager.logEvent(fileDeleted);

        TextView welcomeText = findViewById(R.id.welcomeText);
        EditText nameEditText = findViewById(R.id.et_name);
        Button themeToggleButton = findViewById(R.id.themeToggleButton);
        Button submitCommandButton = findViewById(R.id.btn_submit);
        Button voiceButton = findViewById(R.id.btn_voice);
        Button permissionButton = findViewById(R.id.permissionButton);

        themeToggleButton.setOnClickListener(v -> {
            isDark = !isDark;
            String mode = isDark ? "Dark Mode" : "Light Mode";
            Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
            // Add dark/light theme switch logic here later
        });

        submitCommandButton.setOnClickListener(v -> {
            String userName = nameEditText.getText().toString();

            if (!userName.isEmpty()) {

                // ✅ ADD THIS CODE to start SecondActivity
                Intent secondActivityIntent = new Intent(MainActivity.this, SecondActivity.class);
                secondActivityIntent.putExtra("USER_NAME", userName);
                startActivity(secondActivityIntent);
                // Create the intents first
                Intent mainIntent = new Intent(this, MainActivity.class);
                Intent actionIntent = new Intent(this, MainActivity.class);

                // Then call the method with correct parameters
                NotificationHelper.sendActionNotification(
                        this, // Context
                        1001, // Notification ID
                        "Welcome", // Title
                        "Hello, " + userName + "!", // Message
                        mainIntent, // Main intent (when notification is clicked)
                        actionIntent // Action intent (when action button is clicked)
                );

                welcomeText.setText("Hello, " + userName + "!");

                // Log command event
                try {
                    JSONObject meta = new JSONObject();
                    meta.put("userName", userName);

                    LogEvent commandEvent = new LogEvent("user_greeted", "info", "app", meta);
                    logManager.logEvent(commandEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        JSONObject metaEx = new JSONObject();
                        metaEx.put("exceptionType", e.getClass().getSimpleName());
                        metaEx.put("message", e.getMessage());

                        LogEvent exceptionEvent = new LogEvent("exception_occurred", "error", "app", metaEx);
                        logManager.logEvent(exceptionEvent);
                    } catch (Exception logEx) {
                        logEx.printStackTrace();
                    }
                }

                // Schedule alarm (5 min later)
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent alarmIntent = new Intent(this, ReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent,
                        PendingIntent.FLAG_IMMUTABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // For Android 6.0 (API 23) and above
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60000,
                            pendingIntent);
                } else {
                    // For Android 5.x (API 21-22)
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60000,
                            pendingIntent);
                }

                // Schedule WorkManager job
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(LogSyncWorker.class).build();
                WorkManager.getInstance(this).enqueue(workRequest);

                // 🔔 Step 3.1: Schedule Daily Reminder
                SchedulerHelper.scheduleDailyNotification(this); // Fixed call

                // 📊 Step 3.2: Fetch App Usage
                String usageReport = UsageStatsHelper.getUsageSummary(this);
                if (usageReport.contains("Permission not granted")) {
                    Toast.makeText(this, "Please grant Usage Access permission", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                } else {
                    // Show usage report in RecyclerView
                    RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    List<String> items = new ArrayList<>();
                    items.add("Usage Report: " + usageReport);
                    DashboardAdapter adapter = new DashboardAdapter(items);
                    recyclerView.setAdapter(adapter);
                }
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });

        voiceButton.setOnClickListener(v -> {
            Toast.makeText(this, "Voice command feature coming soon", Toast.LENGTH_SHORT).show();

            // Log preference change event
            try {
                JSONObject metaPref = new JSONObject();
                metaPref.put("key", "voice_enabled");
                metaPref.put("value", true);

                LogEvent prefChanged = new LogEvent("preference_changed", "info", "app", metaPref);
                logManager.logEvent(prefChanged);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    JSONObject metaEx = new JSONObject();
                    metaEx.put("exceptionType", e.getClass().getSimpleName());
                    metaEx.put("message", e.getMessage());

                    LogEvent exceptionEvent = new LogEvent("exception_occurred", "error", "app", metaEx);
                    logManager.logEvent(exceptionEvent);
                } catch (Exception logEx) {
                    logEx.printStackTrace();
                }
            }

            // Schedule daily notification - use the correct method
            SchedulerHelper.scheduleDailyNotification(this);

            // Show app usage stats
            String usageReport = UsageStatsHelper.getUsageSummary(this);
            Toast.makeText(this, usageReport, Toast.LENGTH_LONG).show();
        });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.containsKey(Manifest.permission.CAMERA)
                            ? result.get(Manifest.permission.CAMERA)
                            : false;
                    Boolean storageGranted = result.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE)
                            ? result.get(Manifest.permission.READ_EXTERNAL_STORAGE)
                            : false;
                    if (cameraGranted && storageGranted) {
                        Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                    }
                });

        permissionButton.setOnClickListener(v -> requestPermissions());
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        boolean cameraGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!cameraGranted || !storageGranted) {
            permissionLauncher.launch(permissions);
        } else {
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Register / unregister alert receivers
    @Override
    protected void onStart() {
        super.onStart();
        alertManager.register();
        // 🔎 Smart suggestions
        SmartSuggestions.checkStorageAndSuggest(this);
        SmartSuggestions.checkBatteryAndSuggest(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        alertManager.unregister();
    }
}