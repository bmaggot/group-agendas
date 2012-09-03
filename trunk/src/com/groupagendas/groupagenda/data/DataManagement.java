package com.groupagendas.groupagenda.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bog.calendar.app.model.CEvent;
import com.bog.calendar.app.model.EventsHelper;
import com.google.android.c2dm.C2DMessaging;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsAdapter;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.contacts.GroupsAdapter;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsAdapter;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.utils.AgendaUtils;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;

public class DataManagement {

	public static boolean networkAvailable = true;
	public static boolean eventStatusChanged = false;
	public static ArrayList<Event> contactsBirthdays = new ArrayList<Event>();
	public static final String SERVER_TIMESTAMP_FORMAT = "yyyy-MM-dd hh:mm:ss";
	public static final String ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT = "yyyy-MM-dd";

	private DataManagement(Activity c) {
		Data.setPrefs(new Prefs(c));
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
	}

	private DataManagement(Context c) {
		Data.setPrefs(new Prefs(c));
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
	}

	public static synchronized DataManagement getInstance(Activity c) {
		if (Data.get_instance() == null)
			Data.set_instance(new DataManagement(c));
		return Data.get_instance();
	}

	public static synchronized DataManagement getInstance(Context c) {
		if (Data.get_instance() == null)
			Data.set_instance(new DataManagement(c));
		return Data.get_instance();
	}

	public boolean updateAccount(Account account, boolean removeImage) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("token", new StringBody(Data.getToken()));

			reqEntity.addPart("lastname", new StringBody(account.fullname.replace(account.name + " ", "")));
			reqEntity.addPart("name", new StringBody(account.name));

			reqEntity.addPart("birthdate", new StringBody(account.birthdate));
			reqEntity.addPart("sex", new StringBody(account.sex));

			reqEntity.addPart("country", new StringBody(account.country));
			reqEntity.addPart("city", new StringBody(account.city));
			reqEntity.addPart("street", new StringBody(account.street));
			reqEntity.addPart("zip", new StringBody(account.zip));

			reqEntity.addPart("timezone", new StringBody(account.timezone));
			reqEntity.addPart("phone1", new StringBody(account.phone1));
			reqEntity.addPart("phone2", new StringBody(account.phone2));
			reqEntity.addPart("phone3", new StringBody(account.phone3));

