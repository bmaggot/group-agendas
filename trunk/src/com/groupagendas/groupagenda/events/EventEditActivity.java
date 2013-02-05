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
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
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

import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountActivity;
import com.groupagendas.groupagenda.address.Address;
import com.groupagendas.groupagenda.address.AddressBookActivity;
import com.groupagendas.groupagenda.address.AddressBookInfoActivity;
import com.groupagendas.groupagenda.address.AddressManagement;
import com.groupagendas.groupagenda.alarm.Alarm;
import com.groupagendas.groupagenda.alarm.AlarmsManagement;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactInfoActivity;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeSelectActivity;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.SelectPollForCopyingDialog;
import com.groupagendas.groupagenda.utils.Utils;

public class EventEditActivity extends EventActivity {
	private static boolean dataLoaded = false;

	private class GenericTextWatcher implements TextWatcher {

		private String oldText = null;

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			oldText = charSequence.toString();
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
			if (!editable.toString().equalsIgnoreCase(oldText)) {
				changesMade = true;
				saveButton.setEnabled(changesMade);
				if (cityView.getText().length() > 0 || streetView.getText().length() > 0 || zipView.getText().length() > 0) {
					save_address.setVisibility(View.VISIBLE);
				}
				AddressBookActivity.selectedAddressId = 0;
			}
		}
	}

	private TextWatcher watcher;

	private TextView topText;
	private Button deleteButton;

	private TextView creatorNameTextView;

	private long event_internal_id;
	private long event_external_id;

	protected final static int DELETE_DIALOG = 1;
	private boolean isInvited = false;
	private static boolean changesMade = false;

	private Intent intent;

	private Button chatMessengerButton;

	private LinearLayout startViewBlock;
	private LinearLayout endViewBlock;
	private LinearLayout pollStartEndTime;
	private ProgressDialog pd;
	private int eventPollSize;
	ArrayList<JSONObject> allEventPolls;
	private LinearLayout attending_line;
	private static ArrayList<JSONObject> selectedPollTime;
	private RelativeLayout poll_status_line;
	private TextView reject_poll;
	private TextView rejoin_poll;
	private TextView poll_status_text;
	private int poll_status;
	private boolean to_rejoin_poll;
	private boolean to_reject_poll;
	private Button navigation;
	NavigationDialog navDialog;
	private Button see_results;

	private Button copyEventButton;
	private final String copy = "copy";

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
		AddressBookActivity.selectedAddressId = 0;
		setChangesMade(false);
		setEditInvited(false);
		showAlarmPanel();
		showReminderPanel();
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
		if ((event_internal_id > 0) && (!dataLoaded)) {
			new GetEventTask().execute(new Long[] { event_internal_id, event_external_id });
		} else {
			if (event.getInvited().size() > 0) {
				invitationResponseLine.setVisibility(View.VISIBLE);
				inviteEditButton.setVisibility(View.VISIBLE);
			}

			showInvitesView(this);

			countryView.setText(countriesList.get(timezoneInUse).country2);
			timezoneView.setText(countriesList.get(timezoneInUse).altname);
			startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
			endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
			changesMade = true;
			enableDisableButtons(changesMade);
			if (AddressBookActivity.selectedAddressId > 0) {
				Address address = AddressManagement.getAddressFromLocalDb(EventEditActivity.this, AddressBookActivity.selectedAddressId,
						AddressManagement.ID_INTERNAL);
				cityView.setText(address.getCity());
				streetView.setText(address.getStreet());
				zipView.setText(address.getZip());
				countryView.setText(address.getCountry_name());
				timezoneView.setText(address.getTimezone());
			}
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

		startViewBlock = (LinearLayout) findViewById(R.id.startViewBlock);
		endViewBlock = (LinearLayout) findViewById(R.id.endViewBlock);
		pollStartEndTime = (LinearLayout) findViewById(R.id.pollStartEndTime);
		attending_line = (LinearLayout) findViewById(R.id.attending_line);
		poll_status_line = (RelativeLayout) findViewById(R.id.poll_status_line);
		reject_poll = (TextView) findViewById(R.id.button_reject);
		rejoin_poll = (TextView) findViewById(R.id.button_rejoin);
		poll_status_text = (TextView) findViewById(R.id.poll_status);

		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setEnabled(changesMade);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// enableDisableButtons(false);
				if (intent.getBooleanExtra(copy, false)) {
					new CopyEventTask().execute();
				} else if (!saveButton.getText().toString().equalsIgnoreCase(getResources().getString(R.string.saving))) {
					new UpdateEventTask().execute();
					dataLoaded = false;
				} else {
					Toast.makeText(EventEditActivity.this, R.string.wait, Toast.LENGTH_SHORT).show();
				}
				sendSms();
			}
		});

		// Icon, color and title
		iconView = (ImageView) findViewById(R.id.iconView);
		colorView = (ImageView) findViewById(R.id.colorView);
		titleView = (EditText) findViewById(R.id.title);

		// Start and end time buttons
		startView = (TextView) findViewById(R.id.startView);
		startView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(EventEditActivity.this, DateTimeSelectActivity.class);
				i.putExtra(DateTimeSelectActivity.ACTIVITY_TARGET_KEY, DateTimeSelectActivity.TARGET_EVENT_EDIT);
				startActivity(i);
			}
		});

		endView = (TextView) findViewById(R.id.endView);
		endView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(EventEditActivity.this, DateTimeSelectActivity.class);
				i.putExtra(DateTimeSelectActivity.ACTIVITY_TARGET_KEY, DateTimeSelectActivity.TARGET_EVENT_EDIT);
				startActivity(i);
			}
		});

		// Description
		descView = (EditText) findViewById(R.id.descView);

		// Creator
		creatorNameTextView = (TextView) findViewById(R.id.event_creator_name);
		addressDetailsPanel = (LinearLayout) findViewById(R.id.addressDetailsLine);

		alarmTrigger = (RelativeLayout) findViewById(R.id.alarm_trigger);
		alarmTrigger.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (alarmPanelVisible) {
					hideAlarmPanel();
				} else {
					showAlarmPanel();
				}
			}
		});

		addressTrigger = (TextView) addressDetailsPanel.findViewById(R.id.addressTrigger);
		addressTrigger.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (addressPanelVisible) {
					hideAddressPanel();
				} else {
					showAddressPanel();
				}
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
		detailsTrigger = (TextView) addressDetailsPanel.findViewById(R.id.detailsTrigger);
		detailsTrigger.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (detailsPanelVisible) {
					hideDetailsPanel();
				} else {
					showDetailsPanel();
				}
			}
		});

		locationViewBlock = (LinearLayout) findViewById(R.id.locationViewBlock);
		locationView = (EditText) findViewById(R.id.locationView);
		gobyViewBlock = (LinearLayout) findViewById(R.id.gobyViewBlock);
		gobyView = (EditText) findViewById(R.id.gobyView);
		takewithyouViewBlock = (LinearLayout) findViewById(R.id.takewithyouViewBlock);
		takewithyouView = (EditText) findViewById(R.id.takewithyouView);
		costViewBlock = (LinearLayout) findViewById(R.id.costViewBlock);
		costView = (EditText) findViewById(R.id.costView);
		accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationViewBlock);
		accomodationView = (EditText) findViewById(R.id.accomodationView);

		chatMessengerButton = (Button) findViewById(R.id.messenger_button);

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
		copyEventButton = (Button) findViewById(R.id.copy_event_button);
		if (intent.getBooleanExtra(copy, false)) {
			copyEventButton.setVisibility(View.GONE);
		} else {
			copyEventButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (event.getType().contentEquals("v")) {
						showPollSelectionForCopyingDialog(event);
					} else {
						EventEditActivity.this.finish();
						Intent intent = new Intent(EventEditActivity.this, EventEditActivity.class);
						intent.putExtra("event_id", event.getInternalID());
						intent.putExtra("type", event.getType());
						intent.putExtra("isNative", event.isNative());
						intent.putExtra(copy, true);
						startActivity(intent);
					}
				}
			});
		}

		// invitedPersonListView = (ListView)
		// findViewById(R.id.invited_person_listview);

		super.inviteEditButton = (Button) findViewById(R.id.invite_edit_button);
		super.inviteEditButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int childCount = invitedPersonList.getChildCount();
				setEditInvited(!getEditInvited());
				for (int i = 0; i < childCount; i++) {

					if (getEditInvited()) {
						invitedPersonList.getChildAt(i).findViewById(R.id.invited_remove).setVisibility(View.VISIBLE);
					} else {
						invitedPersonList.getChildAt(i).findViewById(R.id.invited_remove).setVisibility(View.GONE);
					}
				}
			}
		});

		inviteDelegate1 = findViewById(R.id.invite_button_del1);
		inviteDelegate2 = (RelativeLayout) findViewById(R.id.invite_button_del2);

		inviteDelegate1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inviteButton.performClick();
			}
		});

		inviteDelegate2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inviteButton.performClick();
			}
		});

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

		navigation = (Button) findViewById(R.id.navigation_button);
		navigation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// showEventDirection(EventEditActivity.this,
				// "lukiskiu g. 5, vilnius",
				// streetView.getText()+", "+cityView.getText()+", "+countryView.getText().toString());
				// showEventLocation(EventEditActivity.this,
				// streetView.getText()+", "+cityView.getText()+", "+countryView.getText().toString());
				String eventAddress = "";
				if (streetView.getText().length() > 0 || cityView.getText().length() > 0 || countryView.getText().length() > 0) {
					eventAddress = streetView.getText() + ", " + cityView.getText() + ", " + countryView.getText().toString();
				}

				String homeAddress = "";
				if (!account.getStreet().contentEquals("null") && !account.getCity().contentEquals("null")) {
					homeAddress = account.getStreet() + ", " + account.getCity();
				}

				if (DataManagement.networkAvailable) {
					navDialog = new NavigationDialog(EventEditActivity.this, R.style.yearview_eventlist, homeAddress, eventAddress);
					navDialog.show();
				} else {
					Toast.makeText(EventEditActivity.this, getString(R.string.no_internet_conn), Toast.LENGTH_SHORT).show();
				}
			}
		});

		save_address = (Button) findViewById(R.id.save_address);
		save_address.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent addressCreate = new Intent(EventEditActivity.this, AddressBookInfoActivity.class);
				addressCreate.putExtra("action", false);
				addressCreate.putExtra("fill_info", true);
				startActivity(addressCreate);
			}
		});

		address = (Button) findViewById(R.id.address_button);
		address.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent address = new Intent(EventEditActivity.this, AddressBookActivity.class);
				address.putExtra("action", true);
				startActivity(address);
			}
		});

		see_results = (Button) findViewById(R.id.poll_results_button);
		see_results.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventEditActivity.this, PollResultsActivity.class);
				intent.putExtra("pollTime", event.getPoll());
				// event.getInvited().toString();
				// intent.putExtra("invited", event.getInvited().toString());
				startActivity(intent);

			}
		});

		initAlarms();
		initReminders();
	}

	class GetEventTask extends AsyncTask<Long, Event, Event> {
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
			if (intent.getBooleanExtra(copy, false)) {
				result.setIs_owner(true);
				result.setInvited(new ArrayList<Invited>());
			}
			account = new Account(EventEditActivity.this);
			event = result;
			selectedIcon = event.getIcon();

			if (!event.getStreet().contentEquals("") && !event.getCity().contentEquals("")) {
				navigation.setVisibility(View.VISIBLE);
			}

			if (event.getType().contentEquals("v") && !intent.getBooleanExtra(copy, false)) {
				startViewBlock.setVisibility(View.GONE);
				endViewBlock.setVisibility(View.GONE);
				pollStartEndTime.setVisibility(View.VISIBLE);
				invitationResponseLine.setVisibility(View.GONE);
				attending_line.setVisibility(View.GONE);
				poll_status_line.setVisibility(View.VISIBLE);
				pollStartEndTime.removeAllViews();
				to_reject_poll = false;
				to_rejoin_poll = false;

				descView.setEnabled(false);
				inviteButton.setEnabled(false);
				see_results.setVisibility(View.VISIBLE);

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

				selectedPollTime = getSelectedEventPollTimes(event);

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
							backgr.setPadding(DrawingUtils.convertDPtoPX(5), DrawingUtils.convertDPtoPX(5), DrawingUtils.convertDPtoPX(5),
									DrawingUtils.convertDPtoPX(5));

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
							Calendar nowCal = Calendar.getInstance();
							if (tempCal.getTimeInMillis() < nowCal.getTimeInMillis()) {
								selectedTime.setEnabled(false);
								selectedTime.setPadding(2, 0, 0, 0);
							}

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
				if (!intent.getBooleanExtra(copy, false)) {
					deleteButton.setVisibility(View.VISIBLE);
				}

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
								colorView.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundSquare(
										EventEditActivity.this, COLOURED_BUBBLE_SIZE, 5, colorsValues[position], false)));
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
				timezoneView.setEnabled(false);
				startView.setEnabled(false);
				cityView.setEnabled(false);
				streetView.setEnabled(false);
				zipView.setEnabled(false);
				locationView.setEnabled(false);
				gobyView.setEnabled(false);
				takewithyouView.setEnabled(false);
				costView.setEnabled(false);
				accomodationView.setEnabled(false);
				saveButton.setVisibility(View.GONE);
			}

			if (event.isNative()) {
				deleteButton.setVisibility(View.INVISIBLE);
			}

			int id = account.getUser_id();
			for (Invited inv : result.getInvited()) {
				if ((inv.getGuid() == id) && (inv.getGuid() != result.getUser_id())) {
					saveButton.setVisibility(View.VISIBLE);
					isInvited = true;
				}
			}

			colorView.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundSquare(EventEditActivity.this,
					COLOURED_BUBBLE_SIZE, 5, result.getColor(), false)));
			iconView.setImageResource(result.getIconId(EventEditActivity.this));

			// START AND END TIME
			if (result.getType().contentEquals("v") && intent.getBooleanExtra(copy, false)) {
				String[] times = intent.getStringExtra("times").split(" - ");
				result.setStartCalendar(Utils.stringToCalendar(EventEditActivity.this, times[0], "yyyy-MM-dd HH:mm"));
				result.setEndCalendar(Utils.stringToCalendar(EventEditActivity.this, times[1], "yyyy-MM-dd HH:mm"));
			}
			if (result.getStartCalendar() != null) {
				if (!event.is_all_day()) {
					startView.setText(dtUtils.formatDateTime(result.getStartCalendar()));
				} else {
					startView.setText(dtUtils.formatDate(result.getStartCalendar()));
				}
				startCalendar = (Calendar) result.getStartCalendar().clone();
			}
			if (result.getEndCalendar() != null) {
				if (!event.is_all_day()) {
					endView.setText(dtUtils.formatDateTime(result.getEndCalendar()));
				} else {
					endView.setText(dtUtils.formatDate(result.getEndCalendar()));
				}
				endCalendar = (Calendar) result.getEndCalendar().clone();
			}

			if (result.getDescription().length() > 0) {
				LinearLayout parent = (LinearLayout) descView.getParent();
				parent.setVisibility(View.VISIBLE);
				descView.setText(result.getDescription());
			}
			descView.addTextChangedListener(watcher);
			if (ContactManagement.getContactFromLocalDb(getApplicationContext(), result.getCreator_contact_id(), 0) != null
					&& !intent.getBooleanExtra(copy, false)) {
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
			} else if (result.getCreator_contact_id() == 0 && !result.is_owner() && !intent.getBooleanExtra(copy, false)) {
				creatorNameTextView.setText(result.getCreator_fullname());
			} else if (result.is_owner() || intent.getBooleanExtra(copy, false)) {
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
						if (event_internal_id > 0 && event.isUploadedToServer()) {
							Intent intent = new Intent(EventEditActivity.this, ChatMessageActivity.class);
							intent.putExtra("event_id", event.getEvent_id());
							startActivity(intent);
						} else {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.chat_on_event_not_created_online),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
			} else {
				chatMessengerButton.setVisibility(View.INVISIBLE);
			}

			if (!event.isNative()) {
				showInvitesView(EventEditActivity.this);
				if (result.getInvited().size() > 0) {
					invitationResponseLine.setVisibility(View.VISIBLE);
					inviteEditButton.setVisibility(View.VISIBLE);
					respondToInvitation(result.getStatus());
				}
			} else {
				inviteButton.setVisibility(View.INVISIBLE);
			}

			if (intent.getBooleanExtra(copy, false)) {
				saveButton.setVisibility(View.VISIBLE);
				saveButton.setEnabled(true);
			}

			Calendar calendar = Calendar.getInstance();
			if (result.getReminder1() != null && result.getReminder1().getTimeInMillis() > calendar.getTimeInMillis()) {
				reminder1View.setText(dtUtils.formatDateTime(result.getReminder1()));
			} else {
				reminder1View.setText("");
			}
			if (result.getReminder2() != null && result.getReminder2().getTimeInMillis() > calendar.getTimeInMillis()) {
				reminder2View.setText(dtUtils.formatDateTime(result.getReminder2()));
			} else {
				reminder2View.setText("");
			}
			if (result.getReminder3() != null && result.getReminder3().getTimeInMillis() > calendar.getTimeInMillis()) {
				reminder3View.setText(dtUtils.formatDateTime(result.getReminder3()));
			} else {
				reminder3View.setText("");
			}
			alarm1View.setText("");
			alarm2View.setText("");
			alarm3View.setText("");
			String selection = EventsProvider.EMetaData.AlarmsMetaData.EVENT_ID + "=" + event_external_id;
			Cursor cursor = getApplicationContext().getContentResolver().query(EventsProvider.EMetaData.AlarmsMetaData.CONTENT_URI, null,
					selection, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				calendar = Calendar.getInstance();
				DateTimeUtils dateTimeUtils = new DateTimeUtils(EventEditActivity.this);
				do {
					Alarm alarm = AlarmsManagement.createAlarmFromCursor(cursor);
					calendar.setTimeInMillis(alarm.getAlarmTimestamp());
					if (alarm1View.getText().toString().equals("")
							&& !alarm1View.getText().toString().equals(dateTimeUtils.formatDateTime(calendar))) {
						alarm1View.setText(dateTimeUtils.formatDateTime(calendar));
						if (alarm1time == null)
							alarm1time = Calendar.getInstance();
						alarm1time.setTimeInMillis(calendar.getTimeInMillis());
					} else if (alarm2View.getText().toString().equals("")
							&& !alarm2View.getText().toString().equals(dateTimeUtils.formatDateTime(calendar))) {
						alarm2View.setText(dateTimeUtils.formatDateTime(calendar));
						if (alarm2time == null)
							alarm2time = Calendar.getInstance();
						alarm2time.setTimeInMillis(calendar.getTimeInMillis());
					} else if (alarm3View.getText().toString().equals("")
							&& !alarm3View.getText().toString().equals(dateTimeUtils.formatDateTime(calendar))) {
						alarm3View.setText(dateTimeUtils.formatDateTime(calendar));
						if (alarm3time == null)
							alarm3time = Calendar.getInstance();
						alarm3time.setTimeInMillis(calendar.getTimeInMillis());
					}
					cursor.moveToNext();
				} while (!cursor.isAfterLast());
				cursor.close();
			}

			dataLoaded = true;
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
				deleteEventFromPollList(event);

				if (poll_status != Invited.REJECTED) {
					ArrayList<JSONObject> listToAdd = selectedPollTime;
					if (!listToAdd.isEmpty()) {
						addSelectedEventToPollList(EventEditActivity.this, event, selectedPollTime);
					} else {
						addEventToPollList(EventEditActivity.this, event);
					}
				}

				event.setStatus(poll_status);
				event.setSelectedEventPollsTime("" + selectedPollTime);
				// if (DataManagement.networkAvailable) {
				if (!to_reject_poll && !to_rejoin_poll) {
					// event.setUploadedToServer(EventManagement.votePoll(getApplicationContext(),
					// "" + event.getEvent_id(),
					// allEventPolls, "0"));
					// event.setUploadedToServer(EventManagement.votePoll(getApplicationContext(),
					// "" + event.getEvent_id(),
					// selectedPollTime, "1"));
					EventManagement.votePoll(getApplicationContext(), "" + event.getEvent_id(), allEventPolls, "0");
					EventManagement.votePoll(getApplicationContext(), "" + event.getEvent_id(), selectedPollTime, "1");
				} else {
					if (to_reject_poll) {
						EventManagement.rejectPoll(EventEditActivity.this, "" + event.getEvent_id());
					}
					if (to_rejoin_poll) {
						EventManagement.rejoinPoll(EventEditActivity.this, "" + event.getEvent_id());
					}
				}
				// } else {
				// event.setUploadedToServer(false);
				// }

				EventManagement.updateEventInLocalDb(EventEditActivity.this, event);

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
				if (event.getType().contentEquals("v")) {
					deleteEventFromPollList(event);
				}
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
							dataLoaded = false;
							dialog.dismiss();
						}

					}).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dataLoaded = false;
							dialog.dismiss();
							finish();
						}

					}).setCancelable(false).show();
		} else {
			dataLoaded = false;
			super.onBackPressed();
		}
	}

	public void showToast(String msg, int length) {
		Toast.makeText(this, msg, length).show();
	}

	public static void deleteEventFromPollList(Event event) {
		String jsonArrayString = event.getPoll();
		try {
			if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
				final JSONArray jsonArray = new JSONArray(jsonArrayString);
				int eventPollSize = jsonArray.length();
				for (int i = 0; i < eventPollSize; i++) {
					ArrayList<Event> list2 = NavbarActivity.pollsList;
					if (list2 == null) {
						break;
					}
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

	public static void addEventToPollList(Context context, Event event) {
		String jsonArrayString = event.getPoll();
		if (NavbarActivity.pollsList == null) {
			NavbarActivity.pollsList = new ArrayList<Event>();
		}
		try {
			if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
				JSONArray jsonArray = new JSONArray(jsonArrayString);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject e = jsonArray.getJSONObject(i);
					event = EventManagement.getEventFromLocalDb(context, event.getEvent_id(), EventManagement.ID_EXTERNAL);
					event.setStartCalendar(Utils.stringToCalendar(context, e.getString("start"), DataManagement.SERVER_TIMESTAMP_FORMAT));
					event.setEndCalendar(Utils.stringToCalendar(context, e.getString("end"), DataManagement.SERVER_TIMESTAMP_FORMAT));
					NavbarActivity.pollsList.add(event);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void addSelectedEventToPollList(Context context, Event event, ArrayList<JSONObject> selectedPollTime) {
		if (NavbarActivity.pollsList == null) {
			NavbarActivity.pollsList = new ArrayList<Event>();
		}
		try {
			for (JSONObject e : selectedPollTime) {
				event = EventManagement.getEventFromLocalDb(context, event.getEvent_id(), EventManagement.ID_EXTERNAL);
				event.setStartCalendar(Utils.stringToCalendar(context, e.getString("start"), DataManagement.SERVER_TIMESTAMP_FORMAT));
				event.setEndCalendar(Utils.stringToCalendar(context, e.getString("end"), DataManagement.SERVER_TIMESTAMP_FORMAT));
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<JSONObject> getSelectedEventPollTimes(Event event) {
		String jsonArraySelectedTime = event.getSelectedEventPollsTime();
		ArrayList<JSONObject> selectedPollTime = new ArrayList<JSONObject>();
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

		return selectedPollTime;
	}

	public static ArrayList<JSONObject> getAllEventPollTimes(Event event) {
		String jsonArrayString = event.getPoll();
		ArrayList<JSONObject> allEventPolls = new ArrayList<JSONObject>();
		try {
			if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
				final JSONArray jsonArray = new JSONArray(jsonArrayString);
				// eventPollSize = jsonArray.length();
				allEventPolls = new ArrayList<JSONObject>();
				for (int i = 0; i < jsonArray.length(); i++) {
					final JSONObject pollThread = jsonArray.getJSONObject(i);
					allEventPolls.add(pollThread);
				}
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}

		return allEventPolls;
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onResume();
		}
	};

	class CopyEventTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			Toast.makeText(EventEditActivity.this, R.string.copying_new_event, Toast.LENGTH_SHORT).show();

			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			event = EventEditActivity.this.setEventData(event);

			if (event.getColor().equals(Event.DEFAULT_COLOR)) {
				EventEditActivity.this.setAutoColor(EventEditActivity.this);
			}

			if (event.getIcon().equals(Event.DEFAULT_ICON)) {
				EventEditActivity.this.setAutoIcon(EventEditActivity.this);
			}

			int testEvent = event.isValid();
			if (testEvent == 0) {
				EventManagement.createNewEvent(EventEditActivity.this, event);
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
				Toast.makeText(EventEditActivity.this, R.string.new_event_copied, Toast.LENGTH_SHORT).show();
				finish();
			} else {
				showDialog(DIALOG_ERROR);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}

	}

	public void showPollSelectionForCopyingDialog(Event event) {
		String jsonArrayString = event.getPoll();
		ArrayList<String> pollsList = new ArrayList<String>();
		try {
			if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
				final JSONArray jsonArray = new JSONArray(jsonArrayString);
				for (int i = 0; i < jsonArray.length(); i++) {
					final JSONObject pollThread = jsonArray.getJSONObject(i);

					DateTimeUtils dateTimeUtils = new DateTimeUtils(EventEditActivity.this);

					String temp = "";
					try {
						temp = pollThread.getString("start");
					} catch (JSONException e) {
						Log.e("PollAdapter", "Failed getting poll time.");
					}

					final Calendar tempCal = Utils.stringToCalendar(EventEditActivity.this, temp, DataManagement.SERVER_TIMESTAMP_FORMAT);
					String start = dateTimeUtils.formatDate(tempCal) + " " + dateTimeUtils.formatTime(tempCal);
					try {
						temp = pollThread.getString("end");
					} catch (JSONException e) {
						Log.e("PollAdapter", "Failed getting poll time.");
					}

					final Calendar tempCal2 = Utils.stringToCalendar(EventEditActivity.this, temp, DataManagement.SERVER_TIMESTAMP_FORMAT);
					String end = dateTimeUtils.formatDate(tempCal2) + " " + dateTimeUtils.formatTime(tempCal2);

					Calendar nowCal = Calendar.getInstance();
					if (tempCal.getTimeInMillis() > nowCal.getTimeInMillis()) {
						pollsList.add(start + " - " + end);
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		SelectPollForCopyingDialog dialog = new SelectPollForCopyingDialog(this, pollsList, event);
		dialog.show();
	}

	public static void setChangesMade(boolean changed) {
		changesMade = changed;
	}

	public static boolean getChangesMade() {
		return changesMade;
	}
}