package com.groupagendas.groupagenda.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TreeMap;

import android.content.Context;

import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;

public class TreeMapUtils {
	public static final String SERVER_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final DateFormat createFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd", Locale.US); // ascii date, so Locale = US
	}
	
	public static ArrayList<Event> getEventsFromTreemap(Context context, Calendar date, TreeMap<String, ArrayList<Event>> tm, boolean needToSort) {
		if (date != null && tm != null) {
			String key = createFormatter().format(date.getTime());
			ArrayList<Event> stored = tm.get(key);
			if (stored != null)
				if(needToSort){
					return sortEventsByTimeString(context, stored, date);
				} else {
					return stored;
				}
		}
		// otherwise, prevent NPE
		return new ArrayList<Event>();
	}
	
	public static TreeMap<String, ArrayList<Event>> sortEvents(Context context, ArrayList<Event> events) {
		TreeMap<String, ArrayList<Event>> tm = new TreeMap<String, ArrayList<Event>>();
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		ArrayList<Event> pollEvents = EventManagement.getPollEventsFromLocalDb(context, null);
		for (Event event : events) {
			if (event.getStartCalendar() == null || event.getEndCalendar() == null) {
				putNewEventIntoTreeMap(context, tm, event);
				continue;
			}
			
			event_start = (Calendar) event.getStartCalendar().clone();
			event_end = (Calendar) event.getEndCalendar().clone();
			tmp_event_start = (Calendar) event_start.clone();
			if (event_end.getTime().toString().equals("Fri Jan 01 00:00:00 EET 2100")) {
				continue;
			}
			int difference;
			for (difference = 0; tmp_event_start.before(event_end); ++difference) {
				tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
			}
			DateFormat formatter = createFormatter();
			if (difference == 0) {
				String dayStr = formatter.format(event_start.getTime());
				tm = putValueIntoTreeMap(tm, dayStr, event);
			} else if (difference > 0) {
				Calendar eventDay = null;
				for (int i = 0; i < difference; i++) {
					String dayStr = formatter.format(event_start.getTime());
					putValueIntoTreeMap(tm, dayStr, event);
					event_start.add(Calendar.DAY_OF_MONTH, 1);
				}
				String dayStr = formatter.format(event_end.getTime());
				Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
//					Log.e("Not", "Fucked sortEvents");
					dayStr = formatter.format(event_start.getTime());
					putValueIntoTreeMap(tm, dayStr, event);
				} else {
//					Log.d("Not v2", "sortEvents: eventDay = " + eventDay);
				}
			}
		}
		
		for (Event event : pollEvents) {
			putEventIntoTreeMap(context, tm, event);
			
		}
		return tm;
	}

	public static TreeMap<String, ArrayList<Event>> putValueIntoTreeMap(TreeMap<String, ArrayList<Event>> tm, String eventDay, Event event) {
		if (tm == null) {
			tm = new TreeMap<String, ArrayList<Event>>();
		}
		
		// Log.d("TMU", "Adding event with key " + eventDay);
		ArrayList<Event> tmpArrayList = tm.get(eventDay);
		if (tmpArrayList == null) {
			tmpArrayList = new ArrayList<Event>(1);
			tm.put(eventDay, tmpArrayList);
		}
		tmpArrayList.add(event);
		
		return tm;
	}

	public static void putEventIntoTreeMap(Context context, TreeMap<String, ArrayList<Event>> tm,Event event) {
		Calendar event_start = (Calendar) event.getStartCalendar().clone();
		String dayStr = createFormatter().format(event_start.getTime());
		tm = putValueIntoTreeMap(tm, dayStr, event);
	}

	public static void deleteEventfromTheTreeMap(Context context, TreeMap<Calendar, ArrayList<Event>> tm, Event event) {
		if (event.getStartCalendar() == null || event.getEndCalendar() == null)
			return;
		
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		event_start = (Calendar) event.getStartCalendar().clone();
		event_end = (Calendar) event.getEndCalendar().clone();
		tmp_event_start = (Calendar) event_start.clone();
		int difference;
		for (difference = 0; tmp_event_start.before(event_end); ++difference) {
			tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
		}
		DateFormat formatter = createFormatter();
		if (difference == 0) {
			String dayStr = formatter.format(event_start.getTime());
			Calendar eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
			tm.get(eventDay).remove(event);
		} else if (difference > 0) {
			Calendar eventDay = null;
			for (int i = 0; i < difference; i++) {
				String dayStr = formatter.format(event_start.getTime());
				eventDay = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				tm.get(eventDay).remove(event);
				event_start.add(Calendar.DAY_OF_MONTH, 1);
			}
			String dayStr = formatter.format(event_end.getTime());
			Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
			if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
				dayStr = formatter.format(event_start.getTime());
				event_start = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
				tm.get(event_start).remove(event);
			}
		}
	}

	public static void putNewEventIntoTreeMap(Context context, TreeMap<String, ArrayList<Event>> tm, Event event) {

		String dayStr = "";
		if (event.getEvents_day() != null) {
			dayStr = event.getEvents_day();
		} else {
//			Log.e("FUCKED", event.getEvent_id() + "");
			if(event.getStartCalendar() != null)
			dayStr = createFormatter().format(event.getStartCalendar().getTime());
		}
		tm = putValueIntoTreeMap(tm, dayStr, event);
	}
	
	public static void putNewEventPollsIntoTreeMap(Context context, TreeMap<String, ArrayList<Event>> tm, Event event) {
		if (event.getStartCalendar() == null || event.getEndCalendar() == null)
			return;
		
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		Calendar nowCal = Calendar.getInstance();
		event_start = (Calendar) event.getStartCalendar().clone();
		event_end = (Calendar) event.getEndCalendar().clone();
		tmp_event_start = (Calendar) event_start.clone();
		int difference;
		for (difference = 0; tmp_event_start.before(event_end); ++difference) {
			tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
		}
		DateFormat formatter = createFormatter();
		if (difference == 0) {
			String dayStr = "";
			if (event.getEvents_day() != null) {
				dayStr = event.getEvents_day();
			} else {
				dayStr = formatter.format(event_start.getTime());
			}
			if (event_start.getTimeInMillis() > nowCal.getTimeInMillis()) {
				tm = putValueIntoTreeMap(tm, dayStr, event);
			}
		} 
		else if (difference >= 0) {
			Calendar eventDay = null;
			for (int i = 0; i < difference; i++) {
				String dayStr = formatter.format(event_start.getTime());
				if (event_start.getTimeInMillis() > nowCal.getTimeInMillis()) {
					tm = putValueIntoTreeMap(tm, dayStr, event);
				}
				event_start.add(Calendar.DAY_OF_MONTH, 1);
			}
			String dayStr = formatter.format(event_end.getTime());
			Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
			if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
//				Log.e("Not", "Fucked putNewEventPollsIntoTreeMap");
				dayStr = formatter.format(event_start.getTime());
				if (event_start.getTimeInMillis() > nowCal.getTimeInMillis()) {
					tm = putValueIntoTreeMap(tm, dayStr, event);
				}
			} else {
//				Log.d("Not v2", "putNewEventPollsIntoTreeMap: eventDay = " + eventDay);
			}
		}
	}

	public static void putNativeEventsIntoTreeMap(Context context, TreeMap<String, ArrayList<Event>> tm, Event event) {
		if (event.getStartCalendar() == null || event.getEndCalendar() == null)
			return;
		
		Calendar event_start = null;
		Calendar event_end = null;
		Calendar tmp_event_start = null;
		event_start = (Calendar) event.getStartCalendar().clone();
		event_end = (Calendar) event.getEndCalendar().clone();
		tmp_event_start = (Calendar) event_start.clone();
		
		int difference;
		for (difference = 0; tmp_event_start.before(event_end); ++difference) {
			tmp_event_start.add(Calendar.DAY_OF_MONTH, 1);
		}
		DateFormat formatter = createFormatter();
		if (difference == 0) {
			String dayStr = "";
			if (event.getEvents_day() != null) {
				dayStr = event.getEvents_day();
			} else {
				dayStr = formatter.format(event_start.getTime());
			}
			tm = putValueIntoTreeMap(tm, dayStr, event);
		} else if (difference > 0) {
			Calendar eventDay = null;
			for (int i = 0; i < difference; i++) {
				String dayStr = formatter.format(event_start.getTime());
				tm = putValueIntoTreeMap(tm, dayStr, event);
				event_start.add(Calendar.DAY_OF_MONTH, 1);
			}
			String dayStr = formatter.format(event_end.getTime());
			Calendar eventTmpEnd = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
			if (eventTmpEnd.after(eventDay) && event_end.after(eventTmpEnd)) {
//				Log.e("Not", "Fucked putNativeEventsIntoTreeMap");
				dayStr = formatter.format(event_start.getTime());
				tm = putValueIntoTreeMap(tm, dayStr, event);
			} else {
//				Log.d("Not v2", "putNativeEventsIntoTreeMap: eventDay = " + eventDay);
			}
		}
	}
	
	public static String formatTime(String time){
		if(time.equals(EventsProvider.EMetaData.EventsIndexesMetaData.NOT_TODAY)){
			return "0001";
		}
		if(time != null && time.matches("[0-9]*:[0-9]* [A,P]M")){
			String[] times = time.split(":");
			if (times[1].matches("[0-9]* PM")) {
				if (Integer.valueOf(times[0]) != 12)
					times[0] = String.valueOf(Integer.valueOf(times[0]) + 12);
				times[1] = times[1].substring(0, times[1].lastIndexOf('P')-1);
			} else if (times[1].matches("[0-9]* AM")) {
				if (Integer.valueOf(times[0]) == 12)
					times[0] = "00";
				times[1] = times[1].substring(0, times[1].lastIndexOf('A')-1);
			}
			if(times[0].matches("0[0-9]")){
				times[0] = times[0].substring(1);
			}
			if(Integer.valueOf(times[1]) == 00){
				times[1] = "01";
			}
			return times[0]+""+times[1];
		}
		time = time.replaceAll(":", "");
		time = time.replaceAll(" ", "");
		return time;
	}
	
	public static ArrayList<Event> sortEventsByTimeString(Context context, ArrayList<Event> eventsToSort, Calendar key){
		boolean continueSorting = true;
		int listToSortLength = eventsToSort.size();
		while(continueSorting){
			continueSorting = false;
			for(int i = 0; i < (listToSortLength-1); i++){
				if(eventsToSort.get(i).getStartCalendar() != null){
					if(eventsToSort.get(i).getStartCalendar().before(key)){
						eventsToSort.get(i).setEvent_day_start("0001");
					} else if(eventsToSort.get(i).getStartCalendar().get(Calendar.MINUTE) == 0){
						eventsToSort.get(i).setEvent_day_start(eventsToSort.get(i).getStartCalendar().get(Calendar.HOUR_OF_DAY)+"00");
					} else {
						eventsToSort.get(i).setEvent_day_start(eventsToSort.get(i).getStartCalendar().get(Calendar.HOUR_OF_DAY)+""+eventsToSort.get(i).getStartCalendar().get(Calendar.MINUTE));
					}
				}
				if(eventsToSort.get(i+1).getStartCalendar() != null){
					if(eventsToSort.get(i+1).getStartCalendar().before(key)){
						eventsToSort.get(i+1).setEvent_day_start("0001");
					} else if(eventsToSort.get(i+1).getStartCalendar().get(Calendar.MINUTE) == 0){
						eventsToSort.get(i+1).setEvent_day_start(eventsToSort.get(i+1).getStartCalendar().get(Calendar.HOUR_OF_DAY)+"00");
					} else {
						eventsToSort.get(i+1).setEvent_day_start(eventsToSort.get(i+1).getStartCalendar().get(Calendar.HOUR_OF_DAY)+""+eventsToSort.get(i+1).getStartCalendar().get(Calendar.MINUTE));
					}
				}
				if(Integer.valueOf(formatTime(eventsToSort.get(i).getEvent_day_start(context))) 
						> Integer.valueOf(formatTime(eventsToSort.get(i+1).getEvent_day_start(context)))){
					Event tmp = eventsToSort.get(i);
					eventsToSort.set(i, eventsToSort.get(i+1));
					eventsToSort.set(i+1, tmp);
					continueSorting = true;
				}
			}
		}
		return eventsToSort;
	}
}
