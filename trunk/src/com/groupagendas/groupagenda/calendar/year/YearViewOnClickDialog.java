package com.groupagendas.groupagenda.calendar.year;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.YearViewEventsAdapter;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class YearViewOnClickDialog extends Dialog implements android.view.View.OnClickListener{
	
	ListView eventsList;
	Button newEventButton;
	YearViewEventsAdapter eventsAdapter;
	Activity contextAxtivity;
	Calendar selectedDate;
	
	public YearViewOnClickDialog(Context context) {
		this(context, Utils.createNewTodayCalendar());
	}

	public YearViewOnClickDialog(Context context, Calendar selectedDate) {
		super(context);
		contextAxtivity = (Activity) context;
		this.setCanceledOnTouchOutside(true);
		this.setContentView(R.layout.calendar_year_event_list);
		this.selectedDate = selectedDate;
		
		eventsList = (ListView) findViewById(R.id.year_event_list);
		eventsAdapter = new YearViewEventsAdapter(context, Data.getEventByDate(selectedDate));
		eventsList.setAdapter(eventsAdapter);
		eventsAdapter.notifyDataSetChanged();
		
		newEventButton = (Button) findViewById(R.id.year_new_event_button);
		newEventButton.setOnClickListener(this);
		
		
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(contextAxtivity, NewEventActivity.class);
		intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR, CalendarSettings.dateFormatter.format(selectedDate.getTime()));
		contextAxtivity.startActivity(intent);
		
	}

}
