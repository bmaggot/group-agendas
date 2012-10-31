package com.groupagendas.groupagenda.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.error.report.Reporter;

public class DateTimeUtils {
	public static final String DEFAULT_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_DATE = "yyyy-MM-dd";
	public static final String DEFAULT_TIME = "HH:mm";

	private String dateFormat;
	private SimpleDateFormat mDateFormater;


	private boolean am_pm;
	private String dateTimeFormat;
	private SimpleDateFormat mDateTimeFormater;
	private SimpleDateFormat dDateTimeFormater;

	private String timeFormat;
	private SimpleDateFormat mTimeFormater;
	private SimpleDateFormat dTimeFormater;
	private Account account;
	private SimpleDateFormat dDateFormater;

	public DateTimeUtils(Context context) {
		account = new Account(context);

		dateFormat = account.getSetting_date_format();
		mDateFormater = new SimpleDateFormat(dateFormat);
		dDateFormater = new SimpleDateFormat(DEFAULT_DATE);

		am_pm = account.getSetting_ampm() == 1;

		if (am_pm) {
			timeFormat = context.getString(R.string.time_format_AMPM);
			
		} else {
			timeFormat = context.getString(R.string.time_format);		
		}
		
		dateTimeFormat = dateFormat + " " + timeFormat;		
		mDateTimeFormater = new SimpleDateFormat(dateTimeFormat);

		mTimeFormater = new SimpleDateFormat(timeFormat);

	}

	/**
	 * Formats given calendar to String, using user selected date format
	 * @author justinas.marcinka@gmail.com
	 * @param calendar calendar to format
	 * @return
	 */
	public String formatDate(Calendar calendar){
		return mDateFormater.format(calendar.getTime());
	}
	
	/**
	 * Formats given calendar to String, using user selected time format
	 * @author justinas.marcinka@gmail.com
	 * @param calendar calendar to format
	 * @return
	 */
	public String formatTime(Calendar calendar){
		return mTimeFormater.format(calendar.getTime());
	}
	
	/**
	 * Formats given calendar to String, using user selected time and date formats
	 * @author justinas.marcinka@gmail.com
	 * @param calendar calendar to format
	 * @return
	 */
	public String formatDateTime(Calendar calendar){
		return mDateTimeFormater.format(calendar.getTime());
	}

	public String formatDate(Date date) {
		return mDateFormater.format(date);
	}

	public String formatDate(long milis) {
		return mDateFormater.format(milis);
	}


	/**
	 * @deprecated not reliable
	 * @param date
	 * @return
	 */
	public Calendar stringDateToCalendar(Context context, String date){
		Calendar c = Calendar.getInstance();
		try {
			Date dateObj = mDateFormater.parse(date);
			c.setTime(dateObj);
		} catch (ParseException e) {
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		return c;
	}
	// Date and time
	/**
	 * @deprecated not reliable
	 * @param date
	 * @return
	 */
	public String formatDateTime(Context context, String date) {
		String formatedDate = "";
		try {
			Date dateObj = dDateTimeFormater.parse(date);
			formatedDate = mDateTimeFormater.format(dateObj);
		} catch (ParseException e) {
			Reporter.reportError(context, this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}

		return formatedDate;
	}

	public String formatDateTime(Date date) {
		return mDateTimeFormater.format(date);
	}
	
	
	/**
	 * @deprecated not reliable
	 * @param date
	 * @return
	 */
	public String formatDateToDefault(Date date) {
		return dDateFormater.format(date);
	}
	/**
	 * @deprecated not reliable
	 * @param date
	 * @return
	 */
	public String formatDateTimeToDefault(Date date) {
		return dDateTimeFormater.format(date);
	}

	// Time
	public String formatTime(long milis) {
		return mTimeFormater.format(milis);
	}

	/**
	 * @deprecated not reliable
	 * @param date
	 * @return
	 */
	public String formatTimeToDefault(Date date) {
		return dTimeFormater.format(date);
	}

}
