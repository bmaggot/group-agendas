package com.groupagendas.groupagenda.calendar.month;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class MonthSwipeListener extends SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 20;

	protected MonthView parentView;

	public MonthSwipeListener(MonthView parent) {
		parentView = parent;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
			return false;
		}
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				parentView.goNext();
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				parentView.goPrev();
		}
		return false;
//		return super.onFling(e1, e2, velocityX, velocityY);
	}
	
	@Override
    public boolean onDown(MotionEvent e) {
            return true;
    }
}
