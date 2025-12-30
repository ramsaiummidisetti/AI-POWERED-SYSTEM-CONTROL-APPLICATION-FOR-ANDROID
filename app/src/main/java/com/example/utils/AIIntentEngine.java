package com.example.utils;

import android.content.Context;
import android.util.Log;

public class AIIntentEngine {

    public AIIntentEngine(Context context) {
        Log.e("AI_INTENT", "Week-2 ML STUB INITIALIZED");
    }

    public int getIntent(String userInput) {

        Log.e("AI_INTENT", "getIntent CALLED with input = " + userInput);

        if (userInput == null) return -1;

        userInput = userInput.toLowerCase();

        // --- App Launch Commands ---
        if (userInput.contains("youtube")) return 0;
        if (userInput.contains("chrome")) return 3;
        if (userInput.contains("maps")) return 4;
        if (userInput.contains("camera")) return 5;

        // --- System Settings ---
        if (userInput.contains("wifi")) return 1;
        if (userInput.contains("settings")) return 6;

        // --- Info Commands ---
        if (userInput.contains("battery")) return 2;

        return -1;
    }
}
