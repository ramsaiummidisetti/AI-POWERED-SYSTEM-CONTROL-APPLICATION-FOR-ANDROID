package com.example.ai;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.utils.CommandOrchestrator;

public class SensorAutomationManager implements SensorEventListener {

    private static final String TAG = "SENSOR_AUTOMATION";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Context context;
    private CommandOrchestrator orchestrator;

    public SensorAutomationManager(Context context,
                                   CommandOrchestrator orchestrator) {

        this.context = context;
        this.orchestrator = orchestrator;

        sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public void start() {

        if (sensorManager != null && accelerometer != null) {

            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            Log.e(TAG, "Sensor automation started");
        }
    }

    public void stop() {

        if (sensorManager != null) {

            sensorManager.unregisterListener(this);

            Log.e(TAG, "Sensor automation stopped");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Face down detection
        if (z < -9) {

            Log.e(TAG, "Phone placed face down");

            orchestrator.handleIntent("enable_silent_mode", "sensor_trigger");
        }

        // Shake detection
        if (Math.abs(x) > 15 || Math.abs(y) > 15) {

            Log.e(TAG, "Shake detected");

            orchestrator.handleIntent("open_assistant", "sensor_trigger");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}