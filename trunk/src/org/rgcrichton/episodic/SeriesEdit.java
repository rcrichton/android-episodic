package org.rgcrichton.episodic;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SeriesEdit extends Activity {
	
	private EpisodicDbAdapter mDbHelper;

    private EditText mSeriesTitleText;
    private EditText mSeasonNumText;
    private EditText mEpisodeNumText;
    private TextView mTagsTextView;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new EpisodicDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.series_edit);

        mSeriesTitleText = (EditText) findViewById(R.id.series_name);
        mSeasonNumText = (EditText) findViewById(R.id.season_num);
        mEpisodeNumText = (EditText) findViewById(R.id.episode_num);
        mTagsTextView = (TextView) findViewById(R.id.tags);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(EpisodicDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(EpisodicDbAdapter.KEY_ROWID)
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
	                    series.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_SERIES_NAME)));
	        mSeasonNumText.setText(series.getString(
	                series.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_SEASON_NUM)));
	        mEpisodeNumText.setText(series.getString(
	                series.getColumnIndexOrThrow(EpisodicDbAdapter.KEY_EPISODE_NUM)));
	        
	        // fetch tags for this series
	        Cursor tags = mDbHelper.fetchTags(mRowId);
	        String tagsStr = "";
	        while (!tags.isAfterLast()) {
	        	//tags.getInt(tags.getColumnIndex(EpisodicDbAdapter.KEY_ROWID));
	        	String tagName = tags.getString(tags.getColumnIndex(EpisodicDbAdapter.KEY_ROWID));
	        	if (tagName.length() == 0) {
	        		tagsStr += tagName;
	        	} else {
	        		tagsStr += ", " + tagName;
	        	}
	        }
	        mTagsTextView.setText(tagsStr);
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
        
        // Save only if title has some text
        if (seriesTitle.length() > 0) {
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
}
