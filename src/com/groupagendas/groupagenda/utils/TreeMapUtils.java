package com.groupagendas.groupagenda.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import android.content.Context;

import com.groupagendas.groupagenda.events.Event;

public class TreeMapUtils {
	public static final String SERVER_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	
	public static ArrayList<Event> getEventsFromTreemap(Calendar date, TreeMap<Calendar, ArrayList<Event>> tm){
		if (date != null && tm != null)
			if (tm.containsKey(date)) {
				return tm.get(date);
			} 
			return new ArrayList<Event>();
		
	}
	
	public static TreeMap<Calendar, ArrayList<Event>> sortEvents(Context context, ArrayList<Event> events) {
		TreeMap<Calendar, ArrayList<Event>> tm = new TreeMap<Calendar, ArrayList<Event>>();
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		for (Event event : events) {
			if (event.getStartCalendar() != null && event.getEndCalendar() != null) {
				event_start = (Calendar) event.getStartCalendar().clone();
				event_end = (Calendar) event.getEndCalendar().clone();
				tmp_event_start = (Calendar) event_start.clone();
				int difference = 0;
				while (tmp_event_start.before(event_end)) {
					tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
					difference++;
				}
				if (difference == 0) {
					String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
					Calendar eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					tm = putValueIntoTreeMap(tm, eventDay, event);
				} else if (difference >= 0) {
					Calendar eventDay = null;
					for (int i = 0; i < difference; i++) {
						String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
						eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
						putValueIntoTreeMap(tm, eventDay, event);
						event_start.add(Calendar.DAY_OF_MONTH, 1);
					}
					String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_end.getTime());
					Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
						dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
						event_start = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
						putValueIntoTreeMap(tm, event_start, event);
					}
				}
			}
		}
		return tm;
	}

	public static TreeMap<Calendar, ArrayList<Event>> putValueIntoTreeMap(TreeMap<Calendar, ArrayList<Event>> tm, Calendar eventDay, Event event) {
		if(tm == null){
			tm = new TreeMap<Calendar, ArrayList<Event>>();
		}
		if (tm.containsKey(eventDay)) {
			ArrayList<Event> tmpArrayList = tm.get(eventDay);
			tmpArrayList.add(event);
			tm.put(eventDay, tmpArrayList);
		} else {
			ArrayList<Event> tmpArrayList = new ArrayList<Event>();
			tmpArrayList.add(event);
			tm.put(eventDay, tmpArrayList);
		}
		return tm;
	}

	public static void putEventIntoTreeMap(Context context, TreeMap<Calendar, ArrayList<Event>> tm,Event event) {
		String date_format = SERVER_TIMESTAMP_FORMAT;
		Calendar event_start = (Calendar) event.getStartCalendar().clone();
		String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
		Calendar event_day = Utils.stringToCalendar(context, dayStr + " 00:00:00", date_format);
		tm = putValueIntoTreeMap(tm, event_day, event);
	}

	public static void deleteEventfromTheTreeMap(Context context, TreeMap<Calendar, ArrayList<Event>> tm, Event event) {
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		if (event.getStartCalendar() != null && event.getEndCalendar() != null) {
			event_start = (Calendar) event.getStartCalendar().clone();
			event_end = (Calendar) event.getEndCalendar().clone();
			tmp_event_start = (Calendar) event_start.clone();
			int difference = 0;
			while (tmp_event_start.before(event_end)) {
				tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
				difference++;
			}
			if (difference == 0) {
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
				Calendar eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				tm.get(eventDay).remove(event);
			} else if (difference >= 0) {
				Calendar eventDay = null;
				for (int i = 0; i < difference; i++) {
					String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
					eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					tm.get(eventDay).remove(event);
					event_start.add(Calendar.DAY_OF_MONTH, 1);
				}
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_end.getTime());
				Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
					dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
					event_start = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					tm.get(event_start).remove(event);
				}
			}
		}
	}

	public static void putNewEventIntoTreeMap(Context context, TreeMap<Calendar, ArrayList<Event>> tm, Event event) {
		
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		if (event.getStartCalendar() != null && event.getEndCalendar() != null) {
			event_start = (Calendar) event.getStartCalendar().clone();
			event_end = (Calendar) event.getEndCalendar().clone();
			tmp_event_start = (Calendar) event_start.clone();
			int difference = 0;
			while (tmp_event_start.before(event_end)) {
				tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
				difference++;
			}
			if (difference == 0) {
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
				Calendar eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				tm = putValueIntoTreeMap(tm, eventDay, event);
			} else if (difference >= 0) {
				Calendar eventDay = null;
				for (int i = 0; i < difference; i++) {
					String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
					eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					tm = putValueIntoTreeMap(tm, eventDay, event);
					event_start.add(Calendar.DAY_OF_MONTH, 1);
				}
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_end.getTime());
				Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
					dayStr = new SimpleDateFormat("yyyy-MM-dd").format(event_start.getTime());
					event_start = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
					tm = putValueIntoTreeMap(tm, event_start, event);
				}
			}
		}
	}
}
