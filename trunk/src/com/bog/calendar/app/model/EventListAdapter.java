package com.bog.calendar.app.model;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.utils.DateTimeUtils;

public class EventListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<Event> list;
    private DateTimeUtils dt;

    public EventListAdapter(Context context, List<Event> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
        dt = new DateTimeUtils(context);
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.calendar_event_row, null);
        }
        final Event event = list.get(i);
        TextView startTime = (TextView) view.findViewById(R.id.start_time);
        TextView endTime = (TextView) view.findViewById(R.id.end_time);
        TextView descView = (TextView) view.findViewById(R.id.decription);
        startTime.setText(dt.formatTime(event.startCalendar.getTimeInMillis()));
        endTime.setText(dt.formatTime(event.endCalendar.getTimeInMillis()));
        descView.setText(event.title);
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
        ImageView pointView = (ImageView) view.findViewById(R.id.row_point);
        GradientDrawable drawable = (GradientDrawable) pointView.getDrawable();
        
        if(event.color != null && !event.color.equals("null") && event.color.length() > 1){
        	try {
        		drawable.setColor(Integer.parseInt(event.color.replace("#", ""), 16)+0xFF000000);
			} catch (Exception e) {
				drawable.setColor(Color.GRAY);
			}
        	
        }else{
        	drawable.setColor(Color.GRAY);
        }
        
        if(event.icon != null && !event.icon.equals("null") && !event.icon.equals("") ){
        	ImageView iconView = (ImageView) view.findViewById(R.id.row_icon);
            int iconId = context.getResources().getIdentifier(event.icon, "drawable", "com.groupagendas.groupagenda");
    		iconView.setImageResource(iconId);
        }
        
        return view;
    }
}
