package com.groupagendas.groupagenda.calendar.cache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;

/**
 * @author Tadas
 *
 */
public abstract class CalendarViewCache<E extends AbstractCalendarView> {
	private final Map<Integer, Reference<E>> preview;
	
	@SuppressLint("UseSparseArrays")
	protected CalendarViewCache() {
		preview = new HashMap<Integer, Reference<E>>();
	}
	
	public abstract int getLayoutId();
	
	public Reference<E> createRef(E view) {
		return new SoftReference<E>(view);
	}
	
	protected abstract Integer getKey(Calendar date);
	
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
			inheritDay(id, view.getSelectedDate());
		return view;
	}
	
	public abstract void adjustDate(Calendar oldDate, Calendar date, boolean ltr);
	
	public void inheritDay(Calendar from, Calendar to) {
		final int selected = from.get(Calendar.DAY_OF_MONTH);
		final int max = to.getActualMaximum(Calendar.DAY_OF_MONTH);
		to.set(Calendar.DAY_OF_MONTH, Math.min(selected, max));
	}
	
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
