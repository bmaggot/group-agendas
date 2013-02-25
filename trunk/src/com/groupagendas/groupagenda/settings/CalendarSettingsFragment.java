package com.groupagendas.groupagenda.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.adapters.ChatThreadAdapter;
import com.groupagendas.groupagenda.events.EventsProvider;

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
		{
			final TextView gaEventsSwitch = (TextView) container.findViewById(R.id.ga_event_switch);
			if (gaEventsSwitch != null) {
				gaEventsSwitch.setBackgroundResource(account.getShow_ga_calendars() ?
						R.drawable.event_invite_people_button_notalone_c :
							R.drawable.event_invite_people_button_notalone);
				gaEventsSwitch.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EventsProvider.OUT_OF_DATE.set(true);
						if (account.getShow_ga_calendars()) {
							account.setShow_ga_calendars(false);
							gaEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
						} else {
							account.setShow_ga_calendars(true);
							gaEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_notalone_c);
						}
					}
				});
			}
		}
		{
			final TextView gaBirthDayEventsSwitch = (TextView) container.findViewById(R.id.birthday_event_switch);
			if (gaBirthDayEventsSwitch != null) {
				gaBirthDayEventsSwitch.setBackgroundResource(account.getShow_birthdays_calendars() ?
						R.drawable.event_invited_entry_last_background_c :
							R.drawable.event_invited_entry_last_background);
				gaBirthDayEventsSwitch.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EventsProvider.OUT_OF_DATE.set(true);
						if (account.getShow_birthdays_calendars()) {
							account.setShow_birthdays_calendars(false);
							gaBirthDayEventsSwitch.setBackgroundResource(R.drawable.event_invited_entry_last_background);
						} else {
							account.setShow_birthdays_calendars(true);
							gaBirthDayEventsSwitch.setBackgroundResource(R.drawable.event_invited_entry_last_background_c);
						}
					}
				});
			}
		}
		{
			final TextView nativeEventsSwitch = (TextView) container.findViewById(R.id.native_event_switch);
			if (nativeEventsSwitch != null) {
				nativeEventsSwitch.setBackgroundResource(account.getShow_native_calendars() ?
						R.drawable.event_invite_people_button_standalone_c :
							R.drawable.event_invite_people_button_standalone);
				nativeEventsSwitch.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EventsProvider.OUT_OF_DATE.set(true);
						if (account.getShow_native_calendars()) {
							account.setShow_native_calendars(false);
							nativeEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
						} else {
							account.setShow_native_calendars(true);
							nativeEventsSwitch.setBackgroundResource(R.drawable.event_invite_people_button_standalone_c);
						}
					}
				});
			}
		}
	}
}
