package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import com.example.accessibility.UniversalControlService;

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

            case 0: // OPEN YOUTUBE
                VoiceHelper.speak(context, "Opening YouTube");

                Intent ytIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.google.android.youtube");

                if (ytIntent != null) {
                    activity.startActivity(ytIntent);
                } else {
                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com"));
                    activity.startActivity(webIntent);
                }
                return;

            case 1: // WIFI SETTINGS
                VoiceHelper.speak(context, "Opening Wi-Fi settings");
                activity.startActivity(
                        new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                return;

            case 2: // BATTERY STATUS
                String battery = activity.getBatteryInfo();
                VoiceHelper.speak(context, "Battery level is " + battery);
                return;

            case 3: // OPEN CHROME
                VoiceHelper.speak(context, "Opening Chrome");

                Intent chromeIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.android.chrome");

                if (chromeIntent != null) {
                    activity.startActivity(chromeIntent);
                } else {
                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.google.com"));
                    activity.startActivity(webIntent);
                }
                return;

            case 4: // OPEN MAPS
                VoiceHelper.speak(context, "Opening Maps");

                Intent mapsIntent = context.getPackageManager()
                        .getLaunchIntentForPackage("com.google.android.apps.maps");

                if (mapsIntent != null) {
                    activity.startActivity(mapsIntent);
                } else {
                    Intent webIntent = new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://maps.google.com"));
                    activity.startActivity(webIntent);
                }
                return;

            case 5: // OPEN CAMERA
                VoiceHelper.speak(context, "Opening Camera");

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivity(cameraIntent);
                return;

            case 6: // OPEN SETTINGS
                VoiceHelper.speak(context, "Opening Settings");
                activity.startActivity(
                        new Intent(android.provider.Settings.ACTION_SETTINGS));
                return;

            default:
                VoiceHelper.speak(context, "Sorry, I didn't understand that command");

            case 10: // SCROLL DOWN
                VoiceHelper.speak(context, "Scrolling down");
                UniversalControlService service1 = UniversalControlService.getInstance();
                if (service1 != null) {
                    service1.performAction("scroll down");
                }
                return;

            case 11: // SCROLL UP
                VoiceHelper.speak(context, "Scrolling up");
                UniversalControlService service2 = UniversalControlService.getInstance();
                if (service2 != null) {
                    service2.performAction("scroll up");
                }
                return;

            case 12: // BACK
                VoiceHelper.speak(context, "Going back");
                UniversalControlService service3 = UniversalControlService.getInstance();
                if (service3 != null) {
                    service3.performAction("back");
                }
                return;

            case 13: // CLICK
                VoiceHelper.speak(context, "Clicking");
                UniversalControlService service4 = UniversalControlService.getInstance();
                if (service4 != null) {
                    service4.performAction("click");
                }
                return;
        }
    }

}
