package com.groupagendas.groupagenda.utils;

import java.util.Calendar;
import java.util.Comparator;

import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;

public class EventStartComparator implements Comparator<Event> {

	@Override
	public int compare(Event event1, Event event2) {
		Calendar start1 = Utils.stringToCalendar(event1.my_time_start, DataManagement.SERVER_TIMESTAMP_FORMAT);
		Calendar start2 = Utils.stringToCalendar(event2.my_time_start, DataManagement.SERVER_TIMESTAMP_FORMAT);
		
		
		return (start1.before(start2) ? -1 : (start2.before(start1) ? 1 : 0));
	}

	

}
