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
	
	public TagListAdapter(TagList tagList, int layout, Cursor c,
			String[] from, int[] to, long seriesRowId) {
		super(tagList, layout, c, from, to);
		
		this.mDbHelper = new EpisodicDbAdapter(tagList);
		this.tagList = tagList;
		this.seriesRowId = seriesRowId;
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
		
		CheckBox checkbox = (CheckBox) view.findViewById(R.id.tag_checkbox);
		
		mDbHelper.open();
		checkbox.setChecked(mDbHelper.hasTag(seriesRowId, (Long) checkbox.getTag()));
		mDbHelper.close();
		
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				long tagRowId = (Long) buttonView.getTag();
				if (isChecked) {
					mDbHelper.open();
					mDbHelper.addTagToSeries(seriesRowId, tagRowId);
					mDbHelper.close();
				} else {
					mDbHelper.open();
					mDbHelper.removeTagFromSeries(seriesRowId, tagRowId);
					mDbHelper.close();
				}
			}
		});
		
		return view;
	}
	
	

}
