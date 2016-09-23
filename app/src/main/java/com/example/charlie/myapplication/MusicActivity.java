package com.example.charlie.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by charlie on 2016/3/6.
 */

public class MusicActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace {

    //Setting用的變數
    private String brokerIp;
    private String brokerPort;

    //Music用的變數
    private int volume;
    private int tone;  //1個8 2個8
    private String timbre;
    private String speed;
    int speedchose;
    int timbrechose;
    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String data2 = "DATA2";
    private static final String volumeField = "50";
    private static final String toneField = "50";
    private static final String timbreField = "電子鋼琴";
    private static final String speedField = "正常";

    //socket用的變數
    private String serverIP;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private static final int REQUEST_MUSIC = 1;

    private Spinner mspinner_timbre, mspinner_speed;
    private ArrayAdapter lunchList_timbre, lunchList_speed;
    private Context mContext;
    private String[] spn_timbre = {"原聲","電子鋼琴", "吉他", "acapella"};
    private String[] spn_speed = {"0.5", "0.75", "正常", "1.5", "2"};
    private Intent intent = new Intent();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private MediaPlayer mp = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        verifyStoragePermissions(this);

        iniBarComponent();  //初始化AppBar
        intent = this.getIntent();
        serverIP = intent.getStringExtra("serverIP");

        final SeekBar volume_bar = (SeekBar) findViewById(R.id.seekbar_volume);
        final SeekBar tone_bar = (SeekBar) findViewById(R.id.seekbar_tone);

        final TextView volume_value = (TextView) findViewById(R.id.value_volume);
        final TextView tone_value = (TextView) findViewById(R.id.value_tone);

        mContext = this.getApplicationContext();
        mspinner_timbre = (Spinner) findViewById(R.id.spinner_timbre);
        mspinner_speed = (Spinner) findViewById(R.id.spinner_speed);

        //readData();

        volume_bar.setProgress(volume);
        volume_value.setText(String.valueOf(volume));
        //Toast.makeText(this,"Cvolume:" + volume,Toast.LENGTH_LONG).show();

        tone_bar.setProgress(tone);
        tone_value.setText("升高" + String.valueOf(tone) + "個8");
        //Toast.makeText(this,"Ctone:" + tone,Toast.LENGTH_LONG).show();

        lunchList_timbre = new ArrayAdapter<String>(MusicActivity.this, R.layout.spinner_item, spn_timbre);
        lunchList_timbre.setDropDownViewResource(R.layout.spinner_item);
        mspinner_timbre.setAdapter(lunchList_timbre);
        lunchList_speed = new ArrayAdapter<String>(MusicActivity.this, R.layout.spinner_item, spn_speed);
        lunchList_speed.setDropDownViewResource(R.layout.spinner_item);
        mspinner_speed.setAdapter(lunchList_speed);

        mspinner_timbre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                timbrechose = position;

