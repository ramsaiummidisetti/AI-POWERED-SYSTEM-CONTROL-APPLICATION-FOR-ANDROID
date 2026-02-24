package com.example.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PredictionDismissReceiver extends BroadcastReceiver {

    private static final String PREF = "prediction_feedback";

    @Override
    public void onReceive(Context context, Intent intent) {

        String key = intent.getStringExtra("transition_key");
        if (key == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        int dismissCount = prefs.getInt("dismiss_" + key, 0);
        dismissCount++;

        SharedPreferences.Editor editor = prefs.edit();

        if (dismissCount >= 3) {

            long suppressUntil =
                    System.currentTimeMillis() + (24 * 60 * 60 * 1000);

            editor.putLong("suppress_" + key, suppressUntil);
            editor.putInt("dismiss_" + key, 0); // reset
        } else {
            editor.putInt("dismiss_" + key, dismissCount);
        }

        editor.apply();
    }
}