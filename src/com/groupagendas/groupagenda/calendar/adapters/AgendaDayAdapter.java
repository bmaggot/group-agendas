package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
			view = (LinearLayout)mInflater.inflate(R.layout.calendar_agenda_entry, null);
        }

		final Event event = list.get(i);
		TextView text = (TextView) (view.findViewById(R.id.agenda_entry_title_placeholder));
		text.setText(event.title);
		text.setOnClickListener(new EventActivityOnClickListener(context, event));
		
		ImageView bubble = (ImageView) view.findViewById(R.id.agenda_entry_icon_placeholder);
		
		String bubbletitle = "calendarbubble_" + event.color + "_";
		int imgID = getContext().getResources().getIdentifier(bubbletitle, "drawable", getContext().getPackageName());
		bubble.setImageResource(imgID);
		return view;
		
	}

}
