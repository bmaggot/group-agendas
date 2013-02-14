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
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.metadata.MetaUtils;
import com.groupagendas.groupagenda.metadata.impl.AutoColorIconMetaData.AutoColor;

public class AutoColorActivity extends ListActivity {
	private ProgressBar pb;
	private ArrayList<AutoColorItem> mItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auto_color);
		
		LinearLayout addEntry = (LinearLayout) findViewById(R.id.auto_color_add);
		addEntry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            startActivityForResult(new Intent(AutoColorActivity.this, EditAutoColorActivity.class), 1);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		pb = (ProgressBar) findViewById(R.id.progress);

		new GetAutoColors().execute();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==1){
			new SetAutoColors().execute();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		AutoColorItem item = mItems.get(position);
		
		Intent intent = new Intent(AutoColorActivity.this, EditAutoColorActivity.class);
		intent.putExtra("id", item.id);
		intent.putExtra("keyword", item.keyword);
		intent.putExtra("context", item.context);
		intent.putExtra("color", item.color);
		intent.putExtra("edit", true);
		startActivityForResult(intent, 1);
		super.onListItemClick(l, v, position, id);
	}
	
	class SetAutoColors extends AsyncTask<Void, Boolean, Boolean>{
		@Override
		protected Boolean doInBackground(Void... params) {
			return DataManagement.setAutoColors(AutoColorActivity.this);
		}
		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			// new style?!!
			if (!success) {
				ContentValues values = new ContentValues();
				
				values.put(AutoColor.NEED_UPDATE, 1);				
				getContentResolver().update(MetaUtils.getContentUri(AutoColor.class), values, "", null);
			}
			// old style...
			if(!success){
				Account account = new Account(AutoColorActivity.this);
				account.setNeed_update(1);
			}
		}
	}
	
	class GetAutoColors extends AsyncTask<Void, ArrayList<AutoColorItem>, ArrayList<AutoColorItem>>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}
		@Override
		protected ArrayList<AutoColorItem> doInBackground(Void... arg0) {
			return DataManagement.getAutoColors(AutoColorActivity.this);
		}
		
		@Override
		protected void onPostExecute(ArrayList<AutoColorItem> result) {
			super.onPostExecute(result);
			mItems = result; 
			setListAdapter(new AutoColorAdapter(AutoColorActivity.this, result));
			pb.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.auto_color_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.add_color:
	            startActivityForResult(new Intent(AutoColorActivity.this, EditAutoColorActivity.class), 1);
	        break;
	    }
	    return true;
	}
}
