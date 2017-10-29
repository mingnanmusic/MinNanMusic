package org.loader.music.pojo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 2015年8月15日 15:51:26
 * 博文地址：http://blog.csdn.net/u010156024
 */
@DatabaseTable(tableName="tb_searchresult")
public class SearchResult implements Serializable {
	private static final long serialVersionUID = 0X00000001l;
	@DatabaseField(columnName="musicname")
	private String musicName;
	@DatabaseField(columnName="url")
	private String url;
	@DatabaseField(columnName="artist")
	private String artist;
	@DatabaseField(columnName="album")
	private String album;
	@DatabaseField(columnName="lrc")
	private String lrc;
	@DatabaseField(columnName="app")
	private  String App;//用来拼接下载链接，否则无法下载
	//generatedId=true：表示是主键
	@DatabaseField(generatedId=true)
	private  int id;

	public String getApp() {
		return App;
	}

	public void setApp(String app) {
		App = app;
	}

	public String getLrc() {
		return lrc;
	}

	public void setLrc(String lrc) {
		this.lrc = lrc;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}
}
