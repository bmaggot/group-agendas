package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class HourEventsAdapter extends AbstractAdapter {

	public HourEventsAdapter(Context context, List<Event> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
//           TODO view = mInflater.inflate(R.layout.calendar_day_entry, null);
        }
		final Event event = list.get(i);
		
		return view;
	}

}
