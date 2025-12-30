package com.example.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScreenTextReader {

    public static void readText(Bitmap bitmap, TextResultCallback callback) {

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(result -> {
                    callback.onTextFound(result.getText());
                })
                .addOnFailureListener(e -> {
                    Log.e("OCR", "Text recognition failed", e);
                    callback.onTextFound("");
                });
    }

    public interface TextResultCallback {
        void onTextFound(String text);
    }
}
