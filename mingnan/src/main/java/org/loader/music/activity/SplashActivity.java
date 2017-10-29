package org.loader.music.activity;

import org.loader.music.R;
import org.loader.music.utils.PermissionUtils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.MobileAds;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class SplashActivity extends BaseActivity {
    private static final int REQUEST_CODE = 0x001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_layout);
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, getResources().getString(R.string.google_ad_app_id));

        requetsPermission();
        // 2s跳转到主界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                if (!TextUtils.isEmpty(getSpString("isfirst"))){
                     showPopAd();
                    putData("isfirst","yes");
                }

                finish();
            }
        }, 3000);

    }

    @Override
    public void onPublish(int progress) {

    }

    @Override
    public void onChange(int position) {

    }

    /**
     * 请求读写和定位权限
     */
    private void requetsPermission() {
        PermissionUtils.getInstance(SplashActivity.this)
                .requestPermission(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_NETWORK_STATE


                        },
                        REQUEST_CODE,
                        new PermissionUtils.PermissionCallback() {
                            @Override
                            public void permissionSuccess(int requestCode) {
                                // 开启播放和下载服务


                            }

                            @Override
                            public void permissionFail(int requestCode) {

                            }
                        });

    }

}
