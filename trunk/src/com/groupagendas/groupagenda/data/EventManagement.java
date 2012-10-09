package com.groupagendas.groupagenda.data;
/**
 * Class is responsible for communicating with remote and local databases. 
 * @author justinas.marcinka@gmail.com
 * @version 1.0
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsAdapter;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;
import com.groupagendas.groupagenda.utils.Utils;


public class EventManagement {
	static String error = "";
	private static final String CLASS_NAME = "EventManagement.class";
	private static SimpleDateFormat day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
	private static SimpleDateFormat month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
	private static String user_timezone = CalendarSettings.getTimeZone();
	
	
	
	private static final String GET_EVENTS_FROM_REMOTE_DB_URL = "mobile/events_list";
	
	

	
	
	public static boolean networkAvailable = true; //TODO remove hardcode
	
	////////////////////////////METHODS THAT ARE USED BY UI////////////////////////////////////////////////////////////////////
	
	/**
	 * @author justinas.marcinka@gmail.com Method creates event in both remote
	 *         and local databases. NOT YET IMPLEMENTED: If there is no connectivity to remote DB,
	 *         event should be created only in local DB and saved task to upload
	 *         data when available.
	 * @param event
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void createNewEvent(Context context, Event event) {

		if (networkAvailable) {
			int id = createEventInRemoteDb(event);

			if (id > 0) {
				event.setEvent_id(id);
				event.setUploadedToServer(true);
			} else {
				// TODO report error
			}
		} else {
			event.setUploadedToServer(false);
		}	
		
		insertEventToLocalDB(context, event);

	}

	/**
	 * @author justinas.marcinka@gmail.com Method deletes event from both remote
	 *         and local databases. NOT YET IMPLEMENTED: If there is no connectivity to remote DB,
	 *         event should be deleted only in local DB and saved task to delete
	 *         data from remote db when available.
	 * @param event
	 * Fields that MUST BE SET:<br>
	 *  Event.internalID <br>
	 *  Event.event_ID <br>
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void deleteEvent(Context context, Event event) {
		Boolean deletedFromRemote = false;
		if (networkAvailable) {
			 deletedFromRemote = removeEvent(event.getEvent_id());
		}

		if (!deletedFromRemote){
			//TODO add delete event task to tasks list for server remote
		}	
		
		deleteEventFromLocalDb(context, event.getInternalID());	
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context - method call context
	 * @param event - event which invitation should be responded to. <br>
	 * Fields that MUST BE SET:<br>
	 *  Event.internalID <br>
	 *  Event.event_ID <br>
	 *  Event.status<br>
	 * @since 2012-10-09
	 * @version 1.0
	 */
	
	public static void respondToInvitation(Context context, Event event){
		if (!Invited.validateResponse(event.getStatus())){
			Log.e(CLASS_NAME + " ERROR RESPONDING TO INVITE", "Unknown state: " + event.getStatus());
			return;
		}
		if (networkAvailable) {
			event.setUploadedToServer(updateEventStatusInServer(event));
		} else {
			event.setUploadedToServer(false);
		}	
		updateEventStatusInLocalDb(context, event); //TODO OFFLINE MODE
		
	}
	
	/**
	 * returns all existing chat threads
	 * @author justinas.marcinka@gmail.com
	 * @author interfectos@gmail.com
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static ArrayList<Event> getExistingChatThreads (Context context){
		
		Uri uri = EMetaData.EventsMetaData.CONTENT_URI;
		String[] projection = { EMetaData.EventsMetaData.TITLE, EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS,
				EMetaData.EventsMetaData.NEW_MESSAGES_COUNT, EMetaData.EventsMetaData.MESSAGES_COUNT, EMetaData.EventsMetaData.E_ID };
		String selection = EMetaData.EventsMetaData.MESSAGES_COUNT + ">0";
		String sortOrder = EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS;
		Cursor result = context.getContentResolver().query(uri, projection, selection, null, sortOrder);
		ArrayList<Event> resultList = new ArrayList<Event>();
		if (result.moveToFirst()){
			while (result.moveToNext()){
				System.out.println(" ");
				System.out.println(result.getString(result.getColumnIndex(EMetaData.EventsMetaData.E_ID)));
			}
		}
		result.close();

		
		return resultList;
	}



	/**
	 * @author justinas.marcinka@gmail.com Updates event in both local and
	 * remote db with given info. Offline mode NOT YET IMPLEMENTED.
	 * @param event<br>
	 * Fields that MUST BE SET:<br>
	 *  Event.internalID <br>
	 *  Event.event_ID <br>
	 *  all other fields that contain data to be updated
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void updateEvent(Context context, Event event) {
		if (networkAvailable) {
			event.setUploadedToServer(editEvent(event));
		} else {
			event.setUploadedToServer(false);
		}	
		updateEventInLocalDb(context, event);
	}
	
	
	public static final int TM_EVENTS_FROM_GIVEN_DATE = 0;
	public static final int TM_EVENTS_ON_GIVEN_DAY = 1;
	public static final int TM_EVENTS_ON_GIVEN_MONTH = 2;
	public static final int TM_EVENTS_ON_GIVEN_YEAR = 3;
	/**
	 * @author justinas.marcinka@gmail.com
	 * categories for exchange event data with remote server
	 */
	
	private static final String TOKEN = "token";
	private static final String CATEGORY = "category";
	private static final String SUCCESS = "success";
	private static final String REASON = "reason";
	
	private static final String EVENT_ID = "event_id";
	private static final String TIMEZONE = "timezone";
	private static final String TIME_START_UTC = "timestamp_start_utc";
	private static final String TIME_END_UTC = "timestamp_end_utc";
	private static final String USER_ID = "user_id";
	private static final String STATUS = "status";
	private static final String IS_OWNER = "is_owner";
	private static final String TYPE = "type";
	private static final String TITLE = "title";
	private static final String ICON = "icon";
	private static final String COLOR = "color";
	private static final String DESCRIPTION = "description";
	private static final String LOCATION = "location";
	private static final String ACCOMODATION = "accomodation";
	private static final String COST = "cost";
	private static final String TAKE_WITH_YOU = "take_with_you";
	private static final String GO_BY = "go_by";
	private static final String COUNTRY = "country";
	private static final String CITY = "city";
	private static final String STREET = "street";
	private static final String ZIP = "zip";
	private static final String ABSOLUTE_REMINDER_1 = "r1";
	private static final String ABSOLUTE_REMINDER_2 = "r2";
	private static final String ABSOLUTE_REMINDER_3 = "r3";
	private static final String ABSOLUTE_ALARM_1 = "a1";
	private static final String ALARM_1_FIRED = "alarm1_fired";
	private static final String ABSOLUTE_ALARM_2 = "a2";
	private static final String ALARM_2_FIRED = "alarm2_fired";
	private static final String ABSOLUTE_ALARM_3 = "a3";
	private static final String ALARM_3_FIRED = "alarm3_fired";
	private static final String TIMESTAMP_CREATED = "timestamp_created";
	private static final String TIMESTAMP_MODIFIED = "timestamp_modified";
	private static final String ATTENDANT_0_COUNT = "attendant_0_count";
	private static final String ATTENDANT_2_COUNT = "attendant_2_count";
	private static final String ATTENDANT_1_COUNT = "attendant_1_count";
	private static final String ATTENDANT_4_COUNT = "attendant_4_count";
	private static final String IS_SPORTS_EVENT = "is_sports_event";
	private static final String IS_ALL_DAY = "all_day";
	private static final String CREATOR_FULLNAME = "creator_fullname";
	private static final String CREATOR_CONTACT_ID = "is_sports_event";
	private static final String INVITED = "invited";
	private static final String MESSAGE_COUNT = "message_count";
	private static final String NEW_MESSAGE_COUNT = "nmc";
	private static final String MESSAGE_LAST_TIMESTAMP = "message_last_timestamp";
	private static final String ASSIGNED_CONTACTS = "assigned_contacts";
	private static final String EVENTS = "events";

	

	
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
	public static Cursor createEventProjectionByDateFromLocalDb(Context context, String[] projection, Calendar date, int daysToSelect, int eventTimeMode,
			String sortOrder, boolean filterRejected) {
		day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
		month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
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
					// TODO optimisation by using months column
					where = EventsProvider.EMetaData.EventsIndexesMetaData.DAY + " IN " + inStringDay;

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
				where = EventsProvider.EMetaData.EventsIndexesMetaData.MONTH + " = '" + month_index_formatter.format(date.getTime()) + "'" ;
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
		
		String rejectedFilter = " AND " + EventsProvider.EMetaData.EventsMetaData.STATUS + "!=" + Invited.REJECTED;
		if (filterRejected) where += rejectedFilter;
		
		return context.getContentResolver().query(uri, projection, where, null, sortOrder);

	}
	
	
	
	/**
	 * Gets events from remote Database and writes them to local DB.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param eventCategory API category. if empty, gets all events
	 * @since 2012-10-09
	 * @version 1.0
	 */
	public static void getEventsFromRemoteDb(Context context, String eventCategory) {
		boolean success = false;
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
					success = object.getBoolean(SUCCESS);

					if (success == false) {
						// // error = object.getString("error");
					} else {

						JSONArray es = object.getJSONArray(EVENTS);
						for (int i = 0; i < es.length(); i++) {
							try{
							JSONObject e = es.getJSONObject(i);
							event = createEventFromJSON(e);
							if (event != null && !event.isNative()) {
								event.setUploadedToServer(true);
								insertEventToLocalDB(context, event);
							}

							}
							catch (JSONException ex){
								Log.e(CLASS_NAME, "JSON");
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}
	}
	
	/////////////////////////////////////////////////////METHODS THAT WORK WITH LOCAL DB//////////////////////////////////////////////////////
	
	
	/**
	 * Inserts given event to local DB. Rows that correspond to given ID are overwritten.
	 * @param context
	 * @param event
	 * @since 2012-10-09
	 * @version 1.0
	 */
	protected static void insertEventToLocalDB(Context context, Event event) {

		// 1. ADD EVENT details to events table
		ContentValues cv = createCVforEventsTable(event);
		Uri eventUri = context.getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, cv);
		long internalID = ContentUris.parseId(eventUri);
		if (internalID >= 0){ 
		event.setInternalID(internalID);
		
		// 2. INSERT EVENT day indexes into events_days table
		insertEventToDayIndexTable(context, event);
		// 3. INSERT EVENT INVITEs
//		insertEventToInvitesTable(context, event);
		}
	}
	
	

//	private static void insertEventToInvitesTable(Context context, Event event) {
//		long event_id = event.getInternalID();
//		ContentValues cv;
//		ContentResolver resolver = context.getContentResolver();
//		ArrayList<Invited> invites = event.getInvited();
//		for (Invited invite : invites){
//			cv = new ContentValues();
//			cv.put(EventsProvider.EMetaData.InvitedMetaData.EVENT_ID, event_id);
//			cv.put(EventsProvider.EMetaData.InvitedMetaData.GCID, invite.getGcid());
//			cv.put(EventsProvider.EMetaData.InvitedMetaData.GUID, invite.getGuid());
//			cv.put(EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID, invite.getMy_contact_id());
//			cv.put(EventsProvider.EMetaData.InvitedMetaData.STATUS, invite.getStatus());
//			cv.put(EventsProvider.EMetaData.InvitedMetaData.NAME, invite.getName());
//			resolver.insert(EventsProvider.EMetaData.InvitedMetaData.CONTENT_URI, cv);
//		}	
//	}

	/**
	 * Updates event in local DB.
	 * @author justinas.marcinka@gmail.com
	 * @param context 
	 * @param event
	 */

	public static void updateEventInLocalDb(Context context, Event event) {
		
		Uri uri;
		String where = null;
		ContentResolver resolver = context.getContentResolver();
		long internalID = event.getInternalID();
	
//		query for old this event times data to find out if days_index update is needed
		boolean eventTimeChanged = false;
		String[] projection = {EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS};
		uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+ "/" + internalID);
		
		Cursor result = resolver.query(uri, projection, where, null, null);
		long oldStart = 0;
		long oldEnd = 0;
		if(result.moveToFirst()){
			oldStart = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
			oldEnd = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
			result.close();
		}
	
		ContentValues cv = createCVforEventsTable(event);
		cv.put(BaseColumns._ID, event.getInternalID()); //this is VERY important
			// 1 update event in events table
				uri = Uri
						.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI
								+ "/" + internalID);
		
			
			resolver.update(uri, cv, null, null);

		if (event.getStartCalendar() != null && event.getEndCalendar() != null){ // to prevent null pointer exception: that hurts if happens :)
			

			
			eventTimeChanged = oldStart != event.getStartCalendar().getTimeInMillis() || oldEnd != event.getEndCalendar().getTimeInMillis();
			
			// 3 Renew event data in time indexes

			if (eventTimeChanged) {
				where = EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID + "=" + internalID;
				resolver.delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, where, null);
				insertEventToDayIndexTable(context, event);
			}
		}

	}

	
	/**
	 * deletes event from local DB
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param internalID event local DB id
	 * @since 2012-10-09
	 * @version 1.0
	 */
	private static void deleteEventFromLocalDb(Context context, long internalID) {
		String where;

		// 1. Deleting event from events table
		where = BaseColumns._ID + "=" + internalID;
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, where, null);

		// 2. Deleting event from events day indexes table
		where = EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID + "=" + internalID;
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

		long event_id = event.getInternalID();

		if (event.is_all_day()) { // only one row is inserted
			insertEventDayIndexRow(context, event_id, eventDayStart);
		} else
			while (eventDayStart.before(event.getEndCalendar())) { // rows are
																	// inserted
																	// for each
																	// day that
																	// event
																	// lasts
				insertEventDayIndexRow(context, event_id, eventDayStart);
				eventDayStart.add(Calendar.DATE, 1);

			}
	}

	//TODO javadoc
	private static void insertEventDayIndexRow(Context context, long event_id, Calendar eventDayStart) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID, event_id);
		Date time = eventDayStart.getTime();

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY, day_index_formatter.format(time));

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH, month_index_formatter.format(time));
		context.getContentResolver().insert(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, cv);

	}

	//TODO javadoc
		public static Event getEventFromLocalDb(Context context, long internal_ID) {
			Event item = null;
			Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + internal_ID);
			Cursor result = context.getContentResolver().query(uri, null, null, null, null);
			if (result.moveToFirst()) {
				item = createEventFromCursor(result);
			
//			item.setInvited(getInvitesForEvent(context, item));
				
//
//				String assigned_contacts = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS));
//				if (assigned_contacts != null && !assigned_contacts.equals("null")) {
//					if (assigned_contacts.length() == 0) item.setAssigned_contacts(new int[0]);
//					else try {
//						item.setAssigned_contacts(Utils.jsonStringToArray(assigned_contacts));
//					} catch (JSONException e) {
//						Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//								e.getMessage());
//						item.setAssigned_contacts(new int[0]);
//					}
//				}
//
//				String assigned_groups = item.getAssigned_groups_DB_entry();
//				if (assigned_groups != null && !assigned_groups.equals("null")) {
//					if (assigned_groups.length() == 0) item.setAssigned_groups(new int[0]);
//					else try {
//						item.setAssigned_groups(Utils.jsonStringToArray(assigned_groups));
//					} catch (JSONException e) {
//						Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//								e.getMessage());
//						item.setAssigned_groups(new int[0]);
//					}
//				}
//				}
			}
			result.close();
			return item;
		}	
