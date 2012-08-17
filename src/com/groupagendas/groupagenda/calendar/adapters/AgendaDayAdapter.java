package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

public class AgendaDayAdapter extends AbstractAdapter<Event> {

	public AgendaDayAdapter(Context context, List<Event> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.calendar_agenda_entry, null);
        }
		final Event event = list.get(i);
		TextView tmp = (TextView) view;
		tmp.setText(event.title);
//		tmp.setTextAppearance(context, R.style.dayView_hourEvent_firstColumn_entryText);
		tmp.setOnClickListener(new EventActivityOnClickListener(context, event));
		return tmp;
	}

}
