package com.groupagendas.groupagenda.data;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bog.calendar.app.model.CEvent;
import com.bog.calendar.app.model.EventsHelper;
import com.google.android.c2dm.C2DMessaging;
import com.google.android.gcm.GCMRegistrar;
import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.address.Address;
import com.groupagendas.groupagenda.address.AddressProvider;
import com.groupagendas.groupagenda.address.AddressProvider.AMetaData.AddressesMetaData;
import com.groupagendas.groupagenda.alarm.AlarmsManagement;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.templates.Template;
import com.groupagendas.groupagenda.templates.TemplatesProvider;
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;
import com.groupagendas.groupagenda.utils.JSONUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;

public class DataManagement {
	public static boolean networkAvailable = true;
	public static boolean eventStatusChanged = false;
	public static ArrayList<Event> contactsBirthdays = new ArrayList<Event>();

	SimpleDateFormat day_index_formatter;
	SimpleDateFormat month_index_formatter;
	// private String user_timezone; TODO investigate if it's in use.

	public static final String SERVER_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT = "yyyy-MM-dd";

	private static final String TOKEN = "token";

	public static final String PROJECT_ID = "102163820835";
	private static final String LATEST_UPDATE_UNIX_TIMESTAMP = "from_unix_timestamp";
	private static final String DATA_DELTA_URL = "/mobile/data_delta";
	private static final String SUCCESS = "success";
	private static final String EVENTS = "events";
	private static final String TEMPLATES = "templates";
	private static final String CONTACTS = "contacts";
	private static final String GROUPS = "groups";
	private static final String EVENTS_REMOVED = "removed_events";
	private static final String CONTACTS_REMOVED = "removed_contacts";
	private static final String GROUPS_REMOVED = "removed_groups";
	private static final String ALARMS = "alarms";

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

