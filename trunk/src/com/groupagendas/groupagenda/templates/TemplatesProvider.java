package com.groupagendas.groupagenda.templates;

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

/**
 * Extended ContentProvider class.
 * @author meska.lt@gmail.com
 *
 */
public class TemplatesProvider extends ContentProvider {
	public static DatabaseHelper mOpenHelper;

	public static class TMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.templates.TemplatesProvider";
		public static final String DATABASE_NAME = "templates.sqlite";

		public static final int DATABASE_VERSION = 1;

		public static final String TEMPLATES_TABLE = "templates";
		
		public static final class TemplatesMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TEMPLATES_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.events_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.events_item";

			public static final String T_ID 	= "template_id";
			public static final String _ID 	= "_id";
			
			public static final String IS_SPORTS_EVENT	= "is_sports_event";
			public static final String IS_ALL_DAY = "is_all_day";
			
			public static final String T_TITLE	= "template_title";
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
			public static final String TIMEZONE_IN_USE = "timezone_in_use";
			public static final String TIME_START = "time_start";
			public static final String TIME_END = "time_end";
			
			public static final String REMINDER1 = "reminder1";
			public static final String REMINDER2 = "reminder2";
			public static final String REMINDER3 = "reminder3";
			
			public static final String ALARM1 = "alarm1";
			public static final String ALARM2 = "alarm2";
			public static final String ALARM3 = "alarm3";
			
			public static final String CREATED = "created";
			public static final String MODIFIED = "modified";
			
//			public static final String ATTENDANT_1_COUNT = "attendant_1_count";
//			public static final String ATTENDANT_2_COUNT = "attendant_2_count";
//			public static final String ATTENDANT_0_COUNT = "attendant_0_count";
//			public static final String ATTENDANT_4_COUNT = "attendant_4_count";
			
			public static final String ASSIGNED_CONTACTS = "contacts";
			public static final String ASSIGNED_GROUPS = "groups";
			public static final String INVITED = "invited";
			
//			public static final String NEED_UPDATE = "need_update";
			
