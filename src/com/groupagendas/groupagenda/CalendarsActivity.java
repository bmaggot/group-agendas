package com.groupagendas.groupagenda;

import com.groupagendas.groupagenda.data.DataManagement;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CalendarsActivity extends Activity {
	private SharedPreferences prefs;
	private Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendars);
		
		DataManagement dm = DataManagement.getInstance(this);
		
		prefs = getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		
		// GroupAgenda
		boolean isAgenda = prefs.getBoolean("isAgenda", true);
		ToggleButton toggleAgenda = (ToggleButton) findViewById(R.id.toggleAgenda);
		toggleAgenda.setChecked(isAgenda);
		toggleAgenda.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        editor.putBoolean("isAgenda", isChecked);
		        editor.commit();
		    }
		});
		
		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		
		LinearLayout container = (LinearLayout) findViewById(R.id.calendarContainer);
		
		

		Cursor cursor = dm.getNativeCalendars();
		
		while(!cursor.isAfterLast()){
			
			final String _id = cursor.getString(0);
			final String displayName = cursor.getString(1);
			
			
			final View view=layoutInflater.inflate(R.layout.calendar_item, container, false);
			
			TextView nameView = (TextView) view.findViewById(R.id.calendarName);
			nameView.setText(displayName);
			
			boolean isNative = prefs.getBoolean("isNative_"+_id, false);
			final ToggleButton toggleButton = (ToggleButton) view.findViewById(R.id.toggle);
			toggleButton.setChecked(isNative);
			toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			    @Override
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			        editor.putBoolean("isNative_"+_id, isChecked);
			        editor.commit();
			    }
			});
			
			container.addView(view);
			cursor.moveToNext();
		}
		cursor.close();
	}
}
