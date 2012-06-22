package com.groupagendas.groupagenda.settings;

import android.app.Activity;
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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.events.ColorsAdapter;

public class EditAutoColorActivity extends Activity {
	
	private ProgressBar pb;
	
	private ImageView colorView;
	private EditText keywordView;
	
	private AutoColorItem mItem = new AutoColorItem();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_auto_color);
		
		pb = (ProgressBar) findViewById(R.id.progress);
		
		mItem.id = getIntent().getIntExtra("id", 0);
		
		mItem.context = getIntent().getStringExtra("context");
		
		colorView = (ImageView) findViewById(R.id.color);
		mItem.color = getIntent().getStringExtra("color");
		if(mItem.color != null){
			String colorStr = "calendarbubble_"+mItem.color+"_";
			int colorId = getResources().getIdentifier(colorStr, "drawable", "com.groupagendas.groupagenda");
			colorView.setImageResource(colorId);
		}
		
		Button noColor = (Button) findViewById(R.id.no_color);
		noColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mItem.color = "";
				colorView.setImageBitmap(null);
			}
		});
		
		final String[] colorsValues = getResources().getStringArray(R.array.colors_values);
		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new ColorsAdapter(EditAutoColorActivity.this, colorsValues));
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				mItem.color = colorsValues[position];
				setImage(mItem.color);
			}
		});
		
		keywordView = (EditText) findViewById(R.id.keyword);
		mItem.keyword = getIntent().getStringExtra("keyword");
		keywordView.setText(mItem.keyword);
		
		Button saveButton  = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mItem.keyword = keywordView.getText().toString();
				new SaveAutoColor().execute();
			}
		});
	}
	
	private void setImage(String color){
		String nameColor = "calendarbubble_"+color+"_";
		int image = getResources().getIdentifier(nameColor, "drawable", "com.groupagendas.groupagenda");
		colorView.setImageResource(image);
	}
	
	class SaveAutoColor extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			ContentValues values = new ContentValues();
			values.put(AccountProvider.AMetaData.AutocolorMetaData.COLOR, mItem.color);
			values.put(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD, keywordView.getText().toString());
			
			if(getIntent().getBooleanExtra("edit", false)){
				Uri uri = Uri.parse(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI+"/"+mItem.id);
				getContentResolver().update(uri, values, null, null);
			}else{
				values.put(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT, "title");
				getContentResolver().insert(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, values);
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
	    inflater.inflate(R.menu.auto_color_edit_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.delete_color:
	        	Uri uri = Uri.parse(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI+"/"+mItem.id);
	        	getContentResolver().delete(uri, null, null);
	        	
	        	Intent intent = new Intent();
		        setResult(1,intent);
				finish();
	        break;
	    }
	    return true;
	}
}