package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

public class YearViewEventsAdapter extends AbstractAdapter<Event> {

	public YearViewEventsAdapter(Context context, List<Event> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.calendar_year_event_item, null);
        }
		final Event event = list.get(i);
		Button button = (Button) view; 
		String title = event.toString();
		button.setText(title);
		button.setOnClickListener(new EventActivityOnClickListener(context, event));
		return button;
	}

}
