package com.example.ml;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class FeatureVectorBuilder {

    // Simple app ID mapping for ML input
    private static final Map<String, Integer> appMap = new HashMap<>();

    static {

        // Example mappings
        appMap.put("com.google.android.youtube", 0);
        appMap.put("com.instagram.android", 1);
        appMap.put("com.whatsapp", 2);
        appMap.put("com.android.chrome", 3);
        appMap.put("com.snapchat.android", 4);

    }

    public static float[] buildFeatureVector(Context context, String currentApp) {

        int hour =
                ContextFeatureCollector.getCurrentHour();

        int battery =
                ContextFeatureCollector.getBatteryLevel(context);

        int wifi =
                ContextFeatureCollector.isWifiConnected(context);

        int appId =
                appMap.getOrDefault(currentApp, 0);

                return new float[]{
                hour / 24f,
                battery / 100f,
                wifi,
                appId / 5f
        };
    }
}