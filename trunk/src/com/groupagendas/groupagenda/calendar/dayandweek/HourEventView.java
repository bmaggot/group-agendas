package com.groupagendas.groupagenda.calendar.dayandweek;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;


@SuppressLint("ParserError")
public class HourEventView extends RelativeLayout {
	private final Event event;
	private final int TIME_TEXT_ID = 1;
	private TextView title;
	private TextView timeText;
	private ImageView icon;
	boolean usesAMPM;



	public HourEventView(Context context, Event e, boolean usesAMPM, boolean showEventIcon, boolean showSingleLine) {
		super(context);
		
		this.usesAMPM = usesAMPM;
		
		View titleHolder = LayoutInflater.from(context).inflate(R.layout.calendar_dayview_hour_event_icontitle_holder, null);
		GradientDrawable sd = (GradientDrawable)context.getResources().getDrawable(R.drawable.calendar_dayview_secondcolumn_entrybackground);
		this.event = e;
		setId(e.hashCode());
		
		 LayoutParams lp;
		
		
		Calendar startTime  = (Calendar) e.getStartCalendar().clone();
		Calendar endTime = e.getEndCalendar();
		
		SimpleDateFormat df;
//		Set Event start time textView
		timeText = new TextView(getContext());
		if (usesAMPM){
			df = new SimpleDateFormat(getContext().getString(R.string.time_format_AMPM));
		}else{
			df = new SimpleDateFormat(getContext().getString(R.string.time_format));
		}
		
		timeText.setText(df.format(startTime.getTime()));
		timeText.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_timeText);
		timeText.setId(TIME_TEXT_ID);
		
		this.addView(timeText);
		
//		Set event TITLE
		title = (TextView) titleHolder.findViewById(R.id.hour_event_title);
		title.setText(e.getTitle());
		title.setSingleLine(showSingleLine);
//		title.setLayoutParams(lp);
	
		
		
//		set event ICON
		icon = (ImageView) titleHolder.findViewById(R.id.hour_event_icon);
		
		
		 if (e.getIcon().length() <= 0 || !showEventIcon){
				icon.setVisibility(GONE);
			}else{
				
				
				icon.setImageResource(e.getIconId(getContext()));
			}
		
		lp = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
//		CHANGE LAYOUT TO ONE LINE IF THERE IS half-hour event
//		WARNING: START TIME NOW LOSES ITS CORRECT VALUE ;)
		int layoutPadding;
		startTime.add(Calendar.MINUTE, 60);
		if (startTime.before(endTime)) {
			lp.addRule(RelativeLayout.BELOW, TIME_TEXT_ID);
			 layoutPadding= getPixels(5);
			 title.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_entryText);
			
		}else {
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			timeText.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_timeText_small);
			layoutPadding= getPixels(2);
			title.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_entryText_small);
		}
		
		this.addView(titleHolder, lp);
		this.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding);
		
//		SET SHAPE AND COLOR
//		if (e.color == null) e.color = "null";
//		
//		if (!e.color.equalsIgnoreCase("null")){
			sd.setColor(Color.parseColor("#BF" + e.getColor()));
			sd.setStroke(1, Color.parseColor("#" + e.getColor()));
//		}else {
//			sd.setColor(context.getResources().getColor(R.color.defaultHourEventColorTransparent));
//		}
		this.setBackgroundDrawable(sd);
		
//		Add listener
		this.setOnClickListener(new EventActivityOnClickListener(getContext(), event));
	}
	
	

	public void setDimensionsDIP (int widthDIP, int heightDIP){
		this.setLayoutParams(new LayoutParams(getPixels(widthDIP), getPixels(heightDIP)));

	}
	
	public void setDimensions (int width, int height){
		this.setLayoutParams(new LayoutParams(width, height));

	}

	private int getPixels(int dipValue){
        Resources r = getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, 
        r.getDisplayMetrics());
        return px;
}
	public void setStartTime (Calendar startTime){
		SimpleDateFormat df = new SimpleDateFormat(getContext().getString(R.string.time_format));
		timeText.setText(df.format(startTime.getTime()));
	}


	
}