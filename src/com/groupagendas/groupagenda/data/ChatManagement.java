package com.groupagendas.groupagenda.data;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.groupagendas.groupagenda.LoadProgressHook;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.chat.ChatProvider;
import com.groupagendas.groupagenda.chat.ChatProvider.CMMetaData;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.CharsetUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ChatManagement {

	public static String TOKEN = "token";
	public static String deleted = "1";
	public static String LASTEST_UPDATED_TIMESTAMP = "latest_update";

	/**
	 * Make ChatMessageObject from JSON.
	 * 
	 * Makes ChatMessageObject from valid JSON object
	 * 
	 * @author justas@mobileapps.lt
	 * @return ChatMessageObject
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static ChatMessageObject makeChatMessageObjectFromJSON(Context context, JSONObject json) {
		ChatMessageObject chatMessage = new ChatMessageObject();
		try {
			chatMessage.setMessageId(json.getInt(CMMetaData.ChatMetaData.M_ID));
			chatMessage.setEventId(json.getInt(CMMetaData.ChatMetaData.E_ID));
			chatMessage.setCreated(Utils.unixTimestampToMilis(json.getLong(CMMetaData.ChatMetaData.CREATED)));
			chatMessage.setUpdated(Utils.unixTimestampToMilis(json.getLong(CMMetaData.ChatMetaData.CREATED)));
			chatMessage.setUserId(json.getInt(CMMetaData.ChatMetaData.USER_ID));
			chatMessage.setMessage(json.getString(CMMetaData.ChatMetaData.MESSAGE));
			String deleted = json.getString(CMMetaData.ChatMetaData.DELETED);
			chatMessage.setDeleted(!deleted.equals("null"));
			if (json.has(CMMetaData.ChatMetaData.FULLNAME)) {
				chatMessage.setFullname(json.getString(CMMetaData.ChatMetaData.FULLNAME));
			}
			chatMessage.setSuccessfully_uploaded(true);
		} catch (JSONException e) {
			Log.e("makeChatMessageObjectFromJSON(JSONObject json)", e.getMessage());
		}
		return chatMessage;
	}

	/**
	 * Make ChatMessage ContentValues object from JSON.
	 * 
	 * Makes ChatMessage ContentValues (insertable to local DB) object from
	 * valid JSON object
	 * 
	 * @author justas@mobileapps.lt
	 * @return ContentValues object
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static ContentValues makeChatMessageObjectContentValueFromJSON(Context context, JSONObject json) {
		ContentValues cv = new ContentValues();
		try {
			cv.put(CMMetaData.ChatMetaData.M_ID, json.getInt(CMMetaData.ChatMetaData.M_ID));
			cv.put(CMMetaData.ChatMetaData.E_ID, json.getInt(CMMetaData.ChatMetaData.E_ID));
			cv.put(CMMetaData.ChatMetaData.CREATED, Utils.unixTimestampToMilis(json.getLong(CMMetaData.ChatMetaData.CREATED)));
			cv.put(CMMetaData.ChatMetaData.USER_ID, json.getInt(CMMetaData.ChatMetaData.USER_ID));
			cv.put(CMMetaData.ChatMetaData.MESSAGE, json.getString(CMMetaData.ChatMetaData.MESSAGE));
			String deleted = json.getString(CMMetaData.ChatMetaData.DELETED);
			cv.put(CMMetaData.ChatMetaData.DELETED, !deleted.equals("null"));
			cv.put(CMMetaData.ChatMetaData.MODIFIED, Utils.unixTimestampToMilis(json.getLong(CMMetaData.ChatMetaData.CREATED)));
			if (json.has(CMMetaData.ChatMetaData.FULLNAME)) {
				cv.put(CMMetaData.ChatMetaData.FULLNAME, json.getString(CMMetaData.ChatMetaData.FULLNAME));
			}
			cv.put(CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED, true);
		} catch (Exception e) {
			Log.e("makeChatMessageObjectContentValueFromJSON(JSONObject json)", e.getMessage());
		}
		return cv;
	}

	/**
	 * Make ChatMessage ContentValues object from ChatMessageObject.
	 * 
	 * Makes ChatMessage ContentValues (insertable to local DB) object from
	 * valid ChatMessageObject object
	 * 
	 * @author justas@mobileapps.lt
	 * @return ContentValues object
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static ContentValues makeContentValuesFromChatMessageObject(ChatMessageObject chatMessage) {
		ContentValues cv = new ContentValues();
		try {
			cv.put(CMMetaData.ChatMetaData.M_ID, chatMessage.getMessageId());
			cv.put(CMMetaData.ChatMetaData.E_ID, chatMessage.getEventId());
			cv.put(CMMetaData.ChatMetaData.CREATED, chatMessage.getCreated());
			cv.put(CMMetaData.ChatMetaData.USER_ID, chatMessage.getUserId());
			cv.put(CMMetaData.ChatMetaData.MESSAGE, chatMessage.getMessage());
			cv.put(CMMetaData.ChatMetaData.DELETED, chatMessage.isDeleted());
			cv.put(CMMetaData.ChatMetaData.MODIFIED, chatMessage.getUpdated());
			cv.put(CMMetaData.ChatMetaData.FULLNAME, chatMessage.getFullname());
			cv.put(CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED, chatMessage.isSuccessfully_uploaded());
		} catch (Exception e) {
			Log.e("makeContentValuesFromChatMessageObject(ChatMessageObject " + chatMessage.getMessageId() + ")", e.getMessage());
		}
		return cv;
	}

	public static ArrayList<ChatMessageObject> getChatMessagesForEventFromLocalDb(Context context, int eventId) {
		// EventManagement.resetEventsNewMessageCount(context, eventId);
		Cursor cur;
		String selection = ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId;
		cur = context.getContentResolver().query(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, null, selection, null, null);
		ArrayList<ChatMessageObject> chatMessages = new ArrayList<ChatMessageObject>(cur.getCount());

		while (cur.moveToNext()) {
			chatMessages.add(makeChatMessageObjectFromCursor(cur));
		}
		cur.close();
		return chatMessages;
	}

	public static ChatMessageObject makeChatMessageObjectFromCursor(Cursor cur) {
		ChatMessageObject message = new ChatMessageObject();
		message.setMessageId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.M_ID)));
		message.setEventId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.E_ID)));
		message.setCreated(cur.getLong(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.CREATED)));
		message.setUserId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.USER_ID)));
		message.setMessage(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.MESSAGE)));
		message.setDeleted(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.DELETED)).equals(ChatManagement.deleted));
		message.setUpdated(cur.getLong(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.MODIFIED)));
		message.setFullname(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.FULLNAME)));

		return message;
	}

	public static ChatMessageObject getLastMessageForEventFromLocalDb(Context context, int eventId) {
		ChatMessageObject chatMessageObject = new ChatMessageObject();
		String selection = ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId + " AND "
				+ ChatProvider.CMMetaData.ChatMetaData.DELETED + "='0'";
		String sortOrder = ChatProvider.CMMetaData.ChatMetaData.CREATED + " DESC ";
		Cursor cur = context.getContentResolver().query(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, null, selection, null, sortOrder);
		if (cur.moveToFirst()) {
			chatMessageObject.setMessageId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.M_ID)));
			chatMessageObject.setEventId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.E_ID)));
			chatMessageObject.setCreated(cur.getLong(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.CREATED)));
			chatMessageObject.setUserId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.USER_ID)));
			chatMessageObject.setMessage(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.MESSAGE)));
			chatMessageObject.setDeleted(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.DELETED)).equals(
					ChatManagement.deleted));
			chatMessageObject.setUpdated(cur.getLong(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.MODIFIED)));
			chatMessageObject.setFullname(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.FULLNAME)));
		}
		cur.close();
		return chatMessageObject;
	}

	/**
	 * Inserts ChatMessage ContentValue object into local DB.
	 * 
	 * @author justas@mobileapps.lt
	 * @return boolean success
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static boolean insertChatMessageContentValueToLocalDb(Context context, ContentValues cv) {
		try {
			context.getContentResolver().insert(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, cv);
			return true;
		} catch (SQLiteException e) {
			Log.e("insertChatMessageContentValueToLocalDb(chat, " + cv.get("message_id") + ")", e.getMessage());
			return false;
		}
	}

	/**
	 * Inserts ChatMessage object into local DB.
	 * 
	 * @author justas@mobileapps.lt
	 * @return boolean success
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static boolean insertChatMessageToLocalDb(Context context, ChatMessageObject chatMessageObject) {
		if (insertChatMessageContentValueToLocalDb(context, makeContentValuesFromChatMessageObject(chatMessageObject))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes ChatMessage by message id from remote DB.
	 * 
	 * @author justas@mobileapps.lt
	 * @return boolean success
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static boolean removeChatMessageFromRemoteDb(Context context, int messageId) {
		boolean success = false;
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			{
				Account account = new Account(context);
				CharsetUtils.addAllParts(reqEntity, TOKEN, Data.getToken(context), "message_id", messageId,
						"session", account.getSessionId());
			}

			post.setEntity(reqEntity);
			HttpResponse rp = null;
			if (DataManagement.networkAvailable) {
				rp = webService.getResponseFromHttpPost(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						// if (success) {
						// } else {
						// }
					}
				}
			}
		} catch (Exception e) {
			Log.e("removeChatMessageFromRemoteDb(Context context, int " + messageId + ")", e.getMessage());
		}
		return success;
	}

	/**
	 * Removes ChatMessage by message id from local DB.
	 * 
	 * @author justas@mobileapps.lt
	 * @return boolean success
	 * @since 2012-10A02
	 * @version 0.1
	 */

	public static boolean removeChatMessageInLocalDb(Context context, int messageId, boolean uploaded) {
		boolean success = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(ChatProvider.CMMetaData.ChatMetaData.DELETED, 1);
			if(!uploaded){
				cv.put(ChatProvider.CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED, 0);
			}
			context.getContentResolver().update(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, cv,
					ChatProvider.CMMetaData.ChatMetaData.M_ID + "=" + messageId, null);
			success = true;
		} catch (Exception e) {
			Log.e("removeChatMessageFromLoacalDB(Context context, int " + messageId + ")", e.getMessage());
		}
		return success;
	}

	public static boolean removeChatMessageFromLocalDb(Context context, int messageId) {
		boolean success = false;
		try {
			context.getContentResolver().delete(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI,
					ChatProvider.CMMetaData.ChatMetaData.M_ID + "=" + messageId, null);
			success = true;
		} catch (Exception e) {
			Log.e("removeChatMessageFromLoacalDB(Context context, int " + messageId + ")", e.getMessage());
		}
		return success;
	}

	/**
	 * Removes ChatMessage by message id from remote and local DB.
	 * 
	 * @author justas@mobileapps.lt
	 * @return boolean success
	 * @since 2012-10T03
	 * @version 0.1
	 */

	public static boolean removeChatMessage(Context context, int messageId) {
		boolean succcess = false;
		if (removeChatMessageFromRemoteDb(context, messageId)) {
			succcess = removeChatMessageInLocalDb(context, messageId, true);
		} else {
			succcess = removeChatMessageInLocalDb(context, messageId, false);
		}
		return succcess;
	}

	public static ChatMessageObject postChatMessage(int eventId, String message, Context context) {
		ChatMessageObject chatMessageObject = new ChatMessageObject();
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_post");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			{
				Account account = new Account(context);
				CharsetUtils.addAllParts(reqEntity, ChatManagement.TOKEN, Data.getToken(context),
						"event_id", eventId, "message", message != null ? message : "",
								"session", account.getSessionId());
			}

			post.setEntity(reqEntity);
			HttpResponse rp = null;
			if (DataManagement.networkAvailable) {
				rp = webService.getResponseFromHttpPost(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						boolean success = object.getBoolean("success");
						if (success) {
							ChatManagement.insertChatMessageContentValueToLocalDb(context,
									ChatManagement.makeChatMessageObjectContentValueFromJSON(context, object.getJSONObject("message")));
							chatMessageObject = ChatManagement.makeChatMessageObjectFromJSON(context, object.getJSONObject("message"));
							ContentValues cv = new ContentValues(1);
							cv.put(EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS,
									Utils.millisToUnixTimestamp(chatMessageObject.getCreated()));
							Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
							String where = EventsProvider.EMetaData.EventsMetaData.E_ID + "=" + eventId;
							Event event = EventManagement.getEventFromLocalDb(context, eventId, EventManagement.ID_EXTERNAL);
							event.setMessage_count(event.getMessage_count() + 1);
							EventManagement.updateEventInLocalDb(context, event);
							context.getContentResolver().update(uri, cv, where, null);
						} else {
							chatMessageObject = null;
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e("postChatMessage(Context context, message " + message + ", event id " + eventId + ")", e.getMessage());
		}
		return chatMessageObject;
	}

	public static ArrayList<ChatMessageObject> getChatMessagesForEventFromRemoteDb(int eventId, Context context, boolean resetMessageCount,
			long lastMessageTimeStamp) {
		boolean success = false;
		WebService webService = new WebService(context);
		ArrayList<ChatMessageObject> chatMessages = new ArrayList<ChatMessageObject>();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");
		Log.e("ChatManagement", "");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		CharsetUtils.addAllParts(reqEntity, ChatManagement.TOKEN, Data.getToken(context), "event_id", eventId);
		if (lastMessageTimeStamp != 0)
			CharsetUtils.addPart(reqEntity, "from_datetime", lastMessageTimeStamp);
		{
			Account account = new Account(context);
			CharsetUtils.addAllParts(reqEntity, "update_lastview", resetMessageCount ? "1" : "0",
					"session", account.getSessionId());
		}

		post.setEntity(reqEntity);
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");
					if (success) {
						JSONArray JSONchatMessages = object.getJSONArray("items");
						for (int i = 0, l = JSONchatMessages.length(); i < l; i++) {
							ChatManagement.insertChatMessageContentValueToLocalDb(context,
									ChatManagement.makeChatMessageObjectContentValueFromJSON(context, JSONchatMessages.getJSONObject(i)));
							chatMessages.add(ChatManagement.makeChatMessageObjectFromJSON(context, JSONchatMessages.getJSONObject(i)));
						}
					} else {
					}
				}
			}
		} catch (Exception e) {
			Log.e("getChatMessagesForEventFromRemoteDb(Context context, int eventId " + eventId + ")", e.getMessage());
		}
		//
		return chatMessages;
	}

	public static void getAllChatMessagesFromRemoteDb(Context context, LoadProgressHook lph) {
		boolean success = false;
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get_all");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		{
			Account account = new Account(context);
			CharsetUtils.addAllParts(reqEntity, ChatManagement.TOKEN, Data.getToken(context), "session", account.getSessionId());
		}
		post.setEntity(reqEntity);
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);
			if (rp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				return;
			
			String resp = EntityUtils.toString(rp.getEntity());
			if (resp == null)
				return;
			
			JSONObject object = new JSONObject(resp);
			success = object.getBoolean("success");
			if (!success)
				return;
			JSONArray JSONchatMessages = object.getJSONArray("items");
			final int l = JSONchatMessages.length();
			if (lph != null)
				lph.publish(0, l);
			for (int i = 0; i < l; i++) {
				ChatManagement.insertChatMessageContentValueToLocalDb(context,
						ChatManagement.makeChatMessageObjectContentValueFromJSON(context,
								JSONchatMessages.getJSONObject(i)));
				if (lph != null)
					lph.publish(i + 1);
			}
		} catch (Exception e) {
			Log.e("getChatMessagesForEventFromRemoteDb(Context context, int eventId )", e.getMessage());
		}
	}

	public static ChatMessageObject makeChatMessageObjectNow(Context context, String message, int event_id) {
		ChatMessageObject chatMessageObject = new ChatMessageObject();
		Account account = new Account(context);
		Calendar calendar = Calendar.getInstance();
		chatMessageObject.setMessageId(((int) calendar.getTimeInMillis()));
		chatMessageObject.setEventId(event_id);
		chatMessageObject.setCreated(calendar.getTimeInMillis());
		chatMessageObject.setUserId(account.getUser_id());
		chatMessageObject.setMessage(message);
		chatMessageObject.setDeleted(false);
		chatMessageObject.setUpdated(calendar.getTimeInMillis());
		chatMessageObject.setFullname(account.getFullname());
		return chatMessageObject;
	}

	public static boolean removeChatMessagesFromLocalDbForEvent(Context context, long event_id) {
		boolean success = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(ChatProvider.CMMetaData.ChatMetaData.DELETED, true);
			context.getContentResolver().delete(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI,
					ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + event_id, null);
			success = true;
		} catch (Exception e) {
			Log.e("removeChatMessageFromLoacalDB(Context context, int " + event_id + ")", e.getMessage());
		}
		return success;
	}

	public static long getLastMessageTimeStamp(Context context, int eventId) {
		long timestamp = 0;
		Uri uri = ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI;
		String projection[] = { "MAX(" + ChatProvider.CMMetaData.ChatMetaData.CREATED + ") AS " + LASTEST_UPDATED_TIMESTAMP };
		Account account = new Account(context);
		String selection = ChatProvider.CMMetaData.ChatMetaData.USER_ID + "!=" + account.getUser_id() + " AND "
				+ ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId;
		Cursor cur = context.getContentResolver().query(uri, projection, selection, null, null);
		if (cur.moveToFirst()) {
			timestamp = cur.getLong(cur.getColumnIndex(LASTEST_UPDATED_TIMESTAMP));
		}
		cur.close();
		if (timestamp == 0) {
			Uri uriNew = ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI;
			String projectionNew[] = { "MAX(" + ChatProvider.CMMetaData.ChatMetaData.CREATED + ") AS " + LASTEST_UPDATED_TIMESTAMP };
			String selectionNew = ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId;
			Cursor curNew = context.getContentResolver().query(uriNew, projectionNew, selectionNew, null, null);
			if (curNew.moveToFirst()) {
				timestamp = curNew.getLong(curNew.getColumnIndex(LASTEST_UPDATED_TIMESTAMP));
			}
			EventManagement.resetEventsNewMessageCount(context, eventId);
		}
		return Utils.millisToUnixTimestamp(timestamp);
	}

	public static ArrayList<ChatMessageObject> getChatMessagesCreatedOffline(Context context) {
		Uri uri = ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI;
		String projection[] = null;
		String selection = (ChatProvider.CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED + "='0'");
		Cursor cur = context.getContentResolver().query(uri, projection, selection, null, null);
		ArrayList<ChatMessageObject> offlineChatMessages = new ArrayList<ChatMessageObject>(cur.getCount());
		while (cur.moveToNext()) {
			offlineChatMessages.add(makeChatMessageObjectFromCursor(cur));
		}
		cur.close();
		return offlineChatMessages;
	}

	public static void uploadUnploaded(Context context, ArrayList<ChatMessageObject> messages) {
		if (messages != null) {
			for (ChatMessageObject message : messages) {
				if(message.isDeleted()) {
					if(removeChatMessage(context, message.getMessageId())){
						
					} else {
						removeChatMessage(context, postChatMessage(message.getEventId(), message.getMessage(), context).getMessageId());
					}
				} else {
					removeChatMessageFromLocalDb(context, message.getMessageId());
					postChatMessage(message.getEventId(), message.getMessage(), context);
				}
			}
		}
	}
}
