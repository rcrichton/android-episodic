package org.rgcrichton.episodic;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Episodic extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int ACTIVITY_FILTER = 2;
	private static final int ACTIVITY_SETTINGS = 3;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int FILTER_ID = Menu.FIRST + 2;
	private static final int SETTINGS_ID = Menu.FIRST + 3;
	
	private final int mResetEpisodeOnNextSeasonID = 0;
	private final int mDisableAdsID = 1;
	private boolean mDisableAds = false;
	private boolean mResetEpisodeOnNextSeason = false;

	private EpisodicDbAdapter mDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.series_list);
		mDbHelper = new EpisodicDbAdapter(this);
		mDbHelper.open();
		refreshData();
		registerForContextMenu(getListView());
	}

	public void refreshData() {
		// Get the settings from the DB.
		updateSettings();
		
		// Get all of the rows from the database and create the item list
		Cursor seriesCursor = mDbHelper.fetchAllSeries();
		startManagingCursor(seriesCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] {
				EpisodicDbAdapter.KEY_SERIES_NAME,
				EpisodicDbAdapter.KEY_SEASON_NUM,
				EpisodicDbAdapter.KEY_EPISODE_NUM };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] {
				R.id.series_name,
				R.id.season_num,
				R.id.episode_num };

		// Now create a simple cursor adapter and set it to display
		SeriesListAdapter series = new SeriesListAdapter(this,
				R.layout.series_row, seriesCursor, from, to);
		
		setListAdapter(series);
	}

	private void updateSettings() {
		Cursor settingsCursor = mDbHelper.fetchSettings();
		
		while (!settingsCursor.isAfterLast()) {
			boolean settingValue = settingsCursor.getInt(settingsCursor.getColumnIndex(EpisodicDbAdapter.KEY_SETTING_VALUE)) > 0 ? true : false;
			
			switch (settingsCursor.getInt(settingsCursor.getColumnIndex(EpisodicDbAdapter.KEY_ROWID))) {
				case mResetEpisodeOnNextSeasonID:
					mResetEpisodeOnNextSeason = settingValue;
					break;
				
				case mDisableAdsID:
					mDisableAds = settingValue;
					break;
			}
			
			settingsCursor.moveToNext();
		}
	}
	
	public boolean GetDisableAds() {
		return mDisableAds;
	}
	
	public boolean GetResetEpisodeOnNextSeason() {
		return mResetEpisodeOnNextSeason;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		refreshData();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteSeries(info.id);
			refreshData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_add);
		menu.add(0, FILTER_ID, 0, R.string.menu_filter);
		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case INSERT_ID:
				createSeries();
				return true;
				
			case FILTER_ID:
				filterSeries();
				return true;
			
			case SETTINGS_ID:
				episodicSettings();
				return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void createSeries() {
		Intent i = new Intent(this, SeriesEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
	}
	
	private void filterSeries() {
		Intent i = new Intent(this, SeriesEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
	}
	
	private void episodicSettings() {
		Intent i = new Intent(this, EpisodicSettings.class);
		startActivityForResult(i, ACTIVITY_SETTINGS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteSeries(info.id);
			refreshData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, SeriesEdit.class);
        i.putExtra(EpisodicDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
	}
	
}