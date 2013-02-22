package com.groupagendas.groupagenda.calendar.month;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;

import com.groupagendas.groupagenda.R;

/**
 * @author Tadas
 *
 */
public final class MonthViewCache {
	private final Map<Integer, Reference<MonthView>> previews;
	
	@SuppressLint("UseSparseArrays")
	private MonthViewCache() {
		previews = new HashMap<Integer, Reference<MonthView>>();
	}
	
	public Reference<MonthView> createRef(MonthView view) {
		return new SoftReference<MonthView>(view);
	}
	
	private Integer getKey(int year, int month) {
		return (year << 8) | month;
	}
	
	public synchronized MonthView getView(Calendar id, LayoutInflater inflater) {
		MonthView view = null;
		final Integer key = getKey(id.get(Calendar.YEAR), id.get(Calendar.MONTH));
		Reference<MonthView> ref = previews.get(key);
		if (ref != null)
			view = ref.get();
		if (view == null) {
			view = (MonthView) inflater.inflate(R.layout.calendar_month, null);
//			Log.d("MVC", "==== START ==== MVC default initialization (bugless)");
			view.init(id, false);
//			Log.d("MVC", "===== END ===== MVC default initialization (bugless)");
			previews.put(key, createRef(view));
		} else
			inheritDay(id, view.getSelectedDate());
		return view;
	}
	
	public void inheritDay(Calendar from, Calendar to) {
		final int selected = from.get(Calendar.DAY_OF_MONTH);
		final int max = to.getActualMaximum(Calendar.DAY_OF_MONTH);
		to.set(Calendar.DAY_OF_MONTH, Math.min(selected, max));
	}
	/*
	private static class UiThreadProbe extends Thread {
		private final Thread uiThread;
		
		private UiThreadProbe(Thread uiThread) {
			super("UiThreadProbe");
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
	public void prefetchInUiThread(Calendar selected, LayoutInflater inflater) {
		MonthViewCache mvc = getInstance();
		Calendar c = (Calendar) selected.clone();
		{
			Calendar p = (Calendar) c.clone();
			p.set(Calendar.DAY_OF_MONTH, 1);
			p.add(Calendar.MONTH, -1);
			mvc.inheritDay(selected, p);
			mvc.getView(p, inflater);
		}
		{
			Calendar n = (Calendar) c.clone();
			n.set(Calendar.DAY_OF_MONTH, 1);
			n.add(Calendar.MONTH, 1);
			mvc.inheritDay(selected, n);
			mvc.getView(n, inflater);
		}
	}
	
	public static MonthViewCache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final MonthViewCache INSTANCE = new MonthViewCache();
	}
}
