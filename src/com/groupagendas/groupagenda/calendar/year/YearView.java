package com.groupagendas.groupagenda.calendar.year;

import java.util.Calendar;

import android.R;
import android.content.Context;
import android.util.AttributeSet;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;

public class YearView extends AbstractCalendarView {

	public YearView(Context context) {
		this(context, null);
	}
	
	public YearView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setTopPanel() {
		String title = getContext().getString(R.string.yes);
		title += selectedDate.get(Calendar.YEAR);
//		this.getTopPanelTitle().setText(title);

	}

	@Override
	public void goPrev() {
		// TODO Auto-generated method stub

	}

	@Override
	public void goNext() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupView() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateEventLists() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		selectedDate = initializationDate;

	}

	@Override
	public Calendar getDateToResume() {
		// TODO Auto-generated method stub
		return null;
	}

}
