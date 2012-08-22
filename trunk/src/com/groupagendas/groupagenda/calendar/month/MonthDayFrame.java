package com.groupagendas.groupagenda.calendar.month;

import java.util.List;

import com.groupagendas.groupagenda.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MonthDayFrame extends LinearLayout {
	

	TextView dayTitle;
	private boolean isToday;
	private boolean isSelected;
	
	public MonthDayFrame(Context context) {
		this(context, null);
	}
	public MonthDayFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		isSelected = false;
		isToday = false;
		
	}
	
	public void setDayTitle (String title){
		if(dayTitle == null) dayTitle = (TextView) findViewById(R.id.month_day_title);
		dayTitle.setText(title);
	}
	
	public void DrawColourBubbles (List<String> colourList){
//		TODO
//		
	}
	/**
	 * @author
	 */
	public void setSelected(boolean bool) {
		if (bool != isSelected) {
			isSelected = bool;
			// TODO
		}
	}
	public void setToday(boolean bool){
		if (bool != isToday) {
			isToday = bool;
//			TODO selected stiliaus prioritetas aukstesnis nei today
		}
	}
		
//	public void setToday(boolean bool){
//			if (bool != isToday) {
//				isToday = bool;
////				TODO selected stiliaus prioritetas aukstesnis nei today
//			}
//
//	}

}
