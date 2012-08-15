package com.groupagendas.groupagenda.calendar.week;

import java.util.Calendar;

import com.groupagendas.groupagenda.calendar.day.DayInstance;

import android.app.Activity;
import android.content.Context;

public class WeekInstance {
	
	private Activity activity;
	private DayInstance[] shownDays;
	private Calendar selectedDate;
	private Calendar shownDate;
	
//	TODO change dynamicaly on pinch also, adjust selectedDate accordingly;
	private int daysToShow = 7;
	
	
	public WeekInstance (Context context, Calendar selectedDate){
		activity = (Activity) context;
		this.selectedDate = selectedDate;
		this.shownDate = (Calendar)selectedDate.clone();
//		TODO uzstatyti showndate kairiausio langelio data
		
		shownDays = new DayInstance[7];
		updateEventLists();		
	}


	private void updateEventLists() {
		System.out.println("SAVAITES DIENA: " + shownDate.get(Calendar.DAY_OF_WEEK));
		
	}


	public Calendar getSelectedDate() {
		return selectedDate;
	}


	public void nextPage() {
		shownDate.add(Calendar.DATE, daysToShow ); 
		
	}


	public void prevPage() {
		shownDate.add(Calendar.DATE, daysToShow * -1); 
		
	}


	public Calendar getShownDate() {
		return shownDate;
	}


	public int getMaxAllDayEventCount() {
		// TODO Auto-generated method stub
		return 1;
	}
	
}
