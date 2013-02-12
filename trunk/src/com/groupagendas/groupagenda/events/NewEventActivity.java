package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.address.Address;
import com.groupagendas.groupagenda.address.AddressBookActivity;
import com.groupagendas.groupagenda.address.AddressBookInfoActivity;
import com.groupagendas.groupagenda.address.AddressManagement;
import com.groupagendas.groupagenda.alarm.AlarmsManagement;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.metadata.impl.AddressMetaData;
import com.groupagendas.groupagenda.templates.Template;
import com.groupagendas.groupagenda.templates.TemplatesActivity;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeSelectActivity;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.TimezoneUtils;
import com.groupagendas.groupagenda.utils.TimezoneUtils.StaticTimezone;
import com.groupagendas.groupagenda.utils.Utils;

public class NewEventActivity extends EventActivity implements AddressMetaData {
	private static boolean timezoneFound = false;
	
	@SuppressWarnings("unused")
	private Button templatesButton;

	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	int templateInUse = 0;

	private CheckBox templateTrigger;
	boolean changesMade;
	private TextWatcher watcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_event);
		account = new Account(this);
		selectedContacts = null;
		selectedGroups = null;
		setEditInvited(false);

		startCalendar.clear(Calendar.SECOND);
		endCalendar.clear(Calendar.SECOND);
		endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);
		dtUtils = new DateTimeUtils(this);

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
		colorView.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundSquare(this, COLOURED_BUBBLE_SIZE, 5, selectedColor, false)));

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
						colorView.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundSquare(NewEventActivity.this, COLOURED_BUBBLE_SIZE, 5, selectedColor, false)));

						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});

		// title
		titleView = (EditText) findViewById(R.id.title);
		titleView.setEnabled(true);

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
		startView = (TextView) findViewById(R.id.startView);
		startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
		startView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(NewEventActivity.this, DateTimeSelectActivity.class);
				i.putExtra(DateTimeSelectActivity.ACTIVITY_TARGET_KEY, DateTimeSelectActivity.TARGET_NEW_EVENT);
				startActivity(i);
			}
		});
		
		// end
		endView = (TextView) findViewById(R.id.endView);
		endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
		endView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(NewEventActivity.this, DateTimeSelectActivity.class);
				i.putExtra(DateTimeSelectActivity.ACTIVITY_TARGET_KEY, DateTimeSelectActivity.TARGET_NEW_EVENT);
				startActivity(i);
			}
		});

		// Description
		descView = (EditText) findViewById(R.id.descView);

		final List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(this);

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

						timezonesAdapter = new TimezonesAdapter(NewEventActivity.this, R.layout.search_dialog_item,
								TimezoneUtils.getTimezonesByCc(NewEventActivity.this, event.getCountry()));
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
		streetView = (EditText) findViewById(R.id.streetView);
		zipView = (EditText) findViewById(R.id.zipView);

		// location
		locationView = (EditText) findViewById(R.id.locationView);
		gobyView = (EditText) findViewById(R.id.gobyView);
		takewithyouView = (EditText) findViewById(R.id.takewithyouView);
		costView = (EditText) findViewById(R.id.costView);
		accomodationView = (EditText) findViewById(R.id.accomodationView);
		
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
		
		address = (Button) findViewById(R.id.address_button);
		address.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent address = new Intent(NewEventActivity.this, AddressBookActivity.class);
				address.putExtra("action", true);
				startActivity(address);
			}
		});
		
		save_address = (Button) findViewById(R.id.save_address);
		save_address.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent addressCreate = new Intent(NewEventActivity.this, AddressBookInfoActivity.class);
				addressCreate.putExtra("action", false);
				addressCreate.putExtra("fill_info", true);
				startActivity(addressCreate);
			}
		});

		hideAddressPanel(addressPanel, detailsPanel);
		hideDetailsPanel(addressPanel, detailsPanel);
		addressDetailsPanel = (LinearLayout) findViewById(R.id.addressDetailsLine);
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
		
		// INVITES SECTION
		invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
		invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
//		invitedPersonListView = (ListView) findViewById(R.id.invited_person_listview);
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
		
		super.inviteEditButton = (Button) findViewById(R.id.invite_edit_button);
		super.inviteEditButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int childCount = invitedPersonList.getChildCount();
				setEditInvited(!getEditInvited());
				for (int i = 0; i < childCount; i++) {

					if (getEditInvited()) {
						invitedPersonList.getChildAt(i)
								.findViewById(R.id.invited_remove)
								.setVisibility(View.VISIBLE);
					} else {
						invitedPersonList.getChildAt(i)
								.findViewById(R.id.invited_remove)
								.setVisibility(View.GONE);
					}
				}
			}
		});
		
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

		EventActivity.event = new Event();
		hideAlarmPanel();
