package com.groupagendas.groupagenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver {

	Handler handler = new Handler();
	DataManagement dm;

	@Override
	public void onReceive(Context context, Intent intent) {
		// Debug.waitForDebugger();
		dm = DataManagement.getInstance(context);
		System.out.println("DataManagement" + dm);
		int eventId = intent.getIntExtra("eventId", 0);
		Event event = dm.getEventFromDb(eventId);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Group Agendas");
		wl.acquire();
		Intent i = new Intent(context, AlarmActivity.class);
		i.putExtra("event_id", eventId);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		wl.release();
	}

	public void SetAlarm(Context context, long time, Event event) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmReceiver.class);
		i.putExtra("eventId", event.event_id);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.set(AlarmManager.RTC_WAKEUP, time, pi);
		System.out.println(event.title + "alarm set");
	}

}
