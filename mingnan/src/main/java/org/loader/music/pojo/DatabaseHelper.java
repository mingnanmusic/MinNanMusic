package org.loader.music.pojo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String TABLE_NAME = "ormlite-news.db";
	private Dao<SearchResult, Integer> SearchResultDao;
	
	private DatabaseHelper(Context context) {
		super(context, TABLE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, SearchResult.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
			ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			// ɾ����
			TableUtils.dropTable(connectionSource, SearchResult.class, true);
			onCreate(database, connectionSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static DatabaseHelper instance;


	public static synchronized DatabaseHelper getHelper(Context context) {
		if (instance == null) {
			synchronized (DatabaseHelper.class) {
				if (instance == null)
					instance = new DatabaseHelper(context);
			}
		}
		return instance;
	}


	public Dao<SearchResult, Integer> getSearchResultDao() throws SQLException {
		if (SearchResultDao == null) {
			SearchResultDao = getDao(SearchResult.class);
		}
		return SearchResultDao;
	}


	@Override
	public void close() {
		super.close();
		SearchResultDao = null;
	}
}
