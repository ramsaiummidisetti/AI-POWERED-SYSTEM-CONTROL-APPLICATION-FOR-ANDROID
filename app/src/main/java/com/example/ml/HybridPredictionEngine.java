package com.example.ml;

import android.content.Context;
import android.util.Log;

import com.example.ai.PatternEngine;

public class HybridPredictionEngine {

    private static final String TAG = "HYBRID_ENGINE";

    private static TFLiteModelRunner modelRunner;

    // Initialize ML model once
    public static void initialize(Context context) {

        if (modelRunner == null) {
            modelRunner = new TFLiteModelRunner(context);
        }
    }

    public static String predictNextApp(Context context, String currentApp) {

        if (currentApp == null || currentApp.isEmpty())
            return null;

        if (modelRunner == null)
            initialize(context);

        // Pattern prediction
        String patternPrediction = PatternEngine.predictNextApp(context, currentApp);

        // ML prediction
        float[] features = FeatureVectorBuilder.buildFeatureVector(context, currentApp);

        int mlPredictionId = -1;

        try {
            mlPredictionId = modelRunner.predict(features);
        } catch (Throwable e) {
            Log.e("ML_ENGINE", "ML prediction failed", e);
        }

        String mlPrediction = null;

        if (mlPredictionId >= 0)
            mlPrediction = mapAppIdToPackage(mlPredictionId);

        Log.e(TAG,
        "Hybrid Debug -> " +
        "Current: " + currentApp +
        " | Pattern: " + patternPrediction +
        " | ML: " + mlPrediction);

        // Hybrid decision

         if(patternPrediction != null && mlPrediction != null){

            if(patternPrediction.equals(mlPrediction)){
                Log.e(TAG,"Hybrid decision: BOTH agree");
                return patternPrediction;
            }

            Log.e(TAG,"Hybrid decision: Pattern preferred");
            return patternPrediction;
        }

        if(patternPrediction != null){
            Log.e(TAG,"Hybrid decision: Pattern only");
            return patternPrediction;
        }

        if(mlPrediction != null){
            Log.e(TAG,"Hybrid decision: ML only");
            return mlPrediction;
        }

            return null;
    }

    // Mapping ML class → app package
    private static String mapAppIdToPackage(int id) {

        switch (id) {

            case 0:
                return "com.google.android.youtube";

            case 1:
                return "com.instagram.android";

            case 2:
                return "com.whatsapp";

            case 3:
                return "com.android.chrome";

            case 4:
                return "com.snapchat.android";

            default:
                return null;
        }
    }
}