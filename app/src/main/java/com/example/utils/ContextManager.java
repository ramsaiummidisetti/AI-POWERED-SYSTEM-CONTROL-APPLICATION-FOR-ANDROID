package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;

public class ContextManager {

    private final Context context;

    public ContextManager(Context context) {
        this.context = context;
    }

    public String detectContext() {
        StringBuilder result = new StringBuilder();

        // Battery context
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging) result.append("Charging ");

        // Location-based context (e.g., driving)
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null && loc.getSpeed() > 2.7) { // ~10 km/h
                result.append("Driving ");
            } else {
                result.append("Stationary ");
            }
        } catch (SecurityException e) {
            result.append("(Location permission needed) ");
        }

        return result.toString().trim();
    }
}
