package com.groupagendas.groupagenda.calendar.cache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;

import com.groupagendas.groupagenda.CustomAnimator;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.month.MonthView;

/**
 * Allows caching various {@link AbstractCalendarView}s, mainly to allow animations
 * when performing a transition between dates.<BR><BR>
 * Currently, caches are only used in conjunction with a {@link CustomAnimator}.
 * Otherwise, data is updated inside the view that displayed the previous date.
 * 
 * @param <E> cached view type
 * @author Tadas
 */
public abstract class CalendarViewCache<E extends AbstractCalendarView> {
	protected final Map<Integer, Reference<E>> preview;
	
	/** Creates a cache. */
	@SuppressLint("UseSparseArrays")
	protected CalendarViewCache() {
		preview = new HashMap<Integer, Reference<E>>();
	}
	
	/**
	 * Returns the layout ID that produces an instance of a cacheable type when inflated.
	 * 
	 * @return the layout ID
	 */
	public abstract int getLayoutId();
	/**
	 * Returns an unique key identifying a cacheable view that displays the specified date.
	 * 
	 * @param date a date that is shown in a cacheable view
	 * @return an unique key
	 */
	protected abstract Integer getKey(Calendar date);
	/**
	 * Adjusts a date to point to an immediately accessible view.
	 * 
	 * @param oldDate date of the old view
	 * @param date a date that needs to be adjusted
	 * @param ltr whether a swipe was made from left to right
	 */
	public abstract void adjustDate(Calendar oldDate, Calendar date, boolean ltr);
	/**
	 * Prepares a cached view before animation.<BR>
	 * <BR>
	 * Only used with {@link MonthView}, in order to match the selected day between months
	 * before an animation starts.
	 * @param prevDate date of the old view
	 * @param view the view to be displayed
	 */
	protected void prepareCachedView(Calendar prevDate, E view) {
		// do nothing
	}
	
	protected Reference<E> createRef(E view) {
		return new SoftReference<E>(view);
	}
	
	/**
	 * Retrieves a view from cache, [re]adding it to cache, if necessary.<BR>
	 * <BR>
	 * The view will be prepared for animation.
	 * @param id date that identifies the cached view
	 * @param inflater a layout inflater
	 * @return a cached view
	 */
	@SuppressWarnings("unchecked")
	public synchronized E getView(Calendar id, LayoutInflater inflater) {
		E view = null;
		final Integer key = getKey(id);
		Reference<E> ref = preview.get(key);
		if (ref != null)
			view = ref.get();
		if (view == null) {
			view = (E) inflater.inflate(getLayoutId(), null);
			view.init(id, false);
			preview.put(key, createRef(view));
		} else
			prepareCachedView(id, view);
		return view;
	}
	
	/**
	 * Stores immediately accessible (previous/next) views to cache.
	 * Does not recreate views already in cache.<BR>
	 * <BR>
	 * <B>Must be called from the UI thread.</B>
	 * 
	 * @param selected date that identifies the displayed view
	 * @param inflater a layout inflater
	 */
	public void prefetchInUiThread(Calendar selected, LayoutInflater inflater) {
		Calendar c = (Calendar) selected.clone();
		{
			Calendar p = (Calendar) c.clone();
			adjustDate(selected, p, true);
			getView(p, inflater);
		}
		{
			Calendar n = (Calendar) c.clone();
			adjustDate(selected, n, false);
			getView(n, inflater);
		}
	}
	
	/*
	private static class UiProbeThread extends Thread {
		private final Thread uiThread;
		
		private UiProbeThread(Thread uiThread) {
			super("UiProbeThread");
			this.uiThread = uiThread;
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}
		
		@Override
		public void run() {
			try {
				final StringBuilder sb = new StringBuilder(1 << 8);
				for (int i = 0; !isInterrupted(); i++) {
					Thread.sleep(15L);
					
					StackTraceElement[] trace = uiThread.getStackTrace();
					
					if (trace[0].isNativeMethod() && trace[1].getLineNumber() == 118)
						// idle cycles already indicated by missing numbers
						continue;
					
					// stack trace always begins with
					// NativeStart
					// ZygoteInit (x2)
					// Then a reflective [native] method invocation
					// Then the method (ActivityThread#main)
					// then the message loop (Looper, Handler)
					
					sb.append(i).append(": ");
					sb.append(trace[0]).append(" <- ").append(trace[1]);
					if (trace.length > 9) {
						sb.append("\r\n...\r\n");
						sb.append(trace[trace.length - 9]).append(" <- ").append(trace[trace.length - 8]);
					}
					sb.append("\r\n");
					
					if (i % 100 == 0) {
						Log.e(getClass().getSimpleName(), sb.toString());
						sb.setLength(0);
					}
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	*/
}
