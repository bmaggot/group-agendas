package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountActivity;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactInfoActivity;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class EventEditActivity extends EventActivity {

	private class GenericTextWatcher implements TextWatcher {

		private String oldText = null;

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			oldText = charSequence.toString();
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void afterTextChanged(Editable editable) {
			if (!editable.toString().equalsIgnoreCase(oldText)) {
				changesMade = true;
				saveButton.setEnabled(changesMade);
			}
		}
	}

	private TextWatcher watcher;

	private TextView topText;
	private Button deleteButton;

//	private LinearLayout reminderBlock;
//	private TextView setReminderTrigger;

//	private LinearLayout reminder1container;
//	private LinearLayout reminder2container;
//	private LinearLayout reminder3container;
//	private EditText reminder1;
//	private EditText reminder2;
//	private EditText reminder3;

//	private LinearLayout alarmBlock;
//	private TextView setAlarmTrigger;
	private TextView creatorNameTextView;
//	private LinearLayout alarm1container;
//	private LinearLayout alarm2container;
//	private LinearLayout alarm3container;
//	private EditText alarm1;
//	private EditText alarm2;
//	private EditText alarm3;

	private long event_internal_id;
	private long event_external_id;

	protected final static int DELETE_DIALOG = 1;
//	private boolean remindersShown = false;
//	private boolean alarmsShown = false;
	private boolean isInvited = false;
	// private boolean eventEdited = false;
	private boolean changesMade = false;

	private Intent intent;

	private Button chatMessengerButton;

	private LinearLayout eventStartEndTime;
	private LinearLayout pollStartEndTime;
	private ProgressDialog pd;
	private int eventPollSize;
	ArrayList<JSONObject> allEventPolls;
	private LinearLayout allDayLayout;
	private LinearLayout attending_line;
	private static ArrayList<JSONObject> selectedPollTime;
	private RelativeLayout poll_status_line;
	private TextView reject_poll;
	private TextView rejoin_poll;
	private TextView poll_status_text;
	private int poll_status;
	private boolean to_rejoin_poll;
	private boolean to_reject_poll;

	public void enableDisableButtons(Boolean state) {
		saveButton.setEnabled(state);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pd = new ProgressDialog(this);
		setContentView(R.layout.event_edit);
		selectedContacts = null;
		selectedGroups = null;
	}

	@Override
	public void onResume() {
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter(C2DMReceiver.REFRESH_EVENT_EDIT_ACTIVITY));
		super.onResume();
		if (watcher == null) {
			watcher = new GenericTextWatcher();
		}
		dtUtils = new DateTimeUtils(this);
		account = new Account(EventEditActivity.this);
		String[] cities;
		String[] countries;
		String[] countries2;
		String[] country_codes;
		String[] timezones;
		String[] altnames;
		countriesList = new ArrayList<StaticTimezones>();

		cities = getResources().getStringArray(R.array.city);
		countries = getResources().getStringArray(R.array.countries);
		countries2 = getResources().getStringArray(R.array.countries2);
		country_codes = getResources().getStringArray(R.array.country_codes);
		timezones = getResources().getStringArray(R.array.timezones);
		altnames = getResources().getStringArray(R.array.timezone_altnames);
		for (int i = 0; i < cities.length; i++) {
			StaticTimezones temp = new StaticTimezones();

			temp.id = "" + i;
			temp.city = cities[i];
			temp.country = countries[i];
			temp.country2 = countries2[i];
			temp.country_code = country_codes[i];
			temp.timezone = timezones[i];
			temp.altname = altnames[i];

			countriesList.add(temp);
		}
		if (countriesList != null) {
			countriesAdapter = new CountriesAdapter(EventEditActivity.this, R.layout.search_dialog_item, countriesList);
			timezonesAdapter = new TimezonesAdapter(EventEditActivity.this, R.layout.search_dialog_item, countriesList);
		}

		initViewItems();
		hideAddressPanel();
		hideDetailsPanel();
		addressPanel.setVisibility(View.GONE);
		detailsPanel.setVisibility(View.GONE);
		addressDetailsPanel.setVisibility(View.VISIBLE);

		if (selectedContacts == null)
			selectedContacts = new ArrayList<Contact>();

		if (!selectedContacts.isEmpty()) {
			changesMade = true;
			saveButton.setEnabled(changesMade);
		}

		if (selectedGroups == null)
			selectedGroups = new ArrayList<Group>();

		if (!selectedGroups.isEmpty()) {
			changesMade = true;
			saveButton.setEnabled(changesMade);
		}

		if (newInvites == null)
			newInvites = new ArrayList<Invited>();

		for (Contact temp : EventActivity.selectedContacts) {
			Invited nu = new Invited();
			nu.setMy_contact_id(temp.contact_id);
			nu.setName(temp.name + " " + temp.lastname);
			nu.setStatus(Invited.PENDING);

			EventActivity.newInvites.add(nu);
		}

		ArrayList<Contact> selectedContactsFromGroups = new ArrayList<Contact>();
		for (Group group : EventActivity.selectedGroups) {
			for (String id : group.contacts.values()) {
				selectedContactsFromGroups.add(ContactManagement.getContactFromLocalDb(this, Integer.valueOf(id), 0));
			}
		}
		for (Contact temp : selectedContactsFromGroups) {
			Invited nu = new Invited();
			nu.setMy_contact_id(temp.contact_id);
			nu.setName(temp.name + " " + temp.lastname);
			nu.setStatus(Invited.PENDING);
			boolean contains = false;
			for (Invited tmp : EventActivity.newInvites) {
				if (nu.getMy_contact_id() == tmp.getMy_contact_id()) {
					contains = true;
				}
			}
			if (EventManagement.getEventFromLocalDb(this, event_internal_id, EventManagement.ID_INTERNAL) != null) {
				for (Invited tmp : EventManagement.getEventFromLocalDb(this, event_internal_id, EventManagement.ID_INTERNAL).getInvited()) {
					if (nu.getMy_contact_id() == tmp.getMy_contact_id()) {
						contains = true;
					}
				}
			}
			if (!contains) {
				EventActivity.newInvites.add(nu);
			}
		}

		// TODO implement offline
		event_internal_id = intent.getLongExtra("event_id", 0);
		if (event_external_id == 0
				&& EventManagement.getEventFromLocalDb(getApplicationContext(), event_internal_id, EventManagement.ID_INTERNAL) != null) {
			event_external_id = EventManagement
					.getEventFromLocalDb(getApplicationContext(), event_internal_id, EventManagement.ID_INTERNAL).getEvent_id();
		}
		// mode event Edit
		if (event_internal_id > 0) {
			new GetEventTask().execute(new Long[] { event_internal_id, event_external_id });
		}
	}

	private void makeToastWarning(String text, int length) {
		Toast.makeText(this, text, length).show();
	}

	private void initViewItems() {

		pb = (ProgressBar) findViewById(R.id.progress);
		intent = getIntent();
		// Top text and SAVE Button
		topText = (TextView) findViewById(R.id.topText);

		eventStartEndTime = (LinearLayout) findViewById(R.id.eventStartEndTime);
		// pollStartEndTime_list = (ListView)
		// findViewById(R.id.pollStartEndTime_list);
		pollStartEndTime = (LinearLayout) findViewById(R.id.pollStartEndTime);
		allDayLayout = (LinearLayout) findViewById(R.id.allDayLayout);
		attending_line = (LinearLayout) findViewById(R.id.attending_line);
		poll_status_line = (RelativeLayout) findViewById(R.id.poll_status_line);
		reject_poll = (TextView) findViewById(R.id.button_reject);
		rejoin_poll = (TextView) findViewById(R.id.button_rejoin);
		poll_status_text = (TextView) findViewById(R.id.poll_status);

		saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setEnabled(changesMade);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// enableDisableButtons(false);
				if (!saveButton.getText().toString().equalsIgnoreCase(getResources().getString(R.string.saving))) {

					new UpdateEventTask().execute();
				} else {
					Toast.makeText(EventEditActivity.this, R.string.wait, Toast.LENGTH_SHORT);
				}
				sendSms();
			}
		});

		// Icon, color and title
		iconView = (ImageView) findViewById(R.id.iconView);
		colorView = (ImageView) findViewById(R.id.colorView);
		titleView = (EditText) findViewById(R.id.title);

		// Start and end time buttons
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateTimeDialog(startView, DIALOG_START);
			}
		});

		endButton = (Button) findViewById(R.id.endButton);
		endButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateTimeDialog(endView, DIALOG_END);
			}
		});

		// EVENT START AND END TIMES
		startView = (EditText) findViewById(R.id.startView);
		endView = (EditText) findViewById(R.id.endView);

		// Description
		descView = (EditText) findViewById(R.id.descView);
		// Creator
		creatorNameTextView = (TextView) findViewById(R.id.EventEditCreatorName);
		// allday
		allDayToggleButton = (ToggleButton) findViewById(R.id.allDayToggleButton);
		allDayToggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!allDayToggleButton.isChecked()) {
					startView.setText(dtUtils.formatDateTime(event.getStartCalendar().getTime()));
					endView.setText(dtUtils.formatDateTime(event.getEndCalendar().getTime()));
				} else {
					startView.setText(dtUtils.formatDate(event.getStartCalendar()));
					endView.setText(dtUtils.formatDate(event.getStartCalendar()));
				}
				saveButton.setEnabled(true);
			}
		});
		// Addres and details panel
		addressDetailsPanel = (RelativeLayout) findViewById(R.id.addressDetailsLine);

		// ADDRESS PANEL
		addressPanel = (LinearLayout) findViewById(R.id.addressLine);
		addressPanel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (addressPanelVisible) {
					hideAddressPanel();
				} else {
					showAddressPanel();
				}
			}
		});
		addressTrigger = (TextView) addressDetailsPanel.findViewById(R.id.addressTrigger);
		addressTrigger.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addressDetailsPanel.setVisibility(View.GONE);
				addressPanel.setVisibility(View.VISIBLE);
				detailsPanel.setVisibility(View.VISIBLE);
				showAddressPanel();
			}
		});

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (TextView) findViewById(R.id.countryView);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(EventEditActivity.this);
				dia1.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dia1.setContentView(R.layout.search_dialog);

				ListView diaList = (ListView) dia1.findViewById(R.id.dialog_list);
				diaList.setAdapter(countriesAdapter);
				countriesAdapter.notifyDataSetChanged();

				EditText searchView = (EditText) dia1.findViewById(R.id.dialog_search);

				TextWatcher filterTextWatcher = new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						if (s != null) {
							if (countriesAdapter != null)
								countriesAdapter.getFilter().filter(s);
						}
					}
				};

				searchView.addTextChangedListener(filterTextWatcher);

				diaList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
						timezoneInUse = Integer.parseInt(view.getTag().toString());
						countryView.setText(countriesList.get(timezoneInUse).country2);
						event.setCountry(countriesList.get(timezoneInUse).country_code);

						filteredCountriesList = new ArrayList<StaticTimezones>();

						for (StaticTimezones tz : countriesList) {
							if (tz.country_code.equalsIgnoreCase(event.getCountry())) {
								filteredCountriesList.add(tz);
							}
						}

						timezonesAdapter = new TimezonesAdapter(EventEditActivity.this, R.layout.search_dialog_item, filteredCountriesList);
						timezonesAdapter.notifyDataSetChanged();

						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						event.setTimezone(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityView = (EditText) findViewById(R.id.cityView);
		streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetView = (EditText) findViewById(R.id.streetView);
		zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipView = (EditText) findViewById(R.id.zipView);
		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneView = (TextView) findViewById(R.id.timezoneView);
		timezoneSpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(EventEditActivity.this);
				dia1.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dia1.setContentView(R.layout.search_dialog);

				ListView diaList = (ListView) dia1.findViewById(R.id.dialog_list);
				diaList.setAdapter(timezonesAdapter);
				timezonesAdapter.notifyDataSetChanged();

				EditText searchView = (EditText) dia1.findViewById(R.id.dialog_search);

				TextWatcher filterTextWatcher = new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						if (s != null) {
							if (timezonesAdapter != null)
								timezonesAdapter.getFilter().filter(s);
						}
					}
				};

				searchView.addTextChangedListener(filterTextWatcher);

				diaList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
						timezoneInUse = Integer.parseInt(view.getTag().toString());
						countryView.setText(countriesList.get(timezoneInUse).country2);
						event.setCountry(countriesList.get(timezoneInUse).country_code);
						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						event.setTimezone(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		// DETAILS PANEL
		detailsPanel = (LinearLayout) findViewById(R.id.detailsLine);
		detailsPanel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (detailsPanelVisible) {
					hideDetailsPanel();
				} else {
					showDetailsPanel();
				}
			}
		});
		detailsTrigger = (TextView) addressDetailsPanel.findViewById(R.id.detailsTrigger);
		detailsTrigger.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addressDetailsPanel.setVisibility(View.GONE);
				addressPanel.setVisibility(View.VISIBLE);
				detailsPanel.setVisibility(View.VISIBLE);
				showDetailsPanel();
			}
		});
		locationViewBlock = (LinearLayout) findViewById(R.id.locationBlock);
		locationView = (EditText) findViewById(R.id.locationView);
		gobyViewBlock = (LinearLayout) findViewById(R.id.go_byBlock);
		gobyView = (EditText) findViewById(R.id.gobyView);
		takewithyouViewBlock = (LinearLayout) findViewById(R.id.take_with_youBlock);
		takewithyouView = (EditText) findViewById(R.id.takewithyouView);
		costViewBlock = (LinearLayout) findViewById(R.id.costBlock);
		costView = (EditText) findViewById(R.id.costView);
		accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationBlock);
		accomodationView = (EditText) findViewById(R.id.accomodationView);

		chatMessengerButton = (Button) findViewById(R.id.messenger_button);

		// REMINDERS PANEL
