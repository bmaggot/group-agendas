package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;

public class NativeCalendarReader {
	static Cursor cursor;

	public static ArrayList<Event> readAllCalendar(Context context) {
		ArrayList<Event> nativeEvents = new ArrayList<Event>();
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"),
					new String[] { "_id", "title", "description", "dtstart", "dtend", "allDay", "eventTimezone", "eventLocation" }, null,
					null, null);
		} else {
			cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"),
					new String[] { "_id", "title", "description", "dtstart", "dtend", "allDay", "eventTimezone", "eventLocation" }, null,
					null, null);
		}

		if (cursor != null && cursor.moveToFirst()) {
			do {
				nativeEvents.add(makeNativeEventFromCursor(context, cursor));
			} while (cursor.moveToNext());
			cursor.close();
		}
		return nativeEvents;
	}

	public static ArrayList<Event> readNativeCalendarEventsForTimeInterval(Context context, long startTime, long endTime) {
		ArrayList<Event> nativeEvents = new ArrayList<Event>();
		String selection = "";

		if (startTime > 0) {
			selection = "(dtstart >=" + startTime + " AND dtstart <=" + endTime + ")";
		}
		if (endTime > 0 && !selection.equals("")) {
			selection += " OR (dtend >=" + startTime + " AND dtend <=" + endTime + ")";
		} else if (endTime > 0 && selection.equals("")) {
			selection = "dtend <=" + startTime;
		}

		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"),
					new String[] { "_id", "title", "description", "dtstart", "dtend", "allDay", "eventTimezone", "eventLocation" },
					selection, null, null);
		} else {
			cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"),
					new String[] { "_id", "title", "description", "dtstart", "dtend", "allDay", "eventTimezone", "eventLocation" },
					selection, null, null);
		}

		if (cursor != null) {

			if (cursor.moveToFirst()) {
				do {
					nativeEvents.add(makeNativeEventFromCursor(context, cursor));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return nativeEvents;
	}

	public static ArrayList<Event> readNativeCalendarEventsForADay(Context context, Calendar day) {
		long startTime = day.getTimeInMillis();

		Calendar nextDay = Calendar.getInstance();
		nextDay.setTimeInMillis(day.getTimeInMillis());
		nextDay.add(Calendar.DAY_OF_MONTH, 1);
		long endTime = nextDay.getTimeInMillis();

		return readNativeCalendarEventsForTimeInterval(context, startTime, endTime);
	}

	public static ArrayList<Event> readNativeCalendarEventsForAMonth(Context context, Calendar day) {
		Calendar firstDayOfAMonth = Calendar.getInstance();
		firstDayOfAMonth.setTimeInMillis(day.getTimeInMillis());
		firstDayOfAMonth.set(Calendar.DAY_OF_MONTH, day.getMinimum(Calendar.DAY_OF_MONTH));

		Calendar lastDayOfAMonth = Calendar.getInstance();
		lastDayOfAMonth.setTimeInMillis(day.getTimeInMillis());
		lastDayOfAMonth.set(Calendar.DAY_OF_MONTH, day.getMaximum(Calendar.DAY_OF_MONTH));

		return readNativeCalendarEventsForTimeInterval(context, firstDayOfAMonth.getTimeInMillis(), lastDayOfAMonth.getTimeInMillis());
	}

	public static ArrayList<Event> readNativeCalendarEventsForAYear(Context context, Calendar day) {
		Calendar firstDayOfAYear = Calendar.getInstance();
		firstDayOfAYear.setTimeInMillis(day.getTimeInMillis());
		firstDayOfAYear.set(Calendar.MONTH, day.getMinimum(Calendar.MONTH));
		firstDayOfAYear.set(Calendar.DAY_OF_MONTH, day.getMinimum(Calendar.DAY_OF_MONTH));

		Calendar lastDayOfAYear = Calendar.getInstance();
		lastDayOfAYear.setTimeInMillis(day.getTimeInMillis());
		lastDayOfAYear.set(Calendar.MONTH, day.getMaximum(Calendar.MONTH));
		lastDayOfAYear.set(Calendar.DAY_OF_MONTH, day.getMaximum(Calendar.DAY_OF_MONTH));

		return readNativeCalendarEventsForTimeInterval(context, firstDayOfAYear.getTimeInMillis(), lastDayOfAYear.getTimeInMillis());
	}

	public static Event getNativeEventFromLocalDbById(Context context, long id) {
		Event nativeEvent = new Event();
		String selection = "_id=" + id;

		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"),
					new String[] { "_id", "title", "description", "dtstart", "dtend", "allDay", "eventTimezone", "eventLocation" },
					selection, null, null);
		} else {
			cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"),
					new String[] { "_id", "title", "description", "dtstart", "dtend", "allDay", "eventTimezone", "eventLocation" },
					selection, null, null);
		}

		if (cursor != null && cursor.moveToFirst()) {
			nativeEvent = makeNativeEventFromCursor(context, cursor);
			cursor.close();
		}
		return nativeEvent;
	}

	public static long getNtiveEventStartTimeInUTCmillis(Context context, long id) {
		long startTime = 0;
		String selection = "_id=" + id;

		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"), new String[] { "dtstart" },
					selection, null, null);
		} else {
			cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"), new String[] { "dtstart" }, selection,
					null, null);
		}
		if (cursor != null && cursor.moveToFirst()) {
			startTime = Long.valueOf(cursor.getString(0));
		}
		cursor.close();
		return startTime;
	}

	public static long getNtiveEventEndTimeInUTCmillis(Context context, long id) {
		long endTime = 0;
		String selection = "_id=" + id;

		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"), new String[] { "dtend" },
					selection, null, null);
		} else {
			cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"), new String[] { "dtend" }, selection, null,
					null);
		}
		if (cursor != null && cursor.moveToFirst()) {
			endTime = Long.valueOf(cursor.getString(0));
		}
		cursor.close();
		return endTime;
	}

	public static ArrayList<Event> readNativeCalendarEventsForAFewDays(Context context, Calendar startDate, int days) {
		Calendar endDate = Calendar.getInstance();
		;
		if (days > 0) {
			endDate.setTimeInMillis(startDate.getTimeInMillis());
			int counter = 0;
			do {
				endDate.add(Calendar.DAY_OF_MONTH, 1);
				counter++;
			} while (counter < days);
		}
		return readNativeCalendarEventsForTimeInterval(context, startDate.getTimeInMillis(), endDate.getTimeInMillis());
	}
	
	public static Event makeNativeEventFromCursor(Context context, Cursor cur){
		Event event = new Event();
		Account account = new Account(context);
		Calendar startCalendar = Calendar.getInstance();
		event.setInternalID(Long.valueOf(cursor.getString(0)));
		event.setTitle(cursor.getString(1));
		event.setDescription(cursor.getString(2));
		startCalendar.setTimeInMillis(Long.valueOf(cursor.getString(3)));
		event.setStartCalendar(startCalendar);
		Calendar endCalendar = Calendar.getInstance();
		if(cursor.getString(4) != null){
			endCalendar.setTimeInMillis(Long.valueOf(cursor.getString(4)));
		} else {
			endCalendar.setTimeInMillis(Long.valueOf(cursor.getString(3)));
		}
		event.setEndCalendar(endCalendar);
		event.setIs_all_day(cursor.getString(5).equals("1"));
		event.setTimezone(account.getTimezone());
		event.setCreator_fullname(context.getResources().getString(R.string.you));
		event.setLocation(cursor.getString(7));
		event.setType("native");
		event.setStatus(1);
		event.setNative(true);
		return event;
	}
}