package com.example.voice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioInputReader {

    private static final int SAMPLE_RATE = 16000;
    private static final int BUFFER_SIZE =
            AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );

    private final AudioRecord audioRecord;
    private boolean running = false;

    public AudioInputReader() {
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE
        );
    }

    public void start() {
        audioRecord.startRecording();
        running = true;
    }

    public void stop() {
        running = false;
        audioRecord.stop();
    }

    public short[] readChunk() {
        short[] buffer = new short[BUFFER_SIZE];
        audioRecord.read(buffer, 0, buffer.length);
        return buffer;
    }

    public boolean isRunning() {
        return running;
    }
}