	public static boolean updateAccount(Context context, boolean removeImage) {
		Account account = new Account(context);
		boolean success = false;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			reqEntity.addPart(Account.AccountMetaData.LASTNAME, new StringBody(account.getLastname(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.NAME, new StringBody(account.getName(), Charset.forName("UTF-8")));

			if(account.getBirthdate() != null){
				SimpleDateFormat sdf = new SimpleDateFormat(ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT, Locale.getDefault());
				reqEntity.addPart(Account.AccountMetaData.BIRTHDATE, new StringBody(sdf.format(account.getBirthdate().getTime()), Charset.forName("UTF-8")));
			}
			reqEntity.addPart(Account.AccountMetaData.SEX, new StringBody(account.getSex(), Charset.forName("UTF-8")));

			reqEntity.addPart(Account.AccountMetaData.COUNTRY, new StringBody(account.getCountry(context), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.CITY, new StringBody(account.getCity(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.STREET, new StringBody(account.getStreet(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.ZIP, new StringBody(account.getZip(), Charset.forName("UTF-8")));

			reqEntity.addPart(Account.AccountMetaData.TIMEZONE, new StringBody(account.getTimezone(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.PHONE1, new StringBody(account.getPhone1(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.PHONE1_CODE, new StringBody(account.getPhone1_code(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.PHONE2, new StringBody(account.getPhone2(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.PHONE2_CODE, new StringBody(account.getPhone2_code(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.PHONE3, new StringBody(account.getPhone3(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.PHONE3_CODE, new StringBody(account.getPhone3_code(), Charset.forName("UTF-8")));
			
			reqEntity.addPart(Account.AccountMetaData.EMAIL1, new StringBody(account.getEmail1(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.EMAIL2, new StringBody(account.getEmail2(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.EMAIL3, new StringBody(account.getEmail3(), Charset.forName("UTF-8")));
			reqEntity.addPart(Account.AccountMetaData.EMAIL4, new StringBody(account.getEmail4(), Charset.forName("UTF-8")));

			reqEntity.addPart("language", new StringBody(account.getLanguage(), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (!success) {
							Log.e("Change account ERROR", object.getJSONObject("error").getString("reason"));
						} else {

						}
					}
				}
			}

		} catch (Exception ex) {
			Data.setERROR(ex.getMessage());
			Reporter.reportError(context, "DataManagement", Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		// image
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_image");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context)));

			if (removeImage == false) {
				if (account.image_bytes != null) {
					ByteArrayBody bab = new ByteArrayBody(account.image_bytes, "image");
					reqEntity.addPart("image", bab);
				}
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("0"), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("1"), Charset.forName("UTF-8")));
			}

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			}
		} catch (Exception ex) {
			Data.setERROR(ex.getMessage());
			Reporter.reportError(context, "DataManagement", Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		return success;
	}

	public Account getAccountFromRemoteDb(Context context) {
		boolean success = false;
		Account u = null;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == true) {
						JSONObject profile = object.getJSONObject("profile");
						u = new Account(context);

						try {
							u.setUser_id(profile.getInt(Account.AccountMetaData.U_ID));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

						try {
							u.setName(profile.getString(Account.AccountMetaData.NAME));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setLastname(profile.getString(Account.AccountMetaData.LASTNAME));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setFullname(profile.getString(Account.AccountMetaData.FULLNAME));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

						try {
							String temp = profile.getString(Account.AccountMetaData.BIRTHDATE);
							if (temp != null && (temp.length() == ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT.length())) {
								u.setBirthdate(Utils.stringToCalendar(context, temp, ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT));
							}
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setSex(profile.getString(Account.AccountMetaData.SEX));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL), 0);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL1), 1);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail_verified((profile.getInt(Account.AccountMetaData.EMAIL1_VERIFIED) == 1), 1);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL2), 2);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail_verified((profile.getInt(Account.AccountMetaData.EMAIL2_VERIFIED) == 1), 2);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL3), 3);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail_verified((profile.getInt(Account.AccountMetaData.EMAIL3_VERIFIED) == 1), 3);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail(profile.getString(Account.AccountMetaData.EMAIL4), 4);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setEmail_verified((profile.getInt(Account.AccountMetaData.EMAIL4_VERIFIED) == 1), 4);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						/* PHONE1 */
//						Debug.waitForDebugger(); // TODO remove debugger call
						try {
							u.setPhone_code(profile.getString(Account.AccountMetaData.PHONE1_CODE), 1);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							String temp = profile.getString(Account.AccountMetaData.PHONE1);
							if ((temp != null) && (temp.length() > 0)) {
								temp = temp.replace(u.getPhone1_code(), "");
							}
							u.setPhone(temp, 1);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setPhone_verified(profile.getString(Account.AccountMetaData.PHONE1_VERIFIED).equalsIgnoreCase("1"), 1);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						/* PHONE2 */
						try {
							u.setPhone_code(profile.getString(Account.AccountMetaData.PHONE2_CODE), 2);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							String temp = profile.getString(Account.AccountMetaData.PHONE2);
							if ((temp != null) && (temp.length() > 0)) {
								temp = temp.replace(u.getPhone2_code(), "");
							}
							u.setPhone(temp, 2);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setPhone_verified(profile.getString(Account.AccountMetaData.PHONE2_VERIFIED).equalsIgnoreCase("1"), 2);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						/* PHONE3 */
						try {
							u.setPhone_code(profile.getString(Account.AccountMetaData.PHONE3_CODE), 3);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							String temp = profile.getString(Account.AccountMetaData.PHONE3);
							if ((temp != null) && (temp.length() > 0)) {
								temp = temp.replace(u.getPhone3_code(), "");
							}
							u.setPhone(temp, 3);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setPhone_verified(profile.getString(Account.AccountMetaData.PHONE3_VERIFIED).equalsIgnoreCase("1"), 3);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

						/* IMAGE */
						try {
							u.setImage(profile.getBoolean(Account.AccountMetaData.IMAGE));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setImage_url(profile.getString(Account.AccountMetaData.IMAGE_URL));
							u.image_bytes = Utils.imageToBytes(u.getImage_url(), context);
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setImage_thumb_url(profile.getString(Account.AccountMetaData.IMAGE_THUMB_URL));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

						try {
							u.setCountry(profile.getString(Account.AccountMetaData.COUNTRY));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setCity(profile.getString(Account.AccountMetaData.CITY));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setStreet(profile.getString(Account.AccountMetaData.STREET));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setZip(profile.getString(Account.AccountMetaData.ZIP));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

						try {
							u.setTimezone(profile.getString(Account.AccountMetaData.TIMEZONE));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setLocal_time(profile.getString(Account.AccountMetaData.LOCAL_TIME));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setLanguage(profile.getString(Account.AccountMetaData.LANGUAGE));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setSetting_default_view(profile.getString(Account.AccountMetaData.SETTING_DEFAULT_VIEW));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setSetting_date_format(profile.getString(Account.AccountMetaData.SETTING_DATE_FORMAT));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setSetting_ampm(profile.getInt(Account.AccountMetaData.SETTING_AMPM));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setGoogle_calendar_link(profile.getString(Account.AccountMetaData.GOOGLE_CALENDAR_LINK));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setColor_my_event(profile.getString(Account.AccountMetaData.COLOR_MY_EVENT));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setColor_attending(profile.getString(Account.AccountMetaData.COLOR_ATTENDING));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setColor_pending(profile.getString(Account.AccountMetaData.COLOR_PENDING));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setColor_invitation(profile.getString(Account.AccountMetaData.COLOR_INVITATION));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setColor_notes(profile.getString(Account.AccountMetaData.COLOR_NOTES));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setColor_birthday(profile.getString(Account.AccountMetaData.COLOR_BIRTHDAY));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setCreated(profile.getLong(Account.AccountMetaData.CREATED));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}
						try {
							u.setModified(profile.getLong(Account.AccountMetaData.MODIFIED));
						} catch (JSONException e) {
							Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
									.getMethodName().toString(), e.getMessage());
						}

					} else {
						Data.setERROR(object.getJSONObject("error").getString("reason"));
						Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
								.toString(), object.getJSONObject("error").getString("reason"));
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		Data.setLoadAccountData(false);
		return u;
	}

	public static boolean registerAccount(String language, String country, String timezone, String sex, String name, String lastname,
			String email, String phonecode, String phone, String password, String city, String street, String streetNo, String zip,
			boolean ampm, String dateFormat, String birthdate, Context context) {
		boolean success = false;

		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_register");

		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("language", new StringBody(language, Charset.forName("UTF-8")));
			reqEntity.addPart("country", new StringBody(country, Charset.forName("UTF-8")));
			reqEntity.addPart("timezone", new StringBody(timezone, Charset.forName("UTF-8")));
			reqEntity.addPart("sex", new StringBody(sex, Charset.forName("UTF-8")));
			reqEntity.addPart("name", new StringBody(name, Charset.forName("UTF-8")));
			reqEntity.addPart("lastname", new StringBody(lastname, Charset.forName("UTF-8")));
			reqEntity.addPart("email", new StringBody(email, Charset.forName("UTF-8")));
			reqEntity.addPart("phone1_code", new StringBody(phonecode, Charset.forName("UTF-8")));
			reqEntity.addPart("phone1", new StringBody(phone, Charset.forName("UTF-8")));
			reqEntity.addPart("password", new StringBody(password, Charset.forName("UTF-8")));
			reqEntity.addPart("confirm_password", new StringBody(password, Charset.forName("UTF-8")));
			reqEntity.addPart("city", new StringBody(city, Charset.forName("UTF-8")));
			reqEntity.addPart("street", new StringBody(street + " " + streetNo, Charset.forName("UTF-8")));
			reqEntity.addPart("zip", new StringBody(zip, Charset.forName("UTF-8")));
			reqEntity.addPart("ampm", new StringBody(ampm ? "1" : "0", Charset.forName("UTF-8")));
			reqEntity.addPart("date_format", new StringBody(dateFormat, Charset.forName("UTF-8")));
			reqEntity.addPart("birthdate", new StringBody(birthdate, Charset.forName("UTF-8")));

			post.setEntity(reqEntity);

			HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			Log.e("registerAccount()", "'sum shit failed.");
		}

		return success;
	}

	public boolean changeEmail(Context context, String email, int email_id) {
		boolean success = false;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/account_email_change");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("password", new StringBody(Data.getPassword(), Charset.forName("UTF-8")));
			reqEntity.addPart("email", new StringBody(email, Charset.forName("UTF-8")));
			if (email_id > 1)
				reqEntity.addPart("email_id", new StringBody(String.valueOf(email_id), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

		return success;
	}

	public boolean changeCalendarSettings(Context context, int am_pm, String defaultview, String dateformat) {
		boolean success = false;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/settings_update");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			reqEntity.addPart("setting_ampm", new StringBody(String.valueOf(am_pm), Charset.forName("UTF-8")));
			reqEntity.addPart("setting_default_view", new StringBody(defaultview, Charset.forName("UTF-8")));
			reqEntity.addPart("setting_date_format", new StringBody(dateformat, Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			success = false;
		}

		return success;
	}

	public boolean login(Context context, String email, String password) {
		boolean success = false;
		String token = null;
		SharedPreferences cred = Data.getmContext().getSharedPreferences("LATEST_CREDENTIALS", 0);
		Editor sPrefsEditor = cred.edit();

		if (networkAvailable) {
			try {
				WebService webService = new WebService(context);
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/login");
				post.setHeader("User-Agent", "Linux; AndroidPhone " + android.os.Build.VERSION.RELEASE);
				post.setHeader("Accept", "*/*");
				// post.setHeader("Content-Type", "text/vnd.ms-sync.wbxml");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("email", new StringBody(email, Charset.forName("UTF-8")));
				reqEntity.addPart("password", new StringBody(password, Charset.forName("UTF-8")));

				post.setEntity(reqEntity);

				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (success == true) {
							token = object.getString(TOKEN);
							JSONObject profile = object.getJSONObject("profile");
							int id = Integer.parseInt(profile.getString("user_id"));

							sPrefsEditor.putString("token", token);
							sPrefsEditor.commit();

							Data.setUserId(id);

							// Last login set
							post = new HttpPost(Data.getServerUrl() + "mobile/set_lastlogin");

							reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

							reqEntity.addPart(TOKEN, new StringBody(token, Charset.forName("UTF-8")));

							post.setEntity(reqEntity);

							rp = webService.getResponseFromHttpPost(post);
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
							Data.setERROR(object.getString("reason"));
							Reporter.reportError(context, DataManagement.class.toString(), "login", Data.getERROR());
						}
					}
				}

			} catch (Exception ex) {
				Data.setERROR(ex.getMessage());
				Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
						.toString(), ex.getMessage());
			}
		} else {
			if ((cred.getString("email", "").equals(email)) && (cred.getString("password", "").equals(password))) {
				success = true;
				Data.needToClearData = false;
			}
		}
		return success;
	}

	public void registerPhone() {
		try {
			getImei(Data.getmContext());
			GCMRegistrar.checkDevice(DataManagement.getContext());
			GCMRegistrar.checkManifest(DataManagement.getContext());
			Account account = new Account(DataManagement.getContext());
			account.setPushId(GCMRegistrar.getRegistrationId(DataManagement.getContext()));
			if (account.getPushId().equals("")) {

				C2DMessaging.register(Data.getmContext(), PROJECT_ID);
			} else {
				sendPushIdToServer(Data.getmContext(), account.getPushId());
			}
		} catch (Exception e) {
			Reporter.reportError(getContext(), DataManagement.class.toString(), "registerPhone", e.getMessage().toString());
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
			Reporter.reportError(context, DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), ex.getMessage());
		}

		return "";
	}

	public static HttpURLConnection sendHttpRequest(Context context, String path, String method, List<NameValuePair> paramsList,
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
			Reporter.reportError(context, DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), ex.getMessage());
		}

		return null;
	}

	public static void sendPushIdToServer(Context context, String pushId) {

		try {

			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/push/subscribe");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("device_uuid", new StringBody(pushId, Charset.forName("UTF-8")));
			reqEntity.addPart("platform", new StringBody(context.getResources().getString(R.string.platform), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);

			@SuppressWarnings("unused")
			HttpResponse rp = webService.getResponseFromHttpPost(post);

		} catch (Exception ex) {
			Reporter.reportError(context, DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), ex.getMessage());
		}
	}

	public static boolean setAutoIcons(Context context) {
		boolean success = false;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/settings_set_autoicons");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			Cursor result = Data.getmContext().getContentResolver()
					.query(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, null, null, null, null);
			result.moveToFirst();

			int i = 1;
			while (!result.isAfterLast()) {

				reqEntity.addPart(
						"autoicon[" + i + "][icon]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.ICON)), Charset
								.forName("UTF-8")));
				reqEntity.addPart(
						"autoicon[" + i + "][keyword]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.KEYWORD)), Charset
								.forName("UTF-8")));
				reqEntity.addPart(
						"autoicon[" + i + "][context]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutoiconMetaData.CONTEXT)), Charset
								.forName("UTF-8")));

				i++;
				result.moveToNext();
			}

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			Reporter.reportError(context, "DataManagement", Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}

		return success;
	}

	public static ArrayList<AutoIconItem> getAutoIcons(Context context) {
		ArrayList<AutoIconItem> Items = new ArrayList<AutoIconItem>();

		Cursor result = context.getContentResolver().query(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, null, null, null, null);
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
		result.close();

		return Items;
	}

	public static boolean setAutoColors(Context context) {
		boolean success = false;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/settings_set_autocolors");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context)));

			Cursor result = Data.getmContext().getContentResolver()
					.query(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, null, null, null, null);
			result.moveToFirst();

			int i = 1;
			while (!result.isAfterLast()) {

				reqEntity.addPart(
						"autocolor[" + i + "][color]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.COLOR)), Charset
								.forName("UTF-8")));
				reqEntity.addPart("autocolor[" + i + "][keyword]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD)),
								Charset.forName("UTF-8")));
				reqEntity.addPart("autocolor[" + i + "][context]",
						new StringBody(result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT)),
								Charset.forName("UTF-8")));

				i++;
				result.moveToNext();
			}

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			Reporter.reportError(context, "DataManagement", Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}

		return success;
	}

	public static ArrayList<AutoColorItem> getAutoColors(Context context) {
		ArrayList<AutoColorItem> Items = new ArrayList<AutoColorItem>();

		Cursor result = context.getContentResolver().query(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, null, null, null, null);
		if (result.moveToFirst()) {

			while (!result.isAfterLast()) {

				final AutoColorItem item = new AutoColorItem();

				item.id = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.C_ID));
				item.color = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.COLOR));
				item.keyword = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.KEYWORD));
				item.context = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AutocolorMetaData.CONTEXT));

				Items.add(item);

				result.moveToNext();
			}
		}
		result.close();

		return Items;
	}

