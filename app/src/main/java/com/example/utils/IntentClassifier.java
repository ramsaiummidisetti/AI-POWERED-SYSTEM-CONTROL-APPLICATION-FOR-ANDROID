package com.example.utils;
import org.tensorflow.lite.Interpreter;

public class IntentClassifier {

    private Interpreter interpreter;

    public IntentClassifier(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public int predictIntent(float[][] inputVector) {

        float[][] output = new float[1][5]; // 5 intents

        interpreter.run(inputVector, output);

        return getMaxIndex(output[0]);
    }

    private int getMaxIndex(float[] probabilities) {
        int maxIndex = 0;
        float maxValue = probabilities[0];

        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxValue) {
                maxValue = probabilities[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
