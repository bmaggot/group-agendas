package com.groupagendas.groupagenda.calendar.cache;

import java.util.Calendar;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.minimonth.MiniMonthView;
import com.groupagendas.groupagenda.calendar.month.MonthView;

/**
 * Shared methods for {@link MonthView} and {@link MiniMonthView}.
 * @author Tadas
 */
public abstract class MonthCache<E extends AbstractCalendarView> extends CalendarViewCache<E> {
	@Override
	public void adjustDate(Calendar oldDate, Calendar date, boolean ltr) {
		date.add(Calendar.MONTH, ltr ? -1 : 1);
	}
	
	@Override
	protected Integer getKey(Calendar date) {
		return (date.get(Calendar.YEAR) << 8) | date.get(Calendar.MONTH);
	}
}
