package com.groupagendas.groupagenda.calendar.cache;

import java.util.Calendar;

import android.view.LayoutInflater;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.dayandweek.DayWeekView;

/**
 * @author Tadas
 *
 */
public class DayWeekViewCache extends CalendarViewCache<DayWeekView> {
	public void flush(DayWeekView flushed, Calendar startDate, LayoutInflater inflater) {
		preview.clear();
		preview.put(getKey(startDate), createRef(flushed));
		prefetchInUiThread(startDate, inflater);
	}
	
	@Override
	public int getLayoutId() {
		return R.layout.calendar_week;
	}
	
	@Override
	protected Integer getKey(Calendar date) {
		return (date.get(Calendar.YEAR) << 16) | date.get(Calendar.DAY_OF_YEAR);
	}
	
	@Override
	public void adjustDate(Calendar oldDate, Calendar date, boolean ltr) {
		date.add(Calendar.DAY_OF_YEAR, ltr ? -DayWeekView.getDaysToShow() : DayWeekView.getDaysToShow());
	}
	
	public static DayWeekViewCache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final DayWeekViewCache INSTANCE = new DayWeekViewCache();
	}
}
