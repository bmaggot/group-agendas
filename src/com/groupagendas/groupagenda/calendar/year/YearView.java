package com.groupagendas.groupagenda.calendar.year;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.utils.Utils;

public class YearView extends AbstractCalendarView {

	private static final int MonthsPerRow = 3;
	private static final int MonthsInYear = 12;
	private static final int DaysPerWeek = 7;
	private static final int WeeksPerMonth = 6;
	private final int[] weekends = {1,7}; //TODO remove this hardcode
	private LinearLayout year_Table;
	private ArrayList<LinearLayout> monthFramesList;
	private ArrayList<ArrayList<YearViewMonthInnerCell>>yearDaysMap = new ArrayList<ArrayList<YearViewMonthInnerCell>>();
	private LayoutParams WeekCellParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1);
	private Calendar today = Utils.createNewTodayCalendar();
	

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
		// TODO Auto-generated method stub

	}

	@Override
	public void goNext() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupView() {
		// TODO Auto-generated method stub
		monthFramesList = new ArrayList<LinearLayout>();
		year_Table = (LinearLayout) findViewById(R.id.year_table);
//		LinearLayout year = (LinearLayout) findViewById(R.id.year_table);
//		LinearLayout month = (LinearLayout) mInflater.inflate(R.layout.calendar_year_month_cell, year);
//		((TextView)month.findViewById(R.id.year_month_name)).setText("TESTAS");
		int MonthNr = 0;
		for (int i = 0; i < year_Table.getChildCount(); i++){
			LinearLayout row = (LinearLayout) year_Table.getChildAt(i);
			for (int j = 0; j < row.getChildCount(); j++){	

				LinearLayout month = (LinearLayout) row.getChildAt(j);

				
				Calendar firstDayOfMonthContainer = (Calendar) selectedDate.clone();
				firstDayOfMonthContainer.set(Calendar.MONTH, MonthNr);
				Utils.setCalendarToFirstDayOfMonth(firstDayOfMonthContainer);
				initMonthCell(month, firstDayOfMonthContainer);
				MonthNr++;
			} 	 	
		}
		
		
		
		YearViewMonthInnerCell cell = getCellFromMap(selectedDate);
		cell.setState(MonthCellState.SELECTED);
		if (!Utils.isSameDay(today, selectedDate)){
			cell = getCellFromMap(today);
			cell.setState(MonthCellState.TODAY);
			
		
		}
		
		
		
		

	}

	
	private YearViewMonthInnerCell getCellFromMap(Calendar date) {
		ArrayList<YearViewMonthInnerCell> monthCellsList = yearDaysMap.get(date.get(Calendar.MONTH));
		Calendar tmp = (Calendar) date.clone();
		Utils.setCalendarToFirstDayOfMonth(tmp);
		Utils.setCalendarToFirstDayOfWeek(tmp);
		int hiddenDates = tmp.getActualMaximum(Calendar.DATE) - tmp.get(Calendar.DATE);
		
		int day = date.get(Calendar.DATE) + hiddenDates;
		return monthCellsList.get(day);
	}

	private void initMonthCell(LinearLayout monthContainer, Calendar firstDayOfMonthContainer) {
		TextView title = (TextView) monthContainer.findViewById(R.id.year_month_name);
		String text = MonthNames[firstDayOfMonthContainer.get(Calendar.MONTH)];
		title.setText(text);
		
		int thisMonth = firstDayOfMonthContainer.get(Calendar.MONTH);
		Utils.setCalendarToFirstDayOfWeek(firstDayOfMonthContainer);

		
		ArrayList<YearViewMonthInnerCell> dayCellsArray = yearDaysMap.get(thisMonth);
		dayCellsArray.clear();
		
		
		LinearLayout monthTable = (LinearLayout) monthContainer.findViewById(R.id.year_month_table);
		monthTable.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int cellWidth = (((LinearLayout) v).getChildAt(0)).getWidth();
				int cellHeight = (((LinearLayout) v).getChildAt(0)).getHeight();
				System.out.println("CLICKED: " + event.getX() + " " + event.getY());
				System.out.println("Cell: " + cellHeight + "x" + cellWidth);
				return false;
			}
		});
		for (int i = 0; i < WeeksPerMonth; i++){
			LinearLayout row = (LinearLayout) monthTable.getChildAt(i);
			
			TextView weekDayNum = (TextView) row.getChildAt(0);
			weekDayNum.setText("" + firstDayOfMonthContainer.get(Calendar.WEEK_OF_YEAR));
			
			YearViewMonthInnerCell monthCell = null;
			
			
			for (int j = 1; j <= DaysPerWeek; j++){
				
				monthCell = (YearViewMonthInnerCell) row.getChildAt(j);
				text = "" + firstDayOfMonthContainer.get(Calendar.DATE);
				
				MonthCellState state = MonthCellState.DEFAULT;
				if (firstDayOfMonthContainer.get(Calendar.MONTH) != thisMonth){
					state = MonthCellState.OTHER_MONTH;
				} else {
					for (int w = 0; w < weekends.length; w++)
						if(weekends[w] == firstDayOfMonthContainer.get(Calendar.DAY_OF_WEEK)){
							state = MonthCellState.WEEK_END;
							break;
						}
				}
				
				monthCell.setupDayCell(text, state);	
				if (!Data.getEventByDate(firstDayOfMonthContainer).isEmpty())
					monthCell.setHasEvents(true);
				else monthCell.setHasEvents(false);
				
				dayCellsArray.add(monthCell);
				firstDayOfMonthContainer.add(Calendar.DATE, 1);
			}			
		}
		
		
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
