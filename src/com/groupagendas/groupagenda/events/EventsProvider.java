package com.groupagendas.groupagenda.events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.groupagendas.groupagenda.events.EventsProvider.EMetaData.AlarmsMetaData;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;

@SuppressLint("SimpleDateFormat")
public class EventsProvider extends ContentProvider{
	public static DatabaseHelper mOpenHelper;

	private SimpleDateFormat day_index_formatter = new SimpleDateFormat(EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);

	private SimpleDateFormat month_index_formatter = new SimpleDateFormat(EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
	
//	private SimpleDateFormat time_index_formatter = new SimpleDateFormat("");
	
	public static class EMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.events.EventsProvider";
		public static final String DATABASE_NAME = "events.sqlite";

		public static final int DATABASE_VERSION = 1;

		public static final String EVENTS_TABLE = "events";
		public static final String EVENT_DAY_INDEX_TABLE = "events_days";
		public static final String ALARMS_TABLE = "alarms";
//		public static final String INVITED_TABLE = "invited";
		
		private static final String events_on_date = "events_on_date";
		protected static final String indexed_events = "indexed_events";
		
		public static final Uri EVENTS_ON_DATE_URI = Uri.parse("content://" + AUTHORITY + "/" + events_on_date);
		public static final Uri INDEXED_EVENTS_URI = Uri.parse("content://" + AUTHORITY + "/" + indexed_events);
		
		
		public static final class EventsIndexesMetaData{
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENT_DAY_INDEX_TABLE);	
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.groupagendas.events_days_item";
			
			public static final String DAY_COLUMN_FORMAT = "yyyy-MM-dd";
			public static final String MONTH_COLUMN_FORMAT = "yyyy-MM";
			
			public static final String EVENT_INTERNAL_ID = EventsMetaData._ID;
			public static final String EVENT_EXTERNAL_ID = EventsMetaData.E_ID;
			public static final String DAY = "day";
			public static final String MONTH = "month";
			public static final String DAY_TIME_START = "day_time_start";
			public static final String DAY_TIME_END = "day_time_end";
			public static final String NOT_TODAY = "...";
			
		}
		
		public static final class InvitedMetaData{
//			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + INVITED_TABLE);	
//			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.groupagendas.invited_item";
//			
//			public static final String EVENT_ID = "event_internal_id";
			
			public static final String STATUS = "status";
			public static final String MY_CONTACT_ID = "my_contact_id";
			public static final String GCID = "gcid";
			public static final String GUID = "guid";
			public static final String NAME = "gname";

		}
		
		public static final class AlarmsMetaData{
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ALARMS_TABLE);	
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.groupagendas.alarm_item";
			
			public static final String ALARM_ID = "_id";
			public static final String USER_ID = "user_id";
			public static final String EVENT_ID = "event_id";
			
			public static final String TIMESTAMP = "timestamp";
			public static final String OFFSET = "offset";
			public static final String SENT = "sent";
			public static final String DEFAULT_SORT_ORDER = TIMESTAMP+" ASC";

		}
		
		public static final class EventsMetaData implements BaseColumns{
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE);
			public static final Uri CONTENT_URI_EXTERNAL_ID = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE + "/external");
			public static final Uri UPDATE_EVENTS_NEW_MESSAGES_COUNT = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE + "/increment_messages_attributes");
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.events_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.events_item";
			

			public static final String E_ID 	= "event_id";
			public static final String USER_ID 	= "user_id";
			
			public static final String IS_SPORTS_EVENT	= "is_sports_event";
			public static final String STATUS 	= "status";
			public static final String IS_OWNER = "is_owner";
			public static final String TYPE 	= "type";
			
			public static final String CREATOR_FULLNAME		= "creator_fullname";
			public static final String CREATOR_CONTACT_ID	= "author_contact_id";
			
			
			public static final String TITLE	= "title";
			public static final String ICON		= "icon";
			public static final String COLOR 	= "color";
