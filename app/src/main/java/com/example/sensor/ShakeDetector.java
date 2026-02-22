package com.example.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.example.ai.IntentData;

import com.example.utils.CommandOrchestrator;

public class ShakeDetector implements SensorEventListener {

    private SensorManager sensorManager;
    private CommandOrchestrator orchestrator;

    private static final float SHAKE_THRESHOLD = 15.0f;

    public ShakeDetector(Context context) {

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        orchestrator = new CommandOrchestrator(context, null);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (float) Math.sqrt(x*x + y*y + z*z);

        if (acceleration > SHAKE_THRESHOLD) {
            orchestrator.handleIntent(
                new IntentData("SET_SILENT", null, 1.0)
            );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}