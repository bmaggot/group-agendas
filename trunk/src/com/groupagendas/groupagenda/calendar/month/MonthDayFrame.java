package com.groupagendas.groupagenda.calendar.month;

import java.util.List;

import com.groupagendas.groupagenda.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MonthDayFrame extends RelativeLayout {
	

	TextView dayTitle;
	private boolean today;
	private boolean selected;
	private boolean otherMonth;
	
	public MonthDayFrame(Context context) {
		this(context, null);
	}
	public MonthDayFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		selected = false;
		today = false;
		otherMonth = false;
	}
	
	public void setDayTitle (String title){
		if(dayTitle == null) dayTitle = (TextView) findViewById(R.id.month_day_title);
		dayTitle.setText(title);
	}
	
	public void DrawColourBubbles (List<String> colourList){
//		TODO
//		
	}
	
//	Must be called after inflate, because dayTitle cannot be null
	public void refreshStyle(){
		
		if (selected){
			this.setBackgroundResource(R.drawable.calendar_month_day_selected);
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_selectedday);
			return;
		}
		if (today){
			this.setBackgroundResource(R.drawable.calendar_month_day_today);
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_today);
			return;
		}
		
		this.setBackgroundResource(R.drawable.calendar_month_day_inactive);		
		if (otherMonth){
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_othermonth);
		}else dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_thismonth);
		
	}
	/**
	 * @author
	 */
	public void setSelected(boolean bool) {
		
			this.selected = bool;
	}
	public void setToday(boolean bool){
	
			this.today = bool;
	}
	public void setOtherMonth(boolean bool) {
	
		this.otherMonth = bool;
		if (bool) {
			selected = false;
			today = false;
		}
	}
	public boolean isToday() {
		return today;
	}
	public boolean isSelected() {
		return selected;
	}
	public boolean isOtherMonth() {
		return otherMonth;
	}
	
	

}
