package com.example.charlie.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.example.charlie.myapplication.setting.*;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace{

    //Setting用的變數
    private static String brokerIp;
    private static String serverIp = "";
    private static String babyName;
    private static String height;
    private static String weight;
    private static String headshotPath;
    private static String backgroundPath;
    private int gender;
    private static final int GENDER_BOY = 0;
    private static final int GENDER_GIRL = 1;
    private ImageView genderImg;
    private static int birthYear;
    private static int birthMonth;
    private static int birthDay;
    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String nameField = "NAME";
    private static final String heightField = "HEIGHT";
    private static final String weightField = "WEIGHT";
    private static final String genderField = "GENDER";
    private static final String headshotField = "HEADSHOT";
    private static final String bakcgroundField = "BACKGROUND";


    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;

    // 外部 App 回傳結果的類型判斷碼
    private static final int FILE_SELECT_BACKGROUND = 2;
    private static final int FILE_SELECT_HEADSHOT = 3;


    //沒用到的
    private boolean processMenu = false;
    private static String clientId = "TurtleCarAndroid";
    private static MqttClient mqttClient;
    public static final String TOPIC_STATUS = "TurtleCarData";
    public static final int TIMEOUT = 3;

    private TextView show_name;
    private TextView show_height;
    private TextView show_weight;
    private TextView show_injectionType;
    private TextView show_injectDay;
    private ImageView mimageView3;
    private ImageView mmain_imageBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setEnterSwichLayout();

        show_name = (TextView) findViewById(R.id.txtEdtName);
        show_height = (TextView) findViewById(R.id.txtEdtHeight);
        show_weight = (TextView) findViewById(R.id.txtEdtWeight);
        show_injectionType = (TextView) findViewById(R.id.txtInjectType);
        show_injectDay = (TextView) findViewById(R.id.txtInjectDay);



        genderImg = (ImageView) findViewById(R.id.imgGender);

        mimageView3 = (ImageView) findViewById(R.id.imageView3);
        mmain_imageBackground = (ImageView) findViewById(R.id.main_imageBackground);

        setImage();


        readData();

        Log.d("MAIN", "name " + babyName);
        Log.d("MAIN", "headshot " + headshotPath);
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

        Intent stopIntent = new Intent(this, SocketService.class);
        stopService(stopIntent);
        /*
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(getClass().getName(), me.toString());
            }
        }
        */
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readData();
        //Toast.makeText(MainActivity.this, "onResume" + "serverIP" + serverIP, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_SETTING) {
                Log.d("FILE","set");
                brokerIp = data.getStringExtra("brokerIp");
                serverIp = data.getStringExtra("serverIP");
                babyName = data.getStringExtra("babyName");
                height = data.getStringExtra("height");
                weight = data.getStringExtra("weight");
                gender = data.getIntExtra("gender", 0);
                birthYear = data.getIntExtra("birthYear",0);
                birthMonth = data.getIntExtra("birthMonth",0);
                birthDay = data.getIntExtra("birthDay",0);

                Log.d(" activity", "bIP:" + brokerIp);
                Log.d(" activity", "sIP:" + serverIp);
                Log.d(" activity", "name:" + babyName);
                Log.d(" activity", "hei:" + height);
                Log.d(" activity", "wei:" + weight);
                Log.d(" activity", "gen:" + gender);
                Log.d(" activity", "year:" + birthYear);
                Log.d(" activity", "mon:" + birthMonth);
                Log.d(" activity", "day:" + birthDay);

                saveBabyData();
                //Toast.makeText(MainActivity.this, "MainResultserverIP:" + serverIP , Toast.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Main" +"month:" + birthMonth + "day:" + birthDay,Toast.LENGTH_LONG).show();

                show_name.setText(babyName);
                show_height.setText(height);
                show_weight.setText(weight);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (gender == GENDER_BOY) {
                        genderImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_boy, null));
                    } else if (gender == GENDER_GIRL) {
                        genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_girl, null));
                    }
                }
                //Toast.makeText(MainActivity.this, "IP:" + brokerIp + " Port:" + brokerPort + "serverIP:" + serverIP , Toast.LENGTH_LONG).show();
            } else if (requestCode == REQUEST_MUSIC) {
                Log.d("FILE","music");
                volume = data.getIntExtra("volume", 0);
                tone = data.getIntExtra("tone", 0);
                timbre = data.getStringExtra("timbre");
                speed = data.getStringExtra("speed");

            } else if(requestCode == FILE_SELECT_BACKGROUND){
                Log.d("FILE","start");
                // 取得檔案路徑 Uri
                Uri uri = data.getData();
                final String uripath = ImageFilePath.getPath(this, uri);
                Log.d("FILE","uri"+uripath);
                Bitmap temp = BitmapFactory.decodeFile(uripath);
                mmain_imageBackground.setImageBitmap(temp);

                if( uripath.isEmpty() ){
                    Toast.makeText(this, "檔案不對勁!", Toast.LENGTH_SHORT).show();
                    return;
                }

                backgroundPath = uripath;
                saveImage();

            } else if(requestCode == FILE_SELECT_HEADSHOT){
                Log.d("FILE","start");
                // 取得檔案路徑 Uri
                Uri uri = data.getData();
                final String uripath = ImageFilePath.getPath(this, uri);
                Log.d("FILE","uri"+uripath);
                Bitmap temp = BitmapFactory.decodeFile(uripath);
                mimageView3.setImageBitmap(temp);

                if( uripath.isEmpty() ){
                    Toast.makeText(this, "檔案不對勁!", Toast.LENGTH_SHORT).show();
                    return;
                }

                headshotPath = uripath;
                saveImage();
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

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent();

        if (id == R.id.nav_main){
            // 建立大圖示需要的Bitmap物件
            Bitmap largeIcon = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_babydead);
            long[] vibrate_effect =
                    {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};
            final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效

            final int notifyID = 1; // 通知的識別號碼
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
            final Notification notification = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_babydead)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_babydead))
                    .setContentTitle("危險")
                    .setContentText("寶寶吐的一蹋糊塗!")
                    .setVibrate(vibrate_effect)
                    .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert))
                    .setLights(0x00FF00, 1000,1000)
                    .build(); // 建立通知
            notificationManager.notify(notifyID, notification); // 發送通知
            //https://magiclen.org/android-notifications/

            final View temp = LayoutInflater.from(MainActivity.this).inflate(R.layout.alert_dialog, null);
            TextView alert = (TextView) temp.findViewById(R.id.alert_content);
            alert.setText("已枯~~");
            new AlertDialog.Builder(MainActivity.this)
                    .setView(temp)
                    .show();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_music) {
            intent.setClass(MainActivity.this, MusicActivity.class);
            intent.putExtra("serverIp", serverIp);
            intent.putExtra("brokerIp", brokerIp);
            startActivityForResult(intent, REQUEST_MUSIC);
        } else if (id == R.id.nav_video) {
            intent.setClass(MainActivity.this, VideoActivity.class);
            intent.putExtra("brokerIp", brokerIp);
            intent.putExtra("serverIp", serverIp);
            startActivity(intent);
        }  else if (id == R.id.nav_setting){
            intent.setClass(MainActivity.this, com.example.charlie.myapplication.setting.SettingActivity.class);
            startActivityForResult(intent, REQUEST_SETTING);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void saveBabyData(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(nameField, babyName)
                .putString(heightField, height)
                .putString(weightField, weight)
                .putInt(genderField, gender)
                .apply();

    }

    public void saveImage(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(headshotField, headshotPath)
                .putString(bakcgroundField, backgroundPath)
                .apply();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void readData(){
        settingsField = this.getSharedPreferences(data,0);
        show_name.setText(settingsField.getString(nameField, ""));
        show_height.setText(settingsField.getString(heightField, ""));
        show_weight.setText(settingsField.getString(weightField, ""));

        if (settingsField.getInt(genderField,0) == GENDER_BOY) {
            genderImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_boy, null));
        } else if (gender == GENDER_GIRL) {
            genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_girl, null));
        }
    }

    public void buttonChangeBackground(View view) {
        fileBrowserIntent(FILE_SELECT_BACKGROUND);
    }

    public void buttonChangeHeadShot(View view) {
        fileBrowserIntent(FILE_SELECT_HEADSHOT);
    }


    /**
     * 啟動外部 App 的檔案管理員
     */
    private void fileBrowserIntent(int filecode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 設定 MIME Type 但這裡是沒用的 加個心安而已
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, "選擇字型"), filecode );
        } catch (android.content.ActivityNotFoundException ex) {
            // 若使用者沒有安裝檔案瀏覽器的 App 則顯示提示訊息
            Toast.makeText(this, "沒有檔案瀏覽器 是沒辦法選擇字型的", Toast.LENGTH_SHORT).show();
        }
    }

    public void setImage(){
        Log.d("mainmain", "setimage");
        settingsField = this.getSharedPreferences(data,0);
        String headTempPath =  settingsField.getString(headshotField, "");
        String backTempPath =  settingsField.getString(bakcgroundField, "");
        Log.d("mainmain", headTempPath);
        if(headTempPath != null){

            Bitmap temp = BitmapFactory.decodeFile(headTempPath);
            //mimageView3.setImageBitmap(temp);
        } else {
            Log.d("mainmain", "default");
            mimageView3.setImageResource(R.drawable.baby);
        }

        if(backTempPath != null){
            Bitmap temp = BitmapFactory.decodeFile(backTempPath);
            //mmain_imageBackground.setImageBitmap(temp);
        } else {
            mmain_imageBackground.setImageResource(R.drawable.main_backgroud);
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

        }
        catch (MqttException me) {
        }
    }

    /*
    //算下次要打疫苗的日期
    public int whichDay(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        switch (year - birthYear){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 5:
                break;
        }

    }

*/
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

/**
 *
 *
 setBirthDayPercent(birthMonth, birthDay);

 private TextView show_percent;
 private String[] numberOfDays = {"0", "31", "59", "90", "120", "151", "181", "212", "243", "273", "304", "334"};
 private RoundCornerProgressBar mbirthDay_bar;

 show_percent = (TextView) findViewById(R.id.txtPercent);


 mbirthDay_bar = (RoundCornerProgressBar) findViewById(R.id.birthDay_bar);
 mbirthDay_bar.setMax(365);
 mbirthDay_bar.setProgress(0);

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
 **/