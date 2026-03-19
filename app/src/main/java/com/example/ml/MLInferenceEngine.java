package com.example.ml;

import android.content.Context;
import android.util.Log;

public class MLInferenceEngine {

    private static final String TAG = "ML_ENGINE";

    // Dummy ML prediction (will be replaced with TFLite)
    public static String predictNextApp(Context context, String currentApp) {

        float[] features =
                FeatureVectorBuilder.buildFeatureVector(context, currentApp);

        Log.e(TAG,
                "Features -> Hour:" + features[0] +
                " Battery:" + features[1] +
                " Wifi:" + features[2] +
                " AppId:" + features[3]);

        int hour = (int) features[0];

        // Simple rule-based placeholder (for now)

        if (hour >= 20) {
            return "com.google.android.youtube";
        }

        if (hour >= 8 && hour <= 18) {
            return "com.android.chrome";
        }

        return "com.instagram.android";
    }
}