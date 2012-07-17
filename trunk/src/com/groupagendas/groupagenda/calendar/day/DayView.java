package com.groupagendas.groupagenda.calendar.day;




import java.util.Calendar;

import com.groupagendas.groupagenda.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class DayView extends LinearLayout{
	
	Calendar selectedDate = Calendar.getInstance();

	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
 
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		((Activity)getContext()).getLayoutInflater().inflate(R.layout.calendar_day_rewrite, this);
		setupViewItems();
	}
 
	private void setupViewItems() {
		System.out.println("SETINSIM itemus");
	}
 
	
}