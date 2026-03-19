package com.example.ml;

import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelRunner {

    private static final String TAG = "ML_PREDICT";

    private Interpreter interpreter;

    public TFLiteModelRunner(Context context) {

        try {
            interpreter = new Interpreter(loadModelFile(context));
            Log.e(TAG, "TFLite model loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Model loading failed", e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {

        FileInputStream fis =
                new FileInputStream(
                        context.getAssets()
                                .openFd("next_app_model.tflite")
                                .getFileDescriptor());

        FileChannel fileChannel = fis.getChannel();

        long startOffset =
                context.getAssets()
                        .openFd("next_app_model.tflite")
                        .getStartOffset();

        long declaredLength =
                context.getAssets()
                        .openFd("next_app_model.tflite")
                        .getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength);
    }

    public int predict(float[] features) {

        if (interpreter == null) {

            Log.e(TAG, "Interpreter not initialized");
            return -1;
        }

        try {
                        if(features == null || features.length < 4){
                Log.e(TAG,"Invalid feature vector");
                return -1;
            }

            // Model expects 4 input features
            float[][] input = new float[1][4];

            for (int i = 0; i < 4 && i < features.length; i++) {
                input[0][i] = features[i];
            }

            // Output classes (0–4)
            float[][] output = new float[1][5];

            interpreter.run(input, output);

            int predictedClass = 0;
            float maxProb = 0;

            for (int i = 0; i < output[0].length; i++) {

                if (output[0][i] > maxProb) {

                    maxProb = output[0][i];
                    predictedClass = i;
                }
            }

            Log.e(TAG,
                    "Prediction result -> Class: " +
                            predictedClass +
                            " Confidence: " +
                            maxProb);

            return predictedClass;

        } catch (Throwable e) {

            Log.e(TAG, "Prediction failed", e);
            return -1;
        }
    }
}