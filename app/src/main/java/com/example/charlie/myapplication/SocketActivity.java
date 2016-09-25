package com.example.charlie.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import android.os.Message;
import android.os.Handler;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.charlie.myapplication.setting.*;
import com.tandong.swichlayout.BaseAnimViewS;
import com.tandong.swichlayout.BaseEffects;
import com.tandong.swichlayout.SwichLayoutInterFace;
import com.tandong.swichlayout.SwitchLayout;

/**
 * Created by charlie on 2016/3/16.
 */
public class SocketActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace {

    //Setting用的變數
    private String brokerIp;
    private String brokerPort;

    //Music用的變數
    private int volume;
    private int tone;
    private String timbre;
    private String speed;

    //setting的intent要用的
    private static final int REQUEST_SETTING = 0;

    //music的intent要用的
    private  static final int REQUEST_MUSIC = 1;

    private OutputStream os;
    private Handler handler;
    private EditText input, show;
    private Button sendBtn;
    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String serverIPField = "";
    private String serverIP = "";
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_socket);
        input = (EditText) findViewById(R.id.main_et_input);
        show = (EditText) findViewById(R.id.main_et_show);
        sendBtn = (Button) findViewById(R.id.main_btn_send);

        setEnterSwichLayout();

        intent = this.getIntent();  //一定要加在onCreate裡,放到外面會壞掉

        serverIP = intent.getStringExtra("serverIP");

        if(serverIP != null){
            saveData();
        }


        readData();

        Toast.makeText(SocketActivity.this, "SCserverIP:" + serverIP, Toast.LENGTH_LONG).show();


        timbre = intent.getStringExtra("timbre");
        speed = intent.getStringExtra("speed");
        tone = intent.getIntExtra("tone", 0);
        volume = intent.getIntExtra("volume", 0);


        show.append("\n" + "Socket speed:" + speed);
        show.append("\n" + "Socket timbre:" + timbre);
        show.append("\n" + "Socket tone:" + tone);
        show.append("\n" + "Socket volume:" + volume);

        iniBarComponent();  //初始化AppBar

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 如果消息来自子线程
                if (msg.what == 0x234) {
                    // 将读取的内容追加显示在文本框中
                    show.append("\n" + msg.obj.toString());
                }
            }
        };

        new Thread() {
            public void run() {
                Socket socket;
                try {
                    socket = new Socket(serverIP, 8080);
                    //socket = new Socket("140.115.204.92", 8080);
                    //socket = new Socket("192.168.0.101",8080);
                    // 客户端启动ClientThread线程不断读取来自服务器的数据
                    new Thread(new ClientThread(socket, handler)).start();
                    os = socket.getOutputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();

        sendBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // 将用户在文本框内输入的内容写入网络
                    os.write((input.getText().toString() + "\r\n").getBytes());
                    // 清空input文本框数据
                    input.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //初始化AppBar
    public void iniBarComponent(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_socket);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_socket);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_socket);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = this.getIntent();

        if (id == R.id.nav_main){
            //MainActivity.serverIp = serverIP;
            //Toast.makeText(this, "socketPutserver:" + MainActivity.serverIP,Toast.LENGTH_LONG).show();

            setExitSwichLayout();
            this.finish();

        } else if (id == R.id.nav_music) {
            intent.setClass(SocketActivity.this, MusicActivity.class);
            startActivityForResult(intent, REQUEST_MUSIC);
            this.finish();
        } else if (id == R.id.nav_video) {
            intent.setClass(SocketActivity.this, VideoActivity.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting){
            intent.setClass(SocketActivity.this, com.example.charlie.myapplication.setting.SettingActivity.class);

            startActivityForResult(intent, REQUEST_SETTING);
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_socket);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SETTING) {
                serverIP = data.getStringExtra("serverIP");
                //Toast.makeText(SocketActivity.this, "REQUESTserverIP:" + serverIP, Toast.LENGTH_LONG).show();
                saveData();
            } else if (requestCode == REQUEST_MUSIC) {
                volume = data.getIntExtra("volume", 0);
                tone = data.getIntExtra("tone", 0);
                timbre = data.getStringExtra("timbre");
                speed = data.getStringExtra("speed");
                serverIP = data.getStringExtra("serverIP");
            }
        }

    }

    @Override
    protected void onDestroy() {
        setExitSwichLayout();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_socket);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            setExitSwichLayout();
            this.finish();
            super.onBackPressed();
        }
    }

    public void readData(){
        settingsField = getSharedPreferences(data,0);
        serverIP = settingsField.getString(serverIPField, "");
    }

    public void saveData(){
        settingsField = getSharedPreferences(data,0);
        settingsField.edit()
                .putString(serverIPField, serverIP.toString())
                .commit();
    }


    @Override
    public void setEnterSwichLayout() {
        SwitchLayout.animDuration = 1700;
        SwitchLayout.longAnimDuration = 1700;
        BaseAnimViewS.animDuration = 1700;
        BaseAnimViewS.longAnimDuration = 1700;
        SwitchLayout.getSlideFromLeft(this, false, BaseEffects.getMoreSlowEffect());
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
