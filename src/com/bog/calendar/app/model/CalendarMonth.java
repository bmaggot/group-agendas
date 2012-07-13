package com.bog.calendar.app.model;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bog.calendar.app.ui.CalendarView;
import com.bog.calendar.app.ui.UIBuildHelper;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;

public class CalendarMonth {
	private Activity activity;
	private TextView title;
	private ImageView nextMonth;
	private ImageView prevMonth;
	private ListView selectedDayEventsList;

	private DataManagement dm;

	private CalendarView calendarView;
	private Calendar currentDate;
	private Calendar today = Calendar.getInstance();

	private ArrayList<Event> mEvents = null;

	public CalendarMonth(Activity parentActivity) {
		this.activity = parentActivity;

		dm = DataManagement.getInstance(parentActivity);

		title = (TextView) activity.findViewById(R.id.top_panel_title);
		selectedDayEventsList = (ListView) activity.findViewById(R.id.current_events_list);

		nextMonth = (ImageView) activity.findViewById(R.id.next_month_button);
		nextMonth.setOnClickListener(nextButtonClickListener);
		prevMonth = (ImageView) activity.findViewById(R.id.prev_month_button);
		prevMonth.setOnClickListener(prevButtonClickListener);

		currentDate = Calendar.getInstance();
		calendarView = (CalendarView) activity.findViewById(R.id.calendar_view);
		calendarView.setShowWeekNumber(true);
		calendarView.setSelectedDate(today);

		initCalendarView(currentDate, false);

		new getEvents().execute();
	}

	private void initCalendarView(Calendar calendar, boolean events) {
		calendarView.setDate(calendar.getTimeInMillis(), events, selectedDayEventsList);
		calendarView.setFirstDayOfWeek(Calendar.MONDAY);
	}

	protected final View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			currentDate.add(Calendar.MONTH, 1);
			initCalendarView(currentDate, false);
			new getEvents().execute();
		}
	};

	protected final View.OnClickListener prevButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			currentDate.add(Calendar.MONTH, -1);
			initCalendarView(currentDate, false);
			new getEvents().execute();
		}
	};

	private void updateTitle() {
		if (title != null) {
			String month = UIBuildHelper.getMonthByIndex(currentDate.get(Calendar.MONTH));
			title.setText(month + ", " + currentDate.get(Calendar.YEAR));
		}
	}

	class getEvents extends AsyncTask<Void, ArrayList<Event>, ArrayList<Event>> {
		@Override
		protected void onPreExecute() {
			if(mEvents == null) title.setText(CalendarMonth.this.activity.getString(R.string.loading));
		}

		@Override
		protected ArrayList<Event> doInBackground(Void... params) {
			if (mEvents == null) {
				mEvents = dm.getEventsFromLocalDb();
			}
			return mEvents;
		}

		@Override
		protected void onPostExecute(ArrayList<Event> result) {
			calendarView.setEvents(result);
			initCalendarView(currentDate, true);
			updateTitle();
		}
	}
}