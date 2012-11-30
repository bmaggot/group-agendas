package com.groupagendas.groupagenda;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;

public class C2DMReceiver extends C2DMBaseReceiver {
	private static boolean isChatMessage;
	public static boolean chatMessagesWindowUpdated;
	public static long sessionToken;
	public String last_queue_token = "";
	public static String RESUBSCRIBE = "resubscribe";
	public static String DEFAULT_QUEUE_TOKEN = "666";
	public static String ACTION = "action";
	public static String SESSION = "session";
	public static String QUEUE_TOKEN = "queue_token";
	public static String MESSAGE = "message";
	public static String REL_OBJ = "rel_obj";
	public static String REL_ID = "rel_id";
	public static String CHAT = "ch";
	public static String CHAT_LAST_VIEW = "ch_last_view";
	public static String EVENT = "event";
	public static String CONTACT = "contact";
	public static String REFRESH_MESSAGES_LIST = "refreshMessagesList";
	public static String REFRESH_CHAT_THREAD_LIST = "refreshChatThreadList";
	public static String REFRESH_CHAT_MESSAGES_BADGE = "refreshChatMessagesBadge";
	public static String REFRESH_EVENT_EDIT_ACTIVITY = "refreshEventEditActivity";

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
		isChatMessage = false;
		chatMessagesWindowUpdated = false;
		if (receiveIntent.hasExtra(ACTION) && receiveIntent.getStringExtra(ACTION).equals(RESUBSCRIBE)) {
			DataManagement.getInstance(context).registerPhone();
		} else if (receiveIntent.hasExtra(ACTION) && receiveIntent.getStringExtra(ACTION).equals(CHAT_LAST_VIEW)
				&& receiveIntent.hasExtra(REL_ID) && !receiveIntent.getStringExtra(REL_ID).equals("")) {
			EventManagement.resetEventsNewMessageCount(context, Integer.parseInt(receiveIntent.getStringExtra(REL_ID)));
		} else {
			boolean doDataDelta = true;
			if (receiveIntent.hasExtra(SESSION) && receiveIntent.getStringExtra(SESSION).equals(new Account(context).getSessionId())) {
				doDataDelta = false;
			} else {
				if (receiveIntent.hasExtra(QUEUE_TOKEN)
						&& receiveIntent.getStringExtra(QUEUE_TOKEN).equals(String.valueOf(last_queue_token))) {
					doDataDelta = false;
				} else {
					if (receiveIntent.hasExtra(QUEUE_TOKEN)) {
						last_queue_token = receiveIntent.getStringExtra(QUEUE_TOKEN);
					} else {
						last_queue_token = DEFAULT_QUEUE_TOKEN;
					}
					String data = "";
					if (receiveIntent.hasExtra(MESSAGE) && !receiveIntent.getStringExtra(MESSAGE).equals("[A-Z]*[a-z]*Self")) {
						data = receiveIntent.getStringExtra(MESSAGE);
					}
					Log.e("C2DMReceiver: ", "NEW 	PUSH	" + data);
					String rel_id = null;
					if (receiveIntent.hasExtra(REL_OBJ) && receiveIntent.getStringExtra(REL_OBJ).equals(CHAT)
							&& receiveIntent.hasExtra(REL_ID) && !receiveIntent.getStringExtra(REL_ID).equals("")) {
						isChatMessage = true;
						doDataDelta = false;
						rel_id = receiveIntent.getStringExtra(REL_ID);

						Uri uri = EventsProvider.EMetaData.EventsMetaData.UPDATE_EVENTS_NEW_MESSAGES_COUNT;
						ContentValues cv = new ContentValues();
						cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, Integer.parseInt(rel_id));
						context.getContentResolver().update(uri, cv, null, null);
						
						Event event = EventManagement.getEventFromLocalDb(context, Integer.parseInt(rel_id), EventManagement.ID_EXTERNAL);
						if(event != null){
							event.setMessage_count(event.getMessage_count() + 1);
							EventManagement.updateEventInLocalDb(context, event);
	
							if (EventManagement.getEventFromLocalDb(context, Integer.parseInt(rel_id), EventManagement.ID_EXTERNAL) != null) {
								refreshChatMessages(rel_id, context);
								showNotification(context, data, rel_id);
							} else {
								Account account = new Account(context);
								DataManagement.synchronizeWithServer(context, null, account.getLatestUpdateUnixTimestamp());
								refreshChatMessages(rel_id, context);
							}
						}
					} else if (receiveIntent.hasExtra(REL_ID) && !receiveIntent.getStringExtra(REL_ID).equals("")) {
						rel_id = receiveIntent.getStringExtra(REL_ID);
						if (receiveIntent.hasExtra(REL_OBJ)
								&& (receiveIntent.getStringExtra(REL_OBJ).equals(EVENT) || receiveIntent.getStringExtra(REL_OBJ).equals(
										CONTACT))) {
							isChatMessage = false;
						}
						showNotification(context, data, rel_id);
					}
					if (doDataDelta) {
						Account account = new Account(context);
						DataManagement.synchronizeWithServer(context, null, account.getLatestUpdateUnixTimestamp());
					}
				}
			}
		}
	}

	public static void refreshChatMessages(String rel_id, Context context) {
		ChatManagement.getChatMessagesForEventFromRemoteDb(Integer.parseInt(rel_id), context, false,
				ChatManagement.getLastMessageTimeStamp(context, Integer.parseInt(rel_id)));
		
		ContentValues cv = new ContentValues();
		cv.put(EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS, ChatManagement.getLastMessageTimeStamp(context, Integer.parseInt(rel_id)));
		Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		String where = EventsProvider.EMetaData.EventsMetaData.E_ID + "=" + Integer.parseInt(rel_id);
		context.getContentResolver().update(uri, cv, where, null);
		
		Intent intent = new Intent(REFRESH_MESSAGES_LIST + rel_id);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		Intent intent2 = new Intent(REFRESH_CHAT_THREAD_LIST);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
		Intent intent3 = new Intent(REFRESH_CHAT_MESSAGES_BADGE);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
	}

	public static void showNotification(Context context, String data, String rel_id) {
		showNotification(context, "Group Agenda", "Group Agenda", data, 17301620, "", rel_id);
	}

	public static void showNotification(Context context, String tickerText, String title, String text, int icon, String url, String rel_id) {

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
					if (EventManagement.getEventByIdFromRemoteDb(context, rel_id)) {
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
			Reporter.reportError(context, C2DMReceiver.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), ex.getMessage());
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
