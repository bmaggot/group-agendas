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
	 * date format that is currently in use by date formatter. It may differ
	 * date format set on account.
	 */
	private static String formatterDateFormat;

	private static final int DEFAULT_FIRST_DAY_OF_WEEK = Calendar.MONDAY;

	private static final String DEFAULT_SETTING_DEFAULT_VIEW = "m"; // MONTH
																	// VIEW
//	private static final boolean DEFAULT_AM_PM_SETTING = false;

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

	public static int[] getWeekends() {
		// TODO get weekends from account settings
		return DEFAULT_WEEKENDS;
	}

	public static String getTimeZone() {
		Account mAccount = new Account();
		return mAccount.getTimezone();
	}

	public static boolean isUsing_AM_PM() {
		Account mAccount = new Account();
		if (mAccount.getSetting_ampm() == 1)
			return true;
		else
			return false;
	}

	public static void setUsing_AM_PM(boolean bool) {
		Account acc = new Account();
		if (bool) {
			acc.setSetting_ampm(1);
		} else {
			acc.setSetting_ampm(0);
		}
	}

	public static void setDateFormat(String date_format) {
		Account account = new Account();
		account.setSetting_date_format(date_format);
	}

	public static String getDateFormat() {
		Account account = Data.getAccount();
		if (account != null) {
			if (!account.getSetting_date_format().equalsIgnoreCase("null")) {
				formatterDateFormat = account.getSetting_date_format();
				return formatterDateFormat;
			}
		}
		formatterDateFormat = DEFAULT_DATE_FORMAT;
		return formatterDateFormat;
	}

	/**
	 * Method that returns date formatter according to current account selected
	 * date format. It is mainly used by Utils method formatCalendar.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @return date formatter
	 * @see com.groupagendas.groupagenda.utils.Utils
	 */
	public static SimpleDateFormat getDateFormatter() {
		Account account = new Account();
			if (!account.getSetting_date_format().equalsIgnoreCase("null")) {
				if (!account.getSetting_date_format().equalsIgnoreCase(formatterDateFormat)) {
					formatterDateFormat = account.getSetting_date_format();
					dateFormatter = new SimpleDateFormat(formatterDateFormat);
				}
			}
		return dateFormatter;
	}

	public static int getFirstDayofWeek() {
		// TODO get first day of week from account
		return DEFAULT_FIRST_DAY_OF_WEEK;
	}

	public static String getDefaultView() {
		Account account = new Account();
		String dw = account.getSetting_default_view();
		if (dw == null) {
			dw = DEFAULT_SETTING_DEFAULT_VIEW;
		} else if (dw.equalsIgnoreCase("null")) {
			dw = DEFAULT_SETTING_DEFAULT_VIEW;
		}
		return dw;
	}


	/**
	 * Sets default view with one defined in
	 * CalendarSettings.DEFAULT_SETTING_DEFAULT_VIEW
	 */
	public static void setDefaultView() {
		Account account = new Account();
		account.setSetting_default_view(DEFAULT_SETTING_DEFAULT_VIEW);
	}
}
