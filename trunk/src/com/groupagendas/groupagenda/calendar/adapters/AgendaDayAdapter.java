package com.groupagendas.groupagenda.calendar.adapters;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.DrawingUtils;

public class AgendaDayAdapter extends AbstractAdapter<Event> {
	boolean showTime = false;
	SimpleDateFormat hoursFormatter;
	
	private int bubbleHeightDP = 15;
	private int colouredRectangleMarginsDP = 2;
	
	public AgendaDayAdapter(Context context, List<Event> list) {
		this(context, list, true);
	}
	public AgendaDayAdapter(Context context, List<Event> list, boolean showTime) {
		super(context, list);
		this.showTime = showTime;
		
		if (CalendarSettings.isUsing_AM_PM()){
			hoursFormatter = new SimpleDateFormat(getContext().getString(R.string.time_format_AMPM));
		}else{
			hoursFormatter = new SimpleDateFormat(getContext().getString(R.string.time_format));
		}
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.calendar_agenda_entry, null);
        }

		final Event event = list.get(i);
		TextView text = (TextView) (view.findViewById(R.id.agenda_entry_title_placeholder));	
		text.setText(event.getTitle());
		
		TextView timeText = (TextView) (view.findViewById(R.id.agenda_entry_time_placeholder));
		if (showTime) {
			timeText.setVisibility(View.VISIBLE);
			timeText.setText(hoursFormatter.format(event.getStartCalendar().getTime()));
			
		}else{
			timeText.setVisibility(View.GONE);
		}
		
		
		view.setOnClickListener(new EventActivityOnClickListener(context, event));
		
		ImageView bubble = (ImageView) view.findViewById(R.id.agenda_entry_color_placeholder);
		
		bubble.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundRectangle(getContext(), bubbleHeightDP, event.getDisplayColor(), true)));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int margins = DrawingUtils.convertDPtoPX(colouredRectangleMarginsDP );
		params.setMargins(margins, margins, 0, 0);
		bubble.setLayoutParams(params);
		
		
		ImageView icon = (ImageView) view.findViewById(R.id.agenda_entry_icon_placeholder);
		if (event.hasIcon()){
			icon.setVisibility(View.VISIBLE);
			icon.setImageResource(event.getIconId(getContext()));
		}else{
			icon.setVisibility(View.GONE);
		}
		return view;
		
	}

}
