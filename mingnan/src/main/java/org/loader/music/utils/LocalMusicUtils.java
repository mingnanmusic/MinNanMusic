package org.loader.music.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.loader.music.application.App;
import org.loader.music.pojo.Music;
import org.loader.music.pojo.MusicList;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class LocalMusicUtils {
	private  static String TAG  = LocalMusicUtils.class.getSimpleName();
	/**
	 * 根据id获取歌曲uri
	 * @deprecated
	 * @param musicId
	 * @return
	 */
	public static String queryMusicById(int musicId) {
		String result = null;
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DATA },
				MediaStore.Audio.Media._ID + "=?",
				new String[] { String.valueOf(musicId) }, null);

		for (cursor.moveToFirst(); !cursor.isAfterLast();) {
			result = cursor.getString(0);
			break;
		}

		cursor.close();
		return result;
	}

     private static ArrayList<Music>  findFile(File file){
         ArrayList<Music> results = new ArrayList<Music>();
         File f = new File(file.getAbsolutePath());
         if(f.isFile()){
             if (f.getName().endsWith(".mp3")){
                 Music   music = new Music();
                 //       music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                 music.setTitle(f.getName());
                 //   music.setArtist(artist);
                 music.setUri(f.getAbsolutePath());
                 music.setLength((int)f.length()/1024);
                 results.add(music);
             //    music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
             }

         }else{
             File[] fs = f.listFiles();
             if(fs != null && fs.length > 0){
                 for(File cf : fs){
                     findFile(cf);
                 }
             }
         }
         return   results;
     }

	static ArrayList<String> list = new ArrayList<>();
	public static ArrayList<String> searchMp3Infos(File file, String[] ext) {
		if (file != null) {
			if (file.isDirectory()) {
				File[] listFile = file.listFiles();
				if (listFile != null) {
					for (int i = 0; i < listFile.length; i++) {
						searchMp3Infos(listFile[i], ext);
					}
				}
			} else {
				String filename = file.getAbsolutePath();
				for (int i = 0; i < ext.length; i++) {
					if (filename.endsWith(ext[i])) {
						boolean   hasmusic=false;
						for (int j = 0; j < list.size(); j++) {
							if (list.get(j).equals(filename)){
								hasmusic = true;
								break;
							}
						}
						if (!hasmusic){
							list.add(filename);
						}
						break;
					}
				}
			}
		}
		return list;
	}
	static int count=1;
	/**
	 * 获取目录下的歌曲
	 * @param dirName
	 */
	public static void queryMusic(String dirName) {
		Log.i(TAG, "queryMusic: "+count);
		count++;
		MusicUtils.musicList.getResults().clear();
//		ArrayList<Music> results = findFile(new File(dirName));
//
//                return results;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{

			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						String[] ext = { ".mp3" };
						File file = new File(MusicUtils.getAppLocalDir());//可以把"//////"这一串都去掉，扫面范围是整个SD卡，我嫌慢，而且扫出来很多...不是我想要的歌曲，最后还是直接写成QQ音乐的下载目录了，其实自己的APP支持下载的话，可以把歌曲下载的指定目录，然后扫描这个指定目录就行了~全局扫描的确不太合适，又不是杀毒..
						ArrayList<String> 	localMusicList = searchMp3Infos(file,ext);
						Log.i(TAG, "run: "+localMusicList.toString());

						for (int i = 0; i < localMusicList.size(); i++) {
							String path  = localMusicList.get(i);
							Music  music  = new Music();
							music.setUri(path);
							File mFile  = new File(path);
							if (mFile.getName().endsWith(".mp3"));
							{
								music.setTitle(mFile.getName().replace(".mp3",""));
							}
							MusicUtils.musicList.getResults().add(music);
						}

						EventBus.getDefault().post(MusicUtils.musicList);
							//hander.sendEmptyMessage(SEARCH_MUSIC_SUCCESS);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}.start();
		}




/*
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "%" },
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor == null){
			cursor = App.sContext.getContentResolver().query(
					MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null,
					MediaStore.Audio.Media.DATA + " like ?",
					new String[] { dirName + "%" },
					MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if(cursor==null)
				return results;
		}

		// id title singer data time image
		addMedia(cursor, results);

		cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "%" },
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor==null)
			return results;
		addMedia(cursor, results);*/
//		return results;
	}
	
	private static void addMedia(Cursor cursor,ArrayList<Music> results){
		Music music;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			// 如果不是音乐
			String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != null && isMusic.equals("")) continue;
			
			String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			
			if(isRepeat(title, artist)) continue;
			
			music = new Music();
			music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
			music.setTitle(title);
			music.setArtist(artist);
			music.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			music.setLength(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
			results.add(music);
		}

		cursor.close();
	}
	
	/**
	 * 根据音乐名称和艺术家来判断是否重复包含了
	 * @param title
	 * @param artist
	 * @return
	 */
	private static boolean isRepeat(String title, String artist) {
		for(Music music : MusicUtils.sMusicList) {
			if(title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据歌曲id获取图片
	 * @param albumId
	 * @return
	 */
	private static String getAlbumImage(int albumId) {
		String result = "";
		Cursor cursor = null;
		try {
			cursor = App.sContext.getContentResolver().query(
					Uri.parse("content://media/external/audio/albums/"
							+ albumId), new String[] { "album_art" }, null,
					null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast();) {
				result = cursor.getString(0);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return null == result ? null : result;
	}
}
