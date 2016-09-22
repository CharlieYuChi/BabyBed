package com.example.charlie.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Charlie on 2016/9/22.
 */
public class Incall {
    DataInputStream dataIn;
    AudioTrack audioTrack;
    int num;
    byte [] buf;
    AudioFormat af;


    public Incall(InputStream InputStream) {
        dataIn = new DataInputStream(InputStream);
        buf = new byte [800];
    }

    public void start(){
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,8000,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_8BIT,
            800, AudioTrack.MODE_STREAM);

        audioTrack.play();

        try {
            while((num = dataIn.read(buf))!= -1){
                audioTrack.write(buf, 0, num);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        audioTrack.stop();
    }

}
