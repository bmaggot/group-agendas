package com.bog.calendar.app.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bog.calendar.app.ui.EventGroupView;
import com.bog.calendar.app.ui.EventView;
import com.groupagendas.groupagenda.DataManagement;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class CalendarDay implements OnClickListener {
    Activity activity;
    TextView titleView;
    LinearLayout allDayEventsPanel;
    RelativeLayout eventsPanel;
    List<CEvent> eventList;
    List<CEvent> allDayEvents;
    List<BaseEvent> groupedEvents;
    
    private Calendar currentDay;
    
    private Button prevDay;
    private Button nextDay;
    
    private DataManagement dm;
    private DateTimeUtils dtUtils;

    public CalendarDay(Activity parentActivity) {
        this.activity = parentActivity;
        titleView = (TextView) parentActivity.findViewById(R.id.top_panel_title);
        allDayEventsPanel = (LinearLayout) parentActivity.findViewById(R.id.all_day_events_panel);
        eventsPanel = (RelativeLayout) parentActivity.findViewById(R.id.events_panel);
        
        prevDay = (Button) parentActivity.findViewById(R.id.prevDay);
        prevDay.setOnClickListener(this);
        nextDay = (Button) parentActivity.findViewById(R.id.nextDay);
        nextDay.setOnClickListener(this);
        
        currentDay = Calendar.getInstance();
        
        dm = DataManagement.getInstance(activity);
        dtUtils = new DateTimeUtils(activity);
        
        new GetDayEventsTask().execute();
    }

    public void refresh() {
        allDayEventsPanel.removeAllViews();
        for (int a = 0; a < allDayEvents.size(); a++) {
            EventView allDayEventView = new EventView(activity, allDayEvents.get(a));
            allDayEventsPanel.addView(allDayEventView);
        }
        eventsPanel.removeAllViews();
        for (int e = 0; e < groupedEvents.size(); e++) {
            BaseEvent baseEvent = groupedEvents.get(e);
            if (baseEvent instanceof CEvent) {
                EventView eventView = new EventView(activity, (CEvent) baseEvent);
                eventsPanel.addView(eventView);
            } else if (baseEvent instanceof EventsGroup) {
                EventGroupView eventGroupView = new EventGroupView(activity, (EventsGroup) baseEvent);
                eventsPanel.addView(eventGroupView);
            }
        }
    }

    public void setTitle(Date date) {
        titleView.setText(dtUtils.formatDate(date));
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.prevDay:
			currentDay.add(Calendar.DAY_OF_YEAR, -1);
			new GetDayEventsTask().execute();
			break;
		case R.id.nextDay:
			currentDay.add(Calendar.DAY_OF_YEAR, 1);
			new GetDayEventsTask().execute();
			break;
		}
	}
	
	class GetDayEventsTask extends AsyncTask<Void, List<CEvent>, List<CEvent>>{
		@Override
		protected void onPreExecute() {
			titleView.setText(activity.getString(R.string.loading));
			super.onPreExecute();
		}
		@Override
		protected List<CEvent> doInBackground(Void... params) {
			return getActualEvents(dm.getEventsFromDb());
		}
		
		@Override
		protected void onPostExecute(List<CEvent> eventsList) {
			eventList = eventsList;
			allDayEvents = EventsHelper.extractAllDayEvents(eventList);
	        groupedEvents = EventsHelper.findEventsIntersects(eventList);
			setTitle(currentDay.getTime());
	        refresh();
		}
		
	}
	private List<CEvent> getActualEvents(ArrayList<Event> events){
		List<CEvent> eventsList = new ArrayList<CEvent>();
		
		String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(currentDay.getTime());
		Calendar day_start = Utils.stringToCalendar(dayStr+" 00:00:00", Utils.date_format);
		Calendar day_end   = Utils.stringToCalendar(dayStr+" 23:59:59", Utils.date_format);
		
		Calendar calendar_start = null;
		Calendar calendar_end = null;
		
		String startTime;
		String endTime;
		boolean allDay;
		
		for (int i = 0, l = events.size(); i < l; i++) {
			final Event event = events.get(i);
			if (!event.my_time_end.equals("null") && !event.my_time_start.equals("null")) {
				calendar_start = Utils.stringToCalendar(event.my_time_start, event.timezone, Utils.date_format);
				calendar_end = Utils.stringToCalendar(event.my_time_end, event.timezone, Utils.date_format);
				
				if(!calendar_end.before(day_start) && !calendar_start.after(day_end)){
					
					startTime = event.my_time_start.substring(11,16);
					endTime   = event.my_time_end.substring(11,16);
					allDay   = false;
					
					if(calendar_start.before(day_start)){
						startTime = "00:00";
					}
					
					if(calendar_end.after(day_end)){
						endTime = "23:59";
					}
					
					if (calendar_end.after(day_end) && calendar_start.before(day_start)) {
						startTime = "00:00";
						endTime = "01:00";
						allDay = true;
					}
					
					final CEvent cevent = EventsHelper.generateDayEvent(event.event_id, event.type, event.title, event.title, startTime, endTime);
					
					if(event.color != null && !event.color.equals("null")){
						cevent.setColor(Integer.parseInt(event.color, 16)+0xFF000000);
					}
					
					eventsList.add(cevent);
					cevent.setAllDay(allDay);
				}				
			}
		}

		return eventsList;
	}
}