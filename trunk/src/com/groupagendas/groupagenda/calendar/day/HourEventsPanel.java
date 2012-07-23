package com.groupagendas.groupagenda.calendar.day;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class HourEventsPanel extends RelativeLayout {

	public HourEventsPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.out.println("KONSTRUKTORIUS ISKVIECIAMAS");
	}

	public HourEventsPanel(Context context) {
		this(context, null);
	}
	
	
}
