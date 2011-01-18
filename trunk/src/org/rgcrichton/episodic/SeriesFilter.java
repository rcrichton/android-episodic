/*package org.rgcrichton.episodic;

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

		mTagsList = (ListView) findViewById(R.id.tagsList);

		Button filterButton = (Button) findViewById(R.id.filter);

		mRowId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(EpisodicDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras
					.getLong(EpisodicDbAdapter.KEY_ROWID) : null;
		}

		populateFields();

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}

		});
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor series = mDbHelper.fetchSeries(mRowId);
			startManagingCursor(series);
			mSeriesTitleText.setText(series.getString(series
					.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_SERIES_NAME)));
			mSeasonNumText.setText(series.getString(series
					.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_SEASON_NUM)));
			mEpisodeNumText.setText(series.getString(series
					.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_EPISODE_NUM)));
			mTagsText.setText(series.getString(series
					.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_TAGS)));
		}
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
		saveState();
		outState.putSerializable(EpisodicDbAdapter.KEY_ROWID, mRowId);
	}

	private void saveState() {
		String seriesTitle = mSeriesTitleText.getText().toString();

		Integer seasonNum = 0;
		Integer episodeNum = 0;

		try {
			seasonNum = Integer.valueOf(mSeasonNumText.getText().toString());
		} catch (NumberFormatException nfe) {
			// do nothing already set to 0 as default
		}

		try {
			episodeNum = Integer.valueOf(mEpisodeNumText.getText().toString());
		} catch (NumberFormatException nfe) {
			// do nothing already set to 0 as default
		}

		String tags = mTagsText.getText().toString();

		// Save only if title has some text
		if (seriesTitle.length() > 0) {
			if (mRowId == null) {
				long id = mDbHelper.createSeries(seriesTitle, seasonNum,
						episodeNum, tags);
				if (id > 0) {
					mRowId = id;
				}
			} else {
				mDbHelper.updateSeries(mRowId, seriesTitle, seasonNum,
						episodeNum, tags);
			}
		}
	}
}*/