	public boolean removeGroup(Context context, int group_id) {
		boolean success = false;
		String error = null;

		try {
			Account account = new Account(context);
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/group_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			try {
				reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}

			reqEntity.addPart("group_id", new StringBody(String.valueOf(group_id), Charset.forName("UTF-8")));
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			}
		} catch (Exception ex) {
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	public boolean editGroup(Context context, Group g) {
		boolean success = false;

		try {
			Account account = new Account(context);
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_edit");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			try {
				reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}

			if (g.remove_image == false) {
				if (g.image_bytes != null) {
					ByteArrayBody bab = new ByteArrayBody(g.image_bytes, "image");
					reqEntity.addPart("image", bab);
				}
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("0"), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("remove_image", new StringBody(String.valueOf("1"), Charset.forName("UTF-8")));
			}

			reqEntity.addPart("group_id", new StringBody(String.valueOf(g.group_id), Charset.forName("UTF-8")));
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("title", new StringBody(g.title, Charset.forName("UTF-8")));

			Map<String, String> contacts = g.contacts;
			if (contacts != null) {
				for (int i = 0, l = contacts.size(); i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(contacts.get(String.valueOf(i)), Charset.forName("UTF-8")));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody("", Charset.forName("UTF-8")));
			}

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

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
			}
		} catch (Exception ex) {
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
			return false;
		}
		return success;
	}

	public ArrayList<Event> filterInvites(ArrayList<Event> events) {
		ArrayList<Event> newEventList = new ArrayList<Event>();
		for (Event event : events) {
			if (event.getStatus() == 4) {
				newEventList.add(event);
			}
		}
		return newEventList;
	}

	public Event getNativeCalendarEvent(long id) {
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
				item.setNative(true);
				item.setIs_owner(false);
				item.setType(Event.NATIVE_EVENT);
				item.setStatus(1);

				item.setEvent_id(cursor.getInt(0));
				item.setTitle(cursor.getString(1));
				item.setDescription(cursor.getString(2));
				item.setTimezone(cursor.getString(6));

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
		;

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

						item.setNative(true);
						item.setIs_owner(false);
						item.setType(Event.NATIVE_EVENT);
						item.setStatus(1);

						item.setEvent_id(cursor.getInt(0));
						item.setTitle(cursor.getString(1));
						item.setDescription(cursor.getString(2));
						item.setTimezone(cursor.getString(6));

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
		return events;
	}

	public ArrayList<CEvent> getCalendarEvents() {
		CEvent citem;
		ArrayList<CEvent> citems = new ArrayList<CEvent>();
		ArrayList<Event> items = new ArrayList<Event>();

		items = EventManagement.getEventsFromLocalDb(getContext(), false);

		for (int i = 0, l = items.size(); i < l; i++) {
			final Event item = items.get(i);

			citem = EventsHelper.generateEvent(getContext(), item);
			citems.add(citem);
		}

		return citems;
	}

	private class ChangeEventStatus extends AsyncTask<Object, Void, Void> {

		private boolean success = false;

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int event_id = (Integer) params[0];
				String status = (String) params[1];
				WebService webService = new WebService(getContext());
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/set_event_status");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken(getContext()), Charset.forName("UTF-8")));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id), Charset.forName("UTF-8")));
				reqEntity.addPart("status", new StringBody(status, Charset.forName("UTF-8")));

				post.setEntity(reqEntity);
				if (networkAvailable) {
					HttpResponse rp = webService.getResponseFromHttpPost(post);

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
				}
			} catch (Exception ex) {
				Reporter.reportError(getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
						.toString(), ex.getMessage());
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

	/**
	 * @deprecated
	 * @param event_id
	 * @param context
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Event updateEventByIdFromRemoteDb(int event_id, Context context) throws ExecutionException, InterruptedException {
		Event event = null;
		try {
			WebService webService = new WebService();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(), Charset.forName("UTF-8")));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			HttpResponse rp = null;
			rp = webService.getResponseFromHttpPost(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject e1 = new JSONObject(resp);
					boolean success = e1.getBoolean("success");
					if (!success) {
						Log.e("Edit event status ERROR", e1.getJSONObject("error").getString("reason"));
					} else {
						JSONObject e = e1.getJSONObject("event");
						event = JSONUtils.createEventFromJSON(context, e);
						EventManagement.insertEventToLocalDB(context, event);
					}
				}
			}
		} catch (Exception e) {
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e.getMessage());
		}
		return event;
	}

	public boolean changeEventStatus(int event_id, String status) {
		Object[] array = { event_id, status };
		try {
			new ChangeEventStatus().execute(array).get();
		} catch (InterruptedException e) {
			Reporter.reportError(getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e.getMessage());
		} catch (ExecutionException e) {
			Reporter.reportError(getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), e.getMessage());
		}
		return DataManagement.eventStatusChanged;
	}

	public static String getError() {
		String temp = Data.ERROR;
		if (temp != null)
			return Data.ERROR;
		else
			return "Failed getting error message.";
	}

	public static void setError(String error) {
		Data.ERROR = error;
	}

	public static String getCONNECTION_ERROR() {
		return Data.getConnectionError();
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
	// public void updateEventsAdapter(ArrayList<Event> events, EventsAdapter
	// eAdapter) {
	//
	// }

	@Deprecated
	public static Context getContext() {
		return Data.getmContext();
	}

	/**
	 * Upload Event object to remote database as a template.
	 * 
	 * Method creates a multipart entity object, fills it with submitted event's
	 * data. Afterwards creates a connection to remote server and, if successful
	 * - uploads data. If not - stores it in an UnuploadedData object ArrayList.
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
	public static int insertTemplateToRemoteDb(Context context, Event event) {
		int response = 0;
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/templates_set");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			if (event.getEvent_id() > 0)
				reqEntity.addPart("template_id", new StringBody(((Integer) event.getEvent_id()).toString(), Charset.forName("UTF-8")));
			else
				reqEntity.addPart("template_id", new StringBody("", Charset.forName("UTF-8")));

			if (event.getIcon() != null)
				reqEntity.addPart("icon", new StringBody(event.getIcon(), Charset.forName("UTF-8")));

			if (event.getColor() != null)
				reqEntity.addPart("color", new StringBody(event.getColor(), Charset.forName("UTF-8")));

			if (event.getActualTitle() != null) {
				reqEntity.addPart("title", new StringBody(event.getActualTitle(), Charset.forName("UTF-8")));
				reqEntity.addPart("template_title", new StringBody(event.getActualTitle(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("title", new StringBody("", Charset.forName("UTF-8")));
				reqEntity.addPart("template_title", new StringBody("", Charset.forName("UTF-8")));
			}

			if (event.getDescription() != null) {
				reqEntity.addPart("description", new StringBody(event.getDescription(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("description", new StringBody("", Charset.forName("UTF-8")));
			}

			if (event.getCountry() != null)
				reqEntity.addPart("country", new StringBody(event.getCountry(), Charset.forName("UTF-8")));

			if (event.getCity() != null)
				reqEntity.addPart("city", new StringBody(event.getCity(), Charset.forName("UTF-8")));

			if (event.getStreet() != null)
				reqEntity.addPart("street", new StringBody(event.getStreet(), Charset.forName("UTF-8")));

			if (event.getZip() != null)
				reqEntity.addPart("zip", new StringBody(event.getZip(), Charset.forName("UTF-8")));

			if (event.getTimezone() != null)
				reqEntity.addPart("timezone", new StringBody(event.getTimezone(), Charset.forName("UTF-8")));

			long timeInMillis = event.getStartCalendar().getTimeInMillis();
			if (timeInMillis > 0)
				reqEntity.addPart("timestamp_start_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(timeInMillis), Charset.forName("UTF-8")));

			timeInMillis = event.getEndCalendar().getTimeInMillis();
			if (timeInMillis > 0)
				reqEntity.addPart("timestamp_end_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(timeInMillis), Charset.forName("UTF-8")));

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

			// if (event.getLocation() != null)
			// reqEntity.addPart("location", new
			// StringBody(event.getLocation()));
			//
			// if (event.go_by != null)
			// reqEntity.addPart("go_by", new StringBody(event.go_by));
			//
			// if (event.take_with_you != null)
			// reqEntity.addPart("take_with_you", new
			// StringBody(event.take_with_you));
			//
			// if (event.cost != null)
			// reqEntity.addPart("cost", new StringBody(event.cost));
			//
			// if (event.accomodation != null)
			// reqEntity.addPart("accomodation", new
			// StringBody(event.accomodation));
			//
			// if (Data.selectedContacts != null &&
			// !Data.selectedContacts.isEmpty()) {
			// event.assigned_contacts = new int[Data.selectedContacts.size()];
			// int i = 0;
			// for (Contact contact : Data.selectedContacts) {
			// event.assigned_contacts[i] = contact.contact_id;
			// i++;
			// }
			// }

			// if (event.assigned_contacts != null) {
			// for (int i = 0, l = event.assigned_contacts.length; i < l; i++) {
			// reqEntity.addPart("contacts[]", new
			// StringBody(String.valueOf(event.assigned_contacts[i])));
			// }
			// } else {
			// reqEntity.addPart("contacts[]", new StringBody(""));
			// }
			//
			// if (event.assigned_groups != null) {
			// for (int i = 0, l = event.assigned_groups.length; i < l; i++) {
			// reqEntity.addPart("groups[]", new
			// StringBody(String.valueOf(event.assigned_groups[i])));
			// }
			// } else {
			// reqEntity.addPart("groups[]", new StringBody(""));
			// }

			// if (event.reminder1 != null) {
			// reqEntity.addPart("reminder1", new StringBody(event.reminder1));
			// }
			//
			// if (event.reminder2 != null) {
			// reqEntity.addPart("reminder2", new StringBody(event.reminder2));
			// }
			//
			// if (event.reminder3 != null) {
			// reqEntity.addPart("reminder3", new StringBody(event.reminder3));
			// }

			// TODO find out wtf is bd in event
			// if (event.birthday) {
			// reqEntity.addPart("bd", new StringBody("1"));
			// }

			post.setEntity(reqEntity);

			if (networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						boolean success = object.getBoolean("success");

						if (success == false) {
							Log.e("Create event error", object.getJSONObject("error").getString("reason"));
						}
						response = object.getInt("template_id");
						// event.event_id = response;
					}
				} else {
					Log.e("setTemplate - status", rp.getStatusLine().getStatusCode() + "");
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, "DataManagement", Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		}
		return response;
	}

	/**
	 * Upload Event object to local database as a template.
	 * 
	 * Method creates a ContentValues object, fills it with submitted event's
	 * data. Afterwards gets a local daatabase content resolver and, if
	 * successful - uploads data into it.
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
	public static boolean insertTemplateToLocalDb(Context context, Template template) {
		boolean success = false;

		try {
			ContentValues cv = template.toContentValues();

			if (template.getTemplate_id() > 0)
				cv.put(TemplatesMetaData.T_ID, template.getTemplate_id());
			else
				cv.put(TemplatesMetaData.T_ID, 0);

			if (template.getIcon() != null)
				cv.put(TemplatesMetaData.ICON, template.getIcon());
			else
				cv.put(TemplatesMetaData.ICON, "");

			if (template.getColor() != null)
				cv.put(TemplatesMetaData.COLOR, template.getColor());

			if (template.getTitle() != null)
				cv.put(TemplatesMetaData.TITLE, template.getTitle());
			else
				cv.put(TemplatesMetaData.TITLE, "Untitled");

//			long timeInMillis = template.getStartCalendar().getTimeInMillis();
//			if (timeInMillis > 0)
//				cv.put(TemplatesMetaData.TIME_START, Utils.millisToUnixTimestamp(timeInMillis));

//			timeInMillis = template.getEndCalendar().getTimeInMillis();
//			if (timeInMillis > 0)
//				cv.put(TemplatesMetaData.TIME_START, Utils.millisToUnixTimestamp(timeInMillis));

			if (template.getDescription_() != null)
				cv.put(TemplatesMetaData.DESC, template.getDescription_());
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

			context.getContentResolver().insert(TemplatesMetaData.CONTENT_URI, cv);
			success = true;
		} catch (Exception e) {
			Log.e("uploadTemplateToLocalDb(context, template[event_id=" + template.getTemplate_id() + "])", "Sum shit has just failed!");
		}

		return success;
	}

	// TODO getTemplatesFromRemoteDb() documentation pending.
	public static void getTemplatesFromRemoteDb(Context context) {
		String GET_TEMPLATES_FROM_REMOTE_DB_URL = "mobile/templates_get";
		
		boolean success = false;
		Template template = null;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + GET_TEMPLATES_FROM_REMOTE_DB_URL);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean(SUCCESS);

					if (success == false) {
						// error = object.getString("error");
					} else {
						JSONArray es = object.getJSONArray(TEMPLATES);
						for (int i = 0; i < es.length(); i++) {
							try {
								JSONObject e = es.getJSONObject(i);
								template = JSONUtils.createTemplateFromJSON(context, e);
								if (template != null) {
									template.setUploadedToServer(true);
									insertTemplateToLocalDb(context, template);
								}
							} catch (JSONException ex) {
								Log.e("getTemplatesFromRemoteDb(contex)", "Failed parsing JSON");
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, "DataManagement", Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		};
	}

	public static Template getTemplateFromLocalDb(Context context, int template_id) {
		Template template = new Template();
		Uri uri = Uri.parse(TemplatesMetaData.CONTENT_URI.toString() + "/" + template_id);
		Cursor result = Data.getmContext().getContentResolver().query(uri, null, null, null, null);

		if (result.moveToFirst()) {
			template.setColor(result.getString(result.getColumnIndex(TemplatesMetaData.COLOR)));
			template.setIcon(result.getString(result.getColumnIndex(TemplatesMetaData.ICON)));

			template.setTitle(result.getString(result.getColumnIndex(TemplatesMetaData.TITLE)));
//			template.setStartCalendar(Utils.createCalendar(result.getLong(result.getColumnIndex(TemplatesMetaData.TIME_START)),
//					result.getString(result.getColumnIndex(TemplatesMetaData.TIMEZONE))));
//			template.setEndCalendar(Utils.createCalendar(result.getLong(result.getColumnIndex(TemplatesMetaData.TIME_END)),
//					result.getString(result.getColumnIndex(TemplatesMetaData.TIMEZONE))));
			template.setTimezone(result.getString(result.getColumnIndex(TemplatesMetaData.TIMEZONE)));
			template.setDescription_(result.getString(result.getColumnIndex(TemplatesMetaData.DESC)));

			template.setCountry(result.getString(result.getColumnIndex(TemplatesMetaData.COUNTRY)));
			template.setCity(result.getString(result.getColumnIndex(TemplatesMetaData.CITY)));
			template.setStreet(result.getString(result.getColumnIndex(TemplatesMetaData.STREET)));
			template.setZip(result.getString(result.getColumnIndex(TemplatesMetaData.ZIP)));

			template.setLocation(result.getString(result.getColumnIndex(TemplatesMetaData.LOCATION)));
			template.setGo_by(result.getString(result.getColumnIndex(TemplatesMetaData.GO_BY)));
			template.setTake_with_you(result.getString(result.getColumnIndex(TemplatesMetaData.TAKE_WITH_YOU)));
			template.setCost(result.getString(result.getColumnIndex(TemplatesMetaData.COST)));
			template.setAccomodation(result.getString(result.getColumnIndex(TemplatesMetaData.ACCOMODATION)));
			
			try {
				ArrayList<Invited> invites = new ArrayList<Invited>();

				template.setMyInvite(JSONUtils.createInvitedListFromJSONArrayString(context,
						result.getString(result.getColumnIndex(TemplatesMetaData.INVITED)), invites));
				template.setInvited(invites);
			} catch (Exception e) {
				Log.e("getTemplateFromLocalDb", "Error parsing invited array for T_ID: " + template.getTemplate_id());
			}
		}
		
		result.close();

		return template;
	}

	/**
	 * Get all templates
	 * 
	 * Get all templates from remote database and store them in local database
	 * and temporary memory.
	 * 
	 * @author meska.lt@gmail.com
	 * @version 1.1
	 * @since 2012-09-24
	 * @return ArrayList of Event objects retrieved from remote database.
	 */
	public static ArrayList<Template> getTemplateProjectionsFromLocalDb(Context context) {
		ArrayList<Template> list = new ArrayList<Template>();
		
		if (TemplatesProvider.mOpenHelper == null)
			TemplatesProvider.mOpenHelper = new TemplatesProvider.DatabaseHelper(context);
		
		Cursor result = TemplatesProvider.mOpenHelper.getReadableDatabase().rawQuery("SELECT "
			+ TemplatesProvider.TMetaData.TemplatesMetaData._ID + ", "
			+ TemplatesProvider.TMetaData.TemplatesMetaData.T_ID + ", "
			+ TemplatesProvider.TMetaData.TemplatesMetaData.T_TITLE + ", "
			+ TemplatesProvider.TMetaData.TemplatesMetaData.COLOR + " "
			+ "FROM "
			+ TemplatesProvider.TMetaData.TEMPLATES_TABLE
			, null
		);
		
		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				Template templateProjection = new Template();
				
				templateProjection.setInternalID(result.getLong(result.getColumnIndexOrThrow(TemplatesProvider.TMetaData.TemplatesMetaData._ID)));
				templateProjection.setTemplate_id(result.getInt(result.getColumnIndexOrThrow(TemplatesProvider.TMetaData.TemplatesMetaData.T_ID)));
				templateProjection.setTitle(result.getString(result.getColumnIndexOrThrow(TemplatesProvider.TMetaData.TemplatesMetaData.T_TITLE)));
				templateProjection.setColor(result.getString(result.getColumnIndexOrThrow(TemplatesProvider.TMetaData.TemplatesMetaData.COLOR)));
				
				list.add(templateProjection);
				result.moveToNext();
			}
		}
		
		result.close();
		return list;
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

	public ArrayList<Address> getAddressesFromRemoteDb(Context context) {
		boolean success = false;
		ArrayList<Address> addresses = new ArrayList<Address>();
		Address address = null;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);

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
								Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2]
										.getMethodName().toString(), ex.getMessage());
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
			WebService webService = new WebService(getContext());
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_set");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("token", new StringBody(Data.getToken(getContext()), Charset.forName("UTF-8")));

			if (address.getId() > 0) {
				reqEntity.addPart("id", new StringBody(((Integer) address.getId()).toString(), Charset.forName("UTF-8")));
			}

			if (address.getTitle() != null) {
				reqEntity.addPart("title", new StringBody(address.getTitle(), Charset.forName("UTF-8")));
			} else {
				check = false;
			}

			if (address.getStreet() != null) {
				reqEntity.addPart("street", new StringBody(address.getStreet(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("street", new StringBody("", Charset.forName("UTF-8")));
			}

			if (address.getCity() != null) {
				reqEntity.addPart("city", new StringBody(address.getCity(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("city", new StringBody("", Charset.forName("UTF-8")));
			}

			if (address.getZip() != null) {
				reqEntity.addPart("zip", new StringBody(address.getZip(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("zip", new StringBody("", Charset.forName("UTF-8")));
			}

			if (address.getState() != null) {
				reqEntity.addPart("state", new StringBody(address.getState(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("state", new StringBody("", Charset.forName("UTF-8")));
			}

			if (address.getCountry() != null) {
				reqEntity.addPart("country", new StringBody(address.getCountry(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("country", new StringBody("", Charset.forName("UTF-8")));
			}

			if (address.getTimezone() != null) {
				reqEntity.addPart("timezone", new StringBody(address.getTimezone(), Charset.forName("UTF-8")));
			} else {
				reqEntity.addPart("timezone", new StringBody("", Charset.forName("UTF-8")));
			}

			if (check) {
				post.setEntity(reqEntity);

				if (networkAvailable) {
					HttpResponse rp = webService.getResponseFromHttpPost(post);

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
				}
			} else {

			}
		} catch (Exception e) {

		}
		return response;
	}

	public void createTemplate(Context context, Template template) {
//		// TODO implement offline mode
//		Integer templateId = uploadTemplateToRemoteDb(context, template);
		insertTemplateToLocalDb(context, template);

	}

	// TODO javadoc
	public static void synchronizeWithServer(Context context, AsyncTask<Void, Integer, Void> dataSyncTask, long latestUpdateUnixTimestamp) {
		Log.e("synchronizeWithServer", "synchronizing with timestamp " + new Date(Utils.unixTimestampToMilis(latestUpdateUnixTimestamp)));
		if (!DataManagement.networkAvailable) {
			Log.e("synchronizeWithServer", "reason: no network connectivity");
			return;
		}

		JSONObject response = getDataChangesJSON(context, latestUpdateUnixTimestamp);
		if (response == null) {
			Log.e("synchronizeWithServer", "null response");
			return;
		}

		if (!response.optBoolean(SUCCESS)) {
			Log.e("synchronizeWithServer", "reason: " + response.optString("reason"));
			return;
		}
		JSONArray contactChanges = response.optJSONArray(CONTACTS);
		JSONArray deletedContacts = response.optJSONArray(CONTACTS_REMOVED);
		ContactManagement.syncContacts(context, JSONUtils.JSONArrayToContactsArray(contactChanges, context),
				JSONUtils.JSONArrayToLongArray(deletedContacts));
		Intent intent = new Intent(C2DMReceiver.REFRESH_CONTACT_LIST);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		
		JSONArray eventChanges = response.optJSONArray(EVENTS);
		JSONArray deletedEvents = response.optJSONArray(EVENTS_REMOVED);
		EventManagement.syncEvents(context, JSONUtils.JSONArrayToEventArray(context, eventChanges),
				JSONUtils.JSONArrayToLongArray(deletedEvents));

		JSONArray groupChanges = response.optJSONArray(GROUPS);
		JSONArray deletedGroups = response.optJSONArray(GROUPS_REMOVED);
		ContactManagement
				.syncGroups(context, JSONUtils.JSONArrayToGroupsArray(groupChanges, context), JSONUtils.JSONArrayToLongArray(deletedGroups));
		
		DataManagement.getAlarmsFromServer(context);

	}
	
	public static void getAlarmsFromServer(Context context){
		try{
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "/mobile/alarms/get");
	
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
	
			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONArray alarms = new JSONObject(resp).optJSONArray(ALARMS);
					AlarmsManagement.syncAlarms(context, JSONUtils.JSONArrayToAlarmArray(alarms, context));
				}
			} else {
				// TODO set error code
				Log.e("Get Alarms - status", rp.getStatusLine().getStatusCode() + "");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private static JSONObject getDataChangesJSON(Context context, long latestUpdateUnixTimestamp) {
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + DATA_DELTA_URL);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart(LATEST_UPDATE_UNIX_TIMESTAMP, new StringBody("" + latestUpdateUnixTimestamp, Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					return new JSONObject(resp);
				}
			} else {
				// TODO set error code
				Log.e("Data synchronize - status", rp.getStatusLine().getStatusCode() + "");
			}

		} catch (Exception e) {

		}
		return null;
	}

	public static void clearAllData(Context context) {
		Log.d("DataManagement.class", "clearing data");
		// Delete old data
		context.getContentResolver().delete(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(TemplatesProvider.TMetaData.TemplatesMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI, "", null);
		context.getContentResolver().delete(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, "", null);
		// getContentResolver().delete(EventsProvider.EMetaData.InvitedMetaData.CONTENT_URI,
		// "", null);
		context.getContentResolver().getType(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI);

	}
	
	public static JSONObject confirmPhoneNumber(Context context, String number_id, String verify_code) {
		JSONObject object = null;
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/phone/confirm");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("phone_id", new StringBody(number_id, Charset.forName("UTF-8")));
			reqEntity.addPart("verify_code", new StringBody(verify_code, Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					object = new JSONObject(resp);
					
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, context.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		
		return object;
	}
	
	public static JSONObject resendConfirmPhoneNumberCode(Context context, String number_id) {
		JSONObject object = null;
		
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/phone/resend");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("phone_id", new StringBody(number_id, Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					object = new JSONObject(resp);
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, context.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
		
		return object;
	}
}