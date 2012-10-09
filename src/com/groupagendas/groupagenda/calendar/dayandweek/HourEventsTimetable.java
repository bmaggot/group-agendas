package com.groupagendas.groupagenda.calendar.dayandweek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.EventTimeComparator;
import com.groupagendas.groupagenda.utils.Utils;

public class HourEventsTimetable {
	private final static int DEFAULT_TIMETABLE_ACCURACY_MINUTES = 60;
	private static final int milisInMinute = 60000;
	private final int accuracyInMinutes;
	private ArrayList<Event>[] eventsTimetable;
	Calendar todayStart;
	Calendar todayEnd;
	
	public HourEventsTimetable(List<Event> hourEventsList, Calendar selectedDate){
		this(hourEventsList, selectedDate, DEFAULT_TIMETABLE_ACCURACY_MINUTES);
	}
	
	@SuppressWarnings("unchecked")
	public HourEventsTimetable(List<Event> hourEventsList, Calendar selectedDate, int accuracyInMinutes) {
		this.todayStart = selectedDate;
		this.todayEnd = (Calendar)todayStart.clone();
		this.accuracyInMinutes = accuracyInMinutes;
		todayEnd.add(Calendar.HOUR_OF_DAY, 23);
		todayEnd.add(Calendar.MINUTE, 59);
		todayEnd.add(Calendar.SECOND, 59);
		
		int minsPerDay = 24 * 60;
		int rowsCount = minsPerDay / accuracyInMinutes;
		if (accuracyInMinutes * rowsCount < minsPerDay) rowsCount++;
		
		Collections.sort(hourEventsList, new EventTimeComparator());
		eventsTimetable = (new ArrayList[rowsCount]);
		for (Event e : hourEventsList){
			this.add(e);
		}
		

	}
	
//	private void printTimetable() {
//		for (int i = 0; i < eventsTimetable.length; i++){
//			if (eventsTimetable[i] != null) {
//				System.out.println( i + ": ");
//				for (Event e : eventsTimetable[i]) System.out.println(e.title + " ");
//			}
//		}
//		
//	}

	private void add(Event event){
		
		int startIndex = getStartTimetableIndex(event);
		int endIndex = startIndex + getEventDurationUnits(event);
		for (int i = startIndex; i < endIndex; i++) put (i, event);
	}
	
	private int getStartTimetableIndex(Event event) {
		Calendar start = event.getStartCalendar();
		if (start.before(todayStart)) {// if event start is not today, we set
										// event start in time table 00:00
			start = todayStart;
		}
		return (start.get(Calendar.HOUR_OF_DAY) * 60 + start
				.get(Calendar.MINUTE)) / accuracyInMinutes;
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
		int startIndex = getStartTimetableIndex(event);
		int endIndex = startIndex + getEventDurationUnits(event);
		int ret = 1;
		ArrayList<Event> hourList;
		
		
		for (int i = startIndex; i < endIndex; i++){	
			hourList = eventsTimetable[i];
			if (hourList.size() > ret) ret = hourList.size();
		}
			
		
		return ret;
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * Method gets Id for left neighbour of timetable for event
	 * @param event
	 * @return java hashcode of left neighbour of this event in timetable. If this is first event in that table row, method returns 0;
	 */
	public int getLeftNeighbourId(Event event){
		int eventStartIndex = getStartTimetableIndex(event);  // we get number of row, where starts our event
		ArrayList<Event> hourEvents = eventsTimetable[eventStartIndex]; // we get all events of that hour

		int eventIndexInRow = hourEvents.indexOf(event); // we get our event position in table row
//		We use event object JAVA HASH code for Relative Layout id;
		if (eventIndexInRow > 0) return hourEvents.get(eventIndexInRow - 1).hashCode();  // if our event is not the first from left event, we return his left neighbour hashcode
		else return 0; // else return zero id;
	}

	public boolean hasEvents(int hour) {
		return eventsTimetable[hour]!=null;
	}

	/**
	 * This method returns length of this event in quantity of timetable rows, according to timetable accuracy. 
	 * E.g. if event lasts 4 hours and timetable accuracy is 30 minutes, this method returns 8
	 * @author justinas.marcinka@gmail.com
	 * @param event
	 * @return Time units quantity that this event takes on particular day
	 */
	public int getEventDurationUnits(Event event) {
		
		Calendar start = event.getStartCalendar();
		if (start.before(todayStart)) start = todayStart;
		
		Calendar end = event.getEndCalendar();
		
		Utils.formatCalendar(start, DataManagement.SERVER_TIMESTAMP_FORMAT);

		if (end.after(todayEnd)) end = todayEnd;
		long durationInMilis = end.getTimeInMillis() - start.getTimeInMillis();
		long durationInMins = durationInMilis / milisInMinute;
		int durationInTimeUnits = (int) (durationInMins / accuracyInMinutes);
		if (durationInMins % accuracyInMinutes > accuracyInMinutes / 2) durationInTimeUnits++; 
		if (durationInTimeUnits == 0) return 1;
		return durationInTimeUnits;
	}
}
