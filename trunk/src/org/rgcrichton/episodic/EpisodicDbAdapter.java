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
import android.database.sqlite.SQLiteException;
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
	private static final String SERIES_TICKER_TO_TAGS_TABLE = "series_ticker_to_tags";
	// public static final String KEY_ROWID = "_id";
	public static final String KEY_SERIES_TICKER_ID = "series_ticker_id";
	public static final String KEY_TAG_ID = "tags_id";
	
	// Settings table
	private static final String SETTINGS_TABLE = "settings";
	// public static final String KEY_ROWID = "_id";
	public static final String KEY_SETTING_TEXT = "setting_text";
	public static final String KEY_SETTING_VALUE = "setting_value";
	
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
        
        /**
         * Creates a empty database on the system and rewrites it with your own database.
         * */
        public void createDataBase() throws IOException{
     
        	boolean dbExist = checkDataBase();
     
        	if(dbExist){
        		//do nothing - database already exist
        	}else{
     
        		//By calling this method and empty database will be created into the default system path
                   //of your application so we are gonna be able to overwrite that database with our database.
            	this.getReadableDatabase();
     
            	try {
        			copyDataBase();
     
        		} catch (IOException e) {
            		throw new Error("Error copying database");
            	}
        	}
        }
        
        /**
         * Copies your database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transfering bytestream.
         * */
        private void copyDataBase() throws IOException{
     
        	//Open your local db as the input stream
        	InputStream myInput = mCtx.getAssets().open("EpisodicDBSchema.dbs");
     
        	// Path to the just created empty db
        	String outFileName = DB_PATH + DATABASE_NAME;
     
        	//Open the empty db as the output stream
        	OutputStream myOutput = new FileOutputStream(outFileName);
     
        	//transfer bytes from the inputfile to the outputfile
        	byte[] buffer = new byte[1024];
        	int length;
        	while ((length = myInput.read(buffer))>0){
        		myOutput.write(buffer, 0, length);
        	}
     
        	//Close the streams
        	myOutput.flush();
        	myOutput.close();
        	myInput.close();
        }
        
        /**
         * Check if the database already exist to avoid re-copying the file each time you open the application.
         * @return true if it exists, false if it doesn't
         */
        private boolean checkDataBase(){
     
        	SQLiteDatabase checkDB = null;
     
        	try{
        		String myPath = DB_PATH + DATABASE_NAME;
        		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     
        	}catch(SQLiteException e) {
        		//database does't exist yet.
        	}
     
        	if(checkDB != null) {
        		checkDB.close();
        	}

        	return checkDB != null ? true : false;
        }
        
        public void openDataBase() throws SQLException {
        	//Open the database
            String myPath = DB_PATH + DATABASE_NAME;
        	mDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }

        public void onCreate(SQLiteDatabase db) {}
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
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

        Cursor cursor =

            mDb.query(true, SERIES_TICKER_TABLE, new String[] {KEY_ROWID, KEY_SERIES_NAME,
                    KEY_SEASON_NUM, KEY_EPISODE_NUM}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

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
	
	public boolean resetEpisode(Long rowId) {		
		ContentValues args = new ContentValues();
        args.put(KEY_EPISODE_NUM, 1);

        return mDb.update(SERIES_TICKER_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public long createTag(String tagName) {
		ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TAG_NAME, tagName);

        return mDb.insert(TAGS_TABLE, null, initialValues);
	}
	
	public Cursor fetchTags(long rowId) throws SQLException {
		String fetchTagsQuery = "SELECT a." + KEY_ROWID + ", a." + KEY_TAG_NAME + " FROM " + TAGS_TABLE + " a, " + SERIES_TICKER_TO_TAGS_TABLE + " b where a." + KEY_ROWID + 
								" = b." + KEY_TAG_ID + " and b." + KEY_SERIES_TICKER_ID + " = " + rowId;
        Cursor cursor = mDb.rawQuery(fetchTagsQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        
        return cursor;
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
	
	/**
	 * Fetches all settings from the database and returns a Cursor to the first record.
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchSettings() throws SQLException {
		Cursor cursor = mDb.query(true, SETTINGS_TABLE, new String[] {KEY_ROWID, KEY_SETTING_TEXT, KEY_SETTING_VALUE}, 
							      null, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
	}
}
