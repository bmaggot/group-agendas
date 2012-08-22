package com.groupagendas.groupagenda.calendar;

import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.day.DayInstance;
import com.groupagendas.groupagenda.calendar.day.HourEventView;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public abstract class AbstractCalendarViewWithAllDayAndHourEvents extends AbstractCalendarView {
	
	public AbstractCalendarViewWithAllDayAndHourEvents(Context context) {
		this(context, null);
	}

	public AbstractCalendarViewWithAllDayAndHourEvents(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}

	
	protected final int EVENTS_COLUMN_WIDTH =  Math.round(0.9f * VIEW_WIDTH - 1);
	protected final int HOUR_COLUMN_WIDTH =  VIEW_WIDTH - EVENTS_COLUMN_WIDTH;
	
	public static final float DEFAULT_TIME_TO_SCROLL = 7.5f; //DEFAULT HOUR TO SCROLL. 7.5f = 7:30
	public static final int hourLineHeightDP = 23;  //HEIGHT OF ONE HOUR LINE IN DIP for day and week view
	public static final int allDayLineHeightDP = 18;
	
	protected boolean showHourEventsIcon = false;
	
	
	
	
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

}
