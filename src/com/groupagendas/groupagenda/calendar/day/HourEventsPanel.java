package com.groupagendas.groupagenda.calendar.day;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.SwipeOnGestureListener;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class HourEventsPanel extends RelativeLayout {
	GestureDetector swipeGestureDetector;

	public HourEventsPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HourEventsPanel(Context context) {
		this(context, null);	
	}

	public void setSwipeGestureDetector(GestureDetector swipeGestureDetector) {
		this.swipeGestureDetector = swipeGestureDetector;
	}

	public GestureDetector getSwipeGestureDetector() {
		return swipeGestureDetector;
	}
	
	
}
