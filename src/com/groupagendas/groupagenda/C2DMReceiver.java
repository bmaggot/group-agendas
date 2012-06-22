package com.groupagendas.groupagenda;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.utils.AgendaUtils;

public class C2DMReceiver extends C2DMBaseReceiver {
	public C2DMReceiver() {
		super("group.agenda.c2dm@gmail.com");
	}

	@Override
	public void onRegistered(Context context, String registration) {
		Log.w("onRegistered", registration);

		System.out.println("onRegistered: " + registration);

		DataManagement.sendPushIdToServer(this, registration);
	}

	@Override
	public void onUnregistered(Context context) {
		Log.w("onUnregistered", "");
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.w("onError", errorId);
	}

	@Override
	protected void onMessage(Context context, Intent receiveIntent) {
		System.out.println("onMessage()");

		String data = "";
		if (receiveIntent.getStringExtra("message") != null) {
			data = receiveIntent.getStringExtra("message");
		}
		String rel_id = null;
		String type = null;
		String isNative = null;
		if (receiveIntent.hasExtra("rel_id")) {
			rel_id = receiveIntent.getStringExtra("rel_id");
		}
		if (data != null) {
			Log.e("C2DMReceiver", "C2DMReceiver: " + data);

			/*
			 * Intent intent = new Intent(this, Main.class);
			 * intent.putExtra("message", data);
			 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * this.startActivity(intent);
			 */

			showNotification(this, "Group Agenda", "Group Agenda", data,
					17301620, "", rel_id, type, isNative);
		}
	}

	public static void showNotification(Context context, String tickerText,
			String title, String text, int icon, String url, String rel_id,
			String type, String isNative) {

		try {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(ns);

			mNotificationManager.cancelAll();

			Notification notification = new Notification(icon, tickerText,
					System.currentTimeMillis());

			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_AUTO_CANCEL;

			Intent notificationIntent = new Intent(context, EventActivity.class);
			if(getEventById(rel_id, context) == null){
				
			}
			com.groupagendas.groupagenda.events.Event event = DataManagement.getInstance(context).getEventFromDb(Integer.parseInt(rel_id));
			if (event != null) {
				notificationIntent.putExtra("event_id", event.event_id);
				notificationIntent.putExtra("type", event.type);
				notificationIntent.putExtra("isNative", event.isNative);

				notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent contentIntent = PendingIntent.getActivity(
						context, 0, notificationIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				notification.setLatestEventInfo(context, title, text,
						contentIntent);

				mNotificationManager.notify(1, notification);
			} else {
				System.out.println("Bad rel_id!");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static com.groupagendas.groupagenda.events.Event getEventById(
			String rel_id, Context context) {
		ArrayList<com.groupagendas.groupagenda.events.Event> allEvents = AgendaUtils
				.getActualEvents(context, DataManagement.getInstance(context)
						.getEventsFromDb());
		for (com.groupagendas.groupagenda.events.Event event : allEvents) {
			if (event.event_id == Integer.parseInt(rel_id)) {
				return event;
			}
		}
		return null;
	}
}
