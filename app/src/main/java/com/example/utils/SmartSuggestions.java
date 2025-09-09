package com.example.utils;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

import org.json.JSONObject;

public class SmartSuggestions {

    // ✅ Check Storage
    public static void checkStorageAndSuggest(Context context) {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long bytesAvailable = (long) stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        long mbAvailable = bytesAvailable / (1024 * 1024);

        LogManager logManager = new LogManager(context);

        try {
            JSONObject meta = new JSONObject();
            meta.put("freeMB", mbAvailable);

            if (mbAvailable < 500) {
                Toast.makeText(context, "⚠️ Low Storage! Consider deleting files.", Toast.LENGTH_LONG).show();
                logManager.logEvent(new LogEvent("low_storage", "warning", "system", meta));
            } else {
                logManager.logEvent(new LogEvent("storage_ok", "info", "system", meta));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Check Battery
    public static void checkBatteryAndSuggest(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        LogManager logManager = new LogManager(context);

        try {
            JSONObject meta = new JSONObject();
            meta.put("batteryLevel", batteryLevel);

            if (batteryLevel < 20) {
                Toast.makeText(context, "⚠️ Battery Low! Consider charging.", Toast.LENGTH_LONG).show();
                logManager.logEvent(new LogEvent("low_battery", "warning", "system", meta));
            } else {
                logManager.logEvent(new LogEvent("battery_ok", "info", "system", meta));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
