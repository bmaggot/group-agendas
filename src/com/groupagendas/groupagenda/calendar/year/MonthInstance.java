package com.groupagendas.groupagenda.calendar.year;

import java.util.Calendar;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.utils.Utils;

public class MonthInstance implements OnTouchListener{
	private static final int ROWS_COUNT = 6;
	private static final int DAYS_PER_WEEK = 7;
	public static boolean SHOW_BUBBLES = true;
	private final String monthNames[];
	private TextView title;
	private Calendar date;
	private Calendar firstShownDate;
	private static int[] weekendPos = null;
	boolean todayThisMonth = false;
	
	private LinearLayout daysTable;
	
	int firstDayOfMonthPos = 0;
	public MonthInstance(Calendar date, LinearLayout layout, String monthNames[]) {
		
		daysTable = (LinearLayout) layout.findViewById(R.id.year_month_table);
		this.monthNames = monthNames;
		title = (TextView) layout.findViewById(R.id.year_month_name);
		setNewDate (date);
	}
	
	private void setFirstShownDate() {
		firstShownDate = (Calendar) date.clone();
		Utils.setCalendarToFirstDayOfMonth(firstShownDate);
		Utils.setCalendarToFirstDayOfWeek(firstShownDate);
	}

	public void setNewDate(Calendar date){
		this.date = date;
		Calendar tmp = Calendar.getInstance();
		this.todayThisMonth = (tmp.get(Calendar.MONTH) == date.get(Calendar.MONTH) 
				&& tmp.get(Calendar.YEAR) == date.get(Calendar.YEAR)); 
		setFirstShownDate();
		refresh();
	}
	
	private void refresh() {
		title.setText(monthNames[date.get(Calendar.MONTH)]);
		refreshDayCells();
		refreshWeekNumberCells();
		refreshWeekEndCells();
		if (todayThisMonth) markTodayCell();
	}
	
	private void markTodayCell() {
		YearViewMonthInnerCell today = getDayFrame(Utils.createNewTodayCalendar());
		if (today.getState() != MonthCellState.SELECTED) today.setState(MonthCellState.TODAY);
	}

	private void refreshWeekEndCells() {
		
		int [] weekends = this.getWeekends();
		
		for (int i = 0; i < ROWS_COUNT; i++){
			LinearLayout row = (LinearLayout) daysTable.getChildAt(i);
			for (int j = 0; j < weekends.length; j++){
				YearViewMonthInnerCell cell = (YearViewMonthInnerCell) row.getChildAt(weekends[j]);
				if(cell.getState() != MonthCellState.OTHER_MONTH) 
					cell.setState(MonthCellState.WEEK_END);
			}
		}
		
		
	}

