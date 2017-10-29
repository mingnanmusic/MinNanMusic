package org.loader.music.application;

import org.loader.music.service.DownloadService;
import org.loader.music.service.PlayService;
import org.loader.music.utils.CrashHandler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class App extends Application {
	public static Context sContext;
	public static int sScreenWidth;
	public static int sScreenHeight;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();


//		CrashHandler crashHandler = CrashHandler.getInstance();
//		crashHandler.init(getApplicationContext());
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
//		OkHttpClient okHttpClient = new OkHttpClient.Builder()
////                .addInterceptor(new LoggerInterceptor("TAG"))
//				.connectTimeout(10000L, TimeUnit.MILLISECONDS)
//				.readTimeout(10000L, TimeUnit.MILLISECONDS)
//				//其他配置
//				.build();
//
//		OkHttpUtils.initClient(okHttpClient);
	}
	
}
