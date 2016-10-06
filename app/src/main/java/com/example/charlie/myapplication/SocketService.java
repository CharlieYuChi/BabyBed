package com.example.charlie.myapplication;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;

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
    private final byte TERMINATE = 0x5F;
    Bundle bundle;
    int volume;
    int tone;
    int timbre;
    int speed;
    Socket socket;
    boolean socketConnectSuccess = false;

    private final int DANGER = 0;
    private final int MODE = 1;
    private final int PLAYLIST = 2;
    private final int MUSICSTATUS = 3;

    boolean alertFlag = false;

    String dangerDetect;



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("socket","destroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiverMusic, new IntentFilter("MUSICINFO"));
        registerReceiver(receiverMusicControl, new IntentFilter("MUSICCONTROL"));
        registerReceiver(receiverMusicMode, new IntentFilter("MUSICMODE"));
        registerReceiver(receiverPlayList, new IntentFilter("PLAYLIST"));
        registerReceiver(receiverAlert, new IntentFilter("ALERT"));
        registerReceiver(receiverDangerDetect, new IntentFilter("DANGERDETECT"));

        output = new byte[]{};

        output = new byte[]{0x00,0x30};

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        serverIP = intent.getStringExtra("serverIP");
        dangerDetect = intent.getStringExtra("dangerDetect");

        Log.d("socketsocket",serverIP);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 如果消息来自子线程
                if (msg.what == DANGER) {
                    checkState(msg.obj.toString());

                } else if(msg.what == MODE){
                    Log.d("SocketService", "mode");

                    //ArrayList<String> playList = data.getStringArrayList("playlist");

                    Intent intent = new Intent("MODE");
                    intent.putExtra("mode",msg.obj.toString());
                    sendBroadcast(intent);
                    Log.d("SocketService", "modeend3");
                } else if(msg.what == PLAYLIST){

                    Bundle data = msg.getData();
                    //ArrayList<String> playList = data.getStringArrayList("playlist");

                    Intent intent = new Intent("PLAYLISTBACK");
                    intent.putExtra("PLAYLIST", data);
                    sendBroadcast(intent);

                } else if(msg.what == MUSICSTATUS){
                    Intent intent = new Intent("MUSICSTATUS");
                    intent.putExtra("musicStatus", msg.obj.toString());
                    sendBroadcast(intent);
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

                    writer = new PrintStream(socket.getOutputStream());
                    writer.write(output);

                    writer.flush();
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 客户端启动ClientThread线程不断读取来自服务器的数据
                try {
                    if(socket != null){
                        socketConnectSuccess = true;

                        new Thread(new ClientThread(socket, handler)).start();

                        byte dangerType = 0x4D;
                        byte[] content;

                        try {
                            //content = dangerDetect.getBytes();
                            output = new byte[]{dangerType, 0x31, 1};

                            writer.write(output);
                            writer.flush();
                            Thread.sleep(500);

                            if(dangerDetect.contentEquals("0")){
                                content = new byte[]{0};
                                writer.write(content);
                                writer.flush();
                                Thread.sleep(500);
                            }else if(dangerDetect.contentEquals("1")){
                                content = new byte[]{1};
                                writer.write(content);
                                writer.flush();
                                Thread.sleep(500);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }else {
                        Log.d("socketservice", "SOCKetnoconnect");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                while (true){
                    try {
                        Thread.sleep(5000);
                        Log.d("Service", "service executed");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                */

            }
        }).start();

        if (socketConnectSuccess == false){
            Toast.makeText(SocketService.this, "未連接到伺服器",Toast.LENGTH_SHORT).show();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    public BroadcastReceiver receiverMusicControl = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //00 0101  5 play
            //00 0110  6 stop

            final int PLAY_MUSIC = 1;
            final int STOP_MUSIC = 0;
            int control = intent.getIntExtra("control",0);

            if(socketConnectSuccess == true){
                if(control == PLAY_MUSIC){

                    output = new byte[]{0x45,0x31,0x01,0x00};
                    try {
                        writer.write(output);
                        writer.flush();
                        Thread.sleep(500);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else if(control == STOP_MUSIC){

                    output = new byte[]{0x46,0x31,0x01,0x00};
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
            } else {
                Log.d("control","no socket");
                Toast.makeText(SocketService.this, "未連線", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public BroadcastReceiver receiverMusicMode = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final int INTERACTIVE = 1;
            final int NORMAL = 0;
            int control = intent.getIntExtra("mode",0);

            if(socketConnectSuccess == true){
                if(control == INTERACTIVE){
                    Log.d("PLAYPLAY","playmusic");
                    output = new byte[]{0x50,0x32,0x01,0x01};
                    try {
                        writer.write(output);
                        writer.flush();
                        Thread.sleep(500);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else if(control == NORMAL){
                    Log.d("STOPSTOP","stopmusic");
                    output = new byte[]{0x50,0x32,0x01,0x00};
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
            }else {
                Log.d("mode","no socket");
                Toast.makeText(SocketService.this, "未連線", Toast.LENGTH_SHORT).show();
            }
        }
    };

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
            //Toast.makeText(context, "Music: " + intent.getIntExtra("timbre",0), Toast.LENGTH_LONG).show();

            Byte temp;
            Byte dataLength = 1;

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

            temp = (byte) volume;

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

            //終止符號

            output = new byte[]{TERMINATE, 0x31,0};
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

    public BroadcastReceiver receiverPlayList = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(socketConnectSuccess == true){

                int state = intent.getIntExtra("state", 2);
                int list = 0;
                int songname = 1;

                if(state == list){
                    byte dataLength = 0x00;
                    byte requestPlayList = 0x61;

                    output = new byte[]{requestPlayList, 0x30, dataLength};

                    try {
                        writer.write(output);
                        writer.flush();
                        Thread.sleep(500);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }else if(state == songname){
                    byte dataLength;
                    byte sendSongName = 0x70;
                    byte[] content;

                    try {
                        content = intent.getStringExtra("songname").getBytes();
                        dataLength = (byte)content.length;
                        output = new byte[]{sendSongName, 0x31, (byte) (dataLength + 1)};

                        writer.write(output);
                        writer.flush();
                        Thread.sleep(500);

                        writer.write(content);
                        writer.flush();
                        Thread.sleep(500);

                        output = new byte[]{TERMINATE, 0x31,0};
                        writer.write(output);
                        writer.flush();
                        Thread.sleep(500);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            } else {
                Log.d("playlist","no socket");
                Toast.makeText(SocketService.this, "未連線", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void checkState(String content){
        final int notifyID = 1; // 通知的識別號碼
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務

        final String THROWUP = "1";
        final String NOFACE = "2";
        final String STAND = "3";
        //吐奶		0x1
        //找不到臉 	0x2
        //站立		0x3
        // 建立震動效果，陣列中元素依序為停止、震動的時間，單位是毫秒
        long[] vibrate_effect =
                {1000, 500, 1000, 400, 1000, 300, 1000, 200, 1000, 100};
        final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效

        Intent it = new Intent(this, AlertDialogActivity.class);

        //加入判斷寶寶狀態

        Notification notification;
        switch (content){
            case THROWUP:
                if(alertFlag == false){
                notification = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_babydead)
                        .setContentTitle("危險")
                        .setContentText("寶寶吐惹!")
                        .setVibrate(vibrate_effect)
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert))
                        .setLights(0x00FF00, 1000,1000)
                        .build(); // 建立通知
                notificationManager.notify(notifyID, notification); // 發送通知


                    it.putExtra("text","寶寶吐惹!");
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                    alertFlag = true;
                }

                break;

            case NOFACE:
                if(alertFlag == false){
                notification = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_babydead)
                        .setContentTitle("危險")
                        .setContentText("寶寶照不到臉!")
                        .setVibrate(vibrate_effect)
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert))
                        .setLights(0x00FF00, 1000,1000)
                        .build(); // 建立通知
                notificationManager.notify(notifyID, notification); // 發送通知


                    it.putExtra("text","寶寶照不到臉!");
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                    alertFlag = true;
                }

                break;

            case STAND:
                if(alertFlag == false){
                notification = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_babydead)
                        .setContentTitle("危險")
                        .setContentText("寶寶站起來啦!")
                        .setVibrate(vibrate_effect)
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert))
                        .setLights(0x00FF00, 1000,1000)
                        .build(); // 建立通知
                notificationManager.notify(notifyID, notification); // 發送通知


                    it.putExtra("text","寶寶站起來啦!");
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                    alertFlag = true;
                }

                break;

            default:
                break;
        }
    }

    public BroadcastReceiver receiverDangerDetect = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("socketService","receive dnager");

            if(socketConnectSuccess == true){
                byte dangerDetect = 0x4D;
                byte[] content;

                try {
                    String temp = intent.getStringExtra("dangerDetect");
                    output = new byte[]{dangerDetect, 0x31, 1};

                    writer.write(output);
                    writer.flush();
                    Thread.sleep(500);

                    if(temp.contentEquals("0")){
                        content = new byte[]{0};
                        writer.write(content);
                        writer.flush();
                        Thread.sleep(500);
                    }else if(temp.contentEquals("1")){
                        content = new byte[]{1};
                        writer.write(content);
                        writer.flush();
                        Thread.sleep(500);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                Log.d("mode","no socket");
                Toast.makeText(SocketService.this, "未連線", Toast.LENGTH_SHORT).show();
            }

        }
    };


    public BroadcastReceiver receiverAlert = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            setAlertFlag(intent.getBooleanExtra("alert", false));
        }
    };

    public void setAlertFlag(boolean alertFlag){
        this.alertFlag = alertFlag;
    }
}
