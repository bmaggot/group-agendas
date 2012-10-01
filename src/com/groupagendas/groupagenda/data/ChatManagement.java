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

import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.utils.Utils;

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
	public static void getChatMessagesFromRemoteDB(){
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
					boolean getSuccess = object.getBoolean("success");
					if (getSuccess) {
						JSONArray chatMessages = object.getJSONArray("items");
						ChatMessageObject message = new ChatMessageObject();
						for (int i = 0, l = chatMessages.length(); i < l; i++) {
							final JSONObject chatMessage = chatMessages.getJSONObject(i);
							String tmp = "";
							message.messageId = chatMessage.getInt("message_id");
							message.eventId = chatMessage.getInt("event_id");
							message.dateTime = chatMessage.getString("datetime");
							message.dateTimeCalendar = Utils.stringToCalendar(message.dateTime, DataManagement.SERVER_TIMESTAMP_FORMAT);
							message.userId = chatMessage.getInt("user_id");
							message.message = chatMessage.getString("message");
							String deleted = chatMessage.getString("deleted");
							message.deleted = !deleted.equals("null");
							message.updated = chatMessage.getString("updated");
							message.updatedCalendar = Utils.stringToCalendar(message.updated, DataManagement.SERVER_TIMESTAMP_FORMAT);
							message.fullname = chatMessage.getString("fullname");
							message.contactId = chatMessage.getString("contact_id");
							message.dateTimeConverted = chatMessage.getString("datetime_conv");
							message.dateTimeConvertedCalendar = Utils.stringToCalendar(message.dateTimeConverted,
									DataManagement.SERVER_TIMESTAMP_FORMAT);
							message.formatedDateTime = chatMessage.getString("formatted_datetime");
						}
							
					}
				}
			}
		} catch (Exception e) {
			
		}
	}
}
