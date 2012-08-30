package com.groupagendas.groupagenda.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarSettings {
	
	public static final int[] DEFAULT_WEEKENDS = {1,7};
	private static  int[] weekends; //TODO remove this hardcode. should be set when loading account.
	
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	private static String dateFormat;
	
	public static final int DEFAULT_FIRST_DAY_OF_WEEK = Calendar.MONDAY;
	private static int firstDayOfWeek = 0;
	
	public static SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

	public static int[] getWeekends() {
		if (weekends == null) weekends = DEFAULT_WEEKENDS;
		return weekends;
	}

	public static void setWeekends(int[] weekends) {
		CalendarSettings.weekends = weekends;
	}

	

	public static void setDateFormat(String date_format) {
		dateFormatter = new SimpleDateFormat(date_format);
		CalendarSettings.dateFormat = date_format;
	}

	public static String getDateFormat() {
		if (dateFormat == null) dateFormat = DEFAULT_DATE_FORMAT;
		return dateFormat;
	}

	public static int getFirstDayofWeek() {
		if (firstDayOfWeek == 0) firstDayOfWeek = DEFAULT_FIRST_DAY_OF_WEEK;
		return firstDayOfWeek;
	}
	
	
	
}
