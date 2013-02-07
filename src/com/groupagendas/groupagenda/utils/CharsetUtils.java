package com.groupagendas.groupagenda.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import android.util.Log;

/**
 * A convenience class to avoid catching {@link UnsupportedEncodingException} all over the code.<BR>
 * <BR>
 * Allows to change the web service encoding without adjusting relevant code.
 * 
 * @author Tadas
 */
public final class CharsetUtils {
	private static final String DEFAULT_FALLBACK_CHARSET_NAME;
	private static final String DEFAULT_WEB_SERVICE_CHARSET_NAME;
	private static final Charset DEFAULT_WEB_SERVICE_CHARSET;
	
	private CharsetUtils() {
		// utility class
	}
	
	public static Charset getWebServiceCharset() {
		return DEFAULT_WEB_SERVICE_CHARSET;
	}
	
	public static StringBody encodeForWebService(Object o) {
		try {
			return new StringBody(StringValueUtils.valueOf(o), getWebServiceCharset());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static StringBody encodeForWebService(int o) {
		try {
			return new StringBody(StringValueUtils.valueOf(o), getWebServiceCharset());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static StringBody encodeForWebService(long o) {
		try {
			return new StringBody(StringValueUtils.valueOf(o), getWebServiceCharset());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static StringBody encodeForWebService(boolean o) {
		try {
			return new StringBody(StringValueUtils.valueOf(o), getWebServiceCharset());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Adds a specified part to the given multipart entity with value encoded as string.<BR>
	 * <BR>
	 * Null values do not throw a NPE, as they are converted to the {@code "null"} string.
	 * 
	 * @param entity A multipart entity
	 * @param key part name
	 * @param value part value
	 */
	public static void addPart(MultipartEntity entity, Object key, Object value) {
		entity.addPart(String.valueOf(key), encodeForWebService(value));
	}
	
	/**
	 * Adds a specified part to the given multipart entity with value encoded as string.<BR>
	 * <BR>
	 * A {@code null} key will be converted into the {@code "null"} string.
	 * 
	 * @param entity A multipart entity
	 * @param key part name
	 * @param value part value
	 */
	public static void addPart(MultipartEntity entity, String key, int value) {
		entity.addPart(String.valueOf(key), encodeForWebService(value));
	}
	
	/**
	 * Adds a specified part to the given multipart entity with value encoded as string.<BR>
	 * <BR>
	 * A {@code null} key will be converted into the {@code "null"} string.
	 * 
	 * @param entity A multipart entity
	 * @param key part name
	 * @param value part value
	 */
	public static void addPart(MultipartEntity entity, String key, long value) {
		entity.addPart(String.valueOf(key), encodeForWebService(value));
	}
	
	/**
	 * Adds a specified part to the given multipart entity with value encoded as string.<BR>
	 * <BR>
	 * A {@code null} key will be converted into the {@code "null"} string.
	 * 
	 * @param entity A multipart entity
	 * @param key part name
	 * @param value part value
	 */
	public static void addPart(MultipartEntity entity, String key, boolean value) {
		entity.addPart(String.valueOf(key), encodeForWebService(value));
	}
	
	/**
	 * Adds a specified part to the given multipart entity with value encoded as string.<BR>
	 * <BR>
	 * Does nothing if {@code value == null}. A {@code null} key will be converted into the
	 * {@code "null"} string.
	 * 
	 * @param entity A multipart entity
	 * @param key part name
	 * @param value part value
	 * @return {@code value != null}
	 */
	public static boolean addPartNotNull(MultipartEntity entity, Object key, Object value) {
		if (value == null)
			return false;
		
		addPart(entity, key, value);
		return true;
	}
	
	/**
	 * Adds a specified part to the given multipart entity with value encoded as string.<BR>
	 * <BR>
	 * Null values do not throw a NPE, as they are converted to the {@code "null"} string.
	 * 
	 * @param entity A multipart entity
	 * @param key part name
	 * @param value part value
	 */
	public static void addPartEmptyIfNull(MultipartEntity entity, Object key, Object value) {
		addPart(entity, key, value != null ? value : "");
	}
	
	/**
	 * Adds specified parts to the given multipart entity with their values encoded as strings.<BR>
	 * <BR>
	 * The given array must be even sized, where part names are in the even-numbered indexes and
	 * part values are in the odd-numbered indexes.
	 * 
	 * @param entity A multipart entity
	 * @param keyValuePairs A set of parts in key-value pairs
	 */
	public static void addAllParts(MultipartEntity entity, Object... keyValuePairs) {
		final int size = keyValuePairs.length;
		if ((size & 1) != 0)
			throw new IllegalArgumentException("Invalid pairing: " + keyValuePairs);
		
		for (int i = 1; i < size; i += 2)
			entity.addPart(String.valueOf(keyValuePairs[i - 1]), encodeForWebService(keyValuePairs[i]));
	}
	
	static {
		DEFAULT_FALLBACK_CHARSET_NAME = "US-ASCII";
		DEFAULT_WEB_SERVICE_CHARSET_NAME = "UTF-8";
		
		Charset result = Charset.defaultCharset();
		try {
			if (!Charset.isSupported(DEFAULT_WEB_SERVICE_CHARSET_NAME)) {
				Log.e("CharsetUtils", "Catastrophic failure, " + DEFAULT_WEB_SERVICE_CHARSET_NAME + " is not supported. Defaulting to " + DEFAULT_FALLBACK_CHARSET_NAME + ".");
				
				if (!Charset.isSupported(DEFAULT_FALLBACK_CHARSET_NAME)) {
					Log.e("CharsetUtils", "This is getting serious, since " + DEFAULT_FALLBACK_CHARSET_NAME + " is not supported. Will use the default available charset. That will cause problems.");
					
					result = Charset.defaultCharset();
				} else
					result = Charset.forName(DEFAULT_FALLBACK_CHARSET_NAME);
			} else
				result = Charset.forName(DEFAULT_WEB_SERVICE_CHARSET_NAME);
		} catch (UnsupportedCharsetException e) {
			Log.e("CharsetUtils", "Good news, you can blame the Charset#isSupported(String) method for screwing everything up. [Will use default charset]", e);
		}
		DEFAULT_WEB_SERVICE_CHARSET = result;
		
		try {
			new StringBody("", DEFAULT_WEB_SERVICE_CHARSET).hashCode();
		} catch (UnsupportedEncodingException e) {
			Log.e("CharsetUtils", "Well Watson, life is like a Japanese game show. You don't know what the heck is going on.");
		}
	}
}
