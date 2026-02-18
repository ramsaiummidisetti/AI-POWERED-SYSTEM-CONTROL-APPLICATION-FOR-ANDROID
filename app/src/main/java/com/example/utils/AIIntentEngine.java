package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.example.BuildConfig;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIIntentEngine {

    private static AIIntentEngine instance;
    private Interpreter tflite;
    private Map<String, Number> wordIndex;
    private List<String> labelList;

    private static final int MAX_LEN = 20;
    private static final float CONFIDENCE_THRESHOLD = 0.60f;

    public static AIIntentEngine getInstance(Context context) {
        if (instance == null) {
            instance = new AIIntentEngine(context.getApplicationContext());
        }
        return instance;
    }

    private AIIntentEngine(Context context) {
        try {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);

            tflite = new Interpreter(loadModelFile(context), options);

            loadTokenizer(context);
            loadLabels(context);

            Log.e("AI_MODEL", "Model loaded successfully");

            Log.e("AI_SHAPE", "Input shape: " +
                    java.util.Arrays.toString(tflite.getInputTensor(0).shape()));

            Log.e("AI_SHAPE", "Output shape: " +
                    java.util.Arrays.toString(tflite.getOutputTensor(0).shape()));

            Log.e("AI_TYPE", "Input type: " +
                    tflite.getInputTensor(0).dataType());

        } catch (Exception e) {
            Log.e("AI_MODEL", "Model failed to load", e);
        }
    }

    // ===============================
    // MAIN INTENT METHOD
    // ===============================

    public String getIntent(Context context, String userInput) {

        if (userInput == null || tflite == null)
            return null;

        userInput = userInput.toLowerCase().trim();

        // 🔥 STEP 1 — Dynamic App Detection
        String dynamicPackage = detectAppFromCommand(context, userInput);
        if (dynamicPackage != null) {
            return "dynamic:" + dynamicPackage;
        }

        // 🔥 STEP 2 — ML Prediction

        float[][] input = new float[1][MAX_LEN];
        input[0] = textToSequence(userInput);

        float[][] output = new float[1][labelList.size()];

        long start = System.currentTimeMillis();
        tflite.run(input, output);
        long end = System.currentTimeMillis();

        Log.d("AI_PERF", "Inference time: " + (end - start) + " ms");

        int bestIndex = -1;
        float bestScore = 0f;

        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > bestScore) {
                bestScore = output[0][i];
                bestIndex = i;
            }
        }

        if (bestIndex == -1)
            return null;

        if (bestScore >= CONFIDENCE_THRESHOLD) {
            Log.e("AI_ML", "Predicted = " +
                    labelList.get(bestIndex) +
                    " Confidence = " + bestScore);

            return labelList.get(bestIndex);
        }

        return null;
    }

    // ===============================
    // TOKENIZER
    // ===============================

    private float[] textToSequence(String text) {

        float[] sequence = new float[MAX_LEN];

        String[] words = text.split(" ");

        for (int i = 0; i < words.length && i < MAX_LEN; i++) {

            if (wordIndex.containsKey(words[i])) {
                Number value = wordIndex.get(words[i]);
                sequence[i] = value.floatValue();   // ✅ FLOAT
            } else {
                sequence[i] = 0f;
            }
        }

        return sequence;
    }

    // ===============================
    // MODEL LOADER
    // ===============================

    private MappedByteBuffer loadModelFile(Context context) throws IOException {

        FileInputStream inputStream =
                new FileInputStream(
                        context.getAssets()
                                .openFd("intent_model.tflite")
                                .getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset =
                context.getAssets()
                        .openFd("intent_model.tflite")
                        .getStartOffset();

        long declaredLength =
                context.getAssets()
                        .openFd("intent_model.tflite")
                        .getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength);
    }

    // ===============================
    // DYNAMIC APP DETECTION
    // ===============================

    public static String detectAppFromCommand(Context context, String text) {

        if (!text.contains("open"))
            return null;

        String cleanText = text.replaceAll("\\s+", "");

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo app : apps) {

            String label = app.loadLabel(pm).toString().toLowerCase();
            String cleanLabel = label.replaceAll("\\s+", "");

            if (cleanText.contains(cleanLabel)) {
                return app.activityInfo.packageName;
            }
        }

        return null;
    }

    // ===============================
    // LOAD TOKENIZER + LABELS
    // ===============================

    private void loadTokenizer(Context context) throws IOException {

        InputStream is = context.getAssets().open("word_index.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Gson gson = new Gson();
        wordIndex = gson.fromJson(reader, HashMap.class);

        Log.e("AI_MODEL", "Word index loaded. Size = " + wordIndex.size());
    }

    private void loadLabels(Context context) throws IOException {

        InputStream is = context.getAssets().open("label_map.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Gson gson = new Gson();
        labelList = gson.fromJson(reader, List.class);

        Log.e("AI_MODEL", "Labels loaded. Count = " + labelList.size());
    }
}
