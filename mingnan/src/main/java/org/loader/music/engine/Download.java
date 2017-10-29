package org.loader.music.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.loader.music.R;
import org.loader.music.application.App;
import org.loader.music.fragment.NetSearchFragment;
import org.loader.music.utils.Constants;
import org.loader.music.utils.L;
import org.loader.music.utils.MusicUtils;
import org.loader.music.utils.SpUtils;

import android.annotation.SuppressLint;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * 支持断点下载
 * 支持回调进度、完成、手动关闭、下载失败、暂停/继续、取得文件大小, 获取文件名
 * 支持设置同时下载线程个数
 * 还需要优化
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class Download implements Serializable {
	private static final long serialVersionUID = 0x00001000L;
	private static final int START = 1;					// 开始下载
	private static final int PUBLISH = 2;				// 更新进度
	private static final int PAUSE = 3;					// 暂停下载
	private static final int CANCEL = 4;				// 取消下载
	private static final int ERROR = 5;					// 下载错误
	private static final int SUCCESS = 6;				// 下载成功
	private static final int GOON = 7;  				// 继续下载
	
	private static final String UA = "Mozilla/5.0 (Windows NT 6.1; WOW64)" +
			" AppleWebKit/537.36 (KHTML, like Gecko)" +
			" Chrome/37.0.2041.4 Safari/537.36";
	
	private static ExecutorService mThreadPool;// 线程池
	
	static {
		mThreadPool = Executors.newFixedThreadPool(5);  // 默认5个 
	}
		
	private int mDownloadId;							// 下载id
	private String mFileName; 							// 本地保存文件名
	private String mUrl; 								// 下载地址
	private String mLocalPath;							// 本地存放目录

	private boolean isPause = false; 					// 是否暂停
	private boolean isCanceled = false;					// 是否手动停止下载
	
	private OnDownloadListener mListener;		// 监听器
	
	/**
	 * 配置下载线程池的大小
	 * @param maxSize 同时下载的最大线程数
	 */
	public static void configDownloadTheadPool(int maxSize) {
		mThreadPool = Executors.newFixedThreadPool(maxSize);
	}
	
	/**
	 * 添加下载任务
	 * @param downloadId 下载任务的id
	 * @param url        下载地址
	 * @param localPath	  本地存放地址
	 */
	public Download(int downloadId, String url, String localPath) {
		if (!new File(localPath).getParentFile().exists()) {
			new File(localPath).getParentFile().mkdirs();
		}
		
		L.l("下载地址", url);
		Log.i("Download: ",downloadId+"");
		
		mDownloadId = downloadId;
		mUrl = url;
		String[] tempArray = url.split("/");
		mFileName = tempArray[tempArray.length-1];
		mLocalPath = localPath.replaceAll("\"|\\(|\\)", "");
	}
	
	/**
	 * 设置监听器
	 * @param listener 设置下载监听器
	 * @return this
	 */
	public Download setOnDownloadListener(OnDownloadListener listener) {
		mListener = listener;
		return this;
	}
	
	/**
	 * 获取文件名
	 * @return 文件名
	 */
	public String getFileName() {
		return mFileName;
	}
	
	public String getLocalFileName() {
		String[] split = mLocalPath.split(File.separator);
		return split[split.length-1];
	}

	/**
	 * 开始下载
	 * params isGoon是否为继续下载
	 */
	@SuppressLint("HandlerLeak")
	public void start(final boolean isGoon) {
		// 处理消息
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ERROR:
					mListener.onError(mDownloadId);
					break;
				case CANCEL:
					mListener.onCancel(mDownloadId);
					break;
				case PAUSE:
					mListener.onPause(mDownloadId);
					break;
				case PUBLISH:
					mListener.onPublish(mDownloadId,
							Long.parseLong(msg.obj.toString()));
					break;
				case SUCCESS:
					mListener.onSuccess(mDownloadId);
					break;
				case START:
					mListener.onStart(mDownloadId,
							Long.parseLong(msg.obj.toString()));
					break;
				case GOON:
					mListener.onGoon(mDownloadId,
							Long.parseLong(msg.obj.toString()));
					break;
				}
			}
		};
		
		// 真正开始下载
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try{
					Log.i( "start down: ",mUrl);
					mUrl  = mUrl.replace(Constants.MUSIC_URL+Constants.MUSIC_URL,Constants.MUSIC_URL);
					download(isGoon,handler,mUrl,mLocalPath);
					//下载lrc文件
					String lrcurl = Constants.MUSIC_URL+File.separator+NetSearchFragment.mResultData.get(mDownloadId).getApp()+File.separator+"lrc/"+ NetSearchFragment.mResultData.get(mDownloadId).getLrc()+".lrc";
					String mlocalpath  =  MusicUtils.getLrcDir()+"/"+NetSearchFragment.mResultData.get(mDownloadId).getMusicName()+".lrc";
					String filename = NetSearchFragment.mResultData.get(mDownloadId).getMusicName()+".lrc";
					getLrc(lrcurl,mlocalpath,filename);

				}catch (Exception e){
					mListener.onCancel(mDownloadId);
					e.printStackTrace();
				}

			}
		});
	}

	private void getLrc(String downloadurl,String mLocalPath,String  fileName){
		Log.i( "getLrc: ",downloadurl);
		long curAllSize = 0;
		InputStream input = null;
		FileOutputStream output = null;
		HttpURLConnection conn = null;
		File file = null;
		try {
			File parent = new File(MusicUtils.getLrcDir());
			if (!parent.exists())
				parent.mkdirs();
			file = new File(parent, fileName);

			URL url = new URL(downloadurl);
			conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/octet-stream");
			conn.setRequestProperty("Keep-Alive", "Yes");
			conn.connect();
			int mAllSize = conn.getContentLength();
			input = conn.getInputStream();
			output = new FileOutputStream(file);
			byte[] cache = new byte[1024 * 2];
//			msg = Message.obtain();
//			msg.what = PUBLISH;
//			msg.obj = 0;
//			handler.sendMessage(msg);

			while (true)
			{
				// 暂停下载， 退出方法
				if (isPause) {
					// 发送暂停的消息
//					handler.sendEmptyMessage(PAUSE);
					L.l("下载暂停...");
					break;
				}
				// 取消下载， 删除文件并退出方法
				if (isCanceled) {
					L.l("手动关闭下载。。");
					new File(mLocalPath).delete();
					// 发送取消下载的消息
//					handler.sendEmptyMessage(CANCEL);
					return;
				}

				int cacheIndex = 0;
				while (true)
				{
					int stepSize = input.read(cache, cacheIndex, cache.length - cacheIndex);
					if (stepSize == -1)
						break;
					cacheIndex = cacheIndex + stepSize;
					if (cacheIndex == cache.length)
						break;
				}
				if (cacheIndex > 0)
				{
					output.write(cache, 0, cacheIndex);
					curAllSize = curAllSize + cacheIndex;
					int percent = (int) (curAllSize * 100 / mAllSize);
//					msg = Message.obtain();
//					msg.what = PUBLISH;
//					msg.obj = percent;
//					handler.sendMessage(msg);
				}
				if (cacheIndex < cache.length)
				{ // 已到达流的末尾
					if (curAllSize < mAllSize)
						throw new IOException("the input read stream has been interrupted");
					break;
				}
			}
			output.flush();
//			msg = Message.obtain();
//			msg.what = PUBLISH;
//			msg.obj = 100;
//			handler.sendMessage(msg);

		}catch(Exception e) {

			e.printStackTrace();
		}finally{
			try{
				if (input != null)
					input.close();
				if (output != null)
					output.close();
				if (conn != null)
					conn.disconnect();
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * 下载方法
	 * @param handler 消息处理器
	 */
	private void download(boolean isGoon, Handler handler,String downloadurl,String savepath) {
		Message msg = null;
		L.l("开始下载。。。");
		try {
			RandomAccessFile localFile =
					new RandomAccessFile(new File(mLocalPath), "rwd");



			long localFileLength = getLocalFileLength();
			final long remoteFileLength = getRemoteFileLength();
			long downloadedLength = localFileLength;

			// 远程文件不存在
			if (remoteFileLength == -1l) {
				L.l("下载文件不存在...");
				localFile.close();
				handler.sendEmptyMessage(ERROR);
				return;
			}

			// 本地文件存在
			if (localFileLength > -1l && localFileLength < remoteFileLength) {
				L.l("本地文件存在...");
				localFile.seek(localFileLength);
//				get.addHeader("Range", "bytes=" + localFileLength + "-"
//						+ remoteFileLength);
			}


			msg = Message.obtain();

			// 如果不是继续下载
			if(!isGoon) {
				// 发送开始下载的消息并获取文件大小的消息
				msg.what = START;
				msg.obj = remoteFileLength;
			}else {
				msg.what = GOON;
				msg.obj = localFileLength;
			}

			handler.sendMessage(msg);



			long curAllSize = 0;
			InputStream input = null;
			FileOutputStream output = null;
			HttpURLConnection conn = null;
			File file = null;
			try {
				File parent = new File(savepath);
				if (!parent.exists())
					parent.mkdirs();
				file = new File(parent, "");
				//下载url处理
				downloadurl  = downloadurl.replace(Constants.MUSIC_URL+Constants.MUSIC_URL,Constants.MUSIC_URL);
				URL url = new URL(downloadurl);
				conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "audio/mpeg");
				conn.setRequestProperty("Keep-Alive", "Yes");
                conn.connect();
				int mAllSize = conn.getContentLength();
				input = conn.getInputStream();
				output = new FileOutputStream(file);
				byte[] cache = new byte[1024 * 2];
				msg = Message.obtain();
				msg.what = PUBLISH;
				msg.obj = 0;
				handler.sendMessage(msg);

				while (true)
				{
					// 暂停下载， 退出方法
					if (isPause) {
						// 发送暂停的消息
						handler.sendEmptyMessage(PAUSE);
						L.l("下载暂停...");
						break;
					}
					// 取消下载， 删除文件并退出方法
					if (isCanceled) {
						L.l("手动关闭下载。。");
						new File(savepath).delete();
						// 发送取消下载的消息
						handler.sendEmptyMessage(CANCEL);
						return;
					}

					int cacheIndex = 0;
					while (true)
					{
						int stepSize = input.read(cache, cacheIndex, cache.length - cacheIndex);
						if (stepSize == -1)
							break;
						cacheIndex = cacheIndex + stepSize;
						if (cacheIndex == cache.length)
							break;
					}
					if (cacheIndex > 0)
					{
						output.write(cache, 0, cacheIndex);
						curAllSize = curAllSize + cacheIndex;
						int percent = (int) (curAllSize * 100 / mAllSize);
						msg = Message.obtain();
						msg.what = PUBLISH;
						msg.obj = percent;
						handler.sendMessage(msg);
					}
					if (cacheIndex < cache.length)
					{ // 已到达流的末尾
						if (curAllSize < mAllSize)
							throw new IOException("the input read stream has been interrupted");
						break;
					}
				}
				output.flush();
				msg = Message.obtain();
				msg.what = PUBLISH;
				msg.obj = 100;
				handler.sendMessage(msg);

			}catch(Exception e) {
				e.printStackTrace();
			}finally{
				if (input != null)
					input.close();
				if (output != null)
					output.close();
				if (conn != null)
					conn.disconnect();
			}

				// 发送下载完毕的消息
				if(!isPause) handler.sendEmptyMessage(SUCCESS);
		//	}
		} catch (Exception e) {
			e.printStackTrace();
			// 发送下载错误的消息
			handler.sendEmptyMessage(ERROR);
		}
	}
	private void publishProgress(Message msg,Handler handler,int downloadedLength ){
		msg = Message.obtain();
		msg.what = PUBLISH;
		msg.obj = downloadedLength;
		handler.sendMessage(msg);
	}

	/**
	 * 暂停/继续下载
	 * param pause 是否暂停下载
	 * 暂停 return true
	 * 继续 return false
	 */
	public synchronized boolean pause(boolean pause) {
		if(!pause) {
			L.l("继续下载");
			isPause = false;
			start(true); // 开始下载
		}else {
			L.l("暂停下载");
			isPause = true;
		}
		return isPause;
	}

	/**
	 * 关闭下载， 会删除文件
	 */
	public synchronized void cancel() {
		isCanceled = true;
		if(isPause) {
			new File(mLocalPath).delete();
		}
	}

	/**
	 * 获取本地文件大小
	 * @return 本地文件的大小 or 不存在返回-1
	 */
	public synchronized long getLocalFileLength() {
		long size = -1l;
		File localFile = new File(mLocalPath);
		if (localFile.exists()) {
			size = localFile.length();
		}
		L.l("本地文件大小" + size);
		return size <= 0 ? -1l : size;
	}

	/**
	 * 获取远程文件大小 or 不存在返回-1
	 * @return
	 */
	public synchronized long getRemoteFileLength() {
		long size = -1l;
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			client.setParams(getHttpParams());
			HttpGet get = new HttpGet(mUrl);

			HttpResponse response = client.execute(get);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode >= 200 && httpCode <= 300) {
				size = response.getEntity().getContentLength();
			}

			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

		L.l("远程文件大小" + size);
		return size;
	}

	/**
	 * 设置http参数 不能设置soTimeout
	 * @return HttpParams http参数
	 */
	private static HttpParams getHttpParams() {
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, UA);
//		ConnManagerParams.setTimeout(params, 10000);
//		HttpConnectionParams.setConnectionTimeout(params, 10000);
		
		return params;
	}
	
	/**
	 * 关闭下载线程池
	 */
	public static void closeDownloadThread() {
		if(null != mThreadPool) {
			mThreadPool.shutdownNow();
		}
	}
	/**
	 * 下载过程中的监听器
	 * 更新下载信息
	 *
	 */
	public interface OnDownloadListener {
		public void onStart(int downloadId, long fileSize);  // 回调开始下载
		public void onPublish(int downloadId, long size);	 // 回调更新进度
		public void onSuccess(int downloadId);				 // 回调下载成功
		public void onPause(int downloadId); 				 // 回调暂停
		public void onError(int downloadId);				 // 回调下载出错
		public void onCancel(int downloadId);			     // 回调取消下载
		public void onGoon(int downloadId, long localSize);  // 回调继续下载
	}
}
