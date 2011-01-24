package org.rgcrichton.episodic;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class EpisodicSettings extends ListActivity {

	private EpisodicDbAdapter mDbHelper;
	
	private ListView mListView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new EpisodicDbAdapter(this);
        mDbHelper.open();
        
        populateSettings();
        
        mListView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView arg0, View arg1, int arg2,long arg3) {
        		// See which check boxes are checked and save them to DB
	        	SparseBooleanArray checkedBoxes = mListView.getCheckedItemPositions();
	        	for(int i = 0; i < mListView.getCount(); i++) {
	        		// Save 1 for if the checkbox is ticked, 0 otherwise
	        		int settingValue = checkedBoxes.valueAt(i) ? 1 : 0;
		        	mDbHelper.updateSettings(i, settingValue);
	        	}
        	}
    	});
        
        
    }

	private void populateSettings() {
		Cursor settingsCursor = mDbHelper.fetchSettings();
		ArrayList<String> settingTexts = new ArrayList<String>();
		ArrayList<Integer> settingValues = new ArrayList<Integer>();
		while (!settingsCursor.isAfterLast()){
			settingTexts.add(settingsCursor.getString(settingsCursor.getColumnIndex(EpisodicDbAdapter.KEY_SETTING_TEXT)));
			settingValues.add(settingsCursor.getInt(settingsCursor.getColumnIndex(EpisodicDbAdapter.KEY_SETTING_VALUE)));
			settingsCursor.moveToNext();
		}
		
		setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, settingTexts));

        mListView = getListView();

        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        for(int i = 0; i < mListView.getCount(); i++) {
        	boolean active = settingValues.get(i) > 0 ? true : false;
        	mListView.setItemChecked(i, active);
        }
	}
}
