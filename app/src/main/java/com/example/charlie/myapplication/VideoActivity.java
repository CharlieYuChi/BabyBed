package com.example.charlie.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlie.myapplication.setting.*;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tandong.swichlayout.BaseAnimViewS;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by charlie on 2016/3/4.
 */
public class VideoActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace {

    DataInputStream reader;
    OutputStream writer;

    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private static final int REQUEST_MUSIC = 1;

    private WebView webview;
    private TextView mtxtRecord;

    //連線用到的變數
    private static String videoIp = "";
    private static String serverIp = "";
    private String videoPort= "8081";
    private int serverPort= 8080;

    //學長的function-processConnect()的變數
    private static MqttClient mqttClient;
    public static final String TOPIC_STATUS = "TurtleCarData";
    private static String clientId = "TurtleCarAndroid";
    public static final int TIMEOUT = 3;

    //通話變數
    private boolean isRecording = false ;
    private Socket socket;
    private String path = "";
    private AudioCapturer ac;
    private Incall ic;
    private ImageButton mbtn_callBaby;
    private ImageButton mbtn_listenBaby;
    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String ipField = "IP";
    private static final String serverIpField = "serverIp";

    Intent intent;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        iniBarComponent();  //初始化AppBar

        setEnterSwichLayout();

        mbtn_callBaby = (ImageButton) findViewById(R.id.btn_callBaby);
        mbtn_listenBaby = (ImageButton) findViewById(R.id.btn_listenBaby);
        mtxtRecord = (TextView) findViewById(R.id.txtRecord);
        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        intent = this.getIntent();  //一定要加在onCreate裡,放到外面會壞掉

        videoIp = intent.getStringExtra("brokerIp");
        serverIp = intent.getStringExtra("serverIp");

        Log.d(" video", "bIP:" + videoIp);
        Log.d(" video", "sIP:" + serverIp);


        timbre = intent.getStringExtra("timbre");
        speed = intent.getStringExtra("speed");
        tone = intent.getIntExtra("tone", 0);
        volume = intent.getIntExtra("volume", 0);


        if (videoIp == null) {
            Toast.makeText(this, "IP未設定!!!", Toast.LENGTH_SHORT).show();
            Log.d("brokerIP Null", "NULLLLLL");
        } else {
            webview.loadUrl("http://"+ videoIp +":"+videoPort+"/baby.html");
            Log.d("No Null", "NONOONONO");
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //初始化AppBar
    public void iniBarComponent() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_webview);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_video);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_video);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent();

        if (id == R.id.nav_main) {
            setExitSwichLayout();
            this.finish();
        } else if (id == R.id.nav_music) {
            intent.setClass(VideoActivity.this, MusicActivity.class);
            intent.putExtra("serverIp", serverIp);
            intent.putExtra("brokerIp", videoIp);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_video) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_video);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_setting) {
            intent.setClass(VideoActivity.this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_SETTING);
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_video);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SETTING) {
                videoIp = data.getStringExtra("brokerIp");
                serverIp = data.getStringExtra("serverIp");
                saveData();
            }
        }
    }

    @Override
    public void onDestroy() {
        setExitSwichLayout();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_video);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.finish();
            setExitSwichLayout();
            super.onBackPressed();
        }
    }


    //學長的function還不知道功能
    private void processConnect(String brokerIp, String brokerPort) {
        String broker = "tcp://" + brokerIp + ":" + brokerPort;

        try {
            clientId = clientId + System.currentTimeMillis();

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setConnectionTimeout(TIMEOUT);

            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            mqttClient.connect(mqttConnectOptions);
            mqttClient.subscribe(TOPIC_STATUS);

            Toast.makeText(this, R.string.connected, Toast.LENGTH_LONG).show();
        } catch (MqttException me) {
            Toast.makeText(this, R.string.connect_failure, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void setEnterSwichLayout() {
        SwitchLayout.animDuration = 1700;
        SwitchLayout.longAnimDuration = 1700;
        BaseAnimViewS.animDuration = 1700;
        BaseAnimViewS.longAnimDuration = 1700;
        SwitchLayout.getSlideFromTop(this, false, BaseEffects.getMoreSlowEffect());
    }

    @Override
    public void setExitSwichLayout() {
        SwitchLayout.animDuration = 3000;
        SwitchLayout.longAnimDuration = 3000;
        BaseAnimViewS.animDuration = 3000;
        BaseAnimViewS.longAnimDuration = 3000;
        SwitchLayout.get3DRotateFromLeft(this, true, BaseEffects.getMoreSlowEffect());
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Video Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.charlie.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Video Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.charlie.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    //打電話
    public void buttonCallBaby(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] output = new byte[]{0x49, 0x30, 0x00};

                try {
                    socket = new Socket(serverIp, serverPort);
                    writer = socket.getOutputStream();
                    writer.write(output);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(socket != null){
                    ac = new AudioCapturer(socket);
                    ic = new Incall(socket);

                    ac.startCapture();
                    ic.start();


                }
            }
        });

        thread.start();

        if(socket != null){
            mtxtRecord.setText("通話中...");
            mbtn_listenBaby.setEnabled(false);
        }else if(socket == null){
            Toast.makeText(VideoActivity.this, "未連線", Toast.LENGTH_SHORT).show();
        }

    }

    //掛電話
    public void buttonHangup(View view) {

        if(ac != null){
            if(ac.isCaptureStarted() == true){
                ac.stopCapture();
            }
        }

        if(ic != null){
            if(ic.isCaptureStarted() == true){
                ic.stop();
            }
        }

        try {
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mtxtRecord.setText("準備通話");
        mbtn_listenBaby.setEnabled(true);
        mbtn_callBaby.setEnabled(true);
        if(socket == null){
            Toast.makeText(VideoActivity.this, "未連線", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveData(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(ipField, videoIp)
                .putString(serverIpField, serverIp)
                .apply();
    }


    public void buttonListenBaby(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] output = new byte[]{0x49, 0x30, 0x00};

                try {
                    socket = new Socket(serverIp, serverPort);
                    writer = socket.getOutputStream();
                    writer.write(output);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(socket != null){
                    ic = new Incall(socket);
                    ic.start();
                }
            }
        });

        thread.start();

        if(socket != null){
            mtxtRecord.setText("通話中...");
            mbtn_callBaby.setEnabled(false);
        }else if(socket == null){
            Toast.makeText(VideoActivity.this, "未連線", Toast.LENGTH_SHORT).show();
        }
    }

}
