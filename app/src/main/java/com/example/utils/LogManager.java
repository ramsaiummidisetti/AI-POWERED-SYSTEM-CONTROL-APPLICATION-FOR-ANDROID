package com.example.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class LogManager {

    private static final String FILE_NAME = "app_logs.json";
    private static final int MAX_LOGS = 100; // Keep last 100 logs
    private Context context;

    public LogManager(Context context) {
        this.context = context;
    }

    // Log a single LogEvent object
    public void logEvent(LogEvent logEvent) {
        try {
            JSONArray logs = readLogs();

            JSONObject logEntry = new JSONObject();
            logEntry.put("event", logEvent.event);
            logEntry.put("timestamp", logEvent.timestamp);
            logEntry.put("severity", logEvent.severity);
            logEntry.put("source", logEvent.source);

            if (logEvent.meta != null) {
                // Convert meta to JSONObject if possible
                if (logEvent.meta instanceof JSONObject) {
                    logEntry.put("meta", logEvent.meta);
                }

            }

            logs.put(logEntry);

            // Prune old logs if exceeding MAX_LOGS
            while (logs.length() > MAX_LOGS) {
                logs.remove(0);
            }

            writeLogs(logs);
        } catch (JSONException e) {
            Log.e("LogManager", "JSON error: " + e.getMessage());
        }
    }

    // Read existing logs from app_logs.json
    private JSONArray readLogs() {
        JSONArray logs = new JSONArray();
        try (FileInputStream fis = context.openFileInput(FILE_NAME)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String content = new String(buffer);
            if (!content.isEmpty()) {
                logs = new JSONArray(content);
            }
        } catch (IOException | JSONException e) {
            // File may not exist yet or empty
        }
        return logs;
    }

    // Write JSON array back to file
    private void writeLogs(JSONArray logs) {

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            try {
                fos.write(logs.toString(4).getBytes()); // pretty print
            } catch (org.json.JSONException e) {
                Log.e("LogManager", "JSON error: " + e.getMessage());
            }
        } catch (IOException e) {
            Log.e("LogManager", "Write error: " + e.getMessage());
        }

    }
      public void write(LogEvent event) {
        Log.d("LogManager", event.toString());
    }
    public List<String> getLogs() {
    List<String> logs = new ArrayList<>();
    try (FileInputStream fis = context.openFileInput(FILE_NAME)) {
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        String json = new String(buffer);

        JSONArray arr = new JSONArray(json);
        for (int i = arr.length() - 1; i >= 0 && logs.size() < 5; i--) {
            JSONObject obj = arr.getJSONObject(i);
            logs.add(obj.getString("eventType") + " - " + obj.getString("level"));
        }
    } catch (Exception e) {
        logs.add("No logs available");
    }
    return logs;
}
}
