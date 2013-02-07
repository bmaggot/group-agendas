package com.groupagendas.groupagenda.utils;

/**
 * This class allows selective testing for undefined behavior in JDK implementations.<BR>
 * <BR>
 * Please do not commit an uncommented version of this class.
 * 
 * @author Tadas
 */
public final class StringValueUtils {
	// private static boolean USE_ANDROID_IMPLEMENTATION = false;
	
	private StringValueUtils() {
		// utility class
	}
	
	public static String valueOf(Object o) {
		return String.valueOf(o);
	}
	
	public static String valueOf(int o) {
		/*
		if (USE_ANDROID_IMPLEMENTATION) {
			final String s = String.valueOf(o);
			if (o != Integer.parseInt(s))
				Log.e("StringValueUtils", "Received: " + o + ", ended up with " + s);
			return String.valueOf(o);
		}
		*/
		return "" + o;
	}
	
	public static String valueOf(long o) {
		/*
		if (USE_ANDROID_IMPLEMENTATION) {
			final String s = String.valueOf(o);
			if (o != Long.parseLong(s))
				Log.e("StringValueUtils", "Received: " + o + ", ended up with " + s);
			return String.valueOf(o);
		}
		*/
		return "" + o;
	}
	
	public static String valueOf(boolean o) {
		/*
		if (USE_ANDROID_IMPLEMENTATION) {
			final String s = String.valueOf(o);
			if (o != Boolean.parseBoolean(s))
				Log.e("StringValueUtils", "Received: " + o + ", ended up with " + s);
			return String.valueOf(o);
		}
		*/
		return "" + o;
	}
}
