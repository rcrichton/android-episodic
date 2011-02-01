package org.rgcrichton.episodic;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;

public class TagListAdapter extends SimpleCursorAdapter {
	
	private EpisodicDbAdapter mDbHelper;
	private TagList tagList;
	private long seriesRowId;
	
	/**
	 * Set to true if we are filter the series list by tag.
	 */
	private boolean mFiltering; 
	
	public TagListAdapter(TagList tagList, int layout, Cursor c,
			String[] from, int[] to, long seriesRowId) {
		super(tagList, layout, c, from, to);
		
		mDbHelper = new EpisodicDbAdapter(tagList);
		mDbHelper.open();
		
		this.tagList = tagList;
		this.seriesRowId = seriesRowId;
		this.mFiltering = false;
	}
	
	public TagListAdapter(TagList tagList, int layout, Cursor c,
			String[] from, int[] to) {
		super(tagList, layout, c, from, to);
		
		this.mDbHelper = new EpisodicDbAdapter(tagList);
		this.tagList = tagList;
		this.mFiltering = true;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(EpisodicDbAdapter.KEY_ROWID);
		Long tagRowId = cursor.getLong(columnIndex);
		
		View tagCheckbox = view.findViewById(R.id.tag_checkbox);
		tagCheckbox.setTag(tagRowId);
		
		super.bindView(view, context, cursor);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = super.getView(position, convertView, parent);
		
		CheckBox checkbox;
		checkbox = (CheckBox) view.findViewById(R.id.tag_checkbox);
		if (!mFiltering) {	
			checkbox.setChecked(mDbHelper.hasTag(seriesRowId, (Long) checkbox.getTag()));
		}
		
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				long tagRowId = (Long) buttonView.getTag();
				if (!mFiltering) {
					if (isChecked) {
						mDbHelper.addTagToSeries(seriesRowId, tagRowId);
					} else {
						mDbHelper.removeTagFromSeries(seriesRowId, tagRowId);
					}
				}
				else {
					if (isChecked) {
						tagList.addTag((int)tagRowId);
					} else {
						tagList.removeTag((int)tagRowId);
					}
				}
			}
		});
		
		return view;
	}
	
	

}
