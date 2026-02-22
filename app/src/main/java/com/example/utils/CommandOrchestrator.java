package com.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.ai.IntentData;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class CommandOrchestrator {

    private final Context context;
    private final VoiceFeedback voice;

    public CommandOrchestrator(Context context, VoiceFeedback voice) {
        this.context = context;
        this.voice = voice;
    }

    // ==========================================================
    // 🔥 NEW AI ENTRY POINT (Phase 2 Ready)
    // ==========================================================

    public void handleIntent(IntentData intentData) {

        if (intentData == null) {
            speak("Sorry, I did not understand");
            return;
        }

        String intentName = intentData.getIntentType();
        Log.e("DEBUG_INTENT", "IntentName = " + intentName);

        if (intentName == null) {
            speak("Sorry, I did not understand");
            return;
        }

        // Dynamic app launch
        if (intentName != null && intentName.startsWith("dynamic:")) {
            String pkg = intentName.replace("dynamic:", "");
            launchDynamicApp(pkg);
            return;
        }

        executeStaticIntent(intentName, intentData);
    }

    // ==========================================================
    // ⚠ OLD METHOD (Backward Compatibility)
    // ==========================================================
    public void handleIntent(String intentName, String originalCommand) {

        if (intentName == null) {
            speak("Sorry, I did not understand");
            return;
        }

        IntentData data = new IntentData(intentName, null, 1.0);
        handleIntent(data);
    }

    // ==========================================================
    // STATIC INTENT EXECUTION
    // ==========================================================

    private void executeStaticIntent(String intentName, IntentData intentData) {

        switch (intentName) {

            // =========================
            // TIME
            // =========================

            case "get_current_time":
                String time = DateFormat.getTimeInstance().format(new Date());
                speak("Current time is " + time);
                Toast.makeText(context, time, Toast.LENGTH_SHORT).show();
                break;

            // =========================
            // DATE
            // =========================

            case "get_current_date":
                String date = DateFormat.getDateInstance().format(new Date());
                speak("Today's date is " + date);
                Toast.makeText(context, date, Toast.LENGTH_SHORT).show();
                break;

            // =========================
            // BATTERY
            // =========================

            case "battery_status":
                speak(getBatteryInfo());
                break;

            // =========================
            // SYSTEM
            // =========================

            case "exit_app":
                if (context instanceof Activity) {
                    speak("Closing application");
                    ((Activity) context).finish();
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

            // =========================
            // PHASE 2 ADDITIONS
            // =========================

            case "list_installed_apps":
                listInstalledApps();
                break;

            case "set_silent":
                setSilentMode();
                break;

            case "emergency":
                activateEmergency();
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

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int percentage = (int) ((level / (float) scale) * 100);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            return "Battery level is " + percentage + " percent and charging";
        } else {
            return "Battery level is " + percentage + " percent";
        }
    }

    // ==========================================================
    // UNIVERSAL APP LAUNCHER
    // ==========================================================

    private void openApp(String packageName,
            String spokenName,
            String fallbackUrl) {

        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {

            speak("Opening " + spokenName);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);

        } else {

            speak(spokenName + " is not installed");

            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        }
    }

    // ==========================================================
    // DYNAMIC APP LAUNCH
    // ==========================================================

    private void launchDynamicApp(String packageName) {

        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            speak("Opening application");
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        } else {
            speak("Application not installed");
        }
    }

    // ==========================================================
    // LIST INSTALLED APPS
    // ==========================================================

    private void listInstalledApps() {

        PackageManager pm = context.getPackageManager();

        List<ResolveInfo> apps = pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        speak("You have " + apps.size() + " installed applications");

        for (int i = 0; i < Math.min(5, apps.size()); i++) {
            speak(apps.get(i).loadLabel(pm).toString());
        }
    }

    // ==========================================================
    // SET SILENT MODE
    // ==========================================================

    private void setSilentMode() {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            speak("Phone set to silent mode");
        }
    }

    // ==========================================================
    // EMERGENCY (Placeholder – Phase 2)
    // ==========================================================
    private void activateEmergency() {

        speak("Emergency mode activated");

        // Vibrate strongly
        android.os.Vibrator vibrator =
                (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                        android.os.VibrationEffect.createOneShot(
                                1000,
                                android.os.VibrationEffect.DEFAULT_AMPLITUDE
                        )
                );
            } else {
                vibrator.vibrate(1000);
            }
        }

        Toast.makeText(context,
                "Emergency mode triggered!",
                Toast.LENGTH_LONG).show();
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