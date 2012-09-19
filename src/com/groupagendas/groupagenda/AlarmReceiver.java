package com.groupagendas.groupagenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import com.groupagendas.groupagenda.events.Event;

public class AlarmReceiver extends BroadcastReceiver {

    Handler handler = new Handler();
    private Event event;

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("alrm");
//		int eventId = intent.getIntExtra("eventId", 0);
//		Event event = DataManagement.getInstance(Data.getmContext()).getEventFromDb(eventId);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        wl.acquire();
        Toast.makeText(context, "google", Toast.LENGTH_LONG).show();
        wl.release();
//		Dialog dia =  new AlarmDialog(context, event);
//		dia.show();
	}

	public void SetAlarm(Context context, long time, Event event) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmReceiver.class);
//		i.putExtra("eventId", event.event_id);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.set(AlarmManager.RTC_WAKEUP, time, pi);
		System.out.println(event.title + "alarm set");
	}

}
