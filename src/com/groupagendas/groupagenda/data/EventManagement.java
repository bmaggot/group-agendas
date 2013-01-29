package com.groupagendas.groupagenda.data;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.SaveDeletedData;
import com.groupagendas.groupagenda.SaveDeletedData.SDMetaData;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatThreadObject;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventEditActivity;
import com.groupagendas.groupagenda.events.EventsAdapter;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.events.NativeCalendarReader;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.JSONUtils;
import com.groupagendas.groupagenda.utils.Utils;

/**
 * Class is responsible for communicating with remote and local databases.
 * 
 * @author justinas.marcinka@gmail.com
 * @version 1.0
 * @see EventManagement.initUserTimezone(Context context)
 */
public class EventManagement {
	static String error = "";
	public static final String CLASS_NAME = "EventManagement.class";
	private static SimpleDateFormat day_index_formatter = new SimpleDateFormat(
			EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT, Locale.getDefault());
	private static SimpleDateFormat month_index_formatter = new SimpleDateFormat(
			EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT, Locale.getDefault());
	public static String user_timezone = null; // initUserTimezone(Context
												// context) must be called
												// whenever method with context
												// as param is called
	public static final String DATA_ENCODING = "UTF-8";

	private static final String GET_EVENTS_FROM_REMOTE_DB_URL = "mobile/events_list";
	public static int eventsInOnePostRetrieveSize = 200;
	

	// //////////////////////////METHODS THAT ARE USED BY
	// UI////////////////////////////////////////////////////////////////////
	/**
	 * Method gets event from remote db and writes it to local databases
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param id
	 *            Event id in remote database
	 * @return true if success false otherwise. Also, error message is set in
	 *         via getError()
	 */
	public static boolean getEventByIdFromRemoteDb(Context context, String id) {
		initUserTimezone(context);

		boolean success = false;
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("event_id", new StringBody(id, Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						// TODO Error msg
					} else {
						JSONObject e = object.optJSONObject("event");
						if (e == null) {
							// TODO Error msg
							return false;
						}
						Event event = JSONUtils.createEventFromJSON(context, e);
						insertEventToLocalDB(context, event);

					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		}
		return success;
	}

	public static boolean updateEventByIdFromRemoteDb(Context context, String id) {
		initUserTimezone(context);

		boolean success = false;
		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_get");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("event_id", new StringBody(id, Charset.forName("UTF-8")));

			post.setEntity(reqEntity);
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						// TODO Error msg
					} else {
						JSONObject e = object.optJSONObject("event");
						if (e == null) {
							// TODO Error msg
							return false;
						}
						Event event = JSONUtils.createEventFromJSON(context, e);
						event.setInternalID(getEventFromLocalDb(context, event.getEvent_id(), ID_EXTERNAL).getInternalID());
						updateEventInLocalDb(context, event);

					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		}
		return success;
	}

	/**
	 * @author justinas.marcinka@gmail.com Method creates event in both remote
	 *         and local databases. NOT YET IMPLEMENTED: If there is no
	 *         connectivity to remote DB, event should be created only in local
	 *         DB and saved task to upload data when available.
	 * @param event
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void createNewEvent(Context context, Event event) {
		initUserTimezone(context);

		if (DataManagement.networkAvailable) {
			int id = createEventInRemoteDb(context, event);

			if (id > 0) {
				event.setEvent_id(id);
				event.setUploadedToServer(true);
			} else {
				event.setUploadedToServer(false);
				// TODO report error
			}
		} else {
			event.setUploadedToServer(false);
		}

		insertEventToLocalDB(context, event);

	}

	/**
	 * @author justinas.marcinka@gmail.com Method deletes event from both remote
	 *         and local databases. NOT YET IMPLEMENTED: If there is no
	 *         connectivity to remote DB, event should be deleted only in local
	 *         DB and saved task to delete data from remote db when available.
	 * @param event
	 *            Fields that MUST BE SET:<br>
	 *            Event.internalID <br>
	 *            Event.event_ID <br>
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void deleteEvent(Context context, Event event) {
		initUserTimezone(context);
		Boolean deletedFromRemote = false;
		if (DataManagement.networkAvailable) {
			deletedFromRemote = removeEvent(context, event.getEvent_id());
		}

		if (!deletedFromRemote) {
			SaveDeletedData offlineDeletedEvents1 = new SaveDeletedData(context);
			offlineDeletedEvents1.addEventForLaterDelete(event.getEvent_id());

		}

		deleteEventFromLocalDb(context, event.getInternalID(), event.getEvent_id());
	}

	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 *            - method call context
	 * @param event
	 *            - event which invitation should be responded to. <br>
	 *            Fields that MUST BE SET:<br>
	 *            Event.internalID <br>
	 *            Event.event_ID <br>
	 *            Event.status<br>
	 * @since 2012-10-09
	 * @version 1.0
	 */

	public static void respondToInvitation(Context context, Event event) {
		initUserTimezone(context);
		if (!Invited.validateResponse(event.getStatus())) {
			Log.e(CLASS_NAME + " ERROR RESPONDING TO INVITE", "Unknown state: " + event.getStatus());
			return;
		}
		if (DataManagement.networkAvailable) {
			event.setUploadedToServer(updateEventStatusInServer(context, event));
		} else {
			event.setUploadedToServer(false);
		}
		updateEventStatusInLocalDb(context, event); // TODO OFFLINE MODE

	}

	/**
	 * returns all existing chat threads
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @author interfectos@gmail.com
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static ArrayList<ChatThreadObject> getExistingChatThreads(Context context) {
		initUserTimezone(context);
		Uri uri = EMetaData.EventsMetaData.CONTENT_URI;
		String[] projection = { EMetaData.EventsMetaData.TITLE, EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS,
				EMetaData.EventsMetaData.NEW_MESSAGES_COUNT, EMetaData.EventsMetaData.MESSAGES_COUNT, EMetaData.EventsMetaData.E_ID };
		String selection = EMetaData.EventsMetaData.MESSAGES_COUNT + ">0" + " AND " + EMetaData.EventsMetaData.STATUS + "!="
				+ Invited.REJECTED;
		String sortOrder = EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS + " DESC ";
		Cursor result = context.getContentResolver().query(uri, projection, selection, null, sortOrder);
		ArrayList<ChatThreadObject> resultList = new ArrayList<ChatThreadObject>();
		if (result.moveToFirst()) {
			do {
				ChatThreadObject cto = new ChatThreadObject();
				cto.setTitle(result.getString(result.getColumnIndex(EMetaData.EventsMetaData.TITLE)));
				cto.setTimeStart(result.getLong(result.getColumnIndex(EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS)));
				cto.setNew_messages(result.getInt(result.getColumnIndex(EMetaData.EventsMetaData.NEW_MESSAGES_COUNT)));
				cto.setMessage_count(result.getInt(result.getColumnIndex(EMetaData.EventsMetaData.MESSAGES_COUNT)));
				cto.setEvent_id(result.getInt(result.getColumnIndex(EMetaData.EventsMetaData.E_ID)));
				resultList.add(cto);
			} while (result.moveToNext());
		}
		result.close();
		return resultList;
	}

	/**
	 * @author justinas.marcinka@gmail.com Updates event in both local and
	 *         remote db with given info. Offline mode NOT YET IMPLEMENTED.
	 * @param event
	 * <br>
	 *            Fields that MUST BE SET:<br>
	 *            Event.internalID <br>
	 *            Event.event_ID <br>
	 *            all other fields that contain data to be updated
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void updateEvent(Context context, Event event) {
		initUserTimezone(context);
		if (DataManagement.networkAvailable) {
			event.setUploadedToServer(editEvent(context, event));
		} else {
			event.setUploadedToServer(false);
		}
		updateEventInLocalDb(context, event);
	}

	// TODO write a javadoc for inviteExtraContacts()
	public static boolean inviteExtraContacts(Context context, String e_id, ArrayList<Contact> contacts) {
		initUserTimezone(context);
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_invite_extra");
		// Charset charset = Charset.forName(DATA_ENCODING);

		// MultipartEntity reqEntity = new
		// MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "whatever",
		// charset);
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			// reqEntity.addPart(EVENT_ID, new StringBody(e_id,
			// Charset.forName("UTF-8")));
			reqEntity.addPart(EVENT_ID, new StringBody("" + Integer.parseInt(e_id), Charset.forName("UTF-8")));

			if (contacts != null) {
				for (Contact c : contacts) {
					reqEntity.addPart("contacts[]", new StringBody("" + c.contact_id, Charset.forName("UTF-8")));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody("", Charset.forName("UTF-8")));
			}

			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						if (!success) {
							Log.e("inviteExtraContacts() CATCH!", object.getJSONObject("error").getString("reason"));
						}
					}
				} else {
					Log.e("inviteExtraContacts()", rp.getStatusLine().getStatusCode() + "");
				}
			}
		} catch (Exception ex) {
			Log.e("EventManagement", "inviteExtraContacts() CATCH!");
		}
		return success;
	}

	public static final int TM_EVENTS_FROM_GIVEN_DATE = 0;
	public static final int TM_EVENTS_ON_GIVEN_DAY = 1;
	public static final int TM_EVENTS_ON_GIVEN_MONTH = 2;
	public static final int TM_EVENTS_ON_GIVEN_YEAR = 3;
	/**
	 * @author justinas.marcinka@gmail.com categories for exchange event data
	 *         with remote server
	 */

