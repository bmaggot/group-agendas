package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;

import com.groupagendas.groupagenda.events.Event;

public class AgendaUtils {
	public static int newInvites = 0;
	public static ArrayList<Event> getActualEvents(Context context, ArrayList<Event> events) {
		ArrayList<Event> actual_events = new ArrayList<Event>();
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		Calendar calendar_end = null;

		Event event = null;
		newInvites = 0;
		
		for (int i = 0, l = events.size(); i < l; i++) {
			event = events.get(i);
			if (event.getEndCalendar() != null) {
				calendar_end = event.getEndCalendar();

				if (calendar_end.after(now)) {
					if(event.getStatus() == 4){
						newInvites++;
					}
					
					actual_events.add(event);
				}
			}
		}
		return actual_events;
	}
}
