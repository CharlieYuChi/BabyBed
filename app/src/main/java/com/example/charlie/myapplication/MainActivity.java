package com.example.charlie.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace{

    //Setting用的變數
    private String brokerIp;
    private String brokerPort;
    public static String serverIP = "";
    private String babyName;
    private String height;
    private String weight;
    private int gender;
    private static final int GENDER_BOY = 0;
    private static final int GENDER_GIRL = 1;
    private ImageView genderImg;
    private int birthYear;
    private int birthMonth;
    private int birthDay;


    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;

    //沒用到的
    private boolean processMenu = false;
    private static String clientId = "TurtleCarAndroid";
    private static MqttClient mqttClient;
    public static final String TOPIC_STATUS = "TurtleCarData";
    public static final int TIMEOUT = 3;

    private TextView show_name;
    private TextView show_height;
    private TextView show_weight;
    private TextView show_percent;
    private String[] numberOfDays = {"0", "31", "59", "90", "120", "151", "181", "212", "243", "273", "304", "334"};
    private RoundCornerProgressBar mbirthDay_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setEnterSwichLayout();

        show_name = (TextView) findViewById(R.id.txtEdtName);
        show_height = (TextView) findViewById(R.id.txtEdtHeight);
        show_weight = (TextView) findViewById(R.id.txtEdtWeight);
        show_percent = (TextView) findViewById(R.id.txtPercent);


        mbirthDay_bar = (RoundCornerProgressBar) findViewById(R.id.birthDay_bar);
        mbirthDay_bar.setMax(365);
        mbirthDay_bar.setProgress(0);

        genderImg = (ImageView) findViewById(R.id.imgGender);

        //Toast.makeText(MainActivity.this, "onCreate" + "serverIP" + serverIP, Toast.LENGTH_LONG).show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }



    @Override
    public void onDestroy() {

        setExitSwichLayout();

        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(getClass().getName(), me.toString());
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(MainActivity.this, "onResume" + "serverIP" + serverIP, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SETTING) {

                brokerIp = data.getStringExtra("brokerIp");
                brokerPort = data.getStringExtra("brokerPort");
                serverIP = data.getStringExtra("serverIP");
                babyName = data.getStringExtra("babyName");
                height = data.getStringExtra("height");
                weight = data.getStringExtra("weight");
                gender = data.getIntExtra("gender", 0);
                birthYear = data.getIntExtra("birthYear",0);
                birthMonth = data.getIntExtra("birthMonth",0);
                birthDay = data.getIntExtra("birthDay",0);

                //Toast.makeText(MainActivity.this, "MainResultserverIP:" + serverIP , Toast.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Main" +"month:" + birthMonth + "day:" + birthDay,Toast.LENGTH_LONG).show();

                show_name.setText(babyName);
                show_height.setText(height);
                show_weight.setText(weight);

                setBirthDayPercent(birthMonth, birthDay);

                if(gender == GENDER_BOY){
                    genderImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_boy,null));
                } else if(gender == GENDER_GIRL){
                    genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_girl,null));
                }

                //Toast.makeText(MainActivity.this, "IP:" + brokerIp + " Port:" + brokerPort + "serverIP:" + serverIP , Toast.LENGTH_LONG).show();
                processConnect(brokerIp, brokerPort);
            } else if (requestCode == REQUEST_MUSIC) {

                volume = data.getIntExtra("volume", 0);
                tone = data.getIntExtra("tone", 0);
                timbre = data.getStringExtra("timbre");
                speed = data.getStringExtra("speed");

            }
        }

        processMenu = false;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent();

        if (id == R.id.nav_main){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_music) {
            intent.setClass(MainActivity.this, MusicActivity.class);
            startActivityForResult(intent, REQUEST_MUSIC);
        } else if (id == R.id.nav_video) {
            intent.setClass(MainActivity.this, VideoActivity.class);
            intent.putExtra("brokerIP", brokerIp);
            intent.putExtra("brokerPort", brokerPort);
            startActivity(intent);
        } else if (id == R.id.nav_socket){
            intent.setClass(MainActivity.this, SocketActivity.class);
            intent.putExtra("volume", volume);
            intent.putExtra("tone", tone);
            intent.putExtra("timbre", timbre);
            intent.putExtra("speed", speed);
            intent.putExtra("serverIP", serverIP);

            String test = intent.getStringExtra("serverIP");
            //Toast.makeText(MainActivity.this,  "NavserverIP:" + test , Toast.LENGTH_LONG).show();
            startActivityForResult(intent, REQUEST_SETTING);
        } else if (id == R.id.nav_setting){
            intent.setClass(MainActivity.this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_SETTING);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setBirthDayPercent(int birthMonth, int birthDay){
        int birth_total_Days = Integer.valueOf(numberOfDays[birthMonth]) + birthDay;
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int days = calendar.get(Calendar.DAY_OF_MONTH);
        int nowDays = Integer.valueOf(numberOfDays[month]) + days;
        int percent = 0;

        if(birth_total_Days > nowDays){
            int test1 = 365 - (birth_total_Days - nowDays);
            mbirthDay_bar.setProgress(test1);
            percent = birth_total_Days - nowDays;
            String text = Integer.toString(percent);
            show_percent.setText(text);
        } else if(nowDays > birth_total_Days){
            int test2 = nowDays - birth_total_Days;
            mbirthDay_bar.setProgress(test2);
            percent = 365 - (nowDays - birth_total_Days);
            String text = Integer.toString(percent);
            show_percent.setText(text);
        } else {
            mbirthDay_bar.setProgress(365);
            show_percent.setText("0");
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

    //換場特效:http://blog.csdn.net/jay100500/article/details/42227365

    @Override
    public void setEnterSwichLayout() {
        SwitchLayout.get3DRotateFromLeft(this,false,null);
    }

    @Override
    public void setExitSwichLayout() {
        SwitchLayout.get3DRotateFromRight(this,false,null);
    }
}
