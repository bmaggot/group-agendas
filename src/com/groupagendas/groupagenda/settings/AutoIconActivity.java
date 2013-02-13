package com.groupagendas.groupagenda.settings;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.metadata.MetaUtils;
import com.groupagendas.groupagenda.metadata.impl.AutoColorIconMetaData;

public class AutoIconActivity extends ListActivity implements AutoColorIconMetaData {
	private ProgressBar pb;
	private ArrayList<AutoIconItem> mItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auto_icon);

		LinearLayout addEntry = (LinearLayout) findViewById(R.id.auto_icon_add);
		addEntry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            startActivityForResult(new Intent(AutoIconActivity.this, EditAutoIconActivity.class), 1);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		pb = (ProgressBar) findViewById(R.id.progress);

		new GetAutoIcons().execute();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==1){
			new SetAutoIcons().execute();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final AutoIconItem item = mItems.get(position);
		
		Intent intent = new Intent(AutoIconActivity.this, EditAutoIconActivity.class);
		intent.putExtra("id", item.id);
		intent.putExtra("keyword", item.keyword);
		intent.putExtra("context", item.context);
		intent.putExtra("icon", item.icon);
		intent.putExtra("edit", true);
		startActivityForResult(intent, 1);
		super.onListItemClick(l, v, position, id);
	}
	
	class SetAutoIcons extends AsyncTask<Void, Boolean, Boolean>{
		@Override
		protected Boolean doInBackground(Void... params) {
			return DataManagement.setAutoIcons(AutoIconActivity.this);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			if (!success) {
				ContentValues values = new ContentValues();
				
				values.put(AutoIcon.NEED_UPDATE, 1);				
				getContentResolver().update(MetaUtils.getContentUri(AutoIcon.class), values, "", null);
			}
		}
	}
	
	class GetAutoIcons extends AsyncTask<Void, ArrayList<AutoIconItem>, ArrayList<AutoIconItem>>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}
		@Override
		protected ArrayList<AutoIconItem> doInBackground(Void... arg0) { 
			return DataManagement.getAutoIcons(AutoIconActivity.this);
		}
		
		@Override
		protected void onPostExecute(ArrayList<AutoIconItem> result) {
			super.onPostExecute(result);
			mItems = result; 
			setListAdapter(new AutoIconAdapter(AutoIconActivity.this, result));
			pb.setVisibility(View.INVISIBLE);
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.auto_icon_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.add_icon:
	            startActivityForResult(new Intent(AutoIconActivity.this, EditAutoIconActivity.class), 1);
	        break;
	    }
	    return true;
	}
}
