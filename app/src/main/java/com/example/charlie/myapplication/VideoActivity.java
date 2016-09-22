package com.example.charlie.myapplication;

import android.app.Activity;
import android.content.Intent;
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

    //連線用到的變數
    private String Ip = "192.168.0.7";
    private String serverIp = "";
    private String brokerPort= "8080";

    //學長的function-processConnect()的變數
    private static MqttClient mqttClient;
    public static final String TOPIC_STATUS = "TurtleCarData";
    private static String clientId = "TurtleCarAndroid";
    public static final int TIMEOUT = 3;

    //通話變數
    private boolean isRecording = false ;
    private Socket socket;
    private MediaPlayer mr;
    private String path = "";
    private AudioCapturer ac;

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

        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        intent = this.getIntent();  //一定要加在onCreate裡,放到外面會壞掉

        Ip = intent.getStringExtra("brokerIP");
        serverIp = intent.getStringExtra("serverIP");

        Log.d(" activity", "bIP:" + Ip);
        Log.d(" activity", "sIP:" + serverIp);


        timbre = intent.getStringExtra("timbre");
        speed = intent.getStringExtra("speed");
        tone = intent.getIntExtra("tone", 0);
        volume = intent.getIntExtra("volume", 0);


        webview.loadUrl("http://"+ Ip +":8080/?action=stream");


        if (Ip == null) {
            Toast.makeText(this, "IP未設定!!!", Toast.LENGTH_LONG).show();
            Log.d("brokerIP Null", "NULLLLLL");
        } else {
            webview.loadUrl("http://" + "192.168.0.7" + ":" + brokerPort + "/javascript_simple.html");
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
            startActivityForResult(intent, REQUEST_MUSIC);
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
                Ip = data.getStringExtra("brokerIp");
            } else if (requestCode == REQUEST_MUSIC) {
                volume = data.getIntExtra("volume", 0);
                tone = data.getIntExtra("tone", 0);
                timbre = data.getStringExtra("timbre");
                speed = data.getStringExtra("speed");
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

            public void run() {
                Log.d("buttonCall","Start!!!!!!!!!!!");
                //record();

            }

        });
        thread.start();

        ac = new AudioCapturer();
        ac.startCapture();
    }

    //掛電話
    public void buttonHangup(View view) {
        isRecording = false;
        //play();
        ac.stopCapture();
    }


/*
    public void record() {

        int frequency = 8000;
        int channelConfiguration =AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        File file= new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/reverseme.pcm");

        // Delete any previousrecording.

        if (file.exists())

            file.delete();

        // Create the new file.

        try {

            file.createNewFile();

        } catch (IOException e) {

            throw new IllegalStateException("Failed to create " + file.toString());

        }

        try {

            // Create a DataOuputStream to write the audiodata into the saved file.
            Log.d("Record", "Record Start~!");
            //socket = new Socket(serverIp,8080);

            //Log.d("record","socket OK~~");

            OutputStream os = new FileOutputStream(file);
            //OutputStream os = socket.getOutputStream();

            BufferedOutputStream bos = new BufferedOutputStream(os);

            DataOutputStream dos = new DataOutputStream(bos);


            // Create a new AudioRecord object to record theaudio.

            int bufferSize = AudioRecord.getMinBufferSize(frequency,channelConfiguration, audioEncoding);

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,

                    frequency, channelConfiguration,

                    audioEncoding, bufferSize);


            short[] buffer = new short[bufferSize];

            audioRecord.startRecording();

            isRecording = true ;

            //byte[] a = new byte[]{0x49, 0x30, 0x00};

            //dos.write(a);

            while (isRecording) {

                int bufferReadResult = audioRecord.read(buffer, 0,bufferSize);

                Log.d("ISRecording", "" +bufferReadResult);
                for (int i = 0; i < bufferReadResult;i++){
                    dos.writeShort(buffer[i]);
                    //dos.flush();
                }

            }

            audioRecord.stop();
            dos.close();

        } catch (Throwable t) {
            Log.e("AudioRecord","Recording Failed");
        }

    }

    public void play() {
        // Get the file we want toplayback.

        File file= new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/reverseme.pcm");
        // Get the length of the audio stored in the file(16 bit so 2 bytes per short)
        // and create a short array to store the recordedaudio.

        int musicLength = (int)(file.length()/2);

        short[] music = new short[musicLength];


        try {

            // Create a DataInputStream to read the audio databack from the saved file.

            InputStream is = new FileInputStream(file);

            BufferedInputStream bis = new BufferedInputStream(is);

            DataInputStream dis = new DataInputStream(bis);

            // Read the file into the musicarray.


            int i = 0;

            while (dis.available() > 0) {

                music[i] = dis.readShort();

                i++;
                Log.d("while","" + music[i]);

            }

            // Close the input streams.

            dis.close();

            // Create a new AudioTrack object using the sameparameters as the AudioRecord

            // object used to create thefile.

            Log.d("audio", "start");
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,

                    8000,

                    AudioFormat.CHANNEL_IN_MONO,

                    AudioFormat.ENCODING_PCM_16BIT,

                    musicLength*2,

                    AudioTrack.MODE_STREAM);

            // Start playback
            audioTrack.play();
            Log.d("play","start");
            // Write the music buffer to the AudioTrackobject
            audioTrack.write(music, 0, musicLength);

            Log.d("write","finish");
            audioTrack.stop() ;
            Log.d("stop","finish");

        } catch (Throwable t) {
            Log.e("AudioTrack","Playback Failed");
        }

    }
    */
}
