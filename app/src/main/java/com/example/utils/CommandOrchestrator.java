package com.example.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import android.widget.Toast;
import android.content.Intent;
import android.provider.AlarmClock;
import com.example.utils.PreferenceHelper;

import com.example.MainActivity;

public class CommandOrchestrator {

    private final Context context;
    private final TextToSpeech tts;
    private final MainActivity main;
    public CommandOrchestrator(Context context, TextToSpeech tts, MainActivity main) {
        this.context = context;
        this.tts = tts;
        this.main = main;
    }

    public void execute(IntentParser.ParsedIntent intent) {
        switch (intent.target) {
            case "bluetooth":
                handleBluetooth(intent.action);
                break;
            case "battery":
                speak(main.getBatteryInfo());
                break;
            case "network":
                speak("Network status is " + main.getNetworkStatusFallback());
                break;
            case "nfc":
                speak("NFC check complete.");
                break;
              // 🔹 NEW COMMANDS — VC-06 → VC-10
        case "alarm":
            try {
                android.content.Intent alarmIntent = new android.content.Intent(android.provider.AlarmClock.ACTION_SET_ALARM);
                alarmIntent.putExtra(android.provider.AlarmClock.EXTRA_HOUR, 7);
                alarmIntent.putExtra(android.provider.AlarmClock.EXTRA_MINUTES, 0);
                alarmIntent.putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "AI Assistant Alarm");
                main.startActivity(alarmIntent);
                speak("Setting an alarm for 7 AM.");
            } catch (Exception e) {
                speak("Sorry, I couldn't set the alarm.");
            }
            break;

        case "darkmode":
            new PreferenceHelper(main).setThemeMode("dark");
            speak("Dark mode enabled.");
            main.recreate();
            break;

        case "lightmode":
            new PreferenceHelper(main).setThemeMode("light");
            speak("Light mode enabled.");
            main.recreate();
            break;

        case "enablevoice":
            new PreferenceHelper(main).setTTSEnabled(true);
            speak("Voice feedback enabled.");
            break;

        case "mutevoice":
            new PreferenceHelper(main).setTTSEnabled(false);
            Toast.makeText(main, "Voice feedback disabled.", Toast.LENGTH_SHORT).show();
            break;

        case "exit":
            speak("Closing app, goodbye!");
            new android.os.Handler().postDelayed(() -> main.finishAffinity(), 1500);
            break;

        default:
            speak("Sorry, I didn't understand that command.");
    }
           
    }

    private void handleBluetooth(String action) {
            if (action.equals("on")) {
                if (!main.isBluetoothOn()) {
                    boolean success = main.tryEnableBluetoothDirectly();
                    if (success) speak("Bluetooth turned on successfully.");
                    else {
                        speak("I can’t turn it on directly due to system limits. Opening settings.");
                        main.openBluetoothSettings();
                    }
                } else {
                    speak("Bluetooth is already on.");
                }
            } else if (action.equals("off")) {
                if (main.isBluetoothOn()) {
                    boolean success = main.tryDisableBluetoothDirectly();
                    if (success) speak("Bluetooth turned off successfully.");
                    else speak("Unable to turn it off directly on this Android version.");
                } else {
                    speak("Bluetooth is already off.");
                }
            } else {
                speak("Bluetooth is currently " + (main.isBluetoothOn() ? "on" : "off"));
            }
        }

    private void speak(String text) {
        if (tts != null)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
