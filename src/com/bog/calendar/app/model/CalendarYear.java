package com.bog.calendar.app.model;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bog.calendar.app.ui.CalendarYearView;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;

public class CalendarYear {
	private int currentYear;
	private Calendar currentDate;
	private TextView title;
	private ImageView nextYear;
	private ImageView prevYear;
	private Activity activity;

	private DataManagement dm;
	private ArrayList<Event> mEvents = null;
	
	private Calendar today = Calendar.getInstance();

	public CalendarYear(Activity parentActivity) {
		this.activity = parentActivity;

		dm = DataManagement.getInstance(parentActivity);

		currentDate = Calendar.getInstance();
		nextYear = (ImageView) activity.findViewById(R.id.next_month_button);
		nextYear.setOnClickListener(nextButtonClickListener);
		prevYear = (ImageView) activity.findViewById(R.id.prev_month_button);
		prevYear.setOnClickListener(prevButtonClickListener);
		title = (TextView) activity.findViewById(R.id.top_panel_title);
		
		
		
		initView(mEvents, false);
		new getEvents().execute();
	}

	private void initView(ArrayList<Event> events, boolean isEvents) {
		currentYear = currentDate.get(Calendar.YEAR);
		title.setText(Integer.toString(currentYear));
		Calendar currentMonth = Calendar.getInstance();
		for (int m = 0; m < 12; m++) {
			CalendarYearView curMonth = (CalendarYearView) activity.findViewById(R.id.month_00 + m);
			currentMonth.set(Calendar.YEAR, currentYear);
			currentMonth.set(Calendar.MONTH, m);
			currentMonth.set(Calendar.DAY_OF_MONTH, 1);
			curMonth.setShowDayHeader(m < 3);
			curMonth.setShowMonthNameHeader(true);
			curMonth.setFirstDayOfWeek(Calendar.SUNDAY);
			curMonth.setSelectedDate(today);
			
			if(events != null && isEvents) curMonth.setEvents(events);
			
			curMonth.setDate(currentMonth.getTimeInMillis(), isEvents);
		}
	}

	protected final View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			currentDate.set(Calendar.YEAR, ++currentYear);
			initView(mEvents, false);
			new getEvents().execute();
		}
	};

	protected final View.OnClickListener prevButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			currentDate.set(Calendar.YEAR, --currentYear);
			initView(mEvents, false);
			new getEvents().execute();
		}
	};

	class getEvents extends AsyncTask<Void, ArrayList<Event>, ArrayList<Event>> {
		@Override
		protected void onPreExecute() {
			if(mEvents == null) title.setText(CalendarYear.this.activity.getString(R.string.loading));
		}

		@Override
		protected ArrayList<Event> doInBackground(Void... params) {
			if (mEvents == null) {
				mEvents = dm.getEventsFromDb();
			}
			return mEvents;
		}

		@Override
		protected void onPostExecute(ArrayList<Event> result) {
			mEvents = result;
			initView(result, true);
		}
	}
}
