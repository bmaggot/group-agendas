package com.groupagendas.groupagenda.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.groupagendas.groupagenda.metadata.IMetaData.ITable;
import com.groupagendas.groupagenda.metadata.anno.Database;
import com.groupagendas.groupagenda.metadata.anno.Table;
import com.groupagendas.groupagenda.metadata.anno.TableColumn;
import com.groupagendas.groupagenda.metadata.anno.ValueConversion;
import com.groupagendas.groupagenda.metadata.storage.JSONType;
import com.groupagendas.groupagenda.metadata.storage.SQLiteType;
import com.groupagendas.groupagenda.utils.StringValueUtils;

/**
 * Utilities related to annotated database management.
 * 
 * @author Tadas
 */
public final class MetaUtils {
	private static final Map<Class<? extends ITable>, Uri> URI = new HashMap<Class<? extends ITable>, Uri>();
	
	public static Uri getContentUri(Class<? extends ITable> meta) {
		Uri u = URI.get(meta);
		if (u == null) {
			StringBuilder sb = new StringBuilder("content://");
			Table t = meta.getAnnotation(Table.class);
			sb.append(t.databaseMetadata().getAnnotation(Database.class).authority().getName());
			sb.append('/');
			sb.append(t.name());
			u = Uri.parse(sb.toString());
			URI.put(meta, u);
		}
		return u;
	}
	
	public static String getName(Class<? extends ITable> meta) {
		return meta.getAnnotation(Table.class).name();
	}
	
	public static Map<String, String> getFullProjectionTable(Class<? extends ITable> meta) {
		Map<String, String> allColumns = new HashMap<String, String>();
		for (Field f : meta.getFields()) {
			TableColumn tc = f.getAnnotation(TableColumn.class);
			if (tc == null)
				continue;
			
			try {
				String s = StringValueUtils.valueOf(f.get(null));
				allColumns.put(s, s);
			} catch (Exception e) {
				Log.e(MetaUtils.class.getSimpleName(),
						"An invalid field is annotated as TableColumn: " + f.getName() +
						" in class " + meta.getName(), e);
			}
		}
		return Collections.unmodifiableMap(allColumns);
	}
	
	private static String valueOf(Field f) {
		try {
			return StringValueUtils.valueOf(f.get(null));
		} catch (Exception e) {
			Log.e(MetaUtils.class.getSimpleName(),
					"An invalid field is annotated as TableColumn: " + f.getName() +
					" in class " + f.getDeclaringClass().getName(), e);
			return null;
		}
	}
	
