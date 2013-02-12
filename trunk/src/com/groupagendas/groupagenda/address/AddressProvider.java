package com.groupagendas.groupagenda.address;

import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.groupagendas.groupagenda.metadata.AnnotatedDbHelper;
import com.groupagendas.groupagenda.metadata.MetaUtils;
import com.groupagendas.groupagenda.metadata.impl.AddressMetaData;

public class AddressProvider extends ContentProvider implements AddressMetaData {
	private DatabaseHelper mOpenHelper;

	private static final String TABLE;
	/* UriMatcher */
	private static final UriMatcher mUriMatcher;
	private static final Map<String, String> ALL_COLUMNS;

	public static final int ALL_ADDRESSES = 0;
	public static final int SINGLE_ADDRESS = 1;

	static {
		TABLE = MetaUtils.getName(AddressTable.class);
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AddressProvider.class.getName(), TABLE, ALL_ADDRESSES);
		mUriMatcher.addURI(AddressProvider.class.getName(), TABLE + "/#", SINGLE_ADDRESS);
		ALL_COLUMNS = MetaUtils.getFullProjectionTable(AddressTable.class);
	}

	@Override
	public int delete(Uri uri, String where, String[] selectionArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(mUriMatcher.match(uri)){
			case ALL_ADDRESSES:
				count = db.delete(TABLE, where, selectionArgs);
				break;
			case SINGLE_ADDRESS:
				String whereStr = AddressTable.A_ID + "=" + uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND(" + where + ")":"");
				count = db.delete(TABLE, whereStr, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI "+uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case ALL_ADDRESSES:
		case SINGLE_ADDRESS:
			return AddressTable.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = 0;
		Uri insUri;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case ALL_ADDRESSES:
			rowId = db.replace(TABLE, AddressTable.A_ID, values);
			insUri = ContentUris.withAppendedId(MetaUtils.getContentUri(AddressTable.class), rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(insUri, null);
		return insUri;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return (mOpenHelper != null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String orderBy;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (mUriMatcher.match(uri)) {
			case ALL_ADDRESSES:
				qb.setTables(TABLE);
				qb.setProjectionMap(ALL_COLUMNS);
				orderBy = (TextUtils.isEmpty(sortOrder)) ? AddressTable.DEFAULT_SORT_ORDER : sortOrder;
				break;
			case SINGLE_ADDRESS:
				qb.setTables(TABLE);
				qb.setProjectionMap(ALL_COLUMNS);
				qb.appendWhere(AddressTable.A_ID + "=" + uri.getPathSegments().get(1));
				orderBy = (TextUtils.isEmpty(sortOrder)) ? AddressTable.DEFAULT_SORT_ORDER : sortOrder;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
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
			count = db.update(TABLE, values, selection, selectionArgs);
			break;
		case SINGLE_ADDRESS:
			String whereStr = AddressTable._ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection)?"AND(" + selection + ")":"");
			count = db.update(TABLE, values, whereStr, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	private static class DatabaseHelper extends AnnotatedDbHelper<AddressMetaData> {
		protected DatabaseHelper(Context context) {
			super(context, AddressMetaData.class);
		}
	}
}