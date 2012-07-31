package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;

import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Prefs;

public class DayView extends LinearLayout {
	
	DayInstance selectedDay;
	
	boolean am_pmEnabled;

	ImageButton prevDayButton;
	ImageButton nextDaybutton;
	Rect prevDayButtonBounds;
	Rect nextDayButtonBounds;
	TouchDelegate prevDayButtonDelegate;
	TouchDelegate nextDayButtonDelegate;
	TextView topPanelTitle;
	

	String[] WeekDayNames;
	String[] MonthNames;
	String[] HourNames;
	
	private final int hourLineHeightDP = 40;

	private HourEventsPanel hourEventsPanel;
	private ListView allDayEventsPanel;
	private LinearLayout hourList;
	private float densityFactor = getResources().getDisplayMetrics().density;
	private AllDayEventsAdapter allDayEventAdapter;

	public DayView(Context context) {
		this(context, null);
	}

	public DayView(Context context, AttributeSet attrs) {

		super(context, attrs);
		Prefs prefs = new Prefs(getContext());
		
		String am_pm = prefs.getValue(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM, "false");
		am_pmEnabled = Boolean.parseBoolean(am_pm);
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);
		if(am_pmEnabled){
			HourNames = getResources().getStringArray(R.array.hour_names_am_pm);
		}
		else{
			HourNames = getResources().getStringArray(R.array.hour_names);
		}
		
		selectedDay = new DayInstance(context);
		allDayEventAdapter = new AllDayEventsAdapter(getContext(), new ArrayList<Event>());
		

	}


	public void init() {
		setupViewItems();
		drawHourList();
		updateEventLists();
		scrollHourPanelto(this.getResources().getInteger(R.integer.hour_events_scrollPanePositionHour));
		
	}

	private void scrollHourPanelto(final int hour) {
		final ScrollView scrollPanel = (ScrollView)this.findViewById(R.id.calendar_day_view_hour_events_scroll);
		scrollPanel.post(new Runnable() {
		    @Override
		    public void run() {
		        scrollPanel.scrollTo(0, hour * Math.round(hourLineHeightDP * densityFactor));
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
	
		hourEventsPanel.removeAllViews();
	 
		if (selectedDay.hasHourEvents()){
			ArrayList<Event> hourEventsList = selectedDay.getHourEvents();
			HourEventsTimetable hourEventsTimetable = selectedDay.getHourEventsTimeTable();
			
			for (int i = 0; i < hourEventsList.size(); i++){
				Event e = hourEventsList.get(i);
				int neighbourId = hourEventsTimetable.getNeighbourId(e);
				int divider = hourEventsTimetable.getWidthDivider(e);
				System.out.println("Event: " + e.title + "divider " + divider);
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
		} else eventFrame.setStartTime(selectedDay.getSelectedDate()); //set event start hour 0:00
		
		int mul = 0; // half-hour multiplier
		if (selectedDay.getSelectedDate().get(Calendar.DAY_OF_MONTH) == event.endCalendar.get(Calendar.DAY_OF_MONTH)){
			if (selectedDay.getSelectedDate().get(Calendar.MONTH) == event.endCalendar.get(Calendar.MONTH)){
				endTimeHours = event.endCalendar.get(Calendar.HOUR_OF_DAY);
				float minutes = event.endCalendar.get(Calendar.MINUTE);
				endTimeHours += minutes / 60;
									
				}			
		}
		
		float duration = endTimeHours - startTimeHours ;
		
//		if event lasts less than one hour, it's resized to half of hour pane
		if (duration < 1) duration = 0.525f;   
		
		
	
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

	public void setupViewItems() {
		prevDayButton = (ImageButton) findViewById(R.id.prevDay);
		nextDaybutton = (ImageButton) findViewById(R.id.nextDay);
		
		prevDayButtonBounds = new Rect();
		nextDayButtonBounds = new Rect();

		hourEventsPanel = (HourEventsPanel) findViewById(R.id.hour_events);

		allDayEventsPanel = (ListView) findViewById(R.id.allday_events);
		allDayEventsPanel.setAdapter(allDayEventAdapter);
		
		hourList = (LinearLayout) findViewById(R.id.hour_list);
		hourList.setClickable(false);


		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
		updateTopPanelTitle(selectedDay.getSelectedDate());
		
		prevDayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedDay.goPrev();
				updateTopPanelTitle(selectedDay.getSelectedDate());
				updateEventLists();
			}
		});
		
		nextDaybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedDay.goNext();
				updateTopPanelTitle(selectedDay.getSelectedDate());
				updateEventLists();

			}
		});
		
		setupDelegates();
	}

	private void updateTopPanelTitle(Calendar selectedDate) {
		String title = WeekDayNames[selectedDate.get(Calendar.DAY_OF_WEEK) - 1];
		title += ", ";
		title += MonthNames[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.DAY_OF_MONTH);
		title += ", ";
		title += selectedDate.get(Calendar.YEAR);

		topPanelTitle.setText(title);
	}

	private void setupDelegates() {
		int[] tmpCoords = new int[2];
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		View calNavbar = (View) findViewById(R.id.calendar_navbar);
		calNavbar.getLocationOnScreen(tmpCoords);
		prevDayButton.getHitRect(prevDayButtonBounds);
		prevDayButtonBounds.right = tmpCoords[0]+50;
		prevDayButtonBounds.left = tmpCoords[0];
		prevDayButtonBounds.top = tmpCoords[1];
		prevDayButtonBounds.bottom = tmpCoords[1]+50;
		prevDayButtonDelegate = new TouchDelegate(prevDayButtonBounds, prevDayButton);
		
		nextDaybutton.getHitRect(nextDayButtonBounds);
		nextDayButtonBounds.right = tmpCoords[0]+screenWidth;
		nextDayButtonBounds.left = tmpCoords[0]+screenWidth-50;
		nextDayButtonBounds.top = tmpCoords[1];
		nextDayButtonBounds.bottom = tmpCoords[1]+50;		
		nextDayButtonDelegate = new TouchDelegate(nextDayButtonBounds, nextDaybutton);

		if (View.class.isInstance(calNavbar)) {
			calNavbar.setTouchDelegate(prevDayButtonDelegate);
			calNavbar.setTouchDelegate(nextDayButtonDelegate);
		}

	}

}