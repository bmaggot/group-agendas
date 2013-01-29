package com.groupagendas.groupagenda.alarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.groupagendas.groupagenda.utils.Utils;

public class AlarmReceiver extends BroadcastReceiver {
	Handler handler = new Handler();

	@Override
	public void onReceive(Context context, Intent intent) {
		int eventId = intent.getIntExtra("eventId", 0);
		int alarmId = intent.getIntExtra("alarmId", 0);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Group Agendas");
		wl.acquire();
		Intent i = new Intent(context, AlarmActivity.class);
		i.putExtra("event_id", eventId);
		i.putExtra("alarm_id", alarmId);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		wl.release();
	}

	public void SetAlarm(Context context, long time, int event_id) {
		if(time > 0){
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, AlarmReceiver.class);
			i.putExtra("eventId", event_id);
			i.putExtra("alarmId", (int) Utils.millisToUnixTimestamp(time));
			PendingIntent pi = PendingIntent.getBroadcast(context, (int) Utils.millisToUnixTimestamp(time) , i, PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, time, pi);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			Log.e("Alarm set", calendar.getTime().toString());
			Log.e("Alarm set", time +" : "+ (int) Utils.millisToUnixTimestamp(time));
		}
	}
	
    public void CancelAlarm(Context context, int alarmId)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}