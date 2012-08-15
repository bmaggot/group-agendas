package com.groupagendas.groupagenda.calendar.week;

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
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;


import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.AbstractCalendarViewWithAllDayAndHourEvents;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.calendar.day.HourEventsPanel;
import com.groupagendas.groupagenda.calendar.day.HourEventsPanelMotionListener;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;


public class WeekView extends AbstractCalendarViewWithAllDayAndHourEvents {
	
	private static final float DEFAULT_TIME_TO_SCROLL = 7.5f; //DEFAULT HOUR TO SCROLL. 7.5f = 7:30
	public static final int hourLineHeightDP = 23;  //HEIGHT OF ONE HOUR LINE IN DIP

	WeekInstance daysShown;
	
	
	boolean am_pmEnabled;
//

	

	String[] WeekDayNames;
	String[] MonthNames;
	String[] HourNames;
	
	

	private HourEventsPanel hourEventsPanel;
	private RelativeLayout allDayEventsPanel;
	private LinearLayout hourList;
	

//	private AllDayEventsAdapter allDayEventAdapter;
	

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
//		allDayEventAdapter = new AllDayEventsAdapter(getContext(), new ArrayList<Event>());

	}

	//adjusts top panel title accordingly to selectedDate field
	@Override
	protected void setTopPanelTitle() {
		Calendar selectedDate = daysShown.getShownDate();
		
		String title = getResources().getString(R.string.week);
		title += " ";
		title += selectedDate.get(Calendar.WEEK_OF_YEAR);
		title += ", ";
		title += MonthNames[selectedDate.get(Calendar.MONTH)];
		title += " ";
		title += selectedDate.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);
		
	}


	@Override
	public void setupView() {
		allDayEventsPanel = (RelativeLayout) findViewById(R.id.allday_events);
		setAllDayEventsPanelHeight(daysShown.getMaxAllDayEventCount());

		
//		initialize column with hour titles
		hourList = (LinearLayout) findViewById(R.id.hour_list);
		hourList.setClickable(false);
		drawHourList();
		
//		TODO initialize hour event panel
//		hourEventsPanel = (HourEventsPanel) findViewById(R.id.hour_events);
//		hourEventsPanel.setSwipeGestureDetector(new GestureDetector(new HourEventsPanelMotionListener(this, selectedDay.getSelectedDate())));
//		hourEventsPanel.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if (hourEventsPanel.getSwipeGestureDetector().onTouchEvent(event)) {
//				     return false;
//				    } else {
//				     return true;
//				    }
//			}
//		});
//		
//		updateEventLists();
		scrollHourPanel();	
	}
	@Override
	public void goPrev(){
		daysShown.prevPage();
		setTopPanelTitle(); 
		updateEventLists();
	}
	
	@Override
	public void goNext(){
		daysShown.nextPage();
		setTopPanelTitle();
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
			label.setHeight(Math.round(hourLineHeightDP*densityFactor));
			label.setWidth(LayoutParams.FILL_PARENT);
			hourList.addView(label);
		}
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
		// TODO Auto-generated method stub
		
	}


}