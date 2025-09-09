package com.example.utils;

import com.example.utils.AlertManager;
import android.content.Context;
import android.os.StatFs;

public class StorageMonitor {
    public static void checkStorage(Context context) {
        StatFs stat = new StatFs(context.getFilesDir().getAbsolutePath());
        long bytesAvailable = (long) stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        long mbAvailable = bytesAvailable / (1024 * 1024);

        if (mbAvailable < 100) { // Less than 100 MB left
            AlertManager alertManager = new AlertManager(context);
            alertManager.sendSmartAlert(context, "Low storage: " + mbAvailable + "MB free", "STORAGE");
        }
    }
}