	private static Method getSetter(Class<?> type, String name, Object val) throws NoSuchMethodException {
		Method setter;
		try {
			setter = type.getMethod(name, val.getClass());
		} catch (NoSuchMethodException e) {
			try {
				Field primitive = val.getClass().getField("TYPE");
				setter = type.getMethod(name, (Class<?>) primitive.get(null));
			} catch (Exception innerE) {
				throw e;
			}
		}
		return setter;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createFromCursor(Cursor cur, Class<? extends ITable> meta, Class<T> type) {
		Table t = meta.getAnnotation(Table.class);
		if (!type.isAssignableFrom(t.bindTo()))
			throw new RuntimeException("Table " + t.name() + " binds to " + t.bindTo().getName() +
					", but requested type is " + type.getName());
		
		return (T) createFromCursor(cur, meta);
	}
	
	public static Object createFromCursor(Cursor cur, Class<? extends ITable> meta) {
		Table t = meta.getAnnotation(Table.class);
		Class<?> type = t.bindTo();
		Object result;
		try {
			result = type.newInstance();
		} catch (Exception e) {
			Log.e(MetaUtils.class.getSimpleName(),
					"Table " + t.name() + " declares incompatible binding class: " + t.bindTo().getName(), e);
			return null;
		}
		
		for (Field f : meta.getFields()) {
			TableColumn tc = f.getAnnotation(TableColumn.class);
			if (tc == null)
				continue;
			
			String colName = valueOf(f);
			if (colName == null)
				continue;
			
			int idx = cur.getColumnIndex(colName);
			
			SQLiteType dbt = tc.databaseType();
			Method getter = dbt.getGetter();
			try {
				Object val = getter.invoke(cur, idx);
				TypeConversion converter = tc.converter();
				if (converter != TypeConversion.NONE) {
					for (Method conv : converter.getConverter().getClass().getMethods()) {
						ValueConversion vc = conv.getAnnotation(ValueConversion.class);
						if (vc == null || vc.forJSON() || !vc.toRuntimeType())
							continue;
						
						Class<?>[] params = conv.getParameterTypes();
						if (params.length != 1) {
							Log.e(MetaUtils.class.getSimpleName(), "An invalid method is annotated for value conversion: " + conv.getName() + "()");
							continue;
						}
						
						if (params[0] != val.getClass())
							continue;
						
						val = conv.invoke(converter.getConverter(), val);
					}
				}
				
				String setName = tc.bindingSetterAlias();
				if (setName.length() == 0)
					setName = generateMethodName("set", colName);
				
				Method setter = getSetter(type, setName, val);
				setter.invoke(result, val);
			} catch (Exception e) {
				Log.e(MetaUtils.class.getSimpleName(),
						"Failed binding column " + colName + ", possibly invalid definition", e);
			}
		}
		
		return result;
	}
	
	public static ContentValues getContentValues(Class<? extends ITable> meta, Object bound) {
		ContentValues cv = new ContentValues();
		{
			Class<?> type = bound.getClass();
			Table t = meta.getAnnotation(Table.class);
			if (!type.isAssignableFrom(t.bindTo()))
				throw new IllegalArgumentException("Table " + t.name() + " binds to " + t.bindTo().getName() +
						", but given type is " + type.getName());
			
			for (Field f : meta.getFields()) {
				TableColumn tc = f.getAnnotation(TableColumn.class);
				if (tc == null || tc.excludeFromCV())
					continue;
				
				String colName = valueOf(f);
				if (colName == null)
					continue;
				
				String getName = tc.bindingGetterAlias();
				if (getName.length() == 0)
					getName = generateMethodName("get", colName);
				
				try {
					Method getter;
					try {
						getter = type.getMethod(getName);
					} catch (NoSuchMethodException e) {
						if (tc.bindingGetterAlias().length() == 0) {
							getName = generateMethodName("is", colName);
							try {
								getter = type.getMethod(getName);
							} catch (Exception innerEx) {
								throw e;
							}
						} else
							throw e;
					}
					Object val = getter.invoke(bound);
					
					TypeConversion converter = tc.converter();
					if (converter != TypeConversion.NONE) {
						for (Method conv : converter.getConverter().getClass().getMethods()) {
							ValueConversion vc = conv.getAnnotation(ValueConversion.class);
							if (vc == null || vc.forJSON() || vc.toRuntimeType())
								continue;
							
							Class<?>[] params = conv.getParameterTypes();
							if (params.length != 1) {
								Log.e(MetaUtils.class.getSimpleName(), "An invalid method is annotated for value conversion: " + conv.getName() + "()");
								continue;
							}
							
							if (params[0] != val.getClass())
								continue;
							
							val = conv.invoke(converter.getConverter(), val);
						}
					}
					
					Method setter = ContentValues.class.getMethod("put", String.class, val.getClass());
					setter.invoke(cv, colName, val);
				} catch (Exception e) {
					Log.e(MetaUtils.class.getSimpleName(),
							"Failed binding column " + colName + ", possibly invalid definition", e);
				}
			}
		}
		return cv;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createFromJSON(JSONObject jobj, Class<? extends ITable> meta, Class<T> type) throws JSONException {
		Table t = meta.getAnnotation(Table.class);
		if (!type.isAssignableFrom(t.bindTo()))
			throw new RuntimeException("Table " + t.name() + " binds to " + t.bindTo().getName() +
					", but requested type is " + type.getName());
		
		return (T) createFromJSON(jobj, meta);
	}
	
	public static Object createFromJSON(JSONObject jobj, Class<? extends ITable> meta) throws JSONException {
		Table t = meta.getAnnotation(Table.class);
		Class<?> type = t.bindTo();
		Object result;
		try {
			result = type.newInstance();
		} catch (Exception e) {
			Log.e(MetaUtils.class.getSimpleName(),
					"Table " + t.name() + " declares incompatible binding class: " + t.bindTo().getName(), e);
			return null;
		}
		
		for (Field f : meta.getFields()) {
			TableColumn tc = f.getAnnotation(TableColumn.class);
			if (tc == null)
				continue;
			
			String colName = valueOf(f);
			if (colName == null)
				continue;
			
			JSONType jt = tc.jsonType();
			if (jt == JSONType.TRANSIENT)
				continue;
			if (jt == JSONType.INFER) {
				jt = tc.databaseType().getInferredJSON();
				if (jt == null)
					throw new RuntimeException("Cannot infer JSON type for column " + colName +
							" in table " + t.name());
			}
			
			String jsonName = tc.jsonName();
			if (jsonName.length() == 0)
				jsonName = colName;
			
			Method getter = jt.getGetter();
			try {
				Object val;
				try {
					val = getter.invoke(jobj, jsonName);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof Exception)
						throw (Exception) e.getCause();
					else
						throw e;
				}
				
				TypeConversion converter = tc.converter();
				if (converter != TypeConversion.NONE) {
					for (Method conv : converter.getConverter().getClass().getMethods()) {
						ValueConversion vc = conv.getAnnotation(ValueConversion.class);
						if (vc == null || !vc.forJSON() || !vc.toRuntimeType())
							continue;
						
						Class<?>[] params = conv.getParameterTypes();
						if (params.length != 1) {
							Log.e(MetaUtils.class.getSimpleName(), "An invalid method is annotated for value conversion: " + conv.getName() + "()");
							continue;
						}
						
						if (params[0] != val.getClass())
							continue;
						
						val = conv.invoke(converter.getConverter(), val);
					}
				}
				
				String setName = tc.bindingSetterAlias();
				if (setName.length() == 0) 
					setName = generateMethodName("set", colName);
				Method setter = getSetter(type, setName, val);
				setter.invoke(result, val);
			}
			catch (JSONException e) {
				throw e;
			}
			catch (Exception e) {
				Log.e(MetaUtils.class.getSimpleName(),
						"Failed binding column " + colName + ", possibly invalid definition", e);
			}
		}
		
		return result;
	}
	
	private static String generateMethodName(String prefix, String colName) {
		StringBuilder sb = new StringBuilder(prefix);
		sb.append(colName);
		sb.setCharAt(prefix.length(), Character.toUpperCase(colName.charAt(0)));
		return sb.toString();
	}
	
	private MetaUtils() {
		// utility class
	}
}
