package com.example.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryReceiver extends BroadcastReceiver {

    private BatteryListener listener;

    public interface BatteryListener {
        void onBatteryChanged(String info);
    }

    public BatteryReceiver(BatteryListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percentage = (int) ((level / (float) scale) * 100);

        boolean charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) > 0;

        String info = "Battery: " + percentage + "%, " +
                (charging ? "Charging" : "Not Charging");

        if (listener != null) {
            listener.onBatteryChanged(info);
        }
    }
}
