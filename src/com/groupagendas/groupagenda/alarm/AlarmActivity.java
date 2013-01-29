package com.groupagendas.groupagenda.alarm;

import java.io.IOException;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.groupagendas.groupagenda.NavbarActivity;
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
	private int eventId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		final MediaPlayer mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(this, alert);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int volumen = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

		if (volumen != 0) {
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mMediaPlayer.setLooping(true);
			try {
				mMediaPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mMediaPlayer.start();
		}
		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		final long[] pattern = { 0, 300, 500 };
		vibrator.vibrate(pattern, 0);
		super.onCreate(savedInstanceState);
		eventId = getIntent().getIntExtra("event_id", 0);
		final int alarmId = getIntent().getIntExtra("alarm_id", 0);
		this.event = EventManagement.getEventFromLocalDb(this, eventId, EventManagement.ID_EXTERNAL);
		this.setContentView(R.layout.alarm_dialog);
		alarmTitle = (TextView) findViewById(R.id.alarm_title);
		alarmTitle.setText(event.getTitle());
		eventTime = (TextView) findViewById(R.id.alarm_time);
		eventTime.setText(Utils.formatCalendar(event.getStartCalendar(), Utils.DATE_FORMAT_LONG));

		snooze = (Button) findViewById(R.id.alarm_sleep);
		snooze.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(NavbarActivity.alarmReceiver == null){
					NavbarActivity.refreshAlarmReceiver();
				}
				NavbarActivity.alarmReceiver.CancelAlarm(getApplicationContext(), alarmId);
				mMediaPlayer.release();
				vibrator.cancel();
				Calendar tmp = Calendar.getInstance();
				tmp.add(Calendar.MINUTE, snoozeDurationInMins);
				NavbarActivity.alarmReceiver.SetAlarm(AlarmActivity.this, tmp.getTimeInMillis(), event.getEvent_id());
				finish();
			}
		});

		dismiss = (Button) findViewById(R.id.alarm_dismiss);
		dismiss.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(NavbarActivity.alarmReceiver == null){
					NavbarActivity.refreshAlarmReceiver();
				}
				NavbarActivity.alarmReceiver.CancelAlarm(getApplicationContext(), alarmId);
				mMediaPlayer.release();
				vibrator.cancel();
				new DismissAlarm().execute();
				finish();
			}
		});
	}

	private class DismissAlarm extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				WebService webService = new WebService(getApplicationContext());
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/alarms_dismiss");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("token", new StringBody(Data.getToken(getApplicationContext()), Charset.forName("UTF-8")));
				reqEntity.addPart("event_id", new StringBody(eventId + "", Charset.forName("UTF-8")));
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					HttpResponse rp = webService.getResponseFromHttpPost(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (!success) {
								Log.e("AlarmActivity", "smth wrong in DismissAlarm");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}
}
