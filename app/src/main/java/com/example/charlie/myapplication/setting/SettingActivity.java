package com.example.charlie.myapplication.setting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlie.myapplication.MusicActivity;
import com.example.charlie.myapplication.R;
import com.example.charlie.myapplication.SocketActivity;
import com.example.charlie.myapplication.VideoActivity;
import com.example.charlie.myapplication.tab.TabFragment;
import com.tandong.swichlayout.BaseAnimViewS;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

/**
 * Created by charlie on 2016/4/9.
 */
public class SettingActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace
        , SetConnectFragment.callBackConnect, SetBabyInfoFragment.callBackBaby{

    //SetConnect的變數
    private static String brokerIp;
    private String brokerPort;
    private static String serverIP;

    //SetBaby的變數
    private static String name;
    private static String height;
    private static String weight;
    private static int gender;
    private static int birthYear;
    private static int birthMonth;
    private static int birthDay;

    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String ipField = "IP";
    private static final String portField = "PORT";
    private static final String serverIPField = "SERVER_IP";
    private static final String nameField = "NAME";
    private static final String heightField = "HEIGHT";
    private static final String weightField = "WEIGHT";
    private static final String genderField = "GENDER";
    private static final String birthYearField = "YEAR";
    private static final String birthMonthField = "MONTH";
    private static final String birthDayField = "DAY";

    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;

    private static boolean isServiceExist;

    Intent intent;
    /**
     *
     * @param bIP
     * @param sIP
     *
     * 用來讀取連線設定的資料
     */
    public void saveConnect(String bIP, String sIP){
        brokerIp = bIP;
        serverIP = sIP;

        intent.putExtra("brokerIp", brokerIp);
        intent.putExtra("serverIP", serverIP);

        saveConnectData();
        setResult(RESULT_OK, intent);
    }

    /**
     *
     * @param data
     * 用來讀取Baby的資料
     *
     * !!性別儲存還有問題
     */
    public void saveBaby(Bundle data){

        name = data.getString("name");
        height = data.getString("height");
        weight = data.getString("weight");
        gender = data.getInt("gender");

        birthYear = data.getInt("year");
        birthMonth = data.getInt("month");
        birthDay = data.getInt("day");


        intent.putExtra("babyName", name);
        intent.putExtra("height", height);
        intent.putExtra("weight", weight);

        intent.putExtra("gender", gender);
        intent.putExtra("birthYear", birthYear);
        intent.putExtra("birthMonth", birthMonth);
        intent.putExtra("birthDay", birthDay);

        saveBabyData();
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        intent = this.getIntent();
        iniBarComponent();  //初始化BAR
        initTabFragment(savedInstanceState);  //初始化fragment
    }


    //初始化AppBar
    public void iniBarComponent(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_setting);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_setting);
        navigationView.setNavigationItemSelectedListener(this);

    }

    //初始化fragment
    private void initTabFragment(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            TabFragment fragment = new TabFragment();
            transaction.replace(R.id.setting_content_fragment, fragment);
            transaction.commit();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = this.getIntent();

        if (id == R.id.nav_main){
            //Log.d("setEnd ", brokerIp);
            intent.putExtra("babyName", name);
            intent.putExtra("height", height);
            intent.putExtra("weight", weight);

            intent.putExtra("gender", gender);
            intent.putExtra("birthYear", birthYear);
            intent.putExtra("birthMonth", birthMonth);
            intent.putExtra("birthDay", birthDay);
            setResult(RESULT_OK, intent);
            setExitSwichLayout();
            this.finish();
        } else if (id == R.id.nav_music) {
            intent.setClass(SettingActivity.this, MusicActivity.class);
            intent.putExtra("brokerIp", brokerIp);
            intent.putExtra("serverIp", serverIP);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_video) {
            intent.setClass(SettingActivity.this, VideoActivity.class);
            intent.putExtra("brokerIp", brokerIp);
            intent.putExtra("serverIp", serverIP);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_setting);
            drawer.closeDrawer(GravityCompat.START);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_setting);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void saveConnectData(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(ipField, brokerIp)
                .putString(serverIPField, serverIP)
                .apply();
    }

    public void saveBabyData(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(nameField, name)
                .putString(heightField, height)
                .putString(weightField, weight)
                .putInt(genderField, gender)
                .putInt(birthYearField, birthYear)
                .putInt(birthMonthField, birthMonth)
                .putInt(birthDayField, birthDay)
                .apply();

    }

    public void setIsServiceExist(boolean exist){
        isServiceExist = exist;
    }

    public boolean getIsServiceExist(){
        return isServiceExist;
    }

    @Override
    public void setEnterSwichLayout() {
        SwitchLayout.animDuration = 1700;
        SwitchLayout.longAnimDuration = 1700;
        BaseAnimViewS.animDuration = 1700;
        BaseAnimViewS.longAnimDuration = 1700;
        SwitchLayout.getSlideFromBottom(this, false, BaseEffects.getMoreSlowEffect());
    }

    @Override
    public void setExitSwichLayout() {
        SwitchLayout.animDuration = 3000;
        SwitchLayout.longAnimDuration = 3000;
        BaseAnimViewS.animDuration = 3000;
        BaseAnimViewS.longAnimDuration = 3000;
        SwitchLayout.get3DRotateFromLeft(this, true, BaseEffects.getMoreSlowEffect());
    }

    public void buttonClick(View view) {
        Toast.makeText(this,"Button Click",Toast.LENGTH_LONG).show();
    }
}
