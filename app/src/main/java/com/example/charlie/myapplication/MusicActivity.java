package com.example.charlie.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tandong.swichlayout.BaseAnimViewS;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

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
    private int tone;
    private String timbre;
    private String speed;
    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String data2 = "DATA2";
    private static final String volumeField = "50";
    private static final String toneField = "50";
    private static final String timbreField = "木魚聲";
    private static final String speedField = "0.25";

    //socket用的變數
    private String serverIP;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;

    private Spinner mspinner_timbre, mspinner_speed;
    private ArrayAdapter<String> lunchList_timbre, lunchList_speed;
    private Context mContext;
    private String[] spn_timbre = {"木魚聲", "響板聲", "鼓聲", "音聲", "無聲"};
    private String[] spn_speed = {"0.25", "0.5", "正常", "1.25", "1.5", "2"};
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        iniBarComponent();  //初始化AppBar

        final SeekBar volume_bar = (SeekBar) findViewById(R.id.seekbar_volume);
        final SeekBar tone_bar = (SeekBar) findViewById(R.id.seekbar_tone);

        final TextView volume_value = (TextView) findViewById(R.id.value_volume);
        final TextView tone_value = (TextView) findViewById(R.id.value_tone);

        mContext = this.getApplicationContext();
        mspinner_timbre = (Spinner)findViewById(R.id.spinner_timbre);
        mspinner_speed = (Spinner)findViewById(R.id.spinner_speed);

        //readData();

        volume_bar.setProgress(volume);
        volume_value.setText(String.valueOf(volume));
        //Toast.makeText(this,"Cvolume:" + volume,Toast.LENGTH_LONG).show();

        tone_bar.setProgress(tone);
        tone_value.setText(String.valueOf(tone));
        //Toast.makeText(this,"Ctone:" + tone,Toast.LENGTH_LONG).show();


        lunchList_timbre = new ArrayAdapter<String>(MusicActivity.this, android.R.layout.simple_spinner_item, spn_timbre);
        mspinner_timbre.setAdapter(lunchList_timbre);
        lunchList_speed = new ArrayAdapter<String>(MusicActivity.this, android.R.layout.simple_spinner_item, spn_speed);
        mspinner_speed.setAdapter(lunchList_speed);

        mspinner_timbre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MusicActivity.this, "timbre: " + spn_timbre[position],Toast.LENGTH_LONG).show();
                timbre = spn_timbre[position];
                intent.putExtra("timbre", spn_timbre[position]);
                String test = intent.getStringExtra("timbre");
                //Toast.makeText(MusicActivity.this, "TEST timbre: " + test,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mspinner_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speed = spn_speed[position];
                intent.putExtra("speed", spn_speed[position]);
                String test = intent.getStringExtra("speed");
                Toast.makeText(MusicActivity.this, "speed:" + test,Toast.LENGTH_LONG).show();
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
                tone_value.setText(String.valueOf(progress));
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

        setEnterSwichLayout();
    }

    //初始化AppBar
    public void iniBarComponent(){
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
        } else if (id == R.id.nav_socket){
            intent.setClass(MusicActivity.this, SocketActivity.class);
            Intent intent1 = this.getIntent();
            serverIP = intent1.getStringExtra("serverIP");
            intent.putExtra("serverIP", serverIP);
            intent.putExtra("volume", volume);
            intent.putExtra("tone", tone);
            intent.putExtra("timbre", timbre);
            intent.putExtra("speed", speed);

            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting){
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
            if (requestCode == REQUEST_SETTING) {
                brokerIp = data.getStringExtra("brokerIp");
                brokerPort = data.getStringExtra("brokerPort");
            } else if (requestCode == REQUEST_MUSIC) {

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
        Toast.makeText(MusicActivity.this, "成功!" + "tim:" + timbre
                                                    +"spe:" + speed
                                                    +"ton:" + tone
                                                    +"vol:" + volume,Toast.LENGTH_LONG).show();
    }


    public void readData(){
        settingsField = getSharedPreferences(data, 0);
        volume = Integer.valueOf(settingsField.getString(volumeField, ""));
        tone = Integer.valueOf(settingsField.getString(toneField, ""));
        speed = settingsField.getString(speedField, "");
        timbre = settingsField.getString(timbreField, "");
        //Toast.makeText(this, "RVF" + Integer.valueOf(settingsField.getString(volumeField, "")), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, "RTF" + Integer.valueOf(settingsField.getString(toneField, "")), Toast.LENGTH_LONG).show();
    }

    public void saveData(){
        String volTemp = Integer.toString(volume);
        String toneTemp = Integer.toString(tone);
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(volumeField, volTemp)
                .putString(speedField, speed)
                .putString(timbreField, timbre)
                .commit();
        settingsField = getSharedPreferences(data2,0);
        settingsField.edit().putString(toneField, toneTemp).commit();
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

    /*如果想自定义特效动画时长的话，请在此四个变量对应设置 SwitchLayout.animDuration = 1000;
    * SwitchLayout.longAnimDuration = 2000; BaseAnimViewS.animDuration = 1000;
    * BaseAnimViewS.longAnimDuration = 2000;即可。单位毫秒。*/
}
