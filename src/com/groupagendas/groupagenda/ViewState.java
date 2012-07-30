package com.groupagendas.groupagenda;

public enum ViewState {
	MONTH, DAY, WEEK, YEAR, MINI_MONTH, AGENDA, TODAY, GO_TO_DATE, LIST_SEARCH;

	public static ViewState getValueByString(String defaultCalendarView) {
		if (defaultCalendarView.equalsIgnoreCase("TODAY")|| defaultCalendarView.equalsIgnoreCase("T")) return TODAY;
		if (defaultCalendarView.equalsIgnoreCase("DAY") || defaultCalendarView.equalsIgnoreCase("D")) return DAY;
		if (defaultCalendarView.equalsIgnoreCase("WEEK")|| defaultCalendarView.equalsIgnoreCase("W")) return WEEK;
		if (defaultCalendarView.equalsIgnoreCase("MONTH")|| defaultCalendarView.equalsIgnoreCase("M")) return MONTH;
		if (defaultCalendarView.equalsIgnoreCase("MINI_MONTH")) return MINI_MONTH;
		if (defaultCalendarView.equalsIgnoreCase("YEAR")) return YEAR;
		if (defaultCalendarView.equalsIgnoreCase("AGENDA")) return AGENDA;
		if (defaultCalendarView.equalsIgnoreCase("GO_TO_DATE")) return GO_TO_DATE;
		if (defaultCalendarView.equalsIgnoreCase("LIST_SEARCH")) return LIST_SEARCH;
		return null;
	}
}
