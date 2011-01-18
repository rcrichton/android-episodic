package org.rgcrichton.episodic;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

public class SeriesListAdapter extends SimpleCursorAdapter {
	
	private EpisodicDbAdapter mDbHelper;
	private Episodic episodic;

	public SeriesListAdapter(Episodic episodic, int layout, Cursor c,
			String[] from, int[] to) {
		super(episodic, layout, c, from, to);
		mDbHelper = new EpisodicDbAdapter(episodic);
		this.episodic = episodic;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		ImageButton seasonButton = (ImageButton) view.findViewById(R.id.season_plus_button);
		ImageButton episodeButton = (ImageButton) view.findViewById(R.id.episode_plus_button);
		
		seasonButton.setFocusable(false);
		episodeButton.setFocusable(false);
		
		seasonButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	Long rowId = (Long) view.getTag();
            	mDbHelper.open();
                mDbHelper.incrementSeason(rowId);
                mDbHelper.close();
                episodic.refreshData();
            }

        });
		
		episodeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	Long rowId = (Long) view.getTag();
            	mDbHelper.open();
            	mDbHelper.incrementEpisode(rowId);
            	mDbHelper.close();
            	episodic.refreshData();
            }

        });
		
		seasonButton.setFocusable(false);
		episodeButton.setFocusable(false);
		
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(EpisodicDbAdapter.KEY_ROWID);
		Long rowId = cursor.getLong(columnIndex);
		
		view.setTag(rowId);
		
		View seasonButtonView = view.findViewById(R.id.season_plus_button);
		seasonButtonView.setTag(rowId);
		
		View episodeButtonView = view.findViewById(R.id.episode_plus_button);
		episodeButtonView.setTag(rowId);
		
		super.bindView(view, context, cursor);
	}
}
