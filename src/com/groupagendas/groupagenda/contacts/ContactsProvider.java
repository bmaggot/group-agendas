package com.groupagendas.groupagenda.contacts;

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

public class ContactsProvider extends ContentProvider{
	private DatabaseHelper mOpenHelper;

	public static class CMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.contacts.ContactsProvider";
		public static final String DATABASE_NAME = "contacts.sqlite";

		public static final int DATABASE_VERSION = 1;

		public static final String CONTACTS_TABLE = "contacts";
		public static final String GROUPS_TABLE = "groups";
		public static final String BIRTHDAYS_TABLE = "birthday";

		public static final class ContactsMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTACTS_TABLE);
			public static final Uri CONTENT_URI_EXT = Uri.parse("content://" + AUTHORITY + "/" + CONTACTS_TABLE + "/external");
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.contact_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.contact_item";
            
			public static final String C_ID = "contact_id";
			public static final String LID = "lid";
			public static final String NAME = "name";
			public static final String LASTNAME = "lastname";
			public static final String FULLNAME = "fullname";
			public static final String EMAIL = "email";
			public static final String PHONE = "phone1";
			public static final String PHONE_CODE = "phone1_code";
			public static final String BIRTHDATE = "birthdate";
			public static final String COUNTRY = "country";
			public static final String CITY = "city";
			public static final String STREET = "street";
			public static final String ZIP = "zip";
			public static final String VISIBILITY = "visibility";
			public static final String VISIBILITY2 = "visibility2";
			public static final String COLOR = "contact_color";
			
			public static final String IMAGE = "image";
			public static final String IMAGE_URL = "image_url";
			public static final String IMAGE_THUMB_URL = "image_thumb_url";
			public static final String IMAGE_BYTES = "image_bytes";
			public static final String REMOVE_IMAGE = "remove_image";
			
			public static final String CREATED = "timestamp_created";
			public static final String MODIFIED = "timestamp_modified";
			public static final String REG_USER_ID = "reg_user_id";
			public static final String AGENDA_VIEW = "agenda_view";
			public static final String AGENDA_VIEW2 = "agenda_view2";
			public static final String CAN_ADD_NOTE = "can_add_note";
			public static final String TIME_START = "time_start";
			public static final String TIME_END = "time_end";
			public static final String ALL_DAY = "all_day";
			public static final String DISPLAY_TIME_END = "display_time_end";
			public static final String TYPE = "type";
			public static final String TITLE = "title";
			public static final String REGISTERED = "registered";
			public static final String GROUPS = "groups";
			
			public static final String UPLOADED_SUCCESSFULLY = "uploaded";

			public static final String DEFAULT_SORT_ORDER = NAME+" COLLATE NOCASE ASC";
		}
		
		public static final class GroupsMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GROUPS_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.group_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.group_item";
			
			public static final String G_ID = "group_id";
			public static final String TITLE = "title";
			public static final String CREATED = "timestamp_created";
			public static final String MODIFIED = "timestamp_modified";
			public static final String DELETED = "deleted";
			
			public static final String IMAGE = "image";
			public static final String IMAGE_URL = "image_url";
			public static final String IMAGE_THUMB_URL = "image_thumb_url";
			public static final String IMAGE_BYTES		= "image_bytes";
			public static final String REMOVE_IMAGE		= "remove_image";
			
			public static final String CONTACT_COUNT = "contact_count";
			public static final String CONTACTS = "contacts";
			
			public static final String DEFAULT_SORT_ORDER = TITLE+" COLLATE NOCASE ASC";
		}
		
		public static final class BirthdaysMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BIRTHDAYS_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.birthday_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.birthday_item";

			public static final String B_ID = "birthday_id";
			public static final String TITLE = "title";
			public static final String BIRTHDATE = "birthdate";
			public static final String BIRTHDATE_MM_DD = "birthdate_mm_dd";
			public static final String BIRTHDATE_MM = "birthdate_mm";
			public static final String CONTACT_ID = "contact_id";
			public static final String COUNTRY = "country";
			public static final String TIMEZONE = "timezone";

			public static final String DEFAULT_SORT_ORDER = BIRTHDATE+" COLLATE NOCASE ASC";
		}
	}
	
	// Contacts Table Projection Map
	private static HashMap<String, String> CM;
	
	static {
		CM = new HashMap<String, String>();
		CM.put(CMetaData.ContactsMetaData._ID, CMetaData.ContactsMetaData._ID);
		CM.put(CMetaData.ContactsMetaData.C_ID, CMetaData.ContactsMetaData.C_ID);
		CM.put(CMetaData.ContactsMetaData.LID, CMetaData.ContactsMetaData.LID);
		CM.put(CMetaData.ContactsMetaData.NAME, CMetaData.ContactsMetaData.NAME);
		CM.put(CMetaData.ContactsMetaData.LASTNAME, CMetaData.ContactsMetaData.LASTNAME);
		CM.put(CMetaData.ContactsMetaData.FULLNAME, CMetaData.ContactsMetaData.FULLNAME);
		CM.put(CMetaData.ContactsMetaData.EMAIL, CMetaData.ContactsMetaData.EMAIL);
		CM.put(CMetaData.ContactsMetaData.PHONE, CMetaData.ContactsMetaData.PHONE);
		CM.put(CMetaData.ContactsMetaData.PHONE_CODE, CMetaData.ContactsMetaData.PHONE_CODE);
		CM.put(CMetaData.ContactsMetaData.BIRTHDATE, CMetaData.ContactsMetaData.BIRTHDATE);
		CM.put(CMetaData.ContactsMetaData.COUNTRY, CMetaData.ContactsMetaData.COUNTRY);
		CM.put(CMetaData.ContactsMetaData.CITY, CMetaData.ContactsMetaData.CITY);
		CM.put(CMetaData.ContactsMetaData.STREET, CMetaData.ContactsMetaData.STREET);
		CM.put(CMetaData.ContactsMetaData.ZIP, CMetaData.ContactsMetaData.ZIP);
		CM.put(CMetaData.ContactsMetaData.VISIBILITY, CMetaData.ContactsMetaData.VISIBILITY);
		CM.put(CMetaData.ContactsMetaData.VISIBILITY2, CMetaData.ContactsMetaData.VISIBILITY2);
		CM.put(CMetaData.ContactsMetaData.COLOR, CMetaData.ContactsMetaData.COLOR);
		
		CM.put(CMetaData.ContactsMetaData.IMAGE, CMetaData.ContactsMetaData.IMAGE);
		CM.put(CMetaData.ContactsMetaData.IMAGE_URL, CMetaData.ContactsMetaData.IMAGE_URL);
		CM.put(CMetaData.ContactsMetaData.IMAGE_THUMB_URL, CMetaData.ContactsMetaData.IMAGE_THUMB_URL);
		CM.put(CMetaData.ContactsMetaData.IMAGE_BYTES, CMetaData.ContactsMetaData.IMAGE_BYTES);
		CM.put(CMetaData.ContactsMetaData.REMOVE_IMAGE, CMetaData.ContactsMetaData.REMOVE_IMAGE);
		
		CM.put(CMetaData.ContactsMetaData.CREATED, CMetaData.ContactsMetaData.CREATED);
		CM.put(CMetaData.ContactsMetaData.MODIFIED, CMetaData.ContactsMetaData.MODIFIED);
		CM.put(CMetaData.ContactsMetaData.REG_USER_ID, CMetaData.ContactsMetaData.REG_USER_ID);
		CM.put(CMetaData.ContactsMetaData.AGENDA_VIEW, CMetaData.ContactsMetaData.AGENDA_VIEW);
		CM.put(CMetaData.ContactsMetaData.AGENDA_VIEW2, CMetaData.ContactsMetaData.AGENDA_VIEW2);
		CM.put(CMetaData.ContactsMetaData.CAN_ADD_NOTE, CMetaData.ContactsMetaData.CAN_ADD_NOTE);
		CM.put(CMetaData.ContactsMetaData.TIME_START, CMetaData.ContactsMetaData.TIME_START);
		CM.put(CMetaData.ContactsMetaData.TIME_END, CMetaData.ContactsMetaData.TIME_END);
		CM.put(CMetaData.ContactsMetaData.ALL_DAY, CMetaData.ContactsMetaData.ALL_DAY);
		CM.put(CMetaData.ContactsMetaData.DISPLAY_TIME_END, CMetaData.ContactsMetaData.DISPLAY_TIME_END);
		CM.put(CMetaData.ContactsMetaData.TYPE, CMetaData.ContactsMetaData.TYPE);
		CM.put(CMetaData.ContactsMetaData.TITLE, CMetaData.ContactsMetaData.TITLE);
		CM.put(CMetaData.ContactsMetaData.REGISTERED, CMetaData.ContactsMetaData.REGISTERED);
		CM.put(CMetaData.ContactsMetaData.GROUPS, CMetaData.ContactsMetaData.GROUPS);
		CM.put(CMetaData.ContactsMetaData.UPLOADED_SUCCESSFULLY, CMetaData.ContactsMetaData.UPLOADED_SUCCESSFULLY);
	}
	// END Table Projection Map
	
	// Groups Table Projection Map
	private static HashMap<String, String> GM;
	
	static {
		GM = new HashMap<String, String>();
		GM.put(CMetaData.GroupsMetaData._ID, CMetaData.GroupsMetaData._ID);
		GM.put(CMetaData.GroupsMetaData.G_ID, CMetaData.GroupsMetaData.G_ID);
		GM.put(CMetaData.GroupsMetaData.TITLE, CMetaData.GroupsMetaData.TITLE);
		GM.put(CMetaData.GroupsMetaData.CREATED, CMetaData.GroupsMetaData.CREATED);
		GM.put(CMetaData.GroupsMetaData.MODIFIED, CMetaData.GroupsMetaData.MODIFIED);
		GM.put(CMetaData.GroupsMetaData.DELETED, CMetaData.GroupsMetaData.DELETED);
		
		GM.put(CMetaData.GroupsMetaData.IMAGE, CMetaData.GroupsMetaData.IMAGE);
		GM.put(CMetaData.GroupsMetaData.IMAGE_URL, CMetaData.GroupsMetaData.IMAGE_URL);
		GM.put(CMetaData.GroupsMetaData.IMAGE_THUMB_URL, CMetaData.GroupsMetaData.IMAGE_THUMB_URL);
		GM.put(CMetaData.GroupsMetaData.IMAGE_BYTES, CMetaData.GroupsMetaData.IMAGE_BYTES);
		GM.put(CMetaData.GroupsMetaData.REMOVE_IMAGE, CMetaData.GroupsMetaData.REMOVE_IMAGE);
		
		GM.put(CMetaData.GroupsMetaData.CONTACT_COUNT, CMetaData.GroupsMetaData.CONTACT_COUNT);
		GM.put(CMetaData.GroupsMetaData.CONTACTS, CMetaData.GroupsMetaData.CONTACTS);
	}
	// END Table Projection Map
	
	// Birthdays Table Projection Map
		private static HashMap<String, String> BM;
		
		static {
			BM = new HashMap<String, String>();
			BM.put(CMetaData.BirthdaysMetaData.B_ID, CMetaData.BirthdaysMetaData.B_ID);
			BM.put(CMetaData.BirthdaysMetaData.TITLE, CMetaData.BirthdaysMetaData.TITLE);
			BM.put(CMetaData.BirthdaysMetaData.BIRTHDATE, CMetaData.BirthdaysMetaData.BIRTHDATE);
			BM.put(CMetaData.BirthdaysMetaData.BIRTHDATE_MM_DD, CMetaData.BirthdaysMetaData.BIRTHDATE_MM_DD);
			BM.put(CMetaData.BirthdaysMetaData.BIRTHDATE_MM, CMetaData.BirthdaysMetaData.BIRTHDATE_MM);
			BM.put(CMetaData.BirthdaysMetaData.CONTACT_ID, CMetaData.BirthdaysMetaData.CONTACT_ID);
			BM.put(CMetaData.BirthdaysMetaData.COUNTRY, CMetaData.BirthdaysMetaData.COUNTRY);
			BM.put(CMetaData.BirthdaysMetaData.TIMEZONE, CMetaData.BirthdaysMetaData.TIMEZONE);
		}
		// END Table Projection Map
	
	// UriMatcher
	private static final UriMatcher mUriMatcher;

	private static final int CONTACTS_ALL = 0;
	private static final int CONTACTS_ONE = 1;
	private static final int GROUPS_ALL = 2;
	private static final int GROUPS_ONE = 3;
	private static final int BIRTHDAYS_ALL = 4;
	private static final int BIRTHDAYS_ONE = 5;
	private static final int CONTACTS_ONE_BY_EXT = 6;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.CONTACTS_TABLE, CONTACTS_ALL);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.CONTACTS_TABLE + "/#", CONTACTS_ONE);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.GROUPS_TABLE, GROUPS_ALL);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.GROUPS_TABLE + "/#",GROUPS_ONE);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.BIRTHDAYS_TABLE, BIRTHDAYS_ALL);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.BIRTHDAYS_TABLE + "/#",BIRTHDAYS_ONE);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.CONTACTS_TABLE + "/external/#", CONTACTS_ONE_BY_EXT);
	}
	// END UriMatcher
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(this.getContext());
		return (mOpenHelper == null) ? false : true;
	}
	
	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case CONTACTS_ALL:
		case CONTACTS_ONE:
			return CMetaData.ContactsMetaData.CONTENT_TYPE;
		case CONTACTS_ONE_BY_EXT:
			return CMetaData.ContactsMetaData.CONTENT_TYPE;
		case GROUPS_ALL:
		case GROUPS_ONE:
			return CMetaData.GroupsMetaData.CONTENT_TYPE;
		case BIRTHDAYS_ALL:
		case BIRTHDAYS_ONE:
			return CMetaData.BirthdaysMetaData.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String orderBy;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case CONTACTS_ALL:
			qb.setTables(CMetaData.CONTACTS_TABLE);
			qb.setProjectionMap(CM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.ContactsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case CONTACTS_ONE:
			qb.setTables(CMetaData.CONTACTS_TABLE);
			qb.setProjectionMap(CM);
			qb.appendWhere(CMetaData.ContactsMetaData.C_ID + "=" + uri.getPathSegments().get(1));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.ContactsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case CONTACTS_ONE_BY_EXT:
			qb.setTables(CMetaData.CONTACTS_TABLE);
			qb.setProjectionMap(CM);
			qb.appendWhere(CMetaData.ContactsMetaData.REG_USER_ID + "=" + uri.getPathSegments().get(2));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.ContactsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case GROUPS_ALL:
			qb.setTables(CMetaData.GROUPS_TABLE);
			qb.setProjectionMap(GM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.GroupsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case GROUPS_ONE:
			qb.setTables(CMetaData.GROUPS_TABLE);
			qb.setProjectionMap(GM);
			qb.appendWhere(CMetaData.GroupsMetaData.G_ID + "=" + uri.getPathSegments().get(1));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.GroupsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
			
		case BIRTHDAYS_ALL:
			qb.setTables(CMetaData.BIRTHDAYS_TABLE);
			qb.setProjectionMap(BM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.BirthdaysMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case BIRTHDAYS_ONE:
			qb.setTables(CMetaData.BIRTHDAYS_TABLE);
			qb.setProjectionMap(BM);
			qb.appendWhere(CMetaData.BirthdaysMetaData.B_ID + "=" + uri.getPathSegments().get(1));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMetaData.BirthdaysMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
			case CONTACTS_ALL:
				count = db.delete(CMetaData.CONTACTS_TABLE, where, whereArgs);
				break;
			case CONTACTS_ONE:
				where = CMetaData.ContactsMetaData.C_ID + "=" + uri.getPathSegments().get(1);
				count = db.delete(CMetaData.CONTACTS_TABLE, where, whereArgs);
				break;
			
			case GROUPS_ALL:
				count = db.delete(CMetaData.GROUPS_TABLE, where, whereArgs);
				break;
			case GROUPS_ONE:
				where = CMetaData.GroupsMetaData.G_ID + "=" + uri.getPathSegments().get(1);
				count = db.delete(CMetaData.GROUPS_TABLE, where, whereArgs);
				break;
				
			case BIRTHDAYS_ALL:
				count = db.delete(CMetaData.BIRTHDAYS_TABLE, where, whereArgs);
				break;
			case BIRTHDAYS_ONE:
				where = CMetaData.BirthdaysMetaData.B_ID + "=" + uri.getPathSegments().get(1);
				count = db.delete(CMetaData.BIRTHDAYS_TABLE, where, whereArgs);
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
		case CONTACTS_ALL:
			rowId = db.replace(CMetaData.CONTACTS_TABLE, CMetaData.ContactsMetaData.C_ID, values);
			insUri = ContentUris.withAppendedId(CMetaData.ContactsMetaData.CONTENT_URI, rowId);
			break;
		case GROUPS_ALL:
			rowId = db.replace(CMetaData.GROUPS_TABLE, CMetaData.GroupsMetaData.G_ID, values);
			insUri = ContentUris.withAppendedId(CMetaData.GroupsMetaData.CONTENT_URI, rowId);
			break;
		case BIRTHDAYS_ALL:
			rowId = db.replace(CMetaData.BIRTHDAYS_TABLE, CMetaData.BirthdaysMetaData.B_ID, values);
			insUri = ContentUris.withAppendedId(CMetaData.BirthdaysMetaData.CONTENT_URI, rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(insUri, null);
		return insUri;
	}
	
//	@Override
//	public int bulkInsert(Uri uri, ContentValues[] values) {
//		  final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//		  
//		  int count = 0;
//		  
//		  switch (mUriMatcher.match(uri)) {
//			case CONTACTS_ALL:
//			db.beginTransaction();
//				try {
//					SQLiteStatement insert = db.compileStatement("insert into " + CMetaData.CONTACTS_TABLE
//				    + "(" 
//					+ ")"		
//					+" values " + "(?,?,?)");
//					
//					for (ContentValues entry : values){
//						insert.
//						insert.executeInsert();
//					}
//	
//				} finally {
//					db.endTransaction();
//				}
//				break;
////			case GROUPS_ALL:
////				break;
//			default:
//				throw new IllegalArgumentException("Unknow URI " + uri);
//			}
//		return count;
//	}
		
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count;
		String rowId;
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		switch (mUriMatcher.match(uri)) {
		case CONTACTS_ALL:
			count = db.update(CMetaData.CONTACTS_TABLE, values, where, whereArgs);
			break;
		case CONTACTS_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.update(
				CMetaData.CONTACTS_TABLE,
				values,
				CMetaData.ContactsMetaData.C_ID+"="+rowId+(!TextUtils.isEmpty(where)?"AND("+where+")":""),
				whereArgs
			);
			break;
		case GROUPS_ALL:
			count = db.update(CMetaData.GROUPS_TABLE, values, where, whereArgs);
			break;
		case GROUPS_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.update(
				CMetaData.GROUPS_TABLE,
				values,
				CMetaData.GroupsMetaData.G_ID+"="+rowId+(!TextUtils.isEmpty(where)?"AND("+where+")":""),
				whereArgs
			);
			break;
		case BIRTHDAYS_ALL:
			count = db.update(CMetaData.BIRTHDAYS_TABLE, values, where, whereArgs);
			break;
		case BIRTHDAYS_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.update(
				CMetaData.BIRTHDAYS_TABLE,
				values,
				CMetaData.BirthdaysMetaData.B_ID+"="+rowId+(!TextUtils.isEmpty(where)?"AND("+where+")":""),
				whereArgs
			);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		return count;
	}
	
private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, CMetaData.DATABASE_NAME, null, CMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
		
			String query =	"CREATE TABLE "
				+CMetaData.CONTACTS_TABLE+" ("
				+CMetaData.ContactsMetaData._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+CMetaData.ContactsMetaData.C_ID+" INTEGER UNIQUE  ON CONFLICT REPLACE,"
				+CMetaData.ContactsMetaData.LID+" TEXT ,"
				+CMetaData.ContactsMetaData.NAME+" TEXT ,"
				+CMetaData.ContactsMetaData.LASTNAME+" TEXT ,"
				+CMetaData.ContactsMetaData.FULLNAME+" TEXT ,"
				+CMetaData.ContactsMetaData.EMAIL+" TEXT ,"
				+CMetaData.ContactsMetaData.PHONE+" TEXT ,"
				+CMetaData.ContactsMetaData.PHONE_CODE+" TEXT ,"
				+CMetaData.ContactsMetaData.BIRTHDATE+" TEXT ,"
				+CMetaData.ContactsMetaData.COUNTRY+" TEXT ,"
				+CMetaData.ContactsMetaData.CITY+" TEXT ,"
				+CMetaData.ContactsMetaData.STREET+" TEXT ,"
				+CMetaData.ContactsMetaData.ZIP+" TEXT ,"
				+CMetaData.ContactsMetaData.VISIBILITY+" TEXT ,"
				+CMetaData.ContactsMetaData.VISIBILITY2+" TEXT ,"
				+CMetaData.ContactsMetaData.COLOR+" TEXT ,"
				
				+CMetaData.ContactsMetaData.IMAGE+" TEXT ,"
				+CMetaData.ContactsMetaData.IMAGE_URL+" TEXT ,"
				+CMetaData.ContactsMetaData.IMAGE_THUMB_URL+" TEXT ,"
				+CMetaData.ContactsMetaData.IMAGE_BYTES+" BLOB ,"
				+CMetaData.ContactsMetaData.REMOVE_IMAGE+" TEXT ,"
				
				+CMetaData.ContactsMetaData.CREATED+" INT ,"
				+CMetaData.ContactsMetaData.MODIFIED+" INT ,"
				+CMetaData.ContactsMetaData.REG_USER_ID+" INT ,"
				+CMetaData.ContactsMetaData.AGENDA_VIEW+" TEXT ,"
				+CMetaData.ContactsMetaData.AGENDA_VIEW2+" TEXT ,"
				+CMetaData.ContactsMetaData.CAN_ADD_NOTE+" TEXT ,"
				+CMetaData.ContactsMetaData.TIME_START+" TEXT ,"
				+CMetaData.ContactsMetaData.TIME_END+" TEXT ,"
				+CMetaData.ContactsMetaData.ALL_DAY+" TEXT ,"
				+CMetaData.ContactsMetaData.DISPLAY_TIME_END+" TEXT ,"
				+CMetaData.ContactsMetaData.TYPE+" TEXT ,"
				+CMetaData.ContactsMetaData.TITLE+" TEXT ,"
				+CMetaData.ContactsMetaData.GROUPS+" TEXT ,"
				+CMetaData.ContactsMetaData.UPLOADED_SUCCESSFULLY+" INTEGER DEFAULT 0, "
				+CMetaData.ContactsMetaData.REGISTERED+" TEXT)";
			db.execSQL(query);
			
			query =	"CREATE TABLE "
				+CMetaData.GROUPS_TABLE+" ("
				+CMetaData.GroupsMetaData._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+CMetaData.GroupsMetaData.G_ID+" INTEGER UNIQUE  ON CONFLICT REPLACE,"
				+CMetaData.GroupsMetaData.TITLE+" TEXT ,"
				+CMetaData.GroupsMetaData.CREATED+" INTEGER ,"
				+CMetaData.GroupsMetaData.MODIFIED+" INTEGER ,"
				+CMetaData.GroupsMetaData.DELETED+" TEXT ,"
				
				+CMetaData.GroupsMetaData.IMAGE+" TEXT ,"
				+CMetaData.GroupsMetaData.IMAGE_URL+" TEXT ,"
				+CMetaData.GroupsMetaData.IMAGE_THUMB_URL+" TEXT ,"
				+CMetaData.GroupsMetaData.IMAGE_BYTES+" BLOB ,"
				+CMetaData.GroupsMetaData.REMOVE_IMAGE+" TEXT ,"
				
				+CMetaData.GroupsMetaData.CONTACT_COUNT+" INTEGER ,"
				+CMetaData.GroupsMetaData.CONTACTS+" TEXT)";
				
			db.execSQL(query);
			
			query =	"CREATE TABLE "
					+CMetaData.BIRTHDAYS_TABLE+" ("
					+CMetaData.BirthdaysMetaData.B_ID+" INTEGER PRIMARY KEY,"
					+CMetaData.BirthdaysMetaData.TITLE+" TEXT ,"
					+CMetaData.BirthdaysMetaData.BIRTHDATE+" TEXT ,"
					+CMetaData.BirthdaysMetaData.BIRTHDATE_MM_DD+" TEXT ,"
					+CMetaData.BirthdaysMetaData.BIRTHDATE_MM+" TEXT ,"
					+CMetaData.BirthdaysMetaData.CONTACT_ID+" TEXT ,"
					+CMetaData.BirthdaysMetaData.COUNTRY+" TEXT ,"
					+CMetaData.BirthdaysMetaData.TIMEZONE+" TEXT)";
					
				db.execSQL(query);
		}

		
		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}
}
