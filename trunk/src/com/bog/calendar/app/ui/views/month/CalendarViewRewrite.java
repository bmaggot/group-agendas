package com.bog.calendar.app.ui.views.month;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bog.calendar.app.model.EventListAdapter;
import com.bog.calendar.app.ui.Widget;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;


@Widget
public class CalendarViewRewrite extends FrameLayout {
	
	private static final int LENGTH_OF_WEEK = 7;
	Calendar currentDate;
	
	private View view; //this view
	private ListView weeksListView; //listview that contais all weeks in month

	private ViewGroup mDayNamesHeader; //header that holds week days names

	

	public CalendarViewRewrite(Context context, AttributeSet attrs)
	 {
	   super(context, attrs);
	 
	   LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	   view = layoutInflater.inflate(R.layout.calendar_view,this);
	   
	   currentDate = Calendar.getInstance();
//	   TODO Justas: laikinas hardcode testavimui - reik sukurt metoda, kuris leistu uzsetint
//	   kokia nori diena ir iskviestu this.updateView();
	   setFirstDayofWeek(Calendar.MONDAY); 
	   
	   weeksListView = (ListView) view.findViewById(R.id.list);
       mDayNamesHeader = (ViewGroup) view.findViewById(R.id.day_names);
	   
	  updateView();
	 }

	private void setFirstDayofWeek(int value) {
		currentDate.setFirstDayOfWeek(value);
		
	}

	private void updateView() {
		updateWeekTitles();
		updateWeeksContent();
		
	}


	private void updateWeekTitles() {
		int firstDayOfWeek = currentDate.getFirstDayOfWeek();

		String[] labels = getContext().getResources().getStringArray(R.array.week_days_title);
		//array that holds week days label	
		String[] mDayLabels = new String[LENGTH_OF_WEEK];
		
        for (int i = firstDayOfWeek; 
        		i < firstDayOfWeek + currentDate.getActualMaximum(Calendar.DAY_OF_WEEK);
        		i++) {
            int calendarDay = (i > Calendar.SATURDAY) ? i - Calendar.SATURDAY : i;
            mDayLabels[i - firstDayOfWeek] = labels[calendarDay - 1];
        }
		
        TextView label = (TextView) mDayNamesHeader.getChildAt(0);
        label.setText(R.string.week_title);
        
        for (int i = 0; i < LENGTH_OF_WEEK; i ++){
        	label = (TextView) mDayNamesHeader.getChildAt(i + 1);
        	label.setText(labels[i]);
        }
        
		
	}

	private void updateWeeksContent() {
//		TODO
		System.out.println("LOPISKAS METODAS");
		
	}
   
}