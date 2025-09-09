package com.example.utils;

import android.content.BroadcastReceiver;
import android.content.Context;

import com.example.MainActivity; // Add this import

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "⏰ Reminder Triggered!", Toast.LENGTH_SHORT).show();

        // Optionally send a notification here
        // Replace the sendNotification call with:
        Intent reminderIntent = new Intent(context, MainActivity.class);
        NotificationHelper.sendBasicNotification(context, 1003, "Reminder", "Time for your daily check!", reminderIntent);
        }
}