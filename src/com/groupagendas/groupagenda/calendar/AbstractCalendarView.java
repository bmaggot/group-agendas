package com.groupagendas.groupagenda.calendar;


import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public abstract class AbstractCalendarView extends LinearLayout {
	protected GestureDetector swipeGestureDetector;
	
	public AbstractCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AbstractCalendarView(Context context) {
		this(context, null);
		
	}
	
	public abstract void goPrev();
	
	public abstract void goNext();
	
	protected void setUpSwipeGestureListener(){
		swipeGestureDetector = new GestureDetector(new SwipeOnGestureListener(this));
		
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (swipeGestureDetector.onTouchEvent(event)) {
				     return false;
				    } else {
				     return true;
				    }
			}
		});
	}
}
