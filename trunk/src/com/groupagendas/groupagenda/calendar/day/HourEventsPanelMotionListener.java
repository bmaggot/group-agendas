package com.groupagendas.groupagenda.calendar.day;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.SwipeOnGestureListener;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.Utils;

public class HourEventsPanelMotionListener extends SwipeOnGestureListener {
	
	

	private Calendar selectedDate;


	public HourEventsPanelMotionListener(AbstractCalendarView parent, Calendar selectedDate) {		
		super(parent);
		this.selectedDate = selectedDate;
	}
	
	  public boolean onSingleTapUp(MotionEvent e){
		  
		  int y = Math.round(e.getY());
		  
		int hour = y/Math.round(DayView.hourLineHeightDP * parentView.getResources().getDisplayMetrics().density);
		  
		  Activity navbar = (Activity)(parentView.getContext());
		  Intent intent = new Intent(navbar, NewEventActivity.class);
		  
		  Calendar tmp = (Calendar)selectedDate.clone();
		  tmp.set(Calendar.HOUR_OF_DAY, hour);
		  
		  SimpleDateFormat df = new SimpleDateFormat(Utils.date_format);
		  
		  intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR, df.format(tmp.getTime()));
		  
		  navbar.startActivity(intent);
		return true;
	  }
	
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return super.onFling(e1, e2, velocityX, velocityY);
		
	}

}
