package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Prefs;

public class EventActivity extends Activity {
	public static final int DEFAULT_EVENT_DURATION_IN_MINS = 30;
	public static final String EXTRA_STRING_FOR_START_CALENDAR = "strTime";
	
	public static ArrayList<Invited> newInvites;

	protected DateTimeUtils dtUtils;
	protected EditText descView;

	protected EditText countryView;
	protected EditText cityView;
	protected EditText streetView;
	protected EditText zipView;
	protected EditText timezoneView;
	
	protected EditText locationView;
	protected EditText gobyView;
	protected EditText takewithyouView;
	protected EditText costView;
	protected EditText accomodationView;
	protected Calendar startCalendar = Calendar.getInstance();
	protected Calendar endCalendar = Calendar.getInstance();
	protected EditText titleView;
	protected Button saveButton;
	protected ImageView iconView;
	protected ImageView colorView;
	
	protected LinearLayout addressPanel;
	protected LinearLayout addressLine;
	
	protected LinearLayout countrySpinnerBlock;
	protected LinearLayout cityViewBlock;
	protected LinearLayout streetViewBlock;
	protected LinearLayout zipViewBlock;
	protected LinearLayout timezoneSpinnerBlock;
	
	protected LinearLayout detailsPanel;
	protected LinearLayout detailsLine;
	
	protected LinearLayout locationViewBlock;
	protected LinearLayout gobyViewBlock;
	protected LinearLayout takewithyouViewBlock;
	protected LinearLayout costViewBlock;
	protected LinearLayout accomodationViewBlock;
	
	protected boolean addressPanelVisible = true;
	protected boolean detailsPanelVisible = true;

	protected EditText startView;
	protected Button startButton;

	protected EditText endView;
	protected Button endButton;

	protected final static int DIALOG_START = 0;
	protected final static int DIALOG_END = 1;

	protected String errorStr = "";
	protected static final int DIALOG_ERROR = 0;

	protected final static int REMINDER1 = 11;
	protected Calendar reminder1time;
	protected final static int REMINDER2 = 22;
	protected Calendar reminder2time;
	protected final static int REMINDER3 = 33;
	protected Calendar reminder3time;

	protected final static int ALARM1 = 111;
	protected Calendar alarm1time;
	protected final static int ALARM2 = 222;
	protected Calendar alarm2time;
	protected final static int ALARM3 = 333;
	protected Calendar alarm3time;

	protected Prefs prefs;

	protected ContentValues cv;

	protected RelativeLayout addressDetailsPanel;
	protected TextView addressTrigger;
	protected TextView detailsTrigger;
	protected ProgressBar pb;
	protected String selectedIcon = Event.DEFAULT_ICON;
	protected String selectedColor = Event.DEFAULT_COLOR;
	
	protected Event event;
	protected ArrayList<StaticTimezones> countriesList = null;
	protected CountriesAdapter countriesAdapter = null;
	protected TimezonesAdapter timezonesAdapter = null;
	protected InvitedAdapter invitedAdapter = null;
	protected LinearLayout invitedPersonList;
	protected int timezoneInUse = 0;
	protected Button inviteButton;
	protected LinearLayout invitesColumn;
	
