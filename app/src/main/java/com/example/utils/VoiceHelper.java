package com.example.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class VoiceHelper {

    private static TextToSpeech tts;
    private static boolean ready = false;

    public static void init(Context context) {

        if (tts != null)
            return;

        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(Locale.ENGLISH);

                // ⭐ Force OFFLINE usage
                tts.setSpeechRate(1.0f);
                tts.setPitch(1.1f);

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    Log.e("TTS", "Offline TTS language not supported");
                } else {
                    ready = true;
                    Log.e("TTS", "Offline TTS Ready");
                }
            }
        });
    }

    public static void speak(Context context, String text) {

        if (!ready || tts == null)
            return;

        tts.speak(
                text,
                TextToSpeech.QUEUE_ADD,
                null,
                "OFFLINE_TTS");
    }

    public static void shutdown() {
        if (tts != null) {
            tts.shutdown();
            tts = null;
            ready = false;
        }
    }
}
