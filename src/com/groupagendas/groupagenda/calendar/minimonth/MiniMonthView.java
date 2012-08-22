package com.groupagendas.groupagenda.calendar.minimonth;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.agenda.AgendaFrame;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;

public class MiniMonthView extends AbstractCalendarView {
	
	
	private Calendar selectedMonth;
	
	
	
	
	ArrayList<AgendaFrame> daysList = new ArrayList<AgendaFrame>();
	
	private TableLayout miniMonthTable;




	private Calendar firstShownDate;


	public MiniMonthView(Context context) {
		this(context, null);
	}
	public MiniMonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);

		

		
	}

	
	@Override
	protected void setTopPanel() {
		String title = MonthNames[selectedMonth.get(Calendar.MONTH) - 1];
		title += " ";
		title += selectedMonth.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);

	}

	@Override
	public void goPrev() {
		int LastMonthWeeksCount = selectedMonth.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedMonth.add(Calendar.MONTH, -1);
		firstShownDate = updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedMonth.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedMonth);
		}
		setDaysTitles();
		updateEventLists();

	}

	
	@Override
	public void goNext() {
		int LastMonthWeeksCount = selectedMonth.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedMonth.add(Calendar.MONTH, 1);
		firstShownDate = updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedMonth.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedMonth);
		}
		
		setDaysTitles();
		updateEventLists();

	}
	

	@Override
	public void setupView() {
		miniMonthTable = (TableLayout) findViewById(R.id.agenda_table);
		paintTable(selectedMonth);
		setDaysTitles();
		updateEventLists();
	}
	
	private void paintTable(Calendar date) {
		miniMonthTable.removeAllViews();
		int FRAMES_PER_ROW = date.getMaximum(Calendar.DAY_OF_WEEK);
		int TABLE_ROWS_COUNT = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		
		
//	TODO	miniMonthTable.setOnTouchListener(createListener(swipeGestureDetector));
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);		
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
		        VIEW_WIDTH / FRAMES_PER_ROW,
		        VIEW_HEIGHT/TABLE_ROWS_COUNT,
		        1.0f);		

//		Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT; i++){
			row = (TableRow) mInflater.inflate(R.layout.calendar_mm_row, null);
			for (int j = 0; j < FRAMES_PER_ROW; j++)
			addDay(row, cellLp);
			miniMonthTable.addView(row, rowLp);
		}		
	}
	
	
	private void addDay(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout dayFrame = (LinearLayout) mInflater.inflate(R.layout.calendar_mm_day_container, null);
		row.addView(dayFrame, cellLp);
		daysList.add(new AgendaFrame(dayFrame, getContext()));
		
	}
	
	private void setDaysTitles() {
		
		Calendar tmp = (Calendar) firstShownDate.clone();
		int firstDayOfWeek = tmp.getFirstDayOfWeek();
		
		for (AgendaFrame frame : daysList){
			TextView dayTitle = (TextView) frame.getDayContainer().findViewById(R.id.agenda_day_title);		
			String title = "" + tmp.get(Calendar.DATE);
			dayTitle.setText(title);
			
			if (Utils.isToday(tmp)){
				dayTitle.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			} else{
				dayTitle.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			}
			
			if (tmp.get(Calendar.DAY_OF_WEEK) == firstDayOfWeek){
				TextView WeekNum = (TextView) frame.getDayContainer().findViewById(R.id.agenda_week_title);	
				WeekNum.setVisibility(VISIBLE);
				WeekNum.setText("" + tmp.get(Calendar.WEEK_OF_MONTH));
			}
			
			tmp.add(Calendar.DATE, 1);
		}
		
	}
	@Override
	protected void updateEventLists() {
		Calendar tmp = (Calendar) firstShownDate.clone();
		
		for (AgendaFrame frame : daysList){	
			if (tmp.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH)){
				frame.setEventList(Data.getEventByDate(tmp));
			}else {
				frame.setEventList(new ArrayList<Event>());
			}
			frame.UpdateList();
			tmp.add(Calendar.DATE, 1);
		}
	}
	
	private Calendar updateShownDate() {
		Calendar tmp = (Calendar) selectedMonth.clone();
		Utils.setCalendarToFirstDayOfMonth(tmp);
		Utils.setCalendarToFirstDayOfWeek(tmp);
		return tmp;
	}
	@Override
	public Calendar getDateToResume() {
		return selectedMonth;
	}
	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedMonth = initializationDate;
		firstShownDate = updateShownDate();
		
	}
	
	

}
