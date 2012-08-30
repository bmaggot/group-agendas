package com.groupagendas.groupagenda.calendar.week;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.day.DayView;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class WeekDayTouchListener extends SimpleOnGestureListener {

	private Calendar selectedDate;
	private AbstractCalendarView parentView;
	
	public WeekDayTouchListener (AbstractCalendarView parent, Calendar selectedDate) {
		parentView = parent;
		this.selectedDate = selectedDate;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		System.out.println("TAP");
		int y = Math.round(e.getY());

		float tmpF = DayView.hourLineHeightDP * parentView.getResources().getDisplayMetrics().density;
		int hour = y / Math.round(tmpF);

		Activity navbar = (Activity) (parentView.getContext());
		Intent intent = new Intent(navbar, NewEventActivity.class);

		Calendar tmp = (Calendar) selectedDate.clone();
		tmp.set(Calendar.HOUR_OF_DAY, hour);
		if(y/tmpF % 1 >= 0.5){
			tmp.add(Calendar.MINUTE, 30);
		}

		intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR, Utils.formatCalendar(tmp));

		navbar.startActivity(intent);
		return true;
	}

}
