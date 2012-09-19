package com.groupagendas.groupagenda.alarm;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.OfflineData;
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
				try{
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/alarms_dismiss");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("token", new StringBody(Data.getToken()));
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					HttpResponse rp = hc.execute(post);
				} else {
					OfflineData uplooad = new OfflineData("mobile/alarms_dismiss", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
				} catch (Exception e){
					
				}
			}
		});
	}
//	@Override
//	public void onBackPressed() {
//		Toast.makeText(this, "Suck My Balls", Toast.LENGTH_LONG).show();
//	}

}
