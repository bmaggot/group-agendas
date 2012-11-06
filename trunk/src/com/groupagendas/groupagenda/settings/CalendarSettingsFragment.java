package com.groupagendas.groupagenda.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.adapters.ChatThreadAdapter;

public class CalendarSettingsFragment extends Fragment {
	ChatThreadAdapter adapter;
	ViewGroup container;

	public static Fragment newInstance() {
		CalendarSettingsFragment f = new CalendarSettingsFragment();
		Bundle args = new Bundle();
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.filters, container, false);
		this.container = container;
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		final Account account = new Account(getActivity().getApplicationContext());
		final TextView gaEventsSwitch = (TextView) container.findViewById(R.id.ga_event_switch);
		if (gaEventsSwitch != null && account.getShow_ga_calendars()) {
			gaEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_standalone_c);
		} else if (gaEventsSwitch != null) {
			gaEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
		}
		if (gaEventsSwitch != null) {
			gaEventsSwitch.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (account.getShow_ga_calendars()) {
						account.setShow_ga_calendars(false);
						gaEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
					} else {
						account.setShow_ga_calendars(true);
						gaEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_standalone_c);
					}
				}
			});
		}
		final TextView nativeEventsSwitch = (TextView) container.findViewById(R.id.native_event_switch);
		if (nativeEventsSwitch != null && account.getShow_native_calendars()) {
			nativeEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_notalone_c);
		} else if (nativeEventsSwitch != null) {
			nativeEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
		}
		if (nativeEventsSwitch != null) {
			nativeEventsSwitch.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (account.getShow_native_calendars()) {
						account.setShow_native_calendars(false);
						nativeEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
					} else {
						account.setShow_native_calendars(true);
						nativeEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_notalone_c);
					}
				}
			});
		}
		
		final TextView birthDayEventsSwitch = (TextView) container.findViewById(R.id.birthday_event_switch);
		if(birthDayEventsSwitch != null && account.getShow_birthdays_calendars()){
			birthDayEventsSwitch.setBackgroundResource(R.drawable.event_invited_entry_last_background_c);
		} else if(birthDayEventsSwitch != null){
			birthDayEventsSwitch.setBackgroundResource(R.drawable.event_invited_entry_last_background);
		}
		if(birthDayEventsSwitch != null){
			birthDayEventsSwitch.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(account.getShow_birthdays_calendars()){
						account.setShow_birthdays_calendars(false);
						birthDayEventsSwitch.setBackgroundResource(R.drawable.event_invited_entry_last_background);
					} else {
						account.setShow_birthdays_calendars(true);
						birthDayEventsSwitch.setBackgroundResource(R.drawable.event_invited_entry_last_background_c);
					}
				}
			});
		}
	}

}
