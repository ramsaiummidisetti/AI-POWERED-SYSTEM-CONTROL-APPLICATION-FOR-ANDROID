package com.example;

import android.view.View;
import com.example.utils.ReminderReceiver;
import com.example.utils.AlertManager;
import com.example.utils.BatteryReceiver;
import com.example.utils.LogEvent;
import com.example.utils.LogManager;
import com.example.utils.LogSyncWorker;
import com.example.utils.NotificationHelper;
import com.example.utils.SchedulerHelper;
import com.example.utils.SmartSuggestions;
import com.example.utils.NetworkHelper;
import com.example.utils.DashboardAdapter;
import com.example.utils.UsageStatsHelper;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

    // Dashboard data
    private List<String> titles;
    private List<String> details;
    private DashboardAdapter adapter;
    private EditText nameEditText;
    private Button btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize nameEditText
        nameEditText = findViewById(R.id.nameEditText);

        btn_submit = findViewById(R.id.btn_submit);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                String userName = nameEditText.getText().toString();
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
            }
        });
        alertManager = new AlertManager(this);

        // ✅ Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }String userName = nameEditText.getText().toString();
        }
        // ✅ Week 1: Open Second Activity with explicit intent
        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            String userName = nameEditText.getText().toString();
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            intent.putExtra("USER_NAME", userName.isEmpty() ? "Guest" : userName);
            startActivity(intent);
        });

        // ✅ Setup notifications
        NotificationHelper.createChannel(this);

        // ✅ Logging example
        LogManager logManager = new LogManager(this);
        try {
            JSONObject metaFile = new JSONObject();
            metaFile.put("fileName", "example.txt");
            metaFile.put("fileSize", 1024);
            logManager.logEvent(new LogEvent("file_deleted", "info", "app", metaFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // UI references
        TextView welcomeText = findViewById(R.id.welcomeText);
        nameEditText = findViewById(R.id.nameEditText);
        Button themeToggleButton = findViewById(R.id.themeToggleButton);
        Button submitCommandButton = findViewById(R.id.btn_submit);
        Button voiceButton = findViewById(R.id.btn_voice);
        Button permissionButton = findViewById(R.id.permissionButton);

        // ✅ Theme toggle
        themeToggleButton.setOnClickListener(v -> {
            isDark = !isDark;
            String mode = isDark ? "Dark Mode" : "Light Mode";
            Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
        });

        // ✅ Submit button
        submitCommandButton.setOnClickListener(v -> {
            String userName = nameEditText.getText().toString();
            if (!userName.isEmpty()) {
                welcomeText.setText("Hello, " + userName + "!");

                // Log event
                try {
                    JSONObject meta = new JSONObject();
                    meta.put("userName", userName);
                    logManager.logEvent(new LogEvent("user_greeted", "info", "app", meta));
                } catch (Exception ignored) {}

                // 🔔 Notification example
                Intent mainIntent = new Intent(this, MainActivity.class);
                NotificationHelper.sendActionNotification(
                        this, 1001, "Welcome", "Hello, " + userName + "!",
                        mainIntent, mainIntent
                );

                // ⏰ Schedule alarm
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent alarmIntent = new Intent(this, ReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE
                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60000,
                            pendingIntent);
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60000,
                            pendingIntent);
                }

                // 📦 Background log sync
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(LogSyncWorker.class).build();
                WorkManager.getInstance(this).enqueue(workRequest);

                // Daily notifications
                SchedulerHelper.scheduleDailyNotification(this);
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Voice button
        voiceButton.setOnClickListener(v -> {
            Toast.makeText(this, "Voice command feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // ✅ Permission button
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean cameraGranted = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
                    boolean storageGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                    if (cameraGranted && storageGranted) {
                        Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                    }
                });
        permissionButton.setOnClickListener(v -> requestPermissions());

        // ✅ Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        titles = new ArrayList<>();
        details = new ArrayList<>();

        // Add dashboard cards
        titles.add("App Usage");
        details.add(UsageStatsHelper.getUsageSummary(this));

        titles.add("Battery Info");
        details.add("Loading...");

        titles.add("Network");
        details.add(NetworkHelper.getNetworkStatus(this));

        titles.add("Logs");
        details.add("Fetching logs...");

        DashboardAdapter adapter = new DashboardAdapter(titles, details, item -> {
        Toast.makeText(this, "Clicked: " + item, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);


        // ✅ Auto-update battery info
        BatteryReceiver br = new BatteryReceiver(info -> {
            details.set(1, info); // Update Battery Info card
            adapter.notifyItemChanged(1);
        });
        registerReceiver(br, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // ✅ Auto-update logs
        List<String> logs = logManager.getLogs();
        if (!logs.isEmpty()) {
            details.set(3, String.join("\n", logs));
            adapter.notifyItemChanged(3);
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        alertManager.register();
        SmartSuggestions.checkStorageAndSuggest(this);
        SmartSuggestions.checkBatteryAndSuggest(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        alertManager.unregister();
    }
  
     
}