//			public static final String TEXT_COLOR 	= "text_color";//2012-10-24
			public static final String EVENT_DISPLAY_COLOR 	= "event_display_color";
			public static final String DESC 	= "description_";
			
			public static final String LOCATION		= "location";
			public static final String ACCOMODATION	= "accomodation";
			
			public static final String COST 			= "cost";
			public static final String TAKE_WITH_YOU	= "take_with_you";
			public static final String GO_BY 			= "go_by";
			
			public static final String COUNTRY 	= "country";
			public static final String CITY 	= "city";
			public static final String STREET 	= "street";
			public static final String ZIP 		= "zip";
			
			public static final String TIMEZONE = "timezone";
			public static final String TIME_START_UTC_MILLISECONDS = "time_start_utc";
			public static final String TIME_END_UTC_MILLISECONDS = "time_end_utc";
			
			public static final String REMINDER1 = "reminder1";
			public static final String REMINDER2 = "reminder2";
			public static final String REMINDER3 = "reminder3";
			
			public static final String ALARM1 = "alarm1";
			public static final String ALARM2 = "alarm2";
			public static final String ALARM3 = "alarm3";
			
			public static final String CREATED_UTC_MILLISECONDS = "created_utc";
			public static final String MODIFIED_UTC_MILLISECONDS = "modified_utc";
			
			public static final String ATTENDANT_1_COUNT = "attendant_1_count";
			public static final String ATTENDANT_2_COUNT = "attendant_2_count";
			public static final String ATTENDANT_0_COUNT = "attendant_0_count";
			public static final String ATTENDANT_4_COUNT = "attendant_4_count";
			
			public static final String ASSIGNED_CONTACTS = "assigned_contacts";
			public static final String ASSIGNED_GROUPS = "assigned_groups";
			
			public static final String UPLOADED_SUCCESSFULLY = "uploaded";
			
			public static final String DEFAULT_SORT_ORDER = TIME_START_UTC_MILLISECONDS+" ASC";
			public static final String IS_BIRTHDAY = "is_birthday";
			public static final String IS_ALL_DAY = "is_all_day";
			public static final String MESSAGES_COUNT = "messages_count";
			public static final String NEW_MESSAGES_COUNT = "nmc";
			public static final String LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS = "last_message_date_time";
			public static final String INVITED = "invited";
			public static final String POLL = "poll";
			public static final String SELECTED_EVENT_POLLS_TIME = "selected_event_polls_time";
			
		}
	}
	
	// Events Table Projection Map
	private static HashMap<String, String> EM;
	
	static {
		EM = new HashMap<String, String>();
		EM.put(EMetaData.EventsMetaData._ID, EMetaData.EVENTS_TABLE+ "." + EMetaData.EventsMetaData._ID);
		EM.put(EMetaData.EventsMetaData.E_ID, EMetaData.EVENTS_TABLE+ "." + EMetaData.EventsMetaData.E_ID);
		EM.put(EMetaData.EventsMetaData.USER_ID, EMetaData.EventsMetaData.USER_ID);
		
		EM.put(EMetaData.EventsMetaData.IS_SPORTS_EVENT, EMetaData.EventsMetaData.IS_SPORTS_EVENT);
		EM.put(EMetaData.EventsMetaData.STATUS, EMetaData.EventsMetaData.STATUS);
		EM.put(EMetaData.EventsMetaData.IS_OWNER, EMetaData.EventsMetaData.IS_OWNER);
		EM.put(EMetaData.EventsMetaData.TYPE, EMetaData.EventsMetaData.TYPE);
		EM.put(EMetaData.EventsMetaData.IS_ALL_DAY, EMetaData.EventsMetaData.IS_ALL_DAY);
		EM.put(EMetaData.EventsMetaData.IS_BIRTHDAY, EMetaData.EventsMetaData.IS_BIRTHDAY);
		
		EM.put(EMetaData.EventsMetaData.CREATOR_FULLNAME, EMetaData.EventsMetaData.CREATOR_FULLNAME);
		EM.put(EMetaData.EventsMetaData.CREATOR_CONTACT_ID, EMetaData.EventsMetaData.CREATOR_CONTACT_ID);
		
		EM.put(EMetaData.EventsMetaData.TITLE, EMetaData.EventsMetaData.TITLE);
		EM.put(EMetaData.EventsMetaData.ICON, EMetaData.EventsMetaData.ICON);
		EM.put(EMetaData.EventsMetaData.COLOR, EMetaData.EventsMetaData.COLOR);
//		EM.put(EMetaData.EventsMetaData.TEXT_COLOR, EMetaData.EventsMetaData.TEXT_COLOR);//2012-10-24
		EM.put(EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR, EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR);//2012-10-24
		EM.put(EMetaData.EventsMetaData.DESC, EMetaData.EventsMetaData.DESC);
		
		EM.put(EMetaData.EventsMetaData.LOCATION, EMetaData.EventsMetaData.LOCATION);
		EM.put(EMetaData.EventsMetaData.ACCOMODATION, EMetaData.EventsMetaData.ACCOMODATION);
		
		EM.put(EMetaData.EventsMetaData.COST, EMetaData.EventsMetaData.COST);
		EM.put(EMetaData.EventsMetaData.TAKE_WITH_YOU, EMetaData.EventsMetaData.TAKE_WITH_YOU);
		EM.put(EMetaData.EventsMetaData.GO_BY, EMetaData.EventsMetaData.GO_BY);
		
		EM.put(EMetaData.EventsMetaData.COUNTRY, EMetaData.EventsMetaData.COUNTRY);
		EM.put(EMetaData.EventsMetaData.CITY, EMetaData.EventsMetaData.CITY);
		EM.put(EMetaData.EventsMetaData.STREET, EMetaData.EventsMetaData.STREET);
		EM.put(EMetaData.EventsMetaData.ZIP, EMetaData.EventsMetaData.ZIP);
		
		EM.put(EMetaData.EventsMetaData.TIMEZONE, EMetaData.EventsMetaData.TIMEZONE);
		EM.put(EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS);
		EM.put(EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS, EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS);
		
		EM.put(EMetaData.EventsMetaData.REMINDER1, EMetaData.EventsMetaData.REMINDER1);
		EM.put(EMetaData.EventsMetaData.REMINDER2, EMetaData.EventsMetaData.REMINDER2);
		EM.put(EMetaData.EventsMetaData.REMINDER3, EMetaData.EventsMetaData.REMINDER3);
		
		EM.put(EMetaData.EventsMetaData.ALARM1, EMetaData.EventsMetaData.ALARM1);
		EM.put(EMetaData.EventsMetaData.ALARM2, EMetaData.EventsMetaData.ALARM2);
		EM.put(EMetaData.EventsMetaData.ALARM3, EMetaData.EventsMetaData.ALARM3);
		

		EM.put(EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS, EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS);
		EM.put(EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS, EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS);
		
		EM.put(EMetaData.EventsMetaData.ATTENDANT_1_COUNT, EMetaData.EventsMetaData.ATTENDANT_1_COUNT);
		EM.put(EMetaData.EventsMetaData.ATTENDANT_2_COUNT, EMetaData.EventsMetaData.ATTENDANT_2_COUNT);
		EM.put(EMetaData.EventsMetaData.ATTENDANT_0_COUNT, EMetaData.EventsMetaData.ATTENDANT_0_COUNT);
		EM.put(EMetaData.EventsMetaData.ATTENDANT_4_COUNT, EMetaData.EventsMetaData.ATTENDANT_4_COUNT);
		
		EM.put(EMetaData.EventsMetaData.INVITED, EMetaData.EventsMetaData.INVITED);
//		EM.put(EMetaData.EventsMetaData.ASSIGNED_CONTACTS, EMetaData.EventsMetaData.ASSIGNED_CONTACTS);
//		EM.put(EMetaData.EventsMetaData.ASSIGNED_GROUPS, EMetaData.EventsMetaData.ASSIGNED_GROUPS);
		EM.put(EMetaData.EventsMetaData.MESSAGES_COUNT, EMetaData.EventsMetaData.MESSAGES_COUNT);
		EM.put(EMetaData.EventsMetaData.NEW_MESSAGES_COUNT, EMetaData.EventsMetaData.NEW_MESSAGES_COUNT);
		EM.put(EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS, EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS);
		
		EM.put(EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY, EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY);
		EM.put(EMetaData.EventsMetaData.POLL, EMetaData.EventsMetaData.POLL);
		EM.put(EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME, EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME);
		
	}
	// END Table Projection Map
	
	// Events Table Projection Map
	private static HashMap<String, String> EM1;
	
	static {
		EM1 = new HashMap<String, String>();
		EM1.put(EMetaData.EventsMetaData._ID, EMetaData.EVENTS_TABLE+ "." + EMetaData.EventsMetaData._ID);
		EM1.put(EMetaData.EventsMetaData.E_ID, EMetaData.EVENTS_TABLE+ "." + EMetaData.EventsMetaData.E_ID);
		EM1.put(EMetaData.EventsMetaData.USER_ID, EMetaData.EventsMetaData.USER_ID);
		
		EM1.put(EMetaData.EventsMetaData.IS_SPORTS_EVENT, EMetaData.EventsMetaData.IS_SPORTS_EVENT);
		EM1.put(EMetaData.EventsMetaData.STATUS, EMetaData.EventsMetaData.STATUS);
		EM1.put(EMetaData.EventsMetaData.IS_OWNER, EMetaData.EventsMetaData.IS_OWNER);
		EM1.put(EMetaData.EventsMetaData.TYPE, EMetaData.EventsMetaData.TYPE);
		EM1.put(EMetaData.EventsMetaData.IS_ALL_DAY, EMetaData.EventsMetaData.IS_ALL_DAY);
		EM1.put(EMetaData.EventsMetaData.IS_BIRTHDAY, EMetaData.EventsMetaData.IS_BIRTHDAY);
		
		EM1.put(EMetaData.EventsMetaData.CREATOR_FULLNAME, EMetaData.EventsMetaData.CREATOR_FULLNAME);
		EM1.put(EMetaData.EventsMetaData.CREATOR_CONTACT_ID, EMetaData.EventsMetaData.CREATOR_CONTACT_ID);
		
		EM1.put(EMetaData.EventsMetaData.TITLE, EMetaData.EventsMetaData.TITLE);
		EM1.put(EMetaData.EventsMetaData.ICON, EMetaData.EventsMetaData.ICON);
		EM1.put(EMetaData.EventsMetaData.COLOR, EMetaData.EventsMetaData.COLOR);
//		EM.put(EMetaData.EventsMetaData.TEXT_COLOR, EMetaData.EventsMetaData.TEXT_COLOR);//2012-10-24
		EM1.put(EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR, EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR);//2012-10-24
		EM1.put(EMetaData.EventsMetaData.DESC, EMetaData.EventsMetaData.DESC);
		
		EM1.put(EMetaData.EventsMetaData.LOCATION, EMetaData.EventsMetaData.LOCATION);
		EM1.put(EMetaData.EventsMetaData.ACCOMODATION, EMetaData.EventsMetaData.ACCOMODATION);
		
		EM1.put(EMetaData.EventsMetaData.COST, EMetaData.EventsMetaData.COST);
		EM1.put(EMetaData.EventsMetaData.TAKE_WITH_YOU, EMetaData.EventsMetaData.TAKE_WITH_YOU);
		EM1.put(EMetaData.EventsMetaData.GO_BY, EMetaData.EventsMetaData.GO_BY);
		
		EM1.put(EMetaData.EventsMetaData.COUNTRY, EMetaData.EventsMetaData.COUNTRY);
		EM1.put(EMetaData.EventsMetaData.CITY, EMetaData.EventsMetaData.CITY);
		EM1.put(EMetaData.EventsMetaData.STREET, EMetaData.EventsMetaData.STREET);
		EM1.put(EMetaData.EventsMetaData.ZIP, EMetaData.EventsMetaData.ZIP);
		
		EM1.put(EMetaData.EventsMetaData.TIMEZONE, EMetaData.EventsMetaData.TIMEZONE);
		EM1.put(EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS);
		EM1.put(EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS, EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS);
		
		EM1.put(EMetaData.EventsMetaData.REMINDER1, EMetaData.EventsMetaData.REMINDER1);
		EM1.put(EMetaData.EventsMetaData.REMINDER2, EMetaData.EventsMetaData.REMINDER2);
		EM1.put(EMetaData.EventsMetaData.REMINDER3, EMetaData.EventsMetaData.REMINDER3);
		
		EM1.put(EMetaData.EventsMetaData.ALARM1, EMetaData.EventsMetaData.ALARM1);
		EM1.put(EMetaData.EventsMetaData.ALARM2, EMetaData.EventsMetaData.ALARM2);
		EM1.put(EMetaData.EventsMetaData.ALARM3, EMetaData.EventsMetaData.ALARM3);
		

		EM1.put(EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS, EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS);
		EM1.put(EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS, EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS);
		
		EM1.put(EMetaData.EventsMetaData.ATTENDANT_1_COUNT, EMetaData.EventsMetaData.ATTENDANT_1_COUNT);
		EM1.put(EMetaData.EventsMetaData.ATTENDANT_2_COUNT, EMetaData.EventsMetaData.ATTENDANT_2_COUNT);
		EM1.put(EMetaData.EventsMetaData.ATTENDANT_0_COUNT, EMetaData.EventsMetaData.ATTENDANT_0_COUNT);
		EM1.put(EMetaData.EventsMetaData.ATTENDANT_4_COUNT, EMetaData.EventsMetaData.ATTENDANT_4_COUNT);
		
		EM1.put(EMetaData.EventsMetaData.INVITED, EMetaData.EventsMetaData.INVITED);
//		EM.put(EMetaData.EventsMetaData.ASSIGNED_CONTACTS, EMetaData.EventsMetaData.ASSIGNED_CONTACTS);
//		EM.put(EMetaData.EventsMetaData.ASSIGNED_GROUPS, EMetaData.EventsMetaData.ASSIGNED_GROUPS);
		EM1.put(EMetaData.EventsMetaData.MESSAGES_COUNT, EMetaData.EventsMetaData.MESSAGES_COUNT);
		EM1.put(EMetaData.EventsMetaData.NEW_MESSAGES_COUNT, EMetaData.EventsMetaData.NEW_MESSAGES_COUNT);
		EM1.put(EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS, EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS);
		
		EM1.put(EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY, EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY);
		EM1.put(EMetaData.EventsMetaData.POLL, EMetaData.EventsMetaData.POLL);
		EM1.put(EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME, EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME);
		EM1.put(EMetaData.EventsIndexesMetaData.DAY, EMetaData.EventsIndexesMetaData.DAY);
		
	}
	
	// Events day indexes table projection map
	private static HashMap<String, String> DEM;
	
	static{
		DEM = new HashMap<String, String>();
		DEM.put(EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID, EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID);
		DEM.put(EMetaData.EventsIndexesMetaData.DAY, EMetaData.EventsIndexesMetaData.DAY);
		DEM.put(EMetaData.EventsIndexesMetaData.MONTH, EMetaData.EventsIndexesMetaData.MONTH);
	}
	
	// Events day indexes table projection map