                int test = intent.getIntExtra("timbre",0);
                Toast.makeText(MusicActivity.this, "TEST timbre: " + timbrechose,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mspinner_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speedchose = position;

                String test = intent.getStringExtra("speed");
                Toast.makeText(MusicActivity.this, "speed:" + speedchose, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        volume_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int bar_value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume_value.setText(String.valueOf(progress));
                bar_value = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                volume = bar_value;
                //Toast.makeText(MusicActivity.this, "Bar volume:" + volume, Toast.LENGTH_LONG).show();
                intent.putExtra("volume", volume);
                int test = intent.getIntExtra("volume", 0);
                //Toast.makeText(MusicActivity.this, "volume:" + test, Toast.LENGTH_LONG).show();
            }
        });

        tone_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int bar_value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tone_value.setText("升高" + String.valueOf(progress) + "個8");
                bar_value = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tone = bar_value;
                intent.putExtra("tone", bar_value);
                int test = intent.getIntExtra("tone", 0);
                //Toast.makeText(MusicActivity.this, "tone:" + test,Toast.LENGTH_LONG).show();
            }
        });

        //readData();
        setEnterSwichLayout();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //初始化AppBar
    public void iniBarComponent() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_music);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_music);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_music);
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
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_music);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_video) {
            intent.setClass(MusicActivity.this, VideoActivity.class);
            intent.putExtra("serverIP", serverIP);
            intent.putExtra("volume", volume);
            intent.putExtra("tone", tone);
            intent.putExtra("timbre", timbre);
            intent.putExtra("speed", speed);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting) {
            intent.setClass(MusicActivity.this, SettingActivity.class);
            intent.putExtra("serverIP", serverIP);
            intent.putExtra("volume", volume);
            intent.putExtra("tone", tone);
            intent.putExtra("timbre", timbre);
            intent.putExtra("speed", speed);
            startActivityForResult(intent, REQUEST_SETTING);
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_music);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String uripath = ImageFilePath.getPath(this, uri);

            Log.d("uriPath", uripath);

            Log.d("buttonSend","Startsend");
            SendFile sendFile = new SendFile();
            try {
                sendFile.sendFile(uripath,serverIP);
            } catch (IOException e) {

            }
            Log.d("buttonSend","Endsend");
            Toast.makeText(this,"傳送完成^^",Toast.LENGTH_LONG).show();
        }


        if (requestCode == REQUEST_SETTING) {
            brokerIp = data.getStringExtra("brokerIp");
            brokerPort = data.getStringExtra("brokerPort");

        } else if (requestCode == REQUEST_MUSIC) {

        } else {
            setTitle("取消選擇檔案 !!");
        }
    }


    @Override
    public void onDestroy() {
        setExitSwichLayout();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_music);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.finish();
            setExitSwichLayout();
            super.onBackPressed();
        }
    }

    public void buttonSave(View view) {
        saveData();
        setResult(RESULT_OK, intent);
        Toast.makeText(MusicActivity.this, "成功!" + "tim:" + timbrechose
                + "spe:" + speedchose
                + "ton:" + tone
                + "vol:" + volume, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent("MUSICINFO");
        intent.putExtra("volume", volume);
        intent.putExtra("tone", tone);
        intent.putExtra("timbre", timbrechose);
        intent.putExtra("speed", speedchose);
        sendBroadcast(intent);
    }


    public void buttonSend(View view) {
        Log.d("FileDir", String.valueOf(mContext.getFilesDir()));
        fileBrowserIntent();

    }

    // 外部 App 回傳結果的類型判斷碼
    private static final int FILE_SELECT_CODE = 0;
    /**
     * 啟動外部 App 的檔案管理員
     */
    private void fileBrowserIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 設定 MIME Type 但這裡是沒用的 加個心安而已
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, "選擇字型"), FILE_SELECT_CODE );
        } catch (android.content.ActivityNotFoundException ex) {
            // 若使用者沒有安裝檔案瀏覽器的 App 則顯示提示訊息
            Toast.makeText(this, "沒有檔案瀏覽器 是沒辦法選擇字型的", Toast.LENGTH_SHORT).show();
        }
    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void readData() {
        settingsField = getSharedPreferences(data2, 0);
        volume = Integer.valueOf(settingsField.getString(volumeField, ""));
        tone = Integer.valueOf(settingsField.getString(toneField, ""));
        speed = settingsField.getString(speedField, "");
        timbre = settingsField.getString(timbreField, "");
        //Toast.makeText(this, "RVF" + Integer.valueOf(settingsField.getString(volumeField, "")), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, "RTF" + Integer.valueOf(settingsField.getString(toneField, "")), Toast.LENGTH_LONG).show();
    }

    public void saveData() {
        String volTemp = Integer.toString(volume);
        String toneTemp = Integer.toString(tone);
        settingsField = getSharedPreferences(data2, 0);
        settingsField.edit()
                .putString(volumeField, volTemp)
                .putString(speedField, speed)
                .putString(timbreField, timbre)
                .apply();
        settingsField = getSharedPreferences(data2, 0);
        settingsField.edit().putString(toneField, toneTemp).apply();
        //Toast.makeText(this, "VF" + settingsField.getString(volumeField,""), Toast.LENGTH_LONG).show();

        //Toast.makeText(this, "TF" + settingsField.getString(toneField,""), Toast.LENGTH_LONG).show();
    }

    @Override
    public void setEnterSwichLayout() {
        SwitchLayout.animDuration = 1700;
        SwitchLayout.longAnimDuration = 1700;
        BaseAnimViewS.animDuration = 1700;
        BaseAnimViewS.longAnimDuration = 1700;
        SwitchLayout.getSlideFromRight(this, false, BaseEffects.getMoreSlowEffect());
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
                "Music Page", // TODO: Define a title for the content shown.
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
                "Music Page", // TODO: Define a title for the content shown.
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

    /*如果想自定义特效动画时长的话，请在此四个变量对应设置 SwitchLayout.animDuration = 1000;
    * SwitchLayout.longAnimDuration = 2000; BaseAnimViewS.animDuration = 1000;
    * BaseAnimViewS.longAnimDuration = 2000;即可。单位毫秒。*/

}
