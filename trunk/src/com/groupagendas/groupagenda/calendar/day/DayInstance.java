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
		private List<Event> hourEvents; 
		private Calendar selectedDate = Calendar.getInstance(); 
		private Activity activity;
		
		public DayInstance(Context context){
			activity = (Activity) context;
			String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime());
			selectedDate = Utils.stringToCalendar(dayStr + " 00:00:00", Utils.date_format);
			selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);
			
			updateEventLists();
		}
		

		
		private void updateEventLists() {
			System.out.println("got " + new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime()) + " events");

			ArrayList<Event> events = Data.getEventByDate(selectedDate);
			allDayEvents = new ArrayList<Event>();
			hourEvents = new ArrayList<Event>();
			
			
			if (events != null)
				for (Event e : events){
					if (e.is_all_day) allDayEvents.add(e);
					else hourEvents.add(e);
				}
			
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
		
		public List<Event> getHourEvents(){
			return hourEvents;
		}
		
		private List<Event> filterAllDayEvents(ArrayList<Event> events) {
			ArrayList<Event> allDayEvents = new ArrayList<Event>();

			
			String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime());
			Calendar day_start = Utils.stringToCalendar(dayStr + " 00:00:00", Utils.date_format);
			Calendar day_end = Utils.stringToCalendar(dayStr + " 23:59:59", Utils.date_format);
			Calendar event_start = null;
			Calendar event_end = null;

			for (Event event : events) {

				if (!event.my_time_end.equals("null") && !event.my_time_start.equals("null")) {
					event_start = Utils.stringToCalendar(event.my_time_start, event.timezone, Utils.date_format);
					event_end = Utils.stringToCalendar(event.my_time_end, event.timezone, Utils.date_format);

					if (!event_end.before(day_start) && !event_start.after(day_end)) {
						if ((event_end.getTime().after(day_end.getTime()) || event_end.getTime().equals(day_end.getTime()))
								&& (event_start.getTime().before(day_start.getTime()) || event_start.getTime().equals(day_start.getTime()))
								|| event.is_all_day) {
							allDayEvents.add(event);
//							System.out.println("added all day event        " + event.title);
						}
					}
				}
			}
			return allDayEvents;
		}
		private List<Event> filterHourEvents(ArrayList<Event> events) {
			ArrayList<Event> dayEvents = new ArrayList<Event>();
			String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime());
			Calendar day_start = Utils.stringToCalendar(dayStr + " 00:00:00", Utils.date_format);
			Calendar day_end = Utils.stringToCalendar(dayStr + " 23:59:59", Utils.date_format);
			Calendar event_start = null;
			Calendar event_end = null;
			for (Event event : events) {
				if (!event.my_time_end.equals("null") && !event.my_time_start.equals("null")) {
					event_start = Utils.stringToCalendar(event.my_time_start, event.timezone, Utils.date_format);
					event_end = Utils.stringToCalendar(event.my_time_end, event.timezone, Utils.date_format);
					if(day_start.getTime().before(event_start.getTime())){
						
					}
					if ((day_start.getTime().before(event_start.getTime()) || day_start.getTime().equals(event_start.getTime()))
							&& (day_end.getTime().after(event_end.getTime()) || day_end.getTime().equals(event_end.getTime())) 
							&& !event.is_all_day) {
						dayEvents.add(event);
//						System.out.println("added hour event        " + event.title);
					}
				}
			}
			return dayEvents;
		}
		
		public Context getContext(){
			return activity;
		}
		
		
}