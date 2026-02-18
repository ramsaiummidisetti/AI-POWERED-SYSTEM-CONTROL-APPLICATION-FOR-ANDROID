package com.example.voice;

public class FeatureExtractor {

    public static float[] extract(short[] audio) {
        float[] features = new float[audio.length];
        for (int i = 0; i < audio.length; i++) {
            features[i] = audio[i] / 32768.0f; // normalize
        }
        return features;
    }
}
