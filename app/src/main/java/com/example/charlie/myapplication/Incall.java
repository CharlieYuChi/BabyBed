package com.example.charlie.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Charlie on 2016/9/22.
 */
public class Incall {
    DataInputStream dataIn;
    AudioTrack audioTrack;
    int num;
    byte [] buf;
    AudioFormat af;

    private Thread mIncallThread;
    private volatile boolean mIsLoopExit = false;

    public Incall(Socket socket) {
        try {
            dataIn = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        buf = new byte [4000];
    }

    public void start(){
        Log.d("fuck", "startAudio");
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,44100,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT,
            4000, AudioTrack.MODE_STREAM);

        Log.d("fuck", "startPlay");
        audioTrack.play();

        mIncallThread = new Thread(new IncallRunnable());
        mIncallThread.start();

    }

    public void stop(){
        mIsLoopExit = true;
        audioTrack.stop();
        Log.d("incall", "Stop Incall success !");
    }

    private class IncallRunnable implements Runnable {

        @Override
        public void run(){

            while (!mIsLoopExit){
                try {
                    Log.d("fuck", "startWhile");
                    while((num = dataIn.read(buf))!= -1){
                        Log.d("fuck", "audioWrite");
                        audioTrack.write(buf, 0, num);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d("Incall", "Incall close!");
        }

    }

}
