package com.groupagendas.groupagenda.address;

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

public class AddressProvider extends ContentProvider {
	private DatabaseHelper mOpenHelper;

	public static class AMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.address.AddressProvider";
		public static final String DATABASE_NAME = "addresses.sqlite";
		public static final int DATABASE_VERSION = 1;
		public static final String ADDRESSES_TABLE = "addresses";

		public static final class AddressesMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ADDRESSES_TABLE);
			public static final Uri CONTENT_URI_EXTERNAL_ID = Uri.parse("content://" + AUTHORITY + "/" + ADDRESSES_TABLE + "/external");
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.address_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.address_item";

			public static final String A_ID = "id";
			public static final String USER_ID = "user_id";
			public static final String TITLE = "title";
			public static final String STREET = "street";
			public static final String CITY = "city";
			public static final String ZIP = "zip";
			public static final String STATE = "state";
			public static final String COUNTRY = "country";
			public static final String TIMEZONE = "timezone";
			public static final String COUNTRY_NAME = "country_name";

			public static final String DEFAULT_SORT_ORDER = TITLE + " ASC";
		}
	}

	public static HashMap<String, String> AD;

	static {
		AD = new HashMap<String, String>();
		AD.put(AMetaData.AddressesMetaData.A_ID, AMetaData.AddressesMetaData.A_ID);
		AD.put(AMetaData.AddressesMetaData.USER_ID, AMetaData.AddressesMetaData.USER_ID);
		AD.put(AMetaData.AddressesMetaData.TITLE, AMetaData.AddressesMetaData.TITLE);
		AD.put(AMetaData.AddressesMetaData.STREET, AMetaData.AddressesMetaData.STREET);
		AD.put(AMetaData.AddressesMetaData.CITY, AMetaData.AddressesMetaData.CITY);
		AD.put(AMetaData.AddressesMetaData.ZIP, AMetaData.AddressesMetaData.ZIP);
		AD.put(AMetaData.AddressesMetaData.STATE, AMetaData.AddressesMetaData.STATE);
		AD.put(AMetaData.AddressesMetaData.COUNTRY, AMetaData.AddressesMetaData.COUNTRY);
		AD.put(AMetaData.AddressesMetaData.TIMEZONE, AMetaData.AddressesMetaData.TIMEZONE);
		AD.put(AMetaData.AddressesMetaData.COUNTRY_NAME, AMetaData.AddressesMetaData.COUNTRY_NAME);
	}

	/* UriMatcher */
	private static final UriMatcher mUriMatcher;

	public static final int ALL_ADDRESSES = 0;
	public static final int SINGLE_ADDRESS = 1;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.ADDRESSES_TABLE, ALL_ADDRESSES);
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.ADDRESSES_TABLE + "/#", SINGLE_ADDRESS);
	}

	@Override
	public int delete(Uri uri, String where, String[] selectionArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(mUriMatcher.match(uri)){
			case ALL_ADDRESSES:
				count = db.delete(AMetaData.ADDRESSES_TABLE, where, selectionArgs);
				break;
			case SINGLE_ADDRESS:
				String whereStr = AMetaData.AddressesMetaData.A_ID + "=" + uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND(" + where + ")":"");
				count = db.delete(AMetaData.ADDRESSES_TABLE, whereStr, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknow URI "+uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case ALL_ADDRESSES:
		case SINGLE_ADDRESS:
			return AMetaData.AddressesMetaData.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = 0;
		Uri insUri;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case ALL_ADDRESSES:
			rowId = db.replace(AMetaData.ADDRESSES_TABLE, AMetaData.AddressesMetaData.A_ID, values);
			insUri = ContentUris.withAppendedId(AMetaData.AddressesMetaData.CONTENT_URI, rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(insUri, null);
		return insUri;
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
			case ALL_ADDRESSES:
				qb.setTables(AMetaData.ADDRESSES_TABLE);
				qb.setProjectionMap(AD);
				orderBy = (TextUtils.isEmpty(sortOrder)) ? AMetaData.AddressesMetaData.DEFAULT_SORT_ORDER : sortOrder;
				break;
			case SINGLE_ADDRESS:
				qb.setTables(AMetaData.ADDRESSES_TABLE);
				qb.setProjectionMap(AD);
				qb.appendWhere(AMetaData.AddressesMetaData.A_ID + "=" + uri.getPathSegments().get(1));
				orderBy = (TextUtils.isEmpty(sortOrder)) ? AMetaData.AddressesMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		int count;

		switch (mUriMatcher.match(uri)) {
		case ALL_ADDRESSES:
			count = db.update(AMetaData.ADDRESSES_TABLE, values, selection, selectionArgs);
			break;
		case SINGLE_ADDRESS:
			String whereStr = AMetaData.AddressesMetaData.A_ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection)?"AND(" + selection + ")":"");
			count = db.update(AMetaData.ADDRESSES_TABLE, values, whereStr, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, AMetaData.DATABASE_NAME, null, AMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query =	"CREATE TABLE "
					+AMetaData.ADDRESSES_TABLE+" ("
					+AMetaData.AddressesMetaData._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
					+AMetaData.AddressesMetaData.A_ID + " INTEGER,"
					+AMetaData.AddressesMetaData.USER_ID + " TEXT ,"
					+AMetaData.AddressesMetaData.TITLE + " TEXT ,"
					+AMetaData.AddressesMetaData.STREET + " TEXT ,"
					+AMetaData.AddressesMetaData.CITY + " TEXT ,"
					+AMetaData.AddressesMetaData.ZIP + " TEXT ,"
					+AMetaData.AddressesMetaData.STATE + " TEXT ,"
					+AMetaData.AddressesMetaData.COUNTRY + " TEXT ,"
					+AMetaData.AddressesMetaData.TIMEZONE + " TEXT ,"
					+AMetaData.AddressesMetaData.COUNTRY_NAME  + " TEXT )";
			
			db.execSQL(query);
					
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}

}