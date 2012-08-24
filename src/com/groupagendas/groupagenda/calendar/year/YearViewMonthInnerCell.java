package com.groupagendas.groupagenda.calendar.year;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.MonthCellState;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class YearViewMonthInnerCell extends RelativeLayout {
	TextView text;
	FrameLayout BubbleContainer;
	boolean isWeekNum;
	boolean hasEvents = false;
	MonthCellState state = MonthCellState.DEFAULT;
	
	public YearViewMonthInnerCell(Context context) {
		this(context, null);
	}
	
	public YearViewMonthInnerCell(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public YearViewMonthInnerCell(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	/**
	 * Used to setup weekNUm
	 * @param isWeekNum
	 * @param text
	 */
	public void setupWeekNum(String text){
		this.isWeekNum = true;
		this.text.setText(text);
	}
	
	public void setup(String text, MonthCellState state){
		this.isWeekNum = false;
		this.text.setText(text);
	}
	
	public void setHasEvents(boolean bool){
		hasEvents = bool;
		if (hasEvents){
			drawEventBubble();
		}
	}
	
	

	public void setState(MonthCellState state) {
		this.state = state;
		refresh();
	}

	private void refresh() {
		switch (state){
		case SELECTED:
//			this.setBackgroundResource(R.drawable.calendar_month_day_selected);
//			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_selectedday);
			break;
		case TODAY:
//			this.setBackgroundResource(R.drawable.calendar_month_day_today);
//			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_today);
			break;
		case OTHER_MONTH:
//			this.setBackgroundResource(R.drawable.calendar_month_day_inactive);	
//			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_othermonth);
			break;
		default:
//			this.setBackgroundResource(R.drawable.calendar_month_day_inactive);	
//			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_thismonth);
			break;		
		}
		
	}

	private void drawEventBubble() {
		// TODO Auto-generated method stub
		
	}

}