	private int[] getWeekends() {
		if (weekendPos == null){
			weekendPos = new int [CalendarSettings.getWeekends().length];
			int firstDayOfWeek = date.getFirstDayOfWeek();
			int offset = date.getActualMinimum(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
			for (int i = 0; i < CalendarSettings.getWeekends().length; i++){
				int calculatedPos = CalendarSettings.getWeekends()[i] + offset;
				if (calculatedPos < date.getActualMinimum(Calendar.DAY_OF_WEEK)) calculatedPos += date.getActualMaximum(Calendar.DAY_OF_WEEK);
				weekendPos[i] = calculatedPos;
			}
		}
		return weekendPos;
	}

	private void refreshWeekNumberCells() {
		Calendar tmp = (Calendar) date.clone();
		for (int i = 0; i < ROWS_COUNT; i++){
			TextView weekNumber = (TextView) ((LinearLayout)daysTable.getChildAt(i)).getChildAt(0);
			weekNumber.setText("" + tmp.get(Calendar.WEEK_OF_YEAR));
			tmp.add(Calendar.DATE, DAYS_PER_WEEK);
		}	
	}
	
//	private YearViewMonthInnerCell getDay (Calendar day){
//		YearViewMonthInnerCell ret = null;
//		Calendar tmp = (Calendar) firstShownDate.clone();
//		for (int i = 0; i < ROWS_COUNT; i++){
//			for (int j = 1; j <= DAYS_PER_WEEK; j++){
//				if(Utils.isSameDay(day, tmp)){
//					LinearLayout row = (LinearLayout) daysTable.getChildAt(i);
//					return (YearViewMonthInnerCell) row.getChildAt(j);
//				}
//				tmp.add(Calendar.DATE, 1);
//			}
//		}
//		return ret;
//	}
	
	private void refreshDayCells() {
		
		Calendar tmp = (Calendar) date.clone();		
		//set first few days invisible
		for (int j = 1; j <= Utils.getDayOfWeek(tmp); j++){
			YearViewMonthInnerCell dayCell = (YearViewMonthInnerCell) ((LinearLayout)daysTable.getChildAt(0)).getChildAt(j);
			dayCell.setState(MonthCellState.OTHER_MONTH);
		}
		
		//setup first week visible days
		int day = 1;
		int i, j;
		YearViewMonthInnerCell dayCell;
		
		for (j = Utils.getDayOfWeek(tmp); j <= DAYS_PER_WEEK; j++){
			dayCell = (YearViewMonthInnerCell) ((LinearLayout)daysTable.getChildAt(0)).getChildAt(j);
			dayCell.setDayNum("" + day);
			dayCell.setState(MonthCellState.DEFAULT);
			dayCell.setHasEvents(!Data.getEventByDate(tmp).isEmpty() && SHOW_BUBBLES);
			tmp.add(Calendar.DATE, 1);
			day++;
		}
		
		//setup other visible days
		loop: for (i = 1; i < ROWS_COUNT; i++) {
				for (j = 1; j <= DAYS_PER_WEEK; j++) {
					dayCell = (YearViewMonthInnerCell) ((LinearLayout) daysTable
							.getChildAt(i)).getChildAt(j);
					dayCell.setDayNum("" + day);
					dayCell.setState(MonthCellState.DEFAULT);
					dayCell.setHasEvents(!Data.getEventByDate(tmp).isEmpty() && SHOW_BUBBLES);
					day++;
					tmp.add(Calendar.DATE, 1);
					if (day > date.getActualMaximum(Calendar.DAY_OF_MONTH))
						break loop;
				}
			}
		
		//setup last week invisible days
		j++;
		for (; j <= DAYS_PER_WEEK; j++){
			dayCell = (YearViewMonthInnerCell) ((LinearLayout)daysTable.getChildAt(i)).getChildAt(j);
			dayCell.setState(MonthCellState.OTHER_MONTH);
		}
		//setup other invisible days (if they exist)
		i++;
		for (; i < ROWS_COUNT; i++){
			for (j = 1; j <= DAYS_PER_WEEK; j++){
				dayCell = (YearViewMonthInnerCell) ((LinearLayout)daysTable.getChildAt(i)).getChildAt(j);
				dayCell.setState(MonthCellState.OTHER_MONTH);
			}
		}
			
		
	}

	public YearViewMonthInnerCell getDayFrame (Calendar day){
		LinearLayout row = (LinearLayout) daysTable.getChildAt(day.get(Calendar.WEEK_OF_MONTH) - 1);
		return (YearViewMonthInnerCell) row.getChildAt(Utils.getDayOfWeek(day));
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return false;
	}

	public Calendar getDateByCoordinates(int rowPos, int colPos) {
		Calendar day = (Calendar) firstShownDate.clone();
		day.add(Calendar.DATE, ((DAYS_PER_WEEK * rowPos) + colPos - 1));
		return day;
	}

	public void unselectDay(Calendar day) {
		YearViewMonthInnerCell frame = getDayFrame(day);
		
		if (Utils.isToday(day)){
			frame.setState(MonthCellState.TODAY);
			return;
		}
		
		if (Utils.isWeekend(day)){
			frame.setState(MonthCellState.WEEK_END);
			return;
		}
		
		frame.setState(MonthCellState.DEFAULT);
		
	}
	
	

}
