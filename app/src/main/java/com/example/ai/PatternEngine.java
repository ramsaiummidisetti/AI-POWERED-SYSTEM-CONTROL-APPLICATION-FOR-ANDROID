package com.example.ai;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import android.content.SharedPreferences;

public class PatternEngine {

    private static final int MIN_TRANSITION_COUNT = 3;

    public static Map<String, Integer> getStrongTransitions(Context context) {

        Map<String, Integer> all =
                TransitionTracker.getAllTransitions(context);

        Map<String, Integer> strong = new HashMap<>();

        for (Map.Entry<String, Integer> entry : all.entrySet()) {

            if (entry.getValue() >= MIN_TRANSITION_COUNT) {
                strong.put(entry.getKey(), entry.getValue());
            }
        }

        return strong;
    }

   public static String predictNextApp(Context context, String currentApp) {

            Map<String, Integer> transitions =
                    TransitionTracker.getAllTransitions(context);

            int totalFromCurrent = 0;

            // 1️⃣ Calculate total outgoing transitions
            for (Map.Entry<String, Integer> entry : transitions.entrySet()) {
                if (entry.getKey().startsWith(currentApp + "->")) {
                    totalFromCurrent += entry.getValue();
                }
            }

            if (totalFromCurrent == 0)
                return null;

            double maxConfidence = 0;
            String bestPrediction = null;

            SharedPreferences prefs =
                    context.getSharedPreferences("prediction_feedback",
                            Context.MODE_PRIVATE);

            // 2️⃣ Evaluate each possible next app
            for (Map.Entry<String, Integer> entry : transitions.entrySet()) {

                String key = entry.getKey();
                int count = entry.getValue();

                if (!key.startsWith(currentApp + "->"))
                    continue;

                String nextApp =
                        key.substring((currentApp + "->").length());

                // 🔥 Suppression Check (AFTER nextApp is defined)
                String transitionKey = currentApp + "->" + nextApp;

                long suppressUntil =
                        prefs.getLong("suppress_" + transitionKey, 0);

                if (System.currentTimeMillis() < suppressUntil)
                    continue;

                double confidence =
                        (double) count / totalFromCurrent;

                if (confidence > maxConfidence) {
                    maxConfidence = confidence;
                    bestPrediction = nextApp;
                }
            }

            // 3️⃣ Suggest only if confidence ≥ 60%
            if (maxConfidence >= 0.6)
                return bestPrediction;

            return null;
}
    
}