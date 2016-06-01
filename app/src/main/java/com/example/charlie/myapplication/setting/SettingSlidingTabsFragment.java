package com.example.charlie.myapplication.setting;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.charlie.myapplication.R;
import com.example.charlie.myapplication.view.SlidingTabLayout;

/**
 * Created by charlie on 2016/4/9.
 */
public class SettingSlidingTabsFragment extends Fragment{

    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());



        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

    }

    class SamplePagerAdapter extends PagerAdapter {


        //控制頁面數量
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        //設定TAG標籤的名子
        @Override
        public CharSequence getPageTitle(int position) {

            String string = "";
            if(position == 0){
                string = "基本資料";
            } else if(position == 1){
                string = "連線";
            }

            return string;
        }

        //設定標籤內的頁面
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = null;

            if(position == 0){
                view = getActivity().getLayoutInflater().inflate(R.layout.pager_babyinfo,
                        container, false);

                container.addView(view);
                return view;
            } else if (position == 1){
                view = getActivity().getLayoutInflater().inflate(R.layout.pager_connect,
                        container, false);

                container.addView(view);

            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}
