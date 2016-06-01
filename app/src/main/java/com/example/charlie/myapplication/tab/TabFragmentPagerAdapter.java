package com.example.charlie.myapplication.tab;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.LinkedList;

/**
 * Created by charlie on 2016/4/9.
 */
/*
    裝Fragment裡的一堆分頁
 */


public class TabFragmentPagerAdapter extends FragmentPagerAdapter{

    LinkedList<BaseFragment> fragments = null;

    public TabFragmentPagerAdapter(FragmentManager fm, LinkedList<BaseFragment> fragments) {
        super(fm);
        if (fragments == null) {
            this.fragments = new LinkedList<BaseFragment>();
        }else{
            this.fragments = fragments;
        }
    }

    //第幾分頁
    @Override
    public BaseFragment getItem(int position) {
        return fragments.get(position);
    }

    //分頁數量
    @Override
    public int getCount() {
        return fragments.size();
    }

    //取得Tags名子
    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }
}
