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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bog.calendar.app.model.CEvent;
import com.bog.calendar.app.model.EventsHelper;
import com.google.android.c2dm.C2DMessaging;
import com.google.android.gcm.GCMRegistrar;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.address.Address;
import com.groupagendas.groupagenda.address.AddressProvider.AMetaData.AddressesMetaData;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.chat.ChatThreadObject;
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
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;
import com.groupagendas.groupagenda.utils.AgendaUtils;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;

public class DataManagement {
	private static ContentResolver resolver;
	public static boolean networkAvailable = true;
	public static boolean eventStatusChanged = false;
	public static ArrayList<Event> contactsBirthdays = new ArrayList<Event>();

	
	SimpleDateFormat day_index_formatter;
	SimpleDateFormat month_index_formatter;
	private String user_timezone;
	

	public static final String SERVER_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT = "yyyy-MM-dd";

	private static final long SECONDS_PER_DAY = 24 * 60 * 60;
	private static final long MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000;

	private static final String GET_EVENTS_FROM_REMOTE_DB_URL = "mobile/events_list";
	private static final String TOKEN = "token";
	private static final String CATEGORY = "category";
	public static final String PROJECT_ID = "102163820835";

	private DataManagement(Context c) {
		Data.setPrefs(new Prefs(c));
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
		resolver = c.getContentResolver();
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

	public boolean updateAccount(boolean removeImage) {
		Account account = new Account();
		boolean success = false;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

			reqEntity.addPart(Account.AccountMetaData.LASTNAME, new StringBody(account.getLastname()));
			reqEntity.addPart(Account.AccountMetaData.NAME, new StringBody(account.getName()));

			reqEntity.addPart(Account.AccountMetaData.BIRTHDATE, new StringBody(account.getBirthdate().toString()));
			reqEntity.addPart(Account.AccountMetaData.SEX, new StringBody(account.getSex()));

			reqEntity.addPart(Account.AccountMetaData.COUNTRY, new StringBody(account.getCountry()));
			reqEntity.addPart(Account.AccountMetaData.CITY, new StringBody(account.getCity()));
			reqEntity.addPart(Account.AccountMetaData.STREET, new StringBody(account.getStreet()));
			reqEntity.addPart(Account.AccountMetaData.ZIP, new StringBody(account.getZip()));

			reqEntity.addPart(Account.AccountMetaData.TIMEZONE, new StringBody(account.getTimezone()));
			reqEntity.addPart(Account.AccountMetaData.PHONE1, new StringBody(account.getPhone1()));
			reqEntity.addPart(Account.AccountMetaData.PHONE2, new StringBody(account.getPhone2()));
			reqEntity.addPart(Account.AccountMetaData.PHONE3, new StringBody(account.getPhone3()));

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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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

						try {
							u.setUser_id(profile.getInt(Account.AccountMetaData.U_ID));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setName(profile.getString(Account.AccountMetaData.NAME));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setLastname(profile.getString(Account.AccountMetaData.LASTNAME));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setFullname(profile.getString(Account.AccountMetaData.FULLNAME));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setBirthdate(Utils.createCalendar(profile.getLong(Account.AccountMetaData.BIRTHDATE),
									profile.getString(Account.AccountMetaData.TIMEZONE)));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setSex(profile.getString(Account.AccountMetaData.SEX));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL), 0);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL2), 2);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL3), 3);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL4), 4);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setPhone(profile.getString(Account.AccountMetaData.PHONE1), 1);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setPhone(profile.getString(Account.AccountMetaData.PHONE2), 2);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setPhone(profile.getString(Account.AccountMetaData.PHONE3), 3);
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setImage(profile.getBoolean(Account.AccountMetaData.IMAGE));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setImage_url(profile.getString(Account.AccountMetaData.IMAGE_URL));
							u.image_bytes = imageToBytes(u.getImage_url());
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setImage_thumb_url(profile.getString(Account.AccountMetaData.IMAGE_THUMB_URL));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setCountry(profile.getString(Account.AccountMetaData.COUNTRY));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setCity(profile.getString(Account.AccountMetaData.CITY));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setStreet(profile.getString(Account.AccountMetaData.STREET));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setZip(profile.getString(Account.AccountMetaData.ZIP));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

						try {
							u.setTimezone(profile.getString(Account.AccountMetaData.TIMEZONE));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setLocal_time(profile.getString(Account.AccountMetaData.LOCAL_TIME));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setLanguage(profile.getString(Account.AccountMetaData.LANGUAGE));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setSetting_default_view(profile.getString(Account.AccountMetaData.SETTING_DEFAULT_VIEW));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setSetting_date_format(profile.getString(Account.AccountMetaData.SETTING_DATE_FORMAT));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setSetting_ampm(profile.getInt(Account.AccountMetaData.SETTING_AMPM));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setGoogle_calendar_link(profile.getString(Account.AccountMetaData.GOOGLE_CALENDAR_LINK));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setColor_my_event(profile.getString(Account.AccountMetaData.COLOR_MY_EVENT));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setColor_attending(profile.getString(Account.AccountMetaData.COLOR_ATTENDING));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setColor_pending(profile.getString(Account.AccountMetaData.COLOR_PENDING));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setColor_invitation(profile.getString(Account.AccountMetaData.COLOR_INVITATION));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setColor_notes(profile.getString(Account.AccountMetaData.COLOR_NOTES));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setColor_birthday(profile.getString(Account.AccountMetaData.COLOR_BIRTHDAY));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setCreated(profile.getLong(Account.AccountMetaData.CREATED));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
						try {
							u.setModified(profile.getLong(Account.AccountMetaData.MODIFIED));
						} catch (JSONException e) {
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}

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

	// public Account getAccountFromLocalDb() {
	// Account u = null;
	//
	// Cursor result = Data
	// .getmContext()
	// .getContentResolver()
	// .query(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI,
	// null, null, null, null);
	//
	// if (result.moveToFirst()) {
	// u = new Account();
	//
	// u.user_id = result
	// .getInt(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.A_ID));
	//
	// u.name = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.NAME));
	// u.fullname = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.FULLNAME));
	//
	// u.birthdate = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.BIRTHDATE));
	// u.sex = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SEX));
	//
	// u.email = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL));
	// u.email2 = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL2));
	// u.email3 = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL3));
	// u.email4 = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.EMAIL4));
	// u.phone1 = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.PHONE1));
	// u.phone2 = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.PHONE2));
	// u.phone3 = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.PHONE3));
	//
	// final int image = result
	// .getInt(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE));
	// u.image = image == 1;
	// u.image_url = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE_URL));
	// u.image_thumb_url = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE_THUMB_URL));
	// u.image_bytes = result
	// .getBlob(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.IMAGE_BYTES));
	// u.remove_image = result
	// .getInt(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.REMOVE_IMAGE));
	//
	// u.country = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COUNTRY));
	// u.city = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.CITY));
	// u.street = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.STREET));
	// u.zip = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.ZIP));
	//
	// u.timezone = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.TIMEZONE));
	// u.local_time = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.LOCAL_TIME));
	// u.language = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.LANGUAGE));
	//
	// u.setting_default_view = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW));
	// u.setting_date_format = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT));
	// u.setting_ampm = result
	// .getInt(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM));
	//
	// u.google_calendar_link = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK));
	//
	// u.color_my_event = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_MY_EVENT));
	// u.color_attending = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_ATTENDING));
	// u.color_pending = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_PENDING));
	// u.color_invitation = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_INVINTATION));
	// u.color_notes = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_NOTES));
	// u.color_birthday = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.COLOR_BIRTHDAY));
	//
	// u.created = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.CREATED));
	// u.modified = result
	// .getString(result
	// .getColumnIndex(AccountProvider.AMetaData.AccountMetaData.MODIFIED));
	// }
	// result.close();
	// Data.setAccount(u);
	// return u;
	// }

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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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
							token = object.getString(TOKEN);
							JSONObject profile = object.getJSONObject("profile");
							int id = Integer.parseInt(profile.getString("user_id"));
							Data.setToken(token);
							Data.setUserId(id);

							// Last login set
							hc = new DefaultHttpClient();
							post = new HttpPost(Data.getServerUrl() + "mobile/set_lastlogin");

							reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

							reqEntity.addPart(TOKEN, new StringBody(token));

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
			SharedPreferences prefs = Data.getmContext().getSharedPreferences("LATEST_CREDENTIALS", 0);
			if ((prefs.getString("email", "").equals(email)) && (prefs.getString("password", "").equals(password))) {
				success = true;
				Data.needToClearData = false;
			}
		}
		return success;
	}

	public void registerPhone() {
		try {
			getImei(Data.getmContext());
			GCMRegistrar.checkDevice(this.getContext());
			GCMRegistrar.checkManifest(this.getContext());
			Data.setPushId(GCMRegistrar.getRegistrationId(this.getContext()));
			if (Data.getPushId().equals("")) {

				C2DMessaging.register(Data.getmContext(), PROJECT_ID);
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
			reqEntity.addPart("android_id", new StringBody(pushId));

			post.setEntity(reqEntity);

			@SuppressWarnings("unused")
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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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
			item.setColor(result.getString(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COLOR)));
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
		Account account = new Account();

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_list");
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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
										birthdateCalendar = Utils.stringToCalendar(contact.birthdate,
												DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
										birthdateCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
										Event birthdayEvent = new Event();
										birthdayEvent.title = contact.name + " " + contact.lastname + " "
												+ Data.getmContext().getString(R.string.contact_birthday);
										birthdayEvent.setStartCalendar(birthdateCalendar);
										birthdayEvent.setEndCalendar(birthdateCalendar);
										// birthdayEvent.my_time_start = Utils
										// .formatCalendar(
										// birthdateCalendar,
										// SERVER_TIMESTAMP_FORMAT);
										// birthdayEvent.my_time_end = Utils
										// .formatCalendar(
										// birthdateCalendar,
										// SERVER_TIMESTAMP_FORMAT);
										birthdayEvent.is_all_day = true;
										birthdayEvent.timezone = account.getTimezone();
										birthdayEvent.birthday = true;
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
									contact.setColor(c.getString(ContactsProvider.CMetaData.ContactsMetaData.COLOR));
									cv.put(ContactsProvider.CMetaData.ContactsMetaData.COLOR, contact.getColor());
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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

			reqEntity.addPart("name", new StringBody(c.name));
			if (c.lastname != null) {
				reqEntity.addPart("lastname", new StringBody(c.lastname));
			} else {
				reqEntity.addPart("lastname", new StringBody(""));
			}
			if (c.email != null) {
				reqEntity.addPart("email", new StringBody(c.email));
			} else {
				reqEntity.addPart("email", new StringBody(""));
			}
			if (c.phone1 != null) {
				reqEntity.addPart("phone1", new StringBody(c.phone1));
			} else {
				reqEntity.addPart("phone1", new StringBody(""));
			}
			if (c.birthdate != null) {
				reqEntity.addPart("birthdate", new StringBody(c.birthdate));
			} else {
				reqEntity.addPart("birthdate", new StringBody(""));
			}
			if (c.country != null) {
				reqEntity.addPart("country", new StringBody(c.country));
			} else {
				reqEntity.addPart("country", new StringBody(""));
			}
			if (c.city != null) {
				reqEntity.addPart("city", new StringBody(c.city));
			} else {
				reqEntity.addPart("city", new StringBody(""));
			}
			if (c.street != null) {
				reqEntity.addPart("street", new StringBody(c.street));
			} else {
				reqEntity.addPart("street", new StringBody(""));
			}
			if (c.zip != null) {
				reqEntity.addPart("zip", new StringBody(c.zip));
			} else {
				reqEntity.addPart("zip", new StringBody(""));
			}
			if (c.visibility != null) {
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

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
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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

	// TODO what is this method for???
	public int loadEvents(Activity instance, EventsAdapter eAdapter) {
		int eventsSize = 0;
		ArrayList<Event> events;
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);		
		events = getEventsFromLocalDb(today, null);			

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


	/**
	 * Gets events from remote Database and writes them to local DB.
	 * @author justinas.marcinka@gmail.com
	 * @param eventCategory API category. if empty, returns all events
	 * @return
	 */
	public ArrayList<Event> getEventsFromRemoteDb(String eventCategory) {
		boolean success = false;
		ArrayList<Event> events = new ArrayList<Event>();
		Event event = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + GET_EVENTS_FROM_REMOTE_DB_URL);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
			reqEntity.addPart(CATEGORY, new StringBody(eventCategory));
			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						// // error = object.getString("error");
					} else {
						JSONArray es = object.getJSONArray("events");
						int count = es.length();
						for (int i = 0; i < count; i++) {
							JSONObject e = es.getJSONObject(i);

							event = createEventFromJSON(e);
							if (event != null){
//								this.insertEventToLocalDB(event);
								events.add(event);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		Data.setLoadEventsData(false);
		if (contactsBirthdays != null && !contactsBirthdays.isEmpty()) {
			events.addAll(contactsBirthdays);
		}
		sortEvents(events);
		return getNaviveCalendarEvents(events);
	}

	/**
	 * Creates Event object from JSON object
	 * @param e JSON object
	 * @return new event object with all info set. WARNING: if not all required fields are set, returns null <br>
	 * Needed fields:
	 * event_id<br>
	 * user_id<br>
	 * status<br>
	 * is_owner<br>
	 * title<br>
	 * timezone<br>
	 * time_start_utc<br>
	 * time_end_utc<br>
	 */
	private Event createEventFromJSON(JSONObject e) {
		Event event = new Event();

		String timezone = CalendarSettings.getTimeZone(); //TODO kaip pasiimam dabar?
		//critical event info. If fetch fails, return null
		try {
			event.setEvent_id(e.getInt("event_id"));
			event.setTimezone(e.getString("timezone"));
			// EVENT TIME START
			long unixTimestamp = e.getLong("timestamp_start_utc");
			event.setStartCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), timezone));
			// EVENT TIME END
			unixTimestamp = e.getLong("timestamp_end_utc");
			event.setEndCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), timezone));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
//			return null;
		}
		
		try {
			event.setUser_id(e.getInt("user_id"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setStatus(e.getInt("status"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setIs_owner(e.getInt("is_owner") == 1);
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setType(e.getString("type"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setTitle(e.getString("title"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setIcon(e.getString("icon"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setColor(e.getString("color"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setDescription(e.getString("description"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setLocation(e.getString("location"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAccomodation(e.getString("accomodation"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setCost(e.getString("cost"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setTake_with_you(e.getString("take_with_you"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setGo_by(e.getString("go_by"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setCountry(e.getString("country"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setCity(e.getString("city"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setStreet(e.getString("street"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setZip(e.getString("zip"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		

		// reminders

		try {
			event.setReminder1(e.getString("reminder1"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setReminder2(e.getString("reminder2"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setReminder3(e.getString("reminder3"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		// alarms

		try {
			event.setAlarm1(e.getString("alarm1"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAlarm1fired(e.getString("alarm1_fired"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAlarm2(e.getString("alarm2"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAlarm2fired(e.getString("alarm2_fired"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAlarm3(e.getString("alarm3"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAlarm3fired(e.getString("alarm3_fired"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setCreated(e.getString("created"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setModified(e.getString("modified"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setAttendant_0_count(e.getInt("attendant_0_count"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAttendant_1_count(e.getInt("attendant_1_count"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		
		try {
			event.setAttendant_2_count(e.getInt("attendant_2_count"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		
		try {
			event.setAttendant_4_count(e.getInt("attendant_4_count"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setSports_event(e.getInt("is_sports_event") == 1);
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setIs_all_day(e.getInt("all_day") == 1);
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setCreator_fullname(e.getString("creator_fullname"));
		} catch (JSONException e1) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setCreator_contact_id(e.getInt("creator_contact_id"));
		} catch (JSONException e1) {
			event.setCreator_contact_id(0);
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}

		try {
			event.setAssigned_contacts_DB_entry(e.getString("assigned_contacts"));
		} catch (JSONException e1) {
			event.setAssigned_contacts_DB_entry("");
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setAssigned_groups_DB_entry(e.getString("assigned_groups"));
		} catch (JSONException e1) {
			event.setAssigned_contacts_DB_entry("");
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		try {
			event.setInvited_DB_entry(e.getString("invited"));
		} catch (JSONException e1) {
			event.setInvited_DB_entry("");
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e1.getMessage());
		}
		
		return event;
	}

	public void insertEventToLocalDB(Event event) {
		ContentValues cv = new ContentValues();

		// 1. ADD EVENT details to events table

		cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.getEvent_id());
		cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, event.getUser_id());
		cv.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, event.getNeedUpdate());
		cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.getStatus());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID, event.getCreator_contact_id());
		
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT, event.getAttendant_1_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT, event.getAttendant_2_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT, event.getAttendant_0_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT, event.getAttendant_4_count());
		
		
		//native events are not held in GA local db so we do not put Event.isNative
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT, event.is_sports_event()? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, event.is_owner() ? 1 : 0);
//		TODO cv.put(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY, event.is_all_day() ? 1 : 0);
//cv.put(EventsProvider.EMetaData.EventsMetaData.IS_BIRTHDAY, event.isBirthday()? 1 : 0);

		cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, event.getType());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME, event.getCreator_fullname());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, event.getTitle());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.getIcon());
		cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.getColor());
		cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, event.getDescription());
		cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION, event.getLocation());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION, event.getAccomodation());
		cv.put(EventsProvider.EMetaData.EventsMetaData.COST, event.getCost());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU, event.getTake_with_you());
		cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY, event.getGo_by());
		
		cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY, event.getCountry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CITY, event.getCity());
		cv.put(EventsProvider.EMetaData.EventsMetaData.STREET, event.getStreet());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP, event.getZip());
		
		// EVENT TIMES UTC
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE, event.getTimezone());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, event.getStartCalendar().getTimeInMillis());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS, event.getEndCalendar().getTimeInMillis());

		// reminders
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.getReminder1());
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.getReminder2());
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.getReminder3());
		
		// TODO alarms DO SOMETHING WITH ALARM FIRED FIELDS
		cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM1, event.getAlarm1());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM2, event.getAlarm2());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM3, event.getAlarm3());
		

		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED, event.getCreated());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED, event.getModified());

		cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS, event.getAssigned_contacts_DB_entry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS, event.getAssigned_groups_DB_entry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, event.getInvited_DB_entry());
		resolver.insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, cv);

		// 2. INSERT EVENT day indexes into events_days table

		Calendar eventDayStart = (Calendar) event.getStartCalendar().clone();
		eventDayStart.set(Calendar.HOUR_OF_DAY, 0);
		eventDayStart.set(Calendar.MINUTE, 0);
		eventDayStart.set(Calendar.SECOND, 0);
		eventDayStart.set(Calendar.MILLISECOND, 0);
		
		
		
		int event_id = event.getEvent_id();
		

		while (eventDayStart.before(event.getEndCalendar())){
			cv = new ContentValues();
			cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID,
					event_id);
			
			Date time = eventDayStart.getTime();
			
			cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY,
					day_index_formatter.format(time));
			
			cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH,
					month_index_formatter.format(time));
			resolver.insert(
					EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI,
					cv);
			eventDayStart.add(Calendar.DATE, 1);
		}
	}

	public void sortEvents(ArrayList<Event> events) {
		TreeMap<Calendar, ArrayList<Event>> tm = new TreeMap<Calendar, ArrayList<Event>>();
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		for (Event event : events) {
			if (event.getStartCalendar() != null && event.getEndCalendar() != null) {
				event_start = (Calendar) event.getStartCalendar().clone();
				event_end = (Calendar) event.getEndCalendar().clone();
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
					if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
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
		Calendar event_start = (Calendar) event.getStartCalendar().clone();
		String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
		Calendar event_day = Utils.stringToCalendar(dayStr + " 00:00:00", date_format);
		Data.setSortedEvents(putValueIntoTreeMap(Data.getSortedEvents(), event_day, event));
	}

	/**
	 * Selects all events in specified time range from local database
	 * @author justinas.marcinka@gmail.com
	 * @param dayFrom selection period start date
	 * @param dayTo selection period end date
	 * @return
	 */
	public ArrayList<Event> getEventsFromLocalDb(Calendar dayFrom, Calendar dayTo) {
	ArrayList<Event> result = new ArrayList<Event>();
	if (dayFrom == null) return result;
	
	Event item;
	Cursor result_cursor;	
	
	Calendar daytimeStart = (Calendar) dayFrom.clone();
	daytimeStart.set(Calendar.HOUR_OF_DAY, 0);
	daytimeStart.set(Calendar.MINUTE, 0);
	daytimeStart.set(Calendar.SECOND, 0);
	daytimeStart.set(Calendar.MILLISECOND, 0);
	
	String where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE
			+ " < 3"
			+ " AND "
			+ "(";		
	if (dayTo == null){
		where +=  EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS + ">" + daytimeStart.getTimeInMillis();	
	}else {
		Calendar daytimeEnd = (Calendar) dayTo.clone();
		daytimeEnd.set(Calendar.HOUR_OF_DAY, 0);
		daytimeEnd.set(Calendar.MINUTE, 0);
		daytimeEnd.set(Calendar.SECOND, 0);
		daytimeEnd.set(Calendar.MILLISECOND, 0);
		where += "(";
		where += EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS + " BETWEEN " + daytimeStart.getTimeInMillis() + " AND " + daytimeEnd.getTimeInMillis();
		where += ")";
		where += " OR ";
		where += "(";
		where += EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + " BETWEEN " + daytimeStart.getTimeInMillis() + " AND " + daytimeEnd.getTimeInMillis();
		where += ")";
	}	
	where += ")";

	result_cursor = Data
			.getmContext()
			.getContentResolver()
			.query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI,
					null, where, null, null);

	if (result_cursor != null) {
		result_cursor.moveToFirst();
		user_timezone = CalendarSettings.getTimeZone();
		while (!result_cursor.isAfterLast()) {
			item = createEventFromCursor(result_cursor);

			result.add(item);
			result_cursor.moveToNext();
		}
		result_cursor.close();
	}
	return result;
}
	
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param date - day of event
	 * @return array list of events that take part on given day
	 */
	public ArrayList<Event> getEventsFromLocalDb(Calendar day) {
		day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
		Event item;
		ArrayList<Event> array = new ArrayList<Event>();

		String dayColumn = day_index_formatter.format(day.getTime());
		

		// TODO find out what NEED_UPDATE field is for and why it should be < 3

		String where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE
				+ " < 3"
				+ " AND "
				+ "("
				+ EventsProvider.EMetaData.EventsIndexesMetaData.DAY + " = '" + dayColumn + "'"
				+ ")";



		Cursor result = Data
				.getmContext()
				.getContentResolver()
				.query(EventsProvider.EMetaData.EVENTS_ON_DATE_URI,
						null, where, null, null);

		if (result != null) {
			result.moveToFirst();
			user_timezone = CalendarSettings.getTimeZone();
			while (!result.isAfterLast()) {
				item = createEventFromCursor(result);
				array.add(item);
				result.moveToNext();
			}
			result.close();
		}
		return array;
	}
	
private Event createEventFromCursor(Cursor result) {
		Event item = new Event();
		item.setEvent_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID)));
		item.setUser_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.USER_ID)));
		item.setNeedUpdate(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE)));
		item.setStatus(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS)));
		item.setCreator_contact_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID)));
		
		item.setAttendant_0_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT)));
		item.setAttendant_2_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT)));
		item.setAttendant_0_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT)));
		item.setAttendant_4_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT)));
		
		item.setSports_event(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT)) == 1);
		final int is_owner = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_OWNER));
		item.setIs_owner(is_owner == 1);
//		TODO 
//		item.setIs_all_day(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY)) == 1);
//		item.setBirthday(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_BIRTHDAY)) == 1);
		item.setNative(false); //native events are not stored in local DB, so they cant be restored also
		
		item.setType(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TYPE)));
		item.setCreator_fullname(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME)));
		item.setTitle(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TITLE)));
		item.setIcon(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ICON)));
		item.setColor(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData .COLOR)));
		item.setDescription(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.DESC)));

		item.setLocation(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.LOCATION)));
		item.setAccomodation(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION)));
		item.setCost(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COST)));
		item.setTake_with_you(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU)));
		item.setGo_by(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.GO_BY)));

		item.setCountry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COUNTRY)));
		item.setCity(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CITY)));
		item.setStreet(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STREET)));
		item.setZip(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ZIP)));

		item.setTimezone(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIMEZONE)));
		
		user_timezone = CalendarSettings.getTimeZone();
		long timeinMillis = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
		item.setStartCalendar(Utils.createCalendar(timeinMillis, user_timezone));
		timeinMillis = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
		item.setEndCalendar(Utils.createCalendar(timeinMillis, user_timezone));
		
		item.setReminder1(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER1)));
		item.setReminder2(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER2)));
		item.setReminder3(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER3)));

		item.setCreated(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATED)));
		item.setModified(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MODIFIED)));

		item.setAssigned_contacts_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS)));
		item.setAssigned_groups_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS)));
		item.setInvited_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.INVITED)));

		return item;
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
				item = createEventFromCursor(result);			
				items.add(item);
				result.moveToNext();
			}
			result.close();
		}
		sortEvents(items);
