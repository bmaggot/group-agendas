package com.groupagendas.groupagenda.calendar.day;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
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
