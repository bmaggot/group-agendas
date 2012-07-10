package com.groupagendas.groupagenda.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.c2dm.C2DMessaging;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;

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
	public boolean connect(String path, HashMap<String, String> parts, Integer email_id) {

		boolean success = false;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getDEFAULT_SERVER_URL() + path);

		post.setHeader("User-Agent", "Android");
		post.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		if (parts != null && !parts.isEmpty()) {
			if (parts.containsKey("email") && parts.containsKey("password")) {
				Data.setEmail(parts.get("email"));
				Data.setPassword(parts.get("password"));
			}
			if (email_id != null && email_id > 1) {
				try {
					reqEntity.addPart("email_id", new StringBody(String.valueOf(email_id)));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < parts.size() - 1; i++) {
				try {
					reqEntity.addPart(parts.keySet().toArray()[i].toString(), new StringBody(parts.values().toArray()[2].toString()));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
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
			try {
				executeTask(path, hc, post, reqEntity, rp);
			} catch (Exception e) {
				e.getMessage();
			}
		} else {
			success = false;
		}
		return success;
	}


	private void executeTask(String path, HttpClient client, HttpPost post, MultipartEntity entity, HttpResponse response) throws Exception {
		switch (states.get(path)) {
		case 1:
			loginTask(response);
			break;
		case 2:
			getAccountTask(client, entity, post, response);
			break;
		case 3:
			updateAccountTask(client, entity, post, response);
		case 4:
			break; //no impl
		case 5:
			break; //no impl
		case 6:
			break;
		case 7:
			break; //no impl
		case 8:
			break;
		case 9:
			break;
		case 10:
			break;
		case 37:
			break;  //no impl
		}
	}
	

	private void loginTask(HttpResponse rp) {
		boolean success = false;
		String token = null;
		try {
			String resp = EntityUtils.toString(rp.getEntity());
			if (resp != null) {
				JSONObject object = new JSONObject(resp);
				success = object.getBoolean("success");
				Log.e("resp", resp);
				if (success == true) {
					token = object.getString("token");
					JSONObject profile = object.getJSONObject("profile");
					int id = Integer.parseInt(profile.getString("user_id"));
					Data.setToken(token);
					Data.setUserId(id);
					//
					Data.setLogged(true);
					//
					Data.save();

					// autoicons and autocolors
					Data.getmContext().getContentResolver().delete(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, "", null);
					JSONArray autoicons = object.getJSONArray("custom_icons");
					for (int i = 0, l = autoicons.length(); i < l; i++) {
						final JSONObject autoicon = autoicons.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(AccountProvider.AMetaData.AutoiconMetaData.ICON, autoicon.getString("icon"));
						values.put(AccountProvider.AMetaData.AutoiconMetaData.KEYWORD, autoicon.getString("keyword"));
						values.put(AccountProvider.AMetaData.AutoiconMetaData.CONTEXT, autoicon.getString("context"));

						Data.getmContext().getContentResolver().insert(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, values);
					}

					Data.getmContext().getContentResolver().delete(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, "", null);
					JSONArray autocolors = object.getJSONArray("custom_colors");
					for (int i = 0, l = autocolors.length(); i < l; i++) {
						final JSONObject autocolor = autocolors.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(AccountProvider.AMetaData.AutocolorMetaData.COLOR, autocolor.getString("color"));
						values.put(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD, autocolor.getString("keyword"));
						values.put(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT, autocolor.getString("context"));

						Data.getmContext().getContentResolver().insert(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, values);
					}
					registerPhone();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerPhone() {
		try {
			getImei(Data.getmContext());
			Data.setPushId(C2DMessaging.getRegistrationId(Data.getmContext()));
			if (Data.getPushId() == "") {
				System.out.println("C2DMessaging.register()");

				C2DMessaging.register(Data.getmContext(), "group.agenda.c2dm@gmail.com");
			} else {
				sendPushIdToServer(Data.getmContext(), Data.getPushId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getImei(Context context) {

		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			if (telephonyManager == null) {
				return "";
			}

			return telephonyManager.getDeviceId();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static void sendPushIdToServer(Context context, String pushId) {

		try {
			DataManagement._instance.connect("mobile/register_android", null, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getAccountTask(HttpClient client, MultipartEntity reqEntity, HttpPost post, HttpResponse response) throws Exception {
		Account u = new Account();

		// FINDOUTWTFLOLWUT
		reqEntity.addPart("token", new StringBody(Data.getToken()));

		String resp = EntityUtils.toString(response.getEntity());
		if (resp != null) {
			JSONObject object = new JSONObject(resp);
			boolean success = object.getBoolean("success");

			if (success == true) {
				JSONObject profile = object.getJSONObject("profile");
				ContentValues cv = new ContentValues();

				try {
					u.user_id = profile.getInt("user_id");
					cv.put(AccountProvider.AMetaData.AccountMetaData.A_ID, u.user_id);
				} catch (JSONException e) {
				}

				try {
					u.name = profile.getString("name");
					cv.put(AccountProvider.AMetaData.AccountMetaData.NAME, u.name);
				} catch (JSONException e) {
				}
				
				try {
					u.fullname = profile.getString("fullname");
					cv.put(AccountProvider.AMetaData.AccountMetaData.FULLNAME, u.fullname);
				} catch (JSONException e) {
				}

				try {
					u.birthdate = profile.getString("birthdate");
					cv.put(AccountProvider.AMetaData.AccountMetaData.BIRTHDATE, u.birthdate);
				} catch (JSONException e) {
				}
				
				try {
					u.sex = profile.getString("sex");
					cv.put(AccountProvider.AMetaData.AccountMetaData.SEX, u.sex);
				} catch (JSONException e) {
				}

				try {
					u.email = profile.getString("email");
					cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL, u.email);
				} catch (JSONException e) {
				}
				
				try {
					u.email2 = profile.getString("email2");
					cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL2, u.email2);
				} catch (JSONException e) {
				}
				
				try {
					u.email3 = profile.getString("email3");
					cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL3, u.email3);
				} catch (JSONException e) {
				}
				
				try {
					u.email4 = profile.getString("email4");
					cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL4, u.email4);
				} catch (JSONException e) {
				}
				
				try {
					u.phone1 = profile.getString("phone1");
					cv.put(AccountProvider.AMetaData.AccountMetaData.PHONE1, u.phone1);
				} catch (JSONException e) {
				}
				
				try {
					u.phone2 = profile.getString("phone2");
					cv.put(AccountProvider.AMetaData.AccountMetaData.PHONE2, u.phone2);
				} catch (JSONException e) {
				}
				
				try {
					u.phone3 = profile.getString("phone3");
					cv.put(AccountProvider.AMetaData.AccountMetaData.PHONE3, u.phone3);
				} catch (JSONException e) {
				}

				try {
					u.image = profile.getBoolean("image");
					cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE, u.image);
				} catch (JSONException e) {
				}
				
				try {
					u.image_url = profile.getString("image_url");
					cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_URL, u.image_url);

//					u.image_bytes = imageToBytes(u.image_url);
//					cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_BYTES, u.image_bytes);
				} catch (JSONException e) {
				}
				
				try {
					u.image_thumb_url = profile.getString("image_thumb_url");
					cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_THUMB_URL, u.image_thumb_url);
				} catch (JSONException e) {
				}

				try {
					u.country = profile.getString("country");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COUNTRY, u.country);
				} catch (JSONException e) {
				}
				
				try {
					u.city = profile.getString("city");
					cv.put(AccountProvider.AMetaData.AccountMetaData.CITY, u.city);
				} catch (JSONException e) {
				}
				
				try {
					u.street = profile.getString("street");
					cv.put(AccountProvider.AMetaData.AccountMetaData.STREET, u.street);
				} catch (JSONException e) {
				}
				
				try {
					u.zip = profile.getString("zip");
					cv.put(AccountProvider.AMetaData.AccountMetaData.ZIP, u.zip);
				} catch (JSONException e) {
				}

				try {
					u.timezone = profile.getString("timezone");
					cv.put(AccountProvider.AMetaData.AccountMetaData.TIMEZONE, u.timezone);
				} catch (JSONException e) {
				}
				
				try {
					u.local_time = profile.getString("local_time");
					cv.put(AccountProvider.AMetaData.AccountMetaData.LOCAL_TIME, u.local_time);
				} catch (JSONException e) {
				}
				
				try {
					u.language = profile.getString("language");
					cv.put(AccountProvider.AMetaData.AccountMetaData.LANGUAGE, u.language);
				} catch (JSONException e) {
				}

				try {
					u.setting_default_view = profile.getString("setting_default_view");
					cv.put(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, u.setting_default_view);
				} catch (JSONException e) {
				}
				
				try {
					u.setting_date_format = profile.getString("setting_date_format");
					cv.put(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT, u.setting_date_format);
				} catch (JSONException e) {
				}
				
				try {
					u.setting_ampm = profile.getInt("setting_ampm");
					cv.put(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM, u.setting_ampm);
				} catch (JSONException e) {
				}

				try {
					u.google_calendar_link = profile.getString("google_calendar_link");
					cv.put(AccountProvider.AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK, u.google_calendar_link);
				} catch (JSONException e) {
				}

				try {
					u.color_my_event = profile.getString("color_my_event");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_MY_EVENT, u.color_my_event);
				} catch (JSONException e) {
				}
				
				try {
					u.color_attending = profile.getString("color_attending");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_ATTENDING, u.color_attending);
				} catch (JSONException e) {
				}
				
				try {
					u.color_pending = profile.getString("color_pending");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_PENDING, u.color_pending);
				} catch (JSONException e) {
				}
				
				try {
					u.color_invitation = profile.getString("color_invitation");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_INVINTATION, u.color_invitation);
				} catch (JSONException e) {
				}
				
				try {
					u.color_notes = profile.getString("color_notes");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_NOTES, u.color_notes);
				} catch (JSONException e) {
				}
				
				try {
					u.color_birthday = profile.getString("color_birthday");
					cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_BIRTHDAY, u.color_birthday);
				} catch (JSONException e) {
				}

				try {
					u.created = profile.getString("created");
					cv.put(AccountProvider.AMetaData.AccountMetaData.CREATED, u.created);
				} catch (JSONException e) {
				}
				
				try {
					u.modified = profile.getString("modified");
					cv.put(AccountProvider.AMetaData.AccountMetaData.MODIFIED, u.modified);
				} catch (JSONException e) {
				}

				Data.getmContext().getContentResolver().insert(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, cv);
				
				Data.setAccount(u);
			} else {
				//
			}	
		} else {
			//
		}
	}

	public void updateAccountTask (HttpClient client, MultipartEntity reqEntity, HttpPost post, HttpResponse response) throws UnsupportedEncodingException {

		reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addPart("token", new StringBody(Data.getToken()));

		reqEntity.addPart("lastname", new StringBody(Data.getAccount().fullname.replace(Data.getAccount().name + " ", "")));
		reqEntity.addPart("name", new StringBody(Data.getAccount().name));

		reqEntity.addPart("birthdate", new StringBody(Data.getAccount().birthdate));
		reqEntity.addPart("sex", new StringBody(Data.getAccount().sex));

		reqEntity.addPart("country", new StringBody(Data.getAccount().country));
		reqEntity.addPart("city", new StringBody(Data.getAccount().city));
		reqEntity.addPart("street", new StringBody(Data.getAccount().street));
		reqEntity.addPart("zip", new StringBody(Data.getAccount().zip));

		reqEntity.addPart("timezone", new StringBody(Data.getAccount().timezone));

		reqEntity.addPart("phone1", new StringBody(Data.getAccount().phone1));
		reqEntity.addPart("phone2", new StringBody(Data.getAccount().phone2));
		reqEntity.addPart("phone3", new StringBody(Data.getAccount().phone3));

		reqEntity.addPart("language", new StringBody("en"));

		post.setEntity(reqEntity);

		try {
			response = client.execute(post);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(response.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					boolean success = object.getBoolean("success");
					if (!success) {
						Log.e("Change account ERROR", object.getJSONObject("error").getString("reason"));
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}