	private static final String TOKEN = "token";
	private static final String CATEGORY = "category";
	private static final String SUCCESS = "success";
	private static final String REASON = "reason";

	public static final String EVENT_ID = "event_id";
	public static final String TIMEZONE = "timezone";
	public static final String TIME_START_UTC = "timestamp_start_utc";
	public static final String TIME_END_UTC = "timestamp_end_utc";
	public static final String USER_ID = "user_id";
	public static final String STATUS = "status";
	public static final String IS_OWNER = "is_owner";
	public static final String TYPE = "type";
	public static final String TITLE = "title";
	public static final String ICON = "icon";
	public static final String COLOR = "color";
	public static final String TEXT_COLOR = "display_text_color"; // 2012-10-24
	public static final String DISPLAY_COLOR = "display_color"; // 2012-10-24
	public static final String DESCRIPTION = "description";
	public static final String LOCATION = "location";
	public static final String ACCOMODATION = "accomodation";
	public static final String COST = "cost";
	public static final String TAKE_WITH_YOU = "take_with_you";
	public static final String GO_BY = "go_by";
	public static final String COUNTRY = "country";
	public static final String CITY = "city";
	public static final String STREET = "street";
	public static final String ZIP = "zip";
	public static final String ABSOLUTE_REMINDER_1 = "r1";
	public static final String ABSOLUTE_REMINDER_2 = "r2";
	public static final String ABSOLUTE_REMINDER_3 = "r3";
	public static final String ABSOLUTE_ALARM_1 = "a1";
	public static final String ALARM_1_FIRED = "alarm1_fired";
	public static final String ABSOLUTE_ALARM_2 = "a2";
	public static final String ALARM_2_FIRED = "alarm2_fired";
	public static final String ABSOLUTE_ALARM_3 = "a3";
	public static final String ALARM_3_FIRED = "alarm3_fired";
	public static final String TIMESTAMP_CREATED = "timestamp_created";
	public static final String TIMESTAMP_MODIFIED = "timestamp_modified";
	public static final String ATTENDANT_0_COUNT = "attendant_0_count";
	public static final String ATTENDANT_2_COUNT = "attendant_2_count";
	public static final String ATTENDANT_1_COUNT = "attendant_1_count";
	public static final String ATTENDANT_4_COUNT = "attendant_4_count";
	public static final String IS_SPORTS_EVENT = "is_sports_event";
	public static final String IS_ALL_DAY = "all_day";
	public static final String CREATOR_FULLNAME = "creator_fullname";
	public static final String CREATOR_CONTACT_ID = "author_contact_id";
	public static final String INVITED = "invited";
	public static final String MESSAGE_COUNT = "message_count";
	public static final String NEW_MESSAGE_COUNT = "nmc";
	public static final String MESSAGE_LAST_TIMESTAMP = "message_last_timestamp";
	// private static final String ASSIGNED_CONTACTS = "assigned_contacts";
	private static final String EVENTS = "events";
	public static final String POLL = "poll";

