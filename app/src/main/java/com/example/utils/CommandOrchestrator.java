package com.example.utils;

import java.util.List;
import android.content.pm.ResolveInfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.example.accessibility.UniversalControlService;

import java.text.DateFormat;
import java.util.Date;

public class CommandOrchestrator {

    private final Context context;
    private final VoiceFeedback voice;

    public CommandOrchestrator(Context context, VoiceFeedback voice) {
        this.context = context;
        this.voice = voice;
    }

    // ==========================================================
    // MAIN ENTRY
    // ==========================================================

    public void handleIntent(String intentName, String originalCommand) {

            Log.e("DEBUG_INTENT", "IntentName = " + intentName + 
        " | Original = " + originalCommand);
        Log.e("ORCHESTRATOR", "Intent = " + intentName);
        
        // 🔥 1️⃣ If ML predicted something → use it directly
        if (intentName != null && !intentName.startsWith("dynamic:")) {
            executeStaticIntent(intentName);
            return;
        }

        // 🔥 2️⃣ If dynamic detected
        if (intentName != null && intentName.startsWith("dynamic:")) {
            String pkg = intentName.replace("dynamic:", "");
            launchDynamicApp(pkg);
            return;
        }

        speak("Sorry, I did not understand");

    }

    private void executeStaticIntent(String intentName) {
    
        switch (intentName) {

            // =========================
            // TIME
            // =========================

            case "get_current_time":
                String time = DateFormat.getTimeInstance()
                        .format(new Date());
                speak("Current time is " + time);
                Toast.makeText(context, time, Toast.LENGTH_SHORT).show();
                break;

            // =========================
            // DATE
            // =========================

            case "get_current_date":
                String date = DateFormat.getDateInstance()
                        .format(new Date());
                speak("Today's date is " + date);
                Toast.makeText(context, date, Toast.LENGTH_SHORT).show();
                break;

            // =========================
            // BATTERY
            // =========================

            case "battery_status":
                String batteryInfo = getBatteryInfo();
                speak(batteryInfo);
                break;

            // =========================
            // SYSTEM
            // =========================

            case "exit_app":
                if (context instanceof android.app.Activity) {
                    speak("Closing application");
                    ((android.app.Activity) context).finish();
                }
                break;

            case "open_camera":
                speak("Opening camera");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(cameraIntent);
                break;

            case "open_settings":
                speak("Opening settings");
                Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                break;

            // =========================
            // APPS
            // =========================

            case "open_youtube":
                openApp("com.google.android.youtube", "YouTube",
                        "https://www.youtube.com");
                break;

            case "open_whatsapp":
        
                openApp("com.whatsapp", "WhatsApp",
                        "https://www.whatsapp.com");
                break;

            case "open_instagram":
                openApp("com.instagram.android", "Instagram",
                        "https://www.instagram.com");
                break;

            case "open_gmail":
                openApp("com.google.android.gm", "Gmail",
                        "https://mail.google.com");
                break;

            case "open_maps":
                openApp("com.google.android.apps.maps", "Google Maps",
                        "https://maps.google.com");
                break;

            case "open_chrome":
                openApp("com.android.chrome", "Chrome",
                        "https://www.google.com");
                break;

            default:
                speak("Command recognized but not implemented yet");
        }

    }

    // ==========================================================
    // BATTERY INFO
    // ==========================================================

    private String getBatteryInfo() {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus == null)
            return "Unable to get battery status";

        int level = batteryStatus.getIntExtra(
                BatteryManager.EXTRA_LEVEL, -1);

        int scale = batteryStatus.getIntExtra(
                BatteryManager.EXTRA_SCALE, -1);

        int percentage = (int) ((level / (float) scale) * 100);

        int status = batteryStatus.getIntExtra(
                BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            return "Battery level is " + percentage + " percent and charging";
        } else {
            return "Battery level is " + percentage + " percent";
        }
    }

    // ==========================================================
    // UNIVERSAL APP LAUNCHER WITH FALLBACK
    // ==========================================================
    private void openApp(String packageName,
                     String spokenName,
                     String fallbackUrl) {

        PackageManager pm =
                context.getPackageManager();

        Intent launchIntent =
                pm.getLaunchIntentForPackage(
                        packageName);

        if (launchIntent != null) {

            speak("Opening " + spokenName);

            launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(launchIntent);

        } else {

            speak(spokenName +
                    " is not installed");

            Intent webIntent =
                    new Intent(Intent.ACTION_VIEW,
                            Uri.parse(fallbackUrl));

            webIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(webIntent);
        }
    }
        // ==========================================================
        // DYNAMIC APP LAUNCH
        // ==========================================================

        private void launchDynamicApp(String packageName) {

            Intent launchIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(packageName);

            if (launchIntent != null) {

                speak("Opening application");

                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);

            } else {

                speak("Application not installed");
            }
        }

    // ==========================================================
    // SPEECH
    // ==========================================================

    private void speak(String text) {
        if (voice != null) {
            voice.speak(text);
        }
    }
}
