package com.example;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.*;

public class UsageDetailActivity extends AppCompatActivity {

    private HorizontalBarChart chart;
    private TextView topAppText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FLOW", "UsageDetailActivity START");

        setContentView(R.layout.activity_usage_detail);

        chart = findViewById(R.id.barChart);
        topAppText = findViewById(R.id.topAppText);

        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Grant Usage Access", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            loadUsageData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasUsageStatsPermission()) {
            Log.d("FLOW", "Reload after permission");
            loadUsageData();
        }
    }

    private boolean hasUsageStatsPermission() {
        try {
            android.app.AppOpsManager appOps =
                    (android.app.AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);

            int mode = appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    getPackageName()
            );

            return mode == android.app.AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadUsageData() {

        UsageStatsManager usm =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (7L * 24 * 60 * 60 * 1000);

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        if (stats == null || stats.isEmpty()) {
            Toast.makeText(this, "No usage data", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Long> usageMap = new HashMap<>();
        PackageManager pm = getPackageManager();

        for (UsageStats us : stats) {

            long time = us.getTotalTimeInForeground();
            if (time < 30 * 1000) continue;

            String pkg = us.getPackageName();

            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);

                if (pkg.equals(getPackageName())) continue;

            } catch (Exception e) {
                continue;
            }

            usageMap.put(pkg,
                    usageMap.getOrDefault(pkg, 0L) + time);
        }

        Log.d("USAGE", "UsageMap size: " + usageMap.size());

        processData(usageMap);
    }

    private void processData(Map<String, Long> usageMap) {

        if (usageMap.isEmpty()) {
            Toast.makeText(this, "No valid usage data", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map.Entry<String, Long>> list =
                new ArrayList<>(usageMap.entrySet());

        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        int limit = Math.min(10, list.size());
        List<Map.Entry<String, Long>> topApps = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            topApps.add(list.get(i));
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        float totalMinutes = 0;

        for (int i = 0; i < topApps.size(); i++) {

            Map.Entry<String, Long> entry = topApps.get(i);

            float minutes = entry.getValue() / (1000f * 60f);

            entries.add(new BarEntry(i, minutes));
            labels.add(getAppName(entry.getKey()));

            totalMinutes += minutes;
        }

        if (entries.isEmpty()) {
            Toast.makeText(this, "No chart data", Toast.LENGTH_SHORT).show();
            return;
        }

        String topApp = getAppName(topApps.get(0).getKey());

        topAppText.setText(
                "Top App: " + topApp +
                        " | Total: " + (int) totalMinutes + " mins"
        );

        Log.d("USAGE", "Entries size: " + entries.size());

        renderChart(entries, labels);
    }

    private void renderChart(List<BarEntry> entries, List<String> labels) {

        Log.d("USAGE", "Rendering chart with entries: " + entries.size());

        chart.clear();

        BarDataSet dataSet = new BarDataSet(entries, "Usage");

        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            colors.add(ColorTemplate.MATERIAL_COLORS[
                i % ColorTemplate.MATERIAL_COLORS.length
            ]);
        }

        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "m";
            }
        });

        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.setFitBars(true);
        chart.animateY(800);
        chart.invalidate();

        createLegend(labels, colors.subList(0, labels.size()));
    }

    private String getAppName(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return pkg;
        }
    }

    private void createLegend(List<String> labels, List<Integer> colors) {

        LinearLayout legendContainer = findViewById(R.id.legendContainer);
        legendContainer.removeAllViews();

        for (int i = 0; i < labels.size(); i++) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            View colorBox = new View(this);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(40, 40);
            params.setMargins(0, 0, 20, 0);

            colorBox.setLayoutParams(params);
            colorBox.setBackgroundColor(colors.get(i));

            TextView text = new TextView(this);
            text.setText(labels.get(i));
            text.setTextSize(14f);

            row.addView(colorBox);
            row.addView(text);

            legendContainer.addView(row);
        }
    }
}