	/**
	 * @author justinas.marcinka@gmail.com Gets events projections from local
	 *         database, according to given date and time mode.
	 * @param projection
	 *            columns to get from events provider
	 * @param date
	 *            date on which events are selected according to eventTimeMode
	 * @param daysToSelect
	 *            selected time range end in days. Used only with timeMode
	 *            TM_EVENTS_FROM_GIVEN_DATE. If set 0, all events from given
	 *            date are selected
	 * @param eventTimeMode
	 *            available time modes:<br>
	 *            DataManagement.TM_EVENTS_FROM_GIVEN_DATE<br>
	 *            DataManagement.TM_EVENTS_ON_GIVEN_DAY <br>
	 *            DataManagement.TM_EVENTS_ON_GIVEN_MONTH<br>
	 *            DataManagement.TM_EVENTS_ON_GIVEN_YEAR<br>
	 * @param sortOrder
	 *            sort order. if null, default will be used
	 * @param context
	 * @return cursor holding projections that met selection criteria. Caller
	 *         has to set Event objects after return
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static Cursor createEventProjectionByDateFromLocalDb(Context context, String[] projection, Calendar date, int daysToSelect,
			int eventTimeMode, String sortOrder, boolean filterRejected) {
//		Calendar calendar = Calendar.getInstance();
//		Log.e("createEventProjectionByDateFromLocalDb", calendar.getTime().toString());
		initUserTimezone(context);
		day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT, Locale.getDefault());
		month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT, Locale.getDefault());
		String where;

		Uri uri;
		if (date != null) {
			switch (eventTimeMode) {
			case TM_EVENTS_FROM_GIVEN_DATE:

				if (daysToSelect > 0) {
					uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;
					Calendar tmpStart = (Calendar) date.clone();
					Calendar tmpEnd = (Calendar) date.clone();
					tmpEnd.add(Calendar.DATE, daysToSelect - 1);
					StringBuilder sb = new StringBuilder("(");
					sb.append('\'');
					sb.append(day_index_formatter.format(tmpStart.getTime()));
					sb.append('\'');
					tmpStart.add(Calendar.DATE, 1);
					while (!tmpStart.after(tmpEnd)) {
						sb.append(',');
						sb.append('\'');
						sb.append(day_index_formatter.format(tmpStart.getTime()));
						sb.append('\'');
						tmpStart.add(Calendar.DATE, 1);
					}
					sb.append(")");
					String inStringDay = sb.toString();
					return EventsProvider.mOpenHelper.getReadableDatabase().rawQuery("SELECT events.event_id, events._id, color, event_display_color, is_all_day, time_start_utc, " +
							"time_end_utc, icon, title, status, is_owner, day " +
							"FROM events_days " +
							"LEFT JOIN events ON (events_days.event_id = events.event_id) " +
							"WHERE `day` IN "+inStringDay+" ",
							null);
				} else {
					uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
					where = EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + ">" + date.getTimeInMillis();
				}

				break;
			case TM_EVENTS_ON_GIVEN_DAY:

				uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;
				where = EventsProvider.EMetaData.EventsIndexesMetaData.DAY + " = '" + day_index_formatter.format(date.getTime()) + "'";
				break;
			case TM_EVENTS_ON_GIVEN_MONTH:
				uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;
                where = EventsProvider.EMetaData.EventsIndexesMetaData.MONTH
                        + " = '" + month_index_formatter.format(date.getTime())
                        + "'";
				break;
			case TM_EVENTS_ON_GIVEN_YEAR:
				uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;

				Calendar tmp = (Calendar) date.clone();
				Utils.setCalendarToFirstDayOfYear(tmp);
				StringBuilder sb = new StringBuilder("(");
				sb.append('\'');
				sb.append(month_index_formatter.format(tmp.getTime()));
				sb.append('\'');
				tmp.add(Calendar.MONTH, 1);
				for (int i = 0; i < 11; i++) {
					sb.append(',');
					sb.append('\'');
					sb.append(month_index_formatter.format(tmp.getTime()));
					sb.append('\'');
					tmp.add(Calendar.MONTH, 1);
				}
				sb.append(")");
				String inString = sb.toString();

				where = EventsProvider.EMetaData.EventsIndexesMetaData.MONTH + " IN " + inString;
				break;

			default:
				throw new IllegalStateException("Wrong event Time mode for projection");
			}
		} else {
			where = null;
			uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		}

//		String rejectedFilter = " AND " + EventsProvider.EMetaData.EventsMetaData.STATUS + "!=" + Invited.REJECTED;
//
//		String pendingFilter = " AND (" + EventsProvider.EMetaData.EventsMetaData.STATUS + "!=" + Invited.PENDING + " OR ("
//				+ EventsProvider.EMetaData.EventsMetaData.STATUS + " == " + Invited.PENDING + " AND "
//				+ EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + " >= " + Calendar.getInstance().getTimeInMillis()
//				+ ")" + ")";
//
//		if (filterRejected) {
//			where += rejectedFilter;
//			where += pendingFilter;
//		}
		
		//for don't get pool
//		where += " AND " + (EventsProvider.EMetaData.EventsMetaData.TYPE + " != 'v'");
		
		if(eventTimeMode == TM_EVENTS_ON_GIVEN_MONTH){			
			return EventsProvider.mOpenHelper.getReadableDatabase().rawQuery("SELECT events.event_id, events._id, color, " +
					"event_display_color, is_all_day, time_start_utc, time_end_utc, icon, title, status, is_owner, day " +
					"FROM events_days LEFT JOIN events USING(event_id) WHERE month = '"+month_index_formatter.format(date.getTime())+"'",
					null);
		} else {
			return context.getContentResolver().query(uri, projection, where, null, sortOrder);
		}

	}

	/**
	 * Gets events from remote Database and writes them to local DB.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param eventCategory
	 *            API category. if empty, gets all events
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void getEventsFromRemoteDb(Context context, String eventCategory, long startTimeUnixTimestamp, long endTimeUnixTimestamp) {
		initUserTimezone(context);
		boolean success = false;
		Event event = null;
		ContentValues[] values;
		ContentValues[] values2;
		int length = 0;
		int pageNumber = 1;
		boolean hasMOreEvents = false;

		do{
			try {
				int value = 0;
				WebService webService = new WebService(context);
				HttpPost post = new HttpPost(Data.getServerUrl() + GET_EVENTS_FROM_REMOTE_DB_URL);
	
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	
				reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
				reqEntity.addPart(CATEGORY, new StringBody(eventCategory, Charset.forName("UTF-8")));
				if(startTimeUnixTimestamp > 0){
					reqEntity.addPart("start", new StringBody(startTimeUnixTimestamp + "", Charset.forName("UTF-8")));
				}
				if(endTimeUnixTimestamp > 0){
					reqEntity.addPart("end", new StringBody(endTimeUnixTimestamp + "", Charset.forName("UTF-8")));
				}
				try {
					reqEntity.addPart("page", new StringBody(pageNumber + "", Charset.forName("UTF-8")));
					pageNumber++;
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				
				try {
					reqEntity.addPart("size", new StringBody(eventsInOnePostRetrieveSize+"", Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
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
							hasMOreEvents = object.getBoolean("has_more");
							JSONArray es = object.getJSONArray(EVENTS);
							length = es.length();
							values = new ContentValues[length];						
							for (int i = 0; i < es.length(); i++) {
								try {
									JSONObject e = es.getJSONObject(i);
										event = JSONUtils.createEventFromJSON(context, e);
										if (event != null && !event.isNative()) {
											event.setUploadedToServer(true);
											if(event.getType().contentEquals("v") && !event.getPoll().contentEquals("null")){											
												context.getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, createCVforEventsTable(event));								
											}
											if(event.getType().contentEquals("v") || 
													event.getStatus() == Invited.REJECTED || 
													(event.getStatus() == Invited.PENDING && 
													event.getStartCalendar().before(Calendar.getInstance()) 
													)){
												
											} else {
//												insertEventToLocalDB(context, event);
												values[value] = createCVforEventsTable(event);
												value++;
											}
										}
									//}
	
								} catch (JSONException ex) {
									Log.e(CLASS_NAME, "JSON");
								}
							}
							if (values != null){
								values2 = new ContentValues[value];
								for (int i = 0; i < value; i++) {
									values2[i] = values[i];
								}
								context.getContentResolver().bulkInsert(EventsProvider.EMetaData.INDEXED_EVENTS_URI, values2);
							}
						}
					}
				}
			} catch (Exception ex) {
				Reporter.reportError(context, CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
			};
		} while(hasMOreEvents);
	}

	public static String getResponsesFromRemoteDb(Context context) {
		boolean success = false;
		String error = null;
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/latest_changes");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("getResponsesFromRemoteDb(contactIds)", "Failed adding token to entity");
		}
		
		if(!NavbarActivity.updateResponsesLastView){
			try {
				reqEntity.addPart("update_lastview", new StringBody(""+0, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				Log.e("getResponsesFromRemoteDb(contactIds)", "Failed adding token to entity");
			}
		} else {
			try {
				reqEntity.addPart("update_lastview", new StringBody(""+1, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				Log.e("getResponsesFromRemoteDb(contactIds)", "Failed adding token to entity");
			}
			NavbarActivity.updateResponsesLastView = false;
		}

		post.setEntity(reqEntity);
		String resp = "";
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");
					account.setResponsesBadge("" + object.getInt("count"));
					account.setResponses(resp);

					if (success == false) {
						error = object.getString("error");
						Log.e("getResponsesList - error: ", error);
					} else {
						JSONArray gs = object.getJSONArray("items");
						int count = gs.length();
						if (count > 0) {
							for (int i = 0; i < count; i++) {
								JSONObject g = gs.getJSONObject(i);
								list.add(g);
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			Log.e("getResponsesFromRemoteDb", "er");
		}
		return resp;
	}
	
	public static boolean votePoll(Context context, String event_id, ArrayList<JSONObject> selectedEventPolls, String status) {
		boolean success = false;
		String error = null;
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/polls/vote");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("votePoll", "Failed adding token to entity");
		}
		
		try {
			reqEntity.addPart("event_id", new StringBody(event_id, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("votePoll", "Failed adding token to entity");
		}
		
		int selectedEventPollsSize = selectedEventPolls.size();
		for(int i = 0; i < selectedEventPollsSize; i++){
			String selectedEventPollTimeId = "";
			try {
				selectedEventPollTimeId = selectedEventPolls.get(i).getString("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(!selectedEventPollTimeId.contentEquals("")){
				try {
					reqEntity.addPart("votes["+i+"][id]", new StringBody(selectedEventPollTimeId, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e1) {
					Log.e("votePoll", "Failed adding votes id to entity");
				}		
				
				try {
					reqEntity.addPart("votes["+i+"][status]", new StringBody(status, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e1) {
					Log.e("votePoll", "Failed adding votes status to entity");
				}	
			}
		}

		post.setEntity(reqEntity);
		String resp = "";
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("error");
						Log.e("votePoll - error: ", error);
					}
				}

			}
		} catch (Exception ex) {
			Log.e("votePoll", "er");
			success = false;
		}
		return success;
	}
	
	public static boolean rejectPoll(Context context, String event_id) {
		boolean success = false;
		String error = null;
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/polls/reject");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("rejectPoll", "Failed adding token to entity");
		}
		
		try {
			reqEntity.addPart("event_id", new StringBody(event_id, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("rejectPoll", "Failed adding token to entity");
		}

		post.setEntity(reqEntity);
		String resp = "";
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("error");
						Log.e("rejectPoll - error: ", error);
					}
				}

			}
		} catch (Exception ex) {
			Log.e("rejectPoll", "er");
		}
		return success;
	}
	
	public static boolean rejoinPoll(Context context, String event_id) {
		boolean success = false;
		String error = null;
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/polls/rejoin");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("rejoinPoll", "Failed adding token to entity");
		}
		
		try {
			reqEntity.addPart("event_id", new StringBody(event_id, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("rejoinPoll", "Failed adding token to entity");
		}

		post.setEntity(reqEntity);
		String resp = "";
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("error");
						Log.e("rejoinPoll - error: ", error);
					}
				}

			}
		} catch (Exception ex) {
			Log.e("rejoinPoll", "er");
		}
		return success;
	}

	// ///////////////////////////////////////////////////METHODS THAT WORK WITH
	// LOCAL DB//////////////////////////////////////////////////////

	/**
	 * Inserts given event to local DB. Rows that correspond to given ID are
	 * overwritten.
	 * 
	 * @param context
	 * @param event
	 * @since 2012-10-09
	 * @version 1.0
	 * @return inserted event internal id
	 */
	protected static long insertEventToLocalDB(Context context, Event event) {
		// 1. ADD EVENT details to events table
		ContentValues cv = createCVforEventsTable(event);
		Uri eventUri = context.getContentResolver().insert(EventsProvider.EMetaData.INDEXED_EVENTS_URI, cv);

		long internalID = 0;
		if(eventUri != null){
			internalID = ContentUris.parseId(eventUri);
		}
		return internalID;
		// if (internalID >= 0){
		// event.setInternalID(internalID);
		//
		// // 2. INSERT EVENT day indexes into events_days table
		// insertEventToDayIndexTable(context, event);
		// 3. INSERT EVENT INVITEs
		// insertEventToInvitesTable(context, event);
		// }
	}

