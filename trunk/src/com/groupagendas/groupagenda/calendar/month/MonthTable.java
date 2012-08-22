package com.groupagendas.groupagenda.calendar.month;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableLayout;

public class MonthTable extends TableLayout {
	Calendar selectedDate;
	Calendar firstDatetoShow;
	public MonthTable(Context context) {
		this(context, null);
	}

	public MonthTable(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.out.println("monthTable constructor");
//		TODO
	}
	
	public void initWithDate(Calendar date){
//		TODO
	}

}
