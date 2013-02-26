package com.groupagendas.groupagenda.calendar.cache;

import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.month.MonthView;

/**
 * @author Tadas
 *
 */
public final class MonthViewCache extends MonthCache<MonthView> {
	private MonthViewCache() {
		// singleton
	}
	
	@Override
	public int getLayoutId() {
		return R.layout.calendar_month;
	}
	
	@Override
	public void adjustDate(Calendar oldDate, Calendar date, boolean ltr) {
		date.set(Calendar.DAY_OF_MONTH, 1);
		super.adjustDate(oldDate, date, ltr);
		MonthViewCache.getInstance().inheritDay(oldDate, date);
	}
	
	@Override
	protected void prepareCachedView(Calendar id, MonthView view) {
		inheritDay(id, view.getSelectedDate());
	}
	
	public void inheritDay(Calendar from, Calendar to) {
		final int selected = from.get(Calendar.DAY_OF_MONTH);
		final int max = to.getActualMaximum(Calendar.DAY_OF_MONTH);
		to.set(Calendar.DAY_OF_MONTH, Math.min(selected, max));
	}
	
	public static MonthViewCache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final MonthViewCache INSTANCE = new MonthViewCache();
	}
}
