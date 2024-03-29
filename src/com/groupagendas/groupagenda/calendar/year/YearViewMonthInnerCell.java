package com.groupagendas.groupagenda.calendar.year;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.MonthCellState;

public class YearViewMonthInnerCell extends RelativeLayout{
	TextView text;
	ImageView eventsIndicator;
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
	}

	public void setDayNum(String text){
		initTextField();
		this.text.setText(text);
	}
	
	
	
	public MonthCellState getState() {
		return state;
	}

	/**
	 * Used to setup day cell
	 * @param isWeekNum
	 * @param text
	 */
	public void setupDayCell(String text, MonthCellState state){
		initTextField();
		this.eventsIndicator = (ImageView) findViewById(R.id.indicator);
		this.text.setText(text);
		setState(state);
	}
	
	public void setHasEvents(boolean bool){
		if(this.eventsIndicator == null)
			this.eventsIndicator = (ImageView) findViewById(R.id.indicator);
		hasEvents = bool;
		if (hasEvents){
			eventsIndicator.setVisibility(VISIBLE);
		}else{
			eventsIndicator.setVisibility(INVISIBLE);
		}
	}
	
	private void initTextField(){
		if (text == null){
			text = (TextView) findViewById(R.id.year_daynumber);
		}
	}

	public void setState(MonthCellState state) {
		
		this.state = state;
		refresh();
	}

	private void refresh() {
		initTextField();
		this.setVisibility(VISIBLE);
		
		switch (state){
		case SELECTED:
			this.setBackgroundColor(R.color.event_invite_button_c_gradstart);
			text.setTextColor(R.color.white);
			break;
		case TODAY:
			this.setBackgroundColor(R.color.darker_gray);
			text.setTextColor(R.color.white);
			break;
		case OTHER_MONTH:
			this.setVisibility(INVISIBLE);
			break;
		case WEEK_END:
			this.setBackgroundColor(Color.TRANSPARENT);	
			text.setTextAppearance(getContext(), R.style.yearview_monthviewinnercell_nd);
			break;
		default:
			this.setBackgroundColor(Color.TRANSPARENT);	
			text.setTextAppearance(getContext(), R.style.yearview_monthviewinnercell_wd);
			break;		
		}
		
		
	}
	
	public String getText() {
		return (String) text.getText();
	}

}
