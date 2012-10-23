package com.groupagendas.groupagenda.chat;

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

import com.groupagendas.groupagenda.contacts.ContactsProvider.CMetaData;

public class ChatProvider extends ContentProvider {

	private DatabaseHelper mOpenHelper;

	public static class CMMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.chat.ChatProvider";
		public static final String DATABASE_NAME = "chat.sqlite";
		public static final String CHAT_TABLE = "chat";

		public static final int DATABASE_VERSION = 1;

		public static final class ChatMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CHAT_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.chat_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.chat_item";

			public static final String M_ID = "message_id";
			public static final String E_ID = "event_id";
			public static final String USER_ID = "user_id";
			public static final String MESSAGE = "message";
			public static final String DELETED = "deleted";
			public static final String UPDATED = "updated";
			public static final String SUCCESSFULLY_UPLOADED = "successfully_uploaded";
			public static final String CREATED = "timestamp";
			public static final String MODIFIED = "modified";
			
			public static final String DEFAULT_SORT_ORDER = M_ID+" COLLATE NOCASE ASC";
		}
	}

	private static HashMap<String, String> CTM;
	static {
		CTM = new HashMap<String, String>();
		CTM.put(CMMetaData.ChatMetaData.M_ID, CMMetaData.ChatMetaData.M_ID);
		CTM.put(CMMetaData.ChatMetaData.E_ID, CMMetaData.ChatMetaData.E_ID);
		CTM.put(CMMetaData.ChatMetaData.USER_ID, CMMetaData.ChatMetaData.USER_ID);
		CTM.put(CMMetaData.ChatMetaData.MESSAGE, CMMetaData.ChatMetaData.MESSAGE);
		CTM.put(CMMetaData.ChatMetaData.DELETED, CMMetaData.ChatMetaData.DELETED);
		CTM.put(CMMetaData.ChatMetaData.UPDATED, CMMetaData.ChatMetaData.UPDATED);
		CTM.put(CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED, CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED);
		CTM.put(CMMetaData.ChatMetaData.CREATED, CMMetaData.ChatMetaData.CREATED);
		CTM.put(CMMetaData.ChatMetaData.MODIFIED, CMMetaData.ChatMetaData.MODIFIED);
	}
	
	private static final UriMatcher mUriMatcher;
	
	private static final int CHAT_ALL = 0;
	private static final int CHAT_ONE = 1;
	
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(CMMetaData.AUTHORITY, CMMetaData.CHAT_TABLE, CHAT_ALL);
		mUriMatcher.addURI(CMMetaData.AUTHORITY, CMMetaData.CHAT_TABLE + "/#", CHAT_ONE);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(mUriMatcher.match(uri)){
			case CHAT_ALL:
				count = db.delete(CMMetaData.CHAT_TABLE, where, whereArgs);
				break;
			case CHAT_ONE:
				where = CMMetaData.ChatMetaData.M_ID + "=" + uri.getPathSegments().get(1);
				count = db.delete(CMMetaData.CHAT_TABLE, where, whereArgs);
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
		case CHAT_ALL:
		case CHAT_ONE:
			return CMetaData.ContactsMetaData.CONTENT_TYPE;
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
		case CHAT_ALL:
			rowId = db.replace(CMMetaData.CHAT_TABLE, CMMetaData.ChatMetaData.M_ID, values);
			insUri = ContentUris.withAppendedId(CMMetaData.ChatMetaData.CONTENT_URI, rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(insUri, null);
		return insUri;
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
		case CHAT_ALL:
			qb.setTables(CMMetaData.CHAT_TABLE);
			qb.setProjectionMap(CTM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMMetaData.ChatMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case CHAT_ONE:
			qb.setTables(CMMetaData.CHAT_TABLE);
			qb.setProjectionMap(CTM);
			qb.appendWhere(CMMetaData.ChatMetaData.M_ID + "=" + uri.getPathSegments().get(1));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CMMetaData.ChatMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		int count;
		String rowId;

		switch (mUriMatcher.match(uri)) {
		case CHAT_ALL:
			count = db.update(CMMetaData.CHAT_TABLE, values, where, whereArgs);
			break;
		case CHAT_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.update(
				CMMetaData.CHAT_TABLE,
				values,
				CMMetaData.ChatMetaData.M_ID+"="+rowId+(!TextUtils.isEmpty(where)?"AND("+where+")":""),
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
			super(context, CMMetaData.DATABASE_NAME, null, CMMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query = "CREATE TABLE " + CMMetaData.CHAT_TABLE + " (" 
					+ CMMetaData.ChatMetaData.M_ID + " INTEGER UNIQUE ON CONFLICT IGNORE,"
					+ CMMetaData.ChatMetaData.E_ID + " INTEGER ,"
					+ CMMetaData.ChatMetaData.USER_ID + " INTEGER ," 
					+ CMMetaData.ChatMetaData.MESSAGE + " TEXT ,"
					+ CMMetaData.ChatMetaData.DELETED + " TEXT ,"
					+ CMMetaData.ChatMetaData.UPDATED + " TEXT ,"
					+ CMMetaData.ChatMetaData.SUCCESSFULLY_UPLOADED + " INTEGER DEFAULT 0 ,"
					+ CMMetaData.ChatMetaData.CREATED + " INTEGER ,"
					+ CMMetaData.ChatMetaData.MODIFIED + " INTEGER "
					+ ")";

			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}

}
