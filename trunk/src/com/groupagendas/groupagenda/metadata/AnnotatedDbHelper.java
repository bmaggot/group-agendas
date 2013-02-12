package com.groupagendas.groupagenda.metadata;

import java.lang.reflect.Field;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.groupagendas.groupagenda.metadata.anno.Database;
import com.groupagendas.groupagenda.metadata.anno.Table;
import com.groupagendas.groupagenda.metadata.anno.TableColumn;

/**
 * A class that automates database creation if fields are declared in a supported metadata format.
 * 
 * @author Tadas
 */
public abstract class AnnotatedDbHelper<T extends IMetaData> extends SQLiteOpenHelper {
	private final Class<T> database;
	
	protected AnnotatedDbHelper(Context context, Class<T> database) {
		super(context, database.getAnnotation(Database.class).name(), null,
				database.getAnnotation(Database.class).version());
		this.database = database;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		final StringBuilder sb = new StringBuilder();
		for (Class<?> c : getDatabase().getClasses()) {
			Table t = c.getAnnotation(Table.class);
			if (t == null)
				continue;
			
			for (Field f : c.getFields()) {
				TableColumn tc = f.getAnnotation(TableColumn.class);
				if (tc == null)
					continue;
				
				try {
					sb.append(f.get(null));
				} catch (IllegalAccessException e) {
					Log.e("AnnotatedDbHelper",
							"Did someone change the for cycle to use getDeclaredFields()? *cough*morons*cough*",
							e);
				}
				sb.append(' ');
				sb.append(tc.databaseType().getDbAlias());
				if (tc.databaseConstraints().length() > 0) {
					sb.append(' ');
					sb.append(tc.databaseConstraints());
				}
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			
			db.execSQL(IMetaData.CREATE_TABLE.replace("%name%", t.name()).replace("%struct%", sb.toString()));
			sb.setLength(0);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// If you were looking for this message, you'd better override this method in an extending class.
		Log.w(getDatabase().getSimpleName(), "Local DB (version " + oldVersion + ") will not be updated to version " + newVersion);
	}
	
	private Class<T> getDatabase() {
		return database;
	}
}
