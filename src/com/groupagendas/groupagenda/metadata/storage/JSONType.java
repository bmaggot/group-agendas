package com.groupagendas.groupagenda.metadata.storage;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Defines possible JSON datatypes.
 * 
 * @author Tadas
 */
public enum JSONType {
	/** Type of data that is not transferred via JSON */
	TRANSIENT(null, null),
	/** Infer type from SQLite type, if possible */
	INFER(null, null),
	OBJECT("", Object.class),
	BOOLEAN("Boolean", Boolean.TYPE),
	DOUBLE("Double", Double.TYPE),
	INT("Int", Integer.TYPE),
	JSON_ARRAY("JSONArray", JSONArray.class),
	JSON_OBJECT("JSONObject", JSONObject.class),
	LONG("Long", Long.TYPE),
	STRING("String", String.class);
	
	private final Class<?> jsonType;
	private final Method getter;
	
	private JSONType(String getterAlias, Class<?> jsonType) {
		this.jsonType = jsonType;
		
		if (getterAlias == null) {
			getter = null;
			return;
		}
		try {
			this.getter = JSONObject.class.getMethod("get" + getterAlias, String.class);
		} catch (Exception e) {
			throw new RuntimeException("Invalid JSON type definition: " + name() +
					", no such getter: get" + getterAlias + "(String)");
		}
	}
	
	public Class<?> getDbType() {
		return jsonType;
	}
	
	public Method getGetter() {
		return getter;
	}
}
