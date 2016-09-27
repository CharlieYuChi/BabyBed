package com.example.charlie.myapplication.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlie.myapplication.R;
import com.example.charlie.myapplication.SocketService;
import com.example.charlie.myapplication.tab.BaseFragment;

/**
 * Created by charlie on 2016/4/9.
 */
public class SetConnectFragment extends BaseFragment{

    private static final String DATA_NAME = "name";

    private String title = "";

    private View v;
    public ImageButton mOkbtn;
    callBackConnect mCallbackConnect;

    private SharedPreferences settingsField;
    private static final String data = "DATA";
    private static final String ipField = "IP";
    private static final String serverIPField = "SERVER_IP";

    private EditText medtIP;
    private EditText medtServerIP;

    //用來和SettingActivity傳遞資料的接口
    public interface callBackConnect{
        void saveConnect(String bIP, String sIP);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbackConnect = (callBackConnect) context;//設定接口
    }

    public static SetConnectFragment newInstance(String title, int indicatorColor, int dividerColor, int iconResId) {
        SetConnectFragment f = new SetConnectFragment();
        f.setTitle(title);
        f.setIndicatorColor(indicatorColor);
        f.setDividerColor(dividerColor);
        //f.setIconResId(iconResId);

        //pass data
        Bundle args = new Bundle();
        args.putString(DATA_NAME, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get data
        title = getArguments().getString(DATA_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.pager_connect, container, false);

        medtIP = (EditText) v.findViewById(R.id.edtIP);
        medtServerIP = (EditText) v.findViewById(R.id.edtServerIP);

        readData();

        mOkbtn = (ImageButton) v.findViewById(R.id.OKbtn);

        //設定按鈕按下去就傳送資料給SettingActivity
        mOkbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbackConnect.saveConnect(medtIP.getText().toString(), medtServerIP.getText().toString());
                Intent startIntent = new Intent(v.getContext(), SocketService.class);
                startIntent.putExtra("serverIP", medtServerIP.getText().toString());
                Log.d("serverIP",startIntent.getStringExtra("serverIP"));
                v.getContext().startService(startIntent);
                Log.d("SetConnect", "startService executed");
                Toast.makeText(getContext(),"Save OK~",Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    public void readData(){
        settingsField = this.getActivity().getSharedPreferences(data,0);
        medtIP.setText(settingsField.getString(ipField, ""));
        medtServerIP.setText(settingsField.getString(serverIPField, ""));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
