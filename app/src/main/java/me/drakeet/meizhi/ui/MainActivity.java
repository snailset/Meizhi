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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.meizhi.App;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.MeizhiData;
import me.drakeet.meizhi.data.entity.Gank;
import me.drakeet.meizhi.data.entity.Meizhi;
import me.drakeet.meizhi.data.休息视频Data;
import me.drakeet.meizhi.func.OnMeizhiTouchListener;
import me.drakeet.meizhi.ui.adapter.MeizhiListAdapter;
import me.drakeet.meizhi.ui.base.SwipeRefreshBaseActivity;
import me.drakeet.meizhi.util.AlarmManagers;
import me.drakeet.meizhi.util.Dates;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.PreferencesLoader;
import me.drakeet.meizhi.util.Toasts;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends SwipeRefreshBaseActivity {

    private static final int PRELOAD_SIZE = 6;

    @Bind(R.id.rv_meizhi) RecyclerView mRecyclerView;

    private MeizhiListAdapter mMeizhiListAdapter;
    private List<Meizhi> mMeizhiList;
    private boolean mIsFirstTimeTouchBottom = true;
    private int mPage = 1;
    private boolean mMeizhiBeTouched;


    @Override protected int provideContentViewId() {
        return R.layout.activity_main;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        System.out.println("============= onCreate =================");
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mMeizhiList = new ArrayList<>();
        QueryBuilder query = new QueryBuilder(Meizhi.class);
        query.appendOrderDescBy("publishedAt");
        query.limit(0, 10);
        mMeizhiList.addAll(App.sDb.query(query));

//        System.out.println("-------------------------------------------");
//        System.out.println("mMeizhiList.size() = " + mMeizhiList.size());
//        for ( Meizhi meizhi : mMeizhiList ) {
//            System.out.println(meizhi);
//        }
//        System.out.println("--------------------------------------------");

        // 设置RecyclerView()
        setupRecyclerView();

        // 设置友盟自动更新
        setupUmeng();

        // 使用系统闹钟服务，如果打开应用，当天12:24:38 会广播，然后通知栏会提醒
        AlarmManagers.register(this);

        System.out.println("============= onCreate end =================");
    }

    //在onCreate()之后执行
    @Override protected void onPostCreate(Bundle savedInstanceState) {
        System.out.println("============= onPostCreate =================");
        super.onPostCreate(savedInstanceState);
        new Handler().postDelayed(() -> setRefresh(true), 358);
//        setRefresh(true);  //-- 在measure之前用这个可能不显示进度条，是官方的bug，所以要延迟再调用，等执行完measure
        loadData(true);
    }


    private void setupUmeng() {
        //友盟 自动更新， 友盟已停止维护
        UmengUpdateAgent.update(this);   // 自动更新
        UmengUpdateAgent.setDeltaUpdate(false); //关闭 增量更新
        UmengUpdateAgent.setUpdateOnlyWifi(false); // 关闭 仅wifi更新
    }


    private void setupRecyclerView() {
//        Log.v("msg", "++++++++++++++++++++++++++++++++++");
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mMeizhiListAdapter = new MeizhiListAdapter(this, mMeizhiList);
        mRecyclerView.setAdapter(mMeizhiListAdapter);
        new Once(this).show("tip_guide_6", () -> {
            //material design库 对话框, 可以取代Toast
            Snackbar.make(mRecyclerView, getString(R.string.tip_guide), Snackbar.LENGTH_INDEFINITE)  //在mRecyclerView的父容器中显示SnackBar
                    .setAction(R.string.i_know, v -> {  //点击后回调
                    })
                    .show();
        });

        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));    // RecyclerView滑动监听
        mMeizhiListAdapter.setOnMeizhiTouchListener(getOnMeizhiTouchListener());  //点击RecyclerView的Item
    }


    /**
     * 获取服务数据
     *
     * @param clean 清除来自数据库缓存或者已有数据。
     */
    private void loadData(boolean clean) {
        mLastVideoIndex = 0;
        // @formatter:off
        Subscription s = Observable
               .zip(sGankIO.getMeizhiData(mPage),
                     sGankIO.get休息视频Data(mPage),
                     this::createMeizhiDataWith休息视频Desc)  // T: meizhiData
               .map(meizhiData -> meizhiData.results)  // T: List<Meizhi>
               .flatMap(Observable::from)            //T: Meizhi
               .toSortedList((meizhi1, meizhi2) ->   // T: List<Meizhi>
                     meizhi2.publishedAt.compareTo(meizhi1.publishedAt))
               .doOnNext(this::saveMeizhis)         // 将List<Meizhi>存放到数据库中
               .observeOn(AndroidSchedulers.mainThread()) //表示观察者执行在主线程中
               .finallyDo(() -> setRefresh(false))  //最后要做的事情
               .subscribe(meizhis -> {              //在Android主线程中执行
                   if (clean) mMeizhiList.clear();  //如果选择clean，将以前的数据清除，说明加载的全是从网上获取来的数据
                   mMeizhiList.addAll(meizhis);
                   mMeizhiListAdapter.notifyDataSetChanged();
                   setRefresh(false);
               }, throwable -> loadError(throwable));
        // @formatter:on
        addSubscription(s);
    }

    // 加载错误重新加载
    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
        Snackbar.make(mRecyclerView, R.string.snap_load_fail, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> {
                    requestDataRefresh();    //点击重试按钮
                })
                .show();
    }

    // 将 Meizhi数据 插入数据库
    private void saveMeizhis(List<Meizhi> meizhis) {
        App.sDb.insert(meizhis, ConflictAlgorithm.Replace);   // 插入失败,怪不得前面读不出数据呢
    }

    // 在meizhi.desc后面追加了对应data.desc
    private MeizhiData createMeizhiDataWith休息视频Desc(MeizhiData data, 休息视频Data love) {
        for (Meizhi meizhi : data.results) {
            meizhi.desc = meizhi.desc + " " +
                    getFirstVideoDesc(meizhi.publishedAt, love.results);
        }
        return data;
    }


    private int mLastVideoIndex = 0;


    private String getFirstVideoDesc(Date publishedAt, List<Gank> results) {
        String videoDesc = "";
        for (int i = mLastVideoIndex; i < results.size(); i++) {
            Gank video = results.get(i);
            if (video.publishedAt == null) video.publishedAt = video.createdAt;
            if (Dates.isTheSameDay(publishedAt, video.publishedAt)) {
                videoDesc = video.desc;
                mLastVideoIndex = i;
                break;
            }
        }
        return videoDesc;
    }


    private void loadData() {
        loadData(/* clean */false);
    }


    private OnMeizhiTouchListener getOnMeizhiTouchListener() {
        // 当点击RecyclerView被调用
        return (v, meizhiView, card, meizhi) -> {
            if (meizhi == null) return;
            // 点击到的View与meizhiView一致，说明点击到RecyclerView的图片上了
            // 此时加载大图 mMeizhiBeTouched标志可以限制一次只能加载一张图片
            if (v == meizhiView && !mMeizhiBeTouched) {
                mMeizhiBeTouched = true;
                Picasso.with(this).load(meizhi.url).fetch(new Callback() {

                    @Override public void onSuccess() {
                        mMeizhiBeTouched = false;
                        startPictureActivity(meizhi, meizhiView); // 显示大图
                    }


                    @Override public void onError() {mMeizhiBeTouched = false;}
                });
            } else if (v == card) { // 点击到RecyclerView上的title上
                startGankActivity(meizhi.publishedAt); //  显示Gank界面
            }
        };
    }


    private void startGankActivity(Date publishedAt) {
        Intent intent = new Intent(this, GankActivity.class);
        intent.putExtra(GankActivity.EXTRA_GANK_DATE, publishedAt);
        startActivity(intent);
    }


    private void startPictureActivity(Meizhi meizhi, View transitView) {
        Intent intent = PictureActivity.newIntent(MainActivity.this, meizhi.url, meizhi.desc);
        // 共享元素Transition效果
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                MainActivity.this, transitView, PictureActivity.TRANSIT_PIC);
        try {   // 为什么 这里明明没有异常，ActivityCompat.startActivity已经兼容了版本
            ActivityCompat.startActivity(MainActivity.this, intent, optionsCompat.toBundle());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            startActivity(intent);
        }
    }

    // 点击Toolbar 返回顶部
    @Override public void onToolbarClick() {mRecyclerView.smoothScrollToPosition(0);}


    @OnClick(R.id.main_fab) public void onFab(View v) {
        if (mMeizhiList != null && mMeizhiList.size() > 0) {
            startGankActivity(mMeizhiList.get(0).publishedAt);
        }
    }


    //刷新数据， 下拉刷新
    @Override public void requestDataRefresh() {
        super.requestDataRefresh();
        mPage = 1;
        loadData(true);
    }


    private void openGitHubTrending() {
        String url = getString(R.string.url_github_trending);
        String title = getString(R.string.action_github_trending);
        Intent intent = WebActivity.newIntent(this, url, title);
        startActivity(intent);
    }


    //如果没有重写这个方法，Toolbar将会不显示菜单
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_notifiable);
        initNotifiableItemState(item);
        return true;
    }


    private void initNotifiableItemState(MenuItem item) {
        PreferencesLoader loader = new PreferencesLoader(this);
        item.setChecked(loader.getBoolean(R.string.action_notifiable, true));
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_trending:
                openGitHubTrending();
                return true;
            case R.id.action_notifiable:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                PreferencesLoader loader = new PreferencesLoader(this);
                loader.saveBoolean(R.string.action_notifiable, isChecked);
                Toasts.showShort(isChecked ? R.string.notifiable_on : R.string.notifiable_off);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    RecyclerView.OnScrollListener getOnBottomListener(StaggeredGridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {

                // findLastCompletelyVisibleItemPositions 返回每一个span(因为是瀑布流嘛)当前可见的最大position
                // isBottom并不是到底了，而是说都了最后几个的时候，最后的大小可以通过PRELOAD_SIZE调节
                boolean isBottom =
                        layoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] >=
                                mMeizhiListAdapter.getItemCount() - PRELOAD_SIZE;
                // 如果不是正在刷新 并且 到了底了，就可开始加载下一页的数据了
                if (!mSwipeRefreshLayout.isRefreshing() && isBottom) { //利用是否显示了进度条，来判断是否正在刷新
                    if (!mIsFirstTimeTouchBottom) {     // 这里是为什么
                        mSwipeRefreshLayout.setRefreshing(true);
                        mPage += 1;
                        loadData();
                    } else {
                        mIsFirstTimeTouchBottom = false;
                    }
                }
            }
        };
    }


    @Override public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }


    @Override public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
