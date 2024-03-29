package com.groupagendas.groupagenda.calendar.dayandweek;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;



public class DayInstance  {

		private List<Event> allDayEvents; 
		private HourEventsTimetable hourEventsTimetable;
		private ArrayList<Event> hourEventsList;
		private Calendar selectedDate; 
		private Activity activity;
		
		public DayInstance(Context context, Calendar selectedDate, TreeMap<String, ArrayList<Event>> tm){
			activity = (Activity) context;
			this.selectedDate = selectedDate;
			updateEventLists(tm);
		}
		/**
		 * This method returns lenght of this event in quantity of timetable rows, according to timetable accuracy. 
		 * E.g. if event lasts 4 hours and timetable accuracy is 30 minutes, this method returns 8
		 * @author justinas.marcinka@gmail.com
		 * @param event
		 * @return Time units quantity that this event takes on particular day
		 */
		public int getEventDuration(Event event){
			return hourEventsTimetable.getEventDurationUnits(event);
		}
		

		
		private void updateEventLists(TreeMap<String, ArrayList<Event>> tm) {
			selectedDate.clear(Calendar.ZONE_OFFSET);
			selectedDate.setTimeZone(Calendar.getInstance().getTimeZone());
			ArrayList<Event> events = TreeMapUtils.getEventsFromTreemap(getContext(), selectedDate, tm, false);
			allDayEvents = new ArrayList<Event>();
			hourEventsList = new ArrayList<Event>();
			hourEventsTimetable = null;
			
			if (events != null){
				for (Event e : events){
					if (allDay(e)){
						allDayEvents.add(e); //if event is all day then add to all day list
					}
					else {//else add event to hour events lists for every hour
						hourEventsList.add(e);							
						}
					 
					}
				
				hourEventsTimetable = new HourEventsTimetable(hourEventsList, selectedDate, WeekInstance.TIMETABLE_ACCURACY);
			}
		}

		@SuppressLint("SimpleDateFormat")
		private boolean allDay(Event e) {
//			return e.is_all_day;
			if (e.is_all_day()) return true;
			
			if (e != null && e.getStartCalendar() != null && !e.getStartCalendar().after(selectedDate)){
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime());
				Calendar tmp = Utils.stringToCalendar(getContext(), dayStr + " 23:59:59", DataManagement.SERVER_TIMESTAMP_FORMAT);
				if (!e.getEndCalendar().before(tmp)) return true;
			}
			
			return false;
		}



		public Calendar getSelectedDate() {
		return selectedDate;
	}
		public void setSelectedDate(Calendar selectedDate) {
			this.selectedDate = selectedDate;
		}

		public void goNext(TreeMap<String, ArrayList<Event>> tm) {
			selectedDate.add(Calendar.DATE, 1);
			updateEventLists(tm);
		}

		public void goPrev(TreeMap<String, ArrayList<Event>> tm) {
			selectedDate.add(Calendar.DATE, -1);
			updateEventLists(tm);
		}

		public List<Event> getAllDayEvents() {
			return allDayEvents;
		}
		
		
		public Context getContext(){
			return activity;
		}
		

		public HourEventsTimetable getHourEventsTimeTable(){
			return hourEventsTimetable;
		}
		
		public boolean hasHourEvents(){
			return !hourEventsList.isEmpty();
		}



		public ArrayList<Event> getHourEvents() {
			return hourEventsList;			
		}



		
		
}