//		return getNaviveCalendarEvents(items);
		return (items);
	}

	public Event getNativeCalendarEvent(int id) {
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

				item.setStartCalendar(Utils.createCalendar(cursor.getLong(3), item.getTimezone()));
				item.setEndCalendar(Utils.createCalendar(cursor.getLong(4), item.getTimezone()));
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

						item.setStartCalendar(Utils.createCalendar(cursor.getLong(3), item.getTimezone()));

						long endLong = cursor.getLong(4);

						if (endLong <= 0) {
							endLong = cursor.getLong(3);
							endLong += 1 * 60 * 60 * 1000;
						}
						item.setEndCalendar(Utils.createCalendar(endLong, item.getTimezone()));

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

	public Event getEventFromLocalDb(int event_id) {
		Event item = null;
		Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + event_id);
		Cursor result = Data.getmContext().getContentResolver().query(uri, null, null, null, null);
		if (result.moveToFirst()) {
		item = createEventFromCursor(result);
		//TODO
//			String assigned_contacts = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS));
//			if (assigned_contacts != null && !assigned_contacts.equals("null")) {
//				try {
//					item.assigned_contacts = Utils.jsonStringToArray(assigned_contacts);
//				} catch (JSONException e) {
//					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//							e.getMessage());
//					item.assigned_contacts = null;
//				}
//			}
//
//			String assigned_groups = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS));
//			if (assigned_groups != null && !assigned_groups.equals("null")) {
//				try {
//					item.assigned_groups = Utils.jsonStringToArray(assigned_groups);
//				} catch (JSONException e) {
//					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//							e.getMessage());
//					item.assigned_groups = null;
//				}
//			}
//
//			String invitedJson = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.INVITED));
//			if (invitedJson != null && !invitedJson.equals("null")) {
//				try {
//					JSONArray arr = new JSONArray(invitedJson);
//					if (arr.length() > 0) {
//						item.invited = new ArrayList<Invited>();
//					}
//					for (int i = 0, l = arr.length(); i < l; i++) {
//						JSONObject obj = arr.getJSONObject(i);
//
//						final Invited invited = new Invited();
//
//						try {
//							invited.status_id = obj.getInt("status");
//
//							if (invited.status_id == 4) {
//								invited.status = Data.getmContext().getString(R.string.status_2);
//							} else {
//								String statusStr = new StringBuilder("status_").append(invited.status_id).toString();
//								int statusId = Data.getmContext().getResources()
//										.getIdentifier(statusStr, "string", "com.groupagendas.groupagenda");
//
//								invited.status = Data.getmContext().getString(statusId);
//							}
//						} catch (JSONException ex) {
//							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
//									.toString(), ex.getMessage());
//						}
//
//						try {
//							if (!obj.getString("my_contact_id").equals("null")) {
//								invited.my_contact_id = obj.getInt("my_contact_id");
//								Contact contact = getContact(invited.my_contact_id);
//								invited.email = contact.email;
//								invited.name = contact.name + " " + contact.lastname;
//								invited.contactId = contact.contact_id;
//							} else if (Data.getAccount().fullname.equals(obj.getString("gname"))) {
//								invited.name = Data.getmContext().getString(R.string.you);
//								invited.email = Data.getEmail();
//								invited.me = true;
//							} else {
//								invited.name = obj.getString("gname");
//								String tmp = obj.getString("gcid");
//								if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
//									invited.gcid = Integer.parseInt(tmp);
//								} else {
//									invited.gcid = 0;
//								}
//								tmp = obj.getString("guid");
//								if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
//									invited.guid = Integer.parseInt(tmp);
//								} else {
//									invited.guid = 0;
//								}
//								tmp = obj.getString("my_contact_id");
//								if (tmp.equalsIgnoreCase("null")) {
//									invited.inMyList = false;
//								} else {
//									invited.inMyList = true;
//								}
//							}
//						} catch (JSONException ex) {
//							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
//									.toString(), ex.getMessage());
//						}
//
//						item.invited.add(invited);
//					}
//				} catch (JSONException e) {
//					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//							e.getMessage());
//				}
//			}
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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
							event.setCreator_contact_id(e.getInt("creator_contact_id"));
						} catch (JSONException ex) {
							event.setCreator_contact_id(0);
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(e.event_id)));

			reqEntity.addPart("event_type", new StringBody(e.type));

			reqEntity.addPart("icon", new StringBody(e.icon));
			reqEntity.addPart("color", new StringBody(e.getColor()));

			reqEntity.addPart("title", new StringBody(e.title));

			reqEntity.addPart("timestamp_start_utc",
					new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis())));
			reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis())));

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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

			if (e.icon != null)
				reqEntity.addPart("icon", new StringBody(e.icon));
			// if (e.color != null)
			reqEntity.addPart("color", new StringBody(e.getColor()));

			reqEntity.addPart("title", new StringBody(e.title));

			reqEntity.addPart("timestamp_start_utc",
					new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis())));
			reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis())));

			if (e.description_ != null) {
				reqEntity.addPart("description", new StringBody(e.description_));
			} else {
				reqEntity.addPart("description", new StringBody(""));
			}

			if (e.country != null && e.country.length() > 0)
				reqEntity.addPart("country", new StringBody(e.country));
			if (e.city != null && e.city.length() > 0)
				reqEntity.addPart("city", new StringBody(e.city));
			if (e.street != null && e.street.length() > 0)
				reqEntity.addPart("street", new StringBody(e.street));
			if (e.zip != null && e.zip.length() > 0)
				reqEntity.addPart("zip", new StringBody(e.zip));
			reqEntity.addPart("timezone", new StringBody(e.timezone));

			if (e.location != null && e.location.length() > 0)
				reqEntity.addPart("location", new StringBody(e.location));
			if (e.go_by != null && e.go_by.length() > 0)
				reqEntity.addPart("go_by", new StringBody(e.go_by));
			if (e.take_with_you != null && e.take_with_you.length() > 0)
				reqEntity.addPart("take_with_you", new StringBody(e.take_with_you));
			if (e.cost != null && e.cost.length() > 0)
				reqEntity.addPart("cost", new StringBody(e.cost));
			if (e.accomodation != null && e.accomodation.length() > 0)
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

			if (e.birthday) {
				reqEntity.addPart("bd", new StringBody("1"));
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

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
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
							Event event = createEventFromJSON(e);
							insertEventToLocalDB(event);

							// TODO JUSTAI V, KAS CIA??? KOL KAS KOMENTUOJU
							// kaip suprantu, updatina calendoriu, jei jis null.

							// Event tmpEvent = getEventFromDb(event_id);
							// if (tmpEvent.getStartCalendar() == null) {
							// tmpEvent.setStartCalendar(Utils
							// .stringToCalendar(event.my_time_start,
							// SERVER_TIMESTAMP_FORMAT));
							// }
							// if (tmpEvent.getEndCalendar() == null) {
							// tmpEvent.setEndCalendar(Utils.stringToCalendar(
							// event.my_time_end,
							// SERVER_TIMESTAMP_FORMAT));
							// }
							// updateEventInsideLocalDb(tmpEvent);
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

	public void getChatMessages(int event_id, String from) {
		if (event_id > 0) {
			Object[] executeArray = { event_id, from };
			new GetChatMessages().execute(executeArray);
		}
	}

	private class GetChatMessages extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int event_id = (Integer) params[0];
				String from = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
				if (from == null) {
					reqEntity.addPart("from_datetime", new StringBody(String.valueOf("")));
				} else {
					reqEntity.addPart("from_datetime", new StringBody(String.valueOf(from)));
				}

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if (networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (!success) {
								Log.e("Change account ERROR", object.getJSONObject("error").getString("reason"));
							} else {
								Data.getChatMessages().clear();
								JSONArray chatMessages = object.getJSONArray("items");
								for (int i = 0, l = chatMessages.length(); i < l; i++) {
									final JSONObject chatMessage = chatMessages.getJSONObject(i);
									ChatMessageObject message = new ChatMessageObject();
									message.messageId = chatMessage.getInt("message_id");
									message.eventId = chatMessage.getInt("event_id");
									message.dateTime = chatMessage.getString("datetime");
									message.dateTimeCalendar = Utils.stringToCalendar(message.dateTime, SERVER_TIMESTAMP_FORMAT);
									message.userId = chatMessage.getInt("user_id");
									message.message = chatMessage.getString("message");
									String deleted = chatMessage.getString("deleted");
									message.deleted = !deleted.equals("null");
									message.updated = chatMessage.getString("updated");
									message.updatedCalendar = Utils.stringToCalendar(message.updated, SERVER_TIMESTAMP_FORMAT);
									message.fullname = chatMessage.getString("fullname");
									message.contactId = chatMessage.getString("contact_id");
									message.dateTimeConverted = chatMessage.getString("datetime_conv");
									message.dateTimeConvertedCalendar = Utils.stringToCalendar(message.dateTimeConverted,
											SERVER_TIMESTAMP_FORMAT);
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

	public void postChatMessage(int event_id, String message) {
		if (event_id > 0) {
			Object[] executeArray = { event_id, message };
			new PostChatMessage().execute(executeArray);
		}
	}

	public class PostChatMessage extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int event_id = (Integer) params[0];
				String message = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_post");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
				if (message == null) {
					reqEntity.addPart("message", new StringBody(String.valueOf("")));
				} else {
					reqEntity.addPart("message", new StringBody(String.valueOf(message)));
				}

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if (networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (success) {
								getChatMessages(event_id, null);
								System.out.println("Meesage posted");
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_post", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}

	}

	public void removeChatMessage(int messageId, int event_id) {
		if (event_id > 0) {
			Object[] executeArray = { messageId, event_id };
			new RemoveChatMessage().execute(executeArray);
		}
	}

	public class RemoveChatMessage extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int message_id = (Integer) params[0];
				int event_id = (Integer) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_remove");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("message_id", new StringBody(String.valueOf(message_id)));

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if (networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (success) {
								getChatMessages(event_id, null);
								System.out.println("Message removed");
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_remove", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}

	}

	public void getChatThreads() {
		new GetChatThreads().execute();
	}

	public class GetChatThreads extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_threads");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if (networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (success) {
								JSONArray chatThreads = object.getJSONArray("items");
								for (int i = 0, l = chatThreads.length(); i < l; i++) {
									final JSONObject thread = chatThreads.getJSONObject(i);
									String tmp = "";
									ChatThreadObject chatThread = new ChatThreadObject();
									tmp = thread.getString("event_id");
									if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
										chatThread.event_id = Integer.parseInt(tmp);
									} else {
										chatThread.event_id = 0;
									}
									tmp = thread.getString("user_id");
									if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
										chatThread.user_id = Integer.parseInt(tmp);
									} else {
										chatThread.user_id = 0;
									}
									chatThread.type = thread.getString("type");
									chatThread.confirmed = thread.getString("confirmed");
									tmp = thread.getString("contact_author_id");
									if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
										chatThread.contact_author_id = Integer.parseInt(tmp);
									} else {
										chatThread.contact_author_id = 0;
									}
									chatThread.title = thread.getString("title");
									chatThread.icon = thread.getString("icon");
									chatThread.color = thread.getString("color");
									chatThread.description = thread.getString("description");
									chatThread.location = thread.getString("location");
									chatThread.accomodation = thread.getString("accomodation");
									chatThread.cost = thread.getString("cost");
									chatThread.take_with_you = thread.getString("take_with_you");
									chatThread.go_by = thread.getString("go_by");
									chatThread.country = thread.getString("country");
									chatThread.city = thread.getString("city");
									chatThread.street = thread.getString("street");
									chatThread.zip = thread.getString("zip");
									chatThread.timezone = thread.getString("timezone");
									chatThread.time_start = thread.getString("time_start");
									chatThread.time_startCalendar = Utils.stringToCalendar(chatThread.time_start, SERVER_TIMESTAMP_FORMAT);
									chatThread.time_end = thread.getString("time_end");
									chatThread.time_endCalendar = Utils.stringToCalendar(chatThread.time_end, SERVER_TIMESTAMP_FORMAT);
									chatThread.all_day_event = thread.getString("all_day_event").equalsIgnoreCase("1");
									chatThread.reminder1 = thread.getString("reminder1");
									chatThread.reminder1calendar = Utils.stringToCalendar(chatThread.reminder1, SERVER_TIMESTAMP_FORMAT);
									chatThread.reminder2 = thread.getString("reminder2");
									chatThread.reminder2calendar = Utils.stringToCalendar(chatThread.reminder2, SERVER_TIMESTAMP_FORMAT);
									chatThread.reminder3 = thread.getString("reminder3");
									chatThread.reminder3calendar = Utils.stringToCalendar(chatThread.reminder3, SERVER_TIMESTAMP_FORMAT);
									chatThread.google_uid = thread.getString("google_uid");
									chatThread.native_id = thread.getString("native_id");
									chatThread.wp = thread.getString("wp");
									chatThread.created = thread.getString("created");
									chatThread.createdCalendar = Utils.stringToCalendar(chatThread.created, SERVER_TIMESTAMP_FORMAT);
									chatThread.modified = thread.getString("modified");
									chatThread.from_feed = thread.getString("from_feed").equals("1");
									tmp = thread.getString("message_count");
									if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
										chatThread.message_count = Integer.parseInt(tmp);
									} else {
										chatThread.message_count = 0;
									}
									chatThread.message_last = thread.getString("message_last");
									chatThread.message_lastCalendar = Utils.stringToCalendar(chatThread.message_last,
											SERVER_TIMESTAMP_FORMAT);
									chatThread.attendant_1_count = thread.getInt("attendant_1_count");
									chatThread.attendant_2_count = thread.getInt("attendant_2_count");
									chatThread.attendant_0_count = thread.getInt("attendant_0_count");
									chatThread.attendant_4_count = thread.getInt("attendant_4_count");
									chatThread.total_invited = thread.getInt("total_invited");
									tmp = thread.getString("sport_team_id");
									if (!tmp.equalsIgnoreCase("null")) {
										chatThread.sport_team_id = Integer.parseInt(tmp);
									} else {
										chatThread.sport_team_id = 0;
									}
									chatThread.sport_event_type = thread.getString("sport_event_type");
									chatThread.sport_location = thread.getString("sport_location");
									chatThread.sport_field = thread.getString("sport_field");
									chatThread.sport_opponent = thread.getString("sport_opponent");
									chatThread.sport_referee = thread.getString("sport_referee");
									chatThread.sport_time_assembly = thread.getString("sport_time_assembly");
									chatThread.sport_time_arrival = thread.getString("sport_time_arrival");
									chatThread.sport_arrival_address = thread.getString("sport_arrival_address");
									chatThread.sport_start_return_trip = thread.getString("sport_start_return_trip");
									chatThread.sport_time_return = thread.getString("sport_time_return");
									chatThread.poll_voted_count = thread.getInt("poll_voted_count");
									chatThread.poll_pending_count = thread.getInt("poll_pending_count");
									chatThread.poll_rejected_count = thread.getInt("poll_rejected_count");
									chatThread.poll_invited_count = thread.getInt("poll_invited_count");
									tmp = thread.getString("org_id");
									if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
										chatThread.org_id = Integer.parseInt(tmp);
									} else {
										chatThread.org_id = 0;
									}
									chatThread.repeat_group = thread.getString("repeat_group");
									chatThread.repeat_data = thread.getString("repeat_data");
									chatThread.alarm1 = thread.getString("alarm1");
									chatThread.alarm1calendar = Utils.stringToCalendar(chatThread.alarm1, SERVER_TIMESTAMP_FORMAT);
									chatThread.alarm2 = thread.getString("alarm2");
									chatThread.alarm2calendar = Utils.stringToCalendar(chatThread.alarm2, SERVER_TIMESTAMP_FORMAT);
									chatThread.alarm3 = thread.getString("alarm3");
									chatThread.alarm3calendar = Utils.stringToCalendar(chatThread.alarm3, SERVER_TIMESTAMP_FORMAT);
									chatThread.alarm1_fired = thread.getString("alarm1_fired").equalsIgnoreCase("1");
									chatThread.alarm2_fired = thread.getString("alarm2_fired").equalsIgnoreCase("1");
									chatThread.alarm3_fired = thread.getString("alarm3_fired").equalsIgnoreCase("1");
									chatThread.my_time_start = thread.getString("my_time_start");
									chatThread.my_time_startCalendar = Utils.stringToCalendar(chatThread.my_time_start,
											SERVER_TIMESTAMP_FORMAT);
									chatThread.my_time_end = thread.getString("my_time_end");
									chatThread.my_time_endCalendar = Utils
											.stringToCalendar(chatThread.my_time_end, SERVER_TIMESTAMP_FORMAT);
									chatThread.created_local = thread.getString("created_local");
									chatThread.created_localCalendar = Utils.stringToCalendar(chatThread.created_local,
											SERVER_TIMESTAMP_FORMAT);
									chatThread.modified_local = thread.getString("modified_local");
									chatThread.modified_localCalendar = Utils.stringToCalendar(chatThread.modified_local,
											SERVER_TIMESTAMP_FORMAT);
									chatThread.status = thread.getInt("status");
									chatThread.is_owner = thread.getInt("is_owner") == 1;
									chatThread.new_messages = thread.getInt("new_messages");
									chatThread.creator_fullname = thread.getString("creator_fullname");
									tmp = thread.getString("creator_contact_id");
									if (!tmp.equalsIgnoreCase("null")) {
										chatThread.creator_contact_id = Integer.parseInt(tmp);
									} else {
										chatThread.creator_contact_id = 0;
									}
									Data.getChatThreads().add(chatThread);
								}
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_threads", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
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

	// TODO write a javadoc && MAYBE AN INTERFACE?
	public void updateContactsAdapter(ArrayList<Contact> contacts, ContactsAdapter cAdapter) {
		if (cAdapter != null) {
			cAdapter.setItems(contacts);
			cAdapter.notifyDataSetChanged();
		}
	}

	// TODO write a javadoc && MAYBE AN INTERFACE?
	public void updateGroupsAdapter(ArrayList<Group> groups, GroupsAdapter gAdapter) {
		if (gAdapter != null) {
			gAdapter.setItems(groups);
			gAdapter.notifyDataSetChanged();
		}
	}

	// TODO write a javadoc && MAYBE AN INTERFACE?
	public void updateEventsAdapter(ArrayList<Event> events, EventsAdapter eAdapter) {
		if (eAdapter != null) {
			eAdapter.setItems(events);
			eAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Get Context object.
	 * 
	 * @author interfectos@gmail.com
	 * @return Existing Context object from Data (bin).
	 */
	public Context getContext() {
		return Data.getmContext();
	}

	/**
	 * Upload Event object to remote database as a template.
	 * 
	 * Method creates a multipart entity object, fills it with submitted event's
	 * data. Afterwards creates a connection to remote server and, if successful
	 * � uploads data. If not � stores it in an UnuploadedData object ArrayList.
	 * 
	 * Note: still missing event field upload features.
	 * 
	 * @author meska.lt@gmail.com
	 * @param event
	 *            Event type object with validated data.
	 * @version 1.1
	 * @since 2012-09-24
	 * @return Uploaded event's ID in remote database.
	 */
	public int uploadTemplateToRemoteDb(Event event) {
		int response = 0;
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/templates_set");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			if (event.getEvent_id() > 0)
				reqEntity.addPart("template_id", new StringBody(((Integer) event.getEvent_id()).toString()));
			else
				reqEntity.addPart("template_id", new StringBody(""));

			if (event.getIcon() != null)
				reqEntity.addPart("icon", new StringBody(event.getIcon()));

			if (event.getColor() != null)
				reqEntity.addPart("color", new StringBody(event.getColor()));

			if (event.getActualTitle() != null) {
				reqEntity.addPart("title", new StringBody(event.getActualTitle()));
				reqEntity.addPart("template_title", new StringBody(event.getActualTitle()));
			} else {
				reqEntity.addPart("title", new StringBody(""));
				reqEntity.addPart("template_title", new StringBody(""));
			}

			if (event.getDescription() != null) {
				reqEntity.addPart("description", new StringBody(event.getDescription()));
			} else {
				reqEntity.addPart("description", new StringBody(""));
			}

			if (event.getCountry() != null)
				reqEntity.addPart("country", new StringBody(event.getCountry()));

			if (event.getCity() != null)
				reqEntity.addPart("city", new StringBody(event.getCity()));

			if (event.getStreet() != null)
				reqEntity.addPart("street", new StringBody(event.getStreet()));

			if (event.getZip() != null)
				reqEntity.addPart("zip", new StringBody(event.getZip()));

			if (event.getTimezone() != null)
				reqEntity.addPart("timezone", new StringBody(event.getTimezone()));

			long timeInMillis = event.getStartCalendar().getTimeInMillis();
			if (timeInMillis > 0)
				reqEntity.addPart("timestamp_start_utc", new StringBody("" + Utils.millisToUnixTimestamp(timeInMillis)));

			timeInMillis = event.getEndCalendar().getTimeInMillis();
			if (timeInMillis > 0)
				reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(timeInMillis)));

			// timeInMillis = event.getReminder1().getTimeInMillis();
			// if (timeInMillis > 0)
			// reqEntity.addPart("reminder_1_utc", new StringBody("" +
			// Utils.millisToUnixTimestamp(timeInMillis)));

			// timeInMillis = event.getReminder2().getTimeInMillis();
			// if (timeInMillis > 0)
			// reqEntity.addPart("reminder_2_utc", new StringBody("" +
			// Utils.millisToUnixTimestamp(timeInMillis)));

			// timeInMillis = event.getReminder3().getTimeInMillis();
			// if (timeInMillis > 0)
			// reqEntity.addPart("reminder_3_utc", new StringBody("" +
			// Utils.millisToUnixTimestamp(timeInMillis)));

			// timeInMillis = event.getAlarm1().getTimeInMillis();
			// if (timeInMillis > 0)
			// reqEntity.addPart("alarm_1_utc", new StringBody("" +
			// Utils.millisToUnixTimestamp(timeInMillis)));

			// timeInMillis = event.getAlarm2().getTimeInMillis();
			// if (timeInMillis > 0)
			// reqEntity.addPart("alarm_2_utc", new StringBody("" +
			// Utils.millisToUnixTimestamp(timeInMillis)));

			// timeInMillis = event.getAlarm3().getTimeInMillis();
			// if (timeInMillis > 0)
			// reqEntity.addPart("alarm_3_utc", new StringBody("" +
			// Utils.millisToUnixTimestamp(timeInMillis)));

			if (event.getLocation() != null)
				reqEntity.addPart("location", new StringBody(event.getLocation()));

			if (event.go_by != null)
				reqEntity.addPart("go_by", new StringBody(event.go_by));

			if (event.take_with_you != null)
				reqEntity.addPart("take_with_you", new StringBody(event.take_with_you));

			if (event.cost != null)
				reqEntity.addPart("cost", new StringBody(event.cost));

			if (event.accomodation != null)
				reqEntity.addPart("accomodation", new StringBody(event.accomodation));

			if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
				event.assigned_contacts = new int[Data.selectedContacts.size()];
				int i = 0;
				for (Contact contact : Data.selectedContacts) {
					event.assigned_contacts[i] = contact.contact_id;
					i++;
				}
			}

			if (event.assigned_contacts != null) {
				for (int i = 0, l = event.assigned_contacts.length; i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(String.valueOf(event.assigned_contacts[i])));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody(""));
			}

			if (event.assigned_groups != null) {
				for (int i = 0, l = event.assigned_groups.length; i < l; i++) {
					reqEntity.addPart("groups[]", new StringBody(String.valueOf(event.assigned_groups[i])));
				}
			} else {
				reqEntity.addPart("groups[]", new StringBody(""));
			}

			if (event.reminder1 != null) {
				reqEntity.addPart("reminder1", new StringBody(event.reminder1));
			}

			if (event.reminder2 != null) {
				reqEntity.addPart("reminder2", new StringBody(event.reminder2));
			}

			if (event.reminder3 != null) {
				reqEntity.addPart("reminder3", new StringBody(event.reminder3));
			}

			// TODO find out wtf is bd in event
			// if (event.birthday) {
			// reqEntity.addPart("bd", new StringBody("1"));
			// }

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = hc.execute(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						boolean success = object.getBoolean("success");

						if (success == false) {
							Log.e("Create event error", object.getJSONObject("error").getString("reason"));
						}
						response = object.getInt("template_id");
						event.event_id = response;
					}
				} else {
					Log.e("setTemplate - status", rp.getStatusLine().getStatusCode() + "");
				}
			} else {
				OfflineData uplooad = new OfflineData("mobile/templates_set", reqEntity);
				Data.getUnuploadedData().add(uplooad);
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		return response;
	}

	/**
	 * Upload Event object to local database as a template.
	 * 
	 * Method creates a ContentValues object, fills it with submitted event's
	 * data. Afterwards gets a local daatabase content resolver and, if
	 * successful � uploads data into it.
	 * 
	 * Note: still missing event field upload features.
	 * 
	 * @author meska.lt@gmail.com
	 * @param template
	 *            Event type object with validated data.
	 * @param template_id
	 *            Integer corresponding to template's ID in remote database.
	 * @version 1.0
	 * @since 2012-09-24
	 * @return True if successful.
	 */
	public boolean uploadTemplateToLocalDb(Event template, int template_id) {
		boolean success = false;

		try {
			ContentValues cv = new ContentValues();

			if (template_id != 0)
				cv.put(TemplatesMetaData.T_ID, template_id);
			else if (template.event_id > 0)
				cv.put(TemplatesMetaData.T_ID, template.getEvent_id());
			else
				cv.put(TemplatesMetaData.T_ID, 0);

			if (template.getIcon() != null)
				cv.put(TemplatesMetaData.ICON, template.getIcon());
			else
				cv.put(TemplatesMetaData.ICON, "");

			if (template.getColor() != null)
				cv.put(TemplatesMetaData.COLOR, template.getColor());

			if (template.getActualTitle() != null)
				cv.put(TemplatesMetaData.TITLE, template.getActualTitle());
			else
				cv.put(TemplatesMetaData.TITLE, "Untitled");

			long timeInMillis = template.getStartCalendar().getTimeInMillis();
			if (timeInMillis > 0)
				cv.put(TemplatesMetaData.TIME_START, Utils.millisToUnixTimestamp(timeInMillis));

			timeInMillis = template.getEndCalendar().getTimeInMillis();
			if (timeInMillis > 0)
				cv.put(TemplatesMetaData.TIME_START, Utils.millisToUnixTimestamp(timeInMillis));

			if (template.getDescription() != null)
				cv.put(TemplatesMetaData.DESC, template.getDescription());
			else
				cv.put(TemplatesMetaData.DESC, "");

			if (template.getCountry() != null)
				cv.put(TemplatesMetaData.COUNTRY, template.getCountry());
			else
				cv.put(TemplatesMetaData.COUNTRY, "");

			if (template.getCity() != null)
				cv.put(TemplatesMetaData.CITY, template.getCity());
			else
				cv.put(TemplatesMetaData.CITY, "");

			if (template.getStreet() != null)
				cv.put(TemplatesMetaData.STREET, template.getStreet());
			else
				cv.put(TemplatesMetaData.STREET, "");

			if (template.getZip() != null)
				cv.put(TemplatesMetaData.ZIP, template.getZip());
			else
				cv.put(TemplatesMetaData.ZIP, "");

			if (template.getTimezone() != null)
				cv.put(TemplatesMetaData.TIMEZONE, template.getTimezone());
			else
				cv.put(TemplatesMetaData.TIMEZONE, "");

			if (template.getLocation() != null)
				cv.put(TemplatesMetaData.LOCATION, template.getLocation());
			else
				cv.put(TemplatesMetaData.LOCATION, "");

			if (template.getGo_by() != null)
				cv.put(TemplatesMetaData.GO_BY, template.getGo_by());
			else
				cv.put(TemplatesMetaData.GO_BY, "");

			if (template.getTake_with_you() != null)
				cv.put(TemplatesMetaData.TAKE_WITH_YOU, template.getTake_with_you());
			else
				cv.put(TemplatesMetaData.TAKE_WITH_YOU, "");

			if (template.getCost() != null)
				cv.put(TemplatesMetaData.COST, template.getCost());
			else
				cv.put(TemplatesMetaData.COST, "");

			if (template.getAccomodation() != null)
				cv.put(TemplatesMetaData.ACCOMODATION, template.getAccomodation());
			else
				cv.put(TemplatesMetaData.ACCOMODATION, "");

			if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
				template.assigned_contacts = new int[Data.selectedContacts.size()];
				int i = 0;
				for (Contact contact : Data.selectedContacts) {
					template.getAssigned_contacts()[i] = contact.contact_id;
					i++;
				}
			}

			if (template.getAssigned_contacts() != null) {
				for (int i = 0, l = template.getAssigned_contacts().length; i < l; i++) {
					cv.put(TemplatesMetaData.ASSIGNED_CONTACTS, String.valueOf(template.getAssigned_contacts()[i]));
				}
			} else {
				cv.put(TemplatesMetaData.ASSIGNED_CONTACTS, "");
			}

			if (template.getAssigned_groups() != null) {
				for (int i = 0, l = template.getAssigned_groups().length; i < l; i++) {
					cv.put(TemplatesMetaData.ASSIGNED_GROUPS, String.valueOf(template.getAssigned_groups()[i]));
				}
			} else {
				cv.put(TemplatesMetaData.ASSIGNED_GROUPS, "");
			}

			// if (template.reminder1 != null)
			// cv.put(TemplatesMetaData.REMINDER1, template.reminder1);
			// else
			// cv.put(TemplatesMetaData.REMINDER1, "");

			// if (template.reminder2 != null)
			// cv.put(TemplatesMetaData.REMINDER2, template.reminder2);
			// else
			// cv.put(TemplatesMetaData.REMINDER2, "");

			// if (template.reminder3 != null)
			// cv.put(TemplatesMetaData.REMINDER3, template.reminder3);
			// else
			// cv.put(TemplatesMetaData.REMINDER3, "");

			// if (template.getAlarm1() != null)
			// cv.put(TemplatesMetaData.ALARM1, template.getAlarm1());
			// else
			// cv.put(TemplatesMetaData.ALARM1, "");

			// if (template.getAlarm2() != null)
			// cv.put(TemplatesMetaData.ALARM2, template.getAlarm2());
			// else
			// cv.put(TemplatesMetaData.ALARM2, "");

			// if (template.getAlarm3() != null)
			// cv.put(TemplatesMetaData.ALARM3, template.getAlarm3());
			// else
			// cv.put(TemplatesMetaData.ALARM3, "");

			// TODO find out wtf is bd in event
			// if (event.birthday) {
			// reqEntity.addPart("bd", new StringBody("1"));
			// }

			Data.getmContext().getContentResolver().insert(TemplatesMetaData.CONTENT_URI, cv);
			success = true;
		} catch (Exception e) {
			Log.e("TemplateUploadLocal", "CATCH!");
		}

		return success;
	}

	// TODO getTemplatesFromRemoteDb() documentation pending.
	public ArrayList<Event> getTemplatesFromRemoteDb() {
		boolean success = false;
		ArrayList<Event> templates = new ArrayList<Event>();
		Event template = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/templates_get");

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
						// TODO show an error.
					} else {
						JSONArray es = object.getJSONArray("templates");
						int count = es.length();
						for (int i = 0; i < count; i++) {
							JSONObject e = es.getJSONObject(i);

							template = new Event();

							try {
								template.event_id = e.getInt("template_id");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID,
								// template.event_id);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							// try {
							// template.user_id = e.getInt("user_id");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID,
							// template.user_id);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							// ex.getMessage());
							// }
							// try {
							// template.status = e.getInt("status");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS,
							// template.status);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							// ex.getMessage());
							// }
							// try {
							// int is_owner = e.getInt("is_owner");
							// template.is_owner = is_owner == 1;
							// cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER,
							// template.is_owner);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							// ex.getMessage());
							// }
							// try {
							// template.type = e.getString("type");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE,
							// template.type);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							// ex.getMessage());
							// }
							try {
								template.title = e.getString("title");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE,
								// template.title);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.setIcon(e.getString("icon"));
								// cv.put(EventsProvider.EMetaData.EventsMetaData.ICON,
								// template.icon);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.setColor(e.getString("color"));
								// cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR,
								// template.getColor());
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.setDescription(e.getString("description"));
								// cv.put(EventsProvider.EMetaData.EventsMetaData.DESC,
								// template.description_);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.location = e.getString("location");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION,
								// template.location);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.accomodation = e.getString("accomodation");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION,
								// template.accomodation);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.cost = e.getString("cost");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.COST,
								// template.cost);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.take_with_you = e.getString("take_with_you");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU,
								// template.take_with_you);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.go_by = e.getString("go_by");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY,
								// template.go_by);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							/* Address START */
							try {
								template.country = e.getString("country");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY,
								// template.country);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.city = e.getString("city");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.CITY,
								// template.city);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.street = e.getString("street");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.STREET,
								// template.street);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.zip = e.getString("zip");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP,
								// template.zip);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							try {
								template.setTimezone(e.getString("timezone"));
								// cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE,
								// template.timezone);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							/* Address END */

							try {
								template.setStartCalendar(Utils.createCalendar(e.getLong("time_start"), template.getTimezone()));
								// cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START,
								// template.time_start);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							try {
								template.setEndCalendar(Utils.createCalendar(e.getLong("time_end"), template.getTimezone()));
								// cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END,
								// template.time_end);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}

							// try {
							// template.time = e.getString("time");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.TIME,
							// template.time);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							// ex.getMessage());
							// }
							// try {
							// template.my_time_start =
							// e.getString("my_time_start");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START,
							// template.my_time_start);
							// template.setStartCalendar(Utils.stringToCalendar(template.my_time_start,
							// SERVER_TIMESTAMP_FORMAT));
							//
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.my_time_end =
							// e.getString("my_time_end");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END,
							// template.my_time_end);
							// template.setEndCalendar(Utils.stringToCalendar(template.my_time_end,
							// SERVER_TIMESTAMP_FORMAT));
							//
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							/* Reminders START */
							// try {
							// template.reminder1 = e.getString("reminder1");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1,
							// template.reminder1);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.reminder2 = e.getString("reminder2");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2,
							// template.reminder2);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.reminder3 = e.getString("reminder3");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3,
							// template.reminder3);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							/* Reminders START */
							/* Alarms START */
							// String tmpAlarmFired = "";
							// try {
							// template.alarm1 = e.getString("alarm1");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1,
							// template.reminder1);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// tmpAlarmFired = e.getString("alarm1_fired");
							// if(!tmpAlarmFired.equals("null") &&
							// tmpAlarmFired.matches("[0-9]*")){
							// template.alarm1fired =
							// Integer.parseInt(tmpAlarmFired) == 1;
							// }
							// try {
							// template.alarm2 = e.getString("alarm2");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2,
							// template.reminder2);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// tmpAlarmFired = e.getString("alarm2_fired");
							// if(!tmpAlarmFired.equals("null") &&
							// tmpAlarmFired.matches("[0-9]*")){
							// template.alarm2fired =
							// Integer.parseInt(tmpAlarmFired) == 1;
							// }
							// try {
							// template.alarm3 = e.getString("alarm3");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3,
							// template.reminder3);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// tmpAlarmFired = e.getString("alarm3_fired");
							// if(!tmpAlarmFired.equals("null") &&
							// tmpAlarmFired.matches("[0-9]*")){
							// template.alarm3fired =
							// Integer.parseInt(tmpAlarmFired) == 1;
							// }
							/* Alarms END */

							// try {
							// template.created = e.getString("created");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED,
							// template.created);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.modified = e.getString("modified");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED,
							// template.modified);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							//
							// try {
							// template.attendant_1_count =
							// e.getInt("attendant_1_count");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT,
							// template.attendant_1_count);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.attendant_2_count =
							// e.getInt("attendant_2_count");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT,
							// template.attendant_2_count);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.attendant_0_count =
							// e.getInt("attendant_0_count");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT,
							// template.attendant_0_count);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.attendant_4_count =
							// e.getInt("attendant_4_count");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT,
							// template.attendant_4_count);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							//
							// try {
							// int is_sports_event =
							// e.getInt("is_sports_event");
							// template.is_sports_event = is_sports_event == 1;
							// cv.put(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT,
							// template.is_sports_event);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.creator_fullname =
							// e.getString("creator_fullname");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME,
							// template.creator_fullname);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// template.creator_contact_id =
							// e.getInt("creator_contact_id");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID,
							// template.creator_contact_id);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// TODO contacts[] ant Templates
							try {
								@SuppressWarnings("unused")
								JSONArray contacts = e.getJSONArray("groups");
								// cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS,
								// assigned_contacts);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							// TODO groups[] ant Templates
							try {
								@SuppressWarnings("unused")
								JSONArray groups = e.getJSONArray("groups");

								// cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS,
								// assigned_groups);
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							// try {
							// String invited = e.getString("invited");
							// cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED,
							// invited);
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName()
							// .toString(), ex.getMessage());
							// }
							// try {
							// int all_day = e.getInt("all_day_event");
							// template.is_all_day = all_day == 1;
							// } catch (JSONException ex) {
							// Reporter.reportError(this.getClass().toString(),
							// Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							// ex.getMessage());
							// }

							// Data.getmContext().getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI,
							// cv);
							templates.add(template);
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		// if (contactsBirthdays != null && !contactsBirthdays.isEmpty()) {
		// templates.addAll(contactsBirthdays);
		// }
		// sortEvents(events);
		return templates;
	}

	public Event getTemplateFromLocalDb(int template_id) {
		Event event = new Event();
		Uri uri = Uri.parse(TemplatesMetaData.CONTENT_URI.toString() + "/" + template_id);
		Cursor result = Data.getmContext().getContentResolver().query(uri, null, null, null, null);

		if (result.moveToFirst()) {
			event.setEvent_id(result.getInt(result.getColumnIndex(TemplatesMetaData.T_ID)));
			event.setColor(result.getString(result.getColumnIndex(TemplatesMetaData.COLOR)));
			event.setIcon(result.getString(result.getColumnIndex(TemplatesMetaData.ICON)));

			event.setTitle(result.getString(result.getColumnIndex(TemplatesMetaData.TITLE)));
			event.setStartCalendar(Utils.createCalendar(result.getLong(result.getColumnIndex(TemplatesMetaData.TIME_START)),
					result.getString(result.getColumnIndex(TemplatesMetaData.TIMEZONE))));
			event.setEndCalendar(Utils.createCalendar(result.getLong(result.getColumnIndex(TemplatesMetaData.TIME_END)),
					result.getString(result.getColumnIndex(TemplatesMetaData.TIMEZONE))));
			event.setDescription(result.getString(result.getColumnIndex(TemplatesMetaData.DESC)));

			event.setCountry(result.getString(result.getColumnIndex(TemplatesMetaData.COUNTRY)));
			event.setCity(result.getString(result.getColumnIndex(TemplatesMetaData.CITY)));
			event.setStreet(result.getString(result.getColumnIndex(TemplatesMetaData.STREET)));
			event.setZip(result.getString(result.getColumnIndex(TemplatesMetaData.ZIP)));
			event.setTimezone(result.getString(result.getColumnIndex(TemplatesMetaData.TIMEZONE)));

			event.setLocation(result.getString(result.getColumnIndex(TemplatesMetaData.LOCATION)));
			event.setGo_by(result.getString(result.getColumnIndex(TemplatesMetaData.GO_BY)));
			event.setTake_with_you(result.getString(result.getColumnIndex(TemplatesMetaData.TAKE_WITH_YOU)));
			event.setCost(result.getString(result.getColumnIndex(TemplatesMetaData.COST)));
			event.setAccomodation(result.getString(result.getColumnIndex(TemplatesMetaData.ACCOMODATION)));

			// event.setReminder1(result.getInt(result.getColumnIndex(TemplatesMetaData.REMINDER1)));
			// event.setReminder2(result.getInt(result.getColumnIndex(TemplatesMetaData.REMINDER2)));
			// event.setReminder3(result.getInt(result.getColumnIndex(TemplatesMetaData.REMINDER3)));

			// event.setAlarm1(result.getInt(result.getColumnIndex(TemplatesMetaData.ALARM1)));
			// event.setAlarm2(result.getInt(result.getColumnIndex(TemplatesMetaData.ALARM2)));
			// event.setAlarm3(result.getInt(result.getColumnIndex(TemplatesMetaData.ALARM3)));
		}

		return event;
	}

	/**
	 * Get all templates
	 * 
	 * Get all templates from remote database and store them in local database
	 * and temporary memory.
	 * 
	 * @author meska.lt@gmail.com
	 * @version 1.0
	 * @since 2012-09-24
	 * @return ArrayList of Event objects retrieved from remote database.
	 */
	public ArrayList<Event> getTemplates() {
		ArrayList<Event> templates = getTemplatesFromRemoteDb();

		for (Event template : templates) {
			uploadTemplateToLocalDb(template, 0);
		}

		return templates;
	}

	// Addresses
	public Address getAddressFromLocalDb(int addressId) {
		Address address = new Address();
		Uri uri = Uri.parse(AddressesMetaData.CONTENT_URI.toString() + "/" + addressId);
		Cursor result = Data.getmContext().getContentResolver().query(uri, null, null, null, null);
		if (result.moveToFirst()) {
			address.setId(result.getInt(result.getColumnIndex(AddressesMetaData.A_ID)));
			address.setUser_id(result.getInt(result.getColumnIndex(AddressesMetaData.USER_ID)));
			address.setTitle(result.getString(result.getColumnIndex(AddressesMetaData.TITLE)));
			address.setStreet(result.getString(result.getColumnIndex(AddressesMetaData.STREET)));
			address.setCity(result.getString(result.getColumnIndex(AddressesMetaData.CITY)));
			address.setZip(result.getString(result.getColumnIndex(AddressesMetaData.ZIP)));
			address.setState(result.getString(result.getColumnIndex(AddressesMetaData.STATE)));
			address.setCountry(result.getString(result.getColumnIndex(AddressesMetaData.COUNTRY)));
			address.setTimezone(result.getString(result.getColumnIndex(AddressesMetaData.TIMEZONE)));
			address.setCountry_name(result.getString(result.getColumnIndex(AddressesMetaData.COUNTRY_NAME)));
		}
		return address;
	}

	public ArrayList<Address> getAddressesFromRemoteDb() {
		boolean success = false;
		ArrayList<Address> addresses = new ArrayList<Address>();
		Address address = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_get");

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
						// TODO show an error.
					} else {
						JSONArray es = object.getJSONArray("data");
						int count = es.length();
						for (int i = 0; i < count; i++) {
							JSONObject e = es.getJSONObject(i);

							address = new Address();

							try {
								address.setId(e.getInt("id"));
								address.setUser_id(e.getInt("user_id"));
								address.setTitle(e.getString("title"));
								address.setStreet(e.getString("street"));
								address.setCity(e.getString("city"));
								address.setZip(e.getString("zip"));
								address.setState(e.getString("state"));
								address.setCountry(e.getString("country"));
								address.setTimezone(e.getString("timezone"));
								address.setCountry_name(e.getString("country_name"));
							} catch (JSONException ex) {
								Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
										.toString(), ex.getMessage());
							}
							addresses.add(address);
						}
					}
				}
			}
		} catch (Exception e) {

		}
		return addresses;
	}

	public boolean uploadAddressToLocalDb(Address address, int addressId) {
		boolean success = false;

		try {
			ContentValues cv = new ContentValues();

			if (addressId != 0)
				cv.put(AddressesMetaData.A_ID, addressId);
			else if (address.getId() > 0)
				cv.put(AddressesMetaData.A_ID, address.getId());
			else
				cv.put(AddressesMetaData.A_ID, 0);

			cv.put(AddressesMetaData.USER_ID, address.getUser_id());

			if (address.getTitle() != null) {
				cv.put(AddressesMetaData.TITLE, address.getTitle());
			} else {
				cv.put(AddressesMetaData.TITLE, "");
			}

			if (address.getStreet() != null) {
				cv.put(AddressesMetaData.STREET, address.getStreet());
			} else {
				cv.put(AddressesMetaData.STREET, "");
			}

			if (address.getCity() != null) {
				cv.put(AddressesMetaData.CITY, address.getCity());
			} else {
				cv.put(AddressesMetaData.CITY, "");
			}

			if (address.getZip() != null) {
				cv.put(AddressesMetaData.ZIP, address.getZip());
			} else {
				cv.put(AddressesMetaData.ZIP, "");
			}

			if (address.getState() != null) {
				cv.put(AddressesMetaData.STATE, address.getState());
			} else {
				cv.put(AddressesMetaData.STATE, "");
			}

			if (address.getCountry() != null) {
				cv.put(AddressesMetaData.COUNTRY, address.getCountry());
			} else {
				cv.put(AddressesMetaData.COUNTRY, "");
			}

			if (address.getTimezone() != null) {
				cv.put(AddressesMetaData.TIMEZONE, address.getTimezone());
			} else {
				cv.put(AddressesMetaData.TIMEZONE, "");
			}

			if (address.getCountry_name() != null) {
				cv.put(AddressesMetaData.COUNTRY_NAME, address.getCountry_name());
			} else {
				cv.put(AddressesMetaData.COUNTRY_NAME, "");
			}

			Data.getmContext().getContentResolver().insert(AddressesMetaData.CONTENT_URI, cv);
			success = true;
		} catch (Exception e) {

		}
		return success;
	}

	public int uploadAddressToRemoteDb(Address address) {
		int response = 0;
		boolean check = true;
		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_set");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken()));

			if (address.getId() > 0) {
				reqEntity.addPart("id", new StringBody(((Integer) address.getId()).toString()));
			}

			if (address.getTitle() != null) {
				reqEntity.addPart("title", new StringBody(address.getTitle()));
			} else {
				check = false;
			}

			if (address.getStreet() != null) {
				reqEntity.addPart("street", new StringBody(address.getStreet()));
			} else {
				reqEntity.addPart("street", new StringBody(""));
			}

			if (address.getCity() != null) {
				reqEntity.addPart("city", new StringBody(address.getCity()));
			} else {
				reqEntity.addPart("city", new StringBody(""));
			}

			if (address.getZip() != null) {
				reqEntity.addPart("zip", new StringBody(address.getZip()));
			} else {
				reqEntity.addPart("zip", new StringBody(""));
			}

			if (address.getState() != null) {
				reqEntity.addPart("state", new StringBody(address.getState()));
			} else {
				reqEntity.addPart("state", new StringBody(""));
			}

			if (address.getCountry() != null) {
				reqEntity.addPart("country", new StringBody(address.getCountry()));
			} else {
				reqEntity.addPart("country", new StringBody(""));
			}

			if (address.getTimezone() != null) {
				reqEntity.addPart("timezone", new StringBody(address.getTimezone()));
			} else {
				reqEntity.addPart("timezone", new StringBody(""));
			}

			if (check) {
				post.setEntity(reqEntity);

				if (networkAvailable) {
					HttpResponse rp = hc.execute(post);

					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");

							if (success == false) {
								Log.e("Create address error", object.getJSONObject("error").getString("reason"));
							}
							response = object.getInt("id");
							address.setId(response);
						}
					} else {
						Log.e("setTemplate - status", rp.getStatusLine().getStatusCode() + "");
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/templates_set", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} else {
				
			}
		} catch (Exception e) {

		}
		return response;
	}

	public void updateEventInLocalDb(Event event) {
		// TODO Auto-generated method stub
		
	}
}