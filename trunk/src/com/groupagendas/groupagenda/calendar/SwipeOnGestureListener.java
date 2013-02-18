package com.groupagendas.groupagenda.calendar;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.groupagendas.groupagenda.calendar.month.MonthView;

public class SwipeOnGestureListener extends SimpleOnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 20;

	protected AbstractCalendarView parentView;

	public SwipeOnGestureListener(AbstractCalendarView parent) {
		parentView = parent;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
			return false;
		}
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			if (!(parentView instanceof MonthView )) {
				GenericSwipeAnimator.startAnimation(parentView, true, new Runnable() {
					@Override
					public void run() {
						parentView.goNext();
					}
				});
				// parentView.goNext();
			}
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			if (!(parentView instanceof MonthView )) {
				GenericSwipeAnimator.startAnimation(parentView, false, new Runnable() {
					@Override
					public void run() {
						parentView.goPrev();
					}
				});
				// parentView.goPrev();
			}
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}
}