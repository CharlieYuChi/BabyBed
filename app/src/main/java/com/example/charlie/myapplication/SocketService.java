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
    int timbre;
    int speed;
    Socket socket;

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

                try {
                    //socket = new Socket("140.115.204.92", 8080);
                    //socket = new Socket("192.168.0.113",8080);
                    socket = new Socket(serverIP,8080);

                    output = new byte[]{0x00,0x30};
                    Log.d("service2:", output.toString());

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
            timbre = intent.getIntExtra("timbre",0);
            speed = intent.getIntExtra("speed",0);
            Toast.makeText(context, "Music: " + intent.getIntExtra("timbre",0), Toast.LENGTH_LONG).show();

            Byte temp;

            temp = (byte) volume;
            Byte dataLength = 1;
            Log.d("socketSend1",temp.toString());
            output = new byte[]{VOLUME, 0x31, dataLength,temp};
            try {
                writer.write(output);
                writer.flush();
                Thread.sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            temp = (byte) tone;

            Log.d("socketSend2",temp.toString());
            output = new byte[]{TONE, 0x31, dataLength , temp};
            try {
                writer.write(output);
                writer.flush();
                Thread.sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            temp = (byte) speed;
            Log.d("socketSend3",temp.toString());
            output = new byte[]{SPEED, 0x31, dataLength, temp};
            try {
                writer.write(output);
                writer.flush();
                Thread.sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            temp = (byte)timbre;
            Log.d("socketSend4",temp.toString());
            output = new byte[]{TIMBRE, 0x31, dataLength ,temp};
            try {
                writer.write(output);
                writer.flush();
                Thread.sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
