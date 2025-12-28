package com.example.utils;

import android.content.Context;
import android.util.Log;

public class AIIntentEngine {

    public AIIntentEngine(Context context) {
        Log.e("AI_INTENT", "TEMP ML STUB INITIALIZED");
    }

    public int getIntent(String userInput) {

        Log.e("AI_INTENT", "getIntent CALLED with input = " + userInput);

        userInput = userInput.toLowerCase();

        if (userInput.contains("youtube")) return 0;   // OPEN_APP
        if (userInput.contains("wifi")) return 1;      // WIFI_SETTINGS
        if (userInput.contains("battery")) return 2;   // BATTERY_STATUS

        return -1;
    }
}
