package com.groupagendas.groupagenda.calendar.listnsearch;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;


public class ListnSearchView extends LinearLayout {

	private StandardArrayAdapter arrayAdapter;

	private SectionListAdapter sectionAdapter;

	private SectionListView listView;

	private LayoutInflater mInflater;
	
	private EditText searchField;
	
	SectionListItem[] eventsArray;

	private String[] weekDayNames;

	private String[] monthNames;


	public ListnSearchView(Context context) {
		this(context, null);
	}
	
	
public ListnSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);
		weekDayNames = context.getResources().getStringArray(R.array.week_days_short);
		monthNames = context.getResources().getStringArray(R.array.month_names);
	
	}


public void init(){
	
	listView = (SectionListView) findViewById(R.id.section_list_view);
	new GetEventsInfoTask().execute();
	listView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
	searchField = (EditText) findViewById(R.id.listnsearch_search);
	searchField.addTextChangedListener(new TextWatcher() {
		
		String prevEntry;
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			prevEntry = s.toString();			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if (arrayAdapter != null && !prevEntry.equalsIgnoreCase(s.toString())){
				arrayAdapter.setList(filterEvents(s.toString(), eventsArray));
			}
		}
	});
	
}

private SectionListItem[] filterEvents (String filterString, SectionListItem[] eventsArray){
	ArrayList<SectionListItem> tmpList = new ArrayList<SectionListItem>();
	
	for (int i = 0; i < eventsArray.length; i++){
		String eventTitle = eventsArray[i].item.toString();
		if (eventTitle.contains(filterString))tmpList.add(eventsArray[i]);
	}
			

	
	

	return tmpList.toArray(new SectionListItem[tmpList.size()]);
	
}


	


	private class StandardArrayAdapter extends ArrayAdapter<SectionListItem> {

		private SectionListItem[] items;
		private SimpleDateFormat df;
//		Set Event start time textView
		

		public StandardArrayAdapter(final Context context, final int textViewResourceId, final SectionListItem[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
			if (CalendarSettings.isUsing_AM_PM()){
				df = new SimpleDateFormat(getContext().getString(R.string.time_format_AMPM));
			}else{
				df = new SimpleDateFormat(getContext().getString(R.string.time_format));
			}
		}		

		public void setList(SectionListItem[] items){
			this.items = items;
			this.notifyDataSetChanged();
		}
		
		@Override
		public int getCount(){
			return items.length;
		}
		
		@Override
		public SectionListItem getItem (int i){
			return items[i];	
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = vi.inflate(R.layout.calendar_listnsearch_list_view, null);
			}
			view.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
			final SectionListItem currentItem = items[position];
			if (currentItem != null) {
				
				final TextView textView = (TextView) view.findViewById(R.id.example_text_view);
				if (textView != null) {
					textView.setText(currentItem.item.toString());
				}
				
				
				
				if (currentItem.item instanceof Event){
					
					
					Event event = (Event) currentItem.item;
					TextView timeText = (TextView) view.findViewById(R.id.listnsearch_event_time_placeholder);
					timeText.setText(df.format(event.getStartCalendar().getTime()));
					ImageView bubble = (ImageView) view
							.findViewById(R.id.listnsearch_entry_color_placeholder);

					
					bubble.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundRectangle(getContext(), 15, event, true)));

					ImageView icon = (ImageView) view
							.findViewById(R.id.listnsearch_entry_icon_placeholder);
					if (event.hasIcon()){
						icon.setBackgroundResource(event.getIconId(getContext()));
						icon.setVisibility(View.VISIBLE);
					}else{
						icon.setVisibility(GONE);
					}
					
					
					view.setOnClickListener(new EventActivityOnClickListener(getContext(), event));}
			}
			
			return view;
		}
	}

	private class GetEventsInfoTask extends AsyncTask<Void, Integer, Void> {
		private Context context = ListnSearchView.this.getContext();
		protected Calendar listStartDate = Utils.createNewTodayCalendar();
		protected TreeMap<Calendar, ArrayList<Event>> sortedEvents;
		/**
		 * @author justinas.marcinka@gmail.com
		 * Returns event projection in: id, color, icon, title, start and end calendars. Other fields are not initialized
		 * @param date
		 * @return
		 */
		private ArrayList<Event>getEventProjectionsForDisplay(Calendar date){
			ArrayList<Event> list = new ArrayList<Event>();
			String[] projection = {
					EventsProvider.EMetaData.EventsMetaData._ID,
					EventsProvider.EMetaData.EventsMetaData.E_ID,
					EventsProvider.EMetaData.EventsMetaData.COLOR,
					EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS,
					EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS,
					EventsProvider.EMetaData.EventsMetaData.ICON,
					EventsProvider.EMetaData.EventsMetaData.TITLE,
					};
			Cursor result = EventManagement.createEventProjectionByDateFromLocalDb(context, projection, date, 0, EventManagement.TM_EVENTS_FROM_GIVEN_DATE, null, true);
			if (result.moveToFirst()) {
				while (!result.isAfterLast()) {
					Event eventProjection = new Event();
					eventProjection.setInternalID(result.getLong(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData._ID)));
					eventProjection.setEvent_id(result.getInt(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.E_ID)));
					eventProjection.setTitle(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TITLE)));
					eventProjection.setIcon(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.ICON)));
					eventProjection.setColor(result.getString(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.COLOR)));
					String user_timezone = CalendarSettings.getTimeZone();
					long timeinMillis = result.getLong(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
					eventProjection.setStartCalendar(Utils.createCalendar(timeinMillis, user_timezone));
					timeinMillis = result.getLong(result.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
					eventProjection.setEndCalendar(Utils.createCalendar(timeinMillis, user_timezone));
					list.add(eventProjection);
					result.moveToNext();
				}
			}
			result.close();
			return list ;
			
		}

		@Override
		protected Void doInBackground(Void... params) {
			sortedEvents = TreeMapUtils.sortEvents(getEventProjectionsForDisplay(listStartDate));
			return null;
		}

		protected void setEventsList(Calendar date) {
			
			ArrayList<SectionListItem> list = new ArrayList<SectionListItem>();
			String section;
		while (!date.after(sortedEvents.lastKey())){
			
			section = weekDayNames[date.get(Calendar.DAY_OF_WEEK) - 1];
			section += ", ";
			section += date.get(Calendar.DAY_OF_MONTH);
			section += " ";
			section += monthNames[date.get(Calendar.MONTH)];
			section += " ";
			section += date.get(Calendar.YEAR);
					
			for (Event e : TreeMapUtils.getEventsFromTreemap(date, sortedEvents)){
				list.add(new SectionListItem(e, section));
			}
			
			date.add(Calendar.DATE, 1);	
		}
		eventsArray =  list.toArray(new SectionListItem[list.size()]);
		
	}
		
		protected void onPostExecute(Void result) {
			setEventsList(listStartDate);
			arrayAdapter = new StandardArrayAdapter(getContext(), R.id.agenda_entry_title_placeholder, eventsArray);
			sectionAdapter = new SectionListAdapter(mInflater, arrayAdapter);
			if(listView != null)
				listView.setAdapter(sectionAdapter);
			
		}


	}	


}