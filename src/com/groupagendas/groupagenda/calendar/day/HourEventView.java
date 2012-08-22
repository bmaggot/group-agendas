package com.groupagendas.groupagenda.calendar.day;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.utils.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


@SuppressLint("ParserError")
public class HourEventView extends RelativeLayout {
	private final Event event;
	private final int TIME_TEXT_ID = 1;
	private TextView title;
	private TextView timeText;
	private ImageView icon;
	boolean usesAMPM;

	
	
	
	

	public HourEventView(Context context, Event e, boolean usesAMPM, boolean showEventIcon) {
		super(context);
		
		this.usesAMPM = usesAMPM;
		
		RelativeLayout.LayoutParams lp = (LayoutParams) this.getLayoutParams();
		
		View titleHolder = LayoutInflater.from(context).inflate(R.layout.calendar_dayview_hour_event_icontitle_holder, null);
		GradientDrawable sd = (GradientDrawable)context.getResources().getDrawable(R.drawable.calendar_dayview_secondcolumn_entrybackground);
		this.event = e;
		setId(e.hashCode());
		
		 lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		
		Calendar startTime  = (Calendar) e.startCalendar.clone();
		Calendar endTime = e.endCalendar;
		
		SimpleDateFormat df;
//		Set Event start time textView
		timeText = new TextView(getContext());
		if (usesAMPM){
			df = new SimpleDateFormat(getContext().getString(R.string.hour_event_view_time_format_AMPM));
		}else{
			df = new SimpleDateFormat(getContext().getString(R.string.hour_event_view_time_format));
		}
		
		timeText.setText(df.format(startTime.getTime()));
		timeText.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_timeText);
		timeText.setId(TIME_TEXT_ID);
		
		this.addView(timeText);
		
//		Set event TITLE
		title = (TextView) titleHolder.findViewById(R.id.hour_event_title);
		title.setText(e.title);
	
		
		
//		set event ICON
		icon = (ImageView) titleHolder.findViewById(R.id.hour_event_icon);
		if(e.icon == null) e.icon = "null";
		
		 if (e.icon.equalsIgnoreCase("null") || !showEventIcon){
				icon.setVisibility(GONE);
			}else{
				int imgID = getResources().getIdentifier(e.icon, "drawable", getContext().getPackageName());
				
				icon.setImageResource(imgID);
			}
		
		
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
		if (e.color == null) e.color = "null";
		
		if (!e.color.equalsIgnoreCase("null")){
			sd.setColor(Color.parseColor("#BF" + e.color));
			sd.setStroke(1, Color.parseColor("#" + e.color));
		}else {
			sd.setColor(context.getResources().getColor(R.color.defaultHourEventColorTransparent));
		}
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
		SimpleDateFormat df = new SimpleDateFormat(getContext().getString(R.string.hour_event_view_time_format));
		timeText.setText(df.format(startTime.getTime()));
	}


	
}
