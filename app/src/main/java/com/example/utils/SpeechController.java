package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechController {

    public interface Callback {
        void onListening();

        void onPartialText(String text);

        void onTextResult(String text);

        void onError(String error);
    }

    private final SpeechRecognizer speechRecognizer;
    private final Intent intent;
    private final Callback callback;

    public SpeechController(Context context, Callback callback) {
        this.callback = callback;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                callback.onListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    callback.onPartialText(data.get(0));
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    callback.onTextResult(data.get(0));
                }
            }

            @Override
            public void onError(int error) {
                callback.onError("Speech error: " + error);
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    public void startListening() {
        speechRecognizer.cancel();
        speechRecognizer.startListening(intent);
    }

    public void destroy() {
        speechRecognizer.destroy();
    }
}
