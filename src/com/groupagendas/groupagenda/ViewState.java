package com.groupagendas.groupagenda;

public enum ViewState {
	MONTH, DAY, WEEK, YEAR, MINI_MONTH, AGENDA, TODAY, GO_TO_DATE, LIST_SEARCH, CHAT_THREADS;

	public static ViewState getValueByString(String defaultCalendarView) {
		if (defaultCalendarView.equalsIgnoreCase("DAY") || defaultCalendarView.equalsIgnoreCase("D")) return DAY;
		if (defaultCalendarView.equalsIgnoreCase("WEEK")|| defaultCalendarView.equalsIgnoreCase("W")) return WEEK;
		if (defaultCalendarView.equalsIgnoreCase("MONTH")|| defaultCalendarView.equalsIgnoreCase("M")) return MONTH;
		if (defaultCalendarView.equalsIgnoreCase("MINI_MONTH")|| defaultCalendarView.equalsIgnoreCase("MM")) return MINI_MONTH;
		if (defaultCalendarView.equalsIgnoreCase("YEAR")|| defaultCalendarView.equalsIgnoreCase("Y")) return YEAR;
		if (defaultCalendarView.equalsIgnoreCase("AGENDA")|| defaultCalendarView.equalsIgnoreCase("A")) return AGENDA;
		if (defaultCalendarView.equalsIgnoreCase("GO_TO_DATE")|| defaultCalendarView.equalsIgnoreCase("G")) return GO_TO_DATE;
		if (defaultCalendarView.equalsIgnoreCase("LIST_SEARCH")|| defaultCalendarView.equalsIgnoreCase("L")) return LIST_SEARCH;
		if (defaultCalendarView.equalsIgnoreCase("CHAT_THREADS")|| defaultCalendarView.equalsIgnoreCase("CH")) return CHAT_THREADS;
		return null;
	}
}
