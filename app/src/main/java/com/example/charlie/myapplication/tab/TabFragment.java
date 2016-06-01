package com.example.charlie.myapplication.tab;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.charlie.myapplication.R;
import com.example.charlie.myapplication.setting.SetBabyInfoFragment;
import com.example.charlie.myapplication.setting.SetConnectFragment;
import com.example.charlie.myapplication.view.SlidingTabLayout;

import java.util.LinkedList;

/**
 * Created by charlie on 2016/4/9.
 */

/*
裝Fragment的Tags和裡面的分頁
 */

public class TabFragment extends Fragment{

    private SlidingTabLayout tabs;
    private ViewPager pager;
    private FragmentPagerAdapter adapter;

    public static Fragment newInstance(){
        TabFragment f = new TabFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //adapter
        final LinkedList<BaseFragment> fragments = getFragments();
        adapter = new TabFragmentPagerAdapter(getFragmentManager(), fragments);

        //pager
        pager = (ViewPager) view.findViewById(R.id.viewpager);
        pager.setAdapter(adapter);
        //tabs
        tabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return fragments.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return fragments.get(position).getDividerColor();
            }
        });
        tabs.setBackgroundResource(R.color.colorPrimary);
        tabs.setDistributeEvenly(true);
        tabs.setViewPager(pager);

    }

    private LinkedList<BaseFragment> getFragments(){
        int indicatorColor = R.color.blueGround;
        int dividerColor = R.color.blue;

        LinkedList<BaseFragment> fragments = new LinkedList<BaseFragment>();
        fragments.add(SetBabyInfoFragment.newInstance("基本資料", indicatorColor, dividerColor,0));
        fragments.add(SetConnectFragment.newInstance("連線", indicatorColor, dividerColor,0));
        return fragments;
    }
}
