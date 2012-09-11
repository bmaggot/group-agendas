package com.groupagendas.groupagenda.utils;

import java.util.Comparator;

import com.groupagendas.groupagenda.events.Event;

public class EventTimeComparator implements Comparator<Event> {

	@Override
	public int compare(Event event1, Event event2) {
		if (event1.getStartCalendar().before(event2.getStartCalendar())) return -1;
		if (event2.getStartCalendar().before(event1.getStartCalendar())) return 1;
		if (event1.getEndCalendar().after(event2.getEndCalendar())) return -1;
		if (event2.getEndCalendar().after(event1.getEndCalendar())) return 1;
		return 0;
	}

	

}
