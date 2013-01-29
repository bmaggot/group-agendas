package com.groupagendas.groupagenda.calendar.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class MonthAdapter extends AbstractAdapter<Event> {
	
	public static final String SERVER_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

	SimpleDateFormat timeFormat;
	TreeMap<String, ArrayList<Event>> sortedEvents;
	Calendar selectedDate;
	float density;

	public MonthAdapter(Context context, List<Event> list) {
		super(context, list);
		setAMPM(false);
	}
	
	public MonthAdapter(Context context, List<Event> list, boolean setAMPM) {
		super(context, list);
		setAMPM(setAMPM);
	}
	
	public MonthAdapter(Context context, List<Event> list, boolean setAMPM, TreeMap<String, ArrayList<Event>> sortedEvents) {
		super(context, list);
		setAMPM(setAMPM);
		this.sortedEvents = sortedEvents;
	}
	
	public MonthAdapter(Context context, List<Event> list, boolean setAMPM, TreeMap<String, ArrayList<Event>> sortedEvents, Calendar selectedDate) {
		super(context, list);
		setAMPM(setAMPM);
		this.sortedEvents = sortedEvents;
		this.selectedDate = selectedDate;
		this.density = context.getResources().getDisplayMetrics().density;
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
	
	public void setSelectedDate (Calendar date, TreeMap<String, ArrayList<Event>> sortedEvents) {
		this.selectedDate = date;
		this.sortedEvents = sortedEvents; 
		notifyDataSetChanged();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null){
			view = mInflater.inflate(R.layout.calendar_month_event_list_entry, null);
		}
		Object item = getItem(i);
		if (item instanceof Event){
			Event event = (Event) item;
			int circlePx = Math.round(12*density);
			
			ArrayList<Event> events;
			boolean isYesterday = false;
			boolean isTomorrow = false;
			
			ImageView icon = (ImageView) view.findViewById(R.id.month_entry_icon_placeholder);
			ImageView colourBubble = (ImageView) view.findViewById(R.id.month_entry_color_placeholder);
			TextView startTime = (TextView) view.findViewById(R.id.month_entry_start);
			TextView endTime = (TextView) view.findViewById(R.id.month_entry_end);
			
			String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate.getTime());
			Calendar date = Utils.stringToCalendar(context, dayStr + " 00:00:00", SERVER_TIMESTAMP_FORMAT);
			
			colourBubble.setImageBitmap(DrawingUtils.getCircleBitmap(context, circlePx, circlePx, event.getDisplayColor(), false));
//			colourBubble.setImageBitmap(DrawingUtils.getColoredRoundRectangle(context, 20, event.getDisplayColor(), true));

			if (sortedEvents != null) {
				date.add(Calendar.DAY_OF_YEAR, -1);
				events = TreeMapUtils.getEventsFromTreemap(date, sortedEvents);
				if (events != null) {
					for (Event e : events) {
						if (event.getEvent_id() == e.getEvent_id() && event.getTitle().equals(e.getTitle())
								&& (event.getEndCalendar().getTime().toString().equals(e.getEndCalendar().getTime().toString()))
								&& (event.getStartCalendar().getTime().toString().equals(e.getStartCalendar().getTime().toString()))) {
							isYesterday = true;
							break;
						}
					}
				}
				
				date.add(Calendar.DAY_OF_YEAR, 2);
				events = TreeMapUtils.getEventsFromTreemap(date, sortedEvents);
				if (events != null) {
					for (Event e : events) {
						if (event.getEvent_id() == e.getEvent_id() && event.getTitle().equals(e.getTitle()) 
								&& (event.getEndCalendar().getTime().toString().equals(e.getEndCalendar().getTime().toString()))
								&& (event.getStartCalendar().getTime().toString().equals(e.getStartCalendar().getTime().toString()))) {
							isTomorrow = true;
							break;
						}
					}
				}
			}
			
			if (isYesterday && isTomorrow || event.isBirthday() || event.is_all_day()) {
				ListView.LayoutParams lParams = new ListView.LayoutParams(LayoutParams.FILL_PARENT, Math.round(40*density));
				view.setLayoutParams(lParams);
				
				startTime.setText(R.string.all_day);
				endTime.setText("");
				
				endTime.setVisibility(View.GONE);
			} else {
				if (isYesterday) {
					startTime.setText(R.string.three_dots);
				} else {
					startTime.setText(timeFormat.format(event.getStartCalendar().getTime()));
				}
				
				if (isTomorrow) {
					endTime.setVisibility(View.VISIBLE);
					endTime.setText(R.string.three_dots);
				} else {
					endTime.setVisibility(View.VISIBLE);
					endTime.setText(timeFormat.format(event.getEndCalendar().getTime()));
				}
			}
			
			if (event.getIcon().length() <= 0) {
				icon.setVisibility(View.GONE);
			}else{
				int iconIdValue = event.getIconId(getContext());
				
				if (iconIdValue > 0) {
					icon.setVisibility(View.VISIBLE);
					icon.setImageResource(event.getIconId(getContext()));
				} else {
					icon.setVisibility(View.GONE);
				}
			}
			
			TextView title = (TextView) view.findViewById(R.id.month_entry_title);
			title.setText(event.getTitle());
			
			view.setOnClickListener(new EventActivityOnClickListener(getContext(), event));
		}
		return view;
	}

}
