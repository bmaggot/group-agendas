package com.groupagendas.groupagenda.calendar.week;

import java.util.Calendar;

import com.groupagendas.groupagenda.calendar.day.DayInstance;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.Utils;

import android.app.Activity;
import android.content.Context;

public class WeekInstance {
	
	private Activity activity;
	private DayInstance[] shownDays;
	private Calendar selectedDate;
	private Calendar shownDate;
	private int maxAllDayEventsCount;
	
//	TODO change dynamically on pinch also, adjust shownDate accordingly;
	private int daysToShow = 7;
	
	
	public WeekInstance (Context context, Calendar selectedDate, int daysToShow){
		this.daysToShow = daysToShow;
		activity = (Activity) context;
		this.selectedDate = selectedDate;
		this.shownDate = (Calendar)selectedDate.clone();
		Utils.setCalendarToFirstDayOfWeek(this.shownDate);		
		shownDays = new DayInstance[daysToShow];
		maxAllDayEventsCount = 1;
		updateEventLists();		
	}


	private void updateEventLists() {
		maxAllDayEventsCount = 1;
		for (int i = 0; i < daysToShow; i++ ){
			Calendar tmp = (Calendar) shownDate.clone();
			tmp.add(Calendar.DATE, i);
			shownDays[i] = new DayInstance(activity, tmp);
//			Check if this day has max number of all day events
			if (shownDays[i].getAllDayEvents().size() > maxAllDayEventsCount) maxAllDayEventsCount = shownDays[i].getAllDayEvents().size();
		}	
		
		
		
	}


	public Calendar getSelectedDate() {
		return selectedDate;
	}


	public void nextPage() {
		shownDate.add(Calendar.DATE, daysToShow ); 
		updateEventLists();	
		
	}


	public void prevPage() {
		shownDate.add(Calendar.DATE, daysToShow * -1);
		updateEventLists();	
		
	}


	public Calendar getShownDate() {
		return shownDate;
	}


	public int getMaxAllDayEventsCount() {
		return maxAllDayEventsCount;
	}


	public int getDaysToShow() {
		return daysToShow;
	}


	public void setDaysToShow(int daysToShow) {
		this.daysToShow = daysToShow;
	}


	public DayInstance getDayInstance(int i) {
		return shownDays[i];
	}
	
	
	
	
	
}
