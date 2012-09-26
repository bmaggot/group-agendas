package com.groupagendas.groupagenda;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.EventsActivity;
import com.groupagendas.groupagenda.utils.AgendaUtils;

public class C2DMReceiver extends C2DMBaseReceiver {
	private static boolean isChatMessage = false;
	public C2DMReceiver() {
		super(DataManagement.PROJECT_ID);
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
		String data = "";
		if (receiveIntent.getStringExtra("message") != null) {
			data = receiveIntent.getStringExtra("message");
		}
		String rel_id = null;
		String type = null;;
		String isNative = null;
		if (receiveIntent.hasExtra("rel_obj") && receiveIntent.getStringExtra("rel_obj").equals("update")) {
			DataManagement.updateAppData(Integer.parseInt(data));
			
		}else if (receiveIntent.hasExtra("rel_id") && receiveIntent.getStringExtra("rel_id") != "" && data != null) {
			if(receiveIntent.hasExtra("rel_obj") && receiveIntent.getStringExtra("rel_obj").equals("ch")){
				isChatMessage = true;
			} else {
				isChatMessage = false;
			}
			rel_id = receiveIntent.getStringExtra("rel_id");
			Log.e("C2DMReceiver", "C2DMReceiver: " + data);
			showNotification(this, "Group Agenda", "Group Agenda", data, 17301620, "", rel_id, type, isNative);
		}
	}

	public static void showNotification(Context context, String tickerText, String title, String text, int icon, String url, String rel_id,
			String type, String isNative) {

		try {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

			mNotificationManager.cancelAll();

			Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());

			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_AUTO_CANCEL;

			Intent notificationIntent;
			com.groupagendas.groupagenda.events.Event event = DataManagement.getInstance(context).getEventFromLocalDb(Integer.parseInt(rel_id));
			if(isChatMessage){
				notificationIntent = new Intent(context, ChatMessageActivity.class);
				notificationIntent.putExtra("event_id", event.event_id);
				notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				notification.setLatestEventInfo(context, title, text, contentIntent);

				mNotificationManager.notify(1, notification);
			} else {
				notificationIntent = new Intent(context, EventsActivity.class);
				NavbarActivity.showInvites = true;
				if (getEventById(rel_id, context) == null) {
					DataManagement.getInstance(context).getEventsFromRemoteDb("");
				}
				if (event != null) {
					notificationIntent.putExtra("event_id", event.event_id);
					notificationIntent.putExtra("type", event.type);
					notificationIntent.putExtra("isNative", event.isNative);

					notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

					notification.setLatestEventInfo(context, title, text, contentIntent);

					mNotificationManager.notify(1, notification);
				} else {
					System.out.println("Bad rel_id!");
				}
			}

		} catch (Exception ex) {
			Reporter.reportError(C2DMReceiver.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		}
	}

	public static com.groupagendas.groupagenda.events.Event getEventById(String rel_id, Context context) {
		ArrayList<com.groupagendas.groupagenda.events.Event> allEvents = AgendaUtils.getActualEvents(context,
				DataManagement.getInstance(context).getEventsFromLocalDb());
		for (com.groupagendas.groupagenda.events.Event event : allEvents) {
			if (event.event_id == Integer.parseInt(rel_id)) {
				return event;
			}
		}
		return null;
	}
}
