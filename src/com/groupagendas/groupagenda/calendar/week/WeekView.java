package com.groupagendas.groupagenda.calendar.week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;


import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.AbstractCalendarViewWithAllDayAndHourEvents;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.calendar.day.DayInstance;
import com.groupagendas.groupagenda.calendar.day.HourEventsPanel;
import com.groupagendas.groupagenda.calendar.day.HourEventsPanelMotionListener;
import com.groupagendas.groupagenda.calendar.day.HourEventsTimetable;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;


public class WeekView extends AbstractCalendarViewWithAllDayAndHourEvents {
	
	private static final float DEFAULT_TIME_TO_SCROLL = 7.5f; //DEFAULT HOUR TO SCROLL. 7.5f = 7:30
	public static final int hourLineHeightDP = 23;  //HEIGHT OF ONE HOUR LINE IN DIP

	WeekInstance daysShown;
	
	
	
	boolean am_pmEnabled;
//

	

	String[] WeekDayNames;
	String[] MonthNames;
	String[] HourNames;
	
	

	private LinearLayout hourEventsPanel;
	private LinearLayout allDayEventsPanel;
	private LinearLayout hourList;
	

	public WeekView(Context context) {
		this(context, null);
	}

	public WeekView(Context context, AttributeSet attrs) {

		super(context, attrs);
		am_pmEnabled =  DataManagement.getInstance(getContext()).getAccount().setting_ampm != 0;
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);
		if(am_pmEnabled){
			HourNames = getResources().getStringArray(R.array.hour_names_am_pm);
		}
		else{
			HourNames = getResources().getStringArray(R.array.hour_names);
		}
		
		this.daysShown = new WeekInstance(context, ((NavbarActivity)context).getSelectedDate());
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
		
		LinearLayout bottomBar = getTopPanelBottomLine();
		bottomBar.removeAllViews();
		TextView entry = new TextView(getContext());
		entry.setWidth(Math.round(HOUR_COLUMN_WIDTH));
		bottomBar.addView(entry);
		for (int i = 0; i < daysShown.getDaysToShow(); i++){
			entry = new TextView(getContext());
//			TODO SUSETTINTI BOTOM LINE
			
			entry.setText("day " + i);
//			entry.setTextAppearance(getContext(), R.style.calendarTopbarBottomline);
			entry.setWidth(Math.round(EVENTS_COLUMN_WIDTH / (float)daysShown.getDaysToShow()));
			entry.setBackgroundColor(Color.RED);
			entry.setHeight(LayoutParams.WRAP_CONTENT);
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
		
		addSwipeListeners();
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
	private void addSwipeListeners() {
		// TODO Auto-generated method stub
//		hourEventsPanel.setSwipeGestureDetector(new GestureDetector(new HourEventsPanelMotionListener(this, selectedDay.getSelectedDate())));
//	hourEventsPanel.setOnTouchListener(new OnTouchListener() {
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			if (hourEventsPanel.getSwipeGestureDetector().onTouchEvent(event)) {
//			     return false;
//			    } else {
//			     return true;
//			    }
//		}
//	});
		
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


}