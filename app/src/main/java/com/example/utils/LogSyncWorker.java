package com.example.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

public class LogSyncWorker extends Worker {

    private static final String TAG = "LogSyncWorker";

    public LogSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Background log sync started");

            // Example log event
            JSONObject meta = new JSONObject();
            meta.put("task", "sync_logs");

            LogEvent syncEvent = new LogEvent("log_sync", "info", "system", meta);
            LogManager logManager = new LogManager(getApplicationContext());
            logManager.logEvent(syncEvent);

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error syncing logs", e);
            return Result.failure();
        }
    }
}