//		private static HashMap<String, String> IM;
		
//		static{
//			IM = new HashMap<String, String>();
//			IM.put(EMetaData.InvitedMetaData.EVENT_ID, EMetaData.InvitedMetaData.EVENT_ID);
//			IM.put(EMetaData.InvitedMetaData.GCID, EMetaData.InvitedMetaData.GCID);
//			IM.put(EMetaData.InvitedMetaData.GUID, EMetaData.InvitedMetaData.GUID);
//			IM.put(EMetaData.InvitedMetaData.MY_CONTACT_ID, EMetaData.InvitedMetaData.MY_CONTACT_ID);
//			IM.put(EMetaData.InvitedMetaData.STATUS, EMetaData.InvitedMetaData.STATUS);
//			IM.put(EMetaData.InvitedMetaData.NAME, EMetaData.InvitedMetaData.NAME);
//		}
	
	private static HashMap<String, String> AEM;
	
	static{
		AEM = new HashMap<String, String>();
		AEM.put(AlarmsMetaData.USER_ID, AlarmsMetaData.USER_ID);
		AEM.put(AlarmsMetaData.EVENT_ID, AlarmsMetaData.EVENT_ID);
		AEM.put(AlarmsMetaData.TIMESTAMP, AlarmsMetaData.TIMESTAMP);
		AEM.put(AlarmsMetaData.OFFSET, AlarmsMetaData.OFFSET);
		AEM.put(AlarmsMetaData.SENT, AlarmsMetaData.SENT);
	}
	
	
	
	// UriMatcher
	private static final UriMatcher mUriMatcher;

	private static final int ALL_EVENTS = 0;
	private static final int ONE_EVENTS = 1;
	private static final int DAY_INDEX = 2;
	private static final int EVENTS_ON_DATE = 3;
	private static final int EVENT_BY_EXTERNAL_ID = 4;
