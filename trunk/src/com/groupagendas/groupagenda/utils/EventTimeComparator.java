package com.groupagendas.groupagenda.utils;

import java.util.Comparator;

import com.groupagendas.groupagenda.events.Event;

public class EventTimeComparator implements Comparator<Event> {

	@Override
	public int compare(Event event1, Event event2) {
		if (event1.startCalendar.before(event2.startCalendar)) return -1;
		if (event2.startCalendar.before(event1.startCalendar)) return 1;
		if (event1.endCalendar.after(event2.endCalendar)) return -1;
		if (event2.endCalendar.after(event1.endCalendar)) return 1;
		return 0;
	}

	

}
