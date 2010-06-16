package org.rgcrichton.episodic;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

public class SeriesListAdapter extends SimpleCursorAdapter {
	
	private EpisodicDbAdapter mDbHelper;
	private Episodic seriesTicker;

	public SeriesListAdapter(Episodic seriesTicker, int layout, Cursor c,
			String[] from, int[] to) {
		super(seriesTicker, layout, c, from, to);
		mDbHelper = new EpisodicDbAdapter(seriesTicker);
		this.seriesTicker = seriesTicker;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		Button seasonButton = (Button) view.findViewById(R.id.season_plus_button);
		Button episodeButton = (Button) view.findViewById(R.id.episode_plus_button);
		
		seasonButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	Long rowId = (Long) view.getTag();
            	mDbHelper.open();
                mDbHelper.incrementSeason(rowId);
                mDbHelper.close();
                seriesTicker.refreshData();
            }

        });
		
		episodeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	Long rowId = (Long) view.getTag();
            	mDbHelper.open();
            	mDbHelper.incrementEpisode(rowId);
            	mDbHelper.close();
            	seriesTicker.refreshData();
            }

        });
		
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				View seasonButtonView = view.findViewById(R.id.season_plus_button);
				seriesTicker.customOnListItemClick((Long)(seasonButtonView.getTag()));
			}
		});
		
		return view; 
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(EpisodicDbAdapter.KEY_ROWID);
		Long rowId = cursor.getLong(columnIndex);
		
		View seasonButtonView = view.findViewById(R.id.season_plus_button);
		seasonButtonView.setTag(rowId);
		
		View episodeButtonView = view.findViewById(R.id.episode_plus_button);
		episodeButtonView.setTag(rowId);
		
		super.bindView(view, context, cursor);
		
	}
	
	

}
