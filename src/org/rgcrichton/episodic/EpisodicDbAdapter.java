package org.rgcrichton.episodic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
	private static final String SERIES_TICKER_TO_TAGS_TABLE = "series_ticker_to_tags_table";
	// public static final String KEY_ROWID = "_id";
	public static final String KEY_SERIES_TICKER_ID = "series_ticker_id";
	public static final String KEY_TAG_ID = "tag_id";
	
	//The Android's default system path of your application database.
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

        @Override
        public void onCreate(SQLiteDatabase db) {
        	//this.getReadableDatabase();
            
        	try {
	            //Open your local db as the input stream
	        	InputStream myInput = mCtx.getAssets().open(DATABASE_NAME);
	     
	        	// Path to the just created empty db
	        	String outFileName = DB_PATH + DATABASE_NAME;
	     
	        	//Open the empty db as the output stream
	        	OutputStream myOutput = new FileOutputStream(outFileName);
	     
	        	//transfer bytes from the inputfile to the outputfile
	        	byte[] buffer = new byte[1024];
	        	int length;
	        	while ((length = myInput.read(buffer)) > 0){
	        		myOutput.write(buffer, 0, length);
	        	}
	     
	        	//Close the streams
	        	myOutput.flush();
	        	myOutput.close();
	        	myInput.close();
        	}
        	catch (IOException e) {
        		Log.e(this.getClass().toString(), e.getMessage());
        	}
       }


		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(this.getClass().toString(), "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            //db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            //onCreate(db);
        }
    }
    
    public EpisodicDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public EpisodicDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public synchronized void close() {
    	if(mDb != null) {
    		mDb.close();
    	}
    }


    public long createSeries(String seriesName, Integer seasonNum, Integer episodeNum) {
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
        return mDb.query(SERIES_TICKER_TABLE, new String[] {KEY_ROWID, KEY_SERIES_NAME,
                KEY_SEASON_NUM, KEY_EPISODE_NUM}, null, null, null, null, null);
    }

    public Cursor fetchSeries(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, SERIES_TICKER_TABLE, new String[] {KEY_ROWID, KEY_SERIES_NAME,
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

        return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

	public boolean incrementSeason(Long rowId) {
		Cursor series = fetchSeries(rowId);
		Integer seasonNum = series.getInt(series.getColumnIndex(KEY_SEASON_NUM));
		
		ContentValues args = new ContentValues();
        args.put(KEY_SEASON_NUM, seasonNum + 1);

        return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean incrementEpisode(Long rowId) {
		Cursor series = fetchSeries(rowId);
		Integer episodeNum = series.getInt(series.getColumnIndex(KEY_EPISODE_NUM));
		
		ContentValues args = new ContentValues();
        args.put(KEY_EPISODE_NUM, episodeNum + 1);

        return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public long createTag(String tagName) {
		ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TAG_NAME, tagName);

        return mDb.insert(TAGS_TABLE, null, initialValues);
	}
	
	public Cursor fetchTags(long rowId) throws SQLException {
		String fetchTagsQuery = "SELECT a." + KEY_ROWID + " a." + KEY_TAG_NAME + " FROM " + TAGS_TABLE + " a, " + SERIES_TICKER_TO_TAGS_TABLE + " b where a." + KEY_ROWID + 
								" = b." + KEY_TAG_ID + " and b." + KEY_SERIES_TICKER_ID + " = " + rowId;
        Cursor mCursor = mDb.rawQuery(fetchTagsQuery, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        return mCursor;
    }
	
	public boolean updateTag(long rowId, String tagName) {
        ContentValues args = new ContentValues();
        args.put(KEY_TAG_NAME, tagName);

        return mDb.update(TAGS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
	
	public boolean deleteTag(long rowId) {
        return mDb.delete(TAGS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
