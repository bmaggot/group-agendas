package com.groupagendas.groupagenda.calendar.month;

import java.util.Calendar;

import com.groupagendas.groupagenda.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MonthViewContainer extends LinearLayout {
	private Calendar selectedDate = Calendar.getInstance();
	private Calendar currentDate = Calendar.getInstance();
	private LayoutInflater mInflater;
	private String[] month_names;
	private CalendarViewRewrite calendarView;

	public MonthViewContainer(Context context) {
		this(context, null);
	}

	public MonthViewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		month_names = getContext().getResources().getStringArray(R.array.month_names);
		
		selectedDate.setTimeInMillis(currentDate.getTimeInMillis());
		mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.month_rewrite, this);
		setMonthViewTitle(currentDate);
		
		ImageView prevButton = (ImageView) this.findViewById(R.id.prev_month_button);
		ImageView nextButton = (ImageView) this.findViewById(R.id.next_month_button);
		
		prevButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedDate.add(Calendar.MONTH, -1);
				setMonthViewTitle(selectedDate);
//				TODO get events
//				TODO update calendar view
				
			}
		});
		nextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedDate.add(Calendar.MONTH, 1);
				setMonthViewTitle(selectedDate);
//				TODO get events
//				TODO update calendar view
			}
		});
		
		calendarView = (CalendarViewRewrite)this.findViewById(R.id.calendar_view_rewrite);
		
	}
	protected void setMonthViewTitle(Calendar date) {
		TextView top_panel_title = (TextView) this.findViewById(R.id.top_panel_title);
		top_panel_title.setText(month_names[date.get(Calendar.MONTH)] + " " + date.get(Calendar.YEAR));
		
	}
	
}
