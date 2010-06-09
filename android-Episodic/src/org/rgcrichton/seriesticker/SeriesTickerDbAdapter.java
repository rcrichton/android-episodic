package org.rgcrichton.seriesticker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SeriesTickerDbAdapter {
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SERIES_NAME = "series_name";
	public static final String KEY_SEASON_NUM = "season_num";
	public static final String KEY_EPISODE_NUM = "episode_num";
	
	private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "series_ticker";
    private static final int DATABASE_VERSION = 1;
    
    private static final String TAG = "SeriesTickerDbAdapter";
	
	private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " ("
		+ KEY_ROWID + " integer primary key autoincrement, "
        + KEY_SERIES_NAME + " text not null, "
        + KEY_SEASON_NUM + " int not null, "
        + KEY_EPISODE_NUM + " int not null);";

    private final Context mCtx;
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
    
    public SeriesTickerDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public SeriesTickerDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    public long createSeries(String seriesName, Integer seasonNum, Integer episodeNum) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SERIES_NAME, seriesName);
        initialValues.put(KEY_SEASON_NUM, seasonNum);
        initialValues.put(KEY_EPISODE_NUM, episodeNum);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteSeries(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllSeries() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_SERIES_NAME,
                KEY_SEASON_NUM, KEY_EPISODE_NUM}, null, null, null, null, null);
    }

    public Cursor fetchSeries(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_SERIES_NAME,
                    KEY_SEASON_NUM, KEY_EPISODE_NUM}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public boolean updateSeries(long rowId, String seriesName, Integer seasonNum, Integer episodeNum) {
        ContentValues args = new ContentValues();
        args.put(KEY_SERIES_NAME, seriesName);
        args.put(KEY_SEASON_NUM, seasonNum);
        args.put(KEY_EPISODE_NUM, episodeNum);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

	public boolean incrementSeason(Long rowId) {
		Cursor series = fetchSeries(rowId);
		Integer seasonNum = series.getInt(series.getColumnIndex(KEY_SEASON_NUM));
		
		ContentValues args = new ContentValues();
        args.put(KEY_SEASON_NUM, seasonNum + 1);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean incrementEpisode(Long rowId) {
		Cursor series = fetchSeries(rowId);
		Integer episodeNum = series.getInt(series.getColumnIndex(KEY_EPISODE_NUM));
		
		ContentValues args = new ContentValues();
        args.put(KEY_EPISODE_NUM, episodeNum + 1);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
