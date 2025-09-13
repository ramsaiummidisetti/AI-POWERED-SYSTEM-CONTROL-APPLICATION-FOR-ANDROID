package com.example;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean isDark = false;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private com.example.utils.AlertManager alertManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // your main layout

        // init AlertManager (your utility)
        alertManager = new com.example.utils.AlertManager(this);

        // request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        NotificationHelper.createChannel(this);

        // Logging manager
        LogManager logManager = new LogManager(this);

        // Demo log event
        try {
            JSONObject metaFile = new JSONObject();
            metaFile.put("fileName", "example.txt");
            metaFile.put("fileSize", 1024);
            LogEvent fileDeleted = new LogEvent("file_deleted", "info", "app", metaFile);
            logManager.logEvent(fileDeleted);
        } catch (Exception e) {
            Log.e(TAG, "Log meta creation failed", e);
        }

        // Views
        EditText nameEditText = findViewById(R.id.et_name);
        Button submitCommandButton = findViewById(R.id.btn_submit);
        Button themeToggleButton = findViewById(R.id.themeToggleButton);
        Button voiceButton = findViewById(R.id.btn_voice);
        Button permissionButton = findViewById(R.id.permissionButton);

        themeToggleButton.setOnClickListener(v -> {
            isDark = !isDark;
            String mode = isDark ? "Dark Mode" : "Light Mode";
            Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean cameraGranted = result.containsKey(Manifest.permission.CAMERA) && result.get(Manifest.permission.CAMERA);
            Boolean storageGranted = result.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE) && result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (cameraGranted && storageGranted) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        });

        permissionButton.setOnClickListener(v -> requestPermissions());

        // RecyclerView: 2 columns grid for cards (2x2)
        RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Titles and initial placeholders for details
        List<String> titles = new ArrayList<>();
        List<String> details = new ArrayList<>();

        titles.add("App Usage");
        details.add("Loading...");

        titles.add("Battery Info");
        details.add("Loading...");

        titles.add("Network");
        details.add("Loading...");

        titles.add("Logs");
        details.add("Loading...");

        // Adapter with click listener
        DashboardAdapter adapter = new DashboardAdapter(titles, details, (title, position) -> {
            // On card click — show expanded info (simple behavior)
            switch (title) {
                case "App Usage":
                    Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
                    break;
                case "Battery Info":
                    Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
                    break;
                case "Network":
                    Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
                    break;
                case "Logs":
                    Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        recyclerView.setAdapter(adapter);

        // Populate details with real data (keeps UI thread safe — these are light)
        // 1) App usage (top 5 in last 24 hours)
        try {
            String usage = UsageStatsHelper.getTopUsageSummary(this); // updated helper (see instructions below)
            details.set(0, usage);
        } catch (Exception e) {
            Log.e(TAG, "UsageStats error", e);
            details.set(0, "Usage: error");
        }

        // 2) Battery (instant sticky intent)
        try {
            String batteryInfo = getBatteryInfo();
            details.set(1, batteryInfo);
        } catch (Exception e) {
            Log.e(TAG, "Battery error", e);
            details.set(1, "Battery: error");
        }

        // 3) Network
        try {
            String net = NetworkHelper.getNetworkStatus(this); // if you have this helper
            if (net == null) net = getNetworkStatusFallback();
            details.set(2, net);
        } catch (Exception e) {
            Log.e(TAG, "Network error", e);
            details.set(2, "Network: error");
        }

        // 4) Logs (last few lines)
        try {
            List<String> logs = logManager.getLogs(); // your LogManager should provide this
            if (logs == null || logs.isEmpty()) details.set(3, "No logs available");
            else details.set(3, String.join("\n", logs.subList(Math.max(0, logs.size() - 6), logs.size())));
        } catch (Exception e) {
            Log.e(TAG, "LogManager error", e);
            details.set(3, "Logs: error");
        }

        // Tell adapter data changed
        adapter.notifyDataSetChanged();

        // Extra: schedule a one-time work (example) and alarm (kept minimal)
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(LogSyncWorker.class).build();
        WorkManager.getInstance(this).enqueue(workRequest);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pendingIntent);
            }
        }

        // Register smart suggestions on start (you already had these)
        SmartSuggestions.checkStorageAndSuggest(this);
        SmartSuggestions.checkBatteryAndSuggest(this);
    }

    private void requestPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        boolean cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!cameraGranted || !storageGranted) permissionLauncher.launch(permissions);
        else Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show();
    }

    private String getBatteryInfo() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) return "Battery: unavailable";
        int level = batteryStatus.getIntExtra("level", -1);
        int scale = batteryStatus.getIntExtra("scale", -1);
        int status = batteryStatus.getIntExtra("status", -1);

        int percent = (int) ((level / (float) scale) * 100);
        String chargeStatus = (status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                status == android.os.BatteryManager.BATTERY_STATUS_FULL) ? "Charging" : "Not charging";

        return percent + "% - " + chargeStatus;
    }

    private String getNetworkStatusFallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "Network: unknown";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (caps == null) return "No network";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "Wi-Fi connected";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Mobile data connected";
            return "Other network";
        } else {
            return "Network status requires API >= M";
        }
    }
}
