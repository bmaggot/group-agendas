package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HourListAdapter extends AbstractAdapter<String> {

	public HourListAdapter(Context context, List<String> list) {
		super(context, list);
	}
@Override
	public boolean isEnabled(int pos){
		return false;
	}
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = new TextView(context);
        }
		final String title = list.get(i);
		TextView tmp = (TextView) view;
		tmp.setText(title);
		tmp.setTextAppearance(context, R.style.dayView_hourEvent_firstColumn_entryText);
		return tmp;
	}

}
