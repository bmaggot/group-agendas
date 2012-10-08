package com.groupagendas.groupagenda.calendar.adapters;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

public class MonthAdapter extends AbstractAdapter<Event> {
	
	SimpleDateFormat timeFormat;


	public MonthAdapter(Context context, List<Event> list) {
		super(context, list);
		setAMPM(false);
	}
	public MonthAdapter(Context context, List<Event> list, boolean setAMPM) {
		super(context, list);
		setAMPM(setAMPM);
	}
	
	public void setAMPM(boolean usesAMPM) {

		if (usesAMPM) {
			timeFormat = new SimpleDateFormat(getContext().getString(
					R.string.time_format_AMPM));
		} else {
			timeFormat = new SimpleDateFormat(getContext().getString(
					R.string.time_format));
		}
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null){
			view = mInflater.inflate(R.layout.calendar_month_event_list_entry, null);
		}
		Object item = getItem(i);
		if (item instanceof Event){
			Event event = (Event) item;
			
			ImageView colourBubble = (ImageView) view.findViewById(R.id.month_entry_color_placeholder);
			colourBubble.setImageResource(event.getColorBubbleId(getContext()));
			
			TextView startTime = (TextView) view.findViewById(R.id.month_entry_start);
			startTime.setText(timeFormat.format(event.getStartCalendar().getTime()));
			
			TextView endTime = (TextView) view.findViewById(R.id.month_entry_end);
			endTime.setText(timeFormat.format(event.getEndCalendar().getTime()));
			
			ImageView icon = (ImageView) view.findViewById(R.id.month_entry_icon_placeholder);
			
			
			 if (event.getIcon().length() <= 0){
					icon.setVisibility(View.INVISIBLE);
				}else{
					icon.setImageResource(event.getIconId(getContext()));
				}
			
			TextView title = (TextView) view.findViewById(R.id.month_entry_title);
			title.setText(event.getTitle());
			
			view.setOnClickListener(new EventActivityOnClickListener(getContext(), event));
		}
		return view;
	}

}
