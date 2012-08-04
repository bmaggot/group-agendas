package com.groupagendas.groupagenda.calendar.day;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;



public class DayInstance  {






		private List<Event> allDayEvents; 
		private HourEventsTimetable hourEventsTimetable;
		private ArrayList<Event> hourEventsList;
		private Calendar selectedDate; 
		private Activity activity;
		
		public DayInstance(Context context, Calendar selectedDate){
			activity = (Activity) context;
			this.selectedDate = selectedDate;
			updateEventLists();
		}
		

		
		private void updateEventLists() {
			ArrayList<Event> events = Data.getEventByDate(selectedDate);
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
				
				hourEventsTimetable = new HourEventsTimetable(hourEventsList, selectedDate);
			}
		}

		private boolean allDay(Event e) {
//			return e.is_all_day;
			if (e.is_all_day) return true;
			
			if (!e.startCalendar.after(selectedDate)){
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime());
				Calendar tmp = Utils.stringToCalendar(dayStr + " 23:59:59", Utils.date_format);
				if (!e.endCalendar.before(tmp)) return true;
			}
			
			return false;
		}



		public Calendar getSelectedDate() {
		return selectedDate;
	}
		public void setSelectedDate(Calendar selectedDate) {
			this.selectedDate = selectedDate;
		}

		public void goNext() {
			selectedDate.add(Calendar.DATE, 1);
			updateEventLists();
		}

		public void goPrev() {
			selectedDate.add(Calendar.DATE, -1);
			updateEventLists();
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




		public boolean isToday() {
			Calendar tmp = Calendar.getInstance();
			return tmp.get(Calendar.ERA) == selectedDate.get(Calendar.ERA) &&
	                tmp.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
	                tmp.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR);
		
		}

		
		
}