/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.drakeet.meizhi.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.umeng.analytics.MobclickAgent;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.meizhi.LoveBus;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.event.OnKeyBackClickEvent;
import me.drakeet.meizhi.ui.adapter.GankPagerAdapter;
import me.drakeet.meizhi.ui.base.ToolbarActivity;
import me.drakeet.meizhi.util.Dates;

public class GankActivity extends ToolbarActivity implements ViewPager.OnPageChangeListener {

    public static final String EXTRA_GANK_DATE = "gank_date";

    @Bind(R.id.pager) ViewPager mViewPager;
    @Bind(R.id.tabLayout) TabLayout mTabLayout;

    GankPagerAdapter mPagerAdapter;
    Date mGankDate;


    @Override protected int provideContentViewId() {
        return R.layout.activity_gank;
    }


    @Override public boolean canBack() {
        return true;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mGankDate = (Date) getIntent().getSerializableExtra(EXTRA_GANK_DATE);
        setTitle(Dates.toDate(mGankDate));
        initViewPager();
        initTabLayout();
    }


    //初始化ViewPager
    private void initViewPager() {
        mPagerAdapter = new GankPagerAdapter(getSupportFragmentManager(), mGankDate);
        mViewPager.setAdapter(mPagerAdapter);
        // 当前页 左右1页会被缓存下来，其他的将会从adapter中recreate
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.addOnPageChangeListener(this);
    }


    //初始化TabLayout, 将TabLayout与ViewPager绑定起来
    private void initTabLayout() {
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            mTabLayout.addTab(mTabLayout.newTab());
        }
        mTabLayout.setupWithViewPager(mViewPager);
    }

    // 监听屏幕的改变
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {  // 当为横屏时
            hideOrShowToolbar();
        } else {
            hideOrShowToolbar();
        }
    }


    @Override protected void hideOrShowToolbar() {
        super.hideOrShowToolbar();
        View toolbar = findViewById(R.id.toolbar_with_indicator);
        assert toolbar != null;
        // 一个view可以动画效果
        toolbar.animate()
               .translationY(mIsHidden ? 0 : -mToolbar.getHeight())
               .setInterpolator(new DecelerateInterpolator(2))
               .start();
        mIsHidden = !mIsHidden;
        if (mIsHidden) {
            // 一个view的tag可以存储信息
            mViewPager.setTag(mViewPager.getPaddingTop());
            mViewPager.setPadding(0, 0, 0, 0);
        } else {
            mViewPager.setPadding(0, (int) mViewPager.getTag(), 0, 0);
            mViewPager.setTag(null);
        }
    }


    // otto框架，两个类之间的通信（采用事件的方式）
    // OnKeyBackClickEvent这里是一个空类，当然也可以放数据，那样的话就可以实现通信了，低耦合
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) {
                    LoveBus.getLovelySeat().post(new OnKeyBackClickEvent());
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gank, menu);
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        LoveBus.getLovelySeat().register(this);
    }


    @Override public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        LoveBus.getLovelySeat().unregister(this);
    }


    @Override protected void onDestroy() {
        mViewPager.removeOnPageChangeListener(this);
        super.onDestroy();
        ButterKnife.unbind(this);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override public void onPageSelected(int position) {
        //改变标题
        setTitle(Dates.toDate(mGankDate, -position));
    }


    @Override public void onPageScrollStateChanged(int state) {

    }
}
