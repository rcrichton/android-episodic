package org.rgcrichton.episodic;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class TagList extends ListActivity {

	private EpisodicDbAdapter mDbHelper;
	private long seriesRowId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new EpisodicDbAdapter(this);
		mDbHelper.open();
		
		this.seriesRowId = (Long) this.getIntent().getExtras().get(EpisodicDbAdapter.KEY_ROWID);

		setContentView(R.layout.tag_list);
		
		refreshData();
		
		Button confirmButton = (Button) findViewById(R.id.tag_comfirm);

		confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
		
		Button addnewTagButton = (Button) findViewById(R.id.add_new_tag_button);

		addnewTagButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	EditText newTagEditText = (EditText) findViewById(R.id.new_tag_edit_text);
                String tagName = newTagEditText.getText().toString();
                mDbHelper.createTag(tagName);
                refreshData();
                
            }

        });
	}
	
	public void refreshData() {
		// Get all of the rows from the database and create the item list
		Cursor tagsCursor = mDbHelper.fetchAllTags();
		startManagingCursor(tagsCursor);

		// Create an array to specify the fields we want to display in the list
		String[] from = new String[] { EpisodicDbAdapter.KEY_TAG_NAME };

		// and an array of the fields we want to bind those fields to
		int[] to = new int[] { R.id.tag_name };

		// Now create a simple cursor adapter and set it to display
		TagListAdapter tags = new TagListAdapter(this,
				R.layout.tag_row, tagsCursor, from, to, seriesRowId);
		
		setListAdapter(tags);
	}


}