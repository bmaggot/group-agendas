package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.templates.TemplatesAdapter;
import com.groupagendas.groupagenda.templates.TemplatesDialog;
import com.groupagendas.groupagenda.templates.TemplatesDialogData;
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class NewEventActivity extends EventActivity {

	@SuppressWarnings("unused")
	private Button templatesButton;

	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	// private ArrayList<AutoColorItem> autoColors = null;
	// private ArrayList<AutoIconItem> autoIcons = null;

	boolean addressPanelVisible = false;
	boolean detailsPanelVisible = false;
	int templateInUse = 0;

	private boolean remindersShown = false;
	private boolean alarmsShown = false;
	private CheckBox templateTrigger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_event);
		account = new Account(this);
		selectedContacts = null;
		selectedGroups = null;

		startCalendar.clear(Calendar.SECOND);
		endCalendar.clear(Calendar.SECOND);
		endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);
		dtUtils = new DateTimeUtils(this);

		// new GetAutoTask().execute();
		// new GetContactsTask().execute(); //TODO investigate

		prefs = new Prefs(this);

		pb = (ProgressBar) findViewById(R.id.progress);
		templateTrigger = (CheckBox) findViewById(R.id.templateTrigger);
		saveButton = (Button) findViewById(R.id.saveButton);
		templatesButton = (Button) findViewById(R.id.templatesButton);

		// icon
		final String[] iconsValues = getResources().getStringArray(R.array.icons_values);
		iconView = (ImageView) findViewById(R.id.iconView);
		iconView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(NewEventActivity.this);
				dialog.setContentView(R.layout.list_dialog);
				dialog.setTitle(R.string.choose_icon);

				GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
				gridview.setAdapter(new IconsAdapter(NewEventActivity.this, iconsValues));

				gridview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						if (iconsValues[position].equals("noicon")) {
							iconView.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
						} else {
							selectedIcon = iconsValues[position];
							int iconId = getResources().getIdentifier(iconsValues[position], "drawable", "com.groupagendas.groupagenda");
							iconView.setImageResource(iconId);
						}
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});

		// color
		final String[] colorsValues = getResources().getStringArray(R.array.colors_values);
		colorView = (ImageView) findViewById(R.id.colorView);
		colorView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(this, COLOURED_BUBBLE_SIZE, selectedColor, true));

		colorView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(NewEventActivity.this);
				dialog.setContentView(R.layout.list_dialog);
				dialog.setTitle(R.string.choose_color);

				GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
				gridview.setAdapter(new ColorsAdapter(NewEventActivity.this, colorsValues));

				gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						selectedColor = (colorsValues[position]);
						colorView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(NewEventActivity.this, COLOURED_BUBBLE_SIZE,
								selectedColor, true));

						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});

		// title
		titleView = (EditText) findViewById(R.id.title);
		titleView.setEnabled(true);
		// titleView.addTextChangedListener(filterTextWatcher);

		String strStartTime = getIntent().getStringExtra(EXTRA_STRING_FOR_START_CALENDAR);
		if (strStartTime != null) {
			startCalendar = Utils.stringToCalendar(getApplicationContext(), strStartTime, DataManagement.SERVER_TIMESTAMP_FORMAT);
			startCalendar.clear(Calendar.SECOND);
			endCalendar.setTime(startCalendar.getTime());
			endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);

		}

		String strEndTime = getIntent().getStringExtra(EXTRA_STRING_FOR_END_CALENDAR);
		if (strEndTime != null) {
			endCalendar = Utils.stringToCalendar(getApplicationContext(), strEndTime, DataManagement.SERVER_TIMESTAMP_FORMAT);
			endCalendar.clear(Calendar.SECOND);

		}
		// start
		startView = (EditText) findViewById(R.id.startView);
		startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateTimeDialog(startView, DIALOG_START);
			}
		});
		// end
		endView = (EditText) findViewById(R.id.endView);
		endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
		endButton = (Button) findViewById(R.id.endButton);
		endButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateTimeDialog(endView, DIALOG_END);
			}
		});

		// allday
		allDayToggleButton = (ToggleButton) findViewById(R.id.allDayToggleButton);
		allDayToggleButton.setChecked(false);
		allDayToggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!allDayToggleButton.isChecked()) {
					startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
					endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
				} else {
					startView.setText(dtUtils.formatDate(startCalendar));
					endView.setText(dtUtils.formatDate(endCalendar));
				}
				saveButton.setEnabled(true);
			}
		});
		// Description
		descView = (EditText) findViewById(R.id.descView);

		// Address
		final LinearLayout addressPanel = (LinearLayout) findViewById(R.id.addressLine);
		// timezone
		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneView = (TextView) findViewById(R.id.timezoneView);
		timezoneSpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(NewEventActivity.this);
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

		// country
		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (TextView) findViewById(R.id.countryView);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(NewEventActivity.this);
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
					public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
						timezoneInUse = Integer.parseInt(view.getTag().toString());
						countryView.setText(countriesList.get(timezoneInUse).country2);
						event.setCountry(countriesList.get(timezoneInUse).country_code);

						filteredCountriesList = new ArrayList<StaticTimezones>();

						for (StaticTimezones tz : countriesList) {
							if (tz.country_code.equalsIgnoreCase(event.getCountry())) {
								filteredCountriesList.add(tz);
							}
						}

						timezonesAdapter = new TimezonesAdapter(NewEventActivity.this, R.layout.search_dialog_item, filteredCountriesList);
						timezonesAdapter.notifyDataSetChanged();

						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						event.setTimezone(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		// city
		cityView = (EditText) findViewById(R.id.cityView);
		// street
		streetView = (EditText) findViewById(R.id.streetView);
		// zip
		zipView = (EditText) findViewById(R.id.zipView);

		// Details
		final LinearLayout detailsPanel = (LinearLayout) findViewById(R.id.detailsLine);
		detailsPanel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (detailsPanelVisible) {
					hideDetailsPanel(addressPanel, detailsPanel);
				} else {
					showDetailsPanel();
				}
			}
		});
		addressPanel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (addressPanelVisible) {
					hideAddressPanel(addressPanel, detailsPanel);
				} else {
					showAddressPanel();
				}
			}
		});
		// location
		locationView = (EditText) findViewById(R.id.locationView);
		// Go by
		gobyView = (EditText) findViewById(R.id.gobyView);
		// Take with you
		takewithyouView = (EditText) findViewById(R.id.takewithyouView);
		// Cost
		costView = (EditText) findViewById(R.id.costView);
		// Accomodation
		accomodationView = (EditText) findViewById(R.id.accomodationView);

		hideAddressPanel(addressPanel, detailsPanel);
		hideDetailsPanel(addressPanel, detailsPanel);
		addressDetailsPanel = (RelativeLayout) findViewById(R.id.addressDetailsLine);
		addressPanel.setVisibility(View.GONE);
		detailsPanel.setVisibility(View.GONE);
		addressDetailsPanel.setVisibility(View.VISIBLE);
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

		// reminders
		final LinearLayout reminderBlock = (LinearLayout) findViewById(R.id.reminder_block);
		TextView setReminderTrigger = (TextView) findViewById(R.id.setReminderTrigger);
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

		LinearLayout reminder1container = (LinearLayout) findViewById(R.id.reminder_container1);
		final EditText reminder1 = (EditText) findViewById(R.id.reminder1);
		reminder1.setText("");
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

		LinearLayout reminder2container = (LinearLayout) findViewById(R.id.reminder_container2);
		final EditText reminder2 = (EditText) findViewById(R.id.reminder2);
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

		LinearLayout reminder3container = (LinearLayout) findViewById(R.id.reminder_container3);
		final EditText reminder3 = (EditText) findViewById(R.id.reminder3);
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

		// alarms
		final LinearLayout alarmBlock = (LinearLayout) findViewById(R.id.alarm_block);
		TextView setAlarmTrigger = (TextView) findViewById(R.id.setAlarmTrigger);
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

		LinearLayout alarm1container = (LinearLayout) findViewById(R.id.alarm_container1);
		final EditText alarm1 = (EditText) findViewById(R.id.alarm1);
		if (event != null && event.getAlarm1() != null) {
			alarm1.setText(Utils.formatCalendar(event.getAlarm1(), DataManagement.SERVER_TIMESTAMP_FORMAT));
		} else {
			alarm1.setText("");
		}
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

		LinearLayout alarm2container = (LinearLayout) findViewById(R.id.alarm_container2);
		final EditText alarm2 = (EditText) findViewById(R.id.alarm2);
		if (event != null && event.getAlarm2() != null) {
			alarm2.setText(Utils.formatCalendar(event.getAlarm2(), DataManagement.SERVER_TIMESTAMP_FORMAT));
		} else {
			alarm2.setText("");
		}
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

		LinearLayout alarm3container = (LinearLayout) findViewById(R.id.alarm_container3);
		final EditText alarm3 = (EditText) findViewById(R.id.alarm3);
		if (event != null && event.getAlarm3() != null) {
			alarm3.setText(Utils.formatCalendar(event.getAlarm3(), DataManagement.SERVER_TIMESTAMP_FORMAT));
		} else {
			alarm3.setText("");
		}
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
		});

		// INVITES SECTION
		invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
		invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
		super.inviteButton = (Button) findViewById(R.id.invite_button);
		super.inviteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(NewEventActivity.this, ContactsActivity.class);
				i.putExtra(ContactsActivity.TASK_MODE_KEY, ContactsActivity.TASK_MODE_SELECTION);
				i.putExtra(ContactsActivity.LIST_MODE_KEY, ContactsActivity.LIST_MODE_CONTACTS);
				i.putExtra(ContactsActivity.DESTINATION_KEY, ContactsActivity.DEST_EVENT_ACTIVITY);
				Data.showSaveButtonInContactsForm = true;
				// TODO Data.eventForSavingNewInvitedPersons = event;
				startActivity(i);
			}
		});

		super.event = new Event();
	}

	@Override
	public void onResume() {
		super.onResume();
		account = new Account(this);
		invitationResponseLine = (RelativeLayout) findViewById(R.id.response_to_invitation);
		invitationResponseStatus = (TextView) findViewById(R.id.status);

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
			countriesAdapter = new CountriesAdapter(NewEventActivity.this, R.layout.search_dialog_item, countriesList);
		}

		String tmz = account.getTimezone();
		String countryCode = "";
		for (StaticTimezones item : countriesList) {
			if (item.timezone.equalsIgnoreCase(tmz)) {
				timezoneInUse = Integer.parseInt(item.id);
				countryView.setText(countriesList.get(timezoneInUse).country2);
				countryCode = countriesList.get(timezoneInUse).country_code;
				continue;
			}
		}

		filteredCountriesList = new ArrayList<StaticTimezones>();

		for (StaticTimezones tz : countriesList) {
			if (tz.country_code.equalsIgnoreCase(countryCode)) {
				filteredCountriesList.add(tz);
			}
		}

		timezonesAdapter = new TimezonesAdapter(NewEventActivity.this, R.layout.search_dialog_item, filteredCountriesList);
		timezonesAdapter.notifyDataSetChanged();

		// INVITES SECTION
		response_button_yes = (TextView) findViewById(R.id.button_yes);
		response_button_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				respondToInvitation(1);
			}
		});

		response_button_no = (TextView) findViewById(R.id.button_no);
		response_button_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				respondToInvitation(0);
			}
		});

		response_button_maybe = (TextView) findViewById(R.id.button_maybe);
		response_button_maybe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				respondToInvitation(2);
			}
		});

		timezoneView.setText(account.getTimezone());

		LinearLayout invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
		invitedPersonList.removeAllViews();

		if (selectedContacts == null)
			selectedContacts = new ArrayList<Contact>();

		if (selectedGroups == null)
			selectedGroups = new ArrayList<Group>();

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
			if (!contains) {
				EventActivity.newInvites.add(nu);
			}
		}

		event.setInvited(new ArrayList<Invited>());

		showInvitesView(NewEventActivity.this);

		if (event.getInvited().size() > 0) {
			invitationResponseLine.setVisibility(View.VISIBLE);
			respondToInvitation(event.getStatus());
		} else {
			invitationResponseLine.setVisibility(View.GONE);
		}
	}

	// public View getInvitedView(Invited invited, LayoutInflater inflater, View
	// view, Context mContext) {
	// final TextView nameView = (TextView)
	// view.findViewById(R.id.invited_fullname);
	// nameView.setText(invited.name);
	//
	// final TextView emailView = (TextView)
	// view.findViewById(R.id.invited_available_email);
	// emailView.setText(invited.email);
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
	// view.setId(99999);
	// }
	//
	// return view;
	// }

	// private TextWatcher filterTextWatcher = new TextWatcher() {
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count, int
	// after) {
	// }
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before, int
	// count) {
	// if (s != null) {
	// if (event.icon == null || event.icon.equals("null")) {
	// for (int i = 0, l = autoIcons.size(); i < l; i++) {
	// final AutoIconItem autoIcon = autoIcons.get(i);
	// if (s.toString().contains(autoIcon.keyword)) {
	// event.icon = autoIcon.icon;
	// int iconId = getResources().getIdentifier(autoIcon.icon, "drawable",
	// "com.groupagendas.groupagenda");
	// iconView.setImageResource(iconId);
	// }
	// }
	// }

	// if (event.color == null || event.color.equals("null") ||
	// event.color.equals("")) {
	// for (int i = 0, l = autoColors.size(); i < l; i++) {
	// final AutoColorItem autoColor = autoColors.get(i);
	// if (s.toString().contains(autoColor.keyword)) {
	// event.color = autoColor.color;
	// String nameColor = "calendarbubble_" + autoColor.color + "_";
	// int image = getResources().getIdentifier(nameColor,
	// "drawable", "com.groupagendas.groupagenda");
	// colorView.setImageResource(image);
	// }
	// }
	// }
	// }
	// }
	//
	// };

	public void saveEvent(View v) {
		if (!saveButton.getText().toString().equalsIgnoreCase(getResources().getString(R.string.saving))) {
			if (!templateTrigger.isChecked()) {
				new NewEventTask().execute();
			} else {
				Toast.makeText(this, R.string.saving_new_template, Toast.LENGTH_SHORT).show();
				try {
					new NewTemplateTask().execute().get();
					Toast.makeText(this, R.string.new_event_saved, Toast.LENGTH_SHORT).show();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} else {
			Toast.makeText(NewEventActivity.this, R.string.wait, Toast.LENGTH_SHORT);
		}
	}

	/**
	 * Display template list dialogue.
	 * 
	 * Retrieves list of available templates titles from SQLite database and
	 * displays it in a dialogue.
	 * 
	 * @author meska.lt@gmail.com
	 * @param v
	 * 
	 */
	public void chooseTemplate(View v) {
		String columns[] = { TemplatesMetaData.T_ID, TemplatesMetaData.TITLE };
		Cursor cur;
		ArrayList<TemplatesDialogData> dialogData = new ArrayList<TemplatesDialogData>();
		TemplatesDialogData temp;
		TemplatesAdapter dialogDataAdapter = null;

		cur = this.getContentResolver().query(TemplatesMetaData.CONTENT_URI, columns, null, null, TemplatesMetaData.DEFAULT_SORT_ORDER);

		if (cur.moveToFirst()) {
			while (!cur.isAfterLast()) {
				temp = new TemplatesDialogData();

				temp.setID(cur.getString(cur.getColumnIndex(TemplatesMetaData.T_ID)));
				temp.setTitle(cur.getString(cur.getColumnIndex(TemplatesMetaData.TITLE)));

				dialogData.add(temp);

				cur.moveToNext();
			}
		} else {
			Log.e("chooseTemplate", "No response from local db");
		}

		cur.close();

		if (dialogData != null) {
			dialogDataAdapter = new TemplatesAdapter(this, dialogData);
		}

		TemplatesDialog templateListDialog = new TemplatesDialog(NewEventActivity.this, dialogDataAdapter);
		templateListDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (Data.templateInUse > 0) {
					event = DataManagement.getInstance(NewEventActivity.this).getTemplateFromLocalDb(Data.templateInUse);
					setUIValues(event);
					Data.templateInUse = 0;
				}
			}
		});
		templateListDialog.show();
	}

	protected Event setEventData(Event event) {
		event = super.setEventData(event);
		event.setCreatedMillisUtc(Calendar.getInstance().getTimeInMillis()); // set
																				// create
																				// time
		event.setModifiedMillisUtc(event.getCreatedUtc());
		event.setCreator_fullname(getString(R.string.you));
		event.setIs_owner(true);
		event.setStatus(Invited.ACCEPTED);
		event.setUser_id(account.getUser_id());
		return event;
	}

	public void showAddressPanel() {
		addressPanelVisible = true;

		LinearLayout timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneSpinnerBlock.setVisibility(View.VISIBLE);

		LinearLayout countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countrySpinnerBlock.setVisibility(View.VISIBLE);

		LinearLayout cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityViewBlock.setVisibility(View.VISIBLE);

		LinearLayout streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetViewBlock.setVisibility(View.VISIBLE);

		LinearLayout zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideAddressPanel(LinearLayout addressPanel, LinearLayout detailsPanel) {
		addressPanelVisible = false;
		if (!detailsPanelVisible && addressDetailsPanel != null && addressPanel != null && detailsPanel != null) {
			addressDetailsPanel.setVisibility(View.VISIBLE);
			addressPanel.setVisibility(View.GONE);
			detailsPanel.setVisibility(View.GONE);
		}
		LinearLayout timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneSpinnerBlock.setVisibility(View.GONE);

		LinearLayout countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countrySpinnerBlock.setVisibility(View.GONE);

		LinearLayout cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityViewBlock.setVisibility(View.GONE);

		LinearLayout streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetViewBlock.setVisibility(View.GONE);

		LinearLayout zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipViewBlock.setVisibility(View.GONE);
	}

	public void showDetailsPanel() {
		detailsPanelVisible = true;
		LinearLayout locationViewBlock = (LinearLayout) findViewById(R.id.locationViewBlock);
		locationViewBlock.setVisibility(View.VISIBLE);

		LinearLayout gobyViewBlock = (LinearLayout) findViewById(R.id.gobyViewBlock);
		gobyViewBlock.setVisibility(View.VISIBLE);

		LinearLayout takewithyouViewBlock = (LinearLayout) findViewById(R.id.takewithyouViewBlock);
		takewithyouViewBlock.setVisibility(View.VISIBLE);

		LinearLayout costViewBlock = (LinearLayout) findViewById(R.id.costViewBlock);
		costViewBlock.setVisibility(View.VISIBLE);

		LinearLayout accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationViewBlock);
		accomodationViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideDetailsPanel(LinearLayout addressPanel, LinearLayout detailsPanel) {
		detailsPanelVisible = false;
		if (!addressPanelVisible && addressDetailsPanel != null && addressPanel != null && detailsPanel != null) {
			addressDetailsPanel.setVisibility(View.VISIBLE);
			addressPanel.setVisibility(View.GONE);
			detailsPanel.setVisibility(View.GONE);
		}
		LinearLayout locationViewBlock = (LinearLayout) findViewById(R.id.locationViewBlock);
		locationViewBlock.setVisibility(View.GONE);

		LinearLayout gobyViewBlock = (LinearLayout) findViewById(R.id.gobyViewBlock);
		gobyViewBlock.setVisibility(View.GONE);

		LinearLayout takewithyouViewBlock = (LinearLayout) findViewById(R.id.takewithyouViewBlock);
		takewithyouViewBlock.setVisibility(View.GONE);

		LinearLayout costViewBlock = (LinearLayout) findViewById(R.id.costViewBlock);
		costViewBlock.setVisibility(View.GONE);

		LinearLayout accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationViewBlock);
		accomodationViewBlock.setVisibility(View.GONE);
	}

	class NewEventTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			Toast.makeText(NewEventActivity.this, R.string.saving_new_event, Toast.LENGTH_SHORT).show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			event = NewEventActivity.this.setEventData(event);

			if (event.getColor().equals(Event.DEFAULT_COLOR)) {
				NewEventActivity.this.setAutoColor(NewEventActivity.this);
			}

			if (event.getIcon().equals(Event.DEFAULT_ICON)) {
				NewEventActivity.this.setAutoIcon(NewEventActivity.this);
			}

			int testEvent = event.isValid();
			if (testEvent == 0) {
				EventManagement.createNewEvent(NewEventActivity.this, event);
				return true;
			} else {
				errorStr = setErrorStr(testEvent);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				sendSms();
				Toast.makeText(NewEventActivity.this, R.string.new_event_saved, Toast.LENGTH_SHORT).show();
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
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ERROR:
			((AlertDialog) dialog).setMessage(errorStr);
			break;
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_ERROR:
			builder.setMessage(errorStr).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		// TODO is it used ? case CHOOSE_CONTACTS_DIALOG:
		// builder.setTitle(getString(R.string.choose_contacts))
		// .setMultiChoiceItems(titles, selections, new
		// DialogSelectionClickHandler())
		// .setPositiveButton(getString(R.string.ok), new
		// DialogButtonClickHandler());
		// break;
		}
		return builder.create();
	}

	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		@Override
		public void onClick(DialogInterface dialog, int clicked, boolean selected) {
			selections[clicked] = selected;
		}
	}

	// private class DialogButtonClickHandler implements
	// DialogInterface.OnClickListener {
	// @Override
	// public void onClick(DialogInterface dialog, int clicked) {
	// switch (clicked) {
	// case DialogInterface.BUTTON_POSITIVE:
	// ArrayList<Integer> list = new ArrayList<Integer>();
	//
	// for (int i = 0, l = ids.length; i < l; i++) {
	// if (selections[i]) {
	// list.add(ids[i]);
	// }
	// }
	//
	// event.setAssigned_contacts(new int[list.size()]);
	//
	// for (int i = 0, l = list.size(); i < l; i++) {
	// event.getAssigned_contacts()[i] = list.get(i);
	// }
	// break;
	// }
	// }
	// }

	class GetAutoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// autoColors = dm.getAutoColors();
			// autoIcons = dm.getAutoIcons();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			titleView.setEnabled(true);
			super.onPostExecute(result);
		}

	}

	class GetContactsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Contact> contacts = ContactManagement.getContactsFromLocalDb(NewEventActivity.this, null);
			int l = contacts.size();
			titles = new CharSequence[l];
			ids = new int[l];
			selections = new boolean[l];
			for (int i = 0; i < l; i++) {
				titles[i] = new StringBuilder(contacts.get(i).name).append(" ").append(contacts.get(i).lastname).toString();
				ids[i] = contacts.get(i).contact_id;
				selections[i] = false;
			}

			return null;
		}

	}

	private void showDateTimeDialog(final EditText view, final int id) { // TODO
																			// put
																			// to
																			// parent
		// Create the dialog
		final Dialog mDateTimeDialog = new Dialog(this);
		// Inflate the root layout
		final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
		// Grab widget instance
		final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);

		Calendar c = Calendar.getInstance();
		switch (id) {
		case DIALOG_START:
			c.setTime(startCalendar.getTime());
			break;
		case DIALOG_END:
			c.setTime(endCalendar.getTime());
			break;
		}
		mDateTimePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateTimePicker.updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

		// // Check is system is set to use 24h time (this doesn't seem to work
		// as
		// // expected though)
		// final String timeS =
		// android.provider.Settings.System.getString(getContentResolver(),
		// android.provider.Settings.System.TIME_12_24);
		final boolean is24h = !CalendarSettings.isUsing_AM_PM(NewEventActivity.this);
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
					endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);
					if (!allDayToggleButton.isChecked()) {
						endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
					} else {
						endView.setText(dtUtils.formatDate(endCalendar));
					}
					break;
				case DIALOG_END:
					endCalendar = mDateTimePicker.getCalendar();
					if (!allDayToggleButton.isChecked()) {
						endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
					} else {
						endView.setText(dtUtils.formatDate(endCalendar));
					}
					break;
				case ALARM1:
					alarm1time = mDateTimePicker.getCalendar();
					view.setText(dtUtils.formatDateTime(alarm1time));
					break;
				case ALARM2:
					alarm2time = mDateTimePicker.getCalendar();
					view.setText(dtUtils.formatDateTime(alarm2time));
					break;
				case ALARM3:
					alarm3time = mDateTimePicker.getCalendar();
					view.setText(dtUtils.formatDateTime(alarm3time));
					break;
				case EventActivity.REMINDER1:
					reminder1time = mDateTimePicker.getCalendar();
					view.setText(dtUtils.formatDateTime(reminder1time));
					break;
				case EventActivity.REMINDER2:
					reminder2time = mDateTimePicker.getCalendar();
					view.setText(dtUtils.formatDateTime(reminder2time));
					break;
				case EventActivity.REMINDER3:
					reminder3time = mDateTimePicker.getCalendar();
					view.setText(dtUtils.formatDateTime(reminder3time));
					break;
				}
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

	/*
	 * TODO Improve validation of event's fields + create array(list) of int's
	 * for getting exact errors.
	 */

	class NewTemplateTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			boolean success = false;

			NewEventActivity.super.setEventData(event);
			event.setStatus(Invited.ACCEPTED);

			int testEvent = event.isValid();

			if (testEvent == 0) {
				DataManagement.getInstance(NewEventActivity.this).createTemplate(getApplicationContext(), event);
				return true;
			} else {
				switch (testEvent) {
				case 1: // no title set
					errorStr = getString(R.string.title_is_required);
					break;

				case 2: // no timezone set
					errorStr = getString(R.string.timezone_required);
					break;

				case 3: // calendar fields are null
					// errorStr = getString(R.string.)
					break;

				case 4: // event start is set after end
					errorStr = getString(R.string.invalid_start_end_time);
					break;

				case 5: // event duration is 0
					errorStr = getString(R.string.invalid_start_end_time);
					break;

				default:
					break;
				}
			}

			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
				Toast.makeText(NewEventActivity.this, R.string.new_template_saved, Toast.LENGTH_LONG).show();
			} else {
				showDialog(DIALOG_ERROR);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}
	}

	/**
	 * Update user interface input fields.
	 * 
	 * Update user interface input fields with submitted values. Currently event
	 * timezone and country, start-end time, reminder and alarm field values
	 * aren't updated.
	 * 
	 * @author meska.lt@gmail.com
	 * @param data
	 *            - Event object with values set.
	 * @version 1.0
	 * @since 2012-09-24
	 */
	public void setUIValues(Event data) {
		EditText view;
		ImageView imageView;

		/* Set event's color & icon */
		imageView = (ImageView) findViewById(R.id.colorView);
		if (imageView != null) {
			int image = getResources().getIdentifier("calendarbubble_" + data.getColor() + "_", "drawable", "com.groupagendas.groupagenda");
			if (image != 0)
				imageView.setImageResource(image);
		}

		imageView = (ImageView) findViewById(R.id.iconView);
		if (imageView != null) {
			int image = getResources().getIdentifier(data.getIcon(), "drawable", "com.groupagendas.groupagenda");
			if (image != 0)
				imageView.setImageResource(image);
		}

		/* Set event's title */
		view = (EditText) findViewById(R.id.title);
		if (view != null)
			view.setText(data.getActualTitle());

		/* Set event's start & end time */
		view = (EditText) findViewById(R.id.startView);
		if (view != null)
			view.setText(dtUtils.formatDateTime(data.getStartCalendar()));

		view = (EditText) findViewById(R.id.endView);
		if (view != null)
			view.setText(dtUtils.formatDateTime(data.getEndCalendar()));

		/* Set event's description */
		view = (EditText) findViewById(R.id.descView);
		if (view != null)
			view.setText(data.getDescription());

		/* Set event's address */
		// TODO make country set
		// spinner = (Spinner) findViewById(R.id.countrySpinner);
		// if (spinner != null)
		// spinner.setSelection(position);

		view = (EditText) findViewById(R.id.cityView);
		if (view != null)
			view.setText(data.getCity());

		view = (EditText) findViewById(R.id.streetView);
		if (view != null)
			view.setText(data.getStreet());

		view = (EditText) findViewById(R.id.zipView);
		if (view != null)
			view.setText(data.getZip());

		// TODO make timezone set
		// view = (EditText) findViewById(R.id.timezoneSpinner);
		// if (view != null)
		// view.setText(data.getCity());

		/* Set event's details */
		view = (EditText) findViewById(R.id.locationView);
		if (view != null)
			view.setText(data.getLocation());

		view = (EditText) findViewById(R.id.gobyView);
		if (view != null)
			view.setText(data.getGo_by());

		view = (EditText) findViewById(R.id.takewithyouView);
		if (view != null)
			view.setText(data.getTake_with_you());

		view = (EditText) findViewById(R.id.costView);
		if (view != null)
			view.setText(data.getCost());

		view = (EditText) findViewById(R.id.accomodationView);
		if (view != null)
			view.setText(data.getAccomodation());

		/* Set event's reminders */
		// view = (EditText) findViewById(R.id.reminder1);
		// if (view != null)
		// view.setText(data.getReminder1());
		//
		// view = (EditText) findViewById(R.id.reminder3);
		// if (view != null)
		// view.setText(data.getReminder2());
		//
		// view = (EditText) findViewById(R.id.reminder3);
		// if (view != null)
		// view.setText(data.getReminder3());

		/* Set event's alarms */
		// view = (EditText) findViewById(R.id.alarm1);
		// if (view != null)
		// view.setText(data.getAlarm1());
		//
		// view = (EditText) findViewById(R.id.alarm2);
		// if (view != null)
		// view.setText(data.getAlarm2());
		//
		// view = (EditText) findViewById(R.id.alarm3);
		// if (view != null)
		// view.setText(data.getAlarm3());

	}
}
