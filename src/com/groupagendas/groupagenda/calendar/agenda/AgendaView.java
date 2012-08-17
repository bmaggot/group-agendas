package com.groupagendas.groupagenda.calendar.agenda;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.utils.Utils;

public class AgendaView extends AbstractCalendarView {
	
	private Calendar selectedDay;
	private Calendar shownDate;
	public int TABLE_ROWS_COUNT = 3;
	public int SHOWN_DAYS_COUNT = 7;
	
	ArrayList<LinearLayout> daysList = new ArrayList<LinearLayout>();
	
	private TableLayout agendaTable;

	public AgendaView(Context context) {
		this(context, null);
	}

	public AgendaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names_short);
		this.selectedDay = ((NavbarActivity)context).getSelectedDate();
		this.shownDate = (Calendar)selectedDay.clone();
		Utils.setCalendarToFirstDayOfWeek(this.shownDate);
	}

	@Override
	protected void setTopPanel() {
		String title = getResources().getString(R.string.week);
		title += " ";
		title += shownDate.get(Calendar.WEEK_OF_YEAR);
		this.getTopPanelTitle().setText(title);

	}

	@Override
	public void goPrev() {
		shownDate.add(Calendar.DATE, -1 * SHOWN_DAYS_COUNT);
		setTopPanel();
		setDaysTitles();

	}

	@Override
	public void goNext() {
		shownDate.add(Calendar.DATE, SHOWN_DAYS_COUNT);
		setTopPanel();
		setDaysTitles();

	}

	@Override
	public void setupView() {
		agendaTable = (TableLayout) findViewById(R.id.agenda_table);
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);
		

//		Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT - 1; i++){
			row = (TableRow) mInflater.inflate(R.layout.calendar_agenda_row, null);
			addWorkingDay(row, cellLp);
			addWorkingDay(row, cellLp);	
			agendaTable.addView(row, rowLp);
		}
//		Add last row
		row = (TableRow) mInflater.inflate(R.layout.calendar_agenda_row, null);
		addWorkingDay(row, cellLp);
		addWeekend(row, cellLp);
		agendaTable.addView(row, rowLp);
		
		setDaysTitles();
		

	}

	private void setDaysTitles() {
		int day = 0;
		for (LinearLayout frame : daysList){
			TextView dayTitle = (TextView) frame.findViewById(R.id.agenda_day_title);
			Calendar tmp = (Calendar) shownDate.clone();
			tmp.add(Calendar.DATE, day);
			day++;
			
			String title = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) -1];
			title += ", ";
			title += MonthNames[tmp.get(Calendar.MONTH)];
			title += " ";
			title += tmp.get(Calendar.DATE);
			title += ", ";
			title += tmp.get(Calendar.YEAR);
			dayTitle.setText(title);			
		}
		
	}

	private void addWeekend(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);
		LinearLayout weekEndFrame = new LinearLayout(getContext());
		weekEndFrame.setOrientation(VERTICAL);
		
		LinearLayout saturday = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_nd_container, null);
		weekEndFrame.addView(saturday, params);
		daysList.add(saturday);
		
		LinearLayout sunday = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_nd_container, null);
		weekEndFrame.addView(sunday, params);
		daysList.add(sunday);
		
		row.addView(weekEndFrame, cellLp);	
	}

	private void addWorkingDay(TableRow row, android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout workingDayFrame = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_wd_container, null);
		row.addView(workingDayFrame, cellLp);
		daysList.add(workingDayFrame);
		
	}

}
