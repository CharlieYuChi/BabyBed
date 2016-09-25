package com.example.charlie.myapplication.setting;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlie.myapplication.R;
import com.example.charlie.myapplication.tab.BaseFragment;

import java.util.Calendar;

/**
 * Created by charlie on 2016/4/9.
 */
public class SetBabyInfoFragment extends BaseFragment {

    private static final String DATA_NAME = "name";

    private String title = "";

    public ImageButton mOkbtn;
    callBackBaby mCallbackBaby;

    //姓名,身高,體重
    private EditText medtName;
    private EditText medtHeight;
    private EditText medtWeight;

    //性別變數
    private RadioButton mrBtn_boy;
    private RadioButton mrBtn_girl;
    private RadioGroup mrGenderGroup;
    private int gender;
    private static final int GENDER_BOY = 0;
    private static final int GENDER_GIRL = 1;

    //生日變數
    private static int birthYear;
    private static int birthMonth;
    private static int birthDay;
    private TextView medtBirth;
    private ImageButton mbtnBirth;
    private static int mYear, mMonth, mDay;

    private SharedPreferences settingsField;
    private static final String datashared = "DATA";
    private static final String nameField = "NAME";
    private static final String heightField = "HEIGHT";
    private static final String weightField = "WEIGHT";
    private static final String genderField = "GENDER";
    private static final String birthYearField = "YEAR";
    private static final String birthMonthField = "MONTH";
    private static final String birthDayField = "DAY";

    //傳給SettingActivity的資料
    Bundle data;


    //用來和SettingActivity傳遞資料的接口
    public interface callBackBaby{
        void saveBaby(Bundle data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbackBaby = (callBackBaby) context;  //設定接口
    }

    public static SetBabyInfoFragment newInstance(String title, int indicatorColor, int dividerColor, int iconResId) {

        SetBabyInfoFragment f = new SetBabyInfoFragment();
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

        data = new Bundle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pager_babyinfo, container, false);


        medtName = (EditText) view.findViewById(R.id.edtName);
        medtHeight = (EditText) view.findViewById(R.id.edtHeight);
        medtWeight = (EditText) view.findViewById(R.id.edtWeight);

        mrGenderGroup = (RadioGroup) view.findViewById(R.id.rGenderGroup);
        mrBtn_boy = (RadioButton) view.findViewById(R.id.rBtn_boy);
        mrBtn_girl = (RadioButton) view.findViewById(R.id.rBtn_girl);

        medtBirth = (TextView) view.findViewById(R.id.edtBirth);

        mbtnBirth = (ImageButton) view.findViewById(R.id.btnBirth);

        mbtnBirth.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        //性別的按鈕
        mrGenderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                Log.d("check ", "HI");
                switch (checkedId) {
                    case R.id.rBtn_boy:
                        gender = GENDER_BOY;
                        //Toast.makeText(getActivity(), "" + gender,Toast.LENGTH_LONG).show();
                        break;
                    case R.id.rBtn_girl:
                        gender = GENDER_GIRL;
                        //Toast.makeText(getContext(), "" + gender,Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        readData();
        mOkbtn = (ImageButton) view.findViewById(R.id.OKbtn);

        //設定按鈕按下去就傳送資料給SettingActivity
        mOkbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("gender:", "" + gender);
                data.putString("name", medtName.getText().toString());
                data.putString("height", medtHeight.getText().toString());
                data.putString("weight", medtWeight.getText().toString());
                data.putInt("gender", gender);
                data.putInt("year", birthYear);
                data.putInt("month", birthMonth);
                data.putInt("day", birthDay);

                mCallbackBaby.saveBaby(data);

                Toast.makeText(getContext(),"Save OK~",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    public void showDatePickerDialog() {
        // 設定初始日期
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        // 跳出日期選擇器
        DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // 完成選擇，顯示日期
                        medtBirth.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        mDay = dayOfMonth;
                        mMonth = monthOfYear;
                        mYear = year;

                        birthYear = mYear;
                        birthMonth = mMonth;
                        birthDay = mDay;
                        //Toast.makeText(SettingActivity.this, "M:" + mMonth + "D:" + dayOfMonth,Toast.LENGTH_LONG).show();
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }




    public void readData(){
        settingsField = this.getActivity().getSharedPreferences(datashared,0);
        medtName.setText(settingsField.getString(nameField, ""));
        medtHeight.setText(settingsField.getString(heightField, ""));
        medtWeight.setText(settingsField.getString(weightField, ""));
        if(settingsField.getInt(genderField,0) == 0){
            mrGenderGroup.check(R.id.rBtn_boy);
        } else {
            mrGenderGroup.check(R.id.rBtn_girl);
        }
        medtBirth.setText(settingsField.getInt(birthYearField, 0) + "-"
                + (settingsField.getInt(birthMonthField, 0) + 1) + "-"
                + settingsField.getInt(birthDayField, 0));

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
