package com.example.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LogUtils {
    private static final String FILE_NAME = "app_logs.json";
    private static final Executor IO_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Gson gson = new Gson();

    public static void writeLogAsync(Context ctx, final LogEvent event) {
        IO_EXECUTOR.execute(() -> {
            try {
                File file = new File(ctx.getFilesDir(), FILE_NAME);
                List<LogEvent> logs = new ArrayList<>();

                if (file.exists()) {
                    try (FileReader fr = new FileReader(file);
                         BufferedReader br = new BufferedReader(fr)) {
                        Type listType = new TypeToken<List<LogEvent>>() {}.getType();
                        logs = gson.fromJson(br, listType);
                        if (logs == null) logs = new ArrayList<>();
                    } catch (Exception e) {
                        logs = new ArrayList<>(); // reset if corrupted
                    }
                }

                logs.add(event);

                // keep only last 1000
                int MAX = 1000;
                if (logs.size() > MAX) {
                    logs = logs.subList(logs.size() - MAX, logs.size());
                }

                File tmp = new File(ctx.getFilesDir(), FILE_NAME + ".tmp");
                try (FileWriter fw = new FileWriter(tmp)) {
                    gson.toJson(logs, fw);
                }
                if (!tmp.renameTo(file)) {
                    try (FileWriter fw = new FileWriter(file)) {
                        gson.toJson(logs, fw);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static List<LogEvent> readAllLogs(Context ctx) {
        File file = new File(ctx.getFilesDir(), FILE_NAME);
        if (!file.exists()) return new ArrayList<>();
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            Type listType = new TypeToken<List<LogEvent>>() {}.getType();
            List<LogEvent> logs = gson.fromJson(br, listType);
            return logs != null ? logs : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