/*		reminderBlock = (LinearLayout) findViewById(R.id.reminder_block);
		setReminderTrigger = (TextView) findViewById(R.id.setReminderTrigger);
		// TODO alarms eventedit turn on when ready.
		setReminderTrigger.setVisibility(View.GONE);
		setReminderTrigger.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (remindersShown) {
					remindersShown = false;
					reminderBlock.setVisibility(View.GONE);
				} else {
					remindersShown = true;
					reminderBlock.setVisibility(View.VISIBLE);
				}
			}
		});
		// REMINDER1
		reminder1container = (LinearLayout) findViewById(R.id.reminder_container1);
		reminder1 = (EditText) findViewById(R.id.reminder1);
		reminder1container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(reminder1, REMINDER1);
			}
		});
		reminder1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(reminder1, REMINDER1);
			}
		});
		// REMINDER2
		reminder2container = (LinearLayout) findViewById(R.id.reminder_container2);
		reminder2 = (EditText) findViewById(R.id.reminder2);
		reminder2container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(reminder2, REMINDER2);
			}
		});
		reminder2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(reminder2, REMINDER2);
			}
		});
		// REMINDER3
		reminder3container = (LinearLayout) findViewById(R.id.reminder_container3);
		reminder3 = (EditText) findViewById(R.id.reminder3);
		reminder3container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(reminder3, REMINDER3);
			}
		});
		reminder3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(reminder3, REMINDER3);
			}
		});

		// ALARMS
		alarmBlock = (LinearLayout) findViewById(R.id.alarm_block);
		setAlarmTrigger = (TextView) findViewById(R.id.setAlarmTrigger);
		// TODO alarms eventedit turn on when ready.
		setAlarmTrigger.setVisibility(View.GONE);
		setAlarmTrigger.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (alarmsShown) {
					alarmsShown = false;
					alarmBlock.setVisibility(View.GONE);
				} else {
					alarmsShown = true;
					alarmBlock.setVisibility(View.VISIBLE);
				}
			}
		});

		// ALARM1
		alarm1 = (EditText) findViewById(R.id.alarm1);
		alarm1container = (LinearLayout) findViewById(R.id.alarm_container1);
		alarm1container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(alarm1, ALARM1);
			}
		});
		alarm1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(alarm1, ALARM1);
			}
		});

		// ALARM2
		alarm2container = (LinearLayout) findViewById(R.id.alarm_container2);
		alarm2 = (EditText) findViewById(R.id.alarm2);
		alarm2container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(alarm2, ALARM2);
			}
		});
		alarm2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(alarm2, ALARM2);
			}
		});

		// ALARM3
		alarm3container = (LinearLayout) findViewById(R.id.alarm_container3);
		alarm3 = (EditText) findViewById(R.id.alarm3);
		alarm3container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(alarm3, ALARM3);
			}
		});
		alarm3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDateTimeDialog(alarm3, ALARM3);
			}
		}); */

		// INVITES SECTION
		response_button_yes = (TextView) findViewById(R.id.button_yes);
		response_button_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				respondToInvitation(1);
				new EventEditActivity.UpdateEventStatusTask().execute();
			}
		});

		response_button_no = (TextView) findViewById(R.id.button_no);
		response_button_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				respondToInvitation(0);
				new EventEditActivity.UpdateEventStatusTask().execute();
			}
		});

		response_button_maybe = (TextView) findViewById(R.id.button_maybe);
		response_button_maybe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				respondToInvitation(2);
				new EventEditActivity.UpdateEventStatusTask().execute();
			}
		});

		invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
		super.inviteButton = (Button) findViewById(R.id.invite_button);

		super.inviteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Calendar.getInstance().getTimeInMillis() < event.getEndCalendar().getTimeInMillis()) {
					Intent i = new Intent(EventEditActivity.this, ContactsActivity.class);
					i.putExtra(ContactsActivity.TASK_MODE_KEY, ContactsActivity.TASK_MODE_SELECTION);
					i.putExtra(ContactsActivity.LIST_MODE_KEY, ContactsActivity.LIST_MODE_CONTACTS);
					i.putExtra(ContactsActivity.DESTINATION_KEY, ContactsActivity.DEST_EVENT_ACTIVITY);
					Data.showSaveButtonInContactsForm = true;
					// TODO Data.eventForSavingNewInvitedPersons = event;
					startActivity(i);
				} else {
					makeToastWarning(getResources().getString(R.string.invite_contact_in_the_past), Toast.LENGTH_LONG);
				}
			}
		});

		invitationResponseLine = (RelativeLayout) findViewById(R.id.response_to_invitation);
		invitationResponseLine.setVisibility(View.GONE);
		invitationResponseStatus = (TextView) findViewById(R.id.status);

		deleteButton = (Button) findViewById(R.id.event_delete);
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DELETE_DIALOG);
			}
		});
	}

	// public View getInvitedView(Invited invited, LayoutInflater inflater, View
	// view, Context mContext, boolean setEmailRed) {
	// final TextView nameView = (TextView)
	// view.findViewById(R.id.invited_fullname);
	// nameView.setText(invited.name);
	//
	// final TextView emailView = (TextView)
	// view.findViewById(R.id.invited_available_email);
	// emailView.setText(invited.email);
	// if (setEmailRed) {
	// emailView.setTextColor(Color.GREEN);
	// }
	//
	// final TextView statusView = (TextView)
	// view.findViewById(R.id.invited_status);
	//
	// switch (invited.status_id) {
	// case 0:
	// statusView.setText(mContext.getString(R.string.status_0));
	// break;
	// case 1:
	// statusView.setText(mContext.getString(R.string.status_1));
	// break;
	// case 2:
	// statusView.setText(mContext.getString(R.string.status_2));
	// break;
	// case 4:
	// statusView.setText(mContext.getString(R.string.new_invite));
	// break;
	// }
	//
	// if (invited.me) {
	// view.setTag("my_event_status");
	// view.setId(MY_INVITED_ENTRY_ID);
	// }
	// return view;
	// }

	class GetEventTask extends AsyncTask<Long, Event, Event> {
		// final DataManagement dm = DataManagement.getInstance(getParent());
		final String[] iconsValues = getResources().getStringArray(R.array.icons_values);
		final SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd.setMessage(getResources().getString(R.string.loading));
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Event doInBackground(Long... ids) {
			// autoColors = dm.getAutoColors(); TODO implement or remove shit
			// autoIcons = dm.getAutoIcons();

			if (intent.getBooleanExtra("isNative", false)) {
				return NativeCalendarReader.getNativeEventFromLocalDbById(getApplicationContext(), ids[0]);
			} else {
				if (EventManagement.getEventFromLocalDb(EventEditActivity.this, ids[0], EventManagement.ID_INTERNAL) != null) {
					return EventManagement.getEventFromLocalDb(EventEditActivity.this, ids[0], EventManagement.ID_INTERNAL);
				} else {
					return EventManagement.getEventFromLocalDb(EventEditActivity.this, ids[1], EventManagement.ID_EXTERNAL);
				}
			}
		}

		@Override
		protected void onPostExecute(final Event result) {
			super.onPostExecute(result);

			if (result == null) {
				throw new IllegalStateException("EVENT NOT FOUND IN LOCAL DB!!!!!!");
			}

			account = new Account(EventEditActivity.this);
			event = result;
			selectedIcon = event.getIcon();
			allDayToggleButton.setChecked(event.is_all_day());

			if (event.getType().contentEquals("v")) {
				eventStartEndTime.setVisibility(View.GONE);
				pollStartEndTime.setVisibility(View.VISIBLE);
				allDayLayout.setVisibility(View.GONE);
				invitationResponseLine.setVisibility(View.GONE);
				attending_line.setVisibility(View.GONE);
				poll_status_line.setVisibility(View.VISIBLE);
				pollStartEndTime.removeAllViews();
				to_reject_poll = false;
				to_rejoin_poll = false;

				if (event.getStatus() == Invited.REJECTED) {
					reject_poll.setVisibility(View.INVISIBLE);
					poll_status_text.setText(getString(R.string.rejected));
				} else {
					rejoin_poll.setVisibility(View.INVISIBLE);
					poll_status_text.setText(getString(R.string.joined));					
				}

				reject_poll.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						saveButton.setEnabled(true);
						reject_poll.setVisibility(View.INVISIBLE);
						rejoin_poll.setVisibility(View.VISIBLE);
						poll_status_text.setText(getString(R.string.rejected));
						poll_status = Invited.REJECTED;
						to_reject_poll = true;
						selectedPollTime = new ArrayList<JSONObject>();

					}
				});

				rejoin_poll.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						saveButton.setEnabled(true);
						rejoin_poll.setVisibility(View.INVISIBLE);
						reject_poll.setVisibility(View.VISIBLE);
						poll_status_text.setText(getString(R.string.joined));
						poll_status = Invited.ACCEPTED;
						to_rejoin_poll = true;

					}
				});

				LayoutInflater mInflater = LayoutInflater.from(EventEditActivity.this);

				String jsonArraySelectedTime = event.getSelectedEventPollsTime();
				selectedPollTime = new ArrayList<JSONObject>();
				try {
					if (jsonArraySelectedTime != null && !jsonArraySelectedTime.contentEquals("null")) {
						final JSONArray jsonArray = new JSONArray(jsonArraySelectedTime);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject pollThread = jsonArray.getJSONObject(i);
							selectedPollTime.add(pollThread);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String jsonArrayString = event.getPoll();
				try {
					if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
						final JSONArray jsonArray = new JSONArray(jsonArrayString);
						eventPollSize = jsonArray.length();
						allEventPolls = new ArrayList<JSONObject>();
						for (int i = 0; i < jsonArray.length(); i++) {
							final JSONObject pollThread = jsonArray.getJSONObject(i);
							allEventPolls.add(pollThread);
							final View view = mInflater.inflate(R.layout.poll_thread, null);
							final CheckBox selectedTime = (CheckBox) view.findViewById(R.id.selectedTime);

							// if(pollThread.getString("response").contentEquals("1")){
							// NavbarActivity.selectedPollTime.add(pollThread);
							// //selectedTime.setChecked(true);
							// }

							LinearLayout backgr = (LinearLayout) view.findViewById(R.id.pollTimeBlock);

							if (i == 0) {
								backgr.setBackgroundResource(R.drawable.event_invite_people_button_notalone_i);
							} else {
								if ((i + 1) == jsonArray.length()) {
									backgr.setBackgroundResource(R.drawable.event_invited_entry_last_background);
								} else {
									backgr.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
								}
							}

							TextView startTime = (TextView) view.findViewById(R.id.pollStartTime);
							TextView endTime = (TextView) view.findViewById(R.id.pollEndTime);
							DateTimeUtils dateTimeUtils = new DateTimeUtils(EventEditActivity.this);

							String temp = "";
							try {
								temp = pollThread.getString("start");
							} catch (JSONException e) {
								Log.e("PollAdapter", "Failed getting poll time.");
							}

							final Calendar tempCal = Utils.stringToCalendar(EventEditActivity.this, temp,
									DataManagement.SERVER_TIMESTAMP_FORMAT);
							startTime.setText(dateTimeUtils.formatDate(tempCal) + " " + dateTimeUtils.formatTime(tempCal));

							try {
								temp = pollThread.getString("end");
							} catch (JSONException e) {
								Log.e("PollAdapter", "Failed getting poll time.");
							}

							final Calendar tempCal2 = Utils.stringToCalendar(EventEditActivity.this, temp,
									DataManagement.SERVER_TIMESTAMP_FORMAT);
							endTime.setText(dateTimeUtils.formatDate(tempCal2) + " " + dateTimeUtils.formatTime(tempCal2));

							selectedTime.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									if (selectedTime.isChecked()) {
										selectedPollTime.add(pollThread);
										saveButton.setEnabled(true);
										poll_status = Invited.ACCEPTED;
									} else {
										saveButton.setEnabled(true);
										for (int i = 0; i < eventPollSize; i++) {
											int size = selectedPollTime.size();
											for (int y = 0; y < size; y++) {
												try {
													if (selectedPollTime.get(y).getString("id").equals(pollThread.getString("id"))) {
														selectedPollTime.remove(y);
														poll_status = Invited.ACCEPTED;
														break;

													}
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
										}
									}
								}
							});

							if (selectedPollTime != null) {
								int size = selectedPollTime.size();
								for (int y = 0; y < size; y++) {
									final JSONObject pollThread2 = selectedPollTime.get(y);
									if (pollThread.getString("timestamp_start_utc").contentEquals(
											pollThread2.getString("timestamp_start_utc"))
											&& pollThread.getString("id").contentEquals(pollThread2.getString("id"))) {
										selectedTime.setChecked(true);
									}
								}
							}

							pollStartEndTime.addView(view);

						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			// toptext
			String tmpTopText = event.getType();
			if (tmpTopText.equalsIgnoreCase("t")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[4]);
			} else if (tmpTopText.equalsIgnoreCase("n")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[1]);
			} else if (tmpTopText.equalsIgnoreCase("p")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[0]);
			} else if (tmpTopText.equalsIgnoreCase("r")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[2]);
			} else if (tmpTopText.equalsIgnoreCase("o")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[4]);
			} else if (tmpTopText.equalsIgnoreCase("v")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[5]);
			} else if (tmpTopText.equalsIgnoreCase("native event")) {
				topText.setText(getResources().getStringArray(R.array.type_labels)[6]);
			}

			// title
			titleView.setText(result.getTitle());
			titleView.addTextChangedListener(watcher);

			// if this user is owner of event, fields can be edited
			if (result.is_owner()) {
				saveButton.setVisibility(View.VISIBLE);
				deleteButton.setVisibility(View.VISIBLE);

				// ICON SELECTION
				iconView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Dialog dialog = new Dialog(EventEditActivity.this);
						dialog.setContentView(R.layout.list_dialog);
						dialog.setTitle(R.string.choose_icon);

						GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
						gridview.setAdapter(new IconsAdapter(EventEditActivity.this, iconsValues));

						gridview.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

								String testSelectedIcon = selectedIcon;
								if (iconsValues[position].equals("noicon")) {
									selectedIcon = Event.DEFAULT_ICON;
									iconView.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
								} else {
									selectedIcon = iconsValues[position];
									int iconId = getResources().getIdentifier(iconsValues[position], "drawable",
											"com.groupagendas.groupagenda");
									iconView.setImageResource(iconId);
								}
								if (!changesMade) {
									changesMade = !testSelectedIcon.equalsIgnoreCase(selectedIcon);
									saveButton.setEnabled(changesMade);
								}

								dialog.dismiss();
							}
						});

						dialog.show();
					}
				});

				// COLOR SELECTION

				selectedColor = result.getColor();
				final String[] colorsValues = getResources().getStringArray(R.array.colors_values);
				colorView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Dialog dialog = new Dialog(EventEditActivity.this);
						dialog.setContentView(R.layout.list_dialog);
						dialog.setTitle(R.string.choose_color);

						GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
						gridview.setAdapter(new ColorsAdapter(EventEditActivity.this, colorsValues));

						gridview.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								String testColorsValues = colorsValues[position];
								result.setColor(colorsValues[position]);
								selectedColor = colorsValues[position];
								colorView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(EventEditActivity.this,
										COLOURED_BUBBLE_SIZE, colorsValues[position], true));
								if (!changesMade) {

									changesMade = testColorsValues.equalsIgnoreCase(colorsValues[position]);

									saveButton.setEnabled(changesMade);
								}
								dialog.dismiss();
							}
						});
						dialog.show();
					}
				});

				showView(timezoneView, addressLine);
				showView(countryView, addressLine);
				showView(cityView, addressLine);
				showView(streetView, addressLine);
				showView(zipView, addressLine);
				showView(locationView, detailsLine);
				showView(gobyView, detailsLine);
				showView(takewithyouView, detailsLine);
				showView(costView, detailsLine);
				showView(accomodationView, detailsLine);

			} else {

				descView.setEnabled(false);
				titleView.setEnabled(false);
				endView.setEnabled(false);
				endButton.setEnabled(false);
				timezoneView.setEnabled(false);
				startView.setEnabled(false);
				startButton.setEnabled(false);
				cityView.setEnabled(false);
				streetView.setEnabled(false);
				zipView.setEnabled(false);
				locationView.setEnabled(false);
				gobyView.setEnabled(false);
				takewithyouView.setEnabled(false);
				costView.setEnabled(false);
				accomodationView.setEnabled(false);
				saveButton.setVisibility(View.GONE);
				allDayToggleButton.setEnabled(false);

			}
			if (event.isNative()) {
//				alarm1container.setEnabled(false);
//				alarm2container.setEnabled(false);
//				alarm3container.setEnabled(false);
//				reminder1container.setEnabled(false);
//				reminder2container.setEnabled(false);
//				reminder3container.setEnabled(false);
				allDayToggleButton.setEnabled(false);
				deleteButton.setVisibility(View.INVISIBLE);
			}

			int id = account.getUser_id();
			for (Invited inv : result.getInvited()) {
				if ((inv.getGuid() == id) && (inv.getGuid() != result.getUser_id())) {
					saveButton.setVisibility(View.VISIBLE);
					isInvited = true;
				}
			}

			colorView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(EventEditActivity.this, COLOURED_BUBBLE_SIZE, result, true));
			iconView.setImageResource(result.getIconId(EventEditActivity.this));

			// START AND END TIME
			if (result.getStartCalendar() != null) {
				if (!allDayToggleButton.isChecked()) {
					startView.setText(dtUtils.formatDateTime(result.getStartCalendar()));
				} else {
					startView.setText(dtUtils.formatDate(result.getStartCalendar()));
				}
				startCalendar = (Calendar) result.getStartCalendar().clone();
			}
			if (result.getEndCalendar() != null) {
				endView.setText(dtUtils.formatDateTime(result.getEndCalendar()));
				endCalendar = (Calendar) result.getEndCalendar().clone();
			}

			if (result.getDescription().length() > 0) {
				LinearLayout parent = (LinearLayout) descView.getParent();
				parent.setVisibility(View.VISIBLE);
				descView.setText(result.getDescription());
			}
			descView.addTextChangedListener(watcher);
			if (ContactManagement.getContactFromLocalDb(getApplicationContext(), result.getCreator_contact_id(), 0) != null) {
				creatorNameTextView.setText(result.getCreator_fullname());
				creatorNameTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent contactIntent = new Intent(getApplicationContext(), ContactInfoActivity.class);
						contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						contactIntent.putExtra("contactId", result.getCreator_contact_id());
						startActivity(contactIntent);
					}
				});
			} else if (result.getCreator_contact_id() == 0 && !result.is_owner()) {
				creatorNameTextView.setText(result.getCreator_fullname());
			} else if (result.is_owner()) {
				creatorNameTextView.setText(getResources().getString(R.string.you));
				creatorNameTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent contactIntent = new Intent(getApplicationContext(), AccountActivity.class);
						contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(contactIntent);
					}
				});
			}
			if (result.getTimezone().length() > 0) {
				for (StaticTimezones entry : countriesList) {
					if (entry.timezone.equalsIgnoreCase(result.getTimezone()))
						timezoneInUse = Integer.parseInt(entry.id);
				}
				if (timezoneInUse > 0) {
					filteredCountriesList = new ArrayList<StaticTimezones>();

					for (StaticTimezones tz : countriesList) {
						if (tz.country_code.equalsIgnoreCase(event.getCountry())) {
							filteredCountriesList.add(tz);
						}
					}

					timezonesAdapter = new TimezonesAdapter(EventEditActivity.this, R.layout.search_dialog_item, filteredCountriesList);
					timezonesAdapter.notifyDataSetChanged();

					timezoneView.setText(countriesList.get(timezoneInUse).altname);
					countryView.setText(countriesList.get(timezoneInUse).country2);

					showView(timezoneView, addressLine);
					showView(countryView, addressLine);
				}

			}

			timezoneView.addTextChangedListener(watcher);
			countryView.addTextChangedListener(watcher);

			if (result.getCity().length() > 0) {
				cityView.setText(result.getCity());
				showView(cityView, addressLine);
			}
			cityView.addTextChangedListener(watcher);
			if (result.getStreet().length() > 0) {
				streetView.setText(result.getStreet());
				showView(streetView, addressLine);
			}
			streetView.addTextChangedListener(watcher);
			if (result.getZip().length() > 0) {
				zipView.setText(result.getZip());
				showView(zipView, addressLine);
			}
			zipView.addTextChangedListener(watcher);
			if (result.getLocation().length() > 0) {
				locationView.setText(result.getLocation());
				showView(locationView, detailsLine);
			}
			locationView.addTextChangedListener(watcher);
			if (result.getGo_by().length() > 0) {
				gobyView.setText(result.getGo_by());
				showView(gobyView, detailsLine);
			}
			gobyView.addTextChangedListener(watcher);
			if (result.getTake_with_you().length() > 0) {
				takewithyouView.setText(result.getTake_with_you());
				showView(takewithyouView, detailsLine);
			}
			takewithyouView.addTextChangedListener(watcher);
			if (result.getCost().length() > 0) {
				costView.setText(result.getCost());
				showView(costView, detailsLine);
			}
			costView.addTextChangedListener(watcher);
			if (result.getAccomodation().length() > 0) {
				accomodationView.setText(result.getAccomodation());
				showView(accomodationView, detailsLine);
				accomodationView.addTextChangedListener(watcher);
			}

			if (!event.isNative()) {
				chatMessengerButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (event_internal_id > 0 && event.getEvent_id() > 1) {
							Intent intent = new Intent(EventEditActivity.this, ChatMessageActivity.class);
							intent.putExtra("event_id", event.getEvent_id());
							startActivity(intent);
						} else {
							showToast(getResources().getString(R.string.chat_on_event_not_created_online), Toast.LENGTH_LONG);
						}
					}
				});
			} else {
				chatMessengerButton.setVisibility(View.INVISIBLE);
			}
