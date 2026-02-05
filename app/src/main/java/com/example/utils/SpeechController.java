package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;    
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechController {

    public interface Callback {
        void onTextResult(String text);
        void onError(String error);
    }

    private SpeechRecognizer recognizer;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public SpeechController(Context context, Callback callback) {

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                uiHandler.post(() ->
                        callback.onError("Speech error code: " + error)
                );
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> list =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (list == null || list.isEmpty()) {
                    uiHandler.post(() ->
                            callback.onError("No speech detected")
                    );
                    return;
                }

                String text = list.get(0);
                uiHandler.post(() ->
                        callback.onTextResult(text)
                );
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);

        recognizer.startListening(intent);
    }

    public void destroy() {
        recognizer.destroy();
    }
}
