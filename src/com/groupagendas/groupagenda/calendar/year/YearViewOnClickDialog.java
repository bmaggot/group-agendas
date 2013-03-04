/**
 * This is dialog that is shown when user clicks on one of days in Year View. 
 * Also this dialog acts as OnClickListener on new event button. 
 * @author justinas.marcinka@gmail.com
 *
 */
package com.groupagendas.groupagenda.calendar.year;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.YearViewEventsAdapter;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

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
	 * @param tm 
	 * @param events Events list for this dialog
	 */
	public YearViewOnClickDialog(Context context, Calendar selectedDate, TreeMap<String, ArrayList<Event>> tm) {
		this (context, selectedDate, 0, tm);
		
		
	}

	/**
	 * Constructor that should be used to create this dialog with custom dialog style
	 * @param context Context activity for this dialog
	 * @param selectedDate Date that is used to fill dialog events list and title
	 * @param styleResId custom dialog style
	 * @param tm 
	 * @param events Events list for this dialog
	 */
	public YearViewOnClickDialog (final Context context, Calendar selectedDate,
			int styleResId, TreeMap<String, ArrayList<Event>> tm) {
		super(context, styleResId);
		this.context = context;
		this.setCanceledOnTouchOutside(true);
		this.setContentView(R.layout.calendar_year_event_list);
		this.selectedDate = selectedDate;
//		Utils.formatCalendar(selectedDate)
		DateTimeUtils dtu = new DateTimeUtils(context);
		this.setTitle(dtu.formatDate(selectedDate));
		
		
		eventsList = (ListView) findViewById(R.id.year_event_list);
//		TODO find out why this isn't working
//		eventsList.setOnItemClickListener(new OnItemClickListener() {
//			  @Override
//			  public void onItemClick(AdapterView<?> parent, View view,
//			    int position, long id) {
//			    Toast.makeText(context.getApplicationContext(),
//			      "Click ListItem Number " + position, Toast.LENGTH_LONG)
//			      .show();
//			  }
//			}); 
		
		
		
		eventsAdapter = new YearViewEventsAdapter(context, TreeMapUtils.getEventsFromTreemap(getContext(), selectedDate, tm, false));
		eventsList.setAdapter(eventsAdapter);
		eventsAdapter.notifyDataSetChanged();
		
		newEventButton = (Button) findViewById(R.id.year_new_event_button);
		newEventButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
			YearViewOnClickDialog.this.dismiss();
		
			Intent intent = new Intent(context, NewEventActivity.class);
			
			intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR, Utils.formatCalendar(selectedDate, DataManagement.SERVER_TIMESTAMP_FORMAT));
			
			context.startActivity(intent);
	}

//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		
//	}

}
