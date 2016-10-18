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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.ui.base.ToolbarActivity;
import me.drakeet.meizhi.util.RxMeizhi;
import me.drakeet.meizhi.util.Shares;
import me.drakeet.meizhi.util.Toasts;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PictureActivity extends ToolbarActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IMAGE_TITLE = "image_title";
    public static final String TRANSIT_PIC = "picture";

    @Bind(R.id.picture) ImageView mImageView;

    PhotoViewAttacher mPhotoViewAttacher;
    String mImageUrl, mImageTitle;


    @Override protected int provideContentViewId() {
        return R.layout.activity_picture;
    }


    @Override public boolean canBack() {
        return true;
    }


    public static Intent newIntent(Context context, String url, String desc) {
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra(PictureActivity.EXTRA_IMAGE_URL, url);
        intent.putExtra(PictureActivity.EXTRA_IMAGE_TITLE, desc);
        return intent;
    }


    private void parseIntent() {
        mImageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        mImageTitle = getIntent().getStringExtra(EXTRA_IMAGE_TITLE);
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        parseIntent();
        ViewCompat.setTransitionName(mImageView, TRANSIT_PIC); // 转场共享元素
        Picasso.with(this).load(mImageUrl).into(mImageView);  //加载图片,由于这个url是在之前下好的，所以直接加载好了
        setAppBarAlpha(0.7f);   //设置AppBar的透明度
        setTitle(mImageTitle); // 设置title 这个title太长了会自动变短，好智能
        setupPhotoAttacher();
    }


    private void setupPhotoAttacher() {
        // 图片缩放，双击 效果
        mPhotoViewAttacher = new PhotoViewAttacher(mImageView);
        // 图片单击 开关toolbar
        mPhotoViewAttacher.setOnViewTapListener((view, v, v1) -> hideOrShowToolbar());
        // @formatter:off
        mPhotoViewAttacher.setOnLongClickListener(v -> {   //长按 弹出是否保存到手机的对话框
            new AlertDialog.Builder(PictureActivity.this)
                    .setMessage(getString(R.string.ask_saving_picture))
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(android.R.string.ok,
                            (dialog, which) -> {
                                saveImageToGallery();
                                dialog.dismiss();
                            })
                    .show();
            // @formatter:on
            return true;
        });
    }


    //-- 保存图片，并弹出保存成功或者失败的对话框
    private void saveImageToGallery() {
        // @formatter:off
        Subscription s = RxMeizhi.saveImageAndGetPathObservable(this, mImageUrl, mImageTitle)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(uri -> {
                File appDir = new File(Environment.getExternalStorageDirectory(), "Meizhi");
                String msg = String.format(getString(R.string.picture_has_save_to),
                        appDir.getAbsolutePath());
                Toasts.showShort(msg);
            }, error -> Toasts.showLong(error.getMessage() + "\n再试试..."));
        // @formatter:on
        addSubscription(s);
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture, menu);
        // TODO: 把图片的一些信息，比如 who，加载到 Overflow 当中
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                RxMeizhi.saveImageAndGetPathObservable(this, mImageUrl, mImageTitle)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(uri -> Shares.shareImage(this, uri,
                                getString(R.string.share_meizhi_to)),
                                error -> Toasts.showLong(error.getMessage()));
                return true;
            case R.id.action_save:
                saveImageToGallery();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        mPhotoViewAttacher.cleanup();
        ButterKnife.unbind(this);
    }
}
