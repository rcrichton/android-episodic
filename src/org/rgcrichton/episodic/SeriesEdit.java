package org.rgcrichton.episodic;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
    private String mTagsString;
    private Long mRowId;
    
    private Set<Integer> mTagRowIdsToAdd = new HashSet<Integer>();
    private Set<Integer> mTagRowIdsToDelete = new HashSet<Integer>();
    
    private Context ctx = this;

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
        
        Button addTagsButton = (Button) findViewById(R.id.add_tags);
        

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
        
        addTagsButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(ctx, TagList.class);
                i.putExtra(EpisodicDbAdapter.KEY_ROWID, mRowId);
                startActivityForResult(i, 0);
            }

        });
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null)
		{
			Set<Integer> mTagRowIds = new HashSet<Integer>(data.getIntegerArrayListExtra("org.rgcrichton.episodic.tags"));
			updateTags(mTagRowIds);
		}
	}

	private void updateTags(Set<Integer> mTagRowIds) {
		// fetch current tags for this series and don't display then if they have been deleted
        Cursor tags = mDbHelper.fetchTags(mRowId);
        startManagingCursor(tags);
        
        mTagsString = "";
        mTagRowIdsToDelete.clear();
        mTagRowIdsToAdd = mTagRowIds;
        
        while (!tags.isAfterLast()) {
        	Integer tagId = tags.getInt(tags.getColumnIndex(EpisodicDbAdapter.KEY_ROWID));
        	if (mTagRowIds.contains(tagId)) {
        		mTagRowIdsToAdd.remove(tagId);
	        	String tagName = tags.getString(tags.getColumnIndex(EpisodicDbAdapter.KEY_TAG_NAME));
	        	if (mTagsString.length() <= 0) {
	        		mTagsString += tagName;
	        	} else {
	        		mTagsString += ", " + tagName;
	        	}
        	} else {
        		mTagRowIdsToDelete.add(tagId);
        	}
        	
        	tags.moveToNext();
        }

        // add tags that have been recently checked
        Cursor tags2 = mDbHelper.fetchTags(mTagRowIdsToAdd);
        startManagingCursor(tags2);
        while (!tags2.isAfterLast()) {
        	String tagName = tags2.getString(tags2.getColumnIndex(EpisodicDbAdapter.KEY_TAG_NAME));
        	if (mTagsString.length() <= 0) {
        		mTagsString += tagName;
        	} else {
        		mTagsString += ", " + tagName;
        	}
        	
        	tags2.moveToNext();
        }
        
        Handler refresh = new Handler();
        refresh.post(new Runnable() {
            public void run()
            {
            	updateTagsTextView();
            }
        });
	}
	
	private void updateTagsTextView() {
		mTagsTextView.setText(mTagsString);
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
	        startManagingCursor(tags);
	        String tagsStr = "";
	        while (!tags.isAfterLast()) {
	        	String tagName = tags.getString(tags.getColumnIndex(EpisodicDbAdapter.KEY_TAG_NAME));
	        	if (tagsStr.length() <= 0) {
	        		tagsStr += tagName;
	        	} else {
	        		tagsStr += ", " + tagName;
	        	}
	        	
	        	tags.moveToNext();
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
		mDbHelper.open();
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
        
        // Save/delete tag associations
        for (Integer tagId : mTagRowIdsToAdd) {
        	mDbHelper.addTagToSeries(mRowId, tagId);
        }
        for (Integer tagId : mTagRowIdsToDelete) {
        	mDbHelper.removeTagFromSeries(mRowId, tagId);
        }
    }
}