//TODO javadoc
		private static void updateEventStatusInLocalDb(Context context, Event event) {
			if (event.getMyInvite() != null){
				event.getMyInvite().setStatus(event.getStatus());                            //TODO solve this
			}
			ContentValues cv = new ContentValues();
			cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.getStatus());	
			cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, parseInvitedListToJSONArray(event.getInvited()));
			Uri uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
			String where = EventsProvider.EMetaData.EventsMetaData._ID+"=" + event.getInternalID();
			context.getContentResolver().update(uri, cv, where, null);	
		}
	
//private static ArrayList<Invited> getInvitesForEvent(Context context, Event event) {
//		ArrayList<Invited> invites = new ArrayList<Invited>();
//		long id = event.getInternalID();
//		
//		String where = EventsProvider.EMetaData.InvitedMetaData.EVENT_ID + "=" + id; 
//		Uri uri = EventsProvider.EMetaData.InvitedMetaData.CONTENT_URI;
//		Cursor result = context.getContentResolver().query(uri, null, where, null, null);
//		if (result.moveToFirst()){
//			invites.add(createInvitedFromCursor(result));
//			
//		}
//		result.close();
//		return invites;
//	}



/////////////////////////////////////////////////////METHODS THAT WORK WITH RMOTE DB//////////////////////////////////////////////////////	
		
		
		private static boolean updateEventStatusInServer(Event event) {
			if (event.getEvent_id() == 0) return false;
			boolean success = false;

			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/set_event_status");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart(EVENT_ID, new StringBody("" + event.getEvent_id()));
				reqEntity.addPart(STATUS, new StringBody("" + event.getStatus()));
				
				post.setEntity(reqEntity);
				
				if (networkAvailable) {
					HttpResponse rp = hc.execute(post);

					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							success = object.getBoolean("success");
							if (!success) {
								Log.e("Response to event error", object.getJSONObject("error").getString("reason"));
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
				Reporter.reportError(DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
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
		public static int createEventInRemoteDb(Event e) {
			boolean success = false;

			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_create");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

				if (e.getIcon().length() > 0)
					reqEntity.addPart("icon", new StringBody(e.getIcon()));

				reqEntity.addPart("color", new StringBody(e.getColor()));

				reqEntity.addPart("title", new StringBody(e.getTitle()));

				reqEntity.addPart("timestamp_start_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis())));
				reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis())));

				
				reqEntity.addPart("description", new StringBody(e.getDescription()));
				

				if (e.getCountry().length() > 0)
					reqEntity.addPart("country", new StringBody(e.getCountry()));
				if (e.getCity().length() > 0)
					reqEntity.addPart("city", new StringBody(e.getCity()));
				if (e.getStreet().length() > 0)
					reqEntity.addPart("street", new StringBody(e.getStreet()));
				if (e.getZip().length() > 0)
					reqEntity.addPart("zip", new StringBody(e.getZip()));
				reqEntity.addPart(TIMEZONE, new StringBody(e.getTimezone()));

				
				if (e.getLocation().length() > 0)
					reqEntity.addPart("location", new StringBody(e.getLocation()));
				if (e.getGo_by().length() > 0)
					reqEntity.addPart("go_by", new StringBody(e.getGo_by()));
				if (e.getTake_with_you().length() > 0)
					reqEntity.addPart("take_with_you", new StringBody(e.getTake_with_you()));
				if (e.getCost().length() > 0)
					reqEntity.addPart("cost", new StringBody(e.getCost()));
				if (e.getAccomodation().length() > 0)
					reqEntity.addPart("accomodation", new StringBody(e.getAccomodation()));
				if (e.getAssigned_contacts() != null) {
					for (int i = 0, l = e.getAssigned_contacts().length; i < l; i++) {
						reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.getAssigned_contacts()[i])));
					}
				} else {
					reqEntity.addPart("contacts[]", new StringBody(""));
				}