	protected Account account;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		newInvites = null;
		
	}
	@Override
	protected void onResume() {
		super.onResume();		
	}

	protected Event setEventData(Event event) {

		if (timezoneInUse > 0) {
			event.setTimezone(countriesList.get(timezoneInUse).timezone);
		}
		
		event.setDescription(descView.getText().toString());
		// title
		event.setTitle(titleView.getText().toString());

		// calendars
		event.setStartCalendar(startCalendar);
		event.setEndCalendar(endCalendar);
		event.setModifiedMillisUtc(Calendar.getInstance().getTimeInMillis());

		if (timezoneInUse > 0)
		event.setCountry(countriesList.get(timezoneInUse).country_code);
		event.setZip(zipView.getText().toString());
		event.setCity(cityView.getText().toString());
		event.setStreet(streetView.getText().toString());
		event.setLocation(locationView.getText().toString());
		event.setGo_by(gobyView.getText().toString());
		event.setTake_with_you(takewithyouView.getText().toString());
		event.setCost(costView.getText().toString());
		event.setAccomodation(accomodationView.getText().toString());
		if (alarm1time != null) {
			event.setAlarm1(alarm1time);
		}
		if (alarm2time != null) {
			event.setAlarm2(alarm2time);
		}
		if (alarm3time != null) {
			event.setAlarm3(alarm3time);
		}
		event.setReminder1(reminder1time);
		event.setReminder2(reminder2time);
		event.setReminder3(reminder3time);
		event.setIcon(selectedIcon);
		event.setColor(selectedColor);
		return event;

	}

	public String setErrorStr(int testEvent) {
		switch (testEvent) {
		case 1: // no title set
			return getString(R.string.title_is_required);
		case 2: // no timezone set
			return getString(R.string.timezone_required);
		case 3: // calendar fields are null TODO add proper error texts.
			return getString(R.string.invalid_start_end_time);
		case 4: // event start is set after end
			return getString(R.string.invalid_start_end_time);
		case 5: // event duration is 0
			return getString(R.string.invalid_start_end_time);
		default:
			return getString(R.string.unknown_event_error);
		}
	}
	
	public void showAddressPanel() {
		addressPanelVisible = true;
		timezoneSpinnerBlock.setVisibility(View.VISIBLE);
		countrySpinnerBlock.setVisibility(View.VISIBLE);
		cityViewBlock.setVisibility(View.VISIBLE);
		streetViewBlock.setVisibility(View.VISIBLE);
		zipViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideAddressPanel() {
		addressPanelVisible = false;
		if (!detailsPanelVisible && addressDetailsPanel != null && addressPanel != null && detailsPanel != null) {
			addressDetailsPanel.setVisibility(View.VISIBLE);
			addressPanel.setVisibility(View.GONE);
			detailsPanel.setVisibility(View.GONE);
		}
		
		timezoneSpinnerBlock.setVisibility(View.GONE);
		countrySpinnerBlock.setVisibility(View.GONE);
		cityViewBlock.setVisibility(View.GONE);
		streetViewBlock.setVisibility(View.GONE);
		zipViewBlock.setVisibility(View.GONE);
	}

	public void showDetailsPanel() {
		detailsPanelVisible = true;
		locationViewBlock.setVisibility(View.VISIBLE);
		gobyViewBlock.setVisibility(View.VISIBLE);
		takewithyouViewBlock.setVisibility(View.VISIBLE);
		costViewBlock.setVisibility(View.VISIBLE);
		accomodationViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideDetailsPanel() {
		detailsPanelVisible = false;
		if (!addressPanelVisible && addressDetailsPanel != null && addressPanel != null && detailsPanel != null) {
			addressDetailsPanel.setVisibility(View.VISIBLE);
			addressPanel.setVisibility(View.GONE);
			detailsPanel.setVisibility(View.GONE);
		}
		LinearLayout locationViewBlock = (LinearLayout) findViewById(R.id.locationBlock);
		locationViewBlock.setVisibility(View.GONE);

		LinearLayout gobyViewBlock = (LinearLayout) findViewById(R.id.go_byBlock);
		gobyViewBlock.setVisibility(View.GONE);

		LinearLayout takewithyouViewBlock = (LinearLayout) findViewById(R.id.take_with_youBlock);
		takewithyouViewBlock.setVisibility(View.GONE);

		LinearLayout costViewBlock = (LinearLayout) findViewById(R.id.costBlock);
		costViewBlock.setVisibility(View.GONE);

		LinearLayout accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationBlock);
		accomodationViewBlock.setVisibility(View.GONE);
	}
	
	public class StaticTimezones {
		public String id;
		public String city;
		public String country;
		public String country2;
		public String country_code;
		public String timezone;
		public String altname;
	}

	protected void showInvitesView() {
		// TODO optimizacija panasios jebalos gula ant meskos sazines.
		// Zajabys.
		if (newInvites != null) {
			event.getInvited().addAll(newInvites);

//			long[] newAssignedContacs = new long[newInvites.size()];
//			for (int i = 0; i < newInvites.size(); i++) {
//				newAssignedContacs[i] = newInvites.get(i).getMy_contact_id();
//			}
//			long[] nuAssignedContacts = new long[event.getAssigned_contacts().length + newAssignedContacs.length];
//			System.arraycopy(event.getAssigned_contacts(), 0, nuAssignedContacts, 0, event.getAssigned_contacts().length);
//			System.arraycopy(newAssignedContacs, 0, nuAssignedContacts, event.getAssigned_contacts().length, newAssignedContacs.length);
//			event.assigned_contacts = nuAssignedContacts;
			newInvites = null;
		}

		int invitedListSize = event.getInvited().size();
		invitedPersonList.removeAllViews();
		if (invitedListSize == 0) {
			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
		} else {
			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
			invitedAdapter = new InvitedAdapter(this, event.getInvited());
			for (int i = 0; i < invitedListSize; i++) {
				View view = invitedAdapter.getView(i, null, null);
				invitedPersonList.addView(view);
			}
		}
		
	}
}