package com.groupagendas.groupagenda.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.day.DayInstance;
import com.groupagendas.groupagenda.calendar.day.HourEventView;
import com.groupagendas.groupagenda.calendar.day.HourEventsTimetable;
import com.groupagendas.groupagenda.calendar.week.VerticalDaysSeparator;
import com.groupagendas.groupagenda.calendar.week.WeekDayTouchListener;
import com.groupagendas.groupagenda.calendar.week.WeekInstance;
import com.groupagendas.groupagenda.events.Event;


public class DayWeekView extends AbstractCalendarView {
	
	public DayWeekView(Context context) {
		this(context, null);
	}

	public DayWeekView(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		WeekDayNamesShort = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);
	}

	
	protected final int EVENTS_COLUMN_WIDTH =  Math.round(0.9f * VIEW_WIDTH - 1);
	protected final int HOUR_COLUMN_WIDTH =  VIEW_WIDTH - EVENTS_COLUMN_WIDTH;
	public final static int DEFAULT_DAYS_SHOWN = 7;
	public final static int MAX_DAYS_SHOWN = 7;
	private static final int MIN_DAYS_SHOWN = 1;
	
	public static final float DEFAULT_TIME_TO_SCROLL = 7.5f; //DEFAULT HOUR TO SCROLL. 7.5f = 7:30
	public static final int hourLineHeightDP = 23;  //HEIGHT OF ONE HOUR LINE IN DIP for day and week view
	public static final int allDayLineHeightDP = 18;
	
	private float deltaX;
	private boolean deltaSet = false;
	
	protected static int daysToShow;
	protected boolean showHourEventsIcon = false;
	protected WeekInstance daysShown;

	protected LinearLayout hourEventsPanel;
	protected LinearLayout allDayEventsPanel;
	protected LinearLayout hourList;
	

	protected void setAllDayEventsPanelHeight(int allDayEventsCount) {
		LinearLayout allDayEventsContainer = (LinearLayout) findViewById(R.id.allday_container);		
		int allDayEventRowsNumber = 1;
		 
		
		if (allDayEventsCount > 0){
			if (allDayEventsCount <= 10) allDayEventRowsNumber = allDayEventsCount;
			else allDayEventRowsNumber = 10;
		}
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,  Math.round(allDayEventRowsNumber * allDayLineHeightDP*densityFactor)); 
		allDayEventsContainer.setLayoutParams(layoutParams);
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @return draws specific hour event in specified RelativeLayout
	 * @param event event that will be drawn
	 * @param divider integer that shows how much events share same row, so this event's width should be divided by that number
	 * @param neighbourId id of other view that this event will be displayed relative to
	 * @param container RelativeLayout that holds all events
	 * @param containerWidth
	 * @param day DayInstance that holds this day events data
	 */
	
	protected final void drawHourEvent(final Event event, int divider, int neighbourId, RelativeLayout container, int containerWidth, DayInstance day) {
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int lineHeight = (int) (hourLineHeightDP * scale + 0.5f);
 
	
		HourEventView eventFrame = new HourEventView(getContext(), event, this.am_pmEnabled, this.showHourEventsIcon);
		
		float startTimeHours = 0; 
		float endTimeHours = 24;
		
		if (day.getSelectedDate().before(event.startCalendar)) {
			startTimeHours = event.startCalendar.get(Calendar.HOUR_OF_DAY);
			float minutes = event.startCalendar.get(Calendar.MINUTE);
			startTimeHours += minutes / 60;
		} else eventFrame.setStartTime(day.getSelectedDate()); //set event start hour 0:00 to show
		
		if (day.getSelectedDate().get(Calendar.DAY_OF_MONTH) == event.endCalendar.get(Calendar.DAY_OF_MONTH)){
			if (day.getSelectedDate().get(Calendar.MONTH) == event.endCalendar.get(Calendar.MONTH)){
				endTimeHours = event.endCalendar.get(Calendar.HOUR_OF_DAY);
				float minutes = event.endCalendar.get(Calendar.MINUTE);
				endTimeHours += minutes / 60;
									
				}			
		}
		
		float duration = endTimeHours - startTimeHours ;
		
//		if event lasts less than one hour, it's resized to half of hour pane to make text visible at all :)
		if (duration <= 0.5f) duration = 0.55f;   
		
	
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(containerWidth/divider, (int)(lineHeight * duration));	
	
		params.topMargin = (int) (lineHeight * startTimeHours);
		
		if (neighbourId != 0) {
			params.addRule(RelativeLayout.RIGHT_OF, neighbourId);
		}
		container.addView(eventFrame, params);	
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		int action = event.getAction() & MotionEvent.ACTION_MASK;

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			break;
		}

		case MotionEvent.ACTION_POINTER_DOWN: {
			if (event.getPointerCount() > 1) {
				float x1 = event.getX(0);
				float x2 = event.getX(1);


				if (!deltaSet) {
					deltaX = Math.abs(x2 - x1);
					deltaSet = true;
				}
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			if (event.getPointerCount() > 1) {
				float x1 = event.getX(0);
				float x2 = event.getX(1);

				float deltaX = 1;
				if (deltaSet) {
					deltaX = Math.abs(x2 - x1);
					deltaSet = false;

					float scalling = this.deltaX / deltaX;
					
					int daysToShow;
					if (deltaX > this.deltaX){
						System.out.println("ARTINAM");
						daysToShow = (int) (scalling * this.daysToShow);
					}else{
						System.out.println("TOLINAM");
						daysToShow = (int) (scalling * this.daysToShow);
					}
					this.deltaX = 0;
					if (daysToShow > MAX_DAYS_SHOWN) daysToShow = MAX_DAYS_SHOWN;
					if (daysToShow < MIN_DAYS_SHOWN) daysToShow = MIN_DAYS_SHOWN;
				
					init(getDateToResume(), daysToShow);
				}
			}

			break;
		}
		case MotionEvent.ACTION_UP: {
			break;
		}
		}

		return false;
	}
