package com.groupagendas.groupagenda.data;

import java.io.UnsupportedEncodingException;

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
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.chat.ChatProvider;
import com.groupagendas.groupagenda.chat.ChatProvider.CMMetaData;

public class ChatManagement {

	private static String TOKEN = "token";

	public static ChatMessageObject makeChatMessageObjectFromJSON(JSONObject json) {
		ChatMessageObject chatMessage = new ChatMessageObject();
		try {
			chatMessage.setMessageId(json.getInt("message_id"));
			chatMessage.setEventId(json.getInt("event_id"));
			chatMessage.setDateTime(json.getString("datetime"));
			chatMessage.setUserId(json.getInt("user_id"));
			chatMessage.setMessage(json.getString("message"));
			String deleted = json.getString("deleted");
			chatMessage.setDeleted(!deleted.equals("null"));
			chatMessage.setUpdated(json.getString("updated"));
		} catch (JSONException e) {
			Log.e("makeChatMessageObjectFromJSON(JSONObject json)", e.getMessage());
		}
		return chatMessage;
	}

	public static ContentValues makeChatMessageObjectContentValueFromJSON(JSONObject json) {
		ContentValues cv = new ContentValues();
		try {
			cv.put(CMMetaData.ChatMetaData.M_ID, json.getInt("message_id"));
			cv.put(CMMetaData.ChatMetaData.E_ID, json.getInt("event_id"));
			cv.put(CMMetaData.ChatMetaData.DATE_TIME, json.getString("datetime"));
			cv.put(CMMetaData.ChatMetaData.USER_ID, json.getInt("user_id"));
			cv.put(CMMetaData.ChatMetaData.MESSAGE, json.getString("message"));
			String deleted = json.getString("deleted");
			cv.put(CMMetaData.ChatMetaData.DELETED, !deleted.equals("null"));
			cv.put(CMMetaData.ChatMetaData.UPDATED, json.getString("updated"));
		} catch (Exception e) {
			Log.e("makeChatMessageObjectContentValuesFromJSON(JSONObject json)", e.getMessage());
		}
		return cv;
	}

	public static ContentValues makeContentValuesFromChatMessageObject(ChatMessageObject chatMessage) {
		ContentValues cv = new ContentValues();
		try {
			cv.put(CMMetaData.ChatMetaData.M_ID, chatMessage.getMessageId());
			cv.put(CMMetaData.ChatMetaData.E_ID, chatMessage.getEventId());
			cv.put(CMMetaData.ChatMetaData.DATE_TIME, chatMessage.getDateTime());
			cv.put(CMMetaData.ChatMetaData.USER_ID, chatMessage.getUserId());
			cv.put(CMMetaData.ChatMetaData.MESSAGE, chatMessage.getMessage());
			cv.put(CMMetaData.ChatMetaData.DELETED, chatMessage.isDeleted());
			cv.put(CMMetaData.ChatMetaData.UPDATED, chatMessage.getUpdated());
		} catch (Exception e) {
			Log.e("makeContentValuesFromChatMessageObject(ChatMessageObject " + chatMessage.getMessageId() + ")", e.getMessage());
		}
		return cv;
	}

	/**
	 * Get all chat message entries from remote database.
	 * 
	 * Executes a call to remote database, retrieves all chat messages from it.
	 * 
	 * @author justas@mobileapps.lt
	 * @return boolean success
	 * @since 2012-10P01
	 * @version 0.1
	 */

	public static boolean getChatMessagesFromRemoteDb(Context context) {
		boolean success = false;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_threads");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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
						JSONArray chatMessages = object.getJSONArray("items");
						for (int i = 0, l = chatMessages.length(); i < l; i++) {
							insertChatMessageContentValueToLocalDb(context,
									makeChatMessageObjectContentValueFromJSON(chatMessages.getJSONObject(i)));
						}
					} else {
						Toast.makeText(context, object.getString("error"), Toast.LENGTH_LONG);
					}
				}
			}
		} catch (Exception e) {
			Log.e("getChatMessagesFromRemoteDb(Context context)", e.getMessage());
		}
		return success;
	}

	public static boolean insertChatMessageContentValueToLocalDb(Context context, ContentValues cv) {
		try {
			context.getContentResolver().insert(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, cv);
			return true;
		} catch (SQLiteException e) {
			Log.e("getChatMessagesFromRemoteDB(chat, " + cv.get("message_id") + ")", e.getMessage());
			return false;
		}
	}

	public static boolean insertChatMessageToLoacalDb(Context context, ChatMessageObject chatMessageObject) {
		if (insertChatMessageContentValueToLocalDb(context, makeContentValuesFromChatMessageObject(chatMessageObject))) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean removeChatMessageFromRemoteDb(Context context, int messageId) {
		boolean success = false;
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
			reqEntity.addPart("message_id", new StringBody(String.valueOf(messageId)));

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
							removeChatMessageFromLocalDb(context, messageId);
							Toast.makeText(context, context.getResources().getString(R.string.delete_chat_message), Toast.LENGTH_LONG);
						} else {
							Toast.makeText(context, object.getString("error"), Toast.LENGTH_LONG);
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

	public static boolean postChatMessage(Context context, String message, int eventId) {
		boolean success = false;
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_post");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(eventId)));
			if (message == null) {
				reqEntity.addPart("message", new StringBody(String.valueOf("")));
			} else {
				reqEntity.addPart("message", new StringBody(String.valueOf(message)));
			}

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
							insertChatMessageContentValueToLocalDb(context,
									makeChatMessageObjectContentValueFromJSON(object.getJSONObject("message")));
						} else {
							Toast.makeText(context, object.getString("error"), Toast.LENGTH_LONG);
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
		return success;
	}
}
