package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.example.MainActivity;

public class CommandOrchestrator {

    private final Context context;
    private final MainActivity activity;

    // ✅ SINGLE constructor (ONLY ONE)
    public CommandOrchestrator(Context context, MainActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    // ✅ ML-BASED INTENT HANDLER (Phase-2)
    public void handleIntent(int intent) {

        Log.e("ORCHESTRATOR", "handleIntent CALLED with intent = " + intent);

        switch (intent) {

            case 0: // OPEN_APP → YouTube
                VoiceHelper.speak(context, "Opening YouTube");
                    Intent ytIntent = context.getPackageManager()
                .getLaunchIntentForPackage("com.google.android.youtube");

                if (ytIntent != null) {
                    activity.startActivity(ytIntent);
                } else {
                    VoiceHelper.speak(context, "Opening YouTube in browser");

                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com")
                    );
                    activity.startActivity(webIntent);
                }

                break;

            case 1: // WIFI_ON
                VoiceHelper.speak(context, "Opening WiFi settings");
                activity.startActivity(
                        new Intent(Settings.ACTION_WIFI_SETTINGS)
                );
                break;

            case 2: // BATTERY
                String battery = activity.getBatteryInfo();
                VoiceHelper.speak(context, "Battery level is " + battery);
                break;

            default:
                VoiceHelper.speak(context, "Sorry, I didn't understand that command");
        }
    }
}
