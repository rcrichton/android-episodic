package org.rgcrichton.episodic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;

public class TagList extends ListActivity {

	private EpisodicDbAdapter mDbHelper;
	private long seriesRowId;
	private Set<Integer> tagsList = new HashSet<Integer>();
	private static final String FILTER = "FILTER";
	private static final Integer INVALID_ROW_ID = -1;
	private static final int DELETE_ID = Menu.FIRST;
	private static int tagToDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new EpisodicDbAdapter(this);
		mDbHelper.open();
		
		Bundle extras = this.getIntent().getExtras();
		
		if (!filterTags())
		{
			this.seriesRowId = extras.getLong(EpisodicDbAdapter.KEY_ROWID, INVALID_ROW_ID);
		}

		setContentView(R.layout.tag_list);
		
		refreshData();
		
		Button confirmButton = (Button) findViewById(R.id.tag_comfirm);
		if (filterTags()) {
			confirmButton.setText(R.string.filter);
		}
		
		// register menu
		registerForContextMenu(getListView());
		

		confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	Intent resultIntent = new Intent();
            	ArrayList<Integer> tagArray = new ArrayList<Integer>(tagsList);
            	resultIntent.putIntegerArrayListExtra("org.rgcrichton.episodic.tags", tagArray);
                setResult(RESULT_OK, resultIntent);
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
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			showDialog((int) info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
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
		TagListAdapter tags;
		if (filterTags() || seriesRowId == -1) {
			tags = new TagListAdapter(this,
					R.layout.tag_row, tagsCursor, from, to);
		}
		else {
			tags = new TagListAdapter(this,
					R.layout.tag_row, tagsCursor, from, to, seriesRowId);
		}
		
		setListAdapter(tags);
	}
	
	protected Dialog onCreateDialog (int id) {
		tagToDelete = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.tag_delete_warning)
		       .setTitle(R.string.tag_delete_title)
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   mDbHelper.deleteTag(tagToDelete);
		   			   refreshData();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   tagToDelete = -1;
		               dialog.cancel();
		           }
		       });
		return builder.create();
	}
	
	/**
	 * Returns true if this tags list is being used for filter on the series list.
	 * @return
	 */
	private boolean filterTags() {
		return super.getIntent().hasExtra(FILTER);
	}

	public void addTag(Integer tagRowId) {
		tagsList.add(tagRowId);
	}

	public void removeTag(Integer tagRowId) {
		tagsList.remove(tagRowId);
	}
}