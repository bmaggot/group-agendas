package com.groupagendas.groupagenda.calendar.day;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bog.calendar.app.model.EventListAdapter;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AllDayEventsAdapter;
import com.groupagendas.groupagenda.calendar.adapters.HourEventsAdapter;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;

public class DayView extends LinearLayout {

	Calendar selectedDate = Calendar.getInstance();
	ImageButton prevDayButton;
	ImageButton nextDaybutton;
	TextView topPanelTitle;

	String[] WeekDayNames;
	String[] MonthNames;

	private EventListAdapter eventListAdapter = new EventListAdapter(getContext(), null);
	private ListView dayEventsPanel;
	private ListView allDayEventsPanel;
	private HourEventsAdapter hourEventAdapter = new HourEventsAdapter(getContext(), null);
	private AllDayEventsAdapter allDayEventAdapter = new AllDayEventsAdapter(getContext(), null);;

	public DayView(Context context) {
		this(context, null);
	}

	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		MonthNames = getResources().getStringArray(R.array.month_names);

		selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		((Activity) getContext()).getLayoutInflater().inflate(R.layout.calendar_day_rewrite, this);
		setupViewItems();
		initEventListAdapters(selectedDate);
		// initEventListAdapter(selectedDate, false);
		// initEventListAdapter(selectedDate, true);
	}

	private void initEventListAdapters(Calendar selectedDate) {
		ArrayList<Event> events = Data.getEvents();

		// hourEventAdapter.setList(filterHourEvents(events, selectedDate));
		// dayEventsPanel.setAdapter(hourEventAdapter);
		// TODO hourEventAdapter.notifyDataSetChanged();

		allDayEventAdapter.setList(filterAllDayEvents(events, selectedDate));
		allDayEventAdapter.notifyDataSetChanged();

		

	}

//	private void testAdapter(ArrayList<Event> events) {
////		System.out.println("EVENTU: " + events.size());
////		allDayEventAdapter.setList(events);
////		allDayEventAdapter.notifyDataSetChanged();
//
//	}

	private List<Event> filterAllDayEvents(ArrayList<Event> events, Calendar date) {
		ArrayList<Event> allDayEvents = new ArrayList<Event>();

		Calendar currentDay = Calendar.getInstance();
		System.out.println(currentDay.getTime());
		String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(currentDay.getTime());
		Calendar day_start = Utils.stringToCalendar(dayStr + " 00:00:00", Utils.date_format);
		System.out.println("Day start" + day_start.getTime());
		Calendar day_end = Utils.stringToCalendar(dayStr + " 23:59:59", Utils.date_format);
		System.out.println("Day end" + day_end.getTime());
		Calendar calendar_start = null;
		Calendar calendar_end = null;

		for (Event event : events) {

			if (!event.my_time_end.equals("null") && !event.my_time_start.equals("null")) {
				calendar_start = Utils.stringToCalendar(event.my_time_start, event.timezone, Utils.date_format);
				calendar_end = Utils.stringToCalendar(event.my_time_end, event.timezone, Utils.date_format);

				if (!calendar_end.before(day_start) && !calendar_start.after(day_end)) {
					if ((calendar_end.after(day_end) || calendar_end.equals(day_end)) && (calendar_start.before(day_start) || calendar_start
							.equals(calendar_start)) || event.is_all_day) {
						allDayEvents.add(event);
					}
				}
			}
		}
		return allDayEvents;
	}

	private List<Event> filterHourEvents(ArrayList<Event> events, Calendar date) {
		ArrayList<Event> dayEvents = new ArrayList<Event>();
		for (Event event : events) {
			if (date.after(event.startCalendar) && date.before(event.endCalendar)) {
				dayEvents.add(event);
				System.out.println(event.title);
			}
		}
		return dayEvents;
	}

	private void setupViewItems() {
		prevDayButton = (ImageButton) findViewById(R.id.prevDay);
		nextDaybutton = (ImageButton) findViewById(R.id.nextDay);

		dayEventsPanel = (ListView) findViewById(R.id.hour_events);
		dayEventsPanel.setAdapter(hourEventAdapter);

		allDayEventsPanel = (ListView) findViewById(R.id.allday_events);
		allDayEventsPanel.setAdapter(allDayEventAdapter);

		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
		updateTopPanelTitle(selectedDate);

		prevDayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedDate.add(Calendar.DATE, -1);
				updateTopPanelTitle(selectedDate);
				initEventListAdapters(selectedDate);
			}
		});

		nextDaybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedDate.add(Calendar.DATE, 1);
				updateTopPanelTitle(selectedDate);
				initEventListAdapters(selectedDate);

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