package com.groupagendas.groupagenda.calendar.month;


import java.util.ArrayList;
import java.util.Calendar;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.adapters.MonthAdapter;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;

public class MonthView extends AbstractCalendarView {
	
	private static final int WEEK_TITLE_WIDTH_DP = 0;
	private final int TABLE_ROW_HEIGHT = Math.round(50 * densityFactor);
	private Calendar firstShownDate;
	ArrayList<MonthDayFrame> daysList = new ArrayList<MonthDayFrame>();
	private TableLayout monthTable;
	private ListView eventsList;
	private MonthAdapter eventsAdapter;


    public MonthView(Context context) {
        this(context, null);

    }

    public MonthView(Context context, AttributeSet attrs) {
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
		
		TextView entry = new TextView(getContext());
//		Add week number title
		int weekTitleWidthPx = Math.round(WEEK_TITLE_WIDTH_DP * densityFactor);
		entry.setWidth(weekTitleWidthPx);
		entry.setText(R.string.week_title);
		bottomBar.addView(entry);
		
		Calendar tmp = (Calendar) firstShownDate.clone();
//		add view for every day
		int daysPerWeek =  firstShownDate.getActualMaximum(Calendar.DAY_OF_WEEK);
		for (int i = 0; i < daysPerWeek; i++){
			entry = (TextView) mInflater.inflate(R.layout.calendar_top_bar_bottomline_entry, null);
			
			String text = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1];
			tmp.add(Calendar.DATE, 1);
			entry.setText(text);
			entry.setWidth(Math.round((DISPLAY_WIDTH - weekTitleWidthPx) / (float)daysPerWeek));
			bottomBar.addView(entry);
		}
	}

	@Override
	public void goPrev() {
		int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedDate.add(Calendar.MONTH, -1);
		updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedDate);
		}
		setDayFrames();
		updateEventLists();
		
	}

	@Override
	public void goNext() {
		int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedDate.add(Calendar.MONTH, 1);
		updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedDate);
		}
		
		setDayFrames();
		updateEventLists();
		
	}

	@Override
	public void setupView() {
		monthTable = (TableLayout) findViewById(R.id.month_table);
		eventsAdapter = new MonthAdapter(getContext(), null, am_pmEnabled);
		eventsList = (ListView) findViewById(R.id.month_list);
		eventsList.setAdapter(eventsAdapter);
		paintTable(selectedDate);
		setDayFrames();
		updateEventLists();
		
	}

	@Override
	protected void updateEventLists() {
		eventsAdapter.setList(Data.getEventByDate(selectedDate));
		eventsAdapter.notifyDataSetChanged();
		
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		firstShownDate = updateShownDate();
		
	}

	@Override
	public Calendar getDateToResume() {
		return selectedDate;
	}
	private void setDayFrames() {
		Calendar tmp = (Calendar) firstShownDate.clone();		
		for (MonthDayFrame frame : daysList){
			String title = "" + tmp.get(Calendar.DATE);
			frame.setDayTitle(title);
			
			ArrayList<String> bubbleColorsArray = new ArrayList<String>();
			for (Event e : Data.getEventByDate(tmp)){
				bubbleColorsArray.add(e.color);
			}
			
			frame.DrawColourBubbles(bubbleColorsArray);
			tmp.add(Calendar.DATE, 1);
		}
		
	}

	private void paintTable(Calendar date) {
		monthTable.removeAllViews();
		int FRAMES_PER_ROW = date.getMaximum(Calendar.DAY_OF_WEEK);
		int TABLE_ROWS_COUNT = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		
		
//	TODO	add GESTURE LISTENER
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);		
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
		        VIEW_WIDTH / FRAMES_PER_ROW,
		        TABLE_ROW_HEIGHT, //TODO leave some space for list view
		        1.0f);		

//		Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT; i++){
			row = (TableRow) mInflater.inflate(R.layout.calendar_month_row, null);
			for (int j = 0; j < FRAMES_PER_ROW; j++)
			addDay(row, cellLp);
			monthTable.addView(row, rowLp);
		}		
		
	}

	private void addDay(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
		MonthDayFrame dayFrame = (MonthDayFrame) mInflater.inflate(R.layout.calendar_month_day_container, null);
			dayFrame.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				int clickedDayPos = daysList.indexOf(v);
				System.out.println("clickedDayid: " + clickedDayPos);
				Calendar clickedDate = (Calendar) firstShownDate.clone();
				clickedDate.add(Calendar.DATE, clickedDayPos);
				
				int LastMonthWeeksCount = 0;
				
				if (!Utils.isSameDay(selectedDate, clickedDate)) {
					boolean monthChanged = false;
					if (clickedDate.get(Calendar.MONTH) != selectedDate
							.get(Calendar.MONTH)) {
						monthChanged = true;
						LastMonthWeeksCount = selectedDate
								.getActualMaximum(Calendar.WEEK_OF_MONTH);
					}

					selectedDate = clickedDate;
					updateShownDate();
					
					
					if (monthChanged) {
						setTopPanel();
						if (LastMonthWeeksCount != selectedDate
								.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
							paintTable(selectedDate);
						}
						setDayFrames();
					}

					updateEventLists();
						
					}
				}
			});
			
			
			row.addView(dayFrame, cellLp);
			daysList.add(dayFrame);
	}

	private Calendar updateShownDate() {
		Calendar tmp = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfMonth(tmp);
		Utils.setCalendarToFirstDayOfWeek(tmp);
		return tmp;
	}
}