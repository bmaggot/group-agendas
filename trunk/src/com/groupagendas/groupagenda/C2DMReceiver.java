package com.groupagendas.groupagenda;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsActivity;

public class C2DMReceiver extends C2DMBaseReceiver {
	private static boolean isChatMessage = false;
	public static boolean chatMessagesWindowUpdated = false;

	public C2DMReceiver() {
		super(DataManagement.PROJECT_ID);
	}

	@Override
	public void onRegistered(Context context, String registration) {
		Log.w("onRegistered", registration);

		System.out.println("onRegistered: " + registration);

		Account accout = new Account(context);
		accout.setPushId(registration);
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
		Account acc = new Account(context);
		DataManagement.synchronizeWithServer(context, null, acc.getLatestUpdateUnixTimestamp());
		String data = "";
		if (receiveIntent.getStringExtra("message") != null && !receiveIntent.getStringExtra("message").equals("")
				&& !receiveIntent.getStringExtra("message").equals("^[A-Z][a-z]*Self$")) {
			data = receiveIntent.getStringExtra("message");
		}
		String rel_id = null;
		String type = null;
		String isNative = null;
		if (receiveIntent.hasExtra("rel_id") && receiveIntent.getStringExtra("rel_id") != "") {
			rel_id = receiveIntent.getStringExtra("rel_id");
			if (receiveIntent.hasExtra("rel_obj") && receiveIntent.getStringExtra("rel_obj").equals("ch")) {
				isChatMessage = true;
				ChatManagement.getChatMessagesForEventFromRemoteDb(Integer.parseInt(rel_id), context, true);
				Intent intent = new Intent("refreshMessagesList" + rel_id);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			} else {
				isChatMessage = false;
			}
			Log.e("C2DMReceiver", "C2DMReceiver: " + data);
			if (!chatMessagesWindowUpdated) {
				chatMessagesWindowUpdated = false;
				showNotification(this, "Group Agenda", "Group Agenda", data, 17301620, "", rel_id, type, isNative);
			}
		}
	}

	public static void showNotification(Context context, String tickerText, String title, String text, int icon, String url, String rel_id,
			String type, String isNative) {

		try {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

			Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());

			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_AUTO_CANCEL;

			Intent notificationIntent;
			Event event = EventManagement.getEventFromLocalDb(context, Integer.parseInt(rel_id), EventManagement.ID_EXTERNAL);
			if (isChatMessage && event != null) {
				notificationIntent = new Intent(context, ChatMessageActivity.class);
				notificationIntent.putExtra("event_id", event.getEvent_id());
				C2DMReceiver.notifyNotification(mNotificationManager, notification, notificationIntent, context, text, title);
			} else {
				notificationIntent = new Intent(context, EventsActivity.class);
				NavbarActivity.showInvites = true;
				if (event == null) {
					if(EventManagement.getEventByIdFromRemoteDb(context, rel_id)){
						event = EventManagement.getEventFromLocalDb(context, Long.parseLong(rel_id), EventManagement.ID_EXTERNAL);
					}
				}
				if (event != null) {
					notificationIntent.putExtra("event_id", event.getEvent_id());
					notificationIntent.putExtra("type", event.getType());
					notificationIntent.putExtra("isNative", event.isNative());

					C2DMReceiver.notifyNotification(mNotificationManager, notification, notificationIntent, context, text, title);
				}
			}

		} catch (Exception ex) {
			Reporter.reportError(C2DMReceiver.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
	}

	public static void notifyNotification(NotificationManager notificationManager, Notification notification, Intent notificationIntent,
			Context context, String text, String title) {
		if (!text.equals("")) {
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(context, title, text, contentIntent);

			notificationManager.notify(1, notification);
		}
	}
}
