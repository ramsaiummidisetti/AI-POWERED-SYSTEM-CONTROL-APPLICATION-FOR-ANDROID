package com.example;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.utils.DashboardAdapter;
import com.example.utils.NetworkHelper;
import com.example.utils.UsageStatsHelper;
import com.example.utils.VoiceHelper;

import android.os.Environment;
import android.os.StatFs;
import java.io.File;

import android.view.View;
import android.view.animation.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.bluetooth.BluetoothAdapter;
import android.provider.Settings;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;

import android.app.AppOpsManager;
import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class DashboardScreenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DashboardAdapter adapter;
    private List<String> titles, details;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VoiceHelper.init(this);
        VoiceHelper.speak(this, "System Dashboard opened");

        // 🟡 Check permission first
        if (!hasUsageStatsPermission()) {
            showUsageAccessDialog();
            return;
        }

        // 🟢 Load the dashboard layout & logic
        setupDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If user granted permission after returning from settings, continue setup
        if (hasUsageStatsPermission() && recyclerView == null) {
            setupDashboard();
        }
    }

    // 🧩 Load and setup UI
    private void setupDashboard() {
        setContentView(R.layout.activity_dashboard_screen);

        // Initialize UI elements AFTER layout is loaded
        recyclerView = findViewById(R.id.dashboardRecyclerView);
    

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Floating buttons
        FloatingActionButton openDashboardButton = findViewById(R.id.btn_open_dashboard);
        FloatingActionButton openHelpButton = findViewById(R.id.btn_open_help);

        openDashboardButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_pop));
            startActivity(new Intent(this, DashboardScreenActivity.class));
        });

        openHelpButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_pop));
            startActivity(new Intent(this, HelperActivity.class));
        });

        // Setup data
        titles = new ArrayList<>();
        details = new ArrayList<>();

        titles.add("App Usage"); details.add("Loading...");
        titles.add("Battery Info"); details.add("Loading...");
        titles.add("Network"); details.add("Loading...");
        titles.add("Bluetooth"); details.add(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? "On" : "Off");
        titles.add("NFC"); details.add("Loading...");
        titles.add("Storage Info"); details.add(getStorageDetails());

        adapter = new DashboardAdapter(titles, details, (title, position) -> {
            if (title.equals("Bluetooth")) toggleBluetooth();
            else if (title.equals("NFC")) handleNfc();
            else Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
        });

        recyclerView.setAdapter(adapter);

        // Refresh and display data
        refreshDashboard();
      

        // Voice feedback
        VoiceHelper.speak(this, "System Control Center loaded. Fetching system status.");
    }

    // ✅ NFC Handler
    private void handleNfc() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Enable NFC in settings", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } else {
            Toast.makeText(this, "NFC is active or unsupported", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Check usage permission
    private boolean hasUsageStatsPermission() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    appInfo.uid, appInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            return false;
        }
    }

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("To show app usage details, please allow Usage Access for this app.")
                .setPositiveButton("Grant Access", (dialog, which) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Usage Access denied", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // ✅ Refresh dashboard data
    private void refreshDashboard() {
        try {
            details.set(0, UsageStatsHelper.getUsageSummary(this));
        } catch (Exception e) {
            details.set(0, "Usage: error");
        }

        details.set(1, getBatteryInfo());

        String net = NetworkHelper.getNetworkStatus(this);
        if (net == null) net = getNetworkStatusFallback();
        details.set(2, net);

        if (bluetoothAdapter != null)
            details.set(3, bluetoothAdapter.isEnabled() ? "On" : "Off");
        else
            details.set(3, "Not Supported");

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
            details.set(4, "Not Supported");
        else if (nfcAdapter.isEnabled())
            details.set(4, "On");
        else
            details.set(4, "Off");

        adapter.notifyDataSetChanged();

        VoiceHelper.speak(this,
                "Battery is " + details.get(1) + ". Network " + details.get(2) +
                        ". Bluetooth " + details.get(3) + ". NFC " + details.get(4));
    }

    private String getBatteryInfo() {
        Intent batteryStatus = registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) return "Battery unavailable";
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percent = (int) ((level / (float) scale) * 100);
        return percent + "%";
    }

    private String getNetworkStatusFallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "Unknown";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (caps == null) return "No network";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "Wi-Fi connected";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Mobile data connected";
        }
        return "Network unavailable";
    }

    private void toggleBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            VoiceHelper.speak(this, "Bluetooth not supported");
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(this, "Turning on Bluetooth", Toast.LENGTH_SHORT).show();
            VoiceHelper.speak(this, "Turning on Bluetooth");
        } else {
            bluetoothAdapter.enable();
            Toast.makeText(this, "Turning off Bluetooth", Toast.LENGTH_SHORT).show();
            VoiceHelper.speak(this, "Turning off Bluetooth");
        }
        refreshDashboard();
    }

    private String getStorageDetails() {
    try {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long total = (totalBlocks * blockSize) / (1024 * 1024 * 1024);
        long available = (availableBlocks * blockSize) / (1024 * 1024 * 1024);
        long used = total - available;

        return "Used: " + used + "GB / " + total + "GB\nFree: " + available + "GB";
    } catch (Exception e) {
        return "Storage info unavailable";
    }
}

    @Override
    protected void onDestroy() {
        VoiceHelper.shutdown();
        super.onDestroy();
    }
}
