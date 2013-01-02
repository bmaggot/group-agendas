package com.groupagendas.groupagenda.alarm;

import java.nio.charset.Charset;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
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
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.Utils;

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
		this.event = EventManagement.getEventFromLocalDb(this, eventId, EventManagement.ID_INTERNAL);
		this.setContentView(R.layout.alarm_dialog);
		alarmTitle = (TextView) findViewById(R.id.alarm_title);
		alarmTitle.setText(event.getTitle());
		eventTime = (TextView) findViewById(R.id.alarm_time);
		eventTime.setText(Utils.formatCalendar(event.getStartCalendar(), Utils.DATE_FORMAT_LONG)); //TODO set format from approporiate class.  DataManagement should not be used for formatting UI strings

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
				WebService webService = new WebService(getApplicationContext());
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/alarms_dismiss");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("token", new StringBody(Data.getToken(getApplicationContext()), Charset.forName("UTF-8")));
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					HttpResponse rp = webService.getResponseFromHttpPost(post);
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
				}
			} catch (Exception e) {

			}
			return null;
		}
		
	}
}
