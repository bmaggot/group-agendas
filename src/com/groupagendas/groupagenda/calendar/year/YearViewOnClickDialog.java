/**
 * This is dialog that is shown when user clicks on one of days in Year View. 
 * Also this dialog acts as OnClickListener on new event button. 
 * @author justinas.marcinka@gmail.com
 *
 */
package com.groupagendas.groupagenda.calendar.year;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.YearViewEventsAdapter;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.NewEventActivity;

public class YearViewOnClickDialog extends Dialog implements android.view.View.OnClickListener{
	
	ListView eventsList;
	Button newEventButton;
	YearViewEventsAdapter eventsAdapter;
	Context context;
	Calendar selectedDate;

	/**
	 * Constructor that should be used to create this dialog with default dialog style
	 * @param context Context activity for this dialog
	 * @param selectedDate Date that is used to fill dialog events list and title
	 */
	public YearViewOnClickDialog(Context context, Calendar selectedDate) {
		this (context, selectedDate, 0);
		
		
	}

	/**
	 * Constructor that should be used to create this dialog with custom dialog style
	 * @param context Context activity for this dialog
	 * @param selectedDate Date that is used to fill dialog events list and title
	 * @param styleResId custom dialog style
	 */
	public YearViewOnClickDialog(Context context, Calendar selectedDate,
			int styleResId) {
		super(context, styleResId);
		this.context = context;
		this.setCanceledOnTouchOutside(true);
		this.setContentView(R.layout.calendar_year_event_list);
		this.selectedDate = selectedDate;
		this.setTitle(CalendarSettings.dateFormatter.format(selectedDate.getTime()));
		
		
		eventsList = (ListView) findViewById(R.id.year_event_list);
		eventsAdapter = new YearViewEventsAdapter(context, Data.getEventByDate(selectedDate));
		eventsList.setAdapter(eventsAdapter);
		eventsAdapter.notifyDataSetChanged();
		
		newEventButton = (Button) findViewById(R.id.year_new_event_button);
		newEventButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		Intent intent = new Intent(context, NewEventActivity.class);
		intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR, CalendarSettings.dateFormatter.format(selectedDate.getTime()));
		context.startActivity(intent);
		
	}

}