	// private static void insertEventToInvitesTable(Context context, Event
	// event) {
	// long event_id = event.getInternalID();
	// ContentValues cv;
	// ContentResolver resolver = context.getContentResolver();
	// ArrayList<Invited> invites = event.getInvited();
	// for (Invited invite : invites){
	// cv = new ContentValues();
	// cv.put(EventsProvider.EMetaData.InvitedMetaData.EVENT_ID, event_id);
	// cv.put(EventsProvider.EMetaData.InvitedMetaData.GCID, invite.getGcid());
	// cv.put(EventsProvider.EMetaData.InvitedMetaData.GUID, invite.getGuid());
	// cv.put(EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID,
	// invite.getMy_contact_id());
	// cv.put(EventsProvider.EMetaData.InvitedMetaData.STATUS,
	// invite.getStatus());
	// cv.put(EventsProvider.EMetaData.InvitedMetaData.NAME, invite.getName());
	// resolver.insert(EventsProvider.EMetaData.InvitedMetaData.CONTENT_URI,
	// cv);
	// }
	// }

	/**
	 * Updates event in local DB.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param event
	 */

	public static void updateEventInLocalDb(Context context, Event event) {

		Uri uri;
		String where = null;
		ContentResolver resolver = context.getContentResolver();
		long internalID = event.getInternalID();

		// query for old this event times data to find out if days_index update
		// is needed
		boolean eventTimeChanged = false;
		String[] projection = { EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS,
				EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS };
		uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + internalID);

		Cursor result = resolver.query(uri, projection, where, null, null);
		long oldStart = 0;
		long oldEnd = 0;
		if (result.moveToFirst()) {
			oldStart = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
			oldEnd = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
			result.close();
		}

