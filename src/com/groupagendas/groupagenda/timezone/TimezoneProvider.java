package com.groupagendas.groupagenda.timezone;

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

import com.groupagendas.groupagenda.contacts.ContactsProvider.CMetaData;

public class TimezoneProvider extends ContentProvider {
	private DatabaseHelper mOpenHelper;
	
	public static class TMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.timezone.TimezoneProvider";
		public static final String DATABASE_NAME = "timezones.sqlite";

		public static final int DATABASE_VERSION = 1;

		public static final String TIMEZONE_TABLE = "static_timezone";
		
		
		public static final class TimezoneMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TIMEZONE_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.timezone_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.timezomne_item";

			public static final String _ID		= "id";
			public static final String TIMEZONE		= "timezone";
			public static final String CITY			= "city";
			public static final String COUNTRY		= "country";
			public static final String COUNTRY2		= "country2";
			public static final String COUNTRY_CODE	= "country_code";
			public static final String ALTNAME		= "timezone_altname";
			public static final String SORT			= "sort";
			
			public static final String DEFAULT_SORT_ORDER = "";
			
		}
	}
	
	// Table Projection Map
	private static HashMap<String, String> TM;
	
	static {
		TM = new HashMap<String, String>();
		TM.put(TMetaData.TimezoneMetaData.TIMEZONE, TMetaData.TimezoneMetaData.TIMEZONE);
		TM.put(TMetaData.TimezoneMetaData.CITY, TMetaData.TimezoneMetaData.CITY);
		TM.put(TMetaData.TimezoneMetaData.COUNTRY, TMetaData.TimezoneMetaData.COUNTRY);
		TM.put(TMetaData.TimezoneMetaData.COUNTRY2, TMetaData.TimezoneMetaData.COUNTRY2);
		TM.put(TMetaData.TimezoneMetaData.COUNTRY_CODE, TMetaData.TimezoneMetaData.COUNTRY_CODE);
		TM.put(TMetaData.TimezoneMetaData.ALTNAME, TMetaData.TimezoneMetaData.ALTNAME);		
	}
	// END Table Projection Map
	
	// UriMatcher
	private static final UriMatcher mUriMatcher;

	private static final int TIMEZONE  = 0;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(TMetaData.AUTHORITY, TMetaData.TIMEZONE_TABLE, TIMEZONE);
	}
	// END UriMatcher
	
	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case TIMEZONE:
			return TMetaData.TimezoneMetaData.CONTENT_TYPE;
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
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case TIMEZONE:
			qb.setTables(TMetaData.TIMEZONE_TABLE);
			qb.setProjectionMap(TM);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(mUriMatcher.match(uri)){
			case TIMEZONE:
				count = db.delete(TimezoneProvider.TMetaData.TIMEZONE_TABLE, where, whereArgs);
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
		case TIMEZONE:
			rowId = db.replace(CMetaData.CONTACTS_TABLE, CMetaData.ContactsMetaData.C_ID, values);
			insUri = ContentUris.withAppendedId(CMetaData.ContactsMetaData.CONTENT_URI, rowId);
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
		case TIMEZONE:
			count = db.update(CMetaData.CONTACTS_TABLE, values, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		return count;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		Context context;
		
		public DatabaseHelper(Context context) {
			super(context, TMetaData.DATABASE_NAME, null, TMetaData.DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			ContentValues cv = new ContentValues();
			String query =	"CREATE TABLE "
				+TMetaData.TIMEZONE_TABLE+" ("
				+TMetaData.TimezoneMetaData._ID+ " INTEGER,"
				+TMetaData.TimezoneMetaData.TIMEZONE+ " TEXT,"
				+TMetaData.TimezoneMetaData.CITY+ " TEXT,"
				+TMetaData.TimezoneMetaData.COUNTRY+ " TEXT,"
				+TMetaData.TimezoneMetaData.COUNTRY2+ " TEXT,"
				+TMetaData.TimezoneMetaData.COUNTRY_CODE+ " TEXT,"
				+TMetaData.TimezoneMetaData.ALTNAME+ " TEXT"
				+")";
			
			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		}
	}
}
