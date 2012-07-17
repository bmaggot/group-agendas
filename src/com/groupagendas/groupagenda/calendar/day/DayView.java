package com.groupagendas.groupagenda.calendar.day;




import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DayView extends LinearLayout{
	
	Calendar selectedDate = Calendar.getInstance();
	Button prevDayButton;
	Button nextDaybutton;
	TextView topPanelTitle;
	
	String[] WeekDayNames;

	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_names);
		selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);
	}
 
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		((Activity)getContext()).getLayoutInflater().inflate(R.layout.calendar_day_rewrite, this);
		setupViewItems();
	}
 
	private void setupViewItems() {
		prevDayButton = (Button)findViewById(R.id.prevDay);
		nextDaybutton = (Button)findViewById(R.id.nextDay);
		topPanelTitle = (TextView) findViewById(R.id.top_panel_title); 
				
		updateTopPanelTitle(selectedDate);
	}

	private void updateTopPanelTitle(Calendar selectedDate) {
//		TODO
		
	}
 
	
}