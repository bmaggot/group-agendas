package com.groupagendas.groupagenda.account;

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


public class AccountProvider extends ContentProvider{
	private DatabaseHelper mOpenHelper;

	public static class AMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.account.AccountProvider";
		public static final String DATABASE_NAME = "account.sqlite";

		public static final int DATABASE_VERSION = 1;

		public static final String ACCOUNT_TABLE = "account";
		public static final String AUTOICON_TABLE = "autoicon";
		public static final String AUTOCOLOR_TABLE = "autocolor";
		
		
		public static final class AccountMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ACCOUNT_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.account_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.account_item";
			public static final String DEFAULT_SORT_ORDER = "";

			public static final String A_ID = "user_id";
			
			public static final String NAME 	= "name";
			public static final String LASTNAME = "lastname";
			public static final String FULLNAME = "fullname";
			
			public static final String BIRTHDATE	= "birthdate";
			public static final String SEX		= "sex";
			
			public static final String EMAIL	= "email";
			public static final String EMAIL2	= "email2";
			public static final String EMAIL3	= "email3";
			public static final String EMAIL4	= "email4";
			public static final String PHONE1	= "phone1";
			public static final String PHONE2	= "phone2";
			public static final String PHONE3	= "phone3";
			
			public static final String IMAGE			= "image";
			public static final String IMAGE_URL		= "image_url";
			public static final String IMAGE_THUMB_URL	= "image_thumb_url";
			public static final String IMAGE_BYTES		= "image_bytes";
			public static final String REMOVE_IMAGE		= "remove_image";
			
			public static final String COUNTRY	= "country";
			public static final String CITY		= "city";
			public static final String STREET	= "street";
			public static final String ZIP		= "zip";
			
			public static final String TIMEZONE		= "timezone";
			public static final String LOCAL_TIME	= "local_time";
			public static final String LANGUAGE		= "language";
			
			public static final String SETTING_DEFAULT_VIEW	= "setting_default_view";
			public static final String SETTING_DATE_FORMAT	= "setting_date_format";
			public static final String SETTING_AMPM			= "setting_ampm";
			
			public static final String GOOGLE_CALENDAR_LINK	= "google_calendar_link";
			
			public static final String COLOR_MY_EVENT		= "color_my_event";
			public static final String COLOR_ATTENDING	= "color_attendint";
			public static final String COLOR_PENDING		= "color_pending";
			public static final String COLOR_INVINTATION	= "color_invitation";
			public static final String COLOR_NOTES		= "color_notes";
			public static final String COLOR_BIRTHDAY	= "color_birthday";
			
			public static final String CREATED	= "created";
			public static final String MODIFIED	= "modified";
			
