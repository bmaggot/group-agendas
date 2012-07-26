package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;

import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.calendar.adapters.HourListAdapter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.utils.Utils;

public class DayView extends LinearLayout {
	
	DayInstance selectedDay;

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
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);
		HourNames = getResources().getStringArray(R.array.hour_names);
		selectedDay = new DayInstance(context);
		allDayEventAdapter = new AllDayEventsAdapter(getContext(), new ArrayList<Event>());
		

	}


	public void init() {
		setupViewItems();
		drawHourList();
		updateEventLists();
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
			label.setHeight(Math.round(40*densityFactor));
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
				drawEvent(e, divider, neighbourId);
			}
		}
			
	}

	private void drawEvent(final Event event, int divider, int neighbourId) {

		int dispWidth = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getWidth();
		int panelWidth =  Math.round(0.9f * dispWidth - 1);
		int lineHeightDP = 40;
		
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int lineHeight = (int) (lineHeightDP * scale + 0.5f);
		int oneDP = (int) (1 * scale + 0.5f);
 
       
		
		int startHour  = Utils.stringToCalendar(event.time_start, Utils.date_format).get(Calendar.HOUR_OF_DAY);
		int endHour = Utils.stringToCalendar(event.time_end, Utils.date_format).get(Calendar.HOUR_OF_DAY);
		float duration = endHour - startHour;
		
//		if event lasts less than one hour, it's resized to half of hour pane
		if (duration == 0) duration = 0.525f;   
		
		HourEventView eventFrame = new HourEventView(getContext(), event);
		
	
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(panelWidth/divider, (int)(lineHeight * duration));	
	
		params.topMargin = lineHeight * startHour;
		
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