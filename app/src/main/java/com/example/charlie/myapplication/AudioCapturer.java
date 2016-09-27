package com.example.charlie.myapplication;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by Charlie on 2016/9/21.
 */
public class AudioCapturer {
    private static final String TAG = "AudioCapturer";

    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;

    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    private OutputStream writer;
    private Socket sc;
    private String serverIP = "";

    public AudioCapturer(Socket socket){
        sc = socket;
    }

    public interface OnAudioFrameCapturedListener {
        public void onAudioFrameCaptured(byte[] audioData);
    }

    public boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);
    }

    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {

        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
        Log.e(TAG,""+mMinBufferSize);


        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }

        Log.d("capture", "startcapture2");
        Log.d(TAG , "getMinBufferSize = "+mMinBufferSize+" bytes !");

        mAudioRecord = new AudioRecord(audioSource,sampleRateInHz,channelConfig,audioFormat,mMinBufferSize);
        Log.d("capture", "startcapture2");
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }
        Log.d("capture", "startcapture3");
        mAudioRecord.startRecording();
        Log.d("capture", "startcapture4");
        mIsLoopExit = false;
        Log.d("capture", "startcapture5");
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();
        Log.d("Audio", "start cap thread");

        mIsCaptureStarted = true;

        Log.d(TAG, "Start audio capture success !");

        return true;
    }

    public void stopCapture() {

        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;

        Log.d(TAG, "Stop audio capture success !");
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {

            while (!mIsLoopExit) {
                Log.d("Audio", "loop");
                try {
                    writer = sc.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //byte[] buffer = new byte[mMinBufferSize];
                byte[] buffer = new byte[4000];

                int ret = mAudioRecord.read(buffer, 0, 4000);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                    try {
                        writer.write(buffer, 0, ret);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "OK, Captured " + buffer + " bytes !");
                }

                SystemClock.sleep(10);
            }
            Log.d("fuck", "cap close");
        }
    }
}
