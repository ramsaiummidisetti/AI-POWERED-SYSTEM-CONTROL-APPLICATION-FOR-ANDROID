package com.example;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utils.DashboardAdapter;
import com.example.utils.NetworkHelper;
import com.example.utils.UsageStatsHelper;
import com.example.utils.VoiceHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.bluetooth.BluetoothAdapter;
import android.provider.Settings;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import android.os.Environment;
import android.os.StatFs;

import android.app.AppOpsManager;
import android.content.pm.ApplicationInfo;
import androidx.appcompat.app.AlertDialog;

public class DashboardScreenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DashboardAdapter adapter;
    private List<String> titles, details;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard_screen);

        Log.d("FLOW", "Dashboard created");

        VoiceHelper.init(this);
        if (savedInstanceState == null) {
            VoiceHelper.speak(this, "System Dashboard opened");
        }

        initUI();

        if (!hasUsageStatsPermission()) {
            Log.d("FLOW", "Permission NOT granted");
            showUsageAccessDialog();
        } else {
            Log.d("FLOW", "Permission GRANTED");
            refreshDashboard();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasUsageStatsPermission()) {
            Log.d("FLOW", "Refreshing dashboard after resume");
            refreshDashboard();
        }
    }

    // 🔥 UI INIT (ONLY ONCE)
    private void initUI() {

        recyclerView = findViewById(R.id.dashboardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        FloatingActionButton helpBtn = findViewById(R.id.btn_open_help);

        helpBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, HelperActivity.class));
        });

        titles = new ArrayList<>();
        details = new ArrayList<>();

        titles.add("App Usage");
        details.add("Tap to view insights");
        titles.add("Battery Info");
        details.add("Loading...");
        titles.add("Network");
        details.add("Loading...");
        titles.add("Bluetooth");
        details.add("Loading...");
        titles.add("NFC");
        details.add("Loading...");
        titles.add("Storage Info");
        details.add(getStorageDetails());

        adapter = new DashboardAdapter(titles, details, (title, position) -> {

            if (title.equals("App Usage")) {

                Log.d("FLOW", "App Usage Clicked");

                if (!hasUsageStatsPermission()) {

                    Toast.makeText(this, "Please grant usage access", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

                    return;
                }

                startActivity(new Intent(this, UsageDetailActivity.class));
            } else if (title.equals("Bluetooth")) {
                toggleBluetooth();

            } else if (title.equals("NFC")) {
                handleNfc();

            } else {
                Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    // 🔄 DATA REFRESH ONLY
    private void refreshDashboard() {

        // try {
        // details.set(0, UsageStatsHelper.getUsageSummary(this));
        // } catch (Exception e) {
        // details.set(0, "Usage: error");
        // }

        details.set(1, getBatteryInfo());

        String net = NetworkHelper.getNetworkStatus(this);
        if (net == null)
            net = getNetworkStatusFallback();
        details.set(2, net);

        if (bluetoothAdapter != null)
            details.set(3, bluetoothAdapter.isEnabled() ? "On" : "Off");
        else
            details.set(3, "Not Supported");

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
            details.set(4, "Not Supported");
        else
            details.set(4, nfcAdapter.isEnabled() ? "On" : "Off");

        adapter.notifyDataSetChanged();
    }

    private boolean hasUsageStatsPermission() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    appInfo.uid, appInfo.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("Allow Usage Access to enable App Usage feature")
                .setPositiveButton("Grant", (d, w) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleNfc() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
    }

    private void toggleBluetooth() {
        if (bluetoothAdapter == null)
            return;

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(this, "Bluetooth OFF", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothAdapter.enable();
            Toast.makeText(this, "Bluetooth ON", Toast.LENGTH_SHORT).show();
        }
        refreshDashboard();
    }

    private String getBatteryInfo() {
        Intent batteryStatus = registerReceiver(null,
                new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryStatus == null)
            return "Unknown";

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (int) ((level / (float) scale) * 100) + "%";
    }

    private String getNetworkStatusFallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return "Unknown";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());

            if (caps == null)
                return "No network";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return "Wi-Fi";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return "Mobile Data";
        }
        return "Unavailable";
    }

    private String getStorageDetails() {
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());

            long total = stat.getBlockCountLong() * stat.getBlockSizeLong();
            long free = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();

            return "Used: " + (total - free) / 1e9 + "GB / " + total / 1e9 + "GB";
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    @Override
    protected void onDestroy() {
        VoiceHelper.shutdown();
        super.onDestroy();
    }
}