			public static final String DEFAULT_SORT_ORDER = TIME_START + " ASC";
			public static final String UPLOADED_SUCCESSFULLY = "uploaded";
		}
	}
	
	public static HashMap<String, String> TM;
	
	/* projection map */
	static {
		TM = new HashMap<String, String>();
		
		TM.put(TMetaData.TemplatesMetaData.T_ID, TMetaData.TemplatesMetaData.T_ID);

		TM.put(TMetaData.TemplatesMetaData.IS_SPORTS_EVENT, TMetaData.TemplatesMetaData.IS_SPORTS_EVENT);
		TM.put(TMetaData.TemplatesMetaData.TIMEZONE_IN_USE, TMetaData.TemplatesMetaData.TIMEZONE_IN_USE);
		TM.put(TMetaData.TemplatesMetaData.IS_ALL_DAY, TMetaData.TemplatesMetaData.IS_ALL_DAY);

		TM.put(TMetaData.TemplatesMetaData.T_TITLE, TMetaData.TemplatesMetaData.T_TITLE);
		TM.put(TMetaData.TemplatesMetaData.TITLE, TMetaData.TemplatesMetaData.TITLE);
		TM.put(TMetaData.TemplatesMetaData.ICON, TMetaData.TemplatesMetaData.ICON);
		TM.put(TMetaData.TemplatesMetaData.COLOR, TMetaData.TemplatesMetaData.COLOR);
		TM.put(TMetaData.TemplatesMetaData.DESC, TMetaData.TemplatesMetaData.DESC);

		TM.put(TMetaData.TemplatesMetaData.LOCATION, TMetaData.TemplatesMetaData.LOCATION);
		TM.put(TMetaData.TemplatesMetaData.ACCOMODATION, TMetaData.TemplatesMetaData.ACCOMODATION);

		TM.put(TMetaData.TemplatesMetaData.COST, TMetaData.TemplatesMetaData.COST);
		TM.put(TMetaData.TemplatesMetaData.TAKE_WITH_YOU, TMetaData.TemplatesMetaData.TAKE_WITH_YOU);
		TM.put(TMetaData.TemplatesMetaData.GO_BY, TMetaData.TemplatesMetaData.GO_BY);

		TM.put(TMetaData.TemplatesMetaData.COUNTRY, TMetaData.TemplatesMetaData.COUNTRY);
		TM.put(TMetaData.TemplatesMetaData.CITY, TMetaData.TemplatesMetaData.CITY);
		TM.put(TMetaData.TemplatesMetaData.STREET, TMetaData.TemplatesMetaData.STREET);
		TM.put(TMetaData.TemplatesMetaData.ZIP, TMetaData.TemplatesMetaData.ZIP);

		TM.put(TMetaData.TemplatesMetaData.TIMEZONE, TMetaData.TemplatesMetaData.TIMEZONE);
		TM.put(TMetaData.TemplatesMetaData.TIME_START, TMetaData.TemplatesMetaData.TIME_START);
		TM.put(TMetaData.TemplatesMetaData.TIME_END, TMetaData.TemplatesMetaData.TIME_END);

		TM.put(TMetaData.TemplatesMetaData.REMINDER1, TMetaData.TemplatesMetaData.REMINDER1);
		TM.put(TMetaData.TemplatesMetaData.REMINDER2, TMetaData.TemplatesMetaData.REMINDER2);
		TM.put(TMetaData.TemplatesMetaData.REMINDER3, TMetaData.TemplatesMetaData.REMINDER3);

		TM.put(TMetaData.TemplatesMetaData.ALARM1, TMetaData.TemplatesMetaData.ALARM1);
		TM.put(TMetaData.TemplatesMetaData.ALARM2, TMetaData.TemplatesMetaData.ALARM2);
		TM.put(TMetaData.TemplatesMetaData.ALARM3, TMetaData.TemplatesMetaData.ALARM3);

		TM.put(TMetaData.TemplatesMetaData.CREATED, TMetaData.TemplatesMetaData.CREATED);
		TM.put(TMetaData.TemplatesMetaData.MODIFIED, TMetaData.TemplatesMetaData.MODIFIED);
		
		TM.put(TMetaData.TemplatesMetaData.ASSIGNED_CONTACTS, TMetaData.TemplatesMetaData.ASSIGNED_CONTACTS);
		TM.put(TMetaData.TemplatesMetaData.ASSIGNED_GROUPS, TMetaData.TemplatesMetaData.ASSIGNED_GROUPS);
		TM.put(TMetaData.TemplatesMetaData.INVITED, TMetaData.TemplatesMetaData.INVITED);
	}
	
	/* UriMatcher */
	private static final UriMatcher mUriMatcher;

	public static final int ALL_TEMPLATES = 0;
	public static final int SINGLE_TEMPLATE = 1;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(TMetaData.AUTHORITY, TMetaData.TEMPLATES_TABLE, ALL_TEMPLATES);
		mUriMatcher.addURI(TMetaData.AUTHORITY, TMetaData.TEMPLATES_TABLE+"/#", SINGLE_TEMPLATE);
	}
	
	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case ALL_TEMPLATES:
		case SINGLE_TEMPLATE:
			return TMetaData.TemplatesMetaData.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return (mOpenHelper == null) ? false : true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String orderBy;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (mUriMatcher.match(uri)) {
			case ALL_TEMPLATES:
				qb.setTables(TMetaData.TEMPLATES_TABLE);
				qb.setProjectionMap(TM);
				orderBy = (TextUtils.isEmpty(sortOrder)) ? TMetaData.TemplatesMetaData.DEFAULT_SORT_ORDER : sortOrder;
				break;
			case SINGLE_TEMPLATE:
				qb.setTables(TMetaData.TEMPLATES_TABLE);
				qb.setProjectionMap(TM);
				qb.appendWhere(TMetaData.TemplatesMetaData.T_ID + "=" + uri.getPathSegments().get(1));
				orderBy = (TextUtils.isEmpty(sortOrder)) ? TMetaData.TemplatesMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
			case ALL_TEMPLATES:
				count = db.delete(TMetaData.TEMPLATES_TABLE, where, whereArgs);
				break;
			case SINGLE_TEMPLATE:
				String whereStr = TMetaData.TemplatesMetaData.T_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
				count = db.delete(TMetaData.TEMPLATES_TABLE, whereStr, whereArgs);
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
		case ALL_TEMPLATES:
			rowId = db.replace(TMetaData.TEMPLATES_TABLE, TMetaData.TemplatesMetaData.T_ID, values);
			insUri = ContentUris.withAppendedId(TMetaData.TemplatesMetaData.CONTENT_URI, rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		if(insUri != null){
			getContext().getContentResolver().notifyChange(insUri, null);
		}
		return insUri;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		int count;

		switch (mUriMatcher.match(uri)) {
		case ALL_TEMPLATES:
			count = db.update(TMetaData.TEMPLATES_TABLE, values, where, whereArgs);
			break;
		case SINGLE_TEMPLATE:
			String whereStr = TMetaData.TemplatesMetaData.T_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(TMetaData.TEMPLATES_TABLE, values, whereStr, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		return count;
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, TMetaData.DATABASE_NAME, null, TMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query =	"CREATE TABLE "
				+TMetaData.TEMPLATES_TABLE+" ("
				+TMetaData.TemplatesMetaData._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+TMetaData.TemplatesMetaData.T_ID+" INTEGER UNIQUE ON CONFLICT IGNORE,"
				
				+TMetaData.TemplatesMetaData.IS_SPORTS_EVENT+" TEXT ,"
				+TMetaData.TemplatesMetaData.IS_ALL_DAY +" INTEGER ,"
				
				+TMetaData.TemplatesMetaData.TITLE+" TEXT ,"
				+TMetaData.TemplatesMetaData.T_TITLE+" TEXT ,"
				+TMetaData.TemplatesMetaData.ICON+" TEXT ,"
				+TMetaData.TemplatesMetaData.COLOR+" TEXT ,"
				+TMetaData.TemplatesMetaData.DESC+" TEXT ,"
				
				+TMetaData.TemplatesMetaData.LOCATION+" TEXT ,"
				+TMetaData.TemplatesMetaData.ACCOMODATION+" TEXT ,"
				
				+TMetaData.TemplatesMetaData.COST+" TEXT ,"
				+TMetaData.TemplatesMetaData.TAKE_WITH_YOU+" TEXT ,"
				+TMetaData.TemplatesMetaData.GO_BY+" TEXT ,"
				
				+TMetaData.TemplatesMetaData.COUNTRY+" TEXT ,"
				+TMetaData.TemplatesMetaData.CITY+" TEXT ,"
				+TMetaData.TemplatesMetaData.STREET+" TEXT ,"
				+TMetaData.TemplatesMetaData.ZIP+" TEXT ,"
				
				+TMetaData.TemplatesMetaData.TIMEZONE+" TEXT ,"
				+TMetaData.TemplatesMetaData.TIMEZONE_IN_USE+" INTEGER ,"
				+TMetaData.TemplatesMetaData.TIME_START+" INTEGER ,"
				+TMetaData.TemplatesMetaData.TIME_END+" INTEGER ,"
				
				+TMetaData.TemplatesMetaData.REMINDER1+" INTEGER ,"
				+TMetaData.TemplatesMetaData.REMINDER2+" INTEGER ,"
				+TMetaData.TemplatesMetaData.REMINDER3+" INTEGER ,"
				
				+TMetaData.TemplatesMetaData.ALARM1+" INTEGER ,"
				+TMetaData.TemplatesMetaData.ALARM2+" INTEGER ,"
				+TMetaData.TemplatesMetaData.ALARM3+" INTEGER ,"
				
				+TMetaData.TemplatesMetaData.CREATED+" TEXT ,"
				+TMetaData.TemplatesMetaData.MODIFIED+" TEXT ,"
				
//				+EMetaData.EventsMetaData.ATTENDANT_1_COUNT+" TEXT ,"
//				+EMetaData.EventsMetaData.ATTENDANT_2_COUNT+" TEXT ,"
//				+EMetaData.EventsMetaData.ATTENDANT_0_COUNT+" TEXT ,"
//				+EMetaData.EventsMetaData.ATTENDANT_4_COUNT+" TEXT ,"
				
				+TMetaData.TemplatesMetaData.UPLOADED_SUCCESSFULLY+" INTEGER ,"
				
				+TMetaData.TemplatesMetaData.ASSIGNED_CONTACTS+" TEXT ,"
				+TMetaData.TemplatesMetaData.ASSIGNED_GROUPS+" TEXT ,"
				+TMetaData.TemplatesMetaData.INVITED+" TEXT )";
//				+EMetaData.EventsMetaData.NEED_UPDATE+" INTEGER DEFAULT 0 )";
				
			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}
}
