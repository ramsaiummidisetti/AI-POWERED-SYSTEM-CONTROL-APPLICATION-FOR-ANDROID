package com.example.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class VoiceHelper {

    private static TextToSpeech tts;
    private static boolean isReady = false;

    // Initialize TTS
    public static void init(Context context) {
        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.ENGLISH);
                    tts.setPitch(1.1f);
                    tts.setSpeechRate(1.0f);
                    isReady = (result != TextToSpeech.LANG_MISSING_DATA &&
                               result != TextToSpeech.LANG_NOT_SUPPORTED);
                    Log.i("VoiceHelper", "TTS initialized successfully");
                } else {
                    Log.e("VoiceHelper", "TTS initialization failed");
                    isReady = false;
                }
            });
        }
    }

    // Speak text aloud
    public static void speak(Context context, String text) {
        if (tts == null || !isReady) {
            Log.w("VoiceHelper", "TTS not ready. Initializing again...");
            init(context);
            return;
        }
        if (text != null && !text.trim().isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID");
        }
    }

    // Stop and release TTS
    public static void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isReady = false;
        }
    }
}
