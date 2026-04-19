package com.example.dashboard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.ai.TransitionTracker;
import com.example.ai.AIMetricsManager;

import java.util.Map;

public class AIHealthDashboard extends AppCompatActivity {

    TextView transitionStats;
    TextView aiStatus;
    TextView predictionStats;
    TextView topPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard_layout);

        transitionStats = findViewById(R.id.transitionStats);
        aiStatus = findViewById(R.id.aiStatus);
        predictionStats = findViewById(R.id.predictionStats);
        topPattern = findViewById(R.id.topPattern);

        loadStats();
    }

    private void loadStats() {

            // 🔹 Transitions
            int transitions = TransitionTracker.getTotalTransitions(this);
            transitionStats.setText("Learned Transitions: " + transitions);

            // 🔹 AI Status
            if (transitions < 15) {
                aiStatus.setText("AI Status: Collecting Data 🟡");
            } else if (transitions < 50) {
                aiStatus.setText("AI Status: Learning Patterns 🔵");
            } else {
                aiStatus.setText("AI Status: Smart Mode 🟢");
            }

            // 🔹 Prediction Metrics
            int total = AIMetricsManager.getTotalPredictions(this);
            int correct = AIMetricsManager.getCorrectPredictions(this);
            int wrong = AIMetricsManager.getWrongPredictions(this);
            int accuracy = AIMetricsManager.getAccuracy(this);

            predictionStats.setText(
                    "Predictions: " + total +
                    "\nCorrect: " + correct +
                    "\nWrong: " + wrong +
                    "\nAccuracy: " + accuracy + "%"
            );

            // 🔹 Strong Patterns Only
            Map<String, Integer> strongPatterns =
                    TransitionTracker.getStrongTransitions(this, 3);

            StringBuilder builder = new StringBuilder();
            int count = 0;

            for (Map.Entry<String, Integer> entry : strongPatterns.entrySet()) {

                if (count >= 3) break;

                builder.append(count + 1)
                        .append(". ")
                        .append(entry.getKey())
                        .append(" (")
                        .append(entry.getValue())
                        .append(" times)\n");

                count++;
            }

            if (count == 0) {
                topPattern.setText("Top Patterns: Not enough data");
            } else {
                topPattern.setText("Top Patterns:\n" + builder.toString());
            }
        }
}