package com.groupagendas.groupagenda.calendar.day;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class HourEventsPanel extends RelativeLayout {

	public HourEventsPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HourEventsPanel(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    //getMeasuredHeight() and getMeasuredWidth() now contain the suggested size
	}

	
	
}
