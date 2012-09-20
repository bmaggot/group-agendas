package com.groupagendas.groupagenda.alarm;

import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.OfflineData;
import com.groupagendas.groupagenda.events.Event;

public class AlarmActivity extends Activity {

	private TextView alarmTitle;
	private TextView eventTime;
	private Button snooze;
	private Button dismiss;
	private Event event;
	private final int snoozeDurationInMins = 10;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		final long[] pattern = { 0, 300, 500 };
		vibrator.vibrate(pattern, 0);
		super.onCreate(savedInstanceState);
		final int eventId = getIntent().getIntExtra("event_id", 0);
		final int alarmNR = getIntent().getIntExtra("alarmNr", 0);
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
				cancelAlarm(eventId + alarmNR);
				vibrator.cancel();
				finish();
				AlarmReceiver ar = new AlarmReceiver();
				Calendar tmp = Calendar.getInstance();
				tmp.add(Calendar.MINUTE, snoozeDurationInMins);
				ar.SetAlarm(AlarmActivity.this, tmp.getTimeInMillis(), event, alarmNR);
			}
		});

		dismiss = (Button) findViewById(R.id.alarm_dismiss);
		dismiss.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelAlarm(eventId + alarmNR);
				vibrator.cancel();
				new DismissAlarms().execute();
				finish();
			}
		});
	}

	public void cancelAlarm(int alarmId) {
		AlarmReceiver ar = new AlarmReceiver();
		ar.CancelAlarm(this, alarmId);
	}

	private class DismissAlarms extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/alarms_dismiss");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("token", new StringBody(Data.getToken()));
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					HttpResponse rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (!success) {
								Toast.makeText(AlarmActivity.this, getResources().getString(R.string.alarm_dismiss_all_alarms), Toast.LENGTH_LONG);
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/alarms_dismiss", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {

			}
			return null;
		}
		
	}
}
