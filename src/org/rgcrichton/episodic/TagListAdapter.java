package org.rgcrichton.episodic;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

public class TagListAdapter extends SimpleCursorAdapter {
	
	private EpisodicDbAdapter mDbHelper;
	private TagList tagList;

	public TagListAdapter(TagList tagList, int layout, Cursor c,
			String[] from, int[] to) {
		super(tagList, layout, c, from, to);
		mDbHelper = new EpisodicDbAdapter(tagList);
		this.tagList = tagList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		
		
		return view;
	}

}
