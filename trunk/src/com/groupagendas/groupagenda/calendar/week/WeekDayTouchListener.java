package com.groupagendas.groupagenda.calendar.week;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.DayWeekView;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.Utils;

public class WeekDayTouchListener extends SimpleOnGestureListener {

	private Calendar selectedDate;
	private AbstractCalendarView parentView;
	
	public WeekDayTouchListener (AbstractCalendarView parent, Calendar selectedDate) {
		parentView = parent;
		this.selectedDate = selectedDate;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		int y = Math.round(e.getY());

		float tmpF = DayWeekView.hourLineHeightDP * parentView.getResources().getDisplayMetrics().density;
		int hour = y / Math.round(tmpF);

		Activity navbar = (Activity) (parentView.getContext());
		Intent intent = new Intent(navbar, NewEventActivity.class);

		Calendar tmp = (Calendar) selectedDate.clone();
		tmp.set(Calendar.HOUR_OF_DAY, hour);
		if(y/tmpF % 1 >= 0.5){
			tmp.add(Calendar.MINUTE, 30);
		}

		intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR, Utils.formatCalendar(tmp, DataManagement.SERVER_TIMESTAMP_FORMAT));

		navbar.startActivity(intent);
		return true;
	}

}
