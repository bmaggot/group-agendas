package com.groupagendas.groupagenda.alarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;

public class AlarmActivity extends Activity{

	private TextView alarmTitle;
	private TextView eventTime;
	private Button snooze;
	private Button dismiss;
	private Event event;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int eventId = getIntent().getIntExtra("event_id", 0);
		this.event = DataManagement.getInstance(this).getEventFromDb(eventId);
		this.setContentView(R.layout.alarm_dialog);
		alarmTitle = (TextView) findViewById(R.id.alarm_title);
		alarmTitle.setText(event.title);
		eventTime = (TextView) findViewById(R.id.alarm_time);
		eventTime.setText(event.my_time_start);
		
		snooze = (Button) findViewById(R.id.alarm_sleep);
		snooze.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		
		dismiss = (Button) findViewById(R.id.alarm_dismiss);
		dismiss.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
	}
//	@Override
//	public void onBackPressed() {
//		Toast.makeText(this, "Suck My Balls", Toast.LENGTH_LONG).show();
//	}

}
