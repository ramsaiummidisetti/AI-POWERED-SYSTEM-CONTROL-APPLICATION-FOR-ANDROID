package com.example;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.utils.AlertManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean isDark = false;
    private AlertManager alertManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        // Button themeToggleButton = findViewById(R.id.themeToggleButton);
        Button voiceButton = findViewById(R.id.btn_voice);

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
                        mainIntent
                );
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Theme toggle
        // themeToggleButton.setOnClickListener(v -> {
        //     isDark = !isDark;
        //     String mode = isDark ? "Dark Mode" : "Light Mode";
        //     Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
        // });

        // ✅ RecyclerView with 2x2 grid
        RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<String> titles = new ArrayList<>();
        List<String> details = new ArrayList<>();

        titles.add("App Usage"); details.add("Loading...");
        titles.add("Battery Info"); details.add("Loading...");
        titles.add("Network"); details.add("Loading...");
        titles.add("Logs"); details.add("Loading...");

        DashboardAdapter adapter = new DashboardAdapter(titles, details, (title, position) ->
                Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show()
        );

        recyclerView.setAdapter(adapter);

        // ✅ Populate cards
        // Inside your details population
        try {
            String usage = UsageStatsHelper.getUsageSummary(this); // ✅ fixed method name
            details.set(0, usage);
        } catch (Exception e) {
            Log.e(TAG, "UsageStats error", e);
            details.set(0, "Usage: error");
        }


        try {
            details.set(1, getBatteryInfo());
        } catch (Exception e) {
            Log.e(TAG, "Battery error", e);
            details.set(1, "Battery: error");
        }

        try {
            String net = NetworkHelper.getNetworkStatus(this);
            if (net == null) net = getNetworkStatusFallback();
            details.set(2, net);
        } catch (Exception e) {
            Log.e(TAG, "Network error", e);
            details.set(2, "Network: error");
        }

        try {
            List<String> logs = logManager.getLogs();
            if (logs == null || logs.isEmpty()) details.set(3, "No logs available");
            else details.set(3, String.join("\n", logs.subList(Math.max(0, logs.size() - 6), logs.size())));
        } catch (Exception e) {
            Log.e(TAG, "LogManager error", e);
            details.set(3, "Logs: error");
        }

        adapter.notifyDataSetChanged();

        // ✅ Schedule background work
        WorkManager.getInstance(this).enqueue(new OneTimeWorkRequest.Builder(LogSyncWorker.class).build());

        // ✅ Schedule reminder
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

        // ✅ Smart suggestions
        SmartSuggestions.checkStorageAndSuggest(this);
        SmartSuggestions.checkBatteryAndSuggest(this);
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
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
        }
    }

    // 📌 Battery Info
    private String getBatteryInfo() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) return "Battery: unavailable";
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        int percent = (int) ((level / (float) scale) * 100);
        String chargeStatus = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) ? "Charging" : "Not charging";

        return percent + "% - " + chargeStatus;
    }

    // 📌 Network fallback
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

    // 📌 Usage stats permission check
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
}
