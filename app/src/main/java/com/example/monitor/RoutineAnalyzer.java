package com.example.monitor;

import android.content.Context;

import com.example.utils.UsageStatsHelper;

public class RoutineAnalyzer {

    private Context context;

    public RoutineAnalyzer(Context context) {
        this.context = context;
    }

    public String getUsageSummary() {
        return UsageStatsHelper.getUsageSummary(context);
    }
}