package org.loader.music.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.loader.music.BuildConfig;
import org.loader.music.pojo.DatabaseHelper;
import org.loader.music.pojo.SearchResult;
import org.loader.music.utils.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Request;
import util.AESEncodeAndDecode;

/**
 * 2015年8月15日 15:54:43
 * 博文地址：http://blog.csdn.net/u010156024
 * 该类完成功能： 有URL链接解析出推荐的歌曲列表
 */
public class SongsRecommendation {
	private  String TAG =  SongsRecommendation.class.getSimpleName();
	private ArrayList<SearchResult> result=  new ArrayList<SearchResult>();
	// http://music.baidu.com/top/new/?pst=shouyeTop
	private static final String URL = Constants.MUSIC_URL
			+ "/top/new/?pst=shouyeTop";
	private static SongsRecommendation sInstance;
	private Activity activity;
	/**
	 * 回调接口，传递数据给Activity或者Fragment
	 * 非常好用的数据传递方式
	 */
	private OnRecommendationListener mListener;

	private ExecutorService mThreadPool;

	public static SongsRecommendation getInstance() {
		if (sInstance == null)
			sInstance = new SongsRecommendation();
		return sInstance;
	}

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.SUCCESS:
				if (mListener != null)
					mListener
					.onRecommend((ArrayList<SearchResult>) msg.obj);
				break;
			case Constants.FAILED:
				if (mListener != null)
					mListener.onRecommend(null);
				break;
			}
		}
	};
	
	@SuppressLint("HandlerLeak")
	private SongsRecommendation() {
		// 创建单线程池
		mThreadPool = Executors.newSingleThreadExecutor();
	}

	/**
	 * 设置回调接口OnRecommendationListener类的对象mListener
	 * 
	 * @param l
	 * @return
	 */
	public SongsRecommendation setListener(OnRecommendationListener l) {
		mListener = l;
		return this;
	}
	public  boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 真正执行网页解析的方法
	 * 线程池中开启新的线程执行解析，解析完成之后发送消息
	 * 将结果传递到主线程中
	 */
	public void get(final Activity activity) {
		this.activity  = activity;
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try{

					//判断有没有网络，没有网络，读取本地

					if (isNetworkConnected(activity)) {
						String url = Constants.MUSIC_URL+ File.separator+Constants.APP_TYPE+File.separator+Constants.APP_TYPE+"_Playlist.json";
						OkHttpUtils
								.get()
								.url(url)
								.build()
								.execute(new StringCallback() {
									@Override
									public void onError(Call call, Exception e, int id) {

									}

									@Override
									public void onResponse(String response, int id) {
										try{
											parseData(response,result);
											dealResult();
										}catch (Exception e){
											e.printStackTrace();
										}
									}
								});
					}else{
						getLocalList(result,activity);
						dealResult();
					}

					//ArrayList<SearchResult> result = getMusicList();

				}catch (Exception e){
					e.printStackTrace();
				}

			}
		});
	}
	private  void dealResult(){
		if (result == null) {
			mHandler.sendEmptyMessage(Constants.FAILED);
			return;
		}
		Log.i(TAG, "run: "+result.toString());
		mHandler.obtainMessage(Constants.SUCCESS, result)
				.sendToTarget();
	}
	private   void getLocalList(ArrayList<SearchResult> result,Activity activity) throws   Exception{
		InputStream is = activity.getAssets().open("MinnanPlaylist.json");
		int length = is.available();
		byte[]  buffer = new byte[length];
		is.read(buffer);
		String inputString = new String(buffer, "utf8");
		parseData(inputString,result);
	}
	private  void parseData(String  inputString,ArrayList<SearchResult> result) throws  Exception{
		JSONArray array  = new JSONArray(inputString);
//		Log.i(TAG, "parseData: "+inputString);
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonobject = array.getJSONObject(i);
//			Log.i(TAG, "parseData: "+jsonobject.toString());
			SearchResult searchResult  =  new SearchResult();
			searchResult.setMusicName(AESEncodeAndDecode.decrypt(jsonobject.optString("Title").replace("_", "/")).replace("|",""));
//						searchResult.setUrl("http://www.molinmusic.com/Product/app/OldSongs/mp3/"+jsonobject.optString("FileName"));
			Log.i(TAG, "parseData: "+jsonobject.optString("FileName"));

            searchResult.setApp(jsonobject.optString("App"));
			if (TextUtils.isEmpty(jsonobject.optString("App"))){
				searchResult.setUrl("http://www.molinmusic.com/Product/app/"+  BuildConfig.apptype+"/mp3/"+jsonobject.optString("FileName"));
			}else{
				searchResult.setUrl(Constants.MUSIC_URL+"/"+jsonobject.optString("App")+"/mp3/"+jsonobject.optString("FileName"));
			}
			//	searchResult.setAlbum();
//			searchResult.set
			String[]  info  =AESEncodeAndDecode.decrypt(jsonobject.optString("Title")).split("-");
			searchResult.setArtist(info[0].replace("|",""));
			searchResult.setLrc(jsonobject.optString("LRC"));
			if (searchResult.getMusicName().contains("免费下载好歌")||searchResult.getMusicName().contains("下载免费的少儿歌曲")){
				//如果是广告就不添加
			}else{
				result.add(searchResult);
				DatabaseHelper helper=DatabaseHelper.getHelper(activity);
				try {
					helper.getSearchResultDao().createOrUpdate(searchResult);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i(TAG, "parseData: "+result.toString());
	}

	private ArrayList<SearchResult> getMusicList() {
		try {



			/**
			 * 一下方法调用请参考官网
			 * 说明：timeout设置请求时间，不宜过短。
			 * 时间过短导致异常，无法获取。
			 */
			Document doc = Jsoup
					.connect(URL)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64)" +
							" AppleWebKit/537.36"
									+ " (KHTML, like Gecko)" +
									" Chrome/42.0.2311.22 Safari/537.36")
					.timeout(60 * 1000).get();
			//select为选择器，请参考官网说明
			Elements songTitles = doc.select("span.song-title");
			Elements artists = doc.select("span.author_list");
			ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();

			for (int i = 0; i < songTitles.size(); i++) {
				SearchResult searchResult = new SearchResult();
				Elements urls = songTitles.get(i).getElementsByTag("a");
				searchResult.setUrl(urls.get(0).attr("href"));
				searchResult.setMusicName(urls.get(0).text());

				Elements artistElements = artists.get(i).getElementsByTag("a");
				searchResult.setArtist(artistElements.get(0).text());
				searchResult.setAlbum("最新推荐");
				searchResults.add(searchResult);
			}
			return searchResults;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 回调接口 获取数据之后，通过该接口设置数据传递
	 */
	public interface OnRecommendationListener {
		public void onRecommend(ArrayList<SearchResult> results);
	}
}
