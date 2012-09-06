package com.groupagendas.groupagenda.calendar.agenda;

import java.util.List;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AgendaDayAdapter;
import com.groupagendas.groupagenda.events.Event;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ListView;

public class AgendaFrame {
	private final LinearLayout frame;
	private final AgendaDayAdapter eventsAdapter;
	
	public AgendaFrame (LinearLayout frame, Context context, boolean showEventTime){
		this.frame = frame;
		eventsAdapter = new AgendaDayAdapter(context, null, showEventTime);
		((ListView)frame.findViewById(R.id.agenda_day_entries)).setAdapter(eventsAdapter);
	}
	
	public AgendaFrame(LinearLayout frame, Context context) {
		this(frame, context, true);
	}

	public void setEventList (List<Event> list){
		eventsAdapter.setList(list);
	}

	public LinearLayout getDayContainer() {
		return frame;
	}

	public void UpdateList() {
		eventsAdapter.notifyDataSetChanged();		
	}
	
}