//		hideReminderPanel();
		initAlarms();
//		initReminders();
	}

	@Override
	public void onResume() {
		super.onResume();
		account = new Account(this);
		invitationResponseLine = (RelativeLayout) findViewById(R.id.response_to_invitation);
		invitationResponseStatus = (TextView) findViewById(R.id.status);

		if (watcher == null) {
			watcher = new GenericTextWatcher();
		}

		countryView.addTextChangedListener(watcher);
		cityView.addTextChangedListener(watcher);
		streetView.addTextChangedListener(watcher);
		zipView.addTextChangedListener(watcher);
		timezoneView.addTextChangedListener(watcher);
		startView = (TextView) findViewById(R.id.startView);
		endView = (TextView) findViewById(R.id.endView);

		List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(this);
		String countryCode = "";
		if (!timezoneFound) {
			String tmz = account.getTimezone();
			for (StaticTimezone item : countriesList) {
				if (item.timezone.equalsIgnoreCase(tmz)) {
					timezoneInUse = Integer.parseInt(item.id);
					StaticTimezone st = /*countriesList.get(timezoneInUse)*/item;
					countryView.setText(st.country2);
					countryCode = st.country_code;
					// TODO: continue search? why?
					timezoneFound = true;
					continue;
				}
			}
		}
		timezonesAdapter = new TimezonesAdapter(NewEventActivity.this, R.layout.search_dialog_item,
				TimezoneUtils.getTimezonesByCc(this, countryCode));
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
			Collection<String> c = group.contacts.values();
			selectedContactsFromGroups.ensureCapacity(selectedContactsFromGroups.size() + c.size());
			for (String id : c) {
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
			inviteEditButton.setVisibility(View.VISIBLE);
			respondToInvitation(event.getStatus());
		} else {
			invitationResponseLine.setVisibility(View.GONE);
		}
		
		if(AddressBookActivity.selectedAddressId > 0){
			String where = AddressTable._ID + " = " + AddressBookActivity.selectedAddressId;
			Address address = AddressManagement.getAddressFromLocalDb(NewEventActivity.this, where);
			cityView.setText(address.getCity());
			streetView.setText(address.getStreet());
			zipView.setText(address.getZip());
			countryView.setText(address.getCountry_name());
			timezoneView.setText(address.getTimezone());
			AddressBookActivity.selectedAddressId = 0;
		}
		
		startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
		endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
		countryView.setText(countriesList.get(timezoneInUse).country2);
		timezoneView.setText(countriesList.get(timezoneInUse).altname);
		event.setCountry(countriesList.get(timezoneInUse).country_code);
		event.setTimezone(countriesList.get(timezoneInUse).timezone);
	}

	public void saveEvent(View v) {
		if (!saveButton.getText().toString().equalsIgnoreCase(getResources().getString(R.string.saving))) {
			if (!templateTrigger.isChecked()) {
				new NewEventTask().execute();
			} else {
				Toast.makeText(this, R.string.saving_new_template, Toast.LENGTH_SHORT).show();
				try {
					new NewTemplateTask().execute().get();
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
	 * Call TemplatesActivity instance.
	 * 
	 * Retrieves list of available templates titles from SQLite database and
	 * displays it in a dialogue.
	 * 
	 * @author meska.lt@gmail.com
	 * @param v
	 * 
	 */
	public void chooseTemplate(View v) {
		Intent i = new Intent(NewEventActivity.this, TemplatesActivity.class);
		startActivity(i);
	}

	@Override
	protected Event setEventData(Event event) {
		event = super.setEventData(event);
		event.setCreatedMillisUtc(Calendar.getInstance().getTimeInMillis()); // set create time
		event.setModifiedMillisUtc(event.getCreatedUtc());
		event.setCreator_fullname(getString(R.string.you));
		event.setIs_owner(true);
		event.setStatus(Invited.ACCEPTED);
		event.setUser_id(account.getUser_id());
		return event;
	}

	@Override
	public void showAddressPanel() {
		addressPanelVisible = true;

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countrySpinnerBlock.setVisibility(View.VISIBLE);

		cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityViewBlock.setVisibility(View.VISIBLE);

		streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetViewBlock.setVisibility(View.VISIBLE);

		zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipViewBlock.setVisibility(View.VISIBLE);
		address.setVisibility(View.VISIBLE);
		if(cityView.getText().length() > 0 || streetView.getText().length() > 0 || zipView.getText().length() > 0){
			save_address.setVisibility(View.VISIBLE);
		}
	}

	public void hideAddressPanel(LinearLayout addressPanel, LinearLayout detailsPanel) {
		addressPanelVisible = false;

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countrySpinnerBlock.setVisibility(View.GONE);

		cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityViewBlock.setVisibility(View.GONE);

		streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetViewBlock.setVisibility(View.GONE);

		zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipViewBlock.setVisibility(View.GONE);
		address.setVisibility(View.GONE);
		save_address.setVisibility(View.GONE);
	}

	@Override
	public void showDetailsPanel() {
		detailsPanelVisible = true;
		
		locationViewBlock = (LinearLayout) findViewById(R.id.locationViewBlock);
		locationViewBlock.setVisibility(View.VISIBLE);

		gobyViewBlock = (LinearLayout) findViewById(R.id.gobyViewBlock);
		gobyViewBlock.setVisibility(View.VISIBLE);

		takewithyouViewBlock = (LinearLayout) findViewById(R.id.takewithyouViewBlock);
		takewithyouViewBlock.setVisibility(View.VISIBLE);

		costViewBlock = (LinearLayout) findViewById(R.id.costViewBlock);
		costViewBlock.setVisibility(View.VISIBLE);

		accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationViewBlock);
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
				if(NavbarActivity.alarmReceiver == null){
					NavbarActivity.refreshAlarmReceiver();
				}
				if (alarm1time != null || alarm2time != null || alarm3time != null) {
					long alarm1 = 0;
					long alarm2 = 0;
					long alarm3 = 0;
					if(alarm1time != null){
						alarm1time.clear(Calendar.SECOND);
						alarm1time.clear(Calendar.MILLISECOND);
						alarm1 = alarm1time.getTimeInMillis();
					}
					if (alarm2time != null) {
						alarm2time.clear(Calendar.SECOND);
						alarm2time.clear(Calendar.MILLISECOND);
						alarm2 = alarm2time.getTimeInMillis();
					}
					if (alarm3time != null) {
						alarm3time.clear(Calendar.SECOND);
						alarm3time.clear(Calendar.MILLISECOND);
						alarm3 = alarm3time.getTimeInMillis();
					}
					AlarmsManagement.setAlarmsForEvent(getApplicationContext(), alarm1, 0, alarm2, 0, alarm3, 0, event.getEvent_id());
				}
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
		}
		return builder.create();
	}

	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		@Override
		public void onClick(DialogInterface dialog, int clicked, boolean selected) {
			selections[clicked] = selected;
		}
	}

	class GetAutoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
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
			NewEventActivity.super.setEventData(event);
			event.setStatus(Invited.ACCEPTED);

			int testEvent = event.isValid();

			if (testEvent == 0) {
				Template template = new Template();
				
				template.setTitle(event.getActualTitle());
				template.setTemplate_title(event.getActualTitle());
				template.setIcon(event.getIcon());
				template.setColor(event.getColor());
				template.setDescription_(event.getDescription());
				
				template.setStartCalendar(event.getStartCalendar());
				template.setEndCalendar(event.getEndCalendar());
				template.setIs_all_day(event.is_all_day());
				template.setTimezone(event.getTimezone());
				template.setTimezoneInUse(timezoneInUse);

				template.setCountry(event.getCountry());
				template.setCity(event.getCity());
				template.setStreet(event.getStreet());
				template.setZip(event.getZip());
				
				template.setLocation(event.getLocation());
				template.setAccomodation(event.getAccomodation());
				template.setGo_by(event.getGo_by());
				template.setTake_with_you(event.getTake_with_you());
				template.setCost(event.getCost());
				
				template.setCreated_millis_utc(Calendar.getInstance().getTimeInMillis());
				DataManagement.createTemplate(NewEventActivity.this, template);
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
				return false;
			}
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

		view = (EditText) findViewById(R.id.cityView);
		if (view != null)
			view.setText(data.getCity());

		view = (EditText) findViewById(R.id.streetView);
		if (view != null)
			view.setText(data.getStreet());

		view = (EditText) findViewById(R.id.zipView);
		if (view != null)
			view.setText(data.getZip());

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

	}
	
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
				if(cityView.getText().length() > 0 || streetView.getText().length() > 0 || zipView.getText().length() > 0){
					save_address.setVisibility(View.VISIBLE);
				}
			}
		}
	}
}
