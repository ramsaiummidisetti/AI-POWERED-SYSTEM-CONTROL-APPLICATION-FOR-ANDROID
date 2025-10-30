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
import android.bluetooth.BluetoothAdapter;
import android.provider.Settings;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class DashboardScreenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DashboardAdapter adapter;
    private List<String> titles, details;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_screen);

        recyclerView = findViewById(R.id.dashboardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        titles = new ArrayList<>();
        details = new ArrayList<>();

        // Titles and placeholders
        titles.add("App Usage");
        details.add("Loading...");
        titles.add("Battery Info");
        details.add("Loading...");
        titles.add("Network");
        details.add("Loading...");
        titles.add("Bluetooth");
        details.add(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? "On" : "Off");
        titles.add("NFC");
        details.add("Loading...");

        // Adapter with click handling
        adapter = new DashboardAdapter(titles, details, (title, position) -> {
            if (title.equals("Bluetooth")) {
                toggleBluetooth();
            } else if (title.equals("NFC")) {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
                if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
                    Toast.makeText(this, "Enable NFC in settings", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                } else {
                    Toast.makeText(this, details.get(position), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show();
            }
        });

        recyclerView.setAdapter(adapter);

        refreshDashboard();
    }

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
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(this, "Turning off Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothAdapter.enable();
            Toast.makeText(this, "Turning on Bluetooth", Toast.LENGTH_SHORT).show();
        }
        refreshDashboard();
    }
}
