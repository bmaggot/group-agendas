package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.EventStartComparator;
import com.groupagendas.groupagenda.utils.Utils;

public class HourEventsTimetable {
	private ArrayList<Event>[] eventsTimetable;
	Calendar todayStart;
	Calendar todayEnd;
	
	public HourEventsTimetable(List<Event> hourEventsList, Calendar selectedDate) {
		this.todayStart = selectedDate;
		this.todayEnd = (Calendar)todayStart.clone();
		todayEnd.add(Calendar.HOUR_OF_DAY, 23);
		todayEnd.add(Calendar.MINUTE, 59);
		todayEnd.add(Calendar.SECOND, 59);
		
		Collections.sort(hourEventsList, new EventStartComparator());
		eventsTimetable = (ArrayList<Event>[]) new ArrayList[24];
		for (Event e : hourEventsList){
			this.add(e);
		}
	}
	
	private void add(Event event){
		
		Calendar end = event.endCalendar;
		if (end.after(todayEnd)){
			end = todayEnd;
		}
		
		Calendar start = Utils.stringToCalendar(event.time_start, Utils.date_format);
		if (start.before(todayStart)){//TODO TEST cia jeigu eventas prasideda ne sita diena, o anksciau
			start = (Calendar) todayStart.clone();
			end = todayEnd;
			end.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
		}
		
		while (start.before(end)){
			put(start.get(Calendar.HOUR_OF_DAY), event);
			start.add(Calendar.HOUR_OF_DAY, 1);
		}
		

	}
	
	private void put (int hour, Event event){
		if (eventsTimetable[hour] == null) eventsTimetable[hour] =  new ArrayList<Event>();
		eventsTimetable[hour].add(event);
		
	}
	
	
/**
 * 
 * @param event
 * @return which part of layout is assigned for this event interface. 
 * E.g. if return is 4, then this event details can take 1/4 of layout
 */

	public int getWidthDivider(Event event) {
		Calendar start = Utils.stringToCalendar(event.time_start, Utils.date_format);
		Calendar end = Utils.stringToCalendar(event.time_end, Utils.date_format);
		int ret = 1;
		
		while (start.before(end)){
			ArrayList<Event> hourList;
			if ((hourList = eventsTimetable[start.get(Calendar.HOUR_OF_DAY)]) != null)
				if (hourList.size() > ret) ret = hourList.size();
			start.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		return ret;
	}
	
	public int getNeighbourId(Event event){
		int startHour = Utils.stringToCalendar(event.time_start, Utils.date_format).get(Calendar.HOUR_OF_DAY);
		ArrayList<Event> hourEvents = eventsTimetable[startHour];

		int index = hourEvents.indexOf(event);
		if (index != 0) return hourEvents.get(index -1).event_id;  
		else return 0;
	}
}
