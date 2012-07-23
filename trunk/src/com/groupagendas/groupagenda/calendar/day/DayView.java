package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.calendar.adapters.HourListAdapter;
import com.groupagendas.groupagenda.events.Event;

public class DayView extends LinearLayout {
	
	DayInstance selectedDay;

	ImageButton prevDayButton;
	ImageButton nextDaybutton;
	TextView topPanelTitle;

	String[] WeekDayNames;
	String[] MonthNames;
	String[] HourNames;

	private HourEventsPanel hourEventsPanel;
	private ListView allDayEventsPanel;
	private ListView hourList;
	private HourListAdapter hourListAdapter = new HourListAdapter(getContext(), null);
	private AllDayEventsAdapter allDayEventAdapter = new AllDayEventsAdapter(getContext(), null);


	public DayView(Context context) {
		this(context, null);
	}

	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);
		HourNames = getResources().getStringArray(R.array.hour_names);
		selectedDay = new DayInstance(context);
		

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		
		((Activity) getContext()).getLayoutInflater().inflate(R.layout.calendar_day, this);
		setupViewItems();
		drawHourList();
		initEventListAdapters();
	}

	private void drawHourList() {
		hourListAdapter.setList(Arrays.asList(HourNames));
		hourListAdapter.notifyDataSetChanged();
		
	}

	public void drawHourEvents() {
		System.out.println("DRAW HOUR EVENTS METHOD");
		hourEventsPanel.removeAllViews();
	 
		if (selectedDay.hasHourEvents()){
			ArrayList<Event> hourEventsList = selectedDay.getHourEvents();
			HourEventsTimetable hourEventsTimetable = selectedDay.getHourEventsTimeTable();
			
			for (Event e : hourEventsList){
				int divider = hourEventsTimetable.getWidthDivider(e);
				drawEvent(divider, e);
			}
		}
			
	}

	private void drawEvent(int overlapCount, Event event) {
		int layerWidth = hourEventsPanel.getMeasuredWidth();
		layerWidth = 100;
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Math.round(layerWidth/overlapCount), 50);
		params.topMargin = 40;
		params.leftMargin = 10;
		HourEventView eventFrame = new HourEventView(getContext(), event);
//		eventFrame.setDimensions(, 100);
		hourEventsPanel.addView(eventFrame, params);
	}

	private void initEventListAdapters() {
		allDayEventAdapter.setList(selectedDay.getAllDayEvents());
		allDayEventAdapter.notifyDataSetChanged();

		// VERY CIOTKIJ HARDCORD. Za Yeah! Peace!
		LinearLayout allDayEventsContainer = (LinearLayout) findViewById(R.id.allday_container);
		if (selectedDay.getAllDayEvents().size() < 10) {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
			allDayEventsContainer.setLayoutParams(layoutParams);
		} else {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 180); 
			allDayEventsContainer.setLayoutParams(layoutParams);
		}
		
//		Drawing hour-long events
		drawHourEvents();
	}

	private void setupViewItems() {
		prevDayButton = (ImageButton) findViewById(R.id.prevDay);
		nextDaybutton = (ImageButton) findViewById(R.id.nextDay);

		hourEventsPanel = (HourEventsPanel) findViewById(R.id.hour_events);
		hourEventsPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				System.out.println("JAU GALIU PASIIMTI DIMENSIJAS. Plotis: " + hourEventsPanel.getMeasuredWidth());
				System.out.println("Get width OUTPUT:" + hourEventsPanel.getWidth());
				
			}
		});
				  
		

		allDayEventsPanel = (ListView) findViewById(R.id.allday_events);
		allDayEventsPanel.setAdapter(allDayEventAdapter);
		
		hourList = (ListView) findViewById(R.id.hour_list);
		hourList.setClickable(false);
		hourList.setAdapter(hourListAdapter);

		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
		updateTopPanelTitle(selectedDay.getSelectedDate());

		prevDayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedDay.goPrev();
				updateTopPanelTitle(selectedDay.getSelectedDate());
				initEventListAdapters();
			}
		});

		nextDaybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedDay.goNext();
				updateTopPanelTitle(selectedDay.getSelectedDate());
				initEventListAdapters();

			}
		});
		
	}

	private void updateTopPanelTitle(Calendar selectedDate) {
		String title = WeekDayNames[selectedDate.get(Calendar.DAY_OF_WEEK) - 1];
		title += ", ";
		title += MonthNames[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.DAY_OF_MONTH);
		title += ", ";
		title += selectedDate.get(Calendar.YEAR);

		topPanelTitle.setText(title);
	}

}