//				if (e.assigned_groups != null) {
//					for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
//						reqEntity.addPart("groups[]", new StringBody(String.valueOf(e.assigned_groups[i])));
//					}
//				} else {
//					reqEntity.addPart("groups[]", new StringBody(""));
//				}

				if (e.getAlarm1() != null) {
					reqEntity.addPart("a1", new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm1().getTimeInMillis())));
				}
				if (e.getAlarm2() != null) {
					reqEntity.addPart("a2", new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm2().getTimeInMillis())));
				}
				if (e.getAlarm3() != null) {
					reqEntity.addPart("a3", new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm3().getTimeInMillis())));
				}

				if (e.getReminder1() != null) {
					reqEntity.addPart("r1", new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder1().getTimeInMillis())));
				}
				if (e.getReminder2() != null) {
					reqEntity.addPart("r2", new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder2().getTimeInMillis())));
				}
				if (e.getReminder3() != null) {
					reqEntity.addPart("r3", new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder3().getTimeInMillis())));
				}

				if (e.isBirthday()) {
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
				} else {
					OfflineData uplooad = new OfflineData("mobile/events_create", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception ex) {
				Reporter.reportError(DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
			}
			return 0;

		}
		
		private static boolean editEvent(Event e) {
			boolean success = false;

			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_edit");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(e.getEvent_id())));

				reqEntity.addPart("event_type", new StringBody(e.getType()));

				reqEntity.addPart("icon", new StringBody(e.getIcon()));
				reqEntity.addPart("color", new StringBody(e.getColor()));

				reqEntity.addPart("title", new StringBody(e.getTitle()));

				reqEntity.addPart("timestamp_start_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis())));
				reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis())));

				reqEntity.addPart("timezone", new StringBody(e.getTimezone()));

				reqEntity.addPart("description", new StringBody(e.getDescription()));

				reqEntity.addPart("country", new StringBody(e.getCountry()));
				reqEntity.addPart("zip", new StringBody(e.getZip()));
				reqEntity.addPart("city", new StringBody(e.getCity()));
				reqEntity.addPart("street", new StringBody(e.getStreet()));
				reqEntity.addPart("location", new StringBody(e.getLocation()));

				reqEntity.addPart("go_by", new StringBody(e.getGo_by()));
				reqEntity.addPart("take_with_you", new StringBody(e.getTake_with_you()));
				reqEntity.addPart("cost", new StringBody(e.getCost()));
				reqEntity.addPart("accomodation", new StringBody(e.getAccomodation()));
				
				if(e.getAlarm1() != null){
					reqEntity.addPart("a1", new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm1().getTimeInMillis())));
				}
				if(e.getAlarm2() != null){
					reqEntity.addPart("a2", new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm2().getTimeInMillis())));
				}
				if(e.getAlarm3() != null){
					reqEntity.addPart("a3", new StringBody("" + Utils.millisToUnixTimestamp(e.getAlarm3().getTimeInMillis())));
				}

				if(e.getReminder1() != null){
					reqEntity.addPart("r1", new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder1().getTimeInMillis())));
				}
				if(e.getReminder2() != null){
					reqEntity.addPart("r2", new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder2().getTimeInMillis())));
				}
				if(e.getReminder3() != null){
					reqEntity.addPart("r3", new StringBody("" + Utils.millisToUnixTimestamp(e.getReminder3().getTimeInMillis())));
				}

