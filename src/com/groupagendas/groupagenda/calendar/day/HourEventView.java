package com.groupagendas.groupagenda.calendar.day;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class HourEventView extends FrameLayout {
	private final Event event;
	private View content;
	private TextView title;
	private TextView timeText;
	private ImageView icon;
	
	
	
	
	

	public HourEventView(Context context, Event e) {
		super(context);
		this.event = e;
		setId(e.event_id);
		LayoutInflater.from(context).inflate(R.layout.calendar_dayview_hourevent_entry, this);
		title = (TextView) this.findViewById(R.id.hour_event_title);
		title.setText(e.title);
		if (e.color == "null") e.color = "CC6600";
		this.setBackgroundColor(Color.parseColor("#" + e.color));
		System.out.println("showing event: " + e.title);
		
//		if (this.getChildCount() > 0) {
//			container.setB
//			eventLayout.getChildAt(0).set
//		}
//		
//		if (!event.color.equalsIgnoreCase("null")){
//			sd.setColor(Color.parseColor("#BF" + event.color));
//			sd.setStroke(1, Color.parseColor("#" + event.color));
//		}else {
//			sd.setColor(context.getResources().getColor(R.color.defaultAllDayEventColor));
//		}

		
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