			public static final String NEED_UPDATE = "need_update";
		}
		
		public static final class AutoiconMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + AUTOICON_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.autoicon_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.autoicon_item";
			public static final String DEFAULT_SORT_ORDER = "";
			
			public static final String I_ID = "_id";
			
			public static final String ICON = "icon";
			public static final String KEYWORD = "keyword";
			public static final String CONTEXT 	= "context";
			
			public static final String NEED_UPDATE 	= "need_update";
		}
		
		public static final class AutocolorMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + AUTOCOLOR_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.autocolor_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.autocolor_item";
			public static final String DEFAULT_SORT_ORDER = "";
			
			public static final String C_ID = "_id";
			
			public static final String COLOR = "color";
			public static final String KEYWORD = "keyword";
			public static final String CONTEXT 	= "context";
			
			public static final String NEED_UPDATE 	= "need_update";
		}
	}
	
	// Table Projection Map
	private static HashMap<String, String> AM;
	
	static {
		AM = new HashMap<String, String>();
		AM.put(AMetaData.AccountMetaData.A_ID, AMetaData.AccountMetaData.A_ID);
		
		AM.put(AMetaData.AccountMetaData.NAME, AMetaData.AccountMetaData.NAME);
		AM.put(AMetaData.AccountMetaData.LASTNAME, AMetaData.AccountMetaData.LASTNAME);
		AM.put(AMetaData.AccountMetaData.FULLNAME, AMetaData.AccountMetaData.FULLNAME);
		
		AM.put(AMetaData.AccountMetaData.BIRTHDATE, AMetaData.AccountMetaData.BIRTHDATE);
		AM.put(AMetaData.AccountMetaData.SEX, AMetaData.AccountMetaData.SEX);
		
		AM.put(AMetaData.AccountMetaData.EMAIL, AMetaData.AccountMetaData.EMAIL);
		AM.put(AMetaData.AccountMetaData.EMAIL2, AMetaData.AccountMetaData.EMAIL2);
		AM.put(AMetaData.AccountMetaData.EMAIL3, AMetaData.AccountMetaData.EMAIL3);
		AM.put(AMetaData.AccountMetaData.EMAIL4, AMetaData.AccountMetaData.EMAIL4);
		AM.put(AMetaData.AccountMetaData.PHONE1, AMetaData.AccountMetaData.PHONE1);
		AM.put(AMetaData.AccountMetaData.PHONE2, AMetaData.AccountMetaData.PHONE2);
		AM.put(AMetaData.AccountMetaData.PHONE3, AMetaData.AccountMetaData.PHONE3);
		
		AM.put(AMetaData.AccountMetaData.IMAGE, AMetaData.AccountMetaData.IMAGE);
		AM.put(AMetaData.AccountMetaData.IMAGE_URL, AMetaData.AccountMetaData.IMAGE_URL);
		AM.put(AMetaData.AccountMetaData.IMAGE_THUMB_URL, AMetaData.AccountMetaData.IMAGE_THUMB_URL);
		AM.put(AMetaData.AccountMetaData.IMAGE_BYTES, AMetaData.AccountMetaData.IMAGE_BYTES);
		AM.put(AMetaData.AccountMetaData.REMOVE_IMAGE, AMetaData.AccountMetaData.REMOVE_IMAGE);
		
		AM.put(AMetaData.AccountMetaData.COUNTRY, AMetaData.AccountMetaData.COUNTRY);
		AM.put(AMetaData.AccountMetaData.CITY, AMetaData.AccountMetaData.CITY);
		AM.put(AMetaData.AccountMetaData.STREET, AMetaData.AccountMetaData.STREET);
		AM.put(AMetaData.AccountMetaData.ZIP, AMetaData.AccountMetaData.ZIP);
		
		AM.put(AMetaData.AccountMetaData.TIMEZONE, AMetaData.AccountMetaData.TIMEZONE);
		AM.put(AMetaData.AccountMetaData.LOCAL_TIME, AMetaData.AccountMetaData.LOCAL_TIME);
		AM.put(AMetaData.AccountMetaData.LANGUAGE, AMetaData.AccountMetaData.LANGUAGE);
		
		AM.put(AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW);
		AM.put(AMetaData.AccountMetaData.SETTING_DATE_FORMAT, AMetaData.AccountMetaData.SETTING_DATE_FORMAT);
		AM.put(AMetaData.AccountMetaData.SETTING_AMPM, AMetaData.AccountMetaData.SETTING_AMPM);
		
		AM.put(AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK, AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK);
		
		AM.put(AMetaData.AccountMetaData.COLOR_MY_EVENT, AMetaData.AccountMetaData.COLOR_MY_EVENT);
		AM.put(AMetaData.AccountMetaData.COLOR_ATTENDING, AMetaData.AccountMetaData.COLOR_ATTENDING);
		AM.put(AMetaData.AccountMetaData.COLOR_PENDING, AMetaData.AccountMetaData.COLOR_PENDING);
		AM.put(AMetaData.AccountMetaData.COLOR_INVINTATION, AMetaData.AccountMetaData.COLOR_INVINTATION);
		AM.put(AMetaData.AccountMetaData.COLOR_NOTES, AMetaData.AccountMetaData.COLOR_NOTES);
		AM.put(AMetaData.AccountMetaData.COLOR_BIRTHDAY, AMetaData.AccountMetaData.COLOR_BIRTHDAY);
		
		
		AM.put(AMetaData.AccountMetaData.CREATED, AMetaData.AccountMetaData.CREATED);
		AM.put(AMetaData.AccountMetaData.MODIFIED, AMetaData.AccountMetaData.MODIFIED);
		
		AM.put(AMetaData.AccountMetaData.NEED_UPDATE, AMetaData.AccountMetaData.NEED_UPDATE);
		
	}
	
	private static HashMap<String, String> IM;
	
	static {
		IM = new HashMap<String, String>();
		IM.put(AMetaData.AutoiconMetaData.I_ID, AMetaData.AutoiconMetaData.I_ID);
		IM.put(AMetaData.AutoiconMetaData.ICON, AMetaData.AutoiconMetaData.ICON);
		IM.put(AMetaData.AutoiconMetaData.KEYWORD, AMetaData.AutoiconMetaData.KEYWORD);
		IM.put(AMetaData.AutoiconMetaData.CONTEXT, AMetaData.AutoiconMetaData.CONTEXT);
		IM.put(AMetaData.AutoiconMetaData.NEED_UPDATE, AMetaData.AutoiconMetaData.NEED_UPDATE);
	}
	
	private static HashMap<String, String> CM;
	
	static {
		CM = new HashMap<String, String>();
		CM.put(AMetaData.AutocolorMetaData.C_ID, AMetaData.AutocolorMetaData.C_ID);
		CM.put(AMetaData.AutocolorMetaData.COLOR, AMetaData.AutocolorMetaData.COLOR);
		CM.put(AMetaData.AutocolorMetaData.KEYWORD, AMetaData.AutocolorMetaData.KEYWORD);
		CM.put(AMetaData.AutocolorMetaData.CONTEXT, AMetaData.AutocolorMetaData.CONTEXT);
		CM.put(AMetaData.AutocolorMetaData.NEED_UPDATE, AMetaData.AutocolorMetaData.NEED_UPDATE);
	}
	// END Table Projection Map
	
	// UriMatcher
	private static final UriMatcher mUriMatcher;

	private static final int ACCOUNT = 0;
	
	private static final int AUTOICON = 1;
	private static final int AUTOICON_ONE = 3;
	
	private static final int AUTOCOLOR = 2;
	private static final int AUTOCOLOR_ONE = 4;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.ACCOUNT_TABLE, ACCOUNT);
		
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.AUTOICON_TABLE, AUTOICON);
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.AUTOICON_TABLE+"/#", AUTOICON_ONE);
		
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.AUTOCOLOR_TABLE, AUTOCOLOR);
		mUriMatcher.addURI(AMetaData.AUTHORITY, AMetaData.AUTOCOLOR_TABLE+"/#", AUTOCOLOR_ONE);
	}
	// END UriMatcher
	
	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case ACCOUNT:
			return AMetaData.AccountMetaData.CONTENT_TYPE;
		case AUTOICON:
		case AUTOICON_ONE:
			return AMetaData.AutoiconMetaData.CONTENT_TYPE;
		case AUTOCOLOR:
		case AUTOCOLOR_ONE:
			return AMetaData.AutocolorMetaData.CONTENT_TYPE;
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
		case ACCOUNT:
			qb.setTables(AMetaData.ACCOUNT_TABLE);
			qb.setProjectionMap(AM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? AMetaData.AccountMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case AUTOICON:
			qb.setTables(AMetaData.AUTOICON_TABLE);
			qb.setProjectionMap(IM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? AMetaData.AutoiconMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case AUTOCOLOR:
			qb.setTables(AMetaData.AUTOCOLOR_TABLE);
			qb.setProjectionMap(CM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? AMetaData.AutocolorMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
			case ACCOUNT:
				count = db.delete(AMetaData.ACCOUNT_TABLE, where, whereArgs);
				break;
			case AUTOICON:
				count = db.delete(AMetaData.AUTOICON_TABLE, where, whereArgs);
				break;
			case AUTOICON_ONE:
				String whereStr = AMetaData.AutoiconMetaData.I_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
				count = db.delete(AMetaData.AUTOICON_TABLE, whereStr, whereArgs);
				break;
			case AUTOCOLOR:
				count = db.delete(AMetaData.AUTOCOLOR_TABLE, where, whereArgs);
				break;
			case AUTOCOLOR_ONE:
				String whereStr2 = AMetaData.AutocolorMetaData.C_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
				count = db.delete(AMetaData.AUTOCOLOR_TABLE, whereStr2, whereArgs);
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
		switch (mUriMatcher.match(uri)){
		case ACCOUNT:
			rowId = db.replace(AMetaData.ACCOUNT_TABLE, AMetaData.AccountMetaData.A_ID, values);
			insUri = ContentUris.withAppendedId(AMetaData.AccountMetaData.CONTENT_URI, rowId);
			break;
		case AUTOICON:
			rowId = db.insert(AMetaData.AUTOICON_TABLE, AMetaData.AutoiconMetaData.CONTEXT, values);
			insUri = ContentUris.withAppendedId(AMetaData.AutoiconMetaData.CONTENT_URI, rowId);
			break;
		case AUTOCOLOR:
			rowId = db.insert(AMetaData.AUTOCOLOR_TABLE, AMetaData.AutocolorMetaData.CONTEXT, values);
			insUri = ContentUris.withAppendedId(AMetaData.AutocolorMetaData.CONTENT_URI, rowId);
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
		case ACCOUNT:
			count = db.update(AMetaData.ACCOUNT_TABLE, values, where, whereArgs);
			break;
		case AUTOICON:
			count = db.update(AMetaData.AUTOICON_TABLE, values, where, whereArgs);
			break;
		case AUTOICON_ONE:
			String whereStr = AMetaData.AutoiconMetaData.I_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(AMetaData.AUTOICON_TABLE, values, whereStr, whereArgs);
			break;
		case AUTOCOLOR:
			count = db.update(AMetaData.AUTOCOLOR_TABLE, values, where, whereArgs);
			break;
		case AUTOCOLOR_ONE:
			String where2Str = AMetaData.AutocolorMetaData.C_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(AMetaData.AUTOCOLOR_TABLE, values, where2Str, whereArgs);
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
				+AMetaData.ACCOUNT_TABLE+" ("
				+AMetaData.AccountMetaData.A_ID+" INTEGER PRIMARY KEY,"
				+AMetaData.AccountMetaData.NAME+" TEXT ,"
				+AMetaData.AccountMetaData.LASTNAME+" TEXT ,"
				+AMetaData.AccountMetaData.FULLNAME+" TEXT ,"
				
				+AMetaData.AccountMetaData.BIRTHDATE+" TEXT ,"
				+AMetaData.AccountMetaData.SEX+" TEXT ,"
				
				+AMetaData.AccountMetaData.EMAIL+" TEXT ,"
				+AMetaData.AccountMetaData.EMAIL2+" TEXT ,"
				+AMetaData.AccountMetaData.EMAIL3+" TEXT ,"
				+AMetaData.AccountMetaData.EMAIL4+" TEXT ,"
				+AMetaData.AccountMetaData.PHONE1+" TEXT ,"
				+AMetaData.AccountMetaData.PHONE2+" TEXT ,"
				+AMetaData.AccountMetaData.PHONE3+" TEXT ,"
				
				+AMetaData.AccountMetaData.IMAGE+" TEXT ,"
				+AMetaData.AccountMetaData.IMAGE_URL+" TEXT ,"
				+AMetaData.AccountMetaData.IMAGE_THUMB_URL+" TEXT ,"
				+AMetaData.AccountMetaData.IMAGE_BYTES+" BLOB ,"
				+AMetaData.AccountMetaData.REMOVE_IMAGE+" INTEGER ,"
				
				+AMetaData.AccountMetaData.COUNTRY+" TEXT ,"
				+AMetaData.AccountMetaData.CITY+" TEXT ,"
				+AMetaData.AccountMetaData.STREET+" TEXT ,"
				+AMetaData.AccountMetaData.ZIP+" TEXT ,"
				
				+AMetaData.AccountMetaData.TIMEZONE+" TEXT ,"
				+AMetaData.AccountMetaData.LOCAL_TIME+" TEXT ,"
				+AMetaData.AccountMetaData.LANGUAGE+" TEXT ,"
				
				+AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW+" TEXT ,"
				+AMetaData.AccountMetaData.SETTING_DATE_FORMAT+" TEXT ,"
				+AMetaData.AccountMetaData.SETTING_AMPM+" TEXT ,"
				
				+AMetaData.AccountMetaData.GOOGLE_CALENDAR_LINK+" TEXT ,"
				
				+AMetaData.AccountMetaData.COLOR_MY_EVENT+" TEXT ,"
				+AMetaData.AccountMetaData.COLOR_ATTENDING+" TEXT ,"
				+AMetaData.AccountMetaData.COLOR_PENDING+" TEXT ,"
				+AMetaData.AccountMetaData.COLOR_INVINTATION+" TEXT ,"
				+AMetaData.AccountMetaData.COLOR_NOTES+" TEXT ,"
				+AMetaData.AccountMetaData.COLOR_BIRTHDAY+" TEXT ,"
				
				+AMetaData.AccountMetaData.CREATED+" TEXT ,"
				+AMetaData.AccountMetaData.MODIFIED+" TEXT ,"
			
				+AMetaData.AccountMetaData.NEED_UPDATE+" INTEGER )";
				
			db.execSQL(query);
			
			query =	"CREATE TABLE "
				+AMetaData.AUTOICON_TABLE+" ("
				+AMetaData.AutoiconMetaData.I_ID+" INTEGER PRIMARY KEY,"
				+AMetaData.AutoiconMetaData.ICON+" TEXT,"
				+AMetaData.AutoiconMetaData.KEYWORD+" TEXT,"			
				+AMetaData.AutoiconMetaData.CONTEXT+" TEXT,"
				+AMetaData.AutoiconMetaData.NEED_UPDATE+" INTEGER )";
			db.execSQL(query);
			
			query =	"CREATE TABLE "
				+AMetaData.AUTOCOLOR_TABLE+" ("
				+AMetaData.AutocolorMetaData.C_ID+" INTEGER PRIMARY KEY,"
				+AMetaData.AutocolorMetaData.COLOR+" TEXT,"
				+AMetaData.AutocolorMetaData.KEYWORD+" TEXT,"			
				+AMetaData.AutocolorMetaData.CONTEXT+" TEXT,"
				+AMetaData.AutocolorMetaData.NEED_UPDATE+" INTEGER )";
			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}
}

