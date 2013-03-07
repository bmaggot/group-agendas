package com.groupagendas.groupagenda.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.IconsAdapter;
import com.groupagendas.groupagenda.metadata.MetaUtils;
import com.groupagendas.groupagenda.metadata.impl.AutoColorIconMetaData;

public class EditAutoIconActivity extends Activity implements AutoColorIconMetaData {
	
	private ProgressBar pb;
	
	private ImageView iconView;
	private EditText keywordView;
	
	private AutoIconItem mItem = new AutoIconItem();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_auto_icon);
		
		pb = (ProgressBar) findViewById(R.id.progress);
		iconView = (ImageView) findViewById(R.id.icon);
		keywordView = (EditText) findViewById(R.id.keyword);
		
		mItem.id = getIntent().getIntExtra("id", 0);
		mItem.context = getIntent().getStringExtra("context");
		mItem.icon = getIntent().getStringExtra("icon");
		if(mItem.icon != null){
			int iconId = getResources().getIdentifier(mItem.icon, "drawable", "com.groupagendas.groupagenda");
			iconView.setImageResource(iconId);
		}
		
		final String[] iconsValues = getResources().getStringArray(R.array.icons_values);
		iconView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(EditAutoIconActivity.this);
				dialog.setContentView(R.layout.list_dialog);
				dialog.setTitle(R.string.choose_icon);

				GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
				gridview.setAdapter(new IconsAdapter(EditAutoIconActivity.this, iconsValues));

				gridview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						if (iconsValues[position].equals("noicon")) {
							iconView.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
						} else {
							mItem.icon = iconsValues[position];
							int iconId = getResources().getIdentifier(iconsValues[position], "drawable", "com.groupagendas.groupagenda");
							iconView.setImageResource(iconId);
						}
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
		
		mItem.keyword = getIntent().getStringExtra("keyword");
		keywordView.setText(mItem.keyword);
		
		Button saveButton  = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mItem.keyword = keywordView.getText().toString();
				
				
				//TODO review Rokas code
				if(!mItem.keyword.matches("")){
					
					if(mItem.icon != null){
				
					
					new SaveAutoIcon().execute();
					
					}else{
						
						Toast.makeText(EditAutoIconActivity.this, "Please set Icon !", Toast.LENGTH_LONG).show();	
						
					}
				
				}else{
					
						Toast.makeText(EditAutoIconActivity.this, "Please set Title Description!", Toast.LENGTH_LONG).show();
					
					
				}
				
			}
		});
	}
	
	class SaveAutoIcon extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			ContentValues values = new ContentValues();
			values.put(AutoIcon.ICON, mItem.icon);
			values.put(AutoIcon.KEYWORD, keywordView.getText().toString());
			
			if (getIntent().getBooleanExtra("edit", false)) {
				Uri uri = Uri.parse(MetaUtils.getContentUri(AutoIcon.class) + "/" + mItem.id);
				getContentResolver().update(uri, values, null, null);
			} else {
				values.put(AutoIcon.CONTEXT, "title");
				getContentResolver().insert(MetaUtils.getContentUri(AutoIcon.class), values);
			}

			Intent intent = new Intent();
	        setResult(1,intent);
			finish();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			pb.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.auto_icon_edit_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.delete_icon:
	        	Uri uri = Uri.parse(MetaUtils.getContentUri(AutoIcon.class) + "/" + mItem.id);
	        	getContentResolver().delete(uri, null, null);
	        	
	        	Intent intent = new Intent();
		        setResult(1,intent);
				finish();
	        break;
	    }
	    return true;
	}
}
