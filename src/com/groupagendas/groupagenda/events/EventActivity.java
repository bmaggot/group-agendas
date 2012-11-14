package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
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
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Prefs;

public class EventActivity extends Activity {
	public static final int DEFAULT_EVENT_DURATION_IN_MINS = 60;
	protected static final int COLOURED_BUBBLE_SIZE = 50;
	public static final String EXTRA_STRING_FOR_START_CALENDAR = "strStartTime";
	public static final String EXTRA_STRING_FOR_END_CALENDAR = "strEndTime";

	public static ArrayList<Invited> newInvites;
	public static ArrayList<Contact> selectedContacts;

	protected DateTimeUtils dtUtils;
	protected EditText descView;

	protected TextView countryView;
	protected EditText cityView;
	protected EditText streetView;
	protected EditText zipView;
	protected TextView timezoneView;

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
	protected ArrayList<StaticTimezones> filteredCountriesList = null;
	protected CountriesAdapter countriesAdapter = null;
	protected TimezonesAdapter timezonesAdapter = null;
	protected InvitedAdapter invitedAdapter = null;
	protected LinearLayout invitedPersonList;
	protected int timezoneInUse = 0;
	protected Button inviteButton;
	protected LinearLayout invitesColumn;
	protected RelativeLayout invitationResponseLine;
	protected TextView invitationResponseStatus;
	protected TextView response_button_yes;
	protected TextView response_button_no;
	protected TextView response_button_maybe;

	protected Account account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dtUtils = new DateTimeUtils(this);
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

		if (event.getInvited().size() > 0) {
			event.setType(Event.SHARED_EVENT);
		} else {
			event.setType(Event.NOTE);
		}

		if (event.getColor().equals(Event.DEFAULT_COLOR)) {
			if (!event.getType().equals(Event.NOTE)) {
				if (event.getStatus() == Invited.ACCEPTED) {
					event.setDisplayColor(Event.DEFAULT_COLOR_ATTENDING);
				} else if (!event.isBirthday()) {
					event.setDisplayColor(Event.DEFAULT_COLOR_MAYBE);
				} else {
					event.setDisplayColor(Event.DEFAULT_COLOR);
				}
			} else {
				event.setDisplayColor(Event.DEFAULT_COLOR);
			}
		} else {
			if (!event.getType().equals(Event.NOTE)) {
				if (event.getStatus() == Invited.ACCEPTED) {
					event.setDisplayColor(event.getColor());
				} else if (!event.isBirthday()) {
					event.setDisplayColor(Event.DEFAULT_COLOR_MAYBE);
				} else {
					event.setDisplayColor(Event.DEFAULT_COLOR);
				}
			} else {
				event.setDisplayColor(event.getColor());
			}
		}

		return event;

	}

	public String setErrorStr(int testEvent) {
		switch (testEvent) {
		case 1: // no title set
			return getString(R.string.title_is_required);
		case 2: // no timezone set
			return getString(R.string.timezone_required);
		case 3: // calendar fields are null
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
		public String call_code;
	}

	protected void showInvitesView(Context context) {
		if (newInvites != null) {
			event.getInvited().addAll(newInvites);
			newInvites = null;
		}

		int invitedListSize = event.getInvited().size();
		invitedPersonList.removeAllViews();
		if (invitedListSize == 0) {
			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
		} else {
			boolean contains = false;
			account = new Account(context);
			String fullname = account.getFullname();

			for (Invited i : event.getInvited()) {
				if (i.getName().equals(fullname)) {
					contains = true;
				}
			}

			if (!contains) {
				Invited me = new Invited();
				me.setGuid(account.getUser_id());
				me.setName(fullname);
				me.setStatus(Invited.ACCEPTED);
				event.setStatus(Invited.ACCEPTED);
				event.getInvited().add(me);
				invitedListSize++;
			}

			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
			invitedAdapter = new InvitedAdapter(this, event.getInvited());
			for (int i = 0; i < invitedListSize; i++) {
				View view = invitedAdapter.getView(i, null, null);
				invitedPersonList.addView(view);
			}
		}

	}

	// TODO write a javadoc for respondToInvitation(int response)
	protected void respondToInvitation(int response) {
		invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
		RelativeLayout myInvitation = (RelativeLayout) invitesColumn.findViewWithTag(Invited.OWN_INVITATION_ENTRY);
		TextView myStatus = (TextView) myInvitation.findViewById(R.id.invited_status);

		switch (response) {
		case 0:
			event.setStatus(0);
			invitationResponseStatus.setText(this.getString(R.string.status_not_attending));
			myStatus.setText(this.getString(R.string.status_not_attending));
			myStatus.setBackgroundColor(Color.parseColor("#5d5d5d")); // TODO
																		// hardcoded
																		// color-code
			response_button_yes.setVisibility(View.VISIBLE);
			response_button_maybe.setVisibility(View.VISIBLE);
			response_button_no.setVisibility(View.INVISIBLE);
			break;
		case 1:
			event.setStatus(1);
			invitationResponseStatus.setText(this.getString(R.string.status_attending));
			myStatus.setText(this.getString(R.string.status_attending));
			myStatus.setBackgroundColor(Color.parseColor("#26b2d8")); // TODO
																		// hardcoded
																		// color-code
			response_button_yes.setVisibility(View.INVISIBLE);
			response_button_maybe.setVisibility(View.VISIBLE);
			response_button_no.setVisibility(View.VISIBLE);
			break;
		case 2:
			event.setStatus(2);
			invitationResponseStatus.setText(this.getString(R.string.status_maybe));
			myStatus.setText(this.getString(R.string.status_maybe));
			myStatus.setBackgroundColor(Color.parseColor("#b5b5b5")); // TODO
																		// hardcoded
																		// color-code
			response_button_yes.setVisibility(View.VISIBLE);
			response_button_maybe.setVisibility(View.INVISIBLE);
			response_button_no.setVisibility(View.VISIBLE);
			break;
		case 4:
			event.setStatus(4);
			invitationResponseStatus.setText(this.getString(R.string.status_new_invite));
			response_button_yes.setVisibility(View.VISIBLE);
			response_button_maybe.setVisibility(View.VISIBLE);
			response_button_no.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	protected void setAutoIcon(Context context) {
		ArrayList<AutoIconItem> autoIcons = DataManagement.getAutoIcons(context);

		for (AutoIconItem autoIcon : autoIcons) {
			if (event.getTitle().contains(autoIcon.keyword)) {
				event.setIcon(autoIcon.icon);
				break;
			}
		}
	}

	protected void setAutoColor(Context context) {
		ArrayList<AutoColorItem> autoColors = DataManagement.getAutoColors(context);

		for (AutoColorItem autoColor : autoColors) {
			if (event.getTitle().contains(autoColor.keyword)) {
				event.setColor(autoColor.color);
				break;
			}
		}
	}
}
