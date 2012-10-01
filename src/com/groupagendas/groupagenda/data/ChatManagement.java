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
	public static void getChatThreadsFromRemoteDB(){
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
						JSONArray chatThreads = object.getJSONArray("items");
					}
			}
		} catch (Exception e) {
			
		}
	}
}
