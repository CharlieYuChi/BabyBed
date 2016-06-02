package com.example.charlie.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by charlie on 2016/4/26.
 */
public class SocketService extends Service {

    private Handler handler;
    private String serverIP = "";
    private PrintStream writer;
    private int port = 8080;
    private byte[] output;
    private byte VOLUME = 0x41;
    private byte SPEED = 0x44;
    private byte TIMBRE = 0x43;
    private byte TONE = 0x42;
    Bundle bundle;
    int volume;
    int tone;
    String timbre;
    String speed;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiverMusic, new IntentFilter("MUSICINFO"));
        output = new byte[]{};
        Log.d("Service", "service executed");
        output = new byte[]{0x00,0x30};
        Log.d("service1:", output.toString());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bundle = intent.getExtras();
        serverIP = bundle.getString("serverIP");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 如果消息来自子线程
                if (msg.what == 0x234) {
                    // 将读取的内容追加显示在文本框中
                    //show.append("\n" + msg.obj.toString());
                    Log.d("SocketService", msg.obj.toString());
                }
            }
        };


        new Thread(new Runnable() {
            @Override
            public void run() {
                // 开始执行后台任务
                Socket socket;
                try {
                    //socket = new Socket("140.115.204.92", 8080);
                    socket = new Socket("192.168.0.112",8080);
                    output = new byte[]{0x00,0x30};
                    Log.d("service2:", output.toString());
                    //try {44

                    //} catch (IOException e) {
                        //Log.d("service:", "NO~~~~~");
                        //e.printStackTrace();
                    //}
                    //socket = new Socket(serverIP, port);
                    // 客户端启动ClientThread线程不断读取来自服务器的数据
                    new Thread(new ClientThread(socket, handler)).start();
                    writer = new PrintStream(socket.getOutputStream());
                    writer.write(output);

                    Log.d("serviceOut:", output.toString());
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                while (true){
                    try {
                        Thread.sleep(5000);
                        Log.d("Service", "service executed");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public BroadcastReceiver receiverMusic = new BroadcastReceiver() {
        //01 0011 0001
        //volume 000001
        //tone 000010
        //timbre 000011
        //speed 000100
        @Override
        public void onReceive(Context context, Intent intent) {
            volume = intent.getIntExtra("volume", 0);
            tone = intent.getIntExtra("tone", 0);
            timbre = intent.getStringExtra("timbre");
            speed = intent.getStringExtra("speed");
            Toast.makeText(context, "Music: " + intent.getIntExtra("volume",0), Toast.LENGTH_LONG).show();

            Byte temp;
            temp = Byte.valueOf((byte) volume);
            Log.d("socketSend",temp.toString());
            output = new byte[]{VOLUME, 0x31, temp};
            try {
                writer.write(output);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            output = new byte[]{TONE, 0x31};
            try {
                writer.write(output);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            output = new byte[]{TIMBRE, 0x31};
            try {
                writer.write(output);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            output = new byte[]{SPEED, 0x31};
            try {
                writer.write(output);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            //writer.println("Music:" + tone);
            //writer.println("Music:" + timbre);
            //writer.println("Music:" + speed);


        }
    };

    class MyBinder extends Binder {

        public void startDownload() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 执行具体的下载任务
                }
            }).start();
        }

    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
