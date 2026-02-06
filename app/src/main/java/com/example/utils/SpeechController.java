package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechController {

    public interface Callback {
        void onTextResult(String text);     // final result
        void onPartialText(String partial); // live text
        void onError(String error);
    }

    private final SpeechRecognizer speechRecognizer; // ⭐ MISSING BEFORE
    private final Callback callback;                 // ⭐ MISSING BEFORE
    private final Context context;

    public SpeechController(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(listener);
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        try {
            speechRecognizer.cancel(); // ⭐ prevent ERROR 5
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            callback.onError("Speech recognizer failed");
        }
    }

    private final RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d("STT", "Ready for speech");
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            callback.onError("Speech error code: " + error);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> data =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (data != null && !data.isEmpty()) {
                callback.onTextResult(data.get(0));
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> data =
                    partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (data != null && !data.isEmpty()) {
                callback.onPartialText(data.get(0));
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    public void destroy() {
        speechRecognizer.destroy();
    }
}
