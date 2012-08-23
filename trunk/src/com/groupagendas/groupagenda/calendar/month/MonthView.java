package com.groupagendas.groupagenda.calendar.month;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
	private int FRAME_WIDTH;
	protected boolean redrawBubbles = true; //indicates whether to redraw color bubbles


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
		
		TextView entry = (TextView) mInflater.inflate(R.layout.calendar_top_bar_bottomline_entry, null);
		int weekTitleWidthPx = Math.round(WEEK_TITLE_WIDTH_DP * densityFactor);
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
		redrawBubbles = true;
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
		redrawBubbles = true;
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
		updateShownDate();
		
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
			frame.setToday(Utils.isToday(tmp));
			frame.setSelected(Utils.isSameDay(tmp, selectedDate));
			frame.setOtherMonth(selectedDate.get(Calendar.MONTH) != tmp.get(Calendar.MONTH));
			frame.refreshStyle();
			

			if(!frame.hasBubbles){				
				ArrayList<Event> eventColorsArray =  Data.getEventByDate(tmp);
				frame.DrawColourBubbles(eventColorsArray, FRAME_WIDTH);
			}
			
			tmp.add(Calendar.DATE, 1);
		}
		
	}

	private void paintTable(Calendar date) {
		monthTable.removeAllViews();
		daysList = new ArrayList<MonthDayFrame>();
		int FRAMES_PER_ROW = date.getMaximum(Calendar.DAY_OF_WEEK);
		int TABLE_ROWS_COUNT = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		FRAME_WIDTH = VIEW_WIDTH / FRAMES_PER_ROW;

		System.out.println("ROWS: " + TABLE_ROWS_COUNT);
		LinearLayout month_weeknumbers_container = (LinearLayout) findViewById(R.id.month_weeknumbers_container);
		month_weeknumbers_container.removeAllViews();
		
		
//	TODO	add GESTURE LISTENER
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);		
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
		        FRAME_WIDTH,
		        TABLE_ROW_HEIGHT, 
		        1.0f);		

//		Adding rows
		TableRow row;
		Calendar tmp = (Calendar) firstShownDate.clone();
		for (int i = 0; i < TABLE_ROWS_COUNT; i++){
			
			TextView weekNum = (TextView) mInflater.inflate(R.layout.calendar_month_week_container, null);
			weekNum.setText("" + tmp.get(Calendar.WEEK_OF_YEAR));
			weekNum.setHeight(TABLE_ROW_HEIGHT);
			weekNum.setBackgroundResource(R.drawable.calendar_month_day_inactive);
			month_weeknumbers_container.addView(weekNum);
			
			
			
			row = (TableRow) mInflater.inflate(R.layout.calendar_month_row, null);
			
			for (int j = 0; j < FRAMES_PER_ROW; j++){
				addDay(row, cellLp);
				tmp.add(Calendar.DATE, 1);
			}
			
			monthTable.addView(row, rowLp);
		}		
		
	}

	private void addDay(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
			MonthDayFrame dayFrame = (MonthDayFrame) mInflater.inflate(
				R.layout.calendar_month_day_container, null);
		
			row.addView(dayFrame, cellLp);			
			daysList.add(dayFrame);
			
			dayFrame.setOnClickListener(new OnClickListener() {
	
				

				@Override
				public void onClick(View v) {
					
				MonthDayFrame frame = (MonthDayFrame) v;
				int clickedDayPos = daysList.indexOf(frame);
				
				Calendar clickedDate = (Calendar) firstShownDate.clone();
				clickedDate.add(Calendar.DATE, clickedDayPos);
				
				
				
				int LastMonthWeeksCount = selectedDate
						.getActualMaximum(Calendar.WEEK_OF_MONTH);
				
				if (!frame.isSelected()) {
					
					selectedDate = clickedDate;
					updateShownDate();
					
					
					if (frame.isOtherMonth()) {
						
						redrawBubbles = true;
						setTopPanel();
						
						if (LastMonthWeeksCount != selectedDate
								.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
							paintTable(selectedDate);
						}
						
						
					}
					
					setDayFrames(); //TODO optimize: now all day frames are redrawn
					updateEventLists();

				}
			}
			});
			
			
			
	}

	private void updateShownDate() {
		firstShownDate = (Calendar) selectedDate.clone();
		
		Utils.setCalendarToFirstDayOfMonth(firstShownDate);
		Utils.setCalendarToFirstDayOfWeek(firstShownDate);
	
	}
}