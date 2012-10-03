package com.groupagendas.groupagenda.events;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class EventsProvider extends ContentProvider{
	private DatabaseHelper mOpenHelper;
	
	public static class EMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.events.EventsProvider";
		public static final String DATABASE_NAME = "events.sqlite";

		public static final int DATABASE_VERSION = 1;

		public static final String EVENTS_TABLE = "events";
		public static final String EVENT_DAY_INDEX_TABLE = "events_days";
		public static final String INVITED_TABLE = "invited";
		
		private static final String events_on_date = "events_on_date";
		
		public static final Uri EVENTS_ON_DATE_URI = Uri.parse("content://" + AUTHORITY + "/" + events_on_date);
		
		
		public static final class EventsIndexesMetaData{
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENT_DAY_INDEX_TABLE);	
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.groupagendas.events_days_item";
			
			public static final String DAY_COLUMN_FORMAT = "yyyy-MM-dd";
			public static final String MONTH_COLUMN_FORMAT = "yyyy-MM";
			
			public static final String EVENT_ID = "event_internal_id";
			public static final String DAY = "day";
			public static final String MONTH = "month";
			
		}
		
		public static final class InvitedMetaData{
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + INVITED_TABLE);	
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.groupagendas.invited_item";
			
			public static final String EVENT_ID = "event_internal_id";
			
			public static final String STATUS = "status";
			public static final String MY_CONTACT_ID = "my_contact_id";
			public static final String GCID = "gcid";
			public static final String GUID = "guid";
			public static final String NAME = "gname";

		}
		
		public static final class EventsMetaData implements BaseColumns{
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.events_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.events_item";
			

			public static final String E_ID 	= "event_id";
			public static final String USER_ID 	= "user_id";
			
			public static final String IS_SPORTS_EVENT	= "is_sports_event";
			public static final String STATUS 	= "status";
			public static final String IS_OWNER = "is_owner";
			public static final String TYPE 	= "type";
			
			public static final String CREATOR_FULLNAME		= "creator_fullname";
			public static final String CREATOR_CONTACT_ID	= "creator_contact_id";
			
			
			public static final String TITLE	= "title";
			public static final String ICON		= "icon";
			public static final String COLOR 	= "color";
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
			
//			public static final String ASSIGNED_CONTACTS = "assigned_contacts";
//			public static final String ASSIGNED_GROUPS = "assigned_groups";
			
			public static final String UPLOADED_SUCCESSFULLY = "uploaded";
			
			public static final String DEFAULT_SORT_ORDER = TIME_START_UTC_MILLISECONDS+" ASC";
			public static final String IS_BIRTHDAY = "is_birthday";
			public static final String IS_ALL_DAY = "is_all_day";
			public static final String MESSAGES_COUNT = "messages_count";
			
		}
	}
	
	// Events Table Projection Map
	private static HashMap<String, String> EM;
	
	static {
		EM = new HashMap<String, String>();
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
		
		
//		EM.put(EMetaData.EventsMetaData.ASSIGNED_CONTACTS, EMetaData.EventsMetaData.ASSIGNED_CONTACTS);
//		EM.put(EMetaData.EventsMetaData.ASSIGNED_GROUPS, EMetaData.EventsMetaData.ASSIGNED_GROUPS);
		EM.put(EMetaData.EventsMetaData.MESSAGES_COUNT, EMetaData.EventsMetaData.MESSAGES_COUNT);
		
		EM.put(EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY, EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY);
		
	}
	// END Table Projection Map
	
	// Events day indexes table projection map
	private static HashMap<String, String> DEM;
	
	static{
		DEM = new HashMap<String, String>();
		DEM.put(EMetaData.EventsIndexesMetaData.EVENT_ID, EMetaData.EventsIndexesMetaData.EVENT_ID);
		DEM.put(EMetaData.EventsIndexesMetaData.DAY, EMetaData.EventsIndexesMetaData.DAY);
		DEM.put(EMetaData.EventsIndexesMetaData.MONTH, EMetaData.EventsIndexesMetaData.MONTH);
	}
	
	// Events day indexes table projection map
		private static HashMap<String, String> IM;
		
		static{
			IM = new HashMap<String, String>();
			IM.put(EMetaData.InvitedMetaData.EVENT_ID, EMetaData.InvitedMetaData.EVENT_ID);
			IM.put(EMetaData.InvitedMetaData.GCID, EMetaData.InvitedMetaData.GCID);
			IM.put(EMetaData.InvitedMetaData.GUID, EMetaData.InvitedMetaData.GUID);
			IM.put(EMetaData.InvitedMetaData.MY_CONTACT_ID, EMetaData.InvitedMetaData.MY_CONTACT_ID);
			IM.put(EMetaData.InvitedMetaData.STATUS, EMetaData.InvitedMetaData.STATUS);
			IM.put(EMetaData.InvitedMetaData.NAME, EMetaData.InvitedMetaData.NAME);
		}
	
	
	
	// UriMatcher
	private static final UriMatcher mUriMatcher;

	private static final int ALL_EVENTS = 0;
	private static final int ONE_EVENTS = 1;
	private static final int DAY_INDEX = 2;
	private static final int EVENTS_ON_DATE = 3;
	private static final int INVITED = 4;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENTS_TABLE, ALL_EVENTS);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENTS_TABLE+"/#", ONE_EVENTS);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.EVENT_DAY_INDEX_TABLE, DAY_INDEX);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.events_on_date, EVENTS_ON_DATE);
		mUriMatcher.addURI(EMetaData.AUTHORITY, EMetaData.INVITED_TABLE, INVITED);
	}
	// END UriMatcher
	
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
			qb.setProjectionMap(EM);
			qb.setTables(EMetaData.EVENT_DAY_INDEX_TABLE + "," +  EMetaData.EVENTS_TABLE);
			qb.appendWhere(EMetaData.EventsMetaData._ID
					+"="
					+EMetaData.EventsIndexesMetaData.EVENT_ID);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? EMetaData.EventsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
