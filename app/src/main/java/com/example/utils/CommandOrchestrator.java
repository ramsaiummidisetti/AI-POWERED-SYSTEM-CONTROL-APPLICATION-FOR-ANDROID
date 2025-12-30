package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

public class CommandOrchestrator {

    private final Context context;

    // ✅ ONLY Context constructor
    public CommandOrchestrator(Context context) {
        this.context = context;
    }

    public void handleIntent(int intent) {

        Log.e("ORCHESTRATOR", "handleIntent CALLED with intent = " + intent);

        switch (intent) {

            case 0: // OPEN YOUTUBE
                VoiceHelper.speak(context, "Opening YouTube");

                Intent ytIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.google.android.youtube");

                if (ytIntent != null) {
                    ytIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(ytIntent);
                } else {
                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com")
                    );
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(webIntent);
                }
                break;

            case 1: // WIFI
                VoiceHelper.speak(context, "Opening Wi-Fi settings");
                Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(wifiIntent);
                break;

            case 3: // CHROME
                VoiceHelper.speak(context, "Opening Chrome");

                Intent chromeIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.android.chrome");

                if (chromeIntent != null) {
                    chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(chromeIntent);
                } else {
                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com")
                    );
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(webIntent);
                }
                break;

            case 4: // MAPS
                VoiceHelper.speak(context, "Opening Maps");

                Intent mapsIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.google.android.apps.maps");

                if (mapsIntent != null) {
                    mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mapsIntent);
                } else {
                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://maps.google.com")
                    );
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(webIntent);
                }
                break;

            case 5: // CAMERA
                VoiceHelper.speak(context, "Opening Camera");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(cameraIntent);
                break;

            case 6: // SETTINGS
                VoiceHelper.speak(context, "Opening Settings");
                Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                break;

            default:
                VoiceHelper.speak(context, "Sorry, I didn't understand that command");
        }
    }
}
