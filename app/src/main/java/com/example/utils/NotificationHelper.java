package com.example.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "my_app_channel";

    // Method for basic notifications
    public static void sendBasicNotification(Context context, int id, String title, String message, Intent intent) {
        // Check if we have notification permission
        if (!hasNotificationPermission(context)) {
            // You might want to request permission here or log a warning
            return;
        }
        
        createNotificationChannel(context);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        // Add permission check before calling notify()
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
            == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(id, builder.build());
        }
    }

    // Method for action notifications (with buttons)
    public static void sendActionNotification(Context context, int id, String title, String message,
                                            Intent mainIntent, Intent actionIntent) {
        // Check if we have notification permission
        if (!hasNotificationPermission(context)) {
            return;
        }
        
        createNotificationChannel(context);
        
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 1, actionIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Action", actionPendingIntent)
            .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        // Add permission check before calling notify()
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
            == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(id, builder.build());
        }
    }

    // Check if we have notification permission
    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED;
        }
        // For versions below Android 13, no runtime permission is needed
        return true;
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My App Channel";
            String description = "Channel for My App notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    // Create channel method for external use
    public static void createChannel(Context context) {
        createNotificationChannel(context);
    }
}