//				if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
//					e.assigned_contacts = new int[Data.selectedContacts.size()];
//					int i = 0;
//					for (Contact contact : Data.selectedContacts) {
//						e.assigned_contacts[i] = contact.contact_id;
//						i++;
//					}
//				}
				if (e.getAssigned_contacts() != null) {
					for (int i = 0, l = e.getAssigned_contacts().length; i < l; i++) {
						reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.getAssigned_contacts()[i])));
					}
				} else {
					reqEntity.addPart("contacts[]", new StringBody(""));
				}

//				if (e.assigned_groups != null) {
//					for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
//						reqEntity.addPart("groups[]", new StringBody(String.valueOf(e.assigned_groups[i])));
//					}
//				} else {
//					reqEntity.addPart("groups[]", new StringBody(""));
//				}

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
				Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
				success = false;
			}

			return success;
		}
		
		public static boolean removeEvent(int id) {
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
								EventManagement.error = (errObj.getString("reason"));
								Log.e("removeEvent - error: ", Data.getERROR());
							} else {
//								Data.getEvents().remove(getEventFromLocalDb(id));
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/events_remove", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception ex) {
				Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
			}
			return success;
		}

		
////////////////////////////////////////////////////////////////////DB UTILITY METHODS////////////////////////////////////////////////////////////////////
//	TODO javadoc
		/**
		 * Fills ContentValues instance with event data
		 * IMPORTANT: _ID field must be set MANUALLY
		 * @param event
		 * @return CV for event. 
		 */
	protected static ContentValues createCVforEventsTable(Event event) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.getEvent_id());
		
		
		cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, event.getUser_id());
		cv.put(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY, event.isUploadedToServer()? 1 : 0);
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
		if (event.getStartCalendar()!= null)cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, event.getStartCalendar().getTimeInMillis());
		if (event.getEndCalendar()!= null)cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS, event.getEndCalendar().getTimeInMillis());

		// reminders
		if (event.getReminder1() != null)cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.getReminder1().getTimeInMillis());
		if (event.getReminder2() != null)cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.getReminder2().getTimeInMillis());
		if (event.getReminder3() != null)cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.getReminder3().getTimeInMillis());

		// TODO alarms DO SOMETHING WITH ALARM FIRED FIELDS
		if (event.getAlarm1() != null) cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM1, event.getAlarm1().getTimeInMillis());
		if (event.getAlarm2() != null)cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM2, event.getAlarm2().getTimeInMillis());
		if (event.getAlarm3() != null)cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM3, event.getAlarm3().getTimeInMillis());

		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS, event.getCreatedUtc());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS, event.getModifiedMillisUtc());
		cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, parseInvitedListToJSONArray(event.getInvited()));
