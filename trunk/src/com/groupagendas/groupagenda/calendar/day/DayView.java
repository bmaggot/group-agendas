package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;


import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;


public class DayView extends AbstractCalendarView {
	
	DayInstance selectedDay;
	
	boolean am_pmEnabled;


	String[] WeekDayNames;
	String[] MonthNames;
	String[] HourNames;
	

	private HourEventsPanel hourEventsPanel;
	private ListView allDayEventsPanel;
	private LinearLayout hourList;
	private AllDayEventsAdapter allDayEventAdapter;
	

	public DayView(Context context) {
		this(context, null);
	}

	public DayView(Context context, AttributeSet attrs) {

		super(context, attrs);
		
		this.selectedDay = new DayInstance(context, ((NavbarActivity)context).getSelectedDate());	
		
		am_pmEnabled =  DataManagement.getInstance(getContext()).getAccount().setting_ampm != 0;
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);
		if(am_pmEnabled){
			HourNames = getResources().getStringArray(R.array.hour_names_am_pm);
		}
		else{
			HourNames = getResources().getStringArray(R.array.hour_names);
		}

		allDayEventAdapter = new AllDayEventsAdapter(getContext(), new ArrayList<Event>());

	}


	public void init(Calendar selectedDate) {
		
		super.init();
		
//		setupViewItems();
//		drawHourList();
//		updateEventLists();
//		scrollHourPanel();
		
	}
	

	private void scrollHourPanel() {
		final float hour;
		
		if (selectedDay.isToday()){
			Calendar tmp = Calendar.getInstance();
			hour = tmp.get(Calendar.HOUR_OF_DAY) + tmp.get(Calendar.MINUTE)/60.0f;
		}else{
			hour = DEFAULT_TIME_TO_SCROLL;
		}
		
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
			label.setHeight(Math.round(hourLineHeightDP*densityFactor));
			label.setWidth(LayoutParams.FILL_PARENT);
			hourList.addView(label);
		}
	}

	public void drawHourEvents() {
//	todo add new event when clicked on empty space
		hourEventsPanel.removeAllViews();
		if (selectedDay.hasHourEvents()){
			ArrayList<Event> hourEventsList = selectedDay.getHourEvents();
			HourEventsTimetable hourEventsTimetable = selectedDay.getHourEventsTimeTable();
			
			for (int i = 0; i < hourEventsList.size(); i++){
				Event e = hourEventsList.get(i);
				int neighbourId = hourEventsTimetable.getNeighbourId(e);
				int divider = hourEventsTimetable.getWidthDivider(e);
				drawEvent(e, divider, neighbourId);
			}
		
		}
		
			
	}


	private void drawEvent(final Event event, int divider, int neighbourId) {

		int dispWidth = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getWidth();
		int panelWidth =  Math.round(0.9f * dispWidth - 1);
		
		
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int lineHeight = (int) (hourLineHeightDP * scale + 0.5f);
//		int oneDP = (int) (1 * scale + 0.5f);
 
	
		HourEventView eventFrame = new HourEventView(getContext(), event, this.am_pmEnabled);
		
		float startTimeHours = 0; 
		float endTimeHours = 24;
		
		if (selectedDay.getSelectedDate().before(event.startCalendar)) {
			startTimeHours = event.startCalendar.get(Calendar.HOUR_OF_DAY);
			float minutes = event.startCalendar.get(Calendar.MINUTE);
			startTimeHours += minutes / 60;
		} else eventFrame.setStartTime(selectedDay.getSelectedDate()); //set event start hour 0:00 to show
		
		if (selectedDay.getSelectedDate().get(Calendar.DAY_OF_MONTH) == event.endCalendar.get(Calendar.DAY_OF_MONTH)){
			if (selectedDay.getSelectedDate().get(Calendar.MONTH) == event.endCalendar.get(Calendar.MONTH)){
				endTimeHours = event.endCalendar.get(Calendar.HOUR_OF_DAY);
				float minutes = event.endCalendar.get(Calendar.MINUTE);
				endTimeHours += minutes / 60;
									
				}			
		}
		
		float duration = endTimeHours - startTimeHours ;
		
//		if event lasts less than one hour, it's resized to half of hour pane to make text visible at all :)
		if (duration <= 0.5f) duration = 0.55f;   
		
	
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(panelWidth/divider, (int)(lineHeight * duration));	
	
		params.topMargin = (int) (lineHeight * startTimeHours);
		
		if (neighbourId != 0) {
			params.addRule(RelativeLayout.RIGHT_OF, neighbourId);
		}
		
		hourEventsPanel.addView(eventFrame, params);
		
	}

	private void updateEventLists() {
		allDayEventAdapter.setList(selectedDay.getAllDayEvents());
		allDayEventAdapter.notifyDataSetChanged();

		// VERY CIOTKIJ HARDCORD. Za Yeah! Peace!
		LinearLayout allDayEventsContainer = (LinearLayout) findViewById(R.id.allday_container);
		if (selectedDay.getAllDayEvents().size() < 10) {
			if (selectedDay.getAllDayEvents().size() == 0) {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,  Math.round(18*densityFactor)); 
				allDayEventsContainer.setLayoutParams(layoutParams);
			} else {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, Math.round(selectedDay.getAllDayEvents().size()*18*densityFactor)); 
				allDayEventsContainer.setLayoutParams(layoutParams);
			}
		} else {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, Math.round(180*densityFactor)); 
			allDayEventsContainer.setLayoutParams(layoutParams);
		}

		drawHourEvents(); // Drawing hour-long events
	}

//	public void setupViewItems() {
//		prevButton = (ImageButton) findViewById(R.id.prevDay);
//		nextButton = (ImageButton) findViewById(R.id.nextDay);
//		
//		prevButtonBounds = new Rect();
//		nextDayButtonBounds = new Rect();

		


//		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
//		updateTopPanelTitle(selectedDay.getSelectedDate());
			
//	}
	
	public void goPrev(){
		selectedDay.goPrev();
		updateTopPanelTitle(selectedDay.getSelectedDate());
		updateEventLists();
	}
	
	public void goNext(){
		selectedDay.goNext();
		updateTopPanelTitle(selectedDay.getSelectedDate());
		updateEventLists();
	}

	private void updateTopPanelTitle(Calendar selectedDate) {
		String title = WeekDayNames[selectedDate.get(Calendar.DAY_OF_WEEK) - 1];
		title += ", ";
		title += MonthNames[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.DAY_OF_MONTH);
		title += ", ";
		title += selectedDate.get(Calendar.YEAR);

		this.getTopPanelTitle().setText(title);
	}



	@Override
	protected void setTopPanelTitle() {
		updateTopPanelTitle(selectedDay.getSelectedDate());
	}

	@Override
	public void setupView() {
		
		allDayEventsPanel = (ListView) findViewById(R.id.allday_events);
		allDayEventsPanel.setAdapter(allDayEventAdapter);
		
//		init column with hour titles
		hourList = (LinearLayout) findViewById(R.id.hour_list);
		hourList.setClickable(false);
		drawHourList();
		
		//init hour event panel
		hourEventsPanel = (HourEventsPanel) findViewById(R.id.hour_events);
		hourEventsPanel.setSwipeGestureDetector(new GestureDetector(new HourEventsPanelMotionListener(this, selectedDay.getSelectedDate())));
		hourEventsPanel.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (hourEventsPanel.getSwipeGestureDetector().onTouchEvent(event)) {
				     return false;
				    } else {
				     return true;
				    }
			}
		});
		
		updateEventLists();
		scrollHourPanel();	
	}



}