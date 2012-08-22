package com.groupagendas.groupagenda.calendar.month;

import java.util.List;

import com.groupagendas.groupagenda.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MonthDayFrame extends RelativeLayout {
	

	TextView dayTitle;
	private boolean isToday;
	private boolean isSelected;
	private boolean isOtherMonth;
	
	public MonthDayFrame(Context context) {
		this(context, null);
	}
	public MonthDayFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		isSelected = false;
		isToday = false;
		isOtherMonth = false;
		setBackground();
		
	}
	
	public void setDayTitle (String title){
		if(dayTitle == null) dayTitle = (TextView) findViewById(R.id.month_day_title);
		dayTitle.setText(title);
	}
	
	public void DrawColourBubbles (List<String> colourList){
//		TODO
//		
	}
	
	private void setBackground(){
		if (isSelected){
			this.setBackgroundResource(R.drawable.calendar_month_day_selected);
//			dayTitle.setTextAppearance(getContext(),)
			return;
		}
		if (isToday){
			this.setBackgroundResource(R.drawable.calendar_month_day_today);
			return;
		}
		this.setBackgroundResource(R.drawable.calendar_month_day_inactive);
		if (isOtherMonth){
			
		}
		
	}
	/**
	 * @author
	 */
	public void setSelected(boolean bool) {
		if (bool != isSelected) {
			isOtherMonth = false;
			isSelected = bool;
			setBackground();
		}
	}
	public void setToday(boolean bool){
		isOtherMonth = false;
		if (bool != isToday) {
			isOtherMonth = false;
			isToday = bool;
			setBackground();
		}
	}
	public void setOtherMonth(boolean isOtherMonth) {
		this.isOtherMonth = isOtherMonth;
		isSelected = false;
		isToday = false;
		setBackground();
	}
	
	
//	public void setToday(boolean bool){
//			if (bool != isToday) {
//				isToday = bool;
////				TODO selected stiliaus prioritetas aukstesnis nei today
//			}
//
//	}

}