		ContentValues cv = createCVforEventsTable(event);
		cv.put(BaseColumns._ID, event.getInternalID()); // this is VERY
														// important
		// 1 update event in events table
		uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + internalID);

		resolver.update(uri, cv, null, null);

		if (event.getStartCalendar() != null && event.getEndCalendar() != null) { // to
																					// prevent
																					// null
																					// pointer
																					// exception:
																					// that
																					// hurts
																					// if
																					// happens
																					// :)

			eventTimeChanged = oldStart != event.getStartCalendar().getTimeInMillis() || oldEnd != event.getEndCalendar().getTimeInMillis();

			// 3 Renew event data in time indexes

			if (eventTimeChanged) {
				where = EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID + "=" + internalID;
				resolver.delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, where, null);
				if(event.getType().contentEquals("v") || 
						event.getStatus() == Invited.REJECTED || 
						(event.getStatus() == Invited.PENDING && 
						event.getStartCalendar().before(Calendar.getInstance()))){
					Log.e("Not inserted", event.getTitle() + " id: " + event.getEvent_id());
				} else {
					Log.i("Inserted", event.getTitle() + " id: " + event.getEvent_id());
					insertEventToDayIndexTable(context, event);
				}
			}
		}

	}

	/**
	 * deletes event from local DB
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param internalID
	 *            event local DB id
	 * @since 2012-10-09
	 * @version 1.0
	 */
	private static void deleteEventFromLocalDb(Context context, long internalID, long externalID) {
		String where;

		// 1. Deleting event from events table
		where = BaseColumns._ID + "=" + internalID;
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, where, null);

		// 2. Deleting event from events day indexes table
		where = EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_EXTERNAL_ID + "=" + externalID;
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, where, null);

	}

	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param event
	 * @since 2012-10-09
	 * @version 1.0
	 */
	private static void insertEventToDayIndexTable(Context context, Event event) {
		Calendar eventDayStart = (Calendar) event.getStartCalendar().clone();
		eventDayStart.set(Calendar.HOUR_OF_DAY, 0);
		eventDayStart.set(Calendar.MINUTE, 0);
		eventDayStart.set(Calendar.SECOND, 0);
		eventDayStart.set(Calendar.MILLISECOND, 0);

		long event_internal_id = event.getInternalID();
		long event_external_id = event.getEvent_id();
		String ext_id = null;
		if (event_external_id > 0)
			ext_id = "" + event_external_id;

//		if (event.is_all_day()) { // only one row is inserted
//			insertEventDayIndexRow(context, event_internal_id, ext_id, eventDayStart);
//		} else
			while (eventDayStart.before(event.getEndCalendar())) { // rows are
																	// inserted
																	// for each
																	// day that
																	// event
																	// lasts
				insertEventDayIndexRow(context, event_internal_id, ext_id, eventDayStart);
				eventDayStart.add(Calendar.DATE, 1);

			}
	}

	// TODO javadoc
	private static void insertEventDayIndexRow(Context context, long event_id, String event_external_id, Calendar eventDayStart) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID, event_id);
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_EXTERNAL_ID, event_external_id);
		Date time = eventDayStart.getTime();

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY, day_index_formatter.format(time));

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH, month_index_formatter.format(time));
		context.getContentResolver().insert(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, cv);

	}

	public static final int ID_INTERNAL = 0;
	public static final int ID_EXTERNAL = 1;

	/**
	 * @author justinas.marcinka@gmail.com Gets event object with full info
	 *         about event from local db by internal or external evetn ID
	 * @param context
	 * @param ID
	 * @param id_mode
	 *            Indicates whether given param is internal or external id.
	 *            Available id modes:<br>
	 *            EventManagement.ID_INTERNAL<br>
	 *            EventManagement.ID_EXTERNAL<br>
	 * @return
	 */
	public static Event getEventFromLocalDb(Context context, long ID, int id_mode) {
		Event item = null;
		Uri uri;

		switch (id_mode) {
		case (ID_INTERNAL):
			uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
			break;
		case (ID_EXTERNAL):
			uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI_EXTERNAL_ID;
			break;
		default:
			throw new IllegalStateException("method getEventFromLocalDB: Unknown id mode");
		}

		uri = Uri.parse(uri + "/" + ID);
		Cursor result = context.getContentResolver().query(uri, null, null, null, null);
		if (result.moveToFirst()) {
			item = createEventFromCursor(context, result);
		}
		result.close();
		return item;
	}

	// TODO javadoc
	// nauja update event_id metoda su dviem parametrais: internal_id,
	// external_id, kas suzinoti, kokiam..
	// reiketu papildomos lenteles eventu duomenu bazei saugoti deleted_events

	private static void updateEventStatusInLocalDb(Context context, Event event) {
		if (event.getMyInvite() != null) {
			event.getMyInvite().setStatus(event.getStatus());
		}
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.getStatus());
		cv.put(EventsProvider.EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR, event.getDisplayColor());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS, Calendar.getInstance().getTimeInMillis()); // veliau
		cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, parseInvitedListToJSONArray(event.getInvited()));
		Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		String where = EventsProvider.EMetaData.EventsMetaData._ID + "=" + event.getInternalID();
		context.getContentResolver().update(uri, cv, where, null);
	}
	
	public static void updateEventSelectedPollsTimeInLocalDb(Context context, Event event) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME, event.getSelectedEventPollsTime());
		Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		String where = EventsProvider.EMetaData.EventsMetaData._ID + "=" + event.getInternalID();
		context.getContentResolver().update(uri, cv, where, null);
	}

	public static void resetEventsNewMessageCount(Context context, int eventId) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsMetaData.NEW_MESSAGES_COUNT, "0");
		Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		String where = EventsProvider.EMetaData.EventsMetaData.E_ID + "=" + eventId;
		context.getContentResolver().update(uri, cv, where, null);
	}

	// private static ArrayList<Invited> getInvitesForEvent(Context context,
	// Event event) {
	// ArrayList<Invited> invites = new ArrayList<Invited>();
	// long id = event.getInternalID();
	//
	// String where = EventsProvider.EMetaData.InvitedMetaData.EVENT_ID + "=" +
	// id;
	// Uri uri = EventsProvider.EMetaData.InvitedMetaData.CONTENT_URI;
	// Cursor result = context.getContentResolver().query(uri, null, where,
	// null, null);
	// if (result.moveToFirst()){
	// invites.add(createInvitedFromCursor(result));
	//
	// }
	// result.close();
	// return invites;
	// }

	// ///////////////////////////////////////////////////METHODS THAT WORK WITH
	// RMOTE DB//////////////////////////////////////////////////////

	private static boolean updateEventStatusInServer(Context context, Event event) {
		if (event.getEvent_id() == 0)
			return false;
		boolean success = false;

		try {
			Account account = new Account(context);
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/set_event_status");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart(EVENT_ID, new StringBody("" + event.getEvent_id(), Charset.forName("UTF-8")));
			reqEntity.addPart(STATUS, new StringBody("" + event.getStatus(), Charset.forName("UTF-8")));
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean(SUCCESS);
						if (!success) {
							Log.e("Response to event error", object.getJSONObject("error").getString(REASON));
							return false;
						} else {
							return true;
						}

					}
				} else {
					Log.e("createEvent - status", rp.getStatusLine().getStatusCode() + "");
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), ex.getMessage());
		}

		return false;
	}

	/**
	 * Creates event in remote DB and returns event ID if success.
	 * 
	 * @param e
	 *            - event to create
	 * @return event id. If create failed, returns 0.
	 */
	public static int createEventInRemoteDb(Context context, Event e) {
		boolean success = false;

		try {
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_create");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));

			if (e.getIcon().length() > 0)
				reqEntity.addPart("icon", new StringBody(e.getIcon(), Charset.forName("UTF-8")));

			reqEntity.addPart("color", new StringBody(e.getColor(), Charset.forName("UTF-8")));

			reqEntity.addPart("title", new StringBody(e.getTitle(), Charset.forName("UTF-8")));

			reqEntity.addPart("timestamp_start_utc",
					new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis()), Charset.forName("UTF-8")));
			reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis()),
					Charset.forName("UTF-8")));

			reqEntity.addPart("all_day_event", new StringBody(e.is_all_day() ? "1" : "0", Charset.forName("UTF-8")));

			reqEntity.addPart("description", new StringBody(e.getDescription(), Charset.forName("UTF-8")));

			if (e.getCountry().length() > 0)
				reqEntity.addPart("country", new StringBody(e.getCountry(), Charset.forName("UTF-8")));
			if (e.getCity().length() > 0)
				reqEntity.addPart("city", new StringBody(e.getCity(), Charset.forName("UTF-8")));
			if (e.getStreet().length() > 0)
				reqEntity.addPart("street", new StringBody(e.getStreet(), Charset.forName("UTF-8")));
			if (e.getZip().length() > 0)
				reqEntity.addPart("zip", new StringBody(e.getZip(), Charset.forName("UTF-8")));
			reqEntity.addPart(TIMEZONE, new StringBody(e.getTimezone(), Charset.forName("UTF-8")));

			if (e.getLocation().length() > 0)
				reqEntity.addPart("location", new StringBody(e.getLocation(), Charset.forName("UTF-8")));
			if (e.getGo_by().length() > 0)
				reqEntity.addPart("go_by", new StringBody(e.getGo_by(), Charset.forName("UTF-8")));
			if (e.getTake_with_you().length() > 0)
				reqEntity.addPart("take_with_you", new StringBody(e.getTake_with_you(), Charset.forName("UTF-8")));
			if (e.getCost().length() > 0)
				reqEntity.addPart("cost", new StringBody(e.getCost(), Charset.forName("UTF-8")));
			if (e.getAccomodation().length() > 0)
				reqEntity.addPart("accomodation", new StringBody(e.getAccomodation(), Charset.forName("UTF-8")));
			if (e.getAssigned_contacts() != null) {
				for (int i = 0, l = e.getAssigned_contacts().length; i < l; i++) {
					reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.getAssigned_contacts()[i]), Charset.forName("UTF-8")));
				}
			} else {
				reqEntity.addPart("contacts[]", new StringBody("", Charset.forName("UTF-8")));
			}
			// if (e.assigned_groups != null) {
			// for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
			// reqEntity.addPart("groups[]", new
			// StringBody(String.valueOf(e.assigned_groups[i])));
			// }
			// } else {
			// reqEntity.addPart("groups[]", new StringBody(""));
			// }

			if (e.getAlarm1() != null) {
				reqEntity.addPart("a1",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm1().getTimeInMillis()), Charset.forName("UTF-8")));
			}
			if (e.getAlarm2() != null) {
				reqEntity.addPart("a2",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm2().getTimeInMillis()), Charset.forName("UTF-8")));
			}
			if (e.getAlarm3() != null) {
				reqEntity.addPart("a3",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm3().getTimeInMillis()), Charset.forName("UTF-8")));
			}

			if (e.getReminder1() != null) {
				reqEntity.addPart("r1",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder1().getTimeInMillis()), Charset.forName("UTF-8")));
			}
			if (e.getReminder2() != null) {
				reqEntity.addPart("r2",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder2().getTimeInMillis()), Charset.forName("UTF-8")));
			}
			if (e.getReminder3() != null) {
				reqEntity.addPart("r3",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder3().getTimeInMillis()), Charset.forName("UTF-8")));
			}

			if (e.isBirthday()) {
				reqEntity.addPart("bd", new StringBody("1", Charset.forName("UTF-8")));
			}
			Account account = new Account(context);
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						// Log.e("createEvent - resp", resp + "!!!");
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						// Log.e("createEvent - success", "" + success);

						if (!success) {
							Log.e("Create event error", object.getJSONObject("error").getString("reason"));
							return 0;
						} else {
							return object.optInt("event_id");
						}
					}
				} else {
					Log.e("createEvent - status", rp.getStatusLine().getStatusCode() + "");
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
					.toString(), ex.getMessage());
		}
		return 0;

	}

	private static boolean editEvent(Context context, Event e) {
		boolean success = false;
		if(e.getEvent_id() > 0){
			try {
				WebService webService = new WebService(context);
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_edit");
	
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	
				reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(e.getEvent_id()), Charset.forName("UTF-8")));
	
				reqEntity.addPart("event_type", new StringBody(e.getType(), Charset.forName("UTF-8")));
	
				reqEntity.addPart("icon", new StringBody(e.getIcon(), Charset.forName("UTF-8")));
				reqEntity.addPart("color", new StringBody(e.getColor(), Charset.forName("UTF-8")));
	
				reqEntity.addPart("title", new StringBody(e.getTitle(), Charset.forName("UTF-8")));
	
				reqEntity.addPart("timestamp_start_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis()), Charset.forName("UTF-8")));
				reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis()),
						Charset.forName("UTF-8")));
	
				reqEntity.addPart("timezone", new StringBody(e.getTimezone(), Charset.forName("UTF-8")));
	
				reqEntity.addPart("all_day_event", new StringBody(e.is_all_day() ? "1" : "0", Charset.forName("UTF-8")));
	
				reqEntity.addPart("description", new StringBody(e.getDescription(), Charset.forName("UTF-8")));
	
				reqEntity.addPart("country", new StringBody(e.getCountry(), Charset.forName("UTF-8")));
				reqEntity.addPart("zip", new StringBody(e.getZip(), Charset.forName("UTF-8")));
				reqEntity.addPart("city", new StringBody(e.getCity(), Charset.forName("UTF-8")));
				reqEntity.addPart("street", new StringBody(e.getStreet(), Charset.forName("UTF-8")));
				reqEntity.addPart("location", new StringBody(e.getLocation(), Charset.forName("UTF-8")));
	
				reqEntity.addPart("go_by", new StringBody(e.getGo_by(), Charset.forName("UTF-8")));
				reqEntity.addPart("take_with_you", new StringBody(e.getTake_with_you(), Charset.forName("UTF-8")));
				reqEntity.addPart("cost", new StringBody(e.getCost(), Charset.forName("UTF-8")));
				reqEntity.addPart("accomodation", new StringBody(e.getAccomodation(), Charset.forName("UTF-8")));
	
				if (e.getAlarm1() != null) {
					reqEntity.addPart("a1",
							new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm1().getTimeInMillis()), Charset.forName("UTF-8")));
				}
				if (e.getAlarm2() != null) {
					reqEntity.addPart("a2",
							new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm2().getTimeInMillis()), Charset.forName("UTF-8")));
				}
				if (e.getAlarm3() != null) {
					reqEntity.addPart("a3",
							new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm3().getTimeInMillis()), Charset.forName("UTF-8")));
				}
	
				if (e.getReminder1() != null) {
					reqEntity.addPart("r1",
							new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder1().getTimeInMillis()), Charset.forName("UTF-8")));
				}
				if (e.getReminder2() != null) {
					reqEntity.addPart("r2",
							new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder2().getTimeInMillis()), Charset.forName("UTF-8")));
				}
				if (e.getReminder3() != null) {
					reqEntity.addPart("r3",
							new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder3().getTimeInMillis()), Charset.forName("UTF-8")));
				}
	
				// if (Data.selectedContacts != null &&
				// !Data.selectedContacts.isEmpty()) {
				// e.assigned_contacts = new int[Data.selectedContacts.size()];
				// int i = 0;
				// for (Contact contact : Data.selectedContacts) {
				// e.assigned_contacts[i] = contact.contact_id;
				// i++;
				// }
				// }
				if (e.getAssigned_contacts() != null) {
					for (int i = 0, l = e.getAssigned_contacts().length; i < l; i++) {
						reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.getAssigned_contacts()[i]), Charset.forName("UTF-8")));
					}
				} else {
					reqEntity.addPart("contacts[]", new StringBody("", Charset.forName("UTF-8")));
				}
	
				Account account = new Account(context);
				// if (e.assigned_groups != null) {
				// for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
				// reqEntity.addPart("groups[]", new
				// StringBody(String.valueOf(e.assigned_groups[i])),
				// Charset.forName("UTF-8"));
				// }
				// } else {
				// reqEntity.addPart("groups[]", new StringBody("",
				// Charset.forName("UTF-8")));
				// }
				reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
				post.setEntity(reqEntity);
	
				if (DataManagement.networkAvailable) {
					HttpResponse rp = webService.getResponseFromHttpPost(post);
	
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
				}
			} catch (Exception ex) {
				Reporter.reportError(context, CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
				success = false;
			}
		}
		return success;
	}

	public static boolean removeEvent(Context context, int id) {
		boolean success = false;

		try {
			Account account = new Account(context);
			WebService webService = new WebService(context);
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_remove");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("event_id", new StringBody(String.valueOf(id), Charset.forName("UTF-8")));
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			post.setEntity(reqEntity);

			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

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
							EventManagement.error = (errObj.getString("reason"));
							Log.e("removeEvent - error: ", Data.getERROR());
						} else {
							// Data.getEvents().remove(getEventFromLocalDb(id));
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(context, CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		}
		return success;
	}

	// //////////////////////////////////////////////////////////////////DB
	// UTILITY
	// METHODS////////////////////////////////////////////////////////////////////
	// TODO javadoc
	/**
	 * Fills ContentValues instance with event data IMPORTANT: _ID field must be
	 * set MANUALLY. this method does not take care of that
	 * 
	 * @param event
	 * @return CV for event.
	 */
	protected static ContentValues createCVforEventsTable(Event event) {
		ContentValues cv = new ContentValues();
		if(event.getEvent_id() != 0){
			cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.getEvent_id());
		} else {
			event.setEvent_id((int)Calendar.getInstance().getTimeInMillis());
			cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.getEvent_id());
		}

		cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, event.getUser_id());
		cv.put(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY, event.isUploadedToServer() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.getStatus());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID, event.getCreator_contact_id());

		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT, event.getAttendant_1_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT, event.getAttendant_2_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT, event.getAttendant_0_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT, event.getAttendant_4_count());

		// native events are not held in GA local db so we do not put
		// Event.isNative
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT, event.is_sports_event() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, event.is_owner() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY, event.is_all_day() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_BIRTHDAY, event.isBirthday() ? 1 : 0);

		cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, event.getType());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME, event.getCreator_fullname());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, event.getTitle());
