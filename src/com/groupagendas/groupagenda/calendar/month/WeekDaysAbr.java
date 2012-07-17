package com.groupagendas.groupagenda.calendar.month;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class WeekDaysAbr extends LinearLayout {
	
	public WeekDaysAbr(Context context) {
		this(context, null);	
	}

	public WeekDaysAbr(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		inflater.inflate(R.layout.calendar_week_days_abr, this);
	}

}
