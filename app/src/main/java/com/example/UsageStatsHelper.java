package com.example;

import android.content.Context;

import com.example.utils.LogEvent;
import com.example.utils.LogManager;

import org.json.JSONObject;

public class UsageStatsHelper {

    public static void logUsage(Context context, JSONObject meta) {
        // Create a LogEvent
        LogEvent appUsage = new LogEvent(
                "app_usage_logged",
                "info",
                "system",
                meta
        );

        // Create LogManager
        LogManager logManager = new LogManager(context);

        // Log the event (save to file + logcat)
        logManager.logEvent(appUsage); // saves JSON log
        logManager.write(appUsage);    // prints to Logcat
    }
}