//	private static final int INVITED = 4;
	private static final int INDEXED_EVENTS = 5;
	private static final int UPDATE_EVENTS_NEW_MESSAGE_COUNT_AFTER_CHAT_POST = 6;
	private static final int ALARM = 7;
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENTS_TABLE, ALL_EVENTS);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENTS_TABLE+"/#", ONE_EVENTS);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENT_DAY_INDEX_TABLE, DAY_INDEX);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.events_on_date, EVENTS_ON_DATE);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENTS_TABLE + "/external/#", EVENT_BY_EXTERNAL_ID);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.indexed_events, INDEXED_EVENTS);
//		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.INVITED_TABLE, INVITED);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENTS_TABLE + "/increment_messages_attributes", UPDATE_EVENTS_NEW_MESSAGE_COUNT_AFTER_CHAT_POST);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.ALARMS_TABLE, ALARM);
	}
	// END UriMatcher
	
	// QUICK HACK
	public static final AtomicBoolean OUT_OF_DATE = new AtomicBoolean(false);
	
	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case ALL_EVENTS:
		case ONE_EVENTS:
			return EMetaData.EventsMetaData.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(this.getContext());
		return (mOpenHelper == null) ? false : true;
	}
	
	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String orderBy;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case ALL_EVENTS:
			qb.setTables(EMetaData.EVENTS_TABLE);
			qb.setProjectionMap(EM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? EMetaData.EventsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case ONE_EVENTS:
			qb.setTables(EMetaData.EVENTS_TABLE);
			qb.setProjectionMap(EM);
			qb.appendWhere(EMetaData.EventsMetaData._ID + "=" + uri.getPathSegments().get(1));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? EMetaData.EventsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
			
		case EVENTS_ON_DATE:
			qb.setProjectionMap(EM1);
			qb.setTables(EMetaData.EVENT_DAY_INDEX_TABLE + "," +  EMetaData.EVENTS_TABLE);
			qb.appendWhere(EMetaData.EVENTS_TABLE + "." + EMetaData.EventsMetaData._ID
					+"="
					+EMetaData.EVENT_DAY_INDEX_TABLE+ "." +EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? null : sortOrder; 
			break;
		case EVENT_BY_EXTERNAL_ID:
			qb.setTables(EMetaData.EVENTS_TABLE);
			qb.setProjectionMap(EM);
			qb.appendWhere(EMetaData.EventsMetaData.E_ID + "=" + uri.getPathSegments().get(2));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? EMetaData.EventsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
//		case INVITED:
//			qb.setTables(EMetaData.INVITED_TABLE);
//			qb.setProjectionMap(IM);
//			orderBy = (TextUtils.isEmpty(sortOrder)) ? null : sortOrder;
//			break;
		case ALARM:
			qb.setTables(EMetaData.ALARMS_TABLE);
			qb.setProjectionMap(AEM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? EMetaData.AlarmsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(mUriMatcher.match(uri)){
			case ALL_EVENTS:
				count = db.delete(EMetaData.EVENTS_TABLE, where, whereArgs);
				break;
			case ONE_EVENTS:
				String whereStr = EMetaData.EventsMetaData.E_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
				count = db.delete(EMetaData.EVENTS_TABLE, whereStr, whereArgs);
				break;
			case DAY_INDEX:
				count = db.delete(EMetaData.EVENT_DAY_INDEX_TABLE, where, whereArgs);
				break;
//			case INVITED:
//				count = db.delete(EMetaData.INVITED_TABLE, where, whereArgs);
//				break;
			case ALARM:
				count = db.delete(EMetaData.ALARMS_TABLE, where, whereArgs);
				break;
			default:
				
				throw new IllegalArgumentException("Unknow URI "+uri);
		}
		OUT_OF_DATE.set(true);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = 0;
		Uri insUri;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case ALL_EVENTS:
			rowId = db.replace(EMetaData.EVENTS_TABLE, EMetaData.EventsMetaData.E_ID, values);
			insUri = ContentUris.withAppendedId(EMetaData.EventsMetaData.CONTENT_URI, rowId);
			break;
		case DAY_INDEX:
			rowId = db.replace(EMetaData.EVENT_DAY_INDEX_TABLE, "", values); 
			insUri = ContentUris.withAppendedId(EMetaData.EventsIndexesMetaData.CONTENT_URI, rowId);
			break;
		case INDEXED_EVENTS:
			insUri = insertIndexedEvent(db, values);
			break;
		case ALARM:
			rowId = db.replace(EMetaData.ALARMS_TABLE, EMetaData.AlarmsMetaData.ALARM_ID, values);
			insUri = ContentUris.withAppendedId(EMetaData.AlarmsMetaData.CONTENT_URI, rowId);
			break;	
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		if (insUri != null) {
			OUT_OF_DATE.set(true);
			getContext().getContentResolver().notifyChange(insUri, null);
		}
		return insUri;
	}
	
	
	private Uri insertIndexedEvent(SQLiteDatabase db, ContentValues values) {
		Calendar eventDayStart = Calendar.getInstance();
		Calendar eventTimeEnd = Calendar.getInstance();
		DateTimeUtils dateTimeUtils = new DateTimeUtils(getContext());
		
		Long millisStart = values.getAsLong(EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS);
		Long millisEnd = values.getAsLong(EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS);
		
		if(millisStart == null || millisEnd == null) return null; //dont insert if crucial parts are missing
		eventTimeEnd.setTimeInMillis(millisEnd);
		eventTimeEnd.add(Calendar.MILLISECOND, -1);
		eventDayStart.setTimeInMillis(millisStart);
		Calendar eventTimeStart = (Calendar) eventDayStart.clone();
		eventDayStart.set(Calendar.HOUR_OF_DAY, 0);
		eventDayStart.set(Calendar.MINUTE, 0);
		eventDayStart.set(Calendar.SECOND, 0);
		eventDayStart.set(Calendar.MILLISECOND, 0);
		
		Calendar eventDayEnd = (Calendar) eventDayStart.clone();
		eventDayEnd.set(Calendar.HOUR_OF_DAY, eventDayEnd.getActualMaximum(Calendar.HOUR_OF_DAY));
		eventDayEnd.set(Calendar.MINUTE, eventDayEnd.getActualMaximum(Calendar.MINUTE));
		eventDayEnd.set(Calendar.SECOND, eventDayEnd.getActualMaximum(Calendar.SECOND));
		eventDayEnd.set(Calendar.MILLISECOND, eventDayEnd.getActualMaximum(Calendar.MILLISECOND));
		
		long rowId = db.insert(EMetaData.EVENTS_TABLE, null, values);
		if (rowId < 0) return null;
		Uri insUri = ContentUris.withAppendedId(EMetaData.EventsMetaData.CONTENT_URI, rowId);
		
		String event_internal_id = StringValueUtils.valueOf(rowId);
		String ext_id = values.getAsString(EMetaData.EventsMetaData.E_ID);

			do {
				insertEventDayIndexRow(db, event_internal_id, ext_id, eventDayStart, eventTimeStart.before(eventDayStart), eventTimeEnd.after(eventDayEnd), millisStart, millisEnd, dateTimeUtils);
				eventDayStart.add(Calendar.DAY_OF_MONTH, 1);
			} while (eventDayStart.before(eventTimeEnd));
		return insUri;
	}

	private void insertEventDayIndexRow(SQLiteDatabase db,
		String event_internal_id, String ext_id, Calendar eventDayStart, boolean yesterday, boolean tomorrow, long startTime, long endTime, DateTimeUtils dateTimeUtils) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_EXTERNAL_ID, ext_id);
		Date time = eventDayStart.getTime();
		

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY, day_index_formatter.format(time));
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH, month_index_formatter.format(time));
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_TIME_START, yesterday ? EventsProvider.EMetaData.EventsIndexesMetaData.NOT_TODAY  : dateTimeUtils.formatTime(startTime));
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_TIME_END, tomorrow ? EventsProvider.EMetaData.EventsIndexesMetaData.NOT_TODAY : dateTimeUtils.formatTime(endTime));
		db.insert(EventsProvider.EMetaData.EVENT_DAY_INDEX_TABLE, null, cv);
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		int count;

		switch (mUriMatcher.match(uri)) {
		case ALL_EVENTS:
			count = db.update(EMetaData.EVENTS_TABLE, values, where, whereArgs);
			break;
		case ONE_EVENTS:
			String whereStr = EMetaData.EventsMetaData._ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(EMetaData.EVENTS_TABLE, values, whereStr, whereArgs);
			break;
		case UPDATE_EVENTS_NEW_MESSAGE_COUNT_AFTER_CHAT_POST:
			String e_id = null;
			if (values != null) {
				e_id = values.getAsString(EMetaData.EventsMetaData.E_ID);
				if (!e_id.matches("[0-9]*")) {
					throw new IllegalArgumentException("Event Id not number " + uri);
				}
			} else {
				throw new IllegalArgumentException("Content Values equals null " + uri);
			}
			String sql = "UPDATE `" + EMetaData.EVENTS_TABLE + "` SET `" + EMetaData.EventsMetaData.NEW_MESSAGES_COUNT + "` = `"
					+ EMetaData.EventsMetaData.NEW_MESSAGES_COUNT + "`+1";
			String whereString = " WHERE `" + EMetaData.EventsMetaData.E_ID + "` = '" + e_id + "'";
			sql += whereString;
			Cursor cursor = db.rawQuery(sql, whereArgs);
			cursor.moveToFirst();
			cursor.close(); 
			count = 1;
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		OUT_OF_DATE.set(true);
		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values){
		int numIns = 0;
		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();
		
		switch(mUriMatcher.match(uri)){
		case INDEXED_EVENTS:
			sqlDB.beginTransaction();
		    try {

		        for (ContentValues cv : values) {
		            Uri insUri = insertIndexedEvent(sqlDB, cv);
		            if (insUri == null) {
		                throw new SQLException("Failed to insert row into " + uri);
		            }
		        }
		        sqlDB.setTransactionSuccessful();
				OUT_OF_DATE.set(true);
		        getContext().getContentResolver().notifyChange(EMetaData.EventsMetaData.CONTENT_URI, null);
		        getContext().getContentResolver().notifyChange(EMetaData.EventsIndexesMetaData.CONTENT_URI, null);
		        numIns = values.length;
		    } finally {
		        sqlDB.endTransaction();
		    }
			break;
		default:
			
			throw new IllegalArgumentException("Unknow URI "+uri);
	}
	    
	    
		return numIns ;
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, EMetaData.DATABASE_NAME, null, EMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query =	"CREATE TABLE "
				+EMetaData.EVENTS_TABLE+" ("
				+EMetaData.EventsMetaData._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+EMetaData.EventsMetaData.E_ID+" INTEGER UNIQUE ON CONFLICT IGNORE,"
				+EMetaData.EventsMetaData.USER_ID+" INTEGER ,"
				
				+EMetaData.EventsMetaData.IS_SPORTS_EVENT+" TEXT ,"
				+EMetaData.EventsMetaData.STATUS+" INTEGER ,"
				+EMetaData.EventsMetaData.IS_OWNER+" TEXT ,"
				+EMetaData.EventsMetaData.TYPE+" TEXT ,"
				+EMetaData.EventsMetaData.IS_ALL_DAY+" TEXT ,"
				+EMetaData.EventsMetaData.IS_BIRTHDAY+" TEXT ,"
				
				+EMetaData.EventsMetaData.CREATOR_FULLNAME+" TEXT ,"
				+EMetaData.EventsMetaData.CREATOR_CONTACT_ID+" INTEGER ,"
				
				+EMetaData.EventsMetaData.TITLE+" TEXT ,"
				+EMetaData.EventsMetaData.ICON+" TEXT ,"
				+EMetaData.EventsMetaData.COLOR+" TEXT ,"
//				+EMetaData.EventsMetaData.TEXT_COLOR+" TEXT ,"//2012-10-24
				+EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR+" TEXT ,"
				+EMetaData.EventsMetaData.DESC+" TEXT ,"
				
				+EMetaData.EventsMetaData.LOCATION+" TEXT ,"
				+EMetaData.EventsMetaData.ACCOMODATION+" TEXT ,"
				
				+EMetaData.EventsMetaData.COST+" TEXT ,"
				+EMetaData.EventsMetaData.TAKE_WITH_YOU+" TEXT ,"
				+EMetaData.EventsMetaData.GO_BY+" TEXT ,"
				
				+EMetaData.EventsMetaData.COUNTRY+" TEXT ,"
				+EMetaData.EventsMetaData.CITY+" TEXT ,"
				+EMetaData.EventsMetaData.STREET+" TEXT ,"
				+EMetaData.EventsMetaData.ZIP+" TEXT ,"
				
				+EMetaData.EventsMetaData.TIMEZONE+" TEXT ,"
				+EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS+" INTEGER ,"
				+EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS+" INTEGER ,"
				
				+EMetaData.EventsMetaData.REMINDER1+" INTEGER ,"
				+EMetaData.EventsMetaData.REMINDER2+" INTEGER ,"
				+EMetaData.EventsMetaData.REMINDER3+" INTEGER ,"
				
				+EMetaData.EventsMetaData.ALARM1+" TEXT ,"
				+EMetaData.EventsMetaData.ALARM2+" TEXT ,"
				+EMetaData.EventsMetaData.ALARM3+" TEXT ,"
				
				+EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS+" INTEGER ,"
				+EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS+" INTEGER ,"

				
				+EMetaData.EventsMetaData.ATTENDANT_1_COUNT+" TEXT ,"
				+EMetaData.EventsMetaData.ATTENDANT_2_COUNT+" TEXT ,"
				+EMetaData.EventsMetaData.ATTENDANT_0_COUNT+" TEXT ,"
				+EMetaData.EventsMetaData.ATTENDANT_4_COUNT+" TEXT ,"
				
				+EMetaData.EventsMetaData.INVITED+" TEXT ,"
//				+EMetaData.EventsMetaData.ASSIGNED_CONTACTS+" TEXT ,"
//				+EMetaData.EventsMetaData.ASSIGNED_GROUPS+" TEXT ,"
				+EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY+" INTEGER DEFAULT 0, "
				+EMetaData.EventsMetaData.MESSAGES_COUNT+" INTEGER DEFAULT 0,"
				+EMetaData.EventsMetaData.NEW_MESSAGES_COUNT+" INTEGER DEFAULT 0,"
				+EMetaData.EventsMetaData.LAST_MESSAGE_DATE_TIME_UTC_MILISECONDS+" INTEGER, "
				+EMetaData.EventsMetaData.POLL+" TEXT, "
				+EMetaData.EventsMetaData.SELECTED_EVENT_POLLS_TIME+" TEXT "
				+")";
			db.execSQL(query);
			
			query = "CREATE TABLE "
					+EMetaData.EVENT_DAY_INDEX_TABLE
					+ " ("
					+ EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID + " TEXT ,"
					+ EMetaData.EventsIndexesMetaData.EVENT_EXTERNAL_ID + " TEXT ,"
					+ EMetaData.EventsIndexesMetaData.DAY + " TEXT , "
					+ EMetaData.EventsIndexesMetaData.MONTH + " TEXT , "
					+ EMetaData.EventsIndexesMetaData.DAY_TIME_START + " TEXT , "
					+ EMetaData.EventsIndexesMetaData.DAY_TIME_END + " TEXT , "
					+ "PRIMARY KEY (" + EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID + "),"
					+ "UNIQUE (" + EMetaData.EventsIndexesMetaData.EVENT_INTERNAL_ID + ", " + EMetaData.EventsIndexesMetaData.DAY + ") ON CONFLICT IGNORE"
					+")";
			db.execSQL(query);
			
			query = "CREATE INDEX events_days_month ON events_days(month)";
			db.execSQL(query);
			query = "CREATE INDEX events_days_day ON events_days(day)";
			db.execSQL(query);
			query = "CREATE INDEX events_days_event_id ON events_days(event_id)";
			db.execSQL(query);
			
			query = "CREATE TABLE " +
			EMetaData.ALARMS_TABLE +
			" ("+
			AlarmsMetaData.ALARM_ID + " VARCHAR PRIMARY KEY ON CONFLICT REPLACE, "+
			AlarmsMetaData.USER_ID + " INTEGER, " +
			AlarmsMetaData.EVENT_ID + " INTEGER, " +
			AlarmsMetaData.TIMESTAMP + " INTEGER, " +
			AlarmsMetaData.OFFSET + " INTEGER, " +
			AlarmsMetaData.SENT + " TEXT " +
			")";
			db.execSQL(query);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}
}
