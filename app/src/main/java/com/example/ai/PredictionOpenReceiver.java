package com.example.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.MainActivity;
import com.example.accessibility.UniversalControlService;

public class PredictionOpenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String packageName = intent.getStringExtra("to_app");

        Log.e("AI_NOTIFY", "OPEN clicked: " + packageName);

        // ❌ Null check
        if (packageName == null) {
            Log.e("AI_NOTIFY", "Package is NULL");
            return;
        }

        // ❌ Invalid package check
        if (!packageName.startsWith("com.")) {
            Log.e("AI_NOTIFY", "Invalid package received: " + packageName);
            return;
        }

        try {
            Intent launchIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(packageName);

            if (launchIntent != null) {

                Log.e("AI_NOTIFY", "Launching app: " + packageName);

                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                // 🔥 PREVENT STACKING
               

                // 🔥 IMPORTANT: Return to assistant after delay (FIX YOUR ISSUE)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    Log.e("AI_NOTIFY", "Returning to assistant");

                     Intent backIntent = new Intent(context, MainActivity.class);

                    // 🔥 IMPORTANT FLAGS (REUSE EXISTING ACTIVITY)
                    backIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    context.startActivity(backIntent);

                }, 2000); // ⏱ 2 seconds delay

            } else {
                Log.e("AI_NOTIFY", "Launch intent NULL for: " + packageName);
            }

        } catch (Exception e) {
            Log.e("AI_NOTIFY", "Launch failed", e);
        }

        // 🔥 STOP PREDICTION LOOP
        UniversalControlService service = UniversalControlService.getInstance();

        if (service != null) {
            service.resetPredictionFlag();
           
        } else {
            Log.e("AI_NOTIFY", "Service instance NULL");
        }
    }
}