package com.groupagendas.groupagenda.calendar.week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;


import com.groupagendas.groupagenda.calendar.AbstractCalendarViewWithAllDayAndHourEvents;
import com.groupagendas.groupagenda.calendar.day.DayInstance;
import com.groupagendas.groupagenda.calendar.day.HourEventsPanelMotionListener;
import com.groupagendas.groupagenda.calendar.day.HourEventsTimetable;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;


public class WeekView extends AbstractCalendarViewWithAllDayAndHourEvents {
	
	private static final float DEFAULT_TIME_TO_SCROLL = 7.5f; //DEFAULT HOUR TO SCROLL. 7.5f = 7:30
	public static final int hourLineHeightDP = 23;  //HEIGHT OF ONE HOUR LINE IN DIP

	WeekInstance daysShown;
	

	private LinearLayout hourEventsPanel;
	private LinearLayout allDayEventsPanel;
	private LinearLayout hourList;
	

	public WeekView(Context context) {
		this(context, null);
	}

	public WeekView(Context context, AttributeSet attrs) {

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
	//adjusts top panel title accordingly to shownDate field in WeekInstance class
	@Override
	protected void setTopPanel() {
		Calendar selectedDate = daysShown.getShownDate();
		
		String title = getResources().getString(R.string.week);
		title += " ";
		title += selectedDate.get(Calendar.WEEK_OF_YEAR);
		title += ", ";
		title += MonthNames[selectedDate.get(Calendar.MONTH)];
		title += " ";
		title += selectedDate.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);
		
		LinearLayout bottomBar = (LinearLayout)getTopPanelBottomLine().getChildAt(0);
		bottomBar.removeAllViews();
		
		TextView entry = new TextView(getContext());
//		Add empty space
		entry.setWidth(Math.round(HOUR_COLUMN_WIDTH));
		bottomBar.addView(entry);
		
		Calendar tmp = (Calendar) daysShown.getShownDate().clone();
//		add view for every day
		for (int i = 0; i < daysShown.getDaysToShow(); i++){
			entry = (TextView) mInflater.inflate(R.layout.calendar_top_bar_bottomline_entry, null);
			
			String text = (tmp.get(Calendar.DATE) + " " + WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1]);
			tmp.add(Calendar.DATE, 1);
			entry.setText(text);
			entry.setWidth(Math.round(EVENTS_COLUMN_WIDTH / (float)daysShown.getDaysToShow()));
			bottomBar.addView(entry);
		}
		
		
		
	}


	@Override
	public void setupView() {
		
		allDayEventsPanel = (LinearLayout) findViewById(R.id.allday_events);
		allDayEventsPanel.setOrientation(LinearLayout.HORIZONTAL);
		
		hourEventsPanel = (LinearLayout) findViewById(R.id.hour_events);
		
		hourList = (LinearLayout) findViewById(R.id.hour_list);
		hourList.setClickable(false);
		
//		setting up panels frames
		View child;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.weight = 1;
		
//		adding day frames and listeners from first to last-1 of shown days
		for (int i = 0; i < daysShown.getDaysToShow() - 1; i++){
//			Add events and separator to AlldayPanel
			child = createNewAllDayEventFrame();			
			allDayEventsPanel.addView(child, params);
			allDayEventsPanel.addView(new VerticalDaysSeparator(getContext()));
//			Add events and separator to HoureventsPanel
			child = createNewHourEventFrame();
			hourEventsPanel.addView(child, params);
			hourEventsPanel.addView(new VerticalDaysSeparator(getContext()));
		}
		
//		add last shown day without separators to both panels
		child = createNewAllDayEventFrame();
		allDayEventsPanel.addView(child, params);
		child = createNewHourEventFrame();
		hourEventsPanel.addView(child, params);
		
//		initialize column with hour titles		
		drawHourList();
		
		addMotionListeners();
		updateEventLists();
		scrollHourPanel();	
	}
	

	private void drawHourList() {
		

		/*
		 * 
		 * A-CHU-JE-NAI, DACHUJA ZAJABYS.
		 * 
		 */
		
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
//	TODO
		//Add swipe listener for hourEvent panel
	
	allDayEventsPanel.setOnTouchListener(createListener(swipeGestureDetector));
	hourEventsPanel.setOnTouchListener(createListener(swipeGestureDetector));
	for (int i = 0; i < daysShown.getDaysToShow(); i++){
		RelativeLayout child = (RelativeLayout) hourEventsPanel.getChildAt(i * 2);
		Calendar date = (Calendar) daysShown.getShownDate().clone();
		date.add(Calendar.DATE, i);
//		HourEventsPanelMotionListener listener = new HourEventsPanelMotionListener(this, date);
//		listener.setListenToSwipe(false); //we do not need to listen to swipe inside day cells
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
				
				if (!event.color.equalsIgnoreCase("null")){
					sd.setColor(Color.parseColor("#BF" + event.color));
					sd.setStroke(1, Color.parseColor("#" + event.color));
				}else {
					sd.setColor(getContext().getResources().getColor(R.color.defaultAllDayEventColor));
				}	
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
		this.daysShown = new WeekInstance(getContext(), initializationDate);
		
	}

	@Override
	public Calendar getDateToResume() {
		return daysShown.getShownDate();
	}

	


}