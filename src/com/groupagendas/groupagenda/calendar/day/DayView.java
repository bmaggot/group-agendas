package com.groupagendas.groupagenda.calendar.day;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.bog.calendar.app.model.EventListAdapter;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;
import com.groupagendas.groupagenda.data.Data;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DayView extends LinearLayout{
	
	Calendar selectedDate = Calendar.getInstance();
	Button prevDayButton;
	Button nextDaybutton;
	TextView topPanelTitle;
	
	String[] WeekDayNames;

	private EventListAdapter eventListAdapter = new EventListAdapter(getContext(), null);
	private ArrayList<Event> dayEvents = new ArrayList<Event>();
	private ArrayList<Event> allDayEvents = new ArrayList<Event>();
	private LinearLayout dayEventsPanel;
	private LinearLayout allDayEventsPanel;
	
	public DayView(Context context) {
		super(context);
		dayEventsPanel = (LinearLayout) findViewById(R.id.hour_events);
		allDayEventsPanel = (LinearLayout) findViewById(R.id.allday_events);
	}
	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);
		dayEventsPanel = (LinearLayout) findViewById(R.id.hour_events);
		allDayEventsPanel = (LinearLayout) findViewById(R.id.allday_events);
	}

	protected void initEventListAdapter(Calendar date, boolean allDay){
		DataManagement dm = DataManagement.getInstance(getContext());
		ArrayList<Event> events = dm.getEvents();
		
		String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		Calendar day_start = Utils.stringToCalendar(dayStr+" 00:00:00", Utils.date_format);
		Calendar day_end   = Utils.stringToCalendar(dayStr+" 23:59:59", Utils.date_format);
		for(int i = 0; i < events.size(); i++){
			Event event = events.get(i);
			if(date.after(event.startCalendar) && date.before(event.endCalendar) && !allDay){
				dayEvents.add(event);
			} else if (!event.my_time_end.equals("null") && !event.my_time_start.equals("null")) {
				Calendar calendar_start = Utils.stringToCalendar(event.my_time_start, event.timezone, Utils.date_format);
				Calendar calendar_end = Utils.stringToCalendar(event.my_time_end, event.timezone, Utils.date_format); 
					if(calendar_end.after(day_end) && calendar_start.before(day_start)){
						allDayEvents.add(event);
					}
			}
		}
		if(!allDay){
			eventListAdapter.setList(dayEvents);
		} else {
			eventListAdapter.setList(allDayEvents);
		}
		eventListAdapter.setContext(getContext());
		ListView lv = null;
		if(!allDay){
			lv = (ListView) findViewById(R.id.hour_events);
		} else {
			lv = (ListView) findViewById(R.id.allday_events);
		}
		lv.setAdapter(eventListAdapter);
		eventListAdapter.notifyDataSetChanged();
	}
 
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		((Activity)getContext()).getLayoutInflater().inflate(R.layout.calendar_day_rewrite, this);
		setupViewItems();
	}
 
	private void setupViewItems() {
		prevDayButton = (Button)findViewById(R.id.prevDay);
		nextDaybutton = (Button)findViewById(R.id.nextDay);
		topPanelTitle = (TextView) findViewById(R.id.top_panel_title); 
				
		updateTopPanelTitle(selectedDate);
	}

	private void updateTopPanelTitle(Calendar selectedDate) {
//		TODO
		
	}
 
	
}