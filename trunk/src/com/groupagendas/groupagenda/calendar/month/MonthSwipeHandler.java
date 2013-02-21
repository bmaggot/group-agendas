package com.groupagendas.groupagenda.calendar.month;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewAnimator;

import com.groupagendas.groupagenda.R;

/**
 * @author Tadas
 *
 */
public class MonthSwipeHandler implements Runnable {
	public static final AtomicBoolean IN_PROGRESS = new AtomicBoolean(false);
	
	private final ViewAnimator va;
	private final Calendar oldDate;
	private final LayoutInflater inflater;
	private final boolean ltr;
	
	public MonthSwipeHandler(ViewAnimator va, Calendar oldDate, LayoutInflater inflater, boolean ltr) {
		this.va = va;
		this.oldDate = oldDate;
		this.inflater = inflater;
		this.ltr = ltr;
	}
	
	@Override
	public void run() {
		if (va.getChildCount() < 3) {
			Log.e("MSH", "still not prefetched yet");
			va.postDelayed(this, 100);
			return;
		}
		
		// v.setOnTouchListener(LocalTouchListener.this);
		va.setInAnimation(va.getContext(), ltr ? R.anim.swipe_ltr_enter_fully : R.anim.swipe_rtl_enter_fully);
		va.setOutAnimation(va.getContext(), ltr ? R.anim.swipe_ltr_leave_fully : R.anim.swipe_rtl_leave_fully);
		Animation a = va.getInAnimation();
		if (a.getDuration() > va.getOutAnimation().getDuration())
			a = va.getOutAnimation();
		a.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				va.setInAnimation(null);
				va.setOutAnimation(null);
				va.post(new Runnable() {
					@Override
					public void run() {
						// Log.e("MVC", va.getChildAt(2) + " (" + ((MonthView) va.getChildAt(2)).getSelectedDate().get(Calendar.MONTH) + ") will become parentless");
						va.removeViewAt(ltr ? 1 : 2);
						// Log.e("MVC", va.getChildAt(0) + " (" + ((MonthView) va.getChildAt(0)).getSelectedDate().get(Calendar.MONTH) + ") will become parentless");
						va.removeViewAt(0);
						MonthView cur = (MonthView) va.getCurrentView();
						cur.refresh(oldDate);
						IN_PROGRESS.set(false);
						MonthViewCache.getInstance().prefetch(va, cur.getSelectedDate(), inflater);
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// ((MonthView) va.getCurrentView()).refresh(parentView.getSelectedDate());
			}
		});
		
		if (ltr)
			va.showPrevious();
		else
			va.showNext();
	}
	
}
