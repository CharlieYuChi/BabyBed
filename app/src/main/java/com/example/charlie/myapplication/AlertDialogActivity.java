package com.example.charlie.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Charlie on 2016/10/5.
 */


public class AlertDialogActivity extends Activity {



    @Override
    protected void  onCreate(Bundle savedInstanceState)
    {
        super .onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);


        TextView alert = (TextView) findViewById(R.id.alert_content);

        Intent it = this.getIntent();
        alert.setText(it.getStringExtra("text"));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("ALERT");
        intent.putExtra("alert", false);
        sendBroadcast(intent);
    }
}
