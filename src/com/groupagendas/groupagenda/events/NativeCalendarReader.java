package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;

public class NativeCalendarReader {
	private static String ID = "_id";
	private static String TITLE = "title";
	private static String DESCRIPTION = "description";
	private static String DTSTART = "dtstart";
	private static String BEGIN = "begin";
	private static String DTEND = "dtend";
	private static String END = "end";
	private static String ALLDAY = "allDay";
	private static String EVENTTIMEZONE = "eventTimezone";
	private static String EVENTLOCATION = "eventLocation";
	private static String RRULE = "rrule";
	private static String DURATION = "duration";
	private static String EVENT_ID = "event_id";

	static Cursor cursor;

	public static ArrayList<Event> readAllCalendar(Context context) {
		Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
		Calendar calendar = Calendar.getInstance();
		ContentUris.appendId(builder, calendar.getTimeInMillis());
		ContentUris.appendId(builder, calendar.getTimeInMillis() + DateUtils.YEAR_IN_MILLIS);

		Cursor eventCursor = context.getContentResolver().query(builder.build(),
				new String[] { TITLE, DESCRIPTION, BEGIN, END, EVENT_ID, ALLDAY, EVENTLOCATION, DURATION, RRULE, ID }, null, null,
				"startDay ASC, startMinute ASC");

		ArrayList<Event> nativeEvents = new ArrayList<Event>();
		while (eventCursor.moveToNext()) {
			nativeEvents.add(makeNativeEventFromCursor(context, eventCursor));
		}
		eventCursor.close();
		return nativeEvents;
	}

