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

import com.groupagendas.groupagenda.data.CalendarSettings;
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
			Date date = formatter.parse(date_str);
			calendar.setTime(date);
		} catch (ParseException e) {
			Reporter.reportError(Utils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		return calendar;
	}

	public static Calendar stringToCalendar(String date_str, String pattern) {
		if(!date_str.equalsIgnoreCase("null")){
			Calendar calendar = Calendar.getInstance();
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(pattern);
				Date date = formatter.parse(date_str);
				calendar.setTime(date);
			} catch (ParseException e) {
				Reporter.reportError(Utils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
			}
			return calendar;
		} else {
			return null;
		}
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

		SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarSettings.getDateFormat());
		dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		return dateFormat.format(tmpCalendar.getTime());
	}
/**
 * @author justinas.marcinka@gmail.com
 * @param date calendar that will be set to first day of week
 * @return Sets given calendar to first day of week accordingly to Calendar.FIRST_DAY_OF_WEEK field
 */
	public static void setCalendarToFirstDayOfWeek(Calendar date) {
		int firstDayofWeek = date.getFirstDayOfWeek();
		while (date.get(Calendar.DAY_OF_WEEK) != firstDayofWeek)
			date.add(Calendar.DATE, -1);
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param date calendar that will be set to first day of month
	 * @return Sets given calendar to first day of month
	 */
		public static void setCalendarToFirstDayOfMonth(Calendar date) {
			while (date.get(Calendar.DAY_OF_MONTH) != 1)
				date.add(Calendar.DATE, -1);
		}
		
		/**
		 * @author justinas.marcinka@gmail.com
		 * @param date calendar that will be set to first day of year
		 * @return Sets given calendar to first day of year
		 */
		
		public static void setCalendarToFirstDayOfYear(Calendar date) {
			date.set(Calendar.MONTH, 0);
			Utils.setCalendarToFirstDayOfMonth(date);
		}
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param date that needs to be checked
	 * @return true if selectedDate is today
	 */
	public static boolean isToday(Calendar selectedDate) {
		Calendar tmp = Calendar.getInstance();
		return tmp.get(Calendar.ERA) == selectedDate.get(Calendar.ERA) &&
                tmp.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                tmp.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR);
	
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param dates that need to be checked
	 * @return true if dates are on the same day
	 */
	
	public static boolean isSameDay(Calendar date1, Calendar date2){
		return date1.get(Calendar.ERA) == date2.get(Calendar.ERA) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR);
	
	}
	
	/**
	 * Creates new today calendar with daytime set to 00:00:00:00 and FirstDayofWeek defined in CalendarSettings.class
	 * @author justinas.marcinka@gmail.com
	 * @return
	 */
	
	public static Calendar createNewTodayCalendar() {
		Calendar tmp = Calendar.getInstance();
		tmp.setFirstDayOfWeek(CalendarSettings.getFirstDayofWeek());
		tmp.set(Calendar.HOUR_OF_DAY, 0);
		tmp.set(Calendar.MINUTE, 0);
		tmp.set(Calendar.SECOND, 0);
		tmp.set(Calendar.MILLISECOND, 0);
		return tmp;
	}

	
	/**
	 * Formats given calendar to String, using user selected format, defined in CalendarSettings
	 * @author justinas.marcinka@gmail.com
	 * @param calendar calendar to format
	 * @return
	 */
	public static String formatCalendar(Calendar calendar){
		return CalendarSettings.getDateFormatter().format(calendar.getTime());
	}
	
	/**
	 * Formats given calendar to String, using given pattern
	 * @author justinas.marcinka@gmail.com
	 * @param calendar calendar to format
	 * @param pattern date format pattern
	 * @return
	 */
	public static String formatCalendar(Calendar calendar, String pattern){
		return new SimpleDateFormat(pattern).format(calendar.getTime());
	}

	/**
	 * Gets day of week parameter of given date, according to user calendar settings. First day: 1
	 * @author justinas.marcinka@gmail.com
	 * @param date
	 * @return day of week number
	 */
	public static int getDayOfWeek(Calendar date) {
		
		Calendar tmp = (Calendar) date.clone();
		
		int i = 1;
		int firstDayofWeek = CalendarSettings.getFirstDayofWeek();
		while (tmp.get(Calendar.DAY_OF_WEEK) != firstDayofWeek){
			tmp.add(Calendar.DATE, - 1);
			i++;
		}
		return i;
	}


/**
 * Indicates whether given calendar date is weekend, according to users calendar settings
 * @author justinas.marcinka@gmail.com
 * @param day
 * @return true, if given date is weekend
 */
	public static boolean isWeekend(Calendar day) {
		for (int i = 0; i < CalendarSettings.getWeekends().length; i++)
			if (day.get(Calendar.DAY_OF_WEEK) == CalendarSettings.getWeekends()[i]) return true;
		return false;
	}
	





}