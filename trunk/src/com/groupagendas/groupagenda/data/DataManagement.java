package com.groupagendas.groupagenda.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;

public class DataManagement {

	private static DataManagement _instance = null;
	private static final HashMap<String, Integer> states = new HashMap<String, Integer>();

	private DataManagement(Activity c) {
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
	}

	private DataManagement(Context c) {
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
	}

	public static synchronized DataManagement getInstance(Activity c) {
		if (_instance == null)
			_instance = new DataManagement(c);
		return _instance;
	}

	public static synchronized DataManagement getInstance(Context c) {
		if (_instance == null)
			_instance = new DataManagement(c);
		return _instance;
	}

	public void fillStates() {
		states.put("mobile/login", 1);
		states.put("mobile/account_get", 2);
		states.put("mobile/account_edit", 3);
		states.put("mobile/account_email_change", 4);
		states.put("mobile/account_password_change", 5);
		states.put("mobile/account_image", 6);
		states.put("mobile/account_register", 7);
		states.put("mobile/groups_list", 8);
		states.put("mobile/groups_create", 9);
		states.put("mobile/groups_edit", 10);
		states.put("mobile/group_get", 11);
		states.put("mobile/group_remove", 12);
		states.put("mobile/contact_list", 13);
		states.put("mobile/contact_get", 14);
		states.put("mobile/contact_create", 15);
		states.put("mobile/contact_edit", 16);
		states.put("mobile/contact_copy", 17);
		states.put("mobile/contact_remove", 18);
		states.put("mobile/events_list", 19);
		states.put("mobile/events_get", 20);
		states.put("mobile/events_create", 21);
		states.put("mobile/events_edit", 22);
		states.put("mobile/events_remove", 23);
		states.put("mobile/set_event_status", 24);
		states.put("mobile/events_invite_extra", 25);
		states.put("mobile/settings_update", 26);
		states.put("mobile/settings_set_autocolors", 27);
		states.put("mobile/settings_set_autoicons", 28);
		states.put("mobile/events_list_by_contact_id", 29);
		states.put("mobile/events_change_time", 30);
		states.put("mobile/events_purge_native", 31);
		states.put("mobile/report_events", 32);
		states.put("mobile/chat_get", 33);
		states.put("mobile/chat_post", 34);
		states.put("mobile/chat_remove", 35);
		states.put("mobile/chat_threads", 36);
		states.put("mobile/register_android", 37);
		states.put("mobile/account_forgot_pass1", 38);
		states.put("mobile/account_forgot_pass2", 39);
		states.put("mobile/get_country_code", 40);
		states.put("mobile/set_lastlogin", 41);
	}

	/**
	 * Returns an boolean that determines if connection to server was
	 * successful. The path argument must specify a relative link to project's
	 * API. The parts argument contains request data.
	 * 
	 * This method always returns immediately, whether or not the connection was
	 * successful.
	 * 
	 * @param path
	 *            an absolute URL giving the base location of the image
	 * @param parts
	 *            the location of the image, relative to the url argument
	 * @return request state (successful or not)
	 * @see MultipartEntity
	 */
	public boolean connect(String path, ArrayList<String[]> parts) {

		boolean success = false;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getDEFAULT_SERVER_URL() + path);

		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		for (String[] part : parts) {
			try {
				reqEntity.addPart(part[1], new StringBody(part[2]));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		post.setEntity(reqEntity);

		HttpResponse rp = null;
		try {
			rp = hc.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			success = true;
			try{
				executeTask(path);
			} catch (Exception e) {
				e.getMessage();
			}
		} else {
			success = false;
		}
		return success;
	}
	
	private void executeTask (String path) throws Exception{
		switch (states.get(path)) {
		case 1:
			loginTask();
			break;
		}
	}

	private void loginTask() {
		
	}

}
