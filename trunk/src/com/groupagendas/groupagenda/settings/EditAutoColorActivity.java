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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.events.ColorsAdapter;
import com.groupagendas.groupagenda.utils.DrawingUtils;

public class EditAutoColorActivity extends Activity {
	
	private static final int COLOURED_BUBBLE_SIZE = 20;

	private ProgressBar pb;
	
	private ImageView colorView;
	private EditText keywordView;
	
	private AutoColorItem mItem = new AutoColorItem();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_auto_color);
		
		pb = (ProgressBar) findViewById(R.id.progress);
		colorView = (ImageView) findViewById(R.id.color);
		keywordView = (EditText) findViewById(R.id.keyword);
		
		mItem.id = getIntent().getIntExtra("id", 0);
		mItem.context = getIntent().getStringExtra("context");
		mItem.color = getIntent().getStringExtra("color");
		if ((mItem.color != null) && !mItem.color.equals("")) {
			colorView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(EditAutoColorActivity.this, COLOURED_BUBBLE_SIZE, mItem.color, true));
		}
		
		
		final String[] colorsValues = getResources().getStringArray(R.array.colors_values);
		colorView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(EditAutoColorActivity.this);
				dialog.setContentView(R.layout.list_dialog);
				dialog.setTitle(R.string.choose_color);

				GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
				gridview.setAdapter(new ColorsAdapter(EditAutoColorActivity.this, colorsValues));

				gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mItem.color = (colorsValues[position]);
					colorView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(EditAutoColorActivity.this, COLOURED_BUBBLE_SIZE, mItem.color, true));
					dialog.dismiss();
				}
				});
				dialog.show();
			}
		});
		
//		Button noColor = (Button) findViewById(R.id.no_color);
//		noColor.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				mItem.color = "";
//				colorView.setImageBitmap(null);
//			}
//		});
		
//		GridView gridview = (GridView) findViewById(R.id.gridview);
//		gridview.setAdapter(new ColorsAdapter(EditAutoColorActivity.this, colorsValues));
//		gridview.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//				mItem.color = colorsValues[position];
//				setImage(mItem.color);
//			}
//		});
		
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