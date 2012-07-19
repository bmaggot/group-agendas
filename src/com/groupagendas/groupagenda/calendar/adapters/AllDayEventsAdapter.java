package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AllDayEventsAdapter extends AbstractAdapter {

	public AllDayEventsAdapter(Context context, List<Event> list) {
		super(context, list);
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
//		System.out.println("getview metodas" + i);

		if (view == null) {
            view = mInflater.inflate(R.layout.calendar_dayview_allday_listentry, null);
        }
		
		final Event event = list.get(i);
		TextView title = (TextView) view.findViewById(R.id.allday_eventtitle);
		title.setText(event.title);
		GradientDrawable sd = (GradientDrawable)context.getResources().getDrawable(R.drawable.calendar_dayview_secondcolumn_entrybackground);		
		if (!event.color.equalsIgnoreCase("null")){
//			TODO in future there should be title color also set
			sd.setColor(Color.parseColor("#" + event.color));
//		title.setBackgroundColor(Color.parseColor("#" + event.color));
		}else {
			sd.setColor(context.getResources().getColor(R.color.defaultAllDayEventColor));
		}
		title.setBackgroundDrawable(sd);
		
		
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
