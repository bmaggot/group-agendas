package com.groupagendas.groupagenda.calendar.day;

import android.view.MotionEvent;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.SwipeOnGestureListener;

public class HourEventsPanelMotionListener extends SwipeOnGestureListener {

	public HourEventsPanelMotionListener(AbstractCalendarView parent) {
		
		super(parent);
		System.out.println("sukuria");
	}
	
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		System.out.println("FLING CHILD");
		return super.onFling(e1, e2, velocityX, velocityY);
		
	}

}
