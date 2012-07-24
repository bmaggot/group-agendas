package com.groupagendas.groupagenda.calendar.day;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class HourEventView extends RelativeLayout {
	private Event event;
	private TextView title;
	private TextView timeText;
	private ImageView icon;
	
	
	
	
	public HourEventView(Context context){
		super (context);
		
	
	}

	public HourEventView(Context context, Event event, int id) {
		super(context);
		this.event = event;
		setId(id);
		LayoutInflater.from(context).inflate(R.layout.calendar_dayview_hourevent_entry, this);
		title = (TextView) this.findViewById(R.id.hour_event_title);
		title.setText(event.title);
		if (event.color == "null") event.color = "CC6600";
		this.setBackgroundColor(Color.parseColor("#" + event.color));
		System.out.println("showing event: " + event.title);
	
		// TODO Auto-generated constructor stub
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
