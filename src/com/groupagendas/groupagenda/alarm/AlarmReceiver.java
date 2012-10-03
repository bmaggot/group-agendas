package com.groupagendas.groupagenda.alarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import com.groupagendas.groupagenda.events.Event;

public class AlarmReceiver extends BroadcastReceiver {
	Handler handler = new Handler();

	@Override
	public void onReceive(Context context, Intent intent) {
		int eventId = intent.getIntExtra("eventId", 0);
		int alarmNR = intent.getIntExtra("alarmNr", 0);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Group Agendas");
		wl.acquire();
		Intent i = new Intent(context, AlarmActivity.class);
		i.putExtra("event_id", eventId);
		i.putExtra("alarmNr", alarmNR);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		wl.release();
	}

	public void SetAlarm(Context context, long time, Event event, int alarmNumber) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmReceiver.class);
		i.putExtra("eventId", event.getEvent_id());
		i.putExtra("alarmNr", alarmNumber);
		PendingIntent pi = PendingIntent.getBroadcast(context, event.getEvent_id() + alarmNumber, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, time, pi);
		Calendar tmp = Calendar.getInstance();
		tmp.setTimeInMillis(time);
		
	}
	
    public void CancelAlarm(Context context, int alarmId)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}

/*





*/