//		case EVENTS_BETWEEN_DATES:
//			orderBy = null;
//			break;
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
			case INVITED:
				count = db.delete(EMetaData.INVITED_TABLE, where, whereArgs);
				break;
			default:
				
				throw new IllegalArgumentException("Unknow URI "+uri);
		}
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
		case INVITED:
			rowId = db.replace(EMetaData.INVITED_TABLE, "", values); 
			insUri = ContentUris.withAppendedId(EMetaData.InvitedMetaData.CONTENT_URI, rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(insUri, null);
		return insUri;
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
			String whereStr = EMetaData.EventsMetaData.E_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(EMetaData.EVENTS_TABLE, values, whereStr, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		return count;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, EMetaData.DATABASE_NAME, null, EMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query =	"CREATE TABLE "
				+EMetaData.EVENTS_TABLE+" ("
				+EMetaData.EventsMetaData._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+EMetaData.EventsMetaData.E_ID+" INTEGER,"
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
				
//				+EMetaData.EventsMetaData.ASSIGNED_CONTACTS+" TEXT ,"
//				+EMetaData.EventsMetaData.ASSIGNED_GROUPS+" TEXT ,"
				+EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY+" INTEGER DEFAULT 0, "
				+EMetaData.EventsMetaData.MESSAGES_COUNT+" INTEGER DEFAULT 0 "
				+")";
			db.execSQL(query);
			
			query = "CREATE TABLE "
					+EMetaData.EVENT_DAY_INDEX_TABLE
					+ " ("
					+ EMetaData.EventsIndexesMetaData.EVENT_ID + " TEXT ,"
					+ EMetaData.EventsIndexesMetaData.DAY + " TEXT , "
					+ EMetaData.EventsIndexesMetaData.MONTH + " TEXT , "
					+ "PRIMARY KEY (" + EMetaData.EventsIndexesMetaData.EVENT_ID + ", " + EMetaData.EventsIndexesMetaData.DAY + ") ON CONFLICT REPLACE"
					+")";
			db.execSQL(query);
			
			query = "CREATE TABLE "
					+EMetaData.INVITED_TABLE
					+ " ("
					+ EMetaData.InvitedMetaData.EVENT_ID + " INTEGER ,"
					+ EMetaData.InvitedMetaData.GCID + " INTEGER ,"
					+ EMetaData.InvitedMetaData.GUID + " INTEGER ,"
					+ EMetaData.InvitedMetaData.MY_CONTACT_ID + " INTEGER ,"
					+ EMetaData.InvitedMetaData.STATUS + " INTEGER, "
					+ EMetaData.InvitedMetaData.NAME + " TEXT, "
					+  "PRIMARY KEY (" + EMetaData.InvitedMetaData.EVENT_ID + ") ON CONFLICT REPLACE"
					+")";
			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}
}