//		System.out.println(event.getTitle());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.getIcon());
		cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.getColor());
		// cv.put(EventsProvider.EMetaData.EventsMetaData.TEXT_COLOR,
		// event.getTextColor());//2012-10-24
		cv.put(EventsProvider.EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR, event.getDisplayColor());// 2012-10-24
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
		if (event.getStartCalendar() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, event.getStartCalendar().getTimeInMillis());
		if (event.getEndCalendar() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS, event.getEndCalendar().getTimeInMillis());

		// reminders
		if (event.getReminder1() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.getReminder1().getTimeInMillis());
		if (event.getReminder2() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.getReminder2().getTimeInMillis());
		if (event.getReminder3() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.getReminder3().getTimeInMillis());

		// TODO alarms DO SOMETHING WITH ALARM FIRED FIELDS
		if (event.getAlarm1() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM1, event.getAlarm1().getTimeInMillis());
		if (event.getAlarm2() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM2, event.getAlarm2().getTimeInMillis());
		if (event.getAlarm3() != null)
			cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM3, event.getAlarm3().getTimeInMillis());

		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS, event.getCreatedUtc());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS, event.getModifiedMillisUtc());
		cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, parseInvitedListToJSONArray(event.getInvited()));
		// TODO???
		// cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS,
		// event.getAssigned_contacts_DB_entry());
		// cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS,
		// event.getAssigned_groups_DB_entry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MESSAGES_COUNT, event.getMessage_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.NEW_MESSAGES_COUNT, event.getNew_message_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS, event.getLast_message_date_time());
		cv.put(EventsProvider.EMetaData.EventsMetaData.POLL, event.getPoll());
		cv.put(EventsProvider.EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME, event.getSelectedEventPollsTime());
		return cv;
	}

	private static String parseInvitedListToJSONArray(ArrayList<Invited> invited) {
		if (invited.isEmpty())
			return "[]";
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (Invited invite : invited) {
			sb.append('{');
			sb.append(invite.toString());
			sb.append('}');
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(']');
		return sb.toString();
	}

	// TODO javadoc
	protected static Event createEventFromCursor(Context context, Cursor result) {
		initUserTimezone(context);
		Event item = new Event();
		long timeinMillis;
		item.setInternalID(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData._ID)));
		item.setEvent_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID)));
		item.setUser_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.USER_ID)));
		item.setUploadedToServer(1 == result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY)));
		item.setStatus(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS)));
		item.setCreator_contact_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID)));

		item.setAttendant_1_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT)));
		item.setAttendant_2_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT)));
		item.setAttendant_0_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT)));
		item.setAttendant_4_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT)));

		item.setSports_event(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT)) == 1);
		final int is_owner = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_OWNER));
		item.setIs_owner(is_owner == 1);

		item.setIs_all_day(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY)) == 1);
		item.setBirthday(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_BIRTHDAY)) == 1);
		item.setNative(false); // native events are not stored in local DB, so
								// they cant be restored also

		item.setType(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TYPE)));
		item.setCreator_fullname(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME)));
		item.setTitle(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TITLE)));
		item.setIcon(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ICON)));
		item.setColor(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COLOR)));
		item.setDisplayColor(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR)));
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

		timeinMillis = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
		item.setStartCalendar(Utils.createCalendar(timeinMillis, user_timezone));
		timeinMillis = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
		item.setEndCalendar(Utils.createCalendar(timeinMillis, user_timezone));

		item.setReminder1(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER1)),
				user_timezone));
		item.setReminder2(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER2)),
				user_timezone));
		item.setReminder3(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER3)),
				user_timezone));

		item.setAlarm1(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ALARM1)),
				user_timezone));
		item.setAlarm2(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ALARM2)),
				user_timezone));
		item.setAlarm3(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ALARM3)),
				user_timezone));

		item.setCreatedMillisUtc(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS)));
		item.setModifiedMillisUtc(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS)));
		try {
			ArrayList<Invited> invites = new ArrayList<Invited>();

			item.setMyInvite(JSONUtils.createInvitedListFromJSONArrayString(context,
					result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.INVITED)), invites));
			item.setInvited(invites);
		} catch (JSONException e) {
			Log.e("Error parsing invited array from local db",
					"Event ID: " + item.getEvent_id() + " event local ID: " + item.getInternalID());
		}

		// item.setAssigned_contacts_DB_entry(result.getString(result
		// .getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS)));
		// item.setAssigned_groups_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS)));

		item.setMessage_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MESSAGES_COUNT)));
		item.setNew_message_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.NEW_MESSAGES_COUNT)));
		item.setLast_message_date_time(result.getLong(result
				.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS)));
		item.setPoll(result.getString(result
				.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.POLL)));
		item.setSelectedEventPollsTime(result.getString(result
				.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME)));
		return item;
	}

	// TODO document
	public static ArrayList<Event> getEventsFromLocalDb(Context context, boolean filterActual) {
		Event item;
		String where = null;

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		if (filterActual)
			where = EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + " >= " + today.getTimeInMillis();

		ArrayList<Event> items = new ArrayList<Event>();

		Cursor result = context.getContentResolver().query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);

		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				item = EventManagement.createEventFromCursor(context, result);
				items.add(item);
				result.moveToNext();
			}
		}
		result.close();

		return (items);
	}

	/**
	 * Loads all actual events from local db to given adapter.
	 * 
	 * @param instance
	 * @param eAdapter
	 * @return
	 */
	public static int loadEvents(Context context, EventsAdapter eAdapter) {
		int eventsSize = 0;
		ArrayList<Event> events = new ArrayList<Event>();
		Account account = new Account(context);
		if (account.getShow_ga_calendars()) {
			events = EventManagement.getEventsFromLocalDb(context, true);
		}
		if (account.getShow_native_calendars()) {
			events.addAll(NativeCalendarReader.readAllCalendar(context));
		}
		if (eAdapter != null) {
			eAdapter.setItems(events);
			eAdapter.notifyDataSetChanged();
		}
		return eventsSize;
	}

	public static CharSequence getError() {
		return error;
	}

	protected static void bulkDeleteEvents(Context context, String IDs, int id_mode) {
		String where;
		switch (id_mode) {
		case (ID_INTERNAL):
			where = EventsProvider.EMetaData.EventsMetaData._ID;
			break;
		case (ID_EXTERNAL):
			where = EventsProvider.EMetaData.EventsMetaData.E_ID;
			break;
		default:
			throw new IllegalStateException("method getEventFromLocalDB: Unknown id mode");
		}
		StringBuilder sb = new StringBuilder(where);
		sb.append(" IN (");
		sb.append(IDs);
		sb.append(')');
		where = sb.toString();
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, where, null);
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, where, null);
	}

	protected static void syncEvents(Context context, ArrayList<Event> eventChanges, long[] deletedEventsIDs) {
		StringBuilder sb;
		initUserTimezone(context);
		if (!eventChanges.isEmpty()) {
//			sb = new StringBuilder();
			for (Event e : eventChanges) {
				if(!e.getType().contentEquals("v")){
					if(getEventFromLocalDb(context, e.getEvent_id(), ID_EXTERNAL) == null){
						insertEventToLocalDB(context, e);
	//					sb.append(e.getEvent_id());
	//					sb.append(',');
					}else{
						e.setInternalID(getEventFromLocalDb(context, e.getEvent_id(), ID_EXTERNAL).getInternalID());
						updateEventInLocalDb(context, e);
					}
				} 
				else {
					if(getEventFromLocalDb(context, e.getEvent_id(), ID_EXTERNAL) == null){
						context.getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, createCVforEventsTable(e));
						EventEditActivity.addEventToPollList(context, e);
					} else {
						e.setInternalID(getEventFromLocalDb(context, e.getEvent_id(), ID_EXTERNAL).getInternalID());
						updateEventInLocalDb(context, e);
						EventEditActivity.deleteEventFromPollList(e);
						EventEditActivity.addSelectedEventToPollList(context, e, EventEditActivity.getSelectedEventPollTimes(e));
					}
				}
			}
//			sb.deleteCharAt(sb.length() - 1);
//			EventManagement.bulkDeleteEvents(context, sb.toString(), EventManagement.ID_EXTERNAL);
//			for (Event e : eventChanges) {
//			}
		}

		if (deletedEventsIDs.length > 0) {
			sb = new StringBuilder();
			for (int i = 0; i < deletedEventsIDs.length; i++) {
				sb.append(deletedEventsIDs[i]);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			EventManagement.bulkDeleteEvents(context, sb.toString(), EventManagement.ID_EXTERNAL);

			// TODO cia reikes realizuoti pazymetu eventu (kurios reikia sukurti
			// RDB)

		}
	}

	/**
	 * Method which makes sure that user_timezone static field would be
	 * initialized. Must be called in any public method.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 */
	private static void initUserTimezone(Context context) {
		if (user_timezone == null)
			user_timezone = new Account(context).getTimezone();
	}

	// private static Invited createInvitedFromCursor(Cursor result) {
	// Invited invited = new Invited();
	// invited.setGcid(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.GCID)));
	// invited.setGuid(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.GUID)));
	// invited.setMy_contact_id(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID)));
	// invited.setName(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.NAME)));
	// invited.setStatus(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.STATUS)));
	// return invited;
	// }

	// public static ArrayList<ChatMessageObject> getEventCreatedOffline(Context
	// context) {
	// ArrayList<EventObject> offlineCreatedEvents = new
	// ArrayList<EventObject>();
	// Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
	// String projection[] = null;
	// Account account = new Account(context);
	// String selection =
	// (EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS +">"+
	// account.getLastTimeConnectedToWeb());
	// Cursor cur = context.getContentResolver().query(uri, projection,
	// selection, null, null);
	// if(cur.moveToFirst()){
	// do{
	// offlineCreatedEvents.add(makeChatMessageObjectFromCursor(cur));
	// cur.moveToNext();
	// } while(!cur.isAfterLast());
	// }
	// cur.close();
	// return offlineCreatedEvents;
	// }

	public static void uploadOfflineEvents(Context context) {
		String projection[] = null;
		Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		String where = EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY + " = '0'";
		Cursor result = context.getContentResolver().query(uri, projection, where, null, null);
		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				Event e = createEventFromCursor(context, result);

//				if(e.getType().contentEquals("v")){
//					Log.d("event", "id "+e.getEvent_id());
//					ArrayList<JSONObject> allEventPolls = EventEditActivity.getAllEventPollTimes(e);
//					ArrayList<JSONObject> selectedPollTime = EventEditActivity.getSelectedEventPollTimes(e);
//					if (DataManagement.networkAvailable) {
//						e.setUploadedToServer(votePoll(context, "" + e.getEvent_id(), allEventPolls, "0"));
//						e.setUploadedToServer(votePoll(context, "" + e.getEvent_id(), selectedPollTime, "1"));
//					} else {
//						e.setUploadedToServer(false);
//					}
//					updateEventInLocalDb(context, e);
//					result.moveToNext();
//				} else{
					if (!editEvent(context, e)) {
						int externalId = createEventInRemoteDb(context, e);
						ContentValues values = new ContentValues();
						values.put(EventsProvider.EMetaData.EventsMetaData.E_ID, externalId);
						where = EventsProvider.EMetaData.EventsMetaData._ID + "=" + e.getInternalID();
						context.getContentResolver().update(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, values, where, null);
						result.moveToNext();
					} else {
						result.moveToNext();
					}
//				}
			}
		}
		SaveDeletedData offlineDeletedEvents = new SaveDeletedData(context);
		String offlineDeleted = offlineDeletedEvents.getDELETED_EVENTS();
		String[] ids = offlineDeleted.split(SDMetaData.SEPARATOR);
		if (ids[0] != "") {
			for (int i = 0; i < ids.length; i++) {
				int id = Integer.parseInt(ids[i]);
				removeEvent(context, id);
			}

		}
		offlineDeletedEvents.clear(3);
		result.close();
	}
	
	public static ArrayList<Event> getPollEventsFromLocalDb(Context context) {
		Event item;
		String where = null;
		
		where = (EventsProvider.EMetaData.EventsMetaData.TYPE + " = 'v'" +" AND "+ EventsProvider.EMetaData.EventsMetaData.STATUS + " != '0'");

		ArrayList<Event> items = new ArrayList<Event>();

		Cursor result = context.getContentResolver().query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);

		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				item = EventManagement.createEventFromCursor(context, result);
				String jsonArraySelectedTime = item.getSelectedEventPollsTime();
				try {
					if((jsonArraySelectedTime != null && !jsonArraySelectedTime.contentEquals("null")) && (jsonArraySelectedTime != null && jsonArraySelectedTime.length() > 2)){
						final JSONArray jsonArray= new JSONArray(jsonArraySelectedTime);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject pollThread = jsonArray.getJSONObject(i);
							item = EventManagement.createEventFromCursor(context, result);
							item.setStartCalendar(Utils.stringToCalendar(context, pollThread.getString("start"), DataManagement.SERVER_TIMESTAMP_FORMAT));
							item.setEndCalendar(Utils.stringToCalendar(context, pollThread.getString("end"), DataManagement.SERVER_TIMESTAMP_FORMAT));
							items.add(item);
						}
					} else {
						String jsonArrayString = item.getPoll();
						if(jsonArrayString != null && !jsonArrayString.contentEquals("null")){
							JSONArray jsonArray= new JSONArray(jsonArrayString);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject e = jsonArray.getJSONObject(i);
								//if(e.getString("response").contentEquals("1")){
									item = EventManagement.createEventFromCursor(context, result);
									item.setStartCalendar(Utils.stringToCalendar(context, e.getString("start"), DataManagement.SERVER_TIMESTAMP_FORMAT));
									item.setEndCalendar(Utils.stringToCalendar(context, e.getString("end"), DataManagement.SERVER_TIMESTAMP_FORMAT));
									items.add(item);
								//}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
//				String jsonArrayString = item.getPoll();
//				try {
//					if(jsonArrayString != null && !jsonArrayString.contentEquals("null")){
//						JSONArray jsonArray= new JSONArray(jsonArrayString);
//						for (int i = 0; i < jsonArray.length(); i++) {
//							JSONObject e = jsonArray.getJSONObject(i);
//							//if(e.getString("response").contentEquals("1")){
//								item = EventManagement.createEventFromCursor(context, result);
//								item.setStartCalendar(Utils.stringToCalendar(context, e.getString("start"), DataManagement.SERVER_TIMESTAMP_FORMAT));
//								item.setEndCalendar(Utils.stringToCalendar(context, e.getString("end"), DataManagement.SERVER_TIMESTAMP_FORMAT));
//								items.add(item);
//							//}
//						}
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
				
				result.moveToNext();
			}
		}
		result.close();

		return (items);
	}
}