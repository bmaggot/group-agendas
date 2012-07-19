package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.Event;



public class DayInstance  {



		private ArrayList<Event> allDayEvents; 
		private ArrayList<Event> hourEvents; 
		private Calendar selectedDate = Calendar.getInstance(); 
		private Activity activity;
		
		public DayInstance(Context context){
			activity = (Activity) context;
			selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);
			allDayEvents = new ArrayList<Event>();
			hourEvents = new ArrayList<Event>();
			updateEventLists();
		}
		
		private void updateEventLists() {
			// TODO Auto-generated method stub
			
		}

		public Calendar getSelectedDate() {
		return selectedDate;
	}
		public void setSelectedDate(Calendar selectedDate) {
			this.selectedDate = selectedDate;
		}

		public void nextDay() {
			selectedDate.add(Calendar.DATE, 1);
			
		}

		public void prevDay() {
			selectedDate.add(Calendar.DATE, -1);		
		}

		public List<Event> getAllDayEvents() {
			// TODO Auto-generated method stub
			return null;
		}
		
		
}