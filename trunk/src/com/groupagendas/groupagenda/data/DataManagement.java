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

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

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
//			loginTask();
			break;
		case 2:
			getAccountTask();
			break;
		}
	}

	private void getAccountTask() {
		Account u = null;
		Cursor result = Data.getmContext().getContentResolver().query(
				AccountProvider.AMetaData.AccountMetaData.CONTENT_URI,
				null,
				null,
				null,
				null);

		if (result.moveToFirst()) {
			u = new Account();

			u.name = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.NAME));
			u.fullname = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.FULLNAME));

			u.birthdate = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.BIRTHDATE));
			u.sex = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SEX));

			u.email = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL));
			u.email2 = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL2));
			u.email3 = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL3));
			u.email4 = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL4));
			u.phone1 = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.PHONE1));
			u.phone2 = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.PHONE2));
			u.phone3 = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.PHONE3));

			final int image = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE));
			u.image = image == 1;
			u.image_url = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE_URL));
			u.image_thumb_url = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE_THUMB_URL));
			u.image_bytes = result.getBlob(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE_BYTES));
			u.remove_image = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.REMOVE_IMAGE));

			u.country = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COUNTRY));
			u.city = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.CITY));
			u.street = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.STREET));
			u.zip = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.ZIP));

			u.timezone = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.TIMEZONE));
			u.local_time = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.LOCAL_TIME));
			u.language = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.LANGUAGE));

			u.setting_default_view = result
					.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW));
			u.setting_date_format = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT));
			u.setting_ampm = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM));

			u.google_calendar_link = result
					.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK));

			u.color_my_event = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_MY_EVENT));
			u.color_attending = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_ATTENDING));
			u.color_pending = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_PENDING));
			u.color_invitation = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_INVINTATION));
			u.color_notes = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_NOTES));
			u.color_birthday = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_BIRTHDAY));

			u.created = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.CREATED));
			u.modified = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.MODIFIED));
		}
		result.close();
		Data.setAccount(u);
	}

}
