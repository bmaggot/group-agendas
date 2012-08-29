package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;

import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.events.Event;

public class AgendaUtils {
	public static int newInvites = 0;
	public static ArrayList<Event> getActualEvents(Context context, ArrayList<Event> events) {
		ArrayList<Event> actual_events = new ArrayList<Event>();
		String date_format = CalendarSettings.getDateFormat();
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		Calendar calendar_end = null;

		Event event = null;
		newInvites = 0;
		
		for (int i = 0, l = events.size(); i < l; i++) {
			event = events.get(i);
			if (!event.my_time_end.equals("null")) {
				calendar_end = Utils.stringToCalendar(event.my_time_end, event.timezone, date_format);

				if (calendar_end.after(now)) {
					if(event.status == 4){
						newInvites++;
					}
					
					actual_events.add(event);
				}
			}
		}
		return actual_events;
	}
}
