package com.groupagendas.groupagenda.calendar.month;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.groupagendas.groupagenda.R;

/**
 * @author Tadas
 *
 */
public final class MonthViewCache {
	private final Map<Integer, Reference<MonthView>> previews;
	// private AsyncTask<?, ?, ?> task;
	private Runnable task;
	
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
	/*
	public synchronized MonthView getView(int year, int month, LayoutInflater inflater) {
		Calendar id = Calendar.getInstance();
		id.set(Calendar.YEAR, year);
		id.set(Calendar.MONTH, month);
		return getView(id, inflater);
	}
	*/
	public synchronized MonthView getView(Calendar id, LayoutInflater inflater) {
		MonthView view = null;
		final Integer key = getKey(id.get(Calendar.YEAR), id.get(Calendar.MONTH));
		Reference<MonthView> ref = previews.get(key);
		if (ref != null)
			view = ref.get();
		if (view == null) {
			view = (MonthView) inflater.inflate(R.layout.calendar_month, null);
			view.init(id);
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
	
	public synchronized void prefetch(final ViewGroup root, final Calendar selected, final LayoutInflater inflater) {
		/*
		if (task != null)
			task.cancel(true);
		task = new FetchNext(root, selected, inflater).execute();
		*/
		if (task != null)
			root.removeCallbacks(task);
		root.post(task = new Runnable() {
			@Override
			public void run() {
				MonthView prev, next;
				
				MonthViewCache mvc = getInstance();
				Calendar c = (Calendar) selected.clone();
				{
					Calendar p = (Calendar) c.clone();
					p.set(Calendar.DAY_OF_MONTH, 1);
					p.add(Calendar.MONTH, -1);
					mvc.inheritDay(selected, p);
					prev = mvc.getView(p, inflater);
				}
				{
					Calendar n = (Calendar) c.clone();
					n.set(Calendar.DAY_OF_MONTH, 1);
					n.add(Calendar.MONTH, 1);
					mvc.inheritDay(selected, n);
					next = mvc.getView(n, inflater);
				}

				/*
				if (root.getChildCount() > 1) {
					for (int i = 0; i < root.getChildCount(); i++) {
						Log.e("MVC", "At " + i + ": " + root.getChildAt(i));
					}
				}
				Log.e("MVC", root.getChildAt(0) + " (" + ((MonthView) root.getChildAt(0)).getSelectedDate().get(Calendar.MONTH) + ") is a member of " + root);
				*/
				
				// cyclic VA
				// Log.e("MVC", next + " (" + next.getSelectedDate().get(Calendar.MONTH) +
				// 		") will become a member of " + root);
				root.addView(next);
				// Log.e("MVC", prev + " (" + prev.getSelectedDate().get(Calendar.MONTH) +
				// 		") will become a member of " + root);
				root.addView(prev);
				/*
				Log.e("MVC", "Prev: " + prev + ", next: " + next);
				for (int i = 0; i < root.getChildCount(); i++) {
					Log.e("MVC", "At " + i + ": " + root.getChildAt(i));
				}
				*/
			}
		});
	}
	
	public static MonthViewCache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final MonthViewCache INSTANCE = new MonthViewCache();
	}
}