	public static void getAllCalendars(Context context) {
		ContentResolver contentResolver = context.getContentResolver();

		final Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"), (new String[] { "_id",
				"calendar_displayName", "calendar_color" }), null, null, null);

		HashSet<String> calendarIds = new HashSet<String>();

		while (cursor.moveToNext()) {

			final String _id = cursor.getString(0);
			final String displayName = cursor.getString(1);
			final String color = cursor.getString(2);

			System.out.println("Id: " + _id + " Display Name: " + displayName + " Color: " + color);
			calendarIds.add(_id);
		}
	}

	public static ArrayList<Event> readNativeCalendarEventsForTimeInterval(Context context, Long startTime, Long endTime) {
		Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
		ContentUris.appendId(builder, startTime);
		ContentUris.appendId(builder, endTime);

		Cursor eventCursor = context.getContentResolver()
				.query(builder.build(),
						new String[] { TITLE, DESCRIPTION, BEGIN, END, EVENT_ID, ALLDAY, EVENTLOCATION, DURATION, RRULE, ID}, null, null, null);

		ArrayList<Event> nativeEvents = new ArrayList<Event>();
		while (eventCursor.moveToNext()) {
			nativeEvents.add(makeNativeEventFromCursor(context, eventCursor));
		}
		eventCursor.close();
		return nativeEvents;
	}

	public static Event makeNativeEventFromCursor(Context context, Cursor cursor) {
		Event event = new Event();
		try {
			Account account = new Account(context);
			event.setTitle(cursor.getString(0));
//			Log.e("event.getTitle()", event.getTitle());
			event.setDescription(cursor.getString(1));
			// Log.e("Description", event.getDescription());

			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTimeInMillis(cursor.getLong(2));
			startCalendar.clear(Calendar.DST_OFFSET);
			event.setStartCalendar(startCalendar);
//			Log.e("Start", event.getStartCalendar().getTime().toString());
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTimeInMillis(cursor.getLong(3));
			endCalendar.clear(Calendar.DST_OFFSET);
			event.setEndCalendar(endCalendar);
//			Log.e("End", event.getEndCalendar().getTime().toString());

			event.setInternalID(cursor.getLong(4));
			// Log.e("Intenal id", event.getInternalID()+"");
			event.setIs_all_day(cursor.getString(5).equals("1"));
			// Log.e("All day", event.is_all_day()+"");
			event.setTimezone(account.getTimezone());
			// Log.e("TimeZone", event.getTimezone());

			event.setCreator_fullname(context.getResources().getString(R.string.you));
			event.setLocation(cursor.getString(6));
			// Log.e("Location", event.getLocation());
			event.setType("native_event");
			event.setStatus(1);
			if (event.is_all_day()) {
				event.getEndCalendar().clear(Calendar.HOUR);
				event.getEndCalendar().clear(Calendar.HOUR_OF_DAY);
				event.getEndCalendar().clear(Calendar.MINUTE);
				event.getEndCalendar().clear(Calendar.SECOND);
				// event.getEndCalendar().clear(Calendar.ZONE_OFFSET);
//				Log.e("All day End", event.getEndCalendar().getTime().toString());
				event.getStartCalendar().clear(Calendar.HOUR);
				event.getStartCalendar().clear(Calendar.HOUR_OF_DAY);
				event.getStartCalendar().clear(Calendar.MINUTE);
				event.getStartCalendar().clear(Calendar.SECOND);
				// event.getEndCalendar().clear(Calendar.ZONE_OFFSET);
//				Log.e("All day Start", event.getStartCalendar().getTime().toString());
			}
			event.setNative(true);
		} catch (Exception e) {
			System.out.println("NativeCalendarReader makeNativeEventFromCursor");
			e.printStackTrace();
		}
		return event;
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
		firstDayOfAMonth.set(Calendar.DAY_OF_MONTH, day.getMinimum(Calendar.DAY_OF_MONTH) - 7);

		Calendar lastDayOfAMonth = Calendar.getInstance();
		lastDayOfAMonth.setTimeInMillis(day.getTimeInMillis());
		lastDayOfAMonth.set(Calendar.DAY_OF_MONTH, day.getMaximum(Calendar.DAY_OF_MONTH) + 7);

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
					new String[] { ID, TITLE, DESCRIPTION, DTSTART, DTEND, ALLDAY, EVENTTIMEZONE, EVENTLOCATION, DURATION }, selection,
					null, null);
		}

		if (cursor != null && cursor.moveToFirst()) {
			nativeEvent = makeNativeEventFromCursorById(context, cursor);
			cursor.close();
		}
		return nativeEvent;
	}

	public static long getNtiveEventStartTimeInUTCmillis(Context context, long id) {
		long startTime = 0;
		String selection = ID + "=" + id;

		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"), new String[] { DTSTART },
					selection, null, null);
		} else {
			cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"), new String[] { DTSTART }, selection, null,
					null);
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

	public static Event makeNativeEventFromCursorById(Context context, Cursor cur) {
		Event event = new Event();
		try {
			Account account = new Account(context);
			event.setInternalID(Long.valueOf(cursor.getString(0)));
			event.setTitle(cursor.getString(1));
			event.setDescription(cursor.getString(2));

			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTimeInMillis(Long.valueOf(cursor.getString(3)));
			startCalendar.clear(Calendar.DST_OFFSET);
			if (!startCalendar.getTimeZone().equals(TimeZone.getTimeZone("UTC"))) {
				Calendar output = Calendar.getInstance();

				output.set(Calendar.DAY_OF_MONTH, startCalendar.get(Calendar.DAY_OF_MONTH));
				output.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH));
				output.set(Calendar.YEAR, startCalendar.get(Calendar.YEAR));
				output.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
				output.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
				output.set(Calendar.SECOND, startCalendar.get(Calendar.SECOND));
				output.set(Calendar.MILLISECOND, startCalendar.get(Calendar.MILLISECOND));
			}

			startCalendar.setTimeZone(Calendar.getInstance().getTimeZone());
			event.setStartCalendar(startCalendar);
			Calendar endCalendar = Calendar.getInstance();
			if (cursor.getString(4) != null) {
				endCalendar.setTimeInMillis(Long.valueOf(cursor.getString(4)));
			} else {
				endCalendar.setTimeInMillis(Long.valueOf(cursor.getString(3)));
				if (cursor.getString(8) != null) {
					if (cursor.getString(8).matches("P[0-9]+S")) {
						String tmpDuration = cursor.getString(8).substring(1, cursor.getString(8).length() - 1);
						endCalendar.add(Calendar.SECOND, Integer.valueOf(tmpDuration));
					} else if (cursor.getString(8).matches("P[0-9]+D")) {
						String tmpDuration = cursor.getString(8).substring(1, cursor.getString(8).length() - 1);
						endCalendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(tmpDuration));
					} else {
						System.out.println("NativeCalendarReader.makeNativeEventFromCursor(context, cur) = no endTime for "
								+ event.getTitle());
					}
				}
			}
			endCalendar.clear(Calendar.DST_OFFSET);
			endCalendar.setTimeZone(Calendar.getInstance().getTimeZone());
			event.setEndCalendar(endCalendar);
			event.setIs_all_day(cursor.getString(5).equals("1"));
			event.setTimezone(account.getTimezone());
			event.setCreator_fullname(context.getResources().getString(R.string.you));
			event.setLocation(cursor.getString(7));
			event.setType("native_event");
			event.setStatus(1);
			if (event.is_all_day()) {
				event.getEndCalendar().clear(Calendar.HOUR);
				event.getEndCalendar().clear(Calendar.HOUR_OF_DAY);
				event.getEndCalendar().clear(Calendar.MINUTE);
				event.getEndCalendar().clear(Calendar.SECOND);
			}
			event.setNative(true);
		} catch (Exception e) {
			System.out.println("NativeCalendarReader makeNativeEventFromCursor");
			e.printStackTrace();
		}
		return event;
	}

}