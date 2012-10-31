package com.groupagendas.groupagenda.calendar.month;


import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
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
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.calendar.adapters.MonthAdapter;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
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
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
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
		eventsAdapter.setList(TreeMapUtils.getEventsFromTreemap(selectedDate, sortedEvents));
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
		new UpdateEventsInfoTask().execute();
		Calendar tmp = (Calendar) firstShownDate.clone();		
		for (MonthDayFrame frame : daysList){
			String title = "" + tmp.get(Calendar.DATE);
			
			frame.setDayTitle(title);
			
			MonthCellState state = MonthCellState.DEFAULT;
			if (selectedDate.get(Calendar.MONTH) != tmp.get(Calendar.MONTH)) {
				state = MonthCellState.OTHER_MONTH;
			} else {
				if (Utils.isToday(tmp))
					state = MonthCellState.TODAY;
				if (Utils.isSameDay(tmp, selectedDate))
					state = MonthCellState.SELECTED;
			}
			
			

			frame.setState(state);
			

//			if(!frame.hasBubbles){				
//				ArrayList<Event> eventColorsArray =  Utils.getEventByDate(tmp);
//				frame.DrawColourBubbles(eventColorsArray, FRAME_WIDTH);
//			}
			
			tmp.add(Calendar.DATE, 1);
		}
		
	}

	private void paintTable(Calendar date) {
		monthTable.removeAllViews();
		daysList = new ArrayList<MonthDayFrame>();
		int FRAMES_PER_ROW = date.getMaximum(Calendar.DAY_OF_WEEK);
		int TABLE_ROWS_COUNT = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		FRAME_WIDTH = VIEW_WIDTH / FRAMES_PER_ROW;

		LinearLayout month_weeknumbers_container = (LinearLayout) findViewById(R.id.month_weeknumbers_container);
		month_weeknumbers_container.removeAllViews();
		
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
	

	private class UpdateEventsInfoTask extends AsyncTask<Void, Integer, Void> {
		private Context context = MonthView.this.getContext();
		
		/**
		 * @author justinas.marcinka@gmail.com
		 * Returns event projection in: id, color, icon, title, start and end calendars. Other fields are not initialized
		 * @param date
		 * @return
		 */
		private ArrayList<Event>getEventProjectionsForDisplay(Calendar date){
			ArrayList<Event> list = new ArrayList<Event>();
			Cursor result = EventManagement.createEventProjectionByDateFromLocalDb(context, EventProjectionForDisplay, date, 0, EventManagement.TM_EVENTS_ON_GIVEN_MONTH, null, true);
			if (result.moveToFirst()) {
				while (!result.isAfterLast()) {
					Event eventProjection = new Event();
					eventProjection.setInternalID(result.getLong(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData._ID)));
					eventProjection.setEvent_id(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.E_ID)));
					eventProjection.setTitle(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TITLE)));
					eventProjection.setIcon(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.ICON)));
					eventProjection.setColor(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.COLOR)));
					eventProjection.setTextColor(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TEXT_COLOR)));
					eventProjection.setDisplayColor(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR)));
					String user_timezone = CalendarSettings.getTimeZone(context);
					long timeinMillis = result.getLong(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
					eventProjection.setStartCalendar(Utils.createCalendar(timeinMillis, user_timezone));
					timeinMillis = result.getLong(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
					eventProjection.setEndCalendar(Utils.createCalendar(timeinMillis, user_timezone));
					eventProjection.setStatus(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.STATUS)));
					String owner = result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.IS_OWNER));
					if (owner.equalsIgnoreCase("1")) {
						eventProjection.setIs_owner(true);
					} else {
						eventProjection.setIs_owner(false);
					}
					
					list.add(eventProjection);
					result.moveToNext();
				}
			}
			result.close();
			return list ;
			
		}

		@Override
		protected Void doInBackground(Void... params) {
			sortedEvents = TreeMapUtils.sortEvents(context, getEventProjectionsForDisplay(selectedDate));
			return null;
		}
		
		protected void onPostExecute(Void result) {
			updateEventLists();
			Calendar tmp = (Calendar) firstShownDate.clone();		
			for (MonthDayFrame frame : daysList){
				if(!frame.hasBubbles){		
					frame.DrawColourBubbles(TreeMapUtils.getEventsFromTreemap(tmp, sortedEvents), FRAME_WIDTH);
				}		
				tmp.add(Calendar.DATE, 1);
			}
			
		}


	}
	
}