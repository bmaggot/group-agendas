package com.groupagendas.groupagenda.calendar.week;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;

import com.groupagendas.groupagenda.calendar.day.DayInstance;
import com.groupagendas.groupagenda.utils.Utils;

public class WeekInstance {
	
	private Activity activity;
	private DayInstance[] shownDays;
	private Calendar shownDate;
	private int maxAllDayEventsCount;
	
	private int daysToShow;
	
	
	public WeekInstance (Context context, Calendar selectedDate, int daysToShow){
		this.daysToShow = daysToShow;
		activity = (Activity) context;
		this.shownDate = selectedDate;
		if (daysToShow == shownDate.getActualMaximum(Calendar.DAY_OF_WEEK)){
				Utils.setCalendarToFirstDayOfWeek(this.shownDate);		
		}
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
