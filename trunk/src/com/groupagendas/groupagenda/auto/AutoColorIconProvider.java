package com.groupagendas.groupagenda.auto;

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
import com.groupagendas.groupagenda.metadata.impl.AutoColorIconMetaData;

/**
 * @author Tadas
 */
public class AutoColorIconProvider extends ContentProvider implements AutoColorIconMetaData {
	private static final Map<String, String> COLOR_PROJ;
	private static final Map<String, String> ICON_PROJ;
	private static final UriMatcher mUriMatcher;
	
	private static final String AI;
	private static final int AUTOICON = 1;
	private static final int AUTOICON_ONE = 3;
	
	private static final String AC;
	private static final int AUTOCOLOR = 2;
	private static final int AUTOCOLOR_ONE = 4;
	
	static {
		COLOR_PROJ = MetaUtils.getFullProjectionTable(AutoColor.class);
		ICON_PROJ = MetaUtils.getFullProjectionTable(AutoIcon.class);

		AC = MetaUtils.getName(AutoColor.class);
		AI = MetaUtils.getName(AutoIcon.class);
		
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		mUriMatcher.addURI(AutoColorIconProvider.class.getName(), AC, AUTOCOLOR);
		mUriMatcher.addURI(AutoColorIconProvider.class.getName(), AC + "/#", AUTOCOLOR_ONE);
		
		mUriMatcher.addURI(AutoColorIconProvider.class.getName(), AI, AUTOICON);
		mUriMatcher.addURI(AutoColorIconProvider.class.getName(), AI + "/#", AUTOICON_ONE);
	}

	private DatabaseHelper mOpenHelper;
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
			case AUTOCOLOR:
				count = db.delete(AC, where, whereArgs);
				break;
			case AUTOCOLOR_ONE:
				String whereStr2 = AutoColor.C_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
				count = db.delete(AC, whereStr2, whereArgs);
				break;
			case AUTOICON:
				count = db.delete(AI, where, whereArgs);
				break;
			case AUTOICON_ONE:
				String whereStr = AutoIcon.I_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
				count = db.delete(AI, whereStr, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
			case AUTOCOLOR:
			case AUTOCOLOR_ONE:
				return AutoColor.CONTENT_TYPE;
			case AUTOICON:
			case AUTOICON_ONE:
				return AutoIcon.CONTENT_TYPE;
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
		case AUTOCOLOR:
			rowId = db.insert(AC, AutoColor.CONTEXT, values);
			insUri = ContentUris.withAppendedId(MetaUtils.getContentUri(AutoColor.class), rowId);
			break;
		case AUTOICON:
			rowId = db.insert(AI, AutoIcon.CONTEXT, values);
			insUri = ContentUris.withAppendedId(MetaUtils.getContentUri(AutoIcon.class), rowId);
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
		return (mOpenHelper == null) ? false : true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String orderBy;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case AUTOCOLOR:
			qb.setTables(AC);
			qb.setProjectionMap(COLOR_PROJ);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? AutoColor.DEFAULT_SORT_ORDER : sortOrder;
			break;
		case AUTOICON:
			qb.setTables(AI);
			qb.setProjectionMap(ICON_PROJ);
			orderBy = (TextUtils.isEmpty(sortOrder)) ? AutoIcon.DEFAULT_SORT_ORDER : sortOrder;
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
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		int count;

		switch (mUriMatcher.match(uri)) {
		case AUTOICON:
			count = db.update(AI, values, where, whereArgs);
			break;
		case AUTOICON_ONE:
			String whereStr = AutoIcon.I_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(AI, values, whereStr, whereArgs);
			break;
		case AUTOCOLOR:
			count = db.update(AC, values, where, whereArgs);
			break;
		case AUTOCOLOR_ONE:
			String where2Str = AutoColor.C_ID+"="+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(where)?"AND("+where+")":"");
			count = db.update(AC, values, where2Str, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}
	
	private static class DatabaseHelper extends AnnotatedDbHelper<AutoColorIconMetaData> {
		protected DatabaseHelper(Context context) {
			super(context, AutoColorIconMetaData.class);
		}
	}
}
