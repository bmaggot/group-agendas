package com.groupagendas.groupagenda.calendar;

import com.groupagendas.groupagenda.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public abstract class AbstractCalendarViewWithAllDayAndHourEvents extends AbstractCalendarView {
	
	public AbstractCalendarViewWithAllDayAndHourEvents(Context context) {
		super(context);
	}

	public AbstractCalendarViewWithAllDayAndHourEvents(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}

	public static final float DEFAULT_TIME_TO_SCROLL = 7.5f; //DEFAULT HOUR TO SCROLL. 7.5f = 7:30
	public static final int hourLineHeightDP = 23;  //HEIGHT OF ONE HOUR LINE IN DIP for day and week view
	public static final int allDayLineHeightDP = 18;
	
	protected abstract void updateEventLists();
	
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

}
