package com.example.charlie.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.charlie.myapplication.setting.*;
import com.tandong.swichlayout.BaseAnimViewS;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by charlie on 2016/3/4.
 */
public class VideoActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace{

    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;

    private WebView webview;

    //連線用到的變數
    private String Ip;
    private String brokerPort;

    //學長的function-processConnect()的變數
    private static MqttClient mqttClient;
    public static final String TOPIC_STATUS = "TurtleCarData";
    private static String clientId = "TurtleCarAndroid";
    public static final int TIMEOUT = 3;

    Intent intent;

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

        brokerPort = intent.getStringExtra("brokerPort");
        Ip = intent.getStringExtra("ip");

        Log.d(" activity", "bIP:" + Ip);
        Log.d(" activity", "Port:" + brokerPort);

        timbre = intent.getStringExtra("timbre");
        speed = intent.getStringExtra("speed");
        tone = intent.getIntExtra("tone", 0);
        volume = intent.getIntExtra("volume", 0);

        if(Ip == null || brokerPort == null){
            Toast.makeText(this,"IP or Port 未設定!!!", Toast.LENGTH_LONG).show();
            Log.d("brokerIP Null","NULLLLLL");
        }else {
            webview.loadUrl("http://" + Ip + ":" + brokerPort + "/javascript_simple.html");
            Log.d("No Null", "NONOONONO");
        }

    }

    //初始化AppBar
    public void iniBarComponent(){
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

        if (id == R.id.nav_main){
            setExitSwichLayout();
            this.finish();
        } else if (id == R.id.nav_music) {
            intent.setClass(VideoActivity.this, MusicActivity.class);
            startActivityForResult(intent, REQUEST_MUSIC);
            this.finish();
        } else if (id == R.id.nav_video) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_video);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_setting){
            intent.setClass(VideoActivity.this, com.example.charlie.myapplication.setting.SettingActivity.class);
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
                brokerPort = data.getStringExtra("brokerPort");
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
        }
        catch (MqttException me) {
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
}
