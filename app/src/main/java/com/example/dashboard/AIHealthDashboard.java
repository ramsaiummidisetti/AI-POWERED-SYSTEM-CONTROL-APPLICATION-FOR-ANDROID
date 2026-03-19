package com.example.dashboard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.ai.TransitionTracker;

public class AIHealthDashboard extends AppCompatActivity {

    TextView transitionStats;
    TextView aiStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard_layout);

        transitionStats = findViewById(R.id.transitionStats);
        aiStatus = findViewById(R.id.aiStatus);

        loadStats();
    }

    private void loadStats() {

        int transitions = TransitionTracker.getTotalTransitions(this);

        transitionStats.setText("Learned Transitions: " + transitions);

        if (transitions > 5) {
            aiStatus.setText("AI Status: Learning Active");
        } else {
            aiStatus.setText("AI Status: Collecting Data");
        }
    }
}