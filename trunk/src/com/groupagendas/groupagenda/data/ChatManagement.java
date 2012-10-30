package com.groupagendas.groupagenda.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
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

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.chat.ChatProvider;
import com.groupagendas.groupagenda.chat.ChatProvider.CMMetaData;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData.EventsMetaData;
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

	public static ChatMessageObject makeChatMessageObjectFromJSON(JSONObject json) {
		ChatMessageObject chatMessage = new ChatMessageObject();
		try {
			chatMessage.setMessageId(json.getInt(CMMetaData.ChatMetaData.M_ID));
			chatMessage.setEventId(json.getInt(CMMetaData.ChatMetaData.E_ID));
			chatMessage.setCreated(json.getLong(CMMetaData.ChatMetaData.CREATED));
			chatMessage.setUserId(json.getInt(CMMetaData.ChatMetaData.USER_ID));
			chatMessage.setMessage(json.getString(CMMetaData.ChatMetaData.MESSAGE));
			String deleted = json.getString(CMMetaData.ChatMetaData.DELETED);
			chatMessage.setDeleted(!deleted.equals("null"));
			chatMessage.setUpdated(json.getString(CMMetaData.ChatMetaData.UPDATED));
			chatMessage.setFullname(json.getString(CMMetaData.ChatMetaData.FULLNAME));
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

	public static ContentValues makeChatMessageObjectContentValueFromJSON(JSONObject json) {
		ContentValues cv = new ContentValues();
		try {
			cv.put(CMMetaData.ChatMetaData.M_ID, json.getInt(CMMetaData.ChatMetaData.M_ID));
			cv.put(CMMetaData.ChatMetaData.E_ID, json.getInt(CMMetaData.ChatMetaData.E_ID));
			cv.put(CMMetaData.ChatMetaData.CREATED, json.getString(CMMetaData.ChatMetaData.CREATED));
			cv.put(CMMetaData.ChatMetaData.USER_ID, json.getInt(CMMetaData.ChatMetaData.USER_ID));
			cv.put(CMMetaData.ChatMetaData.MESSAGE, json.getString(CMMetaData.ChatMetaData.MESSAGE));
			String deleted = json.getString(CMMetaData.ChatMetaData.DELETED);
			cv.put(CMMetaData.ChatMetaData.DELETED, !deleted.equals("null"));
			cv.put(CMMetaData.ChatMetaData.UPDATED, json.getString(CMMetaData.ChatMetaData.UPDATED));
			cv.put(CMMetaData.ChatMetaData.FULLNAME, json.getString(CMMetaData.ChatMetaData.FULLNAME));
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
			cv.put(CMMetaData.ChatMetaData.UPDATED, chatMessage.getUpdated());
			cv.put(CMMetaData.ChatMetaData.FULLNAME, chatMessage.getFullname());
		} catch (Exception e) {
			Log.e("makeContentValuesFromChatMessageObject(ChatMessageObject " + chatMessage.getMessageId() + ")", e.getMessage());
		}
		return cv;
	}

	public static ArrayList<ChatMessageObject> getChatMessagesForEventFromLocalDb(Context context, int eventId) {
		ArrayList<ChatMessageObject> chatMessages = new ArrayList<ChatMessageObject>();
		Cursor cur;
		String selection = ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId;
		cur = context.getContentResolver().query(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, null, selection, null, null);

		if (cur.moveToFirst()) {
			while (!cur.isAfterLast()) {
				ChatMessageObject message = new ChatMessageObject();
				message.setMessageId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.M_ID)));
				message.setEventId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.E_ID)));
				message.setCreated(cur.getLong(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.CREATED)));
				message.setUserId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.USER_ID)));
				message.setMessage(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.MESSAGE)));
				message.setDeleted(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.DELETED)).equals(
						ChatManagement.deleted));
				message.setUpdated(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.UPDATED)));
				message.setFullname(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.FULLNAME)));
				chatMessages.add(message);
				cur.moveToNext();
			}
		}
		cur.close();
		return chatMessages;
	}
	
	public static ChatMessageObject getLastMessageForEventFromLocalDb(Context context, int eventId){
		ChatMessageObject chatMessageObject = new ChatMessageObject();
		String selection = ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId + " AND " +  ChatProvider.CMMetaData.ChatMetaData.DELETED + "=0";
		String sortOrder = ChatProvider.CMMetaData.ChatMetaData.CREATED + " DESC ";
		Cursor cur = context.getContentResolver().query(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, null, selection, null, sortOrder);
		if(cur.moveToFirst()){
			chatMessageObject.setMessageId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.M_ID)));
			chatMessageObject.setEventId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.E_ID)));
			chatMessageObject.setCreated(cur.getLong(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.CREATED)));
			chatMessageObject.setUserId(cur.getInt(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.USER_ID)));
			chatMessageObject.setMessage(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.MESSAGE)));
			chatMessageObject.setDeleted(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.DELETED)).equals(
					ChatManagement.deleted));
			chatMessageObject.setUpdated(cur.getString(cur.getColumnIndex(ChatProvider.CMMetaData.ChatMetaData.UPDATED)));
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
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context)));
			reqEntity.addPart("message_id", new StringBody(String.valueOf(messageId)));
			Account account = new Account(context);
			reqEntity.addPart("session", new StringBody(account.getSessionId()));

			post.setEntity(reqEntity);
			HttpResponse rp = null;
			if (DataManagement.networkAvailable) {
				rp = hc.execute(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (success) {
						} else {
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/chat_remove", reqEntity);
				Data.getUnuploadedData().add(uplooad);
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

	public static boolean removeChatMessageFromLocalDb(Context context, int messageId) {
		boolean success = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(ChatProvider.CMMetaData.ChatMetaData.DELETED, true);
			context.getContentResolver().update(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, cv,
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
			succcess = removeChatMessageFromLocalDb(context, messageId);
		}
		return succcess;
	}

	public static ChatMessageObject postChatMessage(int eventId, String message, Context context) {
		ChatMessageObject chatMessageObject = new ChatMessageObject();
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_post");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(ChatManagement.TOKEN, new StringBody(Data.getToken(context)));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(eventId)));
			if (message == null) {
				reqEntity.addPart("message", new StringBody(String.valueOf("")));
			} else {
				reqEntity.addPart("message", new StringBody(String.valueOf(message)));
			}
			Account account = new Account(context);
			reqEntity.addPart("session", new StringBody(account.getSessionId()));

			post.setEntity(reqEntity);
			HttpResponse rp = null;
			if (DataManagement.networkAvailable) {
				rp = hc.execute(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						boolean success = object.getBoolean("success");
						if (success) {
							ChatManagement.insertChatMessageContentValueToLocalDb(context,
									ChatManagement.makeChatMessageObjectContentValueFromJSON(object.getJSONObject("message")));
							chatMessageObject = ChatManagement.makeChatMessageObjectFromJSON(object.getJSONObject("message"));
							ContentValues cv = new ContentValues();
							Uri uri = EventsProvider.EMetaData.EventsMetaData.UPDATE_EVENT_AFTER_CHAT_POST;
							cv.put(EMetaData.EventsMetaData.E_ID, eventId);
							context.getContentResolver().update(uri, cv, null, null);
							cv = new ContentValues();
							cv.put(EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS, chatMessageObject.getCreated());
							uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
							String where = EventsProvider.EMetaData.EventsMetaData.E_ID + "=" + eventId;
							context.getContentResolver().update(uri, cv, where, null);
						} else {
							chatMessageObject = null;
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/chat_post", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception e) {
			Log.e("postChatMessage(Context context, message " + message + ", event id " + eventId + ")", e.getMessage());
		}
		return chatMessageObject;
	}

	public static ArrayList<ChatMessageObject> getChatMessagesForEventFromRemoteDb(int eventId, Context context, boolean resetMessageCount,
			long lastMessageTimeStamp) {
		boolean success = false;
		HttpClient hc = new DefaultHttpClient();
		ArrayList<ChatMessageObject> chatMessages = new ArrayList<ChatMessageObject>();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");
		Log.e("ChatManagement", "");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart(ChatManagement.TOKEN, new StringBody(Data.getToken(context)));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(eventId)));
			if (lastMessageTimeStamp != 0) {
				reqEntity.addPart("from_datetime", new StringBody(String.valueOf(lastMessageTimeStamp)));
			}
			if (resetMessageCount) {
				reqEntity.addPart("update_lastview", new StringBody("0"));
			} else {
				reqEntity.addPart("update_lastview", new StringBody("0"));
			}
			Account account = new Account(context);
			reqEntity.addPart("session", new StringBody(account.getSessionId()));

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		post.setEntity(reqEntity);
		try {
			HttpResponse rp = hc.execute(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");
					if (success) {
						JSONArray JSONchatMessages = object.getJSONArray("items");
						for (int i = 0, l = JSONchatMessages.length(); i < l; i++) {
							ChatManagement.insertChatMessageContentValueToLocalDb(context,
									ChatManagement.makeChatMessageObjectContentValueFromJSON(JSONchatMessages.getJSONObject(i)));
							chatMessages.add(ChatManagement.makeChatMessageObjectFromJSON(JSONchatMessages.getJSONObject(i)));
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

	public static void getAllChatMessagesFromRemoteDb(Context context) {
		boolean success = false;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get_all");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart(ChatManagement.TOKEN, new StringBody(Data.getToken(context)));
			Account account = new Account(context);
			reqEntity.addPart("session", new StringBody(account.getSessionId()));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		post.setEntity(reqEntity);
		try {
			HttpResponse rp = hc.execute(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");
					if (success) {
						JSONArray JSONchatMessages = object.getJSONArray("items");
						for (int i = 0, l = JSONchatMessages.length(); i < l; i++) {
							ChatManagement.insertChatMessageContentValueToLocalDb(context,
									ChatManagement.makeChatMessageObjectContentValueFromJSON(JSONchatMessages.getJSONObject(i)));
						}
					} else {

					}
				}
			}
		} catch (Exception e) {
			Log.e("getChatMessagesForEventFromRemoteDb(Context context, int eventId )", e.getMessage());
		}
	}

	public static ChatMessageObject makeChatMessageObjectNow(Context context, String message, int event_id) {
		ChatMessageObject chatMessageObject = new ChatMessageObject();
		Account account = new Account(context);
		Calendar calendar = Calendar.getInstance();
		chatMessageObject.setMessageId((int) calendar.getTimeInMillis());
		chatMessageObject.setEventId(event_id);
		chatMessageObject.setCreated(Utils.millisToUnixTimestamp(calendar.getTimeInMillis()));
		chatMessageObject.setUserId(account.getUser_id());
		chatMessageObject.setMessage(message);
		chatMessageObject.setDeleted(false);
		chatMessageObject.setUpdated("null");
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
		String projection[] = {"MAX("+ChatProvider.CMMetaData.ChatMetaData.CREATED+") AS "+LASTEST_UPDATED_TIMESTAMP};
		Account account = new Account(context);
		String selection = ChatProvider.CMMetaData.ChatMetaData.USER_ID + "!=" + account.getUser_id() + " AND " + ChatProvider.CMMetaData.ChatMetaData.E_ID + "=" + eventId;
		Cursor cur = context.getContentResolver().query(uri, projection, selection, null, null);
		if(cur.moveToFirst()){
			timestamp = cur.getLong(cur.getColumnIndex(LASTEST_UPDATED_TIMESTAMP));
		}
		cur.close();
		return timestamp;
	}
}
