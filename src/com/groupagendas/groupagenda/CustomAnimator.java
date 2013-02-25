package com.groupagendas.groupagenda;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ViewAnimator;

import com.groupagendas.groupagenda.calendar.month.MonthView;
import com.groupagendas.groupagenda.calendar.month.MonthViewCache;

/**
 * @author Tadas
 * 
 */
public class CustomAnimator extends ViewAnimator {
	private final AtomicBoolean animating = new AtomicBoolean(false);
	private AnimatorState state = new AnimatorState();
	
	/**
	 * @param context
	 */
	public CustomAnimator(Context context) {
		super(context);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public CustomAnimator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public boolean setupAnimator(Calendar oldDate, LayoutInflater inflater, boolean ltr, boolean useGivenDate) {
		if (!animating.compareAndSet(false, true))
			return false;
		
		if (getChildCount() > 1)
			removeViewAt(0);
		
		setInAnimation(getContext(), ltr ? R.anim.swipe_ltr_enter_fully : R.anim.swipe_rtl_enter_fully);
		setOutAnimation(getContext(), ltr ? R.anim.swipe_ltr_leave_fully : R.anim.swipe_rtl_leave_fully);
		
		state.targetOld = (getInAnimation().getDuration() < getOutAnimation().getDuration());
		
		state.newDate = (Calendar) oldDate.clone();
		if (!useGivenDate) {
			state.newDate.set(Calendar.DAY_OF_MONTH, 1);
			state.newDate.add(Calendar.MONTH, ltr ? -1 : 1);
			MonthViewCache.getInstance().inheritDay(oldDate, state.newDate);
		}
		state.inflater = inflater;
		state.view = MonthViewCache.getInstance().getView(state.newDate, state.inflater);
		state.view.redrawInheritedDate();
		ViewParent parent = state.view.getParent();
		if (parent != null) {
			if (parent == this)
				Log.w(getClass().getSimpleName(), "Concurrent swipes must not be allowed.");
			else
				Log.w(getClass().getSimpleName(), "MonthViewCache#getView() must not be added to layouts manually.");
			// try to recover
			if (parent instanceof ViewGroup)
				((ViewGroup) parent).removeView(state.view);
			else
				Log.e(getClass().getSimpleName(), "GAME OVER, exception will follow");
		}
		addView(state.view);
		showNext();
		return true;
	}
	
	public void onAnimationEnd(MonthView view) {
		{
			boolean ok;
			if (state.targetOld)
				ok = view != state.view;
			else
				ok = view == state.view;
			if (!ok)
				return;
		}
		
		setInAnimation(null);
		setOutAnimation(null);
		// removeViewAt(0); causes a NPE, so we move it to setup
		
		state.view.setupDelegates();
		state.view.refresh(state.newDate);
		MonthViewCache.getInstance().prefetchInUiThread(state.newDate, state.inflater);

		state = new AnimatorState();		
		animating.set(false);
	}
	
	private static class AnimatorState {
		private boolean targetOld;
		private Calendar newDate;
		private MonthView view;
		private LayoutInflater inflater;
	}
}
