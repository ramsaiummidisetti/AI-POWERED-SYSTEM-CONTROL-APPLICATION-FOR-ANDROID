package com.example.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelLoader {

    public static Interpreter loadModel(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor =
                context.getAssets().openFd("intent_model.tflite");

        FileInputStream inputStream =
                new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        MappedByteBuffer modelBuffer =
                fileChannel.map(FileChannel.MapMode.READ_ONLY,
                        startOffset, declaredLength);

        return new Interpreter(modelBuffer);
    }
}
