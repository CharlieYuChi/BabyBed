package com.example.charlie.myapplication.tab;

import android.support.v4.app.Fragment;
import android.graphics.Color;

/**
 * Created by charlie on 2016/4/9.
 */

/*
用來給新的Fragment繼承，方便更改顏色和設定標題
 */


public class BaseFragment extends Fragment {

    private String title = "";
    private int indicatorColor = Color.BLUE;
    private int dividerColor = Color.GRAY;

    //標題
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    //標題底下那一條的顏色
    public int getIndicatorColor() {
        return indicatorColor;
    }
    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
    }

    //分隔線的顏色
    public int getDividerColor() {
        return dividerColor;
    }
    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
    }
}
