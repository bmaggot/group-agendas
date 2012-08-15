package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AllDayEventsAdapter extends AbstractAdapter<Event> {

	public AllDayEventsAdapter(Context context, List<Event> list) {
		super(context, list);
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {


		if (view == null) {
            view = mInflater.inflate(R.layout.calendar_dayview_allday_listentry, null);
        }
		
		final Event event = list.get(i);
		TextView title = (TextView) view.findViewById(R.id.allday_eventtitle);
		title.setText(event.title);
		GradientDrawable sd = (GradientDrawable)context.getResources().getDrawable(R.drawable.calendar_dayview_secondcolumn_entrybackground);
		
		if (!event.color.equalsIgnoreCase("null")){
			sd.setColor(Color.parseColor("#BF" + event.color));
			sd.setStroke(1, Color.parseColor("#" + event.color));
		}else {
			sd.setColor(context.getResources().getColor(R.color.defaultAllDayEventColor));
		}
		
		title.setBackgroundDrawable(sd);
		
		
		view.setOnClickListener(new EventActivityOnClickListener(context, event));
		return view;
	}

}