/**
 * @author justinas.marcinka@gmail.com
 * @param initDate - Date that will be the first shown day, if not all days of week are displayed.
 * If all days of week are displayed, first shown day will be the first day of week of this date.
 * @param daysToShow indicates how much days should be shown. If 0 is given, it indicates that it is week view and number of shown days will be indicated by DayWeekView.daysToShow field
 * NOTE: if you want to have day View, externally set DayWeekView.daysToShow field to 1, or to DayWeekView.DEFAULT_DAYS_SHOWN for week view accordingly.
 */
	public synchronized void init(Calendar initDate, int daysToShow) {
		if (daysToShow != 0) { //if there is custom number of days to be shown
			setShownDaysCount(daysToShow);
		} else setShownDaysCount(DayWeekView.daysToShow); // else we use value from static field
		
		
		super.init(initDate);
		
	}
	


	private void setShownDaysCount(int daysToShow) {
		DayWeekView.daysToShow = daysToShow;
		showHourEventsIcon = (daysToShow == 1);	
}

	@Override
	protected void instantiateTopPanelBottomLine() {
		LinearLayout calendarTopPanelBottomLine = new LinearLayout(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		calendarTopPanelBottomLine.setOrientation(LinearLayout.HORIZONTAL);
		calendarTopPanelBottomLine.setLayoutParams(params);
		getTopPanelBottomLine().addView(calendarTopPanelBottomLine);	
	}
	
		//adjusts top panel title accordingly to shownDate field in WeekInstance class
		@Override
		protected void setTopPanel() {
			Calendar selectedDate = daysShown.getShownDate();
			String title;
			LinearLayout bottomBar = (LinearLayout)getTopPanelBottomLine().getChildAt(0);
			bottomBar.removeAllViews();
			
			//If there is shown only one day, title must be different
			if (daysToShow == 1){
				title = WeekDayNames[selectedDate.get(Calendar.DAY_OF_WEEK) - 1];
				title += ", ";
				title += MonthNames[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.DAY_OF_MONTH);
				title += ", ";
				title += selectedDate.get(Calendar.YEAR);
			}else{
				title = getResources().getString(R.string.week);
				title += " ";
				title += selectedDate.get(Calendar.WEEK_OF_YEAR);
				title += ", ";
				title += MonthNames[selectedDate.get(Calendar.MONTH)];
				title += " ";
				title += selectedDate.get(Calendar.YEAR);
				
				// Setting bottom bar
				TextView entry = new TextView(getContext());
//				Add empty space
				entry.setWidth(Math.round(HOUR_COLUMN_WIDTH));
				bottomBar.addView(entry);
				
				Calendar tmp = (Calendar) daysShown.getShownDate().clone();
//				add view for every day
				for (int i = 0; i < daysShown.getDaysToShow(); i++){
					entry = (TextView) mInflater.inflate(R.layout.calendar_top_bar_bottomline_entry, null);
					
					String text = (tmp.get(Calendar.DATE) + " " + WeekDayNamesShort[tmp.get(Calendar.DAY_OF_WEEK) - 1]);
					tmp.add(Calendar.DATE, 1);
					entry.setText(text);
					entry.setWidth(Math.round(EVENTS_COLUMN_WIDTH / (float)daysShown.getDaysToShow()));
					bottomBar.addView(entry);
				}
			}
			
			this.getTopPanelTitle().setText(title);	
		}
		
		@Override
		public void setupView() {
			
			allDayEventsPanel = (LinearLayout) findViewById(R.id.allday_events);
			allDayEventsPanel.setOrientation(LinearLayout.HORIZONTAL);
			allDayEventsPanel.removeAllViews();
			
			hourEventsPanel = (LinearLayout) findViewById(R.id.hour_events);
			hourEventsPanel.removeAllViews();
			
			hourList = (LinearLayout) findViewById(R.id.hour_list);
			hourList.removeAllViews();
			hourList.setClickable(false);
			
//			setting up panels frames
			View child;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			params.weight = 1;
			
//			adding day frames and listeners from first to last-1 of shown days
			for (int i = 0; i < daysShown.getDaysToShow() - 1; i++){
//				Add events and separator to AlldayPanel
				child = createNewAllDayEventFrame();			
				allDayEventsPanel.addView(child, params);
				allDayEventsPanel.addView(new VerticalDaysSeparator(getContext()));
//				Add events and separator to HoureventsPanel
				child = createNewHourEventFrame();
				hourEventsPanel.addView(child, params);
				hourEventsPanel.addView(new VerticalDaysSeparator(getContext()));
			}
			
//			add last shown day without separators to both panels
			child = createNewAllDayEventFrame();
			allDayEventsPanel.addView(child, params);
			child = createNewHourEventFrame();
			hourEventsPanel.addView(child, params);
			
//			initialize column with hour titles		
			drawHourList();
			
			addMotionListeners();
			updateEventLists();
			scrollHourPanel();	
		}
		
		private void drawHourList() {			
			for (int i=0; i<24; i++) {
				TextView label = new TextView(getContext());
				label.setTextAppearance(getContext(), R.style.dayView_hourEvent_firstColumn_entryText);
				label.setText(HourNames[i]);
				label.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
				label.setHeight(Math.round(hourLineHeightDP * densityFactor));
				label.setWidth(LayoutParams.FILL_PARENT);
				hourList.addView(label);
			}
		}
		private void addMotionListeners() {

		allDayEventsPanel.setOnTouchListener(createListener(swipeGestureDetector));
		hourEventsPanel.setOnTouchListener(createListener(swipeGestureDetector));
		for (int i = 0; i < daysShown.getDaysToShow(); i++){
			RelativeLayout child = (RelativeLayout) hourEventsPanel.getChildAt(i * 2);
			Calendar date = (Calendar) daysShown.getShownDate().clone();
			date.add(Calendar.DATE, i);
			child.setOnTouchListener(createListener(new GestureDetector(new WeekDayTouchListener(this, date))));
		}
		
			
		}


		private LinearLayout createNewAllDayEventFrame() {
			LinearLayout child = new LinearLayout(getContext());		
			child.setOrientation(LinearLayout.VERTICAL);
			return child;
		}
		
		
		
		private RelativeLayout createNewHourEventFrame() {
			RelativeLayout child = new RelativeLayout(getContext());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			child.setLayoutParams(params);		
			return child;
		}

		@Override
		public void goPrev(){
			daysShown.prevPage();
			setTopPanel(); 
			updateEventLists();
		}
		
		@Override
		public void goNext(){
			daysShown.nextPage();
			setTopPanel();
			updateEventLists();
		}	
		private void scrollHourPanel() {
			final float hour = DEFAULT_TIME_TO_SCROLL;
			
			
			final ScrollView scrollPanel = (ScrollView)this.findViewById(R.id.calendar_day_view_hour_events_scroll);
			scrollPanel.setOnTouchListener(null);
			
			
			
			scrollPanel.post(new Runnable() {
			    @Override
			    public void run() {
			    	int y = (int) (hour * Math.round(hourLineHeightDP * densityFactor));
			        scrollPanel.scrollTo(0, y);
			    } 
			});	
		}

		@Override
		protected void updateEventLists() {
			setAllDayEventsPanelHeight(daysShown.getMaxAllDayEventsCount());
			for (int i = 0; i < daysShown.getDaysToShow(); i++){
				DayInstance day = daysShown.getDayInstance(i);
				
				LinearLayout AllDayContainer = (LinearLayout)allDayEventsPanel.getChildAt(i * 2);
				AllDayContainer.removeAllViews();
				drawAllDayEvents(AllDayContainer, day);
				
				RelativeLayout HourContainer = (RelativeLayout)hourEventsPanel.getChildAt(i * 2);
				HourContainer.removeAllViews();
				drawHourEvents(HourContainer, day);
			}
			
			
			
		}


		private void drawAllDayEvents(LinearLayout container, DayInstance day) {
			List<Event> events = day.getAllDayEvents();
			if (!events.isEmpty()){
				
				for (final Event event : events){
					View view = mInflater.inflate(R.layout.calendar_dayview_allday_listentry, null);
					TextView title = (TextView) view.findViewById(R.id.allday_eventtitle);
					title.setText(event.title);
					GradientDrawable sd = (GradientDrawable)getContext().getResources().getDrawable(R.drawable.calendar_dayview_secondcolumn_entrybackground);
					
//					if (!event.color.equalsIgnoreCase("null")){
						sd.setColor(Color.parseColor("#BF" + event.getColor()));
						sd.setStroke(1, Color.parseColor("#" + event.getColor()));
//					}else {
//						sd.setColor(getContext().getResources().getColor(R.color.defaultAllDayEventColor));
//					}	
					title.setBackgroundDrawable(sd);
					view.setOnClickListener(new EventActivityOnClickListener(getContext(), event));
					container.addView(view);
				}
			}




		}

		private void drawHourEvents(RelativeLayout container, DayInstance day) {
			int containerWidth = Math.round(EVENTS_COLUMN_WIDTH / (float)daysShown.getDaysToShow());
			
			if (day.hasHourEvents()){
				ArrayList<Event> hourEventsList = day.getHourEvents();
				HourEventsTimetable hourEventsTimetable = day.getHourEventsTimeTable();
				
				for (int i = 0; i < hourEventsList.size(); i++){
					Event e = hourEventsList.get(i);
					int neighbourId = hourEventsTimetable.getNeighbourId(e);
					int divider = hourEventsTimetable.getWidthDivider(e);
					drawHourEvent(e, divider, neighbourId, container, containerWidth, day);
				}
			
			}		
		}

		@Override
		protected void setupSelectedDate(Calendar initializationDate) {
			this.daysShown = new WeekInstance(getContext(), initializationDate, daysToShow);
			
		}

		@Override
		public Calendar getDateToResume() {	
//			DayWeekView.needsToResume = true;
			return daysShown.getShownDate();
		}

		public static int getDaysToShow() {
			return daysToShow;
		}
		
		public static void setDaysToShow(int daysToShow) {
			DayWeekView.daysToShow = daysToShow;
		}

	

	

}
