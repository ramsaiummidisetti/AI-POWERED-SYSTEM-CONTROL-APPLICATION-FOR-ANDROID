package com.example.ml;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import java.util.Calendar;

public class ContextFeatureCollector {

    private static final String TAG = "ML_ENGINE";

    // Get current hour (0 - 23)
    public static int getCurrentHour() {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        Log.e(TAG, "Hour feature: " + hour);

        return hour;
    }

    // Get battery percentage safely
    public static int getBatteryLevel(Context context) {

        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

        int battery = -1;

        if (batteryManager != null) {

            battery = batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY);

            if (battery > 0) {

                Log.e(TAG, "Battery feature: " + battery);

                return battery;
            }
        }

        // Fallback method
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);

        if (batteryStatus == null)
            return -1;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level != -1 && scale != -1) {

            battery = (int) ((level / (float) scale) * 100);
        }

        Log.e(TAG, "Battery feature: " + battery);

        return battery;
    }

    // Check WiFi connection safely
    public static int isWifiConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {

            Log.e(TAG, "WiFi feature: 0");

            return 0;
        }

        if (cm.getActiveNetwork() == null) {

            Log.e(TAG, "WiFi feature: 0");

            return 0;
        }

        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());

        if (caps != null &&
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

            Log.e(TAG, "WiFi feature: 1");

            return 1;
        }

        Log.e(TAG, "WiFi feature: 0");

        return 0;
    }

    // Collect all context features together
    public static int[] collectFeatures(Context context, int appId) {

        int hour = getCurrentHour();

        int battery = getBatteryLevel(context);

        int wifi = isWifiConnected(context);

        Log.e(TAG,
                "Feature Vector -> Hour:" + hour +
                        " Battery:" + battery +
                        " Wifi:" + wifi +
                        " AppId:" + appId);

        return new int[] { hour, battery, wifi, appId };
    }
}