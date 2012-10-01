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
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.groupagendas.groupagenda.chat.ChatProvider;
import com.groupagendas.groupagenda.chat.ChatProvider.CMMetaData;

public class ChatManagement {

	/**
	 * Get all chat thread entries from remote database.
	 * 
	 * Executes a call to remote database, retrieves all chat thread entries from
	 * it and stores it to loacal DB.
	 * 
	 * @author justas@mobileapps.lt
	 * @return ArrayList of ChatThread objects got from response.
	 * @since 2012-10P01
	 * @version 0.1
	 */
	public static void getChatMessagesFromRemoteDB(Context context){
		boolean success = false;
		String error = null;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_threads");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken()));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		post.setEntity(reqEntity);
		try{
			HttpResponse rp = hc.execute(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");
					if (success) {
						JSONArray chatMessages = object.getJSONArray("items");
						ContentValues cv = new ContentValues();
						for (int i = 0, l = chatMessages.length(); i < l; i++) {
							final JSONObject chatMessage = chatMessages.getJSONObject(i);
							cv.put(CMMetaData.ChatMetaData.M_ID, chatMessage.getInt("message_id"));
							cv.put(CMMetaData.ChatMetaData.E_ID, chatMessage.getInt("event_id"));
							cv.put(CMMetaData.ChatMetaData.DATE_TIME, chatMessage.getString("datetime"));
							cv.put(CMMetaData.ChatMetaData.USER_ID, chatMessage.getInt("user_id"));
							cv.put(CMMetaData.ChatMetaData.MESSAGE, chatMessage.getString("message"));
							String deleted = chatMessage.getString("deleted");
							cv.put(CMMetaData.ChatMetaData.DELETED, !deleted.equals("null"));
							cv.put(CMMetaData.ChatMetaData.UPDATED, chatMessage.getString("updated"));
							
							
							try {
								context.getContentResolver().insert(ChatProvider.CMMetaData.ChatMetaData.CONTENT_URI, cv);
							} catch (SQLiteException e) {
								Log.e("getChatMessagesFromRemoteDB(chat, "+chatMessage.getInt("message_id")+")", e.getMessage());
							}
						}
							
					} else {
						error = object.getString("error");
						Data.setERROR(error);
					}
				}
			}
		} catch (Exception e) {
			
		}
	}
}
