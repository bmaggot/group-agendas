package com.bog.calendar.app.model;

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
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.utils.DateTimeUtils;

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
    
//    private DataManagement dm;
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
        
//        dm = DataManagement.getInstance(activity);
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
			return new ArrayList<CEvent>();
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
}