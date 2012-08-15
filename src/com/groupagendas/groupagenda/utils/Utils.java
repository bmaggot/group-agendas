package com.groupagendas.groupagenda.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.error.report.Reporter;

import android.graphics.Bitmap;
import android.text.format.DateFormat;

public class Utils {
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			Reporter.reportError(Utils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
		}
	}

	public final static String date_format = "yyyy-MM-dd HH:mm:ss";
	
	public static String formatDateTime(String date_str, String startPattern, String endPattern){
		Calendar calendar = stringToCalendar(date_str, startPattern);
		SimpleDateFormat formatter = new SimpleDateFormat(endPattern);
		return formatter.format(calendar.getTime());
	}
	
	public static Calendar stringToCalendar(String date_str, String tz, String pattern) {
		TimeZone timezone = TimeZone.getTimeZone(tz);
		Calendar calendar = Calendar.getInstance(timezone);
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			Date date = (Date) formatter.parse(date_str);
			calendar.setTime(date);
		} catch (ParseException e) {
			Reporter.reportError(Utils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		return calendar;
	}

	public static Calendar stringToCalendar(String date_str, String pattern) {
		Calendar calendar = Calendar.getInstance();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			Date date = (Date) formatter.parse(date_str);
			calendar.setTime(date);
		} catch (ParseException e) {
			Reporter.reportError(Utils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		return calendar;
	}

	public static int getArrayIndex(String[] array, String specificValue) {
		for (int i = 0, l = array.length; i < l; i++) {
			if (array[i].equals(specificValue)) {
				return i;
			}
		}
		return -1;
	}

	public static int[] jsonStringToArray(String str) throws JSONException {
		JSONArray arr = new JSONArray(str);
		int[] ret = new int[arr.length()];

		for (int i = 0, l = arr.length(); i < l; i++) {
			ret[i] = arr.getInt(i);
		}

		return ret;
	}

	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
		return resizedBitmap;
	}
	
	public static boolean checkEmail(String email) {
		final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
		          "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
		          "\\@" +
		          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
		          "(" +
		          "\\." +
		          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
		          ")+"
		      );
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}
	
	public static String formatDateTime(String input, String format){
		boolean am_pm = true;
		
		Calendar c = stringToCalendar(input, format);
		
		if(am_pm){
			format+=" aaa";
		}
		
		
		
		return (String) DateFormat.format(format, c.getTimeInMillis());
	}

	public static String getStringDateInDifferentLocale(Calendar calendarInUserLocale,
			String timezone) {
		Calendar tmpCalendar = Calendar.getInstance();
		tmpCalendar.set(calendarInUserLocale.get(Calendar.YEAR), 
						calendarInUserLocale.get(Calendar.MONTH), 
						calendarInUserLocale.get(Calendar.DATE), 
						calendarInUserLocale.get(Calendar.HOUR_OF_DAY),
						calendarInUserLocale.get(Calendar.MINUTE), 
						calendarInUserLocale.get(Calendar.SECOND));

		SimpleDateFormat dateFormat = new SimpleDateFormat(Utils.date_format);
		dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		return dateFormat.format(tmpCalendar.getTime());
	}

	public static void setCalendarToFirstDayOfWeek(Calendar date) {
		int firstDayofWeek = date.getFirstDayOfWeek();
		while (date.get(Calendar.DAY_OF_WEEK) != firstDayofWeek)
			date.add(Calendar.DATE, -1);
	}

}