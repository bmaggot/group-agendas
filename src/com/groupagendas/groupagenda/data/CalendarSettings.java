/**
 * This class acts as an interface to account stored in data class. It is responsible for safe calendar settings setting and getting.
 * @author justinas.marcinka@gmail.com
 */
package com.groupagendas.groupagenda.data;



import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.groupagendas.groupagenda.account.Account;

public class CalendarSettings {
	
	public static final int[] DEFAULT_WEEKENDS = {1,7};
	
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	/**
	 * date format that is currently in use by date formatter. It may differ date format set on account.
	 */
	private static String dateFormat;
	
	private static final int DEFAULT_FIRST_DAY_OF_WEEK = Calendar.MONDAY;
	
	private static final String DEFAULT_SETTING_DEFAULT_VIEW = "m"; //MONTH VIEW

	private static final boolean DEFAULT_AM_PM_SETTING = false;
	
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

	public static int[] getWeekends() {
//		TODO get weekends from account settings
		return DEFAULT_WEEKENDS;
	}	

	public static boolean isUsing_AM_PM() {
		if (Data.getAccount() == null) return DEFAULT_AM_PM_SETTING;
		return (Data.getAccount().setting_ampm == 1);
	}	
	
	public static void setUsing_AM_PM(boolean bool) {
		Account acc = Data.getAccount();
		if (acc != null) {
			if (bool){
				acc.setting_ampm = 1;
			}
		}
	}	

	public static void setDateFormat(String date_format) {
		Account account = Data.getAccount();
		if (account != null){
			account.setting_date_format = date_format;
		}
	}

	public static String getDateFormat() {
		Account account = Data.getAccount();
		if (account != null) {
			if (account.setting_date_format != null) {
				if (!account.setting_date_format.equalsIgnoreCase("null")) {
					dateFormat = account.setting_date_format;
					return dateFormat;
				}
			}
		}
		dateFormat = DEFAULT_DATE_FORMAT;
		return dateFormat;
	}
	
	/**
	 * Method that returns date formatter according to current account selected date format.
	 * It is mainly used by Utils method formatCalendar.
	 * @author justinas.marcinka@gmail.com
	 * @return date formatter
	 * @see com.groupagendas.groupagenda.utils.Utils
	 */
	public static SimpleDateFormat getDateFormatter() {
		Account account = Data.getAccount();	
		if (account.setting_date_format != null){
			if (!account.setting_date_format.equalsIgnoreCase("null")){
				if (!account.setting_date_format.equalsIgnoreCase(dateFormat)){
					dateFormat = account.setting_date_format;
					dateFormatter = new SimpleDateFormat(dateFormat);
				}
			}
		}
		return dateFormatter;
	}

	public static int getFirstDayofWeek() {
//		TODO get first day of week from account
		return DEFAULT_FIRST_DAY_OF_WEEK;
	}

	public static String getDefaultView() {
		if (Data.getAccount() != null) {
			String dw = Data.getAccount().setting_default_view;
			if (dw == null) {
				dw = DEFAULT_DATE_FORMAT;
			} else if (dw.equalsIgnoreCase("null")) {
				dw = DEFAULT_DATE_FORMAT;
			}
			return dw;
		}
		return DEFAULT_DATE_FORMAT;
	}

	
	
	
}
