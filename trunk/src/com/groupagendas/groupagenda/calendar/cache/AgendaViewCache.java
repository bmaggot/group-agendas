package com.groupagendas.groupagenda.calendar.cache;

import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.agenda.AgendaView;

/**
 * @author Tadas
 *
 */
public class AgendaViewCache extends CalendarViewCache<AgendaView> {
	@Override
	public int getLayoutId() {
		return R.layout.calendar_agenda;
	}
	
	@Override
	public void adjustDate(Calendar oldDate, Calendar date, boolean ltr) {
		date.add(Calendar.DATE, ltr ? -AgendaView.SHOWN_DAYS_COUNT : AgendaView.SHOWN_DAYS_COUNT);
	}
	
	@Override
	protected Integer getKey(Calendar date) {
		return (date.get(Calendar.YEAR) << 8) | date.get(Calendar.WEEK_OF_YEAR);
	}
	
	public static AgendaViewCache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final AgendaViewCache INSTANCE = new AgendaViewCache();
	}
}
