package com.groupagendas.groupagenda.calendar.day;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


@SuppressLint("ParserError")
public class HourEventView extends RelativeLayout {
	private final Event event;
	private final int TIME_TEXT_ID = 1;
	private TextView title;
	private TextView timeText;
	private ImageView icon;

	
	
	
	

	public HourEventView(Context context, Event e) {
		super(context);
		RelativeLayout.LayoutParams lp = (LayoutParams) this.getLayoutParams();
		int layoutPadding = getPixels(5);
		this.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding);
		View titleHolder = LayoutInflater.from(context).inflate(R.layout.calendar_dayview_hour_event_icontitle_holder, null);
		GradientDrawable sd = (GradientDrawable)context.getResources().getDrawable(R.drawable.calendar_dayview_secondcolumn_entrybackground);
		this.event = e;
		setId(e.event_id);
		
		 lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		
		Calendar startTime  = Utils.stringToCalendar(event.time_start, Utils.date_format);
		Calendar endTime = Utils.stringToCalendar(event.time_end, Utils.date_format);
		
//		Set Event start time textView
		timeText = new TextView(getContext());
		SimpleDateFormat df = new SimpleDateFormat(getContext().getString(R.string.hour_event_view_time_format));
		timeText.setText(df.format(startTime.getTime()));
		timeText.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_timeText);
		timeText.setId(TIME_TEXT_ID);
		
		this.addView(timeText);
		
//		Set event TITLE
		title = (TextView) titleHolder.findViewById(R.id.hour_event_title);
		title.setText(e.title);
		title.setTextAppearance(getContext(), R.style.dayView_hourEvent_secondColumn_entryText); // Va cia prasides ledas.
		
		
//		set event ICON
		icon = (ImageView) titleHolder.findViewById(R.id.hour_event_icon);
		if (e.icon.equalsIgnoreCase("null")){
			icon.setVisibility(GONE);
		}else{
			int imgID = getResources().getIdentifier(e.icon, "drawable", getContext().getPackageName());
			
			icon.setImageResource(imgID);
		}
		
		
//		CHANGE LAYOUT TO ONE LINE IF THERE IS half-hour event
//		WARNING: START TIME NOW LOSES ITS CORRECT VALUE ;)
		startTime.add(Calendar.MINUTE, 30);
		if (startTime.before(endTime)) {
			lp.addRule(RelativeLayout.BELOW, TIME_TEXT_ID);
		}else {
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
		
		this.addView(titleHolder, lp);
		
//		SET SHAPE AND COLOR
		if (!e.color.equalsIgnoreCase("null")){
			sd.setColor(Color.parseColor("#BF" + e.color));
			sd.setStroke(1, Color.parseColor("#" + e.color));
		}else {
			sd.setColor(context.getResources().getColor(R.color.defaultHourEventColorTransparent));
		}
		this.setBackgroundDrawable(sd);
		
//		Add listener
		
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), EventActivity.class);
	      		intent.putExtra("event_id", event.event_id);
	      		intent.putExtra("type", event.type);
	      		intent.putExtra("isNative", event.isNative);
	      		
	      		getContext().startActivity(intent);
				
			}
		});
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

	
}
