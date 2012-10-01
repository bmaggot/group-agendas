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

public class ChatThreadsProvider extends ContentProvider {

	private DatabaseHelper mOpenHelper;

	public static class CTMetaData {
		public static final String AUTHORITY = "com.groupagendas.groupagenda.contacts.ChatThreadProvider";
		public static final String DATABASE_NAME = "chat_threads.sqlite";
		public static final String CHAT_THREADS_TABLE = "chat_threads";

		public static final int DATABASE_VERSION = 1;

		public static final class ChatThreadsMetaData implements BaseColumns {
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CHAT_THREADS_TABLE);
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.chat_thread_item";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.chat_thread_item";

			public static final String CT_ID = "chat_thread_id";
			public static final String E_ID = "event_id";
			public static final String CONTACT_AUTHOR_ID = "contact_author_id";
			public static final String MESSAGE_COUNT = "message_count";
			public static final String MESSAGE_LAST = "message_last";
			public static final String NEW_MESSAGES = "new_messages";
			public static final String SUCCESSFULLY_UPLOADED = "successfully_uploaded";
			public static final String CREATED = "created";
			public static final String MODIFIED = "modified";
			
			public static final String DEFAULT_SORT_ORDER = CT_ID+" COLLATE NOCASE ASC";
		}
	}

	private static HashMap<String, String> CTM;
	static {
		CTM = new HashMap<String, String>();
		CTM.put(CTMetaData.ChatThreadsMetaData.CT_ID, CTMetaData.ChatThreadsMetaData.CT_ID);
		CTM.put(CTMetaData.ChatThreadsMetaData.E_ID, CTMetaData.ChatThreadsMetaData.E_ID);
		CTM.put(CTMetaData.ChatThreadsMetaData.CONTACT_AUTHOR_ID, CTMetaData.ChatThreadsMetaData.CONTACT_AUTHOR_ID);
		CTM.put(CTMetaData.ChatThreadsMetaData.MESSAGE_COUNT, CTMetaData.ChatThreadsMetaData.MESSAGE_COUNT);
		CTM.put(CTMetaData.ChatThreadsMetaData.MESSAGE_LAST, CTMetaData.ChatThreadsMetaData.MESSAGE_LAST);
		CTM.put(CTMetaData.ChatThreadsMetaData.NEW_MESSAGES, CTMetaData.ChatThreadsMetaData.NEW_MESSAGES);
		CTM.put(CTMetaData.ChatThreadsMetaData.SUCCESSFULLY_UPLOADED, CTMetaData.ChatThreadsMetaData.SUCCESSFULLY_UPLOADED);
		CTM.put(CTMetaData.ChatThreadsMetaData.CREATED, CTMetaData.ChatThreadsMetaData.CREATED);
		CTM.put(CTMetaData.ChatThreadsMetaData.MODIFIED, CTMetaData.ChatThreadsMetaData.MODIFIED);
	}
	
	private static final UriMatcher mUriMatcher;
	
	private static final int CHAT_THREADS_ALL = 0;
	private static final int CHAT_THREADS_ONE = 1;
	
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.CONTACTS_TABLE, CHAT_THREADS_ALL);
		mUriMatcher.addURI(CMetaData.AUTHORITY, CMetaData.CONTACTS_TABLE + "/#", CHAT_THREADS_ONE);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(mUriMatcher.match(uri)){
			case CHAT_THREADS_ALL:
				count = db.delete(CTMetaData.CHAT_THREADS_TABLE, where, whereArgs);
				break;
			case CHAT_THREADS_ONE:
				where = CTMetaData.ChatThreadsMetaData.CT_ID + "=" + uri.getPathSegments().get(1);
				count = db.delete(CTMetaData.CHAT_THREADS_TABLE, where, whereArgs);
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
		case CHAT_THREADS_ALL:
		case CHAT_THREADS_ONE:
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
		case CHAT_THREADS_ALL:
			rowId = db.replace(CTMetaData.CHAT_THREADS_TABLE, CTMetaData.ChatThreadsMetaData.CT_ID, values);
			insUri = ContentUris.withAppendedId(CTMetaData.ChatThreadsMetaData.CONTENT_URI, rowId);
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
		case CHAT_THREADS_ALL:
			qb.setTables(CTMetaData.CHAT_THREADS_TABLE);
			qb.setProjectionMap(CTM);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CTMetaData.ChatThreadsMetaData.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case CHAT_THREADS_ONE:
			qb.setTables(CTMetaData.CHAT_THREADS_TABLE);
			qb.setProjectionMap(CTM);
			qb.appendWhere(CTMetaData.ChatThreadsMetaData.CT_ID + "=" + uri.getPathSegments().get(1));
			orderBy = (TextUtils.isEmpty(sortOrder)) ? CTMetaData.ChatThreadsMetaData.DEFAULT_SORT_ORDER : sortOrder;
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
		case CHAT_THREADS_ALL:
			count = db.update(CTMetaData.CHAT_THREADS_TABLE, values, where, whereArgs);
			break;
		case CHAT_THREADS_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.update(
				CTMetaData.CHAT_THREADS_TABLE,
				values,
				CTMetaData.ChatThreadsMetaData.CT_ID+"="+rowId+(!TextUtils.isEmpty(where)?"AND("+where+")":""),
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
			super(context, CTMetaData.DATABASE_NAME, null, CTMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query = "CREATE TABLE " + CTMetaData.CHAT_THREADS_TABLE + " (" 
					+ CTMetaData.ChatThreadsMetaData.CT_ID + " INTEGER"
					+ CTMetaData.ChatThreadsMetaData.E_ID + "INTEGER"
					+ CTMetaData.ChatThreadsMetaData.CONTACT_AUTHOR_ID + "INTEGER"
					+ CTMetaData.ChatThreadsMetaData.MESSAGE_COUNT + "INTEGER" 
					+ CTMetaData.ChatThreadsMetaData.MESSAGE_LAST + "STRING"
					+ CTMetaData.ChatThreadsMetaData.NEW_MESSAGES + "INTEGER"
					+ CTMetaData.ChatThreadsMetaData.SUCCESSFULLY_UPLOADED + "INTEGER DEFAULT 0"
					+ CTMetaData.ChatThreadsMetaData.CREATED + "INTEGER"
					+ CTMetaData.ChatThreadsMetaData.MODIFIED + "INTEGER"
					+ ")";

			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		}
	}

}
