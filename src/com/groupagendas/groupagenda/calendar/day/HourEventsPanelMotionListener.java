package com.groupagendas.groupagenda.calendar.day;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;

import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.DayWeekView;
import com.groupagendas.groupagenda.calendar.SwipeOnGestureListener;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.Utils;

public class HourEventsPanelMotionListener extends SwipeOnGestureListener {

	private Calendar selectedDate;

	public HourEventsPanelMotionListener(AbstractCalendarView parent, Calendar selectedDate) {
		super(parent);
		this.selectedDate = selectedDate;
	}

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

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return super.onFling(e1, e2, velocityX, velocityY);

	}

}
