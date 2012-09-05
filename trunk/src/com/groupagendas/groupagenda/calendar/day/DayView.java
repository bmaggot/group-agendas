package com.groupagendas.groupagenda.calendar.day;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarViewWithAllDayAndHourEvents;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;


public class DayView extends AbstractCalendarViewWithAllDayAndHourEvents {
	
	DayInstance selectedDay;
	

	private HourEventsPanel hourEventsPanel;
	private ListView allDayEventsPanel;
	private LinearLayout hourList;
	private AllDayEventsAdapter allDayEventAdapter;
	

	public DayView(Context context) {
		this(context, null);
	}

	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		showHourEventsIcon = true;	
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);
		
		allDayEventAdapter = new AllDayEventsAdapter(getContext(), new ArrayList<Event>());

	}
	

	
	@Override
	protected void setTopPanel() {
		Calendar selectedDate = selectedDay.getSelectedDate();
		String title = WeekDayNames[selectedDate.get(Calendar.DAY_OF_WEEK) - 1];
		title += ", ";
		title += MonthNames[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.DAY_OF_MONTH);
		title += ", ";
		title += selectedDate.get(Calendar.YEAR);

		this.getTopPanelTitle().setText(title);
		
	}


	@Override
	public void setupView() {
		
		allDayEventsPanel = (ListView) findViewById(R.id.allday_events);
		allDayEventsPanel.setAdapter(allDayEventAdapter);
		allDayEventsPanel.setOnTouchListener(createListener(swipeGestureDetector));
		
		
		
		
		
		
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


//	@Override
	public void goPrev(){
		selectedDay.goPrev();
		setTopPanel(); //adjust top panel title accordingly
		updateEventLists();
	}
	
//	@Override
	public void goNext(){
		selectedDay.goNext();
		setTopPanel(); //adjust top panel title accordingly
		updateEventLists();
	}


	private void scrollHourPanel() {
		final float hour;
		
		if (Utils.isToday(selectedDay.getSelectedDate())){
			Calendar tmp = Calendar.getInstance();
			hour = tmp.get(Calendar.HOUR_OF_DAY) + tmp.get(Calendar.MINUTE) / 60.0f;
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
			label.setHeight(Math.round(hourLineHeightDP * densityFactor));
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
				drawHourEvent(e, divider, neighbourId, hourEventsPanel, EVENTS_COLUMN_WIDTH, selectedDay);
			}
		
		}
		
			
	}


	

	protected void updateEventLists() {
		
		List<Event> events = selectedDay.getAllDayEvents();
		setAllDayEventsPanelHeight(events.size());
		allDayEventAdapter.setList(events);
		allDayEventAdapter.notifyDataSetChanged();
		drawHourEvents(); // Drawing hour-long events
	}

	@Override
	public Calendar getDateToResume() {
		return selectedDay.getSelectedDate();
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDay = new DayInstance(getContext(), initializationDate);
		
	}

	@Override
	protected int getShownDaysCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setShownDays(int daysToShow) {
		// TODO Auto-generated method stub
		
	}

	
	







}