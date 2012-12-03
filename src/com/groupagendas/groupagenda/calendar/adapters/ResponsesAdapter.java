package com.groupagendas.groupagenda.calendar.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.EventActivityOnClickListener;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.ContactInfoActivity;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.ResponsesActivity;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ResponsesAdapter extends AbstractAdapter<JSONObject> {
	
	ResponsesActivity context;

	public ResponsesAdapter(ResponsesActivity context, List<JSONObject> list) {
		super(context, list);
		this.context = context;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.responses_thread, null);
		}
		
		final JSONObject responsesThread = (JSONObject) this.getItem(i);
		
		TextView contact = (TextView) view.findViewById(R.id.responses_contact);
		try {
			contact.setText(responsesThread.getString("user_fullname"));
		} catch (JSONException e) {
			Log.e("ResponsesAdapter", "Failed getting contact.");
		}
		
		contact.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent contactIntent = new Intent(context, ContactInfoActivity.class);
				try {
					contactIntent.putExtra("contactId", responsesThread.getInt("contact_id"));
				} catch (JSONException e) {
					Log.e("ResponsesAdapter", "Failed getting contact id.");
				}
				context.startActivity(contactIntent);
			}
		});
		
		TextView eventTitle = (TextView) view.findViewById(R.id.responses_event_title);
		try {
			eventTitle.setText(responsesThread.getString("event_title"));
		} catch (JSONException e) {
			Log.e("ResponsesAdapter", "Failed getting event title.");
		}
		
		String deleted = "";
		try {
			deleted = responsesThread.getString("deleted");
		} catch (JSONException e2) {
			Log.e("ResponsesAdapter", "Failed getting deleted feald.");
		}
		
		if(deleted.contentEquals("null")){
			Event event = new Event();;
			try {
				event = EventManagement.getEventFromLocalDb(context, responsesThread.getLong("event_id"), EventManagement.ID_EXTERNAL);
			} catch (JSONException e1) {
				Log.e("ResponsesAdapter", "Failed getting event id.");
			}
			eventTitle.setOnClickListener(new EventActivityOnClickListener(context, event));
			eventTitle.setTypeface(null, Typeface.BOLD);
		}
		
		TextView eventDate = (TextView) view.findViewById(R.id.responses_event_date);
		String temp ="";
		try {
			temp = responsesThread.getString("time_start");
		} catch (JSONException e) {
			Log.e("ResponsesAdapter", "Failed getting event start time.");
		}
		
		Calendar tempCal = Utils.stringToCalendar(context, temp, DataManagement.SERVER_TIMESTAMP_FORMAT);
		DateTimeUtils dateTimeUtils = new DateTimeUtils(context);
		eventDate.setText(dateTimeUtils.formatDate(tempCal));
		
		TextView responsesDate = (TextView) view.findViewById(R.id.responses_date);
		TextView responsesTime = (TextView) view.findViewById(R.id.responses_time);
		try {
			temp = responsesThread.getString("time");
		} catch (JSONException e) {
			Log.e("ResponsesAdapter", "Failed getting respones time.");
		}
		
		Account acc = new Account(context);
		tempCal = Utils.stringToCalendar(context, temp, DataManagement.SERVER_TIMESTAMP_FORMAT);
		tempCal.clear(Calendar.DST_OFFSET);
		tempCal.setTimeZone(TimeZone.getTimeZone("Europe/London"));
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone(acc.getTimezone()));
		sdf.applyPattern(DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
		responsesDate.setText(dateTimeUtils.formatDate(tempCal));
		responsesTime.setText(dateTimeUtils.formatTime(tempCal));
		
		TextView responses_title = (TextView) view.findViewById(R.id.responses_title);
		try {
			temp = responsesThread.getString("action_type");
		} catch (JSONException e) {
			Log.e("ResponsesAdapter", "Failed getting respones action type.");
		}
		
		if(temp.contentEquals("0")){
			responses_title.setText(R.string.responses_type_0);
		} else {
			if(temp.contentEquals("1")){
				responses_title.setText(R.string.responses_type_1);
			} else {
				if(temp.contentEquals("2")){
					responses_title.setText(R.string.responses_type_2);
				} else {
					if(temp.contentEquals("5")){
						responses_title.setText(R.string.responses_type_5);
					} else {
						if(temp.contentEquals("6")){
							responses_title.setText(R.string.responses_type_6);
						} else {
							if(temp.contentEquals("7")){
								responses_title.setText(R.string.responses_type_7);
							} else {
								if(temp.contentEquals("8")){
									responses_title.setText(R.string.responses_type_8);
								} 
							}
						}
					}
				}
			} 
		}
		view.setClickable(false);
		view.setEnabled(false);
		
		return view;
	}

}