// TODO???
//		cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS, event.getAssigned_contacts_DB_entry());
//		cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS, event.getAssigned_groups_DB_entry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MESSAGES_COUNT, event.getMessage_count());
		return cv;
	}

private static String parseInvitedListToJSONArray(ArrayList<Invited> invited) {
	if(invited.isEmpty()) return "[]";
	StringBuilder sb = new StringBuilder();
	sb.append('[');
	for (Invited invite : invited){
		sb.append('{');
		sb.append(invite.toString());
		sb.append('}');
		sb.append(',');
	}
	sb.deleteCharAt(sb.length() - 1);
	sb.append(']');
	return sb.toString();
}

//	TODO javadoc
	protected static Event createEventFromCursor(Cursor result) {
		Event item = new Event();
		long timeinMillis;
		item.setInternalID(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData._ID)));
		item.setEvent_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID)));
		item.setUser_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.USER_ID)));
		item.setUploadedToServer(1 == result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY)));
		item.setStatus(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS)));
		item.setCreator_contact_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID)));

		item.setAttendant_0_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT)));
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

		item.setReminder1(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER1)), user_timezone));
		item.setReminder2(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER2)), user_timezone));
		item.setReminder3(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER3)), user_timezone));
		
		item.setAlarm1(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ALARM1)), user_timezone));
		item.setAlarm2(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ALARM2)), user_timezone));
		item.setAlarm3(Utils.createCalendar(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ALARM3)), user_timezone));

		item.setCreatedMillisUtc(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS)));
		item.setModifiedMillisUtc(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS)));
		try {
			ArrayList<Invited> invites = new ArrayList<Invited>();
			
			item.setMyInvite(createInvitedListFromJSONArrayString(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.INVITED)), invites));
			item.setInvited(invites);
		} catch (JSONException e) {
			Log.e("Error parsing invited array from local db", "Event ID: " + item.getEvent_id() + " event local ID: " + item.getInternalID());
		}
		

