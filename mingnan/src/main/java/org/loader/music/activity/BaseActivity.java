package org.loader.music.activity;

import org.loader.music.R;
import org.loader.music.service.DownloadService;
import org.loader.music.service.MusicIntentReceiver;
import org.loader.music.service.PlayService;
import org.loader.music.utils.L;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public abstract class BaseActivity extends FragmentActivity {
    protected PlayService mPlayService;
    protected DownloadService mDownloadService;
    private InterstitialAd mInterstitialAd;
    private final String TAG = BaseActivity.class.getSimpleName();
    private Handler handler = new Handler();

    public String getSpString(String key){
        SharedPreferences sp = getApplication().getSharedPreferences("config",
                Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public  void putData(String key ,String value){
        SharedPreferences sp = getApplication().getSharedPreferences("config",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,value);
        editor.apply();
    }
    class ShowRateDialog extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

//            if (intent.getAction().equals("com.showDialog")){
//                AlertDialog alertDialog  = new AlertDialog.Builder(BaseActivity.this)
//                        .setMessage("已经下载了五首歌，是否可以好评，好评后请发送邮件给我，可以减少广告")
//                        .setPositiveButton("去好评", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Uri uri = Uri.parse("market://details?id=" + BaseActivity.this.getPackageName());
//                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
//                                try {
//                                    startActivity(goToMarket);
//                                } catch (ActivityNotFoundException e) {
//                                    Toast.makeText(BaseActivity.this, "Couldn't launch the market !", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }).create();
//                alertDialog.show();
//            }

        }
    }

    private AdView mAdView;
    protected ExecutorService executorService  = Executors.newFixedThreadPool(5);
    public void loadBannerAd(final View view){
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) view.findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                loadBannerAd(view);
            }
        });

//        AdRequest request = new AdRequest.Builder()
//                .addTestDevice("33BE2250B43518CCDA7DE426D04EE232")
//                .build();
        if (!mAdView.isShown()){
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    protected void initPopAD() {
        //add pop ad
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.pop_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadNewPopAd();
            }
        });
    }

    public void loadNewPopAd() {
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    public void showPopAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showPopAd();
                }
            }, 2000);
        }
    }

    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.l(TAG, "play--->onServiceDisconnected");
            mPlayService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayService = ((PlayService.PlayBinder) service).getService();
            mPlayService.setOnMusicEventListener(mMusicEventListener);
            onChange(mPlayService.getPlayingPosition());
        }
    };

    private ServiceConnection mDownloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.l(TAG, "download--->onServiceDisconnected");
            mDownloadService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadService = ((DownloadService.DownloadBinder) service).getService();
        }
    };

    /**
     * 音乐播放服务回调接口的实现类
     */
    private PlayService.OnMusicEventListener mMusicEventListener =
            new PlayService.OnMusicEventListener() {
                @Override
                public void onPublish(int progress) {
                    BaseActivity.this.onPublish(progress);
                }

                @Override
                public void onChange(int position) {
                    BaseActivity.this.onChange(position);
                }
            };

    /**
     * Fragment的view加载完成后回调
     * <p>
     * 注意：
     * allowBindService()使用绑定的方式启动歌曲播放的服务
     * allowUnbindService()方法解除绑定
     * <p>
     * 在SplashActivity.java中使用startService()方法启动过该音乐播放服务了
     * 那么大家需要注意的事，该服务不会因为调用allowUnbindService()方法解除绑定
     * 而停止。
     */
    public void allowBindService() {
        getApplicationContext().bindService(new Intent(this, PlayService.class),
                mPlayServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * fragment的view消失后回调
     */
    public void allowUnbindService() {
        getApplicationContext().unbindService(mPlayServiceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定下载服务
        bindService(new Intent(this, DownloadService.class),
                mDownloadServiceConnection,
                Context.BIND_AUTO_CREATE);
        initPopAD();
        loadNewPopAd();
        //注册广播
        intentReceiver = new MusicIntentReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        registerReceiver(intentReceiver, intentFilter);
        registMediaKey(MusicIntentReceiver.class.getName());

        showRateDialog =  new ShowRateDialog();
        showRateIntentfilter   = new IntentFilter("com.showDialog");
        registerReceiver(showRateDialog,showRateIntentfilter);
    }
    private    ShowRateDialog  showRateDialog;
    private  IntentFilter   showRateIntentfilter;

    public AudioManager audioManager;
    public ComponentName mRemoteControlClientReceiverComponent;
    ;

    public void registMediaKey(String className) {
        //获取音频服务
        audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
//注册接收的Receiver
        ComponentName mRemoteControlClientReceiverComponent;
        mRemoteControlClientReceiverComponent = new ComponentName(
                getPackageName(), className);
//注册MediaButton
        audioManager.registerMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
    }

    private MusicIntentReceiver intentReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (executorService!=null){
            executorService.shutdown();
        }
        if (mRemoteControlClientReceiverComponent != null) {
            //取消注册
            audioManager.unregisterMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
        }
        if (showRateDialog!=null){
            unregisterReceiver(showRateDialog);
        }
        //取消广播
        if (intentReceiver != null) {
            unregisterReceiver(intentReceiver);
        }
        unbindService(mDownloadServiceConnection);


        super.onDestroy();
    }

    public DownloadService getDownloadService() {
        return mDownloadService;
    }

    /**
     * 更新进度
     * 抽象方法由子类实现
     * 实现service与主界面通信
     *
     * @param progress 进度
     */
    public abstract void onPublish(int progress);

    /**
     * 切换歌曲
     * 抽象方法由子类实现
     * 实现service与主界面通信
     *
     * @param position 歌曲在list中的位置
     */
    public abstract void onChange(int position);



    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }


}
