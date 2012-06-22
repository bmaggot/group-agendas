package com.groupagendas.groupagenda.timezone;

import java.util.HashMap;

import android.content.ContentProvider;
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

import com.groupagendas.groupagenda.utils.DBUtils;

public class TimezoneProvider extends ContentProvider{
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
		TM.put(TMetaData.TimezoneMetaData.SORT, TMetaData.TimezoneMetaData.SORT);
		
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
	
	public static DBUtils dbc;
	
	@Override
	public boolean onCreate() {
		dbc = new DBUtils(TMetaData.DATABASE_NAME);
		if (!dbc.checkDataBase())
			dbc.copyDataBase(this.getContext());

		mOpenHelper = new DatabaseHelper(this.getContext());
		return (mOpenHelper == null) ? false : true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String orderBy;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case TIMEZONE:
			qb.setTables(TMetaData.TIMEZONE_TABLE);
			qb.setProjectionMap(TM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? TMetaData.TimezoneMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, TMetaData.DATABASE_NAME, null, TMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		}
	}
}
