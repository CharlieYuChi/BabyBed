package com.example.charlie.myapplication.noUse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.tandong.swichlayout.BaseAnimViewS;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

import java.util.Calendar;

/**
 * Created by charlie on 2016/3/3.
 */
public class SettingActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace{

    //Setting用的變數
    private String brokerIp;
    private String brokerPort;
    private String serverIP;
    private EditText medtIP;
    private EditText medtPort;
    private EditText medtServerIP;
    private EditText medtName;
    private EditText medtHeight;
    private EditText medtWeight;
    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String ipField = "IP";
    private static final String portField = "PORT";
    private static final String serverIPField = "SERVER_IP";
    private static final String nameField = "NAME";
    private static final String heightField = "HEIGHT";
    private static final String weightField = "WEIGHT";
    private RadioButton mrBtn_boy;
    private RadioButton mrBtn_girl;
    private RadioGroup mrGenderGroup;
    private int gender;
    private static final int GENDER_BOY = 0;
    private static final int GENDER_GIRL = 1;
    private int birthYear;
    private int birthMonth;
    private int birthDay;
    private TextView medtBirth;
    private ImageButton mbtnBirth;
    private int mYear, mMonth, mDay;

    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        setEnterSwichLayout();

        medtIP = (EditText) findViewById(R.id.edtIP);
        medtPort = (EditText) findViewById(R.id.edtPort);
        medtServerIP = (EditText) findViewById(R.id.edtServerIP);

        medtName = (EditText) findViewById(R.id.edtName);
        medtHeight = (EditText) findViewById(R.id.edtHeight);
        medtWeight = (EditText) findViewById(R.id.edtWeight);

        mrBtn_boy = (RadioButton) findViewById(R.id.rBtn_boy);
        mrBtn_girl = (RadioButton) findViewById(R.id.rBtn_girl);
        mrGenderGroup = (RadioGroup) findViewById(R.id.rGenderGroup);

        Intent intent = this.getIntent();
        timbre = intent.getStringExtra("timbre");
        speed = intent.getStringExtra("speed");
        tone = intent.getIntExtra("tone", 0);
        volume = intent.getIntExtra("volume", 0);

        iniBarComponent();  //初始化AppBar

        medtBirth = (TextView) findViewById(R.id.edtBirth);

        mbtnBirth = (ImageButton) findViewById(R.id.btnBirth);

        mbtnBirth.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        readData();

        mrGenderGroup.setOnCheckedChangeListener(listener);


        //checkNetwork();

    }

    public void showDatePickerDialog() {
        // 設定初始日期
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // 跳出日期選擇器
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // 完成選擇，顯示日期
                        medtBirth.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        mDay = dayOfMonth;
                        mMonth = monthOfYear;
                        mYear = year;
                        //Toast.makeText(SettingActivity.this, "M:" + mMonth + "D:" + dayOfMonth,Toast.LENGTH_LONG).show();
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    //儲存IP,PORT
    public void buttonClick(View view) {
        int i =view.getId();
        if(i == R.id.OKbtn){
            Log.d("BUTTON","BUTTON CLICK");
        }
        String babyName;
        String height;
        String weight;

        brokerIp = medtIP.getText().toString();
        brokerPort = medtPort.getText().toString();
        serverIP = medtServerIP.getText().toString();

        babyName = medtName.getText().toString();
        height = medtHeight.getText().toString();
        weight = medtWeight.getText().toString();

        birthDay = mDay;
        birthMonth = mMonth;
        birthYear = mYear;

        //做判斷式判斷如果沒有輸入就不行
        Intent intent = this.getIntent();//Intent new Intent
        intent.putExtra("brokerIp", brokerIp);
        intent.putExtra("brokerPort", brokerPort);
        intent.putExtra("serverIP", serverIP);

        intent.putExtra("babyName", babyName);
        intent.putExtra("height", height);
        intent.putExtra("weight", weight);

        intent.putExtra("gender", gender);
        intent.putExtra("birthYear", birthYear);
        intent.putExtra("birthMonth", birthMonth);
        intent.putExtra("birthDay", birthDay);

        setResult(RESULT_OK, intent);
        saveData();

        Toast.makeText(SettingActivity.this, "儲存成功~" + "serverIP:" + serverIP, Toast.LENGTH_LONG).show();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = this.getIntent();

        if (id == R.id.nav_main){
            setExitSwichLayout();
            this.finish();
        } else if (id == R.id.nav_music) {
            intent.setClass(SettingActivity.this, MusicActivity.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_video) {
            intent.setClass(SettingActivity.this, VideoActivity.class);
            intent.putExtra("brokerIP", brokerIp);
            intent.putExtra("brokerPort", brokerPort);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SETTING) {
                brokerIp = data.getStringExtra("brokerIp");
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
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_setting);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            this.finish();
            setExitSwichLayout();
            super.onBackPressed();
        }
    }

    //性別的按鈕
    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                case R.id.rBtn_boy:
                    gender = GENDER_BOY;
                    break;
                case R.id.rBtn_girl:
                    gender = GENDER_GIRL;
                    break;
            }
        }
    };

    public void readData(){
        settingsField = getSharedPreferences(data,0);
        medtIP.setText(settingsField.getString(ipField, ""));
        medtPort.setText(settingsField.getString(portField, ""));
        medtServerIP.setText(settingsField.getString(serverIPField, ""));
        medtName.setText(settingsField.getString(nameField, ""));
        medtHeight.setText(settingsField.getString(heightField, ""));
        medtWeight.setText(settingsField.getString(weightField, ""));
    }

    public void saveData(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(ipField, medtIP.getText().toString())
                .putString(portField, medtPort.getText().toString())
                .putString(serverIPField, medtServerIP.getText().toString())
                .putString(nameField, medtName.getText().toString())
                .putString(heightField, medtHeight.getText().toString())
                .putString(weightField, medtWeight.getText().toString())
                .commit();
    }

    //學長的function還不知道功用
    private void checkNetwork() {
        if (TurtleUtil.checkNetwork(this) == false) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(R.string.connection_require);
            ab.setTitle(android.R.string.dialog_alert_title);
            ab.setIcon(android.R.drawable.ic_dialog_alert);
            ab.setCancelable(false);
            ab.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            ab.show();
        }
    }

    //回去的按鈕
    public void backButtonClick(View view) {
        this.finish();
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
}
