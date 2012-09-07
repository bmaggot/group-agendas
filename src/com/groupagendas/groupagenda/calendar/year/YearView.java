package com.groupagendas.groupagenda.calendar.year;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.utils.Utils;

public class YearView extends AbstractCalendarView {

	
	private static final int MonthsInYear = 12;
	private static final int DAYS_PER_WEEK = 7;
	
	private LinearLayout year_Table;
	private ArrayList<ArrayList<YearViewMonthInnerCell>>yearDaysMap = new ArrayList<ArrayList<YearViewMonthInnerCell>>();
	private MonthInstance months[] = new MonthInstance[MonthsInYear];
	

	public YearView(Context context) {
		this(context, null);
	}
	
	public YearView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);
		
		//init array of all year day frames;
		for (int i = 0; i < MonthsInYear; i++){
		yearDaysMap.add(new ArrayList<YearViewMonthInnerCell>());
		}
		
	}

	@Override
	protected void setTopPanel() {
		String title = getContext().getString(R.string.year);
		title += " ";
		title += selectedDate.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);

	}

	@Override
	public void goPrev() {
		selectedDate.add(Calendar.YEAR, -1);
		setTopPanel();
		refresh();
	}



	@Override
	public void goNext() {
		selectedDate.add(Calendar.YEAR, 1);
		setTopPanel();
		refresh();
	}
	
	

	@Override
	public void setupView() {

		year_Table = (LinearLayout) findViewById(R.id.year_table);

		
	
		int MonthNr = 0;
		Calendar tmp = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfYear(tmp);
		
		for (int i = 0; i < year_Table.getChildCount(); i++){
			LinearLayout row = (LinearLayout) year_Table.getChildAt(i);
			for (int j = 0; j < row.getChildCount(); j++){	
				LinearLayout month = (LinearLayout) row.getChildAt(j);
				months[MonthNr] = new MonthInstance((Calendar) tmp.clone(), month, MonthNames);
				tmp.add(Calendar.MONTH, 1);
				month.findViewById(R.id.year_month_table).setOnTouchListener(new YearViewMonthOntouchListener(MonthNr));
				MonthNr++;
			} 	 	
		}
		
		
		
		
		
		

	}
	
	private class YearViewMonthOntouchListener implements OnTouchListener{
		private int Id;

		public YearViewMonthOntouchListener(int ID) {
			this.Id = ID;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			MonthInstance clickedMonthInstance = months[Id];
			TextView tmpCell = (TextView) ((LinearLayout) ((LinearLayout) v).getChildAt(0)).getChildAt(0); //first cell of month table just to get dimensions
			int cellWidth = tmpCell.getWidth();
			int cellHeight = tmpCell.getHeight();
			
			int colPos = (int) ((event.getX() / cellWidth));
			int rowPos = (int) ((event.getY() / cellHeight));
			Calendar date = clickedMonthInstance.getDateByCoordinates(rowPos, colPos);
			if (colPos > 0 && colPos <= DAYS_PER_WEEK){
				YearViewMonthInnerCell clickedDay = (YearViewMonthInnerCell) ((LinearLayout) ((LinearLayout)v).getChildAt(rowPos)).getChildAt(colPos);
				if (clickedDay.getState() != MonthCellState.OTHER_MONTH){
						unselectDate(selectedDate);
						selectDate(date, clickedDay);
						YearViewOnClickDialog dialog = new YearViewOnClickDialog(getContext(), selectedDate, R.style.yearview_eventlist);
						dialog.show();
				}
			}
			
			return false;
		}
		
	}

	private void refresh() {
		Calendar tmp = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfYear(tmp);
		for (int i = 0; i < months.length; i++){
			months[i].setNewDate((Calendar) tmp.clone());
			tmp.add(Calendar.MONTH, 1);
		}
		selectDate(selectedDate, null);
		
	}

	protected void selectDate(Calendar date, YearViewMonthInnerCell clickedDay) {
		if (clickedDay == null){
			months[date.get(Calendar.MONTH)].getDayFrame(date).setState(MonthCellState.SELECTED);
		}
		else{
			selectedDate = date;
			clickedDay.setState(MonthCellState.SELECTED);
		}		
	}

	protected void unselectDate(Calendar date) {
		MonthInstance monthInstance = months[date.get(Calendar.MONTH)];
		monthInstance.unselectDay(date);
		
	}

	@Override
	protected void updateEventLists() {
		// do nothing
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		selectedDate = initializationDate;

	}

	@Override
	public Calendar getDateToResume() {
		return selectedDate;
	}

}
