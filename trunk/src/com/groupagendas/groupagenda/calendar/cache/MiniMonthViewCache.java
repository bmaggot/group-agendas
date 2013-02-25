package com.groupagendas.groupagenda.calendar.cache;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.minimonth.MiniMonthView;

/**
 * @author Tadas
 *
 */
public class MiniMonthViewCache extends MonthCache<MiniMonthView> {
	@Override
	protected int getLayoutId() {
		return R.layout.calendar_mm;
	}
	
	public static MiniMonthViewCache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final MiniMonthViewCache INSTANCE = new MiniMonthViewCache();
	}
}
