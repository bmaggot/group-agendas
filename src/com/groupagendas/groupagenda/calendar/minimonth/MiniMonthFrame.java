package com.groupagendas.groupagenda.calendar.minimonth;

import java.util.List;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.MiniMonthDayAdapter;
import com.groupagendas.groupagenda.events.Event;

public class MiniMonthFrame {
	private final LinearLayout frame;
	private final MiniMonthDayAdapter eventsAdapter;
	
	public MiniMonthFrame (LinearLayout frame, Context context){
		this.frame = frame;
		eventsAdapter = new MiniMonthDayAdapter(context, null);
		((ListView)frame.findViewById(R.id.agenda_day_entries)).setAdapter(eventsAdapter);
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
