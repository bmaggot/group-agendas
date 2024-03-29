package com.groupagendas.groupagenda.alarm;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.groupagendas.groupagenda.LoadProgressHook;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData.AlarmsMetaData;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.CharsetUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class AlarmsManagement {
	
	private static final String TOKEN = "token";
	private static final String EVENT_ID = "event_id";
	private static final String ALARM_1_TIMESTAMP = "alarms[0][timestamp]";
	private static final String ALARM_1_OFFSET = "alarms[0][offset]";
	private static final String ALARM_2_TIMESTAMP = "alarms[1][timestamp]";
	private static final String ALARM_2_OFFSET = "alarms[1][offset]";
	private static final String ALARM_3_TIMESTAMP = "alarms[2][timestamp]";
	private static final String ALARM_3_OFFSET = "alarms[2][offset]";
	private static final String SUCCESS = "success";
	
	public static void syncAlarms(Context context, ArrayList<Alarm> newAlarms, LoadProgressHook lph) {
		ArrayList<Alarm> oldAlarms = getAllAlarmsFromLocalDB(context);
		ArrayList<Alarm> goodOldAlarms = new ArrayList<Alarm>();
		
		for(Alarm alarm : oldAlarms){
			boolean found = false;
			for(Alarm alarm2 : newAlarms){
				if(alarm.getAlarm_id().contentEquals(alarm2.getAlarm_id())){
					goodOldAlarms.add(alarm2);
					found = true;
				}
			}
			if(!found){
				Log.e("Alarm canceled", alarm.getAlarm_id());
				if(NavbarActivity.alarmReceiver == null){
					NavbarActivity.refreshAlarmReceiver();
				}
				NavbarActivity.alarmReceiver.CancelAlarm(context, (int) Utils.millisToUnixTimestamp(alarm.getAlarmTimestamp()));
				context.getContentResolver().delete(EventsProvider.EMetaData.AlarmsMetaData.CONTENT_URI, EventsProvider.EMetaData.AlarmsMetaData.ALARM_ID + "='" + alarm.getEventId() + "_" + alarm.getAlarmTimestamp() + "'", null);
			}
		}
		newAlarms.removeAll(goodOldAlarms);
		final int l = newAlarms.size();
		if (lph != null)
			lph.publish(0, l);
		for (int i = 0; i < l; i++) {
			Alarm alarm = newAlarms.get(i);
			if (!alarm.isSent()) {
				NavbarActivity.alarmReceiver.SetAlarm(context, alarm.getAlarmTimestamp(), alarm.getEventId());
			}
			context.getContentResolver().insert(EventsProvider.EMetaData.AlarmsMetaData.CONTENT_URI, createContentValuesFromAlarms(alarm));
			if (lph != null)
				lph.publish(i + 1);
		}
	}
	
	public static ContentValues createContentValuesFromAlarms(Alarm alarm){
		ContentValues contentValues = new ContentValues();
		contentValues.put(AlarmsMetaData.ALARM_ID, alarm.getAlarm_id());
		contentValues.put(AlarmsMetaData.USER_ID, alarm.getUserId());
		contentValues.put(AlarmsMetaData.EVENT_ID, alarm.getEventId());
		contentValues.put(AlarmsMetaData.TIMESTAMP, alarm.getAlarmTimestamp());
		contentValues.put(AlarmsMetaData.OFFSET, alarm.getOffset());
		contentValues.put(AlarmsMetaData.SENT, alarm.isSent()? 1 : 0 );
		return contentValues;
	}
	
	public static Alarm createAlarmFromCursor(Cursor cursor){
		Alarm alarm = new Alarm();
		alarm.setUserId(cursor.getInt(cursor.getColumnIndex(EventsProvider.EMetaData.AlarmsMetaData.USER_ID)));
		alarm.setEventId(cursor.getInt(cursor.getColumnIndex(EventsProvider.EMetaData.AlarmsMetaData.EVENT_ID)));
		alarm.setAlarmTimestamp(cursor.getLong(cursor.getColumnIndex(EventsProvider.EMetaData.AlarmsMetaData.TIMESTAMP)));
		alarm.setOffset(cursor.getLong(cursor.getColumnIndex(EventsProvider.EMetaData.AlarmsMetaData.OFFSET)));
		alarm.setSent(cursor.getString(cursor.getColumnIndex(EventsProvider.EMetaData.AlarmsMetaData.SENT)).equals("1"));
		alarm.setAlarm_id(alarm.getEventId() + "_" + alarm.getAlarmTimestamp());
		return alarm;
	}
	
	public static ArrayList<Alarm> getAllAlarmsFromLocalDB(Context context){
		Cursor cursor = context.getContentResolver().query(EventsProvider.EMetaData.AlarmsMetaData.CONTENT_URI, null, null, null, null);
		ArrayList<Alarm> alarms = new ArrayList<Alarm>(cursor.getCount());
		while (cursor.moveToNext()) {
			alarms.add(createAlarmFromCursor(cursor));
		}
		cursor.close();
		return alarms;
	}
	
	public static boolean setAlarmsForEvent(Context context, long alarm1, long alarm1offset, long alarm2, long alarm2offset, long alarm3, long alarm3offset, int event_id){
		boolean success = false;
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/alarms/set");
			
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			CharsetUtils.addAllParts(reqEntity, TOKEN, Data.getToken(context), EVENT_ID, event_id,
					ALARM_1_TIMESTAMP, Utils.millisToUnixTimestamp(alarm1),
					ALARM_1_OFFSET, Utils.millisToUnixTimestamp(alarm1offset),
					ALARM_2_TIMESTAMP, Utils.millisToUnixTimestamp(alarm2),
					ALARM_2_OFFSET, Utils.millisToUnixTimestamp(alarm2offset),
					ALARM_3_TIMESTAMP, Utils.millisToUnixTimestamp(alarm3),
					ALARM_3_OFFSET, Utils.millisToUnixTimestamp(alarm3offset));
	
			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					success = new JSONObject(resp).optBoolean(SUCCESS);
					if (success) {
						if (NavbarActivity.alarmReceiver == null) {
							NavbarActivity.refreshAlarmReceiver();
						}
						NavbarActivity.alarmReceiver.SetAlarm(context, alarm1, event_id);
						NavbarActivity.alarmReceiver.SetAlarm(context, alarm2, event_id);
						NavbarActivity.alarmReceiver.SetAlarm(context, alarm3, event_id);
					}
				}
			} else {
				Log.e("Get Alarms - status", StringValueUtils.valueOf(rp.getStatusLine().getStatusCode()));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return success;
	}
}
