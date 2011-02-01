package org.rgcrichton.episodic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class EpisodicDbAdapter {

	// Series table and its columns
	private static final String SERIES_TICKER_TABLE = "series_ticker";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SERIES_NAME = "series_name";
	public static final String KEY_SEASON_NUM = "season_num";
	public static final String KEY_EPISODE_NUM = "episode_num";

	// Tags table and its columns
	private static final String TAGS_TABLE = "tags";
	// public static final String KEY_ROWID = "_id";
	public static final String KEY_TAG_NAME = "tag_name";

	// Series to tags table. Joins series and tags
	private static final String SERIES_TICKER_TO_TAGS_TABLE = "series_ticker_to_tags";
	// public static final String KEY_ROWID = "_id";
	public static final String KEY_SERIES_TICKER_ID = "series_ticker_id";
	public static final String KEY_TAG_ID = "tags_id";

	// Settings table
	private static final String SETTINGS_TABLE = "settings";
	// public static final String KEY_ROWID = "_id";
	public static final String KEY_SETTING_TEXT = "setting_text";
	public static final String KEY_SETTING_VALUE = "setting_value";

	// The Android's default system path of your application database.
	private static final String DB_PATH = "/data/data/org.rgcrichton.episodic/databases/";
	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;

	private final Context mCtx;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Creates a empty database on the system and rewrites it with your own
		 * database.
		 * */
		public void createDataBase() throws IOException {

			boolean dbExist = checkDataBase();

			if (dbExist) {
				// do nothing - database already exist
			} else {

				// By calling this method and empty database will be created
				// into the default system path
				// of your application so we are gonna be able to overwrite that
				// database with our database.
				this.getReadableDatabase();

				try {
					copyDataBase();

				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}
		}

		/**
		 * Copies your database from your local assets-folder to the just
		 * created empty database in the system folder, from where it can be
		 * accessed and handled. This is done by transfering bytestream.
		 * */
		private void copyDataBase() throws IOException {

			// Open your local db as the input stream
			InputStream myInput = mCtx.getAssets().open("EpisodicDBSchema.dbs");

			// Path to the just created empty db
			String outFileName = DB_PATH + DATABASE_NAME;

			// Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);

			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}

			// Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();
		}

		/**
		 * Check if the database already exist to avoid re-copying the file each
		 * time you open the application.
		 * 
		 * @return true if it exists, false if it doesn't
		 */
		private boolean checkDataBase() {

			SQLiteDatabase checkDB = null;

			try {
				String myPath = DB_PATH + DATABASE_NAME;
				checkDB = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READONLY);

			} catch (SQLiteException e) {
				// database does't exist yet.
			}

			if (checkDB != null) {
				checkDB.close();
			}

			return checkDB != null ? true : false;
		}

		public void openDataBase() throws SQLException {
			// Open the database
			String myPath = DB_PATH + DATABASE_NAME;
			mDb = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
		}

		public void onCreate(SQLiteDatabase db) {
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	public EpisodicDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public EpisodicDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		try {
			mDbHelper.createDataBase();
		} catch (IOException e) {
			throw new Error("Unable to create database");
		}
		mDbHelper.openDataBase();
		return this;
	}

	public synchronized void close() {
		if (mDb != null) {
			mDb.close();
		}
	}

	public long createSeries(String seriesName, Integer seasonNum,
			Integer episodeNum) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_SERIES_NAME, seriesName);
		initialValues.put(KEY_SEASON_NUM, seasonNum);
		initialValues.put(KEY_EPISODE_NUM, episodeNum);

		return mDb.insert(SERIES_TICKER_TABLE, null, initialValues);
	}

	public boolean deleteSeries(long rowId) {
		return mDb.delete(SERIES_TICKER_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllSeries() {
		return mDb.query(SERIES_TICKER_TABLE, new String[] { KEY_ROWID,
				KEY_SERIES_NAME, KEY_SEASON_NUM, KEY_EPISODE_NUM }, null, null,
				null, null, null);
	}

	public Cursor fetchSeriesByTags(ArrayList<Integer> tagRowIdsForFilter) {
		// Get all series if there are not tags to filter on.
		if (tagRowIdsForFilter.isEmpty())
		{
			return fetchAllSeries();
		}
		
		// Create the sub query which fetches all the series IDs that match the specified tag IDs.
		String fetchSeriesForTagsSubQuery = "SELECT " + KEY_SERIES_TICKER_ID
				+ " FROM " + SERIES_TICKER_TO_TAGS_TABLE + " WHERE "
				+ KEY_TAG_ID + " in (";

		for (int i = 0; i < tagRowIdsForFilter.size(); i++) {
			fetchSeriesForTagsSubQuery += tagRowIdsForFilter.get(i);
			if (i != tagRowIdsForFilter.size() - 1) {
				fetchSeriesForTagsSubQuery += ",";
			}
		}
		
		fetchSeriesForTagsSubQuery += ") GROUP BY " + KEY_SERIES_TICKER_ID
		+ " HAVING COUNT(*) = " + tagRowIdsForFilter.size();

		// Create the main query, which will use the sub query to generate 
		// the table need in the FROM clause.
		String fetchSeriesForTagsQuery = "SELECT a." + KEY_ROWID + ", a."
				+ KEY_SERIES_NAME + ", a." + KEY_SEASON_NUM + ", a."
				+ KEY_EPISODE_NUM + " FROM " + SERIES_TICKER_TABLE + " a, ("
				+ fetchSeriesForTagsSubQuery + ") b where a." + KEY_ROWID
				+ " = b." + KEY_SERIES_TICKER_ID;

		Cursor cursor = mDb.rawQuery(fetchSeriesForTagsQuery, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}

		return cursor;
	}

	public Cursor fetchSeries(long rowId) throws SQLException {
		Cursor cursor =
		mDb.query(true, SERIES_TICKER_TABLE, new String[] { KEY_ROWID,
				KEY_SERIES_NAME, KEY_SEASON_NUM, KEY_EPISODE_NUM }, KEY_ROWID
				+ "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public boolean updateSeries(long rowId, String seriesName,
			Integer seasonNum, Integer episodeNum) {
		ContentValues args = new ContentValues();
		args.put(KEY_SERIES_NAME, seriesName);
		args.put(KEY_SEASON_NUM, seasonNum);
		args.put(KEY_EPISODE_NUM, episodeNum);

		return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	public boolean incrementSeason(Long rowId) {
		Cursor series = fetchSeries(rowId);
		Integer seasonNum = series
				.getInt(series.getColumnIndex(KEY_SEASON_NUM));
		series.close();

		ContentValues args = new ContentValues();
		args.put(KEY_SEASON_NUM, seasonNum + 1);

		return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	public boolean incrementEpisode(Long rowId) {
		Cursor series = fetchSeries(rowId);
		Integer episodeNum = series.getInt(series
				.getColumnIndex(KEY_EPISODE_NUM));
		series.close();

		ContentValues args = new ContentValues();
		args.put(KEY_EPISODE_NUM, episodeNum + 1);

		return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	public boolean resetEpisode(Long rowId) {
		ContentValues args = new ContentValues();
		args.put(KEY_EPISODE_NUM, 1);

		return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	public long createTag(String tagName) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TAG_NAME, tagName);

		return mDb.insert(TAGS_TABLE, null, initialValues);
	}

	public Cursor fetchAllTags() {
		return mDb.query(TAGS_TABLE, new String[] { KEY_ROWID, KEY_TAG_NAME },
				null, null, null, null, null);
	}

	public Cursor fetchTags(long rowId) throws SQLException {
		String fetchTagsQuery = "SELECT a." + KEY_ROWID + ", a." + KEY_TAG_NAME
				+ " FROM " + TAGS_TABLE + " a, " + SERIES_TICKER_TO_TAGS_TABLE
				+ " b where a." + KEY_ROWID + " = b." + KEY_TAG_ID + " and b."
				+ KEY_SERIES_TICKER_ID + " = " + rowId;
		Cursor cursor = mDb.rawQuery(fetchTagsQuery, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}

		return cursor;
	}
	
	public Cursor fetchTags(Set<Integer> rowIds) throws SQLException {
		String whereClause = KEY_ROWID + " in (";
		
		Iterator<Integer> iter = rowIds.iterator();
		while (iter.hasNext()) {
			whereClause += iter.next();
			if (iter.hasNext()) {
				whereClause += ",";
			}
		}
		whereClause += ")";
		
		Cursor cursor = mDb.query(TAGS_TABLE, new String[] { KEY_ROWID, KEY_TAG_NAME },
				 whereClause, null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
		}
		
		return cursor;
	}

	public boolean hasTag(long rowId, long tagId) {
		Cursor cursor = mDb.query(SERIES_TICKER_TO_TAGS_TABLE, new String[] {
				KEY_SERIES_TICKER_ID, KEY_TAG_ID }, KEY_SERIES_TICKER_ID + "="
				+ rowId + " AND " + KEY_TAG_ID + "=" + tagId, null, null, null,
				null);
		int count = cursor.getCount();

		cursor.close();

		return count > 0 ? true : false;
	}

	public boolean updateTag(long rowId, String tagName) {
		ContentValues args = new ContentValues();
		args.put(KEY_TAG_NAME, tagName);

		return mDb.update(TAGS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteTag(long rowId) {
		return mDb.delete(TAGS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateSettings(long rowId, int settingValue) {
		ContentValues args = new ContentValues();
		args.put(KEY_SETTING_VALUE, settingValue);

		return mDb.update(SETTINGS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public long addTagToSeries(long seriesRowId, long tagId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_SERIES_TICKER_ID, seriesRowId);
		initialValues.put(KEY_TAG_ID, tagId);

		return mDb.insert(SERIES_TICKER_TO_TAGS_TABLE, null, initialValues);
	}

	public int removeTagFromSeries(long seriesRowId, long tagRowId) {
		return mDb.delete(SERIES_TICKER_TO_TAGS_TABLE, KEY_SERIES_TICKER_ID
				+ "=" + seriesRowId + " AND " + KEY_TAG_ID + "=" + tagRowId,
				null);
	}

	/**
	 * Fetches all settings from the database and returns a Cursor to the first
	 * record.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchSettings() throws SQLException {
		Cursor cursor = mDb.query(true, SETTINGS_TABLE, new String[] {
				KEY_ROWID, KEY_SETTING_TEXT, KEY_SETTING_VALUE }, null, null,
				null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

}
