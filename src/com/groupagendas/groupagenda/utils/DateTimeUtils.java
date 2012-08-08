package com.groupagendas.groupagenda.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.error.report.Reporter;

public class DateTimeUtils {
	public static final String DEFAULT_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_DATE = "yyyy-MM-dd";
	public static final String DEFAULT_TIME = "HH:mm";

	private Prefs prefs;

	private String dateFormat;
	private SimpleDateFormat mDateFormater;
	private SimpleDateFormat dDateFormater;

	private String am_pm;
	private String dateTimeFormat;
	private SimpleDateFormat mDateTimeFormater;
	private SimpleDateFormat dDateTimeFormater;

	private String timeFormat;
	private SimpleDateFormat mTimeFormater;
	private SimpleDateFormat dTimeFormater;

	public DateTimeUtils(Context context) {
		prefs = new Prefs(context);

		dateFormat = prefs.getValue(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT, DEFAULT_DATE).replace("mm", "MM");
		mDateFormater = new SimpleDateFormat(dateFormat);
		dDateFormater = new SimpleDateFormat(DEFAULT_DATE);

		am_pm = prefs.getValue(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM, "false");

		if (am_pm.equals("true")) {
			dateTimeFormat = dateFormat + " hh:mm aaa";
			timeFormat = "hh:mm aaa";
		} else {
			dateTimeFormat = dateFormat + " HH:mm";
			timeFormat = DEFAULT_TIME;
		}
		mDateTimeFormater = new SimpleDateFormat(dateTimeFormat);
		dDateTimeFormater = new SimpleDateFormat(DEFAULT_DATETIME);

		mTimeFormater = new SimpleDateFormat(timeFormat);
		dTimeFormater = new SimpleDateFormat(DEFAULT_TIME);

	}

	// Date
	public String formatDate(String date) {
		String formatedDate = "";
		try {
			Date dateObj = dDateFormater.parse(date);
			formatedDate = mDateFormater.format(dateObj);
		} catch (ParseException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}

		return formatedDate;
	}

	public String formatDate(Date date) {
		return mDateFormater.format(date);
	}

	public String formatDate(long milis) {
		return mDateFormater.format(milis);
	}

	public String formatDateToDefault(Date date) {
		return dDateFormater.format(date);
	}
	
	public Calendar stringDateToCalendar(String date){
		Calendar c = Calendar.getInstance();
		try {
			Date dateObj = mDateFormater.parse(date);
			c.setTime(dateObj);
		} catch (ParseException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		return c;
	}
	// Date and time
	public String formatDateTime(String date) {
		String formatedDate = "";
		try {
			Date dateObj = dDateTimeFormater.parse(date);
			formatedDate = mDateTimeFormater.format(dateObj);
		} catch (ParseException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}

		return formatedDate;
	}

	public String formatDateTime(Date date) {
		return mDateTimeFormater.format(date);
	}

	public String formatDateTimeToDefault(Date date) {
		return dDateTimeFormater.format(date);
	}

	// Time
	public String formatTime(long milis) {
		return mTimeFormater.format(milis);
	}

	public String formatTimeToDefault(Date date) {
		return dTimeFormater.format(date);
	}

}