//		item.setAssigned_contacts_DB_entry(result.getString(result
//				.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS)));
//		item.setAssigned_groups_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS)));

		item.setMessage_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MESSAGES_COUNT)));
		return item;
	}	

	/**
	 * Creates Event object from JSON object
	 * 
	 * @param e
	 *            JSON object
	 * @return new event object with all info set. WARNING: if not all required
	 *         fields are set, returns null <br>
	 *         Needed fields: event_id<br>
	 *         user_id<br>
	 *         status<br>
	 *         is_owner<br>
	 *         title<br>
	 *         timezone<br>
	 *         time_start_utc<br>
	 *         time_end_utc<br>
	 */
	protected static Event createEventFromJSON(JSONObject e) {
		Event event = new Event();
		long unixTimestamp;
//		
//		
//		HashMap<String, String> filterMap = new HashMap<String, String>();
//		 Iterator keys = e.keys();
//		
//		while (keys.hasNext()){
//			String key = (String)keys.next();
//			try {
//				filterMap.put(key, e.getString(key));
//			} catch (JSONException e1) {
//				Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//						e1.getMessage()); 
//				e1.printStackTrace();
//			}
//
//		}
//		
//		try{
//			event.setEvent_id(Integer.parseInt(filterMap.get(JSON_TAG_EVENT_ID)));
//			event.setTimezone(filterMap.get(JSON_TAG_TIMEZONE));
//			unixTimestamp = Long.parseLong(filterMap.get(JSON_TAG_TIME_START_UTC));
//			event.setStartCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
//			unixTimestamp = Long.parseLong(filterMap.get(JSON_TAG_TIME_END_UTC));
//			event.setEndCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
//			unixTimestamp = Long.parseLong(filterMap.get(JSON_TAG_TIMESTAMP_CREATED));
//			event.setCreatedMillisUtc(Utils.unixTimestampToMilis(unixTimestamp));
//			unixTimestamp = Long.parseLong(filterMap.get(JSON_TAG_TIMESTAMP_MODIFIED));
//			event.setModifiedMillisUtc(Utils.unixTimestampToMilis(unixTimestamp));
//		}catch (NumberFormatException ex){
//			Log.e(CLASS_NAME, "FAILED TO PARSE ESSENTIAL EVENT DATA", ex);
//			return null;
//		}
//		event.setUser_id(Integer.parseInt(filterMap.get(JSON_TAG_USER_ID)));
//		event.setStatus(Integer.parseInt(filterMap.get(JSON_TAG_STATUS)));
//		event.setCreator_contact_id(Integer.parseInt(filterMap.get(JSON_TAG_CREATOR_CONTACT_ID)));
//		
//		event.setAttendant_0_count(Integer.parseInt(filterMap.get(JSON_TAG_ATTENDANT_0_COUNT)));
//		event.setAttendant_1_count(Integer.parseInt(filterMap.get(JSON_TAG_ATTENDANT_1_COUNT)));
//		event.setAttendant_2_count(Integer.parseInt(filterMap.get(JSON_TAG_ATTENDANT_2_COUNT)));
//		event.setAttendant_4_count(Integer.parseInt(filterMap.get(JSON_TAG_ATTENDANT_4_COUNT)));
//		
//		event.setSports_event(Integer.parseInt(filterMap.get(JSON_TAG_IS_SPORTS_EVENT)) == 1);
//		event.setIs_owner(Integer.parseInt(filterMap.get(JSON_TAG_IS_OWNER)) == 1);
//		event.setIs_all_day(Integer.parseInt(filterMap.get(JSON_TAG_ALL_DAY)) == 1);
//		//TODO birthdays????
//		event.setBirthday(false);
//		
//		event.setType(filterMap.get(JSON_TAG_TYPE));
//		event.setCreator_fullname(filterMap.get(JSON_TAG_CREATOR_FULLNAME));
//		event.setTitle(filterMap.get(JSON_TAG_TITLE));
//		event.setIcon(filterMap.get(JSON_TAG_ICON));
//		event.setColor(filterMap.get(JSON_TAG_COLOR));
//		event.setDescription(filterMap.get(JSON_TAG_DESCRIPTION));
//		event.setLocation(filterMap.get(JSON_TAG_LOCATION));
//		event.setAccomodation(filterMap.get(JSON_TAG_ACCOMODATION));
//		event.setCost(filterMap.get(JSON_TAG_COST));
//		event.setTake_with_you(filterMap.get(JSON_TAG_TAKE_WITH_YOU));
//		event.setGo_by(filterMap.get(JSON_TAG_GO_BY));
//		
//		event.setCountry(filterMap.get(JSON_TAG_COUNTRY));
//		event.setCity(filterMap.get(JSON_TAG_CITY));
//		event.setStreet(filterMap.get(JSON_TAG_STREET));
//		event.setZip(filterMap.get(JSON_TAG_ZIP));
//		event.setMessage_count(Integer.parseInt(filterMap.get(JSON_TAG_MESSAGE_COUNT)));
//		
//		try {
//			event.setInvited(createInvitedListFromJSONArrayString(filterMap.get(JSON_TAG_INVITED)));
//		} catch (JSONException e1) {
//			event.setInvited(new ArrayList<Invited>());
//		}
//		


//		day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
//		month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
		// critical event info. If fetch fails, return null
		try {
			event.setEvent_id(e.getInt(EVENT_ID));
			event.setTimezone(e.getString(TIMEZONE));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
			// EVENT TIME START

			unixTimestamp = e.optLong(TIME_START_UTC);
			if (unixTimestamp == 0) return null;
			event.setStartCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		
			// EVENT TIME END
				unixTimestamp = e.optLong(TIME_END_UTC);
				if (unixTimestamp == 0) return null;
			event.setEndCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		
		
		


			event.setUser_id(e.optInt(USER_ID));
		
			event.setStatus(e.optInt(STATUS));


			event.setIs_owner(e.optInt(IS_OWNER) == 1);

		try {
			event.setType(e.getString(TYPE));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setTitle(e.getString(TITLE));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setIcon(e.getString(ICON));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setColor(e.getString(COLOR));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setDescription(e.getString(DESCRIPTION));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setLocation(e.getString(LOCATION));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAccomodation(e.getString(ACCOMODATION));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCost(e.getString(COST));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setTake_with_you(e.getString(TAKE_WITH_YOU));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setGo_by(e.getString(GO_BY));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCountry(e.getString(COUNTRY));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCity(e.getString(CITY));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setStreet(e.getString(STREET));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setZip(e.getString(ZIP));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		// reminders
		unixTimestamp = e.optLong(ABSOLUTE_REMINDER_1);
		if (unixTimestamp != 0) event.setReminder1(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		unixTimestamp = e.optLong(ABSOLUTE_REMINDER_2);
		if (unixTimestamp != 0) event.setReminder2(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		unixTimestamp = e.optLong(ABSOLUTE_REMINDER_3);
		if (unixTimestamp != 0) event.setReminder3(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		//alarms
		unixTimestamp = e.optLong(ABSOLUTE_ALARM_1);
		if (unixTimestamp != 0) event.setAlarm1(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		unixTimestamp = e.optLong(ABSOLUTE_ALARM_2);
		if (unixTimestamp != 0) event.setAlarm2(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));
		unixTimestamp = e.optLong(ABSOLUTE_ALARM_3);
		if (unixTimestamp != 0) event.setAlarm3(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), user_timezone));

		try {
			event.setAlarm1fired(e.getString(ALARM_1_FIRED));
		} catch (JSONException e1) {
//			System.out.println("LONG PARSE FAILED");
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAlarm2fired(e.getString(ALARM_2_FIRED));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
	
		try {
			event.setAlarm3fired(e.getString(ALARM_3_FIRED));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}


			
			event.setCreatedMillisUtc(Utils.unixTimestampToMilis(e.optLong(TIMESTAMP_CREATED)));
			event.setModifiedMillisUtc(Utils.unixTimestampToMilis(e.optLong(TIMESTAMP_MODIFIED)));


			event.setAttendant_0_count(e.optInt(ATTENDANT_0_COUNT));
			event.setAttendant_1_count(e.optInt(ATTENDANT_1_COUNT));
			event.setAttendant_2_count(e.optInt(ATTENDANT_2_COUNT));
			event.setAttendant_4_count(e.optInt(ATTENDANT_4_COUNT));
	
			event.setSports_event(e.optInt(IS_SPORTS_EVENT) == 1);
			event.setIs_all_day(e.optInt(IS_ALL_DAY) == 1);

		try {
			event.setCreator_fullname(e.getString(CREATOR_FULLNAME));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

			event.setCreator_contact_id(e.optInt(CREATOR_CONTACT_ID));
		
//		try {
//			JSONArray arrayString = e.getJSONArray(JSON_TAG_ASSIGNED_CONTACTS);
////			event.setAssigned_contacts(parseJSONArrayToLongArray(arrayString));
//		} catch (JSONException e1) {
//			
////			event.setAssigned_contacts_DB_entry("");
//			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//					e1.getMessage());
//		}
//		try {
//			event.setAssigned_groups_DB_entry(e.getString("assigned_groups"));
//		} catch (JSONException e1) {
//			event.setAssigned_contacts_DB_entry("");
//			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
//					e1.getMessage());
//		}
		try {
			String jsonstring = e.getString(INVITED);
		
			ArrayList<Invited> invites = new ArrayList<Invited>();
			createInvitedListFromJSONArrayString(jsonstring, invites);
			event.setInvited(invites);
		} catch (JSONException e1) {
			event.setInvited(new ArrayList<Invited>());
		}

			event.setMessage_count(e.optInt(MESSAGE_COUNT));

		return event;
	}

	

	private static Invited createInvitedListFromJSONArrayString(
		String jsonArrayString, ArrayList<Invited> invites) throws JSONException  {
		JSONArray jsonArray= new JSONArray(jsonArrayString);
		if (invites == null) invites = new ArrayList<Invited>();
		Account acc = new Account();
		int id = acc.getUser_id();
		Invited myInvite = null;
		int count = jsonArray.length();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				JSONObject e = jsonArray.getJSONObject(i);
				Invited o = createInvitedFromJSONObject(e);
				invites.add(o);
				if (id == o.getGuid()) myInvite = o;
			}
		}
		
		return myInvite;
		}
	
	private static Invited createInvitedFromJSONObject(JSONObject input) {
		Invited item = new Invited();
		

		try {
			item.setName(input.getString(EventsProvider.EMetaData.InvitedMetaData.NAME));
		} catch (JSONException e) {
//			Log.e("Invited(JSONObject input)", "Failed getting gname");
		}
		
			item.setGcid(input.optInt(EventsProvider.EMetaData.InvitedMetaData.GCID));
			item.setGuid(input.optInt(EventsProvider.EMetaData.InvitedMetaData.GUID));
			item.setStatus(input.optInt(EventsProvider.EMetaData.InvitedMetaData.STATUS));
			item.setMy_contact_id(input.optInt(EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID));

		return item;
	}
	//TODO document
	public static ArrayList<Event> getEventsFromLocalDb(Context context, boolean filterActual) {
		Event item;
		String where = null;
		
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		if (filterActual) where = EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + " > " + today.getTimeInMillis();
				
		ArrayList<Event> items = new ArrayList<Event>();
	
			Cursor result = context.getContentResolver()
					.query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);

		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				item = EventManagement.createEventFromCursor(result);
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
	 * @deprecated
	 */
	public static int loadEvents(Context context, EventsAdapter eAdapter) {
		int eventsSize = 0;
		ArrayList<Event> events;
	
		events = EventManagement.getEventsFromLocalDb(context, true);
		if (eAdapter != null) {
			eAdapter.setItems(events);
			eAdapter.notifyDataSetChanged();
		}
//		TODO Justas M: rewrite
//		events = getEventsFromLocalDb(today, null);
//
//		ArrayList<Event> onlyInvites = null;
//		if (NavbarActivity.showInvites) {
//			onlyInvites = filterInvites(events);
//			eventsSize = onlyInvites.size();
//		} else {
//			eventsSize = events.size();
//		}
//		if (onlyInvites != null && onlyInvites.size() > 0) {
//			updateEventsAdapter(onlyInvites, eAdapter);
//		} else {
//			updateEventsAdapter(events, eAdapter);
//		}

		return eventsSize;
	}

	public static CharSequence getError() {
		return error;
	}

//	private static Invited createInvitedFromCursor(Cursor result) {
//		Invited invited = new Invited();
//		invited.setGcid(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.GCID)));
//		invited.setGuid(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.GUID)));
//		invited.setMy_contact_id(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID)));
//		invited.setName(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.NAME)));
//		invited.setStatus(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.InvitedMetaData.STATUS)));
//		return invited;
//	}
	
}