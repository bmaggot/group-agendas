package com.groupagendas.groupagenda.calendar;


import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.events.NewEventActivity;

import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SwipeOnGestureListener extends SimpleOnGestureListener {

	  private static final int SWIPE_MIN_DISTANCE = 10;
	  private static final int SWIPE_MAX_OFF_PATH = 600;
	  private static final int SWIPE_THRESHOLD_VELOCITY = 20;
	  
	  AbstractCalendarView parentView;
	  
	  public SwipeOnGestureListener (AbstractCalendarView parent){
		  parentView = parent;
	  }
	  
	  public boolean onSingleTapUp(MotionEvent e){
		  Activity navbar = (Activity)(parentView.getContext());
		  Intent intent = new Intent(navbar, NewEventActivity.class);
		  navbar.startActivity(intent);
		return true;
		  
	  }

	  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {  
	   System.out.println(" in onFling() :: ");
	   if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	    return false;
	   if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
	     && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		   parentView.goNext();
	   } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
	     && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		   parentView.goPrev();
	   }
	   return super.onFling(e1, e2, velocityX, velocityY);
	  }
	}