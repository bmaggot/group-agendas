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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.agenda.AgendaFrame;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class MiniMonthView extends AbstractCalendarView {	
	
	ArrayList<AgendaFrame> daysList = new ArrayList<AgendaFrame>();
	
	private TableLayout miniMonthTable;

	private boolean showWeekTitle = false;



	private Calendar firstShownDate;

	private int FRAMES_PER_ROW;

	private int TABLE_ROWS_COUNT;


	public MiniMonthView(Context context) {
		this(context, null);
	}
	public MiniMonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);

		
	}
	@Override
	protected void instantiateTopPanelBottomLine() {
		LinearLayout calendarTopPanelBottomLine = new LinearLayout(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		calendarTopPanelBottomLine.setOrientation(LinearLayout.HORIZONTAL);
		calendarTopPanelBottomLine.setLayoutParams(params);
		getTopPanelBottomLine().addView(calendarTopPanelBottomLine);
		
	}
	
	@Override
	protected void setTopPanel() {
		String title = MonthNames[selectedDate.get(Calendar.MONTH)];
		title += " ";
		title += selectedDate.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);
		
		LinearLayout bottomBar = (LinearLayout)getTopPanelBottomLine().getChildAt(0);
		bottomBar.removeAllViews();
		
		TextView entry;
		if (showWeekTitle) {
			entry = (TextView) mInflater.inflate(
					R.layout.calendar_top_bar_bottomline_entry, null);
			entry.setText(R.string.week_title);
			entry.setPadding(DrawingUtils.convertDPtoPX(getContext(), 1), 0, 0, 0);
			bottomBar.addView(entry);
		}
		Calendar tmp = (Calendar) firstShownDate.clone();
//		add view for every day
		
		for (int i = 0; i < FRAMES_PER_ROW; i++){
			entry = (TextView) mInflater.inflate(R.layout.calendar_top_bar_bottomline_entry, null);
			
			String text = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1];
			tmp.add(Calendar.DATE, 1);
			entry.setText(text);
			entry.setWidth(Math.round((DISPLAY_WIDTH) / (float)FRAMES_PER_ROW));
			bottomBar.addView(entry);
		}

	}

	
	@Override
	public void goPrev() {
		int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedDate.add(Calendar.MONTH, -1);
		firstShownDate = updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedDate);
		}
		setDaysTitles();
		updateEventLists();

	}

	
	@Override
	public void goNext() {
		int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedDate.add(Calendar.MONTH, 1);
		firstShownDate = updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedDate);
		}
		
		setDaysTitles();
		updateEventLists();

	}
	

	@Override
	public void setupView() {
		miniMonthTable = (TableLayout) findViewById(R.id.agenda_table);
		paintTable(selectedDate);
		setDaysTitles();
		updateEventLists();
	}
	
	private void paintTable(Calendar date) {
		miniMonthTable.removeAllViews();
		daysList = new ArrayList<AgendaFrame>();
		
		
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
		daysList.add(new AgendaFrame(dayFrame, getContext(), false));
		
	}
	
	private void setDaysTitles() {
		
		Calendar tmp = (Calendar) firstShownDate.clone();
		int firstDayOfWeek = tmp.getFirstDayOfWeek();
		
		for (AgendaFrame frame : daysList){
			TextView dayTitle = (TextView) frame.getDayContainer().findViewById(R.id.agenda_day_title);	
			TextView weekNum = (TextView) frame.getDayContainer().findViewById(R.id.agenda_week_title);
			String title = "" + tmp.get(Calendar.DATE);
			dayTitle.setText(title);
			
			if (Utils.isToday(tmp)){
				dayTitle.setBackgroundColor(getResources().getColor(R.color.darker_gray));
				weekNum.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			} else{
				dayTitle.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
				weekNum.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			}
			
			if (tmp.get(Calendar.DAY_OF_WEEK) == firstDayOfWeek){					
				weekNum.setVisibility(VISIBLE);
				weekNum.setText("" + tmp.get(Calendar.WEEK_OF_MONTH));
			}else {
				frame.getDayContainer().findViewById(R.id.agenda_week_title).setVisibility(GONE);
			}
			
			
			
			tmp.add(Calendar.DATE, 1);
		}
		
	}
	@Override
	protected void updateEventLists() {
		Calendar tmp = (Calendar) firstShownDate.clone();
		
		for (AgendaFrame frame : daysList){	
			if (tmp.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)){
				frame.setEventList(Data.getEventByDate(tmp));
			}else {
				frame.setEventList(new ArrayList<Event>());
			}
			frame.UpdateList();
			tmp.add(Calendar.DATE, 1);
		}
	}
	
	private Calendar updateShownDate() {
		Calendar tmp = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfMonth(tmp);
		Utils.setCalendarToFirstDayOfWeek(tmp);
		return tmp;
	}
	@Override
	public Calendar getDateToResume() {
		return selectedDate;
	}
	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		FRAMES_PER_ROW = selectedDate.getMaximum(Calendar.DAY_OF_WEEK);
		TABLE_ROWS_COUNT = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		firstShownDate = updateShownDate();
		
	}
	
	

}
