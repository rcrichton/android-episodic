package org.rgcrichton.episodic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

public class SeriesFilter extends Activity {

	private EpisodicDbAdapter mDbHelper;
	private ListView mTagsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new EpisodicDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.series_filter);

		
	}

	private void populateFields() {
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
	}

	private void saveState() {
		
	}
}