//			if (result.getReminder1() != null) {
//				reminder1.setText(dtUtils.formatDateTime(result.getReminder1()));
//			} else {
//				reminder1.setText("");
//			}
//			if (result.getReminder2() != null) {
//				reminder2.setText(dtUtils.formatDateTime(result.getReminder2()));
//			} else {
//				reminder2.setText("");
//			}
//			if (result.getReminder3() != null) {
//				reminder3.setText(dtUtils.formatDateTime(result.getReminder3()));
//			} else {
//				reminder3.setText("");
//			}
//			if (result.getAlarm1() != null) {
//				alarm1.setText(dtUtils.formatDateTime(result.getAlarm1()));
//			} else {
//				alarm1.setText("");
//			}
//			if (result.getAlarm2() != null) {
//				alarm2.setText(dtUtils.formatDateTime(result.getAlarm2()));
//			} else {
//				alarm2.setText("");
//			}
//			if (result.getAlarm3() != null) {
//				alarm3.setText(dtUtils.formatDateTime(result.getAlarm3()));
//			} else {
//				alarm3.setText("");
//			}

			if (!event.isNative()) {
				showInvitesView(EventEditActivity.this);
				if (result.getInvited().size() > 0) {
					invitationResponseLine.setVisibility(View.VISIBLE);
					respondToInvitation(result.getStatus());
				}
			} else {
				inviteButton.setVisibility(View.INVISIBLE);
			}

			pd.dismiss();
		}
	}

	class UpdateEventStatusTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			event = setEventData(event);
			int testEvent = event.isValid();
			if (testEvent == 0) {
				EventManagement.respondToInvitation(EventEditActivity.this, event);
				return true;
			} else {
				errorStr = setErrorStr(testEvent);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pb.setVisibility(View.GONE);
			saveButton.setText(getString(R.string.save));
			if (!result) {
				showDialog(DIALOG_ERROR);
			}
		}
	}

	class UpdateEventTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			if (event.getType().contentEquals("v")) {

//				for (int i = 0; i < eventPollSize; i++) {
//					ArrayList<Event> list2 = NavbarActivity.pollsList;
//					for (Event tempEvent : list2) {
//						if (tempEvent.getEvent_id() == event.getEvent_id()) {
//							NavbarActivity.pollsList.remove(tempEvent);
//							break;
//						}
//					}
//				}
				deleteEventFromPollList(event);

				if(poll_status != Invited.REJECTED){
					try {
						ArrayList<JSONObject> listToAdd = selectedPollTime;
						if (!listToAdd.isEmpty()) {
							for (JSONObject e : listToAdd) {
								event = EventManagement.getEventFromLocalDb(EventEditActivity.this, event.getEvent_id(),
										EventManagement.ID_EXTERNAL);
								event.setStartCalendar(Utils.stringToCalendar(EventEditActivity.this, e.getString("start"),
										DataManagement.SERVER_TIMESTAMP_FORMAT));
								event.setEndCalendar(Utils.stringToCalendar(EventEditActivity.this, e.getString("end"),
										DataManagement.SERVER_TIMESTAMP_FORMAT));
								String jsonArrayString = event.getPoll();
								if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
									JSONArray jsonArray = new JSONArray(jsonArrayString);
									for (int i = 0; i < jsonArray.length(); i++) {
										JSONObject e2 = jsonArray.getJSONObject(i);
										if (e.getString("timestamp_start_utc").contentEquals(e2.getString("timestamp_start_utc"))
												&& e.getString("id").contentEquals(e2.getString("id"))) {
											NavbarActivity.pollsList.add(event);
											break;
										}
									}
								}
							}
						} else {
//							String jsonArrayString = event.getPoll();
//							if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
//								JSONArray jsonArray = new JSONArray(jsonArrayString);
//								for (int i = 0; i < jsonArray.length(); i++) {
//									JSONObject e = jsonArray.getJSONObject(i);
//									event = EventManagement.getEventFromLocalDb(EventEditActivity.this, event.getEvent_id(),
//											EventManagement.ID_EXTERNAL);
//									event.setStartCalendar(Utils.stringToCalendar(EventEditActivity.this, e.getString("start"),
//											DataManagement.SERVER_TIMESTAMP_FORMAT));
//									event.setEndCalendar(Utils.stringToCalendar(EventEditActivity.this, e.getString("end"),
//											DataManagement.SERVER_TIMESTAMP_FORMAT));
//									NavbarActivity.pollsList.add(event);
//								}
//							}
							addEventToPollList(EventEditActivity.this, event);
						}
	
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}				

				event.setStatus(poll_status);
				event.setSelectedEventPollsTime("" + selectedPollTime);
				EventManagement.updateEventInLocalDb(EventEditActivity.this, event);
				if(!to_reject_poll && !to_rejoin_poll){
					EventManagement.votePoll(getApplicationContext(), "" + event.getEvent_id(), allEventPolls, "0");
					EventManagement.votePoll(getApplicationContext(), "" + event.getEvent_id(), selectedPollTime, "1");
				} else {
					if(to_reject_poll){
						EventManagement.rejectPoll(EventEditActivity.this, ""+event.getEvent_id());
					}
					if(to_rejoin_poll){
						EventManagement.rejoinPoll(EventEditActivity.this, ""+event.getEvent_id());
					}
				}

				return true;
			} else {
				if (isInvited) {
					if (EventManagement.inviteExtraContacts(EventEditActivity.this, "" + event.getEvent_id(), selectedContacts)) {
						return true;
					} else {
						errorStr = "Invite wasn't successfull.";
						return false;
					}
				} else {
					event = setEventData(event);

					if (event.getColor().equals(Event.DEFAULT_COLOR)) {
						EventEditActivity.this.setAutoColor(EventEditActivity.this);
					}

					if (event.getIcon().equals(Event.DEFAULT_ICON)) {
						EventEditActivity.this.setAutoIcon(EventEditActivity.this);
					}

					int testEvent = event.isValid();
					if (testEvent == 0) {
						EventManagement.updateEvent(EventEditActivity.this, event);
						return true;
					} else {
						errorStr = setErrorStr(testEvent);
						return false;
					}
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
			} else {
				showDialog(DIALOG_ERROR);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case EventActivity.DIALOG_ERROR:
			builder.setMessage(errorStr).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case EventEditActivity.DELETE_DIALOG:
			builder.setMessage(getString(R.string.sure_delete)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							new DeleteEventTask().execute();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			break;
		}
		return builder.create();
	}

	class DeleteEventTask extends AsyncTask<Void, Boolean, Boolean> {
		@Override
		protected Boolean doInBackground(Void... type) {
			if (event_internal_id > 0) {
				EventManagement.deleteEvent(EventEditActivity.this, event);
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Toast toast = Toast.makeText(EventEditActivity.this, "", Toast.LENGTH_LONG);
			if (result) {
				toast.setText(getString(R.string.event_deleted));
			} else {
				toast.setText(EventManagement.getError());
			}
			toast.show();

			super.onPostExecute(result);
			onBackPressed();
		}

	}

	private void showView(View view, LinearLayout line) {
		if (line != null) {
			if (addressPanelVisible) {
				line.setVisibility(View.VISIBLE);
				LinearLayout parent = (LinearLayout) view.getParent();
				parent.setVisibility(View.VISIBLE);
			} else if (!addressPanelVisible) {
				line.setVisibility(View.VISIBLE);
				LinearLayout parent = (LinearLayout) view.getParent();
				parent.setVisibility(View.GONE);
			}
		}
	}

	private void showDateTimeDialog(final EditText view, final int id) {
		// TODO put to parent
		// Create the dialog
		final Dialog mDateTimeDialog = new Dialog(this);
		// Inflate the root layout
		final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
		// Grab widget instance
		final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);

		Calendar c = Calendar.getInstance();
		switch (id) {
		case EventActivity.DIALOG_START:
			c = startCalendar;
			break;
		case EventActivity.DIALOG_END:
			c = endCalendar;
			break;
		}
		mDateTimePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateTimePicker.updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

		final boolean is24h = !CalendarSettings.isUsing_AM_PM(EventEditActivity.this);
		// Setup TimePicker
		mDateTimePicker.setIs24HourView(is24h);

		// Update demo TextViews when the "OK" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDateTimePicker.clearFocus();
				switch (id) {
				case DIALOG_START:
					startCalendar = mDateTimePicker.getCalendar();
					if (!allDayToggleButton.isChecked()) {
						startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
					} else {
						startView.setText(dtUtils.formatDate(startCalendar));
					}
					endCalendar = Calendar.getInstance();
					endCalendar.setTime(mDateTimePicker.getCalendar().getTime());
					endCalendar.add(Calendar.MINUTE, NewEventActivity.DEFAULT_EVENT_DURATION_IN_MINS);
					if (!allDayToggleButton.isChecked()) {
						endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
					} else {
						endView.setText(dtUtils.formatDate(endCalendar));
					}
					break;
				case DIALOG_END:
					endCalendar = mDateTimePicker.getCalendar();
					break;
				case ALARM1:
					alarm1time = mDateTimePicker.getCalendar();
					break;
				case ALARM2:
					alarm2time = mDateTimePicker.getCalendar();
					break;
				case ALARM3:
					alarm3time = mDateTimePicker.getCalendar();
					break;
				case REMINDER1:
					reminder1time = mDateTimePicker.getCalendar();
					break;
				case REMINDER2:
					reminder2time = mDateTimePicker.getCalendar();
					break;
				case REMINDER3:
					reminder3time = mDateTimePicker.getCalendar();
					break;
				}
				if (!allDayToggleButton.isChecked()) {
					view.setText(dtUtils.formatDateTime(mDateTimePicker.getCalendar()));
				} else {
					view.setText(dtUtils.formatDate(mDateTimePicker.getCalendar()));
				}
				saveButton.setEnabled(true);
				mDateTimeDialog.dismiss();
			}
		});

		// Cancel the dialog when the "Cancel" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDateTimeDialog.cancel();
			}
		});

		// Reset Date and Time pickers when the "Reset" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDateTimePicker.reset();
			}
		});

		// No title on the dialog window
		mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Set the dialog content view
		mDateTimeDialog.setContentView(mDateTimeDialogView);
		// Display the dialog
		mDateTimeDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_event:
			showDialog(DELETE_DIALOG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (changesMade) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(this.getResources().getString(R.string.save_your_changes))
					.setMessage(this.getResources().getString(R.string.do_you_want_to_save_your_changes))
					.setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new UpdateEventTask().execute();
							dialog.dismiss();
						}

					}).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}

					}).setCancelable(false).show();
		} else {
			super.onBackPressed();
		}
	}

	public void showToast(String msg, int length) {
		Toast.makeText(this, msg, length).show();
	}
	
	public static void deleteEventFromPollList(Event event){
		String jsonArrayString = event.getPoll();
		try {
			if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
				final JSONArray jsonArray = new JSONArray(jsonArrayString);
				int eventPollSize = jsonArray.length();
				for (int i = 0; i < eventPollSize; i++) {
					ArrayList<Event> list2 = NavbarActivity.pollsList;
					for (Event tempEvent : list2) {
						if (tempEvent.getEvent_id() == event.getEvent_id()) {
							NavbarActivity.pollsList.remove(tempEvent);
							break;
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static void addEventToPollList(Context context, Event event){
		String jsonArrayString = event.getPoll();
		try {
			if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
				JSONArray jsonArray = new JSONArray(jsonArrayString);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject e = jsonArray.getJSONObject(i);
					event = EventManagement.getEventFromLocalDb(context, event.getEvent_id(),
							EventManagement.ID_EXTERNAL);
					event.setStartCalendar(Utils.stringToCalendar(context, e.getString("start"),
							DataManagement.SERVER_TIMESTAMP_FORMAT));
					event.setEndCalendar(Utils.stringToCalendar(context, e.getString("end"),
							DataManagement.SERVER_TIMESTAMP_FORMAT));
					NavbarActivity.pollsList.add(event);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onResume();
		}
	};
}
