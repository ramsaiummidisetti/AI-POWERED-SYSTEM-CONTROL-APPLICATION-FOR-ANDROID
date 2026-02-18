package com.example.voice;

import android.util.Log;

public class WakeWordEngine {

    private WakeWordListener listener;

    public WakeWordEngine(WakeWordListener listener) {
        this.listener = listener;
    }

    // TEMP SIMULATION (offline hotword placeholder)
    public void simulateWakeWord() {
        Log.d("WakeWordEngine", "Hey Guru detected");
        if (listener != null) {
            listener.onWakeWordDetected();
        }
    }

    public void start() {
        Log.d("WakeWordEngine", "WakeWordEngine started");
    }

    public void stop() {
        Log.d("WakeWordEngine", "WakeWordEngine stopped");
    }
}
