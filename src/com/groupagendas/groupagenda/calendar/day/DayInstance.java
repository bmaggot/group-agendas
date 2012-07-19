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
		
		public Context getContext(){
			return activity;
		}
		
		
}