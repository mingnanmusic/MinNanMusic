package org.loader.music.utils;

import java.io.File;
import java.util.ArrayList;

import org.loader.music.application.App;
import org.loader.music.pojo.Music;
import org.loader.music.pojo.MusicList;

import android.os.Environment;
import android.util.Log;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class MusicUtils {
    public   static   String TAG  = MusicUtils.class.getSimpleName();
	// 存放歌曲列表
	public static ArrayList<Music> sMusicList = new ArrayList<Music>();
	public static MusicList musicList  =new MusicList();
    public static   int  count=1;
	public static void initMusicList() {
		// 获取歌曲列表
        Log.i(TAG, "initMusicList: "+count);
        count++;
        sMusicList.clear();
		musicList.getResults().clear();
		LocalMusicUtils.queryMusic(getBaseDir());
		//sMusicList.addAll(LocalMusicUtils.queryMusic(getBaseDir()));
	}

	/**
	 * 获取内存卡根
	 * @return
	 */
	public static String getBaseDir() {
		String dir = null;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator;
		}
		Log.i("test", "getBaseDir: "+dir);
		return dir;
	}

	/**
	 * 获取应用程序使用的本地目录
	 * @return
	 */
	public static String getAppLocalDir() {
		String dir = null;

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator
					+ Constants.DOWNLOAD_PATH + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator + Constants.DOWNLOAD_PATH + File.separator;
		}

		return mkdir(dir);
	}

	/**
	 * 获取音乐存放目录
	 * @return
	 */
	public static String getMusicDir() {
		String musicDir = getAppLocalDir() + "music" + File.separator;
		return mkdir(musicDir);
	}

	/**
	 * 获取歌词存放目录
	 * 
	 * @return
	 */
	public static String getLrcDir() {
		String lrcDir = getAppLocalDir() + "lrc" + File.separator;
		return mkdir(lrcDir);
	}

	/**
	 * 创建文件夹
	 * @param dir
	 * @return
	 */
	public static String mkdir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			for (int i = 0; i < 5; i++) {
				if(f.mkdirs()) return dir;
			}
			return null;
		}
		
		return dir;
	}
}
