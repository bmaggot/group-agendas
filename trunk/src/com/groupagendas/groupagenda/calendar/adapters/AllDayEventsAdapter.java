package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AllDayEventsAdapter extends AbstractAdapter {

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
		
		if (!event.color.equalsIgnoreCase("null")){
//			TODO in future there should be title color also set
		System.out.println("spalva " + i + " : " + event.color);	
		title.setBackgroundColor(Color.parseColor("#" + event.color));
		}
		
		
		view.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
          	Intent intent = new Intent(context, EventActivity.class);
      		intent.putExtra("event_id", event.event_id);
      		intent.putExtra("type", event.type);
      		intent.putExtra("isNative", event.isNative);
      		
      		context.startActivity(intent);
          }
      });
		return view;
	}

}
