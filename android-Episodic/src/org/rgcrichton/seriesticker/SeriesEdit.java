package org.rgcrichton.seriesticker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SeriesEdit extends Activity {
	
	private SeriesTickerDbAdapter mDbHelper;

    private EditText mSeriesTitleText;
    private EditText mSeasonNumText;
    private EditText mEpisodeNumText;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new SeriesTickerDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.series_edit);

        mSeriesTitleText = (EditText) findViewById(R.id.series_name);
        mSeasonNumText = (EditText) findViewById(R.id.season_num);
        mEpisodeNumText = (EditText) findViewById(R.id.episode_num);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(SeriesTickerDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(SeriesTickerDbAdapter.KEY_ROWID)
                                    : null;
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
	        mSeriesTitleText.setText(series.getString(
	                    series.getColumnIndexOrThrow(SeriesTickerDbAdapter.KEY_SERIES_NAME)));
	        mSeasonNumText.setText(series.getString(
	                series.getColumnIndexOrThrow(SeriesTickerDbAdapter.KEY_SEASON_NUM)));
	        mEpisodeNumText.setText(series.getString(
	                series.getColumnIndexOrThrow(SeriesTickerDbAdapter.KEY_EPISODE_NUM)));
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
        outState.putSerializable(SeriesTickerDbAdapter.KEY_ROWID, mRowId);
	}
	
	private void saveState() {
        String seriesTitle = mSeriesTitleText.getText().toString();
        Integer seasonNum = Integer.valueOf(mSeasonNumText.getText().toString());
        Integer episodeNum = Integer.valueOf(mEpisodeNumText.getText().toString());

        if (mRowId == null) {
            long id = mDbHelper.createSeries(seriesTitle, seasonNum, episodeNum);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateSeries(mRowId, seriesTitle, seasonNum, episodeNum);
        }
    }
}
