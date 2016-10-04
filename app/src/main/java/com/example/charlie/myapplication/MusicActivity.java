package com.example.charlie.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
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
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by charlie on 2016/3/6.
 */

public class MusicActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwichLayoutInterFace {

    //Setting用的變數
    private static String brokerIp;
    private String brokerPort;

    //Music用的變數
    private static int volume = 0;
    private static int tone = 0 ;  //1個8 2個8
    private static String timbre = "";
    private static String speed = "";
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
    private static String serverIp;

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
    private final Boolean INTERACTIVE = true;
    private final Boolean NORMAL = false;
    private TextView mtxtControlMusic;
    private ImageButton mbtn_playmusic;
    private ImageButton mbtn_stopmusic;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        verifyStoragePermissions(this);

        iniBarComponent();  //初始化AppBar
        intent = this.getIntent();
        serverIp = intent.getStringExtra("serverIp");
        brokerIp = intent.getStringExtra("brokerIp");

        Log.d("music", "bIP:" + brokerIp);
        Log.d("music", "sIP:" + serverIp);

        final SeekBar volume_bar = (SeekBar) findViewById(R.id.seekbar_volume);
        final SeekBar tone_bar = (SeekBar) findViewById(R.id.seekbar_tone);

        final TextView volume_value = (TextView) findViewById(R.id.value_volume);
        final TextView tone_value = (TextView) findViewById(R.id.value_tone);

        mContext = this.getApplicationContext();
        mspinner_timbre = (Spinner) findViewById(R.id.spinner_timbre);
        mspinner_speed = (Spinner) findViewById(R.id.spinner_speed);

        mtxtControlMusic = (TextView) findViewById(R.id.txtControlMusic);
        mbtn_playmusic = (ImageButton) findViewById(R.id.btn_playmusic);
        mbtn_stopmusic = (ImageButton) findViewById(R.id.btn_stopmusic);

        mbtn_stopmusic.setEnabled(false);

        final SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.switch_compat);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == INTERACTIVE){
                    Log.d("interactive","mode1");
                    Intent intent = new Intent("MUSICMODE");
                    intent.putExtra("mode", 1);
                    sendBroadcast(intent);
                    switchCompat.setText("互動模式");

                }else if(isChecked == NORMAL){
                    Log.d("normal","mode2");
                    Intent intent = new Intent("MUSICMODE");
                    intent.putExtra("mode", 0);
                    sendBroadcast(intent);
                    switchCompat.setText("普通模式");
                }
            }
        });

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
                //Toast.makeText(MusicActivity.this, "TEST timbre: " + timbrechose,Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(MusicActivity.this, "speed:" + speedchose, Toast.LENGTH_SHORT).show();
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
            intent.putExtra("serverIp", serverIp);
            intent.putExtra("brokerIp", brokerIp);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting) {
            intent.setClass(MusicActivity.this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_SETTING);
            this.onPause();
            //this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_music);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == FILE_SELECT_CODE){
                Uri uri = data.getData();
                final String uripath = ImageFilePath.getPath(this, uri);

                Log.d("uriPath", uripath);

                Log.d("buttonSend","Startsend");
                final SendFile sendFile = new SendFile();
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendFile.sendFile(uripath,serverIp);
                        } catch (InterruptedIOException e1){
                            Log.d("Thread","end");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

                th.start();

                Log.d("buttonSend","Endsend");
                Toast.makeText(this,"傳送完成^^",Toast.LENGTH_SHORT).show();

            }
        }


        if (requestCode == REQUEST_SETTING) {
            brokerIp = data.getStringExtra("brokerIp");

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
        //saveData();
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
    private static final int FILE_SELECT_CODE = 2;
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
            //Toast.makeText(this, "沒有檔案瀏覽器 是沒辦法選擇字型的", Toast.LENGTH_SHORT).show();
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

    }

    public void saveData() {
        String volTemp = Integer.toString(volume);
        String toneTemp = Integer.toString(tone);
        settingsField = getSharedPreferences(data, 0);
        settingsField.edit()
                .putString(volumeField, volTemp)
                .putString(speedField, speed)
                .putString(timbreField, timbre)
                .apply();
        settingsField.edit().putString(toneField, toneTemp).apply();

        Log.d("saveData", "volTemp: " + volTemp + "setfield: " + settingsField.getString(volumeField,""));
    }

    public void buttonPlayMusic(View view) {
        Log.d("BUTTONPLAY","playmusic");
        Intent intent = new Intent("MUSICCONTROL");
        intent.putExtra("control", 1);
        sendBroadcast(intent);

        mbtn_playmusic.setEnabled(false);
        mbtn_stopmusic.setEnabled(true);
        mtxtControlMusic.setText("播放中~");
    }

    public void buttonStopMusic(View view) {
        Log.d("BUTTONSTOP","stopmusic");
        Intent intent = new Intent("MUSICCONTROL");
        intent.putExtra("control", 0);
        sendBroadcast(intent);

        mbtn_stopmusic.setEnabled(false);
        mbtn_playmusic.setEnabled(true);
        mtxtControlMusic.setText("準備播放");
    }

    public void buttonPlaylist(View view) {

        Log.d("BUTTONPLAYLIST","playlist");
        Intent intent = new Intent("PLAYLIST");
        intent.putExtra("state",0);
        sendBroadcast(intent);

        //接收server傳回的資料
        registerReceiver(receiverPlayList, new IntentFilter("PLAYLISTBACK"));

        //test
        /*
        ArrayList<String> playList = new ArrayList<String>();
        playList.add("1st song");
        playList.add("2nd song");
        playList.add("3rd song");
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(new MyAdapter(playList));
        */
        Toast.makeText(MusicActivity.this, "歌單~~",Toast.LENGTH_SHORT).show();
    }

    private class MyAdapter extends BaseAdapter {
        private ArrayList<String> mList;
        private String[] testsong = {"abc","asdf" , "asdfasdf", "asdffasd", "sadfasd","abc","asdf" , "asdfasdf", "asdffasd", "sadfasd","abc","asdf" , "asdfasdf", "asdffasd", "sadfasd","","","","","","","","","","","",""};

        public MyAdapter(ArrayList<String> playListBack){

            Log.d("music","ma");
            mList = new ArrayList<String>();
            mList = playListBack;
            //Log.d("music","ma: " +mList.get(2));

            /*
            for(int i = 0; i < playListBack.size(); i++){
                final boolean add = mList.add(playListBack.get(i));
            }
            */
        }

        public void removeItem(){
            mList.clear();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        /*
        第一個是我們的item到哪一個位置?
        第二個是我們這個item所使用的view
        第三個是我們item的parent
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Holder holder;
            if(v == null){
                v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listview_item, null);
                holder = new Holder();
                holder.text = (TextView) v.findViewById(R.id.item_text);
                v.setTag(holder);
            } else{
                holder = (Holder) v.getTag();
            }

            holder.text.setText(mList.get(position) + "");
            v.setBackgroundColor(Color.WHITE);

            /*
            if(position == 21){
                v.setBackgroundColor(Color.RED);
                //mListView.setAdapter();
            }
            */

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Toast.makeText(MusicActivity.this,"CLICK " + position,Toast.LENGTH_SHORT).show();

                    Log.d("music","click");
                    Intent intent = new Intent("PLAYLIST");
                    intent.putExtra("state",1);
                    intent.putExtra("songname",mList.get(position));
                    sendBroadcast(intent);
                    Log.d("music","clickend");

                    //關閉歌單
                    removeItem();
                    notifyDataSetChanged();
                }
            });

            return v;
        }

        class Holder{
            TextView text;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

    }

    //make playlist
    public BroadcastReceiver receiverPlayList = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("music","receive");
            Bundle data = intent.getBundleExtra("PLAYLIST");

            ArrayList<String> playList = data.getStringArrayList("playlist");

            //Log.d("music", "song: " + playList.get(1));

            mListView = (ListView) findViewById(R.id.list);
            mListView.setAdapter(new MyAdapter(playList));

            Log.d("music","creativeview");
        }
    };

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

}
