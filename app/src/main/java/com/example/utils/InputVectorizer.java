package com.example.utils;
public class InputVectorizer {

    public static float[][] vectorize(String cleanedText) {

        float[][] vector = new float[1][50]; // example size

        // Simple example (real model uses tokenizer)
        for (int i = 0; i < cleanedText.length() && i < 50; i++) {
            vector[0][i] = cleanedText.charAt(i) / 255.0f;
        }

        return vector;
    }
}
