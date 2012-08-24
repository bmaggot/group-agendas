package com.groupagendas.groupagenda.calendar.year;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.utils.Utils;

public class YearView extends AbstractCalendarView {

	private static final int MonthsPerRow = 3;
	private static final int MonthsInYear = 12;
	private static final int DaysPerWeek = 7;
	private static final int WeeksPerMonth = 6;
	private final int[] weekends = {1,7}; //TODO remove this hardcode
	private LinearLayout year_Table;
	private ArrayList<LinearLayout> monthFramesList;
	private HashMap<LinearLayout, ArrayList<YearViewMonthInnerCell>> yearDaysMap = new HashMap<LinearLayout, ArrayList<YearViewMonthInnerCell>>();
	private LayoutParams WeekCellParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1);
	private Calendar today = Utils.createNewTodayCalendar();

	public YearView(Context context) {
		this(context, null);
	}
	
	public YearView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);
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
//				System.out.println("IDEDU " + j);
				LinearLayout month = (LinearLayout) row.getChildAt(j);
				monthFramesList.add(month);
				
				Calendar firstDayOfMonthContainer = (Calendar) selectedDate.clone();
				firstDayOfMonthContainer.set(Calendar.MONTH, MonthNr);
				Utils.setCalendarToFirstDayOfMonth(firstDayOfMonthContainer);
				initMonthCell(monthFramesList.get(MonthNr), firstDayOfMonthContainer);
				MonthNr++;
			} 	 	
		}
		
		int month = today.get(Calendar.MONTH);
		int day = today.get(Calendar.DATE);
		YearViewMonthInnerCell cell = yearDaysMap.get(monthFramesList.get(month)).get(day);
		cell.setState(MonthCellState.TODAY);
		
		month = selectedDate.get(Calendar.MONTH);
		day = selectedDate.get(Calendar.DATE);
		cell = yearDaysMap.get(monthFramesList.get(month)).get(day);
		cell.setState(MonthCellState.TODAY);
		
		

	}

	
	private void initMonthCell(LinearLayout monthContainer, Calendar firstDayOfMonthContainer) {
		
		TextView title = (TextView) monthContainer.findViewById(R.id.year_month_name);
		String text = MonthNames[firstDayOfMonthContainer.get(Calendar.MONTH)];
		title.setText(text);
		
		int thisMonth = firstDayOfMonthContainer.get(Calendar.MONTH);
		
		Utils.setCalendarToFirstDayOfWeek(firstDayOfMonthContainer);
		
		ArrayList<YearViewMonthInnerCell> dayCellsArray = new ArrayList<YearViewMonthInnerCell>();
		yearDaysMap.put(monthContainer, dayCellsArray);
		
		LinearLayout monthTable = (LinearLayout) monthContainer.findViewById(R.id.year_month_table);
		for (int i = 0; i < WeeksPerMonth; i ++){
			LinearLayout row = new LinearLayout(getContext());
			
			
			YearViewMonthInnerCell monthCell = (YearViewMonthInnerCell) mInflater.inflate(R.layout.calendar_year_month_inner_cell, null);
			monthCell.setupWeekNum("" + firstDayOfMonthContainer.get(Calendar.WEEK_OF_YEAR));
			row.addView(monthCell, WeekCellParams);
			
			for (int j = 0; j < DaysPerWeek; j++){
				
				monthCell = (YearViewMonthInnerCell) mInflater.inflate(R.layout.calendar_year_month_inner_cell, null);
				
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
				row.addView(monthCell, WeekCellParams);
				
				dayCellsArray.add(monthCell);
				firstDayOfMonthContainer.add(Calendar.DATE, 1);
			}
			monthTable.addView(row, WeekCellParams);
			
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
