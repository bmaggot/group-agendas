package com.groupagendas.groupagenda.metadata.storage;

import java.lang.reflect.Method;

import android.database.Cursor;

/**
 * Defines possible SQLite datatypes.
 * 
 * @author Tadas
 */
public enum SQLiteType {
	BLOB("???", "Blob", byte[].class, null),
	DOUBLE("???", "Double", Double.TYPE, JSONType.DOUBLE),
	FLOAT("???", "Float", Float.TYPE, JSONType.DOUBLE),
	INT("INTEGER", "Int", Integer.TYPE, JSONType.INT),
	LONG("???", "Long", Long.TYPE, JSONType.LONG),
	SHORT("???", "Short", Short.TYPE, JSONType.INT),
	STRING("TEXT", "String", String.class, JSONType.STRING);
	
	private final String dbAlias;
	// private final String getterAlias;
	private final Class<?> dbType;
	private final Method getter;
	private final JSONType inferredJSON;
	
	private SQLiteType(String dbAlias, String getterAlias, Class<?> dbType, JSONType inferredJSON) {
		this.dbAlias = dbAlias;
		// this.getterAlias = getterAlias;
		this.dbType = dbType;
		try {
			this.getter = Cursor.class.getMethod("get" + getterAlias, Integer.TYPE);
		} catch (Exception e) {
			throw new RuntimeException("Invalid SQLite type definition: " + name() + ", no such getter: get" + getterAlias + "(int)");
		}
		this.inferredJSON = inferredJSON;
	}
	
	public String getDbAlias() {
		return dbAlias;
	}
	
	/*
	public String getGetterAlias() {
		return getterAlias;
	}
	*/
	
	public Class<?> getDbType() {
		return dbType;
	}
	
	public Method getGetter() {
		return getter;
	}
	
	public JSONType getInferredJSON() {
		return inferredJSON;
	}
}