			reqEntity.addPart("language", new StringBody("en"));

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (!success) {
							Log.e("Change account ERROR", object.getJSONObject("error").getString("reason"));
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/account_edit", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}

		} catch (Exception ex) {
			Data.setERROR(ex.getMessage());
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		// image
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_image");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("token", new StringBody(Data.getToken()));

			if (removeImage == false) {
				if (account.image_bytes != null) {
					ByteArrayBody bab = new ByteArrayBody(account.image_bytes, "image");
					reqEntity.addPart("image", bab);
				}
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("0")));
			} else {
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("1")));
			}

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (!success) {
							Log.e("Change account image ERROR", object.getJSONObject("error").getString("reason"));
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/account_image", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}

		} catch (Exception ex) {
			Data.setERROR(ex.getMessage());
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		return success;
	}

	public Account getAccountFromRemoteDb() {
		boolean success = false;
		Account u = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			post.setEntity(reqEntity);

			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == true) {
						JSONObject profile = object.getJSONObject("profile");
						u = new Account();

						ContentValues cv = new ContentValues();

						try {
							u.user_id = profile.getInt("user_id");
							cv.put(AccountProvider.AMetaData.AccountMetaData.A_ID, u.user_id);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.name = profile.getString("name");
							cv.put(AccountProvider.AMetaData.AccountMetaData.NAME, u.name);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.lastname = profile.getString("lastname");
							cv.put(AccountProvider.AMetaData.AccountMetaData.LASTNAME, u.lastname);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.fullname = profile.getString("fullname");
							cv.put(AccountProvider.AMetaData.AccountMetaData.FULLNAME, u.fullname);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.birthdate = profile.getString("birthdate");
							cv.put(AccountProvider.AMetaData.AccountMetaData.BIRTHDATE, u.birthdate);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.sex = profile.getString("sex");
							cv.put(AccountProvider.AMetaData.AccountMetaData.SEX, u.sex);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.email = profile.getString("email");
							cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL, u.email);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.email2 = profile.getString("email2");
							cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL2, u.email2);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.email3 = profile.getString("email3");
							cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL3, u.email3);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.email4 = profile.getString("email4");
							cv.put(AccountProvider.AMetaData.AccountMetaData.EMAIL4, u.email4);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.phone1 = profile.getString("phone1");
							cv.put(AccountProvider.AMetaData.AccountMetaData.PHONE1, u.phone1);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.phone2 = profile.getString("phone2");
							cv.put(AccountProvider.AMetaData.AccountMetaData.PHONE2, u.phone2);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.phone3 = profile.getString("phone3");
							cv.put(AccountProvider.AMetaData.AccountMetaData.PHONE3, u.phone3);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.image = profile.getBoolean("image");
							cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE, u.image);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.image_url = profile.getString("image_url");
							cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_URL, u.image_url);

							u.image_bytes = imageToBytes(u.image_url);
							cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_BYTES, u.image_bytes);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.image_thumb_url = profile.getString("image_thumb_url");
							cv.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_THUMB_URL, u.image_thumb_url);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.country = profile.getString("country");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COUNTRY, u.country);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.city = profile.getString("city");
							cv.put(AccountProvider.AMetaData.AccountMetaData.CITY, u.city);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.street = profile.getString("street");
							cv.put(AccountProvider.AMetaData.AccountMetaData.STREET, u.street);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.zip = profile.getString("zip");
							cv.put(AccountProvider.AMetaData.AccountMetaData.ZIP, u.zip);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.timezone = profile.getString("timezone");
							cv.put(AccountProvider.AMetaData.AccountMetaData.TIMEZONE, u.timezone);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.local_time = profile.getString("local_time");
							cv.put(AccountProvider.AMetaData.AccountMetaData.LOCAL_TIME, u.local_time);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.language = profile.getString("language");
							cv.put(AccountProvider.AMetaData.AccountMetaData.LANGUAGE, u.language);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setting_default_view = profile.getString("setting_default_view");
							cv.put(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, u.setting_default_view);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setting_date_format = profile.getString("setting_date_format");
							cv.put(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT, u.setting_date_format);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setting_ampm = profile.getInt("setting_ampm");
							cv.put(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM, u.setting_ampm);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.google_calendar_link = profile.getString("google_calendar_link");
							cv.put(AccountProvider.AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK, u.google_calendar_link);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.color_my_event = profile.getString("color_my_event");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_MY_EVENT, u.color_my_event);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.color_attending = profile.getString("color_attending");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_ATTENDING, u.color_attending);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.color_pending = profile.getString("color_pending");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_PENDING, u.color_pending);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.color_invitation = profile.getString("color_invitation");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_INVINTATION, u.color_invitation);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.color_notes = profile.getString("color_notes");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_NOTES, u.color_notes);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.color_birthday = profile.getString("color_birthday");
							cv.put(AccountProvider.AMetaData.AccountMetaData.COLOR_BIRTHDAY, u.color_birthday);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.created = profile.getString("created");
							cv.put(AccountProvider.AMetaData.AccountMetaData.CREATED, u.created);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.modified = profile.getString("modified");
							cv.put(AccountProvider.AMetaData.AccountMetaData.MODIFIED, u.modified);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						Data.getmContext().getContentResolver().insert(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, cv);

					} else {
						Data.setERROR(object.getJSONObject("error").getString("reason"));
						Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
								.toString(), object.getJSONObject("error").getString("reason"));
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		Data.setAccount(u);
		Data.setLoadAccountData(false);
		return u;
	}

	public Account getAccountFromLocalDb() {
		Account u = null;

		Cursor result = Data.getmContext().getContentResolver()
				.query(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, null, null, null, null);

		if (result.moveToFirst()) {
			u = new Account();

			u.user_id = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.A_ID));

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
		return u;
	}

	public boolean registerAccount(String language, String country, String timezone, String sex, String name, String lastname,
			String email, String phonecode, String phone, String password, String city, String street, String streetNo, String zip) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_register");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("language", new StringBody(language));
			reqEntity.addPart("country", new StringBody(country));
			reqEntity.addPart("timezone", new StringBody(timezone));
			reqEntity.addPart("sex", new StringBody(sex));
			reqEntity.addPart("name", new StringBody(name));
			reqEntity.addPart("lastname", new StringBody(lastname));
			reqEntity.addPart("email", new StringBody(email));
			reqEntity.addPart("phone1_code", new StringBody(phonecode));
			reqEntity.addPart("phone1", new StringBody(phone));
			reqEntity.addPart("password", new StringBody(password));
			reqEntity.addPart("confirm_password", new StringBody(password));
			reqEntity.addPart("city", new StringBody(city));
			reqEntity.addPart("street", new StringBody(street + " " + streetNo));
			reqEntity.addPart("zip", new StringBody(zip));

			post.setEntity(reqEntity);

			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						Data.setERROR(object.getJSONObject("error").getString("reason"));
					}
				}
			}

		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		return success;
	}

	public boolean changeEmail(String email, int email_id) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_email_change");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("password", new StringBody(Data.getPassword()));
			reqEntity.addPart("email", new StringBody(email));
			if (email_id > 1)
				reqEntity.addPart("email_id", new StringBody(String.valueOf(email_id)));

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						Log.e("resp", resp);
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (!success) {
							Log.e("Change email ERROR", object.getJSONObject("error").getString("reason"));
						}
					}
				}
			} else {
				// OfflineData uplooad = new
				// OfflineData("mobile/account_email_change", reqEntity);
				// Data.getUnuploadedData().add(uplooad);
			}

		} catch (Exception ex) {
			Data.setERROR(ex.getMessage());
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		return success;
	}
	
	
	public boolean changeCalendarSettings(int am_pm, String defaultview, String dateformat) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/settings_update");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			reqEntity.addPart("setting_ampm", new StringBody(String.valueOf(am_pm)));
			reqEntity.addPart("setting_default_view", new StringBody(defaultview));
			reqEntity.addPart("setting_date_format", new StringBody(dateformat));

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (!success) {
							Log.e("Edit settings ERROR", object.getJSONObject("error").getString("reason"));
						}
					}
				}
			} else {
				// OfflineData uplooad = new
				// OfflineData("mobile/settings_update", reqEntity);
				// Data.getUnuploadedData().add(uplooad);
			}
			
			
			
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			success = false;
		}

		return success;
	}

	public boolean login(String email, String password) {
		boolean success = false;
		String token = null;

		if (networkAvailable) {
			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/login");
				post.setHeader("User-Agent", "Linux; AndroidPhone " + android.os.Build.VERSION.RELEASE);
				post.setHeader("Accept", "*/*");
				// post.setHeader("Content-Type", "text/vnd.ms-sync.wbxml");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("email", new StringBody(email));
				reqEntity.addPart("password", new StringBody(password));

				post.setEntity(reqEntity);

				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (success == true) {
							token = object.getString("token");
							JSONObject profile = object.getJSONObject("profile");
							int id = Integer.parseInt(profile.getString("user_id"));
							Data.setToken(token);
							Data.setUserId(id);

							// Last login set
							hc = new DefaultHttpClient();
							post = new HttpPost(Data.getServerUrl() + "mobile/set_lastlogin");

							reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

							reqEntity.addPart("Token", new StringBody(token));

							post.setEntity(reqEntity);

							rp = hc.execute(post);
							//

							//
							Data.setEmail(email);
							Data.setPassword(password);
							Data.setLogged(true);
							//
							Data.save();

							// autoicons and autocolors
							Data.getmContext().getContentResolver()
									.delete(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, "", null);
							JSONArray autoicons = object.getJSONArray("custom_icons");
							for (int i = 0, l = autoicons.length(); i < l; i++) {
								final JSONObject autoicon = autoicons.getJSONObject(i);
								ContentValues values = new ContentValues();
								values.put(AccountProvider.AMetaData.AutoiconMetaData.ICON, autoicon.getString("icon"));
								values.put(AccountProvider.AMetaData.AutoiconMetaData.KEYWORD, autoicon.getString("keyword"));
								values.put(AccountProvider.AMetaData.AutoiconMetaData.CONTEXT, autoicon.getString("context"));

								Data.getmContext().getContentResolver()
										.insert(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, values);
							}

							Data.getmContext().getContentResolver()
									.delete(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, "", null);
							JSONArray autocolors = object.getJSONArray("custom_colors");
							for (int i = 0, l = autocolors.length(); i < l; i++) {
								final JSONObject autocolor = autocolors.getJSONObject(i);
								ContentValues values = new ContentValues();
								values.put(AccountProvider.AMetaData.AutocolorMetaData.COLOR, autocolor.getString("color"));
								values.put(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD, autocolor.getString("keyword"));
								values.put(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT, autocolor.getString("context"));

								Data.getmContext().getContentResolver()
										.insert(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, values);
							}
							registerPhone();
						} else {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Reporter.reportError(DataManagement.class.toString(), "login", Data.getERROR());
						}
					}
				}

			} catch (Exception ex) {
				Data.setERROR(ex.getMessage());
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
			}
		} else {
			if (Data.getEmail().equals(email) && Data.getPassword().equals(password)) {
				success = true;
				Data.setLogged(true);
				Data.needToClearData = false;
			}
		}
		return success;
	}

	public void registerPhone() {
		try {
			getImei(Data.getmContext());
			Data.setPushId(C2DMessaging.getRegistrationId(Data.getmContext()));
			if (Data.getPushId() == "") {

				C2DMessaging.register(Data.getmContext(), "group.agenda.c2dm@gmail.com");
			} else {
				sendPushIdToServer(Data.getmContext(), Data.getPushId());
			}
		} catch (Exception e) {
			Reporter.reportError(DataManagement.class.toString(), "registerPhone", e.getMessage().toString());
		}
	}

	public static String getImei(Context context) {

		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			if (telephonyManager == null) {
				return "";
			}

			return telephonyManager.getDeviceId();
		} catch (Exception ex) {
			Reporter.reportError(DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		return "";
	}

	public static HttpURLConnection sendHttpRequest(String path, String method, List<NameValuePair> paramsList,
			List<NameValuePair> propertyList) {
		HttpURLConnection connection = null;

		String KContentType = "multipart/form-data; boundary=AaB03x";
		String KStartContent = "--AaB03x";
		String KEndContent = "--AaB03x--";
		String KCrlf = "\r\n";

		try {
			URL url = null;
			if (method.equals("POST")) {
				url = new URL(path);
			} else if (method.equals("GET")) {
				url = new URL(path + "?" + URLEncodedUtils.format(paramsList, "utf-8"));
			}
			connection = (HttpURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			connection.setRequestMethod(method);

			if (method.equals("POST")) {
				connection.setRequestProperty("Content-Type", KContentType);
			}

			for (int i = 0; i < propertyList.size(); i++) {
				NameValuePair param = paramsList.get(i);
				connection.setRequestProperty(param.getName(), param.getValue());
			}

			if (method.equals("POST")) {
				DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

				for (int i = 0; i < paramsList.size(); i++) {
					NameValuePair param = paramsList.get(i);

					/*
					 * System.out.println("param[" + i + "]");
					 * System.out.println("param.getName(): " +
					 * param.getName()); System.out.println("param.getValue(): "
					 * + param.getValue());
					 */

					outputStream.writeBytes(KStartContent);
					outputStream.writeBytes(KCrlf);
					outputStream.writeBytes("Content-Disposition: form-data; name=\"" + param.getName() + "\"");
					outputStream.writeBytes(KCrlf);
					outputStream.writeBytes(KCrlf);
					outputStream.write(param.getValue().getBytes("utf-8"));
					outputStream.writeBytes(KCrlf);
				}

				outputStream.writeBytes(KEndContent);
				outputStream.writeBytes(KCrlf);

				outputStream.flush();
				outputStream.close();
			} else if (method.equals("GET")) {
				connection.connect();
			}

			return connection;

		} catch (Exception ex) {
			Reporter.reportError(DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		return null;
	}

	public static void sendPushIdToServer(Context context, String pushId) {

		try {

			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/register_android");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("android_id", new StringBody(pushId));

			post.setEntity(reqEntity);

			HttpResponse rp = hc.execute(post);

		} catch (Exception ex) {
			Reporter.reportError(DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
	}

	public boolean setAutoIcons() {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/settings_set_autoicons");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("token", new StringBody(Data.getToken()));

			Cursor result = Data.getmContext().getContentResolver()
					.query(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, null, null, null, null);
			result.moveToFirst();

			int i = 1;
			while (!result.isAfterLast()) {

				reqEntity.addPart("autoicon[" + i + "][icon]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.ICON))));
				reqEntity.addPart("autoicon[" + i + "][keyword]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.KEYWORD))));
				reqEntity.addPart("autoicon[" + i + "][context]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.CONTEXT))));

				i++;
				result.moveToNext();
			}

			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						Data.setERROR(object.getJSONObject("error").getString("reason"));
						Log.e("set autoicon - error: ", Data.getERROR());
					}
				}
			}

		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}

		return success;
	}

	public ArrayList<AutoIconItem> getAutoIcons() {
		ArrayList<AutoIconItem> Items = new ArrayList<AutoIconItem>();

		Cursor result = Data.getmContext().getContentResolver()
				.query(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, null, null, null, null);
		result.moveToFirst();

		while (!result.isAfterLast()) {

			final AutoIconItem item = new AutoIconItem();

			item.id = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.I_ID));
			item.icon = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.ICON));
			item.keyword = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.KEYWORD));
			item.context = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.CONTEXT));

			result.moveToNext();

			Items.add(item);
		}

		return Items;
	}

	public boolean setAutoColors() {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/settings_set_autocolors");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("token", new StringBody(Data.getToken()));

			Cursor result = Data.getmContext().getContentResolver()
					.query(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, null, null, null, null);
			result.moveToFirst();

			int i = 1;
			while (!result.isAfterLast()) {

				reqEntity.addPart("autocolor[" + i + "][color]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.COLOR))));
				reqEntity.addPart("autocolor[" + i + "][keyword]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD))));
				reqEntity.addPart("autocolor[" + i + "][context]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT))));

				i++;
				result.moveToNext();
			}

			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						Data.setERROR(object.getJSONObject("error").getString("reason"));
						Log.e("set autocolor - error: ", Data.getERROR());
					}
				}
			}

		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}

		return success;
	}

	public ArrayList<AutoColorItem> getAutoColors() {
		ArrayList<AutoColorItem> Items = new ArrayList<AutoColorItem>();

		Cursor result = Data.getmContext().getContentResolver()
				.query(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, null, null, null, null);
		result.moveToFirst();

		while (!result.isAfterLast()) {

			final AutoColorItem item = new AutoColorItem();

			item.id = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.C_ID));
			item.color = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.COLOR));
			item.keyword = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD));
			item.context = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT));

			Items.add(item);

			result.moveToNext();
		}

		return Items;
	}

	// Contacts
	public ArrayList<Contact> getContacts() {
		ArrayList<Contact> contacts = Data.getContacts();
		if (contacts == null)
			contacts = new ArrayList<Contact>();
		return contacts;
	}

	public int loadContacts(Activity instance, ContactsAdapter cAdapter) {
		int contactsSize = 0;

		if (isLoadContactsData()) {
			Data.setContacts(getContactsFromRemoteDb(null));
			updateContactsAdapter(Data.getContacts(), cAdapter);
			contactsSize = Data.getContacts().size();
		} else {
			Data.setContacts(getContactsFromLocalDb(""));
			updateContactsAdapter(Data.getContacts(), cAdapter);
			contactsSize = Data.getContacts().size();
		}

		return contactsSize;
	}

	public ArrayList<Contact> getContactsFromLocalDb(String where) {
		Contact item;
		ArrayList<Contact> items = new ArrayList<Contact>();

		if (where.length() > 0)
			where += " AND ";

		where += ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE + "!=3";

		Cursor result = Data.getmContext().getContentResolver()
				.query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, null, where, null, null);

		result.moveToFirst();

		while (!result.isAfterLast()) {
			item = new Contact();

			item.contact_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
			item.name = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.NAME));
			item.lastname = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME));
			item.email = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
			item.phone1 = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.PHONE));
			item.birthdate = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE));
			item.country = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY));
			item.city = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CITY));
			item.street = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.STREET));
			item.zip = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.ZIP));
			item.visibility = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY));
			item.zip = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.ZIP));
			final int image = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE));
			item.image = image == 1;
			item.image_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL));
			item.image_thumb_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL));
			item.image_bytes = result.getBlob(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES));
			item.created = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CREATED));
			item.modified = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.MODOFIED));
			item.agenda_view = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW));
			item.registered = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED));
			final String groups = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.GROUPS));
			if (groups != null) {
				item.groups = MapUtils.stringToMap(groups);
			}

			items.add(item);
			result.moveToNext();
		}
		result.close();
		return items;
	}

	public ArrayList<Contact> getContactsFromRemoteDb(HashSet<Integer> groupIds) {
		boolean success = false;
		String error = null;
		ArrayList<Contact> contacts = null;
		Contact contact = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_list");
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			if (groupIds != null) {
				Iterator<Integer> it = groupIds.iterator();
				while (it.hasNext()) {
					reqEntity.addPart("group_id[]", new StringBody(String.valueOf(it.next())));
				}
			}

			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("error");
						Log.e("getContactList - error: ", error);
					} else {
						JSONArray cs = object.getJSONArray("contacts");
						int count = cs.length();
						if (count > 0) {
							contacts = new ArrayList<Contact>(count);
							for (int i = 0; i < count; i++) {
								JSONObject c = cs.getJSONObject(i);
								contact = new Contact();
								ContentValues cv = new ContentValues();

								try {
									contact.contact_id = c.getInt(ContactsProvider.CMetaData.ContactsMetaData.C_ID);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.C_ID, contact.contact_id);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									contact.name = c.getString(ContactsProvider.CMetaData.ContactsMetaData.NAME);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, contact.name);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									contact.lastname = c.getString(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, contact.lastname);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.email = c.getString(ContactsProvider.CMetaData.ContactsMetaData.EMAIL);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, contact.email);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.phone1 = c.getString(ContactsProvider.CMetaData.ContactsMetaData.PHONE);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, contact.phone1);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.birthdate = c.getString(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE);
									Calendar birthdateCalendar;
									if (!contact.birthdate.equals("null")) {
										birthdateCalendar = Utils.stringToCalendar(contact.birthdate, DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
										birthdateCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
										Event birthdayEvent = new Event();
										birthdayEvent.title = contact.name + " " + contact.lastname + " "
												+ Data.getmContext().getString(R.string.contact_birthday);
										birthdayEvent.startCalendar = birthdateCalendar;
										birthdayEvent.endCalendar = birthdateCalendar;
										birthdayEvent.my_time_start = Utils.formatCalendar(birthdateCalendar, SERVER_TIMESTAMP_FORMAT);
										birthdayEvent.my_time_end = Utils.formatCalendar(birthdateCalendar, SERVER_TIMESTAMP_FORMAT);
										birthdayEvent.is_all_day = true;
										birthdayEvent.timezone = getAccount().timezone;
										createEvent(birthdayEvent);
										contactsBirthdays.add(birthdayEvent);
									}
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, contact.birthdate);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.country = c.getString(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, contact.country);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.city = c.getString(ContactsProvider.CMetaData.ContactsMetaData.CITY);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.CITY, contact.city);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.street = c.getString(ContactsProvider.CMetaData.ContactsMetaData.STREET);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.STREET, contact.street);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.zip = c.getString(ContactsProvider.CMetaData.ContactsMetaData.ZIP);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.ZIP, contact.zip);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.visibility = c.getString(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, contact.visibility);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									contact.image = c.getBoolean(ContactsProvider.CMetaData.ContactsMetaData.IMAGE);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, contact.image);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
									contact.image = false;
								}
								try {
									contact.image_thumb_url = c.getString(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL, contact.image_thumb_url);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
									contact.image_thumb_url = "false";
								}
								try {
									contact.image_url = c.getString(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL, contact.image_url);
									if (contact.image_url != null && !contact.image_url.equals("null")) {
										contact.image_bytes = imageToBytes(Data.getServerUrl() + contact.image_url);
										cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, contact.image_bytes);
									}
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
									contact.image_url = "false";
								}

								try {
									contact.created = c.getString(ContactsProvider.CMetaData.ContactsMetaData.CREATED);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.CREATED, contact.created);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.modified = c.getString(ContactsProvider.CMetaData.ContactsMetaData.MODOFIED);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.MODOFIED, contact.modified);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.agenda_view = c.getString(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW, contact.agenda_view);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.registered = c.getString(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED, contact.registered);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									contact.email = c.getString(ContactsProvider.CMetaData.ContactsMetaData.EMAIL);
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, contact.email);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									if (!c.getString("groups").equals("null") && c.getString("groups") != null) {
										try {
											JSONArray groups = c.getJSONArray("groups");
											if (groups != null) {
												Map<String, String> set = new HashMap<String, String>();
												for (int j = 0, l = groups.length(); j < l; j++) {
													set.put(String.valueOf(j), groups.getString(j));
												}
												contact.groups = set;
												cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS,
														MapUtils.mapToString(contact.groups));
											}
										} catch (JSONException e) {
											Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
													.getMethodName().toString(), e.getMessage());
										}
									}
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								contacts.add(contact);
								Data.getmContext().getContentResolver().insert(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, cv);
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		Data.setContacts(contacts);
		Data.setLoadContactsData(false);
		return contacts;
	}

	public Contact getContact(int id) {
		Contact item = new Contact();
		Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI + "/" + id);
		Cursor result = Data.getmContext().getContentResolver().query(uri, null, null, null, null);

		if (result.moveToFirst()) {
			item.contact_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
			item.name = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.NAME));
			item.lastname = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME));
			item.email = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
			item.phone1 = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.PHONE));
			item.birthdate = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE));
			item.country = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY));
			item.city = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CITY));
			item.street = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.STREET));
			item.zip = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.ZIP));
			item.visibility = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY));
			item.zip = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.ZIP));
			final int image = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE));
			item.image = image == 1;
			item.image_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL));
			item.image_thumb_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL));
			item.image_bytes = result.getBlob(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES));
			item.created = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CREATED));
			item.modified = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.MODOFIED));
			item.agenda_view = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW));
			item.registered = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED));
			final String groups = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.GROUPS));
			if (groups != null) {
				item.groups = MapUtils.stringToMap(groups);
			}
		}
		result.close();
		return item;
	}

	public boolean removeContact(int id) {
		boolean success = false;
		String error = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("contact_id", new StringBody(String.valueOf(id)));
			reqEntity.addPart("token", new StringBody(Data.getToken()));

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						Log.e("removeContact - success", "" + success);

						if (success == false) {
							error = object.getString("error");
							Log.e("removeContact - error: ", error);
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/contact_remove", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	public boolean editContact(Contact c) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			if (c.remove_image == false) {
				if (c.image_bytes != null) {
					ByteArrayBody bab = new ByteArrayBody(c.image_bytes, "image");
					reqEntity.addPart("image", bab);
				}
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("0")));
			} else {
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("1")));
			}

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			reqEntity.addPart("contact_id", new StringBody(String.valueOf(c.contact_id)));
			reqEntity.addPart("name", new StringBody(c.name));
			reqEntity.addPart("lastname", new StringBody(c.lastname));
			reqEntity.addPart("email", new StringBody(c.email));
			reqEntity.addPart("phone1", new StringBody(c.phone1));
			reqEntity.addPart("birthdate", new StringBody(c.birthdate));
			reqEntity.addPart("country", new StringBody(c.country));
			reqEntity.addPart("city", new StringBody(c.city));
			reqEntity.addPart("street", new StringBody(c.street));
			reqEntity.addPart("zip", new StringBody(c.zip));
			reqEntity.addPart("visibility", new StringBody(c.visibility));

			Map<String, String> groups = c.groups;
			if (groups != null) {
				for (int i = 0, l = groups.size(); i < l; i++) {
					reqEntity.addPart("groups[]", new StringBody(String.valueOf(groups.get(i))));
				}
			} else {
				reqEntity.addPart("groups[]", new StringBody(""));
			}

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						success = object.getBoolean("success");

						Log.e("editContact - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("editContact - error: ", Data.getERROR());
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/contact_edit", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	public boolean createContact(Contact c) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_create");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			if (c.image_bytes != null) {
				try {
					ByteArrayBody bab = new ByteArrayBody(c.image_bytes, "image");
					reqEntity.addPart("image", bab);
				} catch (Exception e) {
					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e.getMessage());
				}
			}

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			reqEntity.addPart("name", new StringBody(c.name));
			if(c.lastname != null){
				reqEntity.addPart("lastname", new StringBody(c.lastname));
			} else {
				reqEntity.addPart("lastname", new StringBody(""));
			}
			if(c.email != null){
				reqEntity.addPart("email", new StringBody(c.email));
			} else{
				reqEntity.addPart("email", new StringBody(""));
			}
			if(c.phone1 != null){
				reqEntity.addPart("phone1", new StringBody(c.phone1));
			} else {
				reqEntity.addPart("phone1", new StringBody(""));
			}
			if(c.birthdate != null){
				reqEntity.addPart("birthdate", new StringBody(c.birthdate));
			} else {
				reqEntity.addPart("birthdate", new StringBody(""));
			}
			if(c.country != null){
				reqEntity.addPart("country", new StringBody(c.country));
			} else {
				reqEntity.addPart("country", new StringBody(""));
			}
			if(c.city != null){
				reqEntity.addPart("city", new StringBody(c.city));
			} else {
				reqEntity.addPart("city", new StringBody(""));
			}
			if(c.street != null){
				reqEntity.addPart("street", new StringBody(c.street));
			} else {
				reqEntity.addPart("street", new StringBody(""));
			}
			if(c.zip != null){
				reqEntity.addPart("zip", new StringBody(c.zip));
			} else {
				reqEntity.addPart("zip", new StringBody(""));
			}
			if(c.visibility != null){
				reqEntity.addPart("visibility", new StringBody(c.visibility));
			} else {
				reqEntity.addPart("visibility", new StringBody("n"));
			}

			Map<String, String> groups = c.groups;
			if (groups != null) {
				for (int i = 0, l = groups.size(); i < l; i++) {
					reqEntity.addPart("groups[]", new StringBody(String.valueOf(groups.get(i))));
				}
			} else {
				reqEntity.addPart("groups[]", new StringBody(""));
			}

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						Log.e("createContact - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("createContact - error: ", Data.getERROR());
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/contact_create", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	// GROUPS
	public ArrayList<Group> getGroups() {
		ArrayList<Group> groups = Data.getGroups();
		if (groups == null)
			groups = new ArrayList<Group>();
		return groups;
	}

	public int loadGroups(Activity instance, GroupsAdapter gAdapter) {
		int groupsSize = 0;

		if (DataManagement.isLoadGroupsData()) {
			Data.setGroups(getGroupsFromRemoteDb());
			updateGroupsAdapter(Data.getGroups(), gAdapter);
			groupsSize = Data.getGroups().size();
		} else {
			Data.setGroups(getGroupsFromLocalDb());
			updateGroupsAdapter(Data.getGroups(), gAdapter);
			groupsSize = Data.getGroups().size();
		}

		return groupsSize;
	}

	public Account getAccount() {
		return Data.getAccount();
	}

	public Account loadAccount() {
		Account acc = new Account();

		if (DataManagement.isLoadAccountData()) {
			acc = getAccountFromRemoteDb();
		} else {
			acc = getAccountFromLocalDb();
		}

		return acc;
	}

	public ArrayList<Group> getGroupsFromLocalDb() {
		Group item;
		ArrayList<Group> items = new ArrayList<Group>();

		String where = ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE + "!=3";

		Cursor result = Data.getmContext().getContentResolver()
				.query(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, null, where, null, null);

		result.moveToFirst();

		while (!result.isAfterLast()) {
			item = new Group();

			item.group_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
			item.title = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.TITLE));
			item.created = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CREATED));
			item.modified = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED));
			item.deleted = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.DELETED));
			final int image = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE));
			item.image = image == 1;
			item.image_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL));
			item.image_thumb_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL));
			item.image_bytes = result.getBlob(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES));
			item.contact_count = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS_COUNT));
			final String contacts = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS));
			if (contacts != null) {
				item.contacts = MapUtils.stringToMap(contacts);
			}

			items.add(item);
			result.moveToNext();
		}
		result.close();
		return items;
	}

	public ArrayList<Group> getGroupsFromRemoteDb() {
		boolean success = false;
		String error = null;
		ArrayList<Group> groups = null;
		Group group = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_list");
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("error");
						Log.e("getGroupList - error: ", error);
					} else {
						JSONArray gs = object.getJSONArray("groups");
						int count = gs.length();
						if (count > 0) {
							groups = new ArrayList<Group>(count);

							for (int i = 0; i < count; i++) {
								JSONObject g = gs.getJSONObject(i);
								group = new Group();

								ContentValues cv = new ContentValues();

								try {
									group.group_id = g.getInt("group_id");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.G_ID, group.group_id);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									group.title = g.getString("title");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, group.title);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									group.created = g.getString("created");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.CREATED, group.created);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									group.modified = g.getString("modified");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED, group.modified);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									group.deleted = g.getString("deleted");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.DELETED, group.deleted);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									group.image = g.getBoolean("image");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, group.image);
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, false);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									group.image_thumb_url = g.getString("image_thumb_url");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, group.image_thumb_url);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}
								try {
									group.image_url = g.getString("image_url");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, group.image_url);

									group.image_bytes = imageToBytes(group.image_url);
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, group.image_bytes);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								try {
									group.contact_count = g.getInt("contact_count");
									cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS_COUNT, group.contact_count);
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
									group.contact_count = 0;
								}
								try {
									if (!g.getString("contacts").equals("null") && g.getString("contacts") != null) {
										try {
											JSONArray contacts = g.getJSONArray("contacts");
											if (contacts != null) {
												Map<String, String> set = null;
												for (int j = 0; j < group.contact_count; j++) {
													if (set == null)
														set = new HashMap<String, String>();
													set.put(String.valueOf(j), contacts.getString(j));
												}
												group.contacts = set;
												cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS,
														MapUtils.mapToString(group.contacts));
											}
										} catch (JSONException e) {
											Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
													.getMethodName().toString(), e.getMessage());
										}
									}
								} catch (JSONException e) {
									Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
											.getMethodName().toString(), e.getMessage());
								}

								if (group.deleted == null || group.deleted.equals("null")) {
									groups.add(group);
									Data.getmContext().getContentResolver()
											.insert(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, cv);
								}
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		Data.setGroups(groups);
		Data.setLoadGroupsData(false);
		return groups;
	}

	public Group getGroup(Context context, int id) {
		Group group = new Group();
		Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI + "/" + id);
		Cursor result = context.getContentResolver().query(uri, null, null, null, null);

		if (result.moveToFirst()) {
			group.group_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
			group.title = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.TITLE));
			group.created = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CREATED));
			group.modified = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED));
			group.deleted = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.DELETED));
			final int image = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE));
			group.image = image == 1;
			group.image_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL));
			group.image_thumb_url = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL));
			group.image_bytes = result.getBlob(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES));
			final int remove_image = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE));
			group.remove_image = remove_image == 1;
			group.contact_count = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS_COUNT));
			final String contacts = result.getString(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS));
			if (contacts != null) {
				group.contacts = MapUtils.stringToMap(contacts);
			}
		}
		result.close();
		return group;
	}

	public boolean removeGroup(int group_id) {
		boolean success = false;
		String error = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/group_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("group_id", new StringBody(String.valueOf(group_id)));
			reqEntity.addPart("token", new StringBody(Data.getToken()));

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						if (success == false) {
							error = object.getString("error");
							Log.e("removeGroup - error: ", error);
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/group_remove", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	public boolean editGroup(Group g) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			if (g.remove_image == false) {
				if (g.image_bytes != null) {
					ByteArrayBody bab = new ByteArrayBody(g.image_bytes, "image");
					reqEntity.addPart("image", bab);
				}
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("0")));
			} else {
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("1")));
			}

			reqEntity.addPart("group_id", new StringBody(String.valueOf(g.group_id)));
			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("title", new StringBody(g.title));

			Map<String, String> contacts = g.contacts;
			if (contacts != null) {
				for (int i = 0, l = contacts.size(); i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(contacts.get(String.valueOf(i))));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody(""));
			}

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						success = object.getBoolean("success");

						Log.e("editGroup - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("editGroup - error: ", Data.getERROR());
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/groups_edit", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	public boolean createGroup(Group g) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_create");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			if (g.image_bytes != null) {
				try {
					ByteArrayBody bab = new ByteArrayBody(g.image_bytes, "image");
					reqEntity.addPart("image", bab);
				} catch (Exception e) {
					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e.getMessage());
				}
			}

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("title", new StringBody(g.title));

			Map<String, String> contacts = g.contacts;
			if (contacts != null) {
				for (int i = 0, l = contacts.size(); i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(contacts.get(String.valueOf(i))));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody(""));
			}

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						Log.e("createGroup - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("createGroup - error: ", Data.getERROR());
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/groups_create", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	// Events
	public ArrayList<Event> getEvents() {
		return Data.getEvents();
	}

	public int loadEvents(Activity instance, EventsAdapter eAdapter) {
		int eventsSize = 0;
		ArrayList<Event> events;

		if (DataManagement.isLoadEventsData()) {
			ArrayList<Event> result = getEventsFromRemoteDb("");
			if (!NavbarActivity.showInvites) {
				events = result;
				eventsSize = events.size();
			} else {
				events = filterInvites(result);
				eventsSize = events.size();
			}
			if (events.size() > 0) {
				updateEventsAdapter(events, eAdapter);
			}
			Data.setEvents(result);
		} else {
			events = AgendaUtils.getActualEvents(instance, getEventsFromLocalDb());
			ArrayList<Event> onlyInvites = null;
			if (NavbarActivity.showInvites) {
				onlyInvites = filterInvites(events);
				eventsSize = onlyInvites.size();
			} else {
				eventsSize = events.size();
			}
			if (onlyInvites != null && onlyInvites.size() > 0) {
				updateEventsAdapter(onlyInvites, eAdapter);
			} else {
				updateEventsAdapter(events, eAdapter);
			}
		}

		return eventsSize;
	}

	public ArrayList<Event> filterInvites(ArrayList<Event> events) {
		ArrayList<Event> newEventList = new ArrayList<Event>();
		for (Event event : events) {
			if (event.status == 4) {
				newEventList.add(event);
			}
		}
		return newEventList;
	}

	public ArrayList<Event> getEventsFromRemoteDb(String eventCategory) {
		boolean success = false;
		ArrayList<Event> events = new ArrayList<Event>();
		Event event = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_list");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			reqEntity.addPart("category", new StringBody(eventCategory));

			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						// error = object.getString("error");
					} else {
						JSONArray es = object.getJSONArray("events");
						int count = es.length();
						for (int i = 0; i < count; i++) {
							JSONObject e = es.getJSONObject(i);

							event = new Event();
							ContentValues cv = new ContentValues();

							try {
								event.event_id = e.getInt("event_id");
								cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.event_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.user_id = e.getInt("user_id");
								cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, event.user_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.status = e.getInt("status");
								cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.status);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								int is_owner = e.getInt("is_owner");
								event.is_owner = is_owner == 1;
								cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, event.is_owner);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.type = e.getString("type");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, event.type);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.title = e.getString("title");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, event.title);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.icon = e.getString("icon");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.icon);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.setColor(e.getString("color"));
								cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.getColor());
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.description_ = e.getString("description");
								cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, event.description_);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.location = e.getString("location");
								cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION, event.location);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.accomodation = e.getString("accomodation");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION, event.accomodation);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.cost = e.getString("cost");
								cv.put(EventsProvider.EMetaData.EventsMetaData.COST, event.cost);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.take_with_you = e.getString("take_with_you");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU, event.take_with_you);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.go_by = e.getString("go_by");
								cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY, event.go_by);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.country = e.getString("country");
								cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY, event.country);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.city = e.getString("city");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CITY, event.city);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.street = e.getString("street");
								cv.put(EventsProvider.EMetaData.EventsMetaData.STREET, event.street);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.zip = e.getString("zip");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP, event.zip);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.timezone = e.getString("timezone");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE, event.timezone);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.time_start = e.getString("time_start");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START, event.time_start);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.time_end = e.getString("time_end");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END, event.time_end);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.time = e.getString("time");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIME, event.time);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.my_time_start = e.getString("my_time_start");
								cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START, event.my_time_start);
								event.startCalendar = Utils.stringToCalendar(event.my_time_start, SERVER_TIMESTAMP_FORMAT);

							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.my_time_end = e.getString("my_time_end");
								cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END, event.my_time_end);
								event.endCalendar = Utils.stringToCalendar(event.my_time_end, SERVER_TIMESTAMP_FORMAT);

							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.reminder1 = e.getString("reminder1");
								cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.reminder1);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.reminder2 = e.getString("reminder2");
								cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.reminder2);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.reminder3 = e.getString("reminder3");
								cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.reminder3);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.created = e.getString("created");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED, event.created);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.modified = e.getString("modified");
								cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED, event.modified);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.attendant_1_count = e.getInt("attendant_1_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT, event.attendant_1_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.attendant_2_count = e.getInt("attendant_2_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT, event.attendant_2_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.attendant_0_count = e.getInt("attendant_0_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT, event.attendant_0_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.attendant_4_count = e.getInt("attendant_4_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT, event.attendant_4_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								int is_sports_event = e.getInt("is_sports_event");
								event.is_sports_event = is_sports_event == 1;
								cv.put(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT, event.is_sports_event);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.creator_fullname = e.getString("creator_fullname");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME, event.creator_fullname);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.creator_contact_id = e.getInt("creator_contact_id");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID, event.creator_contact_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								String assigned_contacts = e.getString("assigned_contacts");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS, assigned_contacts);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								String assigned_groups = e.getString("assigned_groups");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS, assigned_groups);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								String invited = e.getString("invited");
								cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, invited);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								int all_day = e.getInt("all_day");
								event.is_all_day = all_day == 1;
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							// //
							Data.getmContext().getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, cv);
							events.add(event);
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		Data.setLoadEventsData(false);
		if(contactsBirthdays != null && !contactsBirthdays.isEmpty()){
			events.addAll(contactsBirthdays);
		}
		sortEvents(events);
		return getNaviveCalendarEvents(events);
	}

	public void sortEvents(ArrayList<Event> events) {
		TreeMap<Calendar, ArrayList<Event>> tm = new TreeMap<Calendar, ArrayList<Event>>();
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		for (Event event : events) {
			if (!event.my_time_end.equals("null") && !event.my_time_start.equals("null")) {
				event_start = Utils.stringToCalendar(event.my_time_start, event.timezone, SERVER_TIMESTAMP_FORMAT);
				event_end = Utils.stringToCalendar(event.my_time_end, event.timezone, SERVER_TIMESTAMP_FORMAT);
				tmp_event_start = (Calendar) event_start.clone();
				int difference = 0;
				while (tmp_event_start.before(event_end)) {
					tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
					difference++;
				}
				if (difference == 0) {
					String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
					Calendar eventDay = Utils.stringToCalendar(dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					tm = putValueIntoTreeMap(tm, eventDay, event);
				} else if (difference >= 0) {
					Calendar eventDay = null;
					for (int i = 0; i < difference; i++) {
						String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
						eventDay = Utils.stringToCalendar(dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
						putValueIntoTreeMap(tm, eventDay, event);
						event_start.add(Calendar.DAY_OF_MONTH, 1);
					}
					String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_end.getTime());
					Calendar eventTmpEnd = Utils.stringToCalendar(dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					if (eventTmpEnd.after(eventDay)) {
						dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
						event_start = Utils.stringToCalendar(dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
						putValueIntoTreeMap(tm, event_start, event);
					}
				}
			}
		}
		Data.setSortedEvents(tm);
	}

	public TreeMap<Calendar, ArrayList<Event>> putValueIntoTreeMap(TreeMap<Calendar, ArrayList<Event>> tm, Calendar eventDay, Event event) {
		if (tm.containsKey(eventDay)) {
			ArrayList<Event> tmpArrayList = tm.get(eventDay);
			tmpArrayList.add(event);
			tm.put(eventDay, tmpArrayList);
		} else {
			ArrayList<Event> tmpArrayList = new ArrayList<Event>();
			tmpArrayList.add(event);
			tm.put(eventDay, tmpArrayList);
		}
		return tm;
	}

	public void putEventIntoTreeMap(Event event) {
		String date_format = SERVER_TIMESTAMP_FORMAT;
		Calendar event_start = Utils.stringToCalendar(event.my_time_start, event.timezone, date_format);
		String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
		Calendar event_day = Utils.stringToCalendar(dayStr + " 00:00:00", date_format);
		Data.setSortedEvents(putValueIntoTreeMap(Data.getSortedEvents(), event_day, event));
	}

	public ArrayList<Event> getEventsFromLocalDb() {
		Event item;
		ArrayList<Event> items = new ArrayList<Event>();
		if (Data.get_prefs().getBoolean("isAgenda", true)) {
			String where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE + " < 3";
			Cursor result = Data.getmContext().getContentResolver()
					.query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);

			result.moveToFirst();

			while (!result.isAfterLast()) {
				item = new Event();

				item.event_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID));
				item.user_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.USER_ID));

				final int is_sport_event = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT));
				item.is_sports_event = is_sport_event == 1;
				item.status = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS));
				final int is_owner = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_OWNER));
				item.is_owner = is_owner == 1;
				item.type = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TYPE));

				item.creator_fullname = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME));
				item.creator_contact_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID));

				item.title = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TITLE));
				item.icon = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ICON));
				item.setColor(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COLOR)));
				item.description_ = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.DESC));

				item.location = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.LOCATION));
				item.accomodation = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION));

				item.cost = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COST));
				item.take_with_you = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU));
				item.go_by = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.GO_BY));

				item.country = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COUNTRY));
				item.city = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CITY));
				item.street = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STREET));
				item.zip = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ZIP));

				item.timezone = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIMEZONE));
				item.time_start = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START));
				item.time_end = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END));
				item.time = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME));
				item.my_time_start = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START));
				item.startCalendar = Utils.stringToCalendar(item.my_time_start, SERVER_TIMESTAMP_FORMAT);
				// item.startCalendar.add(Calendar.DATE, -1);
				item.my_time_end = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END));
				item.endCalendar = Utils.stringToCalendar(item.my_time_end, SERVER_TIMESTAMP_FORMAT);
				// item.endCalendar.add(Calendar.DATE, 1);

				item.reminder1 = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER1));
				item.reminder2 = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER2));
				item.reminder3 = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER3));

				item.created = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATED));
				item.modified = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MODIFIED));

				item.attendant_1_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT));
				item.attendant_2_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT));
				item.attendant_0_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT));
				item.attendant_4_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT));

				items.add(item);
				result.moveToNext();
			}
			result.close();
		}

		return getNaviveCalendarEvents(items);
	}

	public Event getNativeCalendarEvent(int id) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Event item = new Event();

		Cursor cursor;
		if (Build.VERSION.SDK_INT < 14) {
			cursor = Data
					.getmContext()
					.getContentResolver()
					.query(Uri.parse("content://com.android.calendar/calendars"),
							new String[] { "_id", "title", "description", "dtstart", "dtend", "eventLocation", "eventTimezone" },
							"_id=" + id, null, null);

			if (cursor.moveToFirst()) {
				item.isNative = true;
				item.is_owner = false;
				item.type = "p";
				item.status = 1;

				item.event_id = cursor.getInt(0);
				item.title = cursor.getString(1);
				item.description_ = cursor.getString(2);
				item.timezone = cursor.getString(6);

				Date dt = new Date();
				dt.setTime(cursor.getLong(3));
				item.my_time_start = formatter.format(dt);

				dt.setTime(cursor.getLong(4));
				item.my_time_end = formatter.format(dt);
			}
		}

		return item;
	}

	public Cursor getNativeCalendars() {
		Cursor cursor;
		if (Build.VERSION.SDK_INT < 14) {
			cursor = Data
					.getmContext()
					.getContentResolver()
					.query(Uri.parse("content://com.android.calendar/calendars"), (new String[] { "_id", "displayName" }), null, null, null);
		} else {
			cursor = null;
		}
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public ArrayList<Event> getNaviveCalendarEvents(ArrayList<Event> events) {
		SimpleDateFormat formatter = new SimpleDateFormat(SERVER_TIMESTAMP_FORMAT);
		Cursor calendars = getNativeCalendars();

		if (calendars != null) {
			while (!calendars.isAfterLast()) {
				String calendar_id = calendars.getString(0);
				boolean isNative = Data.get_prefs().getBoolean("isNative_" + calendar_id, false);

				if (isNative) {
					String where = "calendar_id=" + calendar_id;
					Cursor cursor = Data
							.getmContext()
							.getContentResolver()
							.query(Uri.parse("content://com.android.calendar/calendars"),
									new String[] { "_id", "title", "description", "dtstart", "dtend", "eventLocation", "eventTimezone" },
									where, null, null);

					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						final Event item = new Event();

						item.isNative = true;
						item.is_owner = false;
						item.type = "p";
						item.status = 1;

						item.event_id = cursor.getInt(0);
						item.title = cursor.getString(1);
						item.description_ = cursor.getString(2);
						item.timezone = cursor.getString(6);

						Date dt = new Date();
						dt.setTime(cursor.getLong(3));
						item.startCalendar = Calendar.getInstance();
						item.startCalendar.setTime(dt);
						item.my_time_start = formatter.format(dt);

						long endLong = cursor.getLong(4);
						item.endCalendar = Calendar.getInstance();
						if (endLong > 0) {
							dt.setTime(endLong);
							item.endCalendar.setTime(dt);
						} else {
							dt.setTime(cursor.getLong(3));
							item.endCalendar.setTime(dt);
							item.endCalendar.add(Calendar.HOUR_OF_DAY, 1);
						}
						item.my_time_end = formatter.format(dt);

						events.add(item);
						cursor.moveToNext();
					}
				}

				calendars.moveToNext();
			}
		}
		Data.setEvents(events);
		return events;
	}

	public ArrayList<CEvent> getCalendarEvents() {
		CEvent citem;
		ArrayList<CEvent> citems = new ArrayList<CEvent>();
		ArrayList<Event> items = new ArrayList<Event>();

		items = getEventsFromLocalDb();

		for (int i = 0, l = items.size(); i < l; i++) {
			final Event item = items.get(i);

			citem = EventsHelper.generateEvent(item);
			citems.add(citem);
		}

		return citems;
	}

	public Event getEventFromDb(int event_id) {
		Event item = new Event();
		Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + event_id);
		Cursor result = Data.getmContext().getContentResolver().query(uri, null, null, null, null);
		if (result.moveToFirst()) {
			item.event_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID));
			item.user_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.USER_ID));

			final int is_sport_event = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT));
			item.is_sports_event = is_sport_event == 1;
			item.status = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS));
			final int is_owner = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_OWNER));
			item.is_owner = is_owner == 1;
			item.type = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TYPE));

			item.creator_fullname = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME));
			item.creator_contact_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID));

			item.title = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TITLE));
			item.icon = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ICON));
			item.setColor(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COLOR)));
			item.description_ = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.DESC));

			item.location = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.LOCATION));
			item.accomodation = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION));

			item.cost = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COST));
			item.take_with_you = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU));
			item.go_by = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.GO_BY));

			item.country = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COUNTRY));
			item.city = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CITY));
			item.street = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STREET));
			item.zip = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ZIP));

			item.timezone = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIMEZONE));
			item.time_start = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START));
			item.time_end = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END));
			item.time = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME));
			item.my_time_start = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START));
			item.my_time_end = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END));

			item.reminder1 = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER1));
			item.reminder2 = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER2));
			item.reminder3 = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER3));

			item.created = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATED));
			item.modified = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MODIFIED));

			item.attendant_1_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT));
			item.attendant_2_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT));
			item.attendant_0_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT));
			item.attendant_4_count = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT));

			String assigned_contacts = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS));
			if (assigned_contacts != null && !assigned_contacts.equals("null")) {
				try {
					item.assigned_contacts = Utils.jsonStringToArray(assigned_contacts);
				} catch (JSONException e) {
					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e.getMessage());
					item.assigned_contacts = null;
				}
			}

			String assigned_groups = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS));
			if (assigned_groups != null && !assigned_groups.equals("null")) {
				try {
					item.assigned_groups = Utils.jsonStringToArray(assigned_groups);
				} catch (JSONException e) {
					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e.getMessage());
					item.assigned_groups = null;
				}
			}

			String invitedJson = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.INVITED));
			if (invitedJson != null && !invitedJson.equals("null")) {
				try {
					JSONArray arr = new JSONArray(invitedJson);
					if (arr.length() > 0) {
						item.invited = new ArrayList<Invited>();
					}
					for (int i = 0, l = arr.length(); i < l; i++) {
						JSONObject obj = arr.getJSONObject(i);

						final Invited invited = new Invited();

						try {
							invited.status_id = obj.getInt("status");

							if (invited.status_id == 4) {
								invited.status = Data.getmContext().getString(R.string.status_2);
							} else {
								String statusStr = new StringBuilder("status_").append(invited.status_id).toString();
								int statusId = Data.getmContext().getResources()
										.getIdentifier(statusStr, "string", "com.groupagendas.groupagenda");

								invited.status = Data.getmContext().getString(statusId);
							}
						} catch (JSONException ex) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), ex.getMessage());
						}

						try {
							if (!obj.getString("my_contact_id").equals("null")) {
								invited.my_contact_id = obj.getInt("my_contact_id");
								Contact contact = getContact(invited.my_contact_id);
								invited.email = contact.email;
								invited.name = contact.name + " " + contact.lastname;
								invited.contactId = contact.contact_id;
							} else if (Data.getAccount().fullname.equals(obj.getString("gname"))) {
								invited.name = Data.getmContext().getString(R.string.you);
								invited.email = Data.getEmail();
								invited.me = true;
							} else {
								invited.name = obj.getString("gname");
							}
						} catch (JSONException ex) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), ex.getMessage());
						}

						item.invited.add(invited);
					}
				} catch (JSONException e) {
					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e.getMessage());
				}
			}
		}
		result.close();
		return item;
	}

	public Event getEvent(Event event) {
		boolean success = false;
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(event.event_id)));

			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						// TODO Error
					} else {
						JSONObject e = object.getJSONObject("event");

						try {
							int is_sports_event = e.getInt("is_sports_event");
							event.is_sports_event = is_sports_event == 1;
						} catch (JSONException ex) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), ex.getMessage());
						}
						try {
							event.creator_fullname = e.getString("creator_fullname");
						} catch (JSONException ex) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), ex.getMessage());
						}
						try {
							event.creator_contact_id = e.getInt("creator_contact_id");
						} catch (JSONException ex) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), ex.getMessage());
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		return event;
	}

	public boolean editEvent(Event e) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(e.event_id)));

			reqEntity.addPart("event_type", new StringBody(e.type));

			reqEntity.addPart("icon", new StringBody(e.icon));
			reqEntity.addPart("color", new StringBody(e.getColor()));

			reqEntity.addPart("title", new StringBody(e.title));

			reqEntity.addPart("time_start", new StringBody(e.my_time_start));
			reqEntity.addPart("time_end", new StringBody(e.my_time_end));
			reqEntity.addPart("timezone", new StringBody(e.timezone));

			reqEntity.addPart("description", new StringBody(e.description_));

			reqEntity.addPart("country", new StringBody(e.country));
			reqEntity.addPart("zip", new StringBody(e.zip));
			reqEntity.addPart("city", new StringBody(e.city));
			reqEntity.addPart("street", new StringBody(e.street));
			reqEntity.addPart("location", new StringBody(e.location));

			reqEntity.addPart("go_by", new StringBody(e.go_by));
			reqEntity.addPart("take_with_you", new StringBody(e.take_with_you));
			reqEntity.addPart("cost", new StringBody(e.cost));
			reqEntity.addPart("accomodation", new StringBody(e.accomodation));

			// if(){
			reqEntity.addPart("reminder1", new StringBody(e.reminder1));
			reqEntity.addPart("reminder2", new StringBody(e.reminder2));
			reqEntity.addPart("reminder3", new StringBody(e.reminder3));

			if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
				e.assigned_contacts = new int[Data.selectedContacts.size()];
				int i = 0;
				for (Contact contact : Data.selectedContacts) {
					e.assigned_contacts[i] = contact.contact_id;
					i++;
				}
			}
			if (e.assigned_contacts != null) {
				for (int i = 0, l = e.assigned_contacts.length; i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.assigned_contacts[i])));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody(""));
			}

			if (e.assigned_groups != null) {
				for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
					reqEntity.addPart("groups[]", new StringBody(String.valueOf(e.assigned_groups[i])));
				}
			} else {
				reqEntity.addPart("groups[]", new StringBody(""));
			}

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (!success) {
							Log.e("Edit event ERROR", object.getJSONObject("error").getString("reason"));
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/events_edit", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			success = false;
		}

		return success;
	}

	public boolean createEvent(Event e) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_create");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			if (e.icon != null)
				reqEntity.addPart("icon", new StringBody(e.icon));
