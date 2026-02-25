package com.example.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.accessibility.UniversalControlService;

public class PredictionOpenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String fromApp = intent.getStringExtra("from_app");
        String toApp = intent.getStringExtra("to_app");

        if (fromApp != null && toApp != null) {
            TransitionTracker.reinforceTransition(
                    context,
                    fromApp,
                    toApp);
        }

        try {
            Intent launchIntent =
                    context.getPackageManager()
                           .getLaunchIntentForPackage(toApp);

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
        } catch (Exception ignored) {}
        UniversalControlService service =
        UniversalControlService.getInstance();

        if (service != null) {
            service.resetPredictionFlag();
        }
    }
}