//			if (e.color != null)
				reqEntity.addPart("color", new StringBody(e.getColor()));

			reqEntity.addPart("title", new StringBody(e.title));

			reqEntity.addPart("time_start", new StringBody(e.my_time_start));
			reqEntity.addPart("time_end", new StringBody(e.my_time_end));

			reqEntity.addPart("description", new StringBody(e.description_));

			if (e.country.length() > 0)
				reqEntity.addPart("country", new StringBody(e.country));
			if (e.city.length() > 0)
				reqEntity.addPart("city", new StringBody(e.city));
			if (e.street.length() > 0)
				reqEntity.addPart("street", new StringBody(e.street));
			if (e.zip.length() > 0)
				reqEntity.addPart("zip", new StringBody(e.zip));
			reqEntity.addPart("timezone", new StringBody(e.timezone));

			if (e.location.length() > 0)
				reqEntity.addPart("location", new StringBody(e.location));
			if (e.go_by.length() > 0)
				reqEntity.addPart("go_by", new StringBody(e.go_by));
			if (e.take_with_you.length() > 0)
				reqEntity.addPart("take_with_you", new StringBody(e.take_with_you));
			if (e.cost.length() > 0)
				reqEntity.addPart("cost", new StringBody(e.cost));
			if (e.accomodation.length() > 0)
				reqEntity.addPart("accomodation", new StringBody(e.accomodation));

			if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
				e.assigned_contacts = new int[Data.selectedContacts.size()];
				int i = 0;
				for (Contact contact : Data.selectedContacts) {
					e.assigned_contacts[i] = contact.contact_id;
					i++;
				}
			}
			if (e.assigned_contacts != null) {
				for (int i = 0, l = e.assigned_contacts.length; i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.assigned_contacts[i])));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody(""));
			}
			if (e.assigned_groups != null) {
				for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
					reqEntity.addPart("groups[]", new StringBody(String.valueOf(e.assigned_groups[i])));
				}
			} else {
				reqEntity.addPart("groups[]", new StringBody(""));
			}

			if (e.reminder1 != null) {
				reqEntity.addPart("reminder1", new StringBody(e.reminder1));
			}
			if (e.reminder2 != null) {
				reqEntity.addPart("reminder2", new StringBody(e.reminder2));
			}
			if (e.reminder3 != null) {
				reqEntity.addPart("reminder3", new StringBody(e.reminder3));
			}
			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						// Log.e("createEvent - resp", resp + "!!!");
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						// Log.e("createEvent - success", "" + success);

						if (success == false) {
							Log.e("Create event error", object.getJSONObject("error").getString("reason"));
						}
						int event_id = object.getInt("event_id");
						e.event_id = event_id;
						updateEventByIdFromRemoteDb(event_id);
					}
				} else {
					Log.e("createEvent - status", rp.getStatusLine().getStatusCode() + "");
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/events_create", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		return success;
		// return event_id;
	}

	public boolean removeEvent(int id) {
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(id)));

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						String successStr = object.getString("success");

						if (successStr.equals("null")) {
							success = false;
						} else {
							success = object.getBoolean("success");
						}

						// Log.e("removeEvent - success", "" + success);

						if (success == false) {
							// array of errors!!!
							JSONObject errObj = object.getJSONObject("error");
							Data.setERROR(errObj.getString("reason"));
							Log.e("removeEvent - error: ", Data.getERROR());
						} else {
							this.getEventsFromRemoteDb("");
						}
					}
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/events_remove", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		return success;
	}

	public boolean changeEventStatus(int event_id, String status) {
		Object[] array = { event_id, status };
		try {
			new ChangeEventStatus().execute(array).get();
		} catch (InterruptedException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e.getMessage());
		} catch (ExecutionException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e.getMessage());
		}
		return DataManagement.eventStatusChanged;
	}

	private class ChangeEventStatus extends AsyncTask<Object, Void, Void> {

		private boolean success = false;

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int event_id = (Integer) params[0];
				String status = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/set_event_status");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
				reqEntity.addPart("status", new StringBody(status));

				post.setEntity(reqEntity);
				if (networkAvailable) {
					HttpResponse rp = hc.execute(post);

					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							// Log.e("respSt", resp);
							JSONObject object = new JSONObject(resp);
							success = object.getBoolean("success");
							if (!success) {
								Log.e("Edit event status ERROR", object.getJSONObject("error").getString("reason"));
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/set_event_status", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception ex) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
				success = false;
			}
			if (success) {
				DataManagement.eventStatusChanged = true;
			} else {
				DataManagement.eventStatusChanged = false;
			}
			return null;
		}

	}

	public Event updateEventByIdFromRemoteDb(int event_id) throws ExecutionException, InterruptedException {
		new UpdateEventByIdFromRemoteDb().execute(event_id);
		return null;
	}

	private class UpdateEventByIdFromRemoteDb extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				int event_id = params[0];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_get");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				rp = hc.execute(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject e1 = new JSONObject(resp);
						boolean success = e1.getBoolean("success");
						if (!success) {
							Log.e("Edit event status ERROR", e1.getJSONObject("error").getString("reason"));
						} else {
							JSONObject e = e1.getJSONObject("event");
							Event event = new Event();
							ContentValues cv = new ContentValues();

							try {
								event.event_id = e.getInt("event_id");
								cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.event_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.user_id = e.getInt("user_id");
								cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, event.user_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.status = e.getInt("status");
								cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.status);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								int is_owner = e.getInt("is_owner");
								event.is_owner = is_owner == 1;
								cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, event.is_owner);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.type = e.getString("type");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, event.type);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.title = e.getString("title");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, event.title);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.icon = e.getString("icon");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.icon);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.setColor(e.getString("color"));
								cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.getColor());
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.description_ = e.getString("description");
								cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, event.description_);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.location = e.getString("location");
								cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION, event.location);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.accomodation = e.getString("accomodation");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION, event.accomodation);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.cost = e.getString("cost");
								cv.put(EventsProvider.EMetaData.EventsMetaData.COST, event.cost);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.take_with_you = e.getString("take_with_you");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU, event.take_with_you);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.go_by = e.getString("go_by");
								cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY, event.go_by);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.country = e.getString("country");
								cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY, event.country);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.city = e.getString("city");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CITY, event.city);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.street = e.getString("street");
								cv.put(EventsProvider.EMetaData.EventsMetaData.STREET, event.street);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.zip = e.getString("zip");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP, event.zip);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.timezone = e.getString("timezone");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE, event.timezone);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.time_start = e.getString("time_start");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START, event.time_start);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.time_end = e.getString("time_end");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END, event.time_end);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.time = e.getString("time");
								cv.put(EventsProvider.EMetaData.EventsMetaData.TIME, event.time);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.my_time_start = e.getString("my_time_start");
								cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START, event.my_time_start);
								event.startCalendar = Utils.stringToCalendar(event.my_time_start, SERVER_TIMESTAMP_FORMAT);

							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.my_time_end = e.getString("my_time_end");
								cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END, event.my_time_end);
								event.endCalendar = Utils.stringToCalendar(event.my_time_end, SERVER_TIMESTAMP_FORMAT);

							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.reminder1 = e.getString("reminder1");
								cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.reminder1);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.reminder2 = e.getString("reminder2");
								cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.reminder2);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.reminder3 = e.getString("reminder3");
								cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.reminder3);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.created = e.getString("created");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED, event.created);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.modified = e.getString("modified");
								cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED, event.modified);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								event.attendant_1_count = e.getInt("attendant_1_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT, event.attendant_1_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.attendant_2_count = e.getInt("attendant_2_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT, event.attendant_2_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.attendant_0_count = e.getInt("attendant_0_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT, event.attendant_0_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.attendant_4_count = e.getInt("attendant_4_count");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT, event.attendant_4_count);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								int is_sports_event = e.getInt("is_sports_event");
								event.is_sports_event = is_sports_event == 1;
								cv.put(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT, event.is_sports_event);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.creator_fullname = e.getString("creator_fullname");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME, event.creator_fullname);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								event.creator_contact_id = e.getInt("creator_contact_id");
								cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID, event.creator_contact_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								String assigned_contacts = e.getString("assigned_contacts");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS, assigned_contacts);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								String assigned_groups = e.getString("assigned_groups");
								cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS, assigned_groups);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								String invited = e.getString("invited");
								cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, invited);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								int all_day = e.getInt("all_day");
								event.is_all_day = all_day == 1;
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							// //
							Data.getmContext().getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, cv);
							Event tmpEvent = getEventFromDb(event_id);
							if (tmpEvent.startCalendar == null) {
								tmpEvent.startCalendar = Utils.stringToCalendar(event.my_time_start, SERVER_TIMESTAMP_FORMAT);
							}
							if (tmpEvent.endCalendar == null) {
								tmpEvent.endCalendar = Utils.stringToCalendar(event.my_time_end, SERVER_TIMESTAMP_FORMAT);
							}
							updateEventInsideLocalDb(tmpEvent);
						}
					}
				}
			} catch (Exception e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			if (Data.selectedContacts != null) {
				Data.selectedContacts.clear();
			}
			return null;
		}

	}

	public void updateEventInsideLocalDb(Event event) {
		boolean foundEventInLocalDB = false;
		ArrayList<Event> localEvents = Data.getEvents();
		for (Event tmpEvent : localEvents) {
			if (event.event_id == tmpEvent.event_id) {
				foundEventInLocalDB = true;
				localEvents.remove(tmpEvent);
				localEvents.add(event);
				break;
			}
		}
		if (!foundEventInLocalDB) {
			localEvents.add(event);
		}
		Data.setEvents(localEvents);
		sortEvents(Data.getEvents());
	}
	
	public void getChatMessages(int event_id, String from){
		if(event_id > 0){
			Object[] executeArray = {event_id, from};
			new GetChatMessages().execute(executeArray);
		}
	}
	
	private class GetChatMessages extends AsyncTask<Object, Void, Void>{

		@Override
		protected Void doInBackground(Object... params) {
			try{
				int event_id = (Integer) params[0];
				String from = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
				if(from == null){
					reqEntity.addPart("from_datetime", new StringBody(String.valueOf("")));
				} else {
					reqEntity.addPart("from_datetime", new StringBody(String.valueOf(from)));
				}

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if(networkAvailable){
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (!success) {
								Log.e("Change account ERROR", object.getJSONObject("error").getString("reason"));
							} else {
								JSONArray chatMessages = object.getJSONArray("items");
								for (int i = 0, l = chatMessages.length(); i < l; i++) {
									final JSONObject chatMessage = chatMessages.getJSONObject(i);
									ChatMessageObject message = new ChatMessageObject();
									message.messageId = chatMessage.getString("message_id");
									message.eventId = chatMessage.getString("event_id");
									message.dateTime = chatMessage.getString("datetime");
									message.dateTimeCalendar = Utils.stringToCalendar(message.dateTime, SERVER_TIMESTAMP_FORMAT);
									message.userId = chatMessage.getString("user_id");
									message.message = chatMessage.getString("message");
									String deleted = chatMessage.getString("deleted");
									message.deleted = deleted != null;
									message.updated = chatMessage.getString("updated");
									message.updatedCalendar = Utils.stringToCalendar(message.updated, SERVER_TIMESTAMP_FORMAT);
									message.fullname = chatMessage.getString("fullname");
									message.contactId = chatMessage.getString("contact_id");
									message.dateTimeConverted = chatMessage.getString("datetime_conv");
									message.dateTimeConvertedCalendar = Utils.stringToCalendar(message.dateTimeConverted, SERVER_TIMESTAMP_FORMAT);
									message.formatedDateTime = chatMessage.getString("formatted_datetime");
									Data.getChatMessages().add(message);
								}
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_get", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}
		
	}
	
	public void postChatMessage(int event_id, String message){
		if(event_id > 0){
			Object[] executeArray = {event_id, message};
			new GetChatMessages().execute(executeArray);
		}
	}
	
	public class PostChatMessage extends AsyncTask<Object, Void, Void>{

		@Override
		protected Void doInBackground(Object... params) {
			try{
				int event_id = (Integer) params[0];
				String message = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
				if(message == null){
					reqEntity.addPart("message", new StringBody(String.valueOf("")));
				} else {
					reqEntity.addPart("message", new StringBody(String.valueOf(message)));
				}

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if(networkAvailable){
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if(success){
								getChatMessages(event_id, null);
								System.out.println("Meesage posted");
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_post", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e){
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}
		
	}
	
	public void removeChatMessage (int messageId, int event_id){
		if(event_id > 0){
			Object[] executeArray = {messageId, event_id};
			new RemoveChatMessage().execute(executeArray);
		}
	}
	
	public class RemoveChatMessage extends AsyncTask<Object, Void, Void>{

		@Override
		protected Void doInBackground(Object... params) {
			try{
				int message_id = (Integer) params[0];
				int event_id = (Integer) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_remove");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("message_id", new StringBody(String.valueOf(message_id)));

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if(networkAvailable){
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if(success){
								getChatMessages(event_id, null);
								System.out.println("Message removed");
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_remove", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e){
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}
		
	}

	// /////////////////////////////////////

	private byte[] imageToBytes(String image_url) {
		if (image_url != null && !image_url.equals("null")) {
			DefaultHttpClient mHttpClient = new DefaultHttpClient();
			HttpGet mHttpGet = new HttpGet(image_url);
			HttpResponse mHttpResponse;
			try {
				mHttpResponse = mHttpClient.execute(mHttpGet);
				if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = mHttpResponse.getEntity();
					if (entity != null) {
						return EntityUtils.toByteArray(entity);
					}
				}
			} catch (ClientProtocolException e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			} catch (IOException e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return new byte[0];
		}
		return null;
	}

	public String getError() {
		return Data.getERROR();
	}

	public void setError(String error) {
		Data.setERROR(error);
	}

	public static String getCONNECTION_ERROR() {
		return Data.getConnectionError();
	}

	public static void updateAppData(int data) {
		switch (data) {
		case 1:
			Data.setLoadAccountData(true);
			break;
		case 2:
			Data.setLoadContactsData(true);
			break;
		case 3:
			Data.setLoadEventsData(true);
			break;
		case 4:
			Data.setLoadGroupsData(true);
			break;
		case 5:
			Data.setLoadAccountData(true);
			Data.setLoadContactsData(true);
			Data.setLoadEventsData(true);
			Data.setLoadGroupsData(true);
		default:
			System.out.println("UpdateAppData():" + data);
			break;
		}
	}

	public static boolean isLoadAccountData() {
		return Data.isLoadAccountData();
	}

	public static void setLoadAccountData(boolean loadAccountData) {
		Data.setLoadAccountData(loadAccountData);
	}

	public static boolean isLoadContactsData() {
		return Data.isLoadContactsData();
	}

	public static void setLoadContactsData(boolean loadContactsData) {
		Data.setLoadContactsData(loadContactsData);
	}

	public static boolean isLoadGroupsData() {
		return Data.isLoadGroupsData();
	}

	public static void setLoadGroupsData(boolean loadGroupsData) {
		Data.setLoadGroupsData(loadGroupsData);
	}

	public static boolean isLoadEventsData() {
		return Data.isLoadEventsData();
	}

	public static void setLoadEventsData(boolean loadEventsData) {
		Data.setLoadEventsData(loadEventsData);
	}

	public void updateContactsAdapter(ArrayList<Contact> contacts, ContactsAdapter cAdapter) {
		if (cAdapter != null) {
			cAdapter.setItems(contacts);
			cAdapter.notifyDataSetChanged();
		}
	}

	public void updateGroupsAdapter(ArrayList<Group> groups, GroupsAdapter gAdapter) {
		if (gAdapter != null) {
			gAdapter.setItems(groups);
			gAdapter.notifyDataSetChanged();
		}
	}

	public void updateEventsAdapter(ArrayList<Event> events, EventsAdapter eAdapter) {
		if (eAdapter != null) {
			eAdapter.setItems(events);
			eAdapter.notifyDataSetChanged();
		}
	}

	public Context getmContext() {
		return Data.getmContext();
	}
}