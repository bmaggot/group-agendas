package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.templates.TemplatesAdapter;
import com.groupagendas.groupagenda.templates.TemplatesDialog;
import com.groupagendas.groupagenda.templates.TemplatesDialogData;
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.SearchDialog;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class NewEventActivity extends EventActivity {
	private final int CHOOSE_CONTACTS_DIALOG = 1;

	@SuppressWarnings("unused")
	private Button templatesButton;

	private Button contactsButton;
	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	@SuppressWarnings("unused")
	// private ArrayList<AutoColorItem> autoColors = null;
	// private ArrayList<AutoIconItem> autoIcons = null;
	boolean addressPanelVisible = false;
	boolean detailsPanelVisible = false;
	int templateInUse = 0;

	private boolean remindersShown = false;
	private boolean alarmsShown = false;
	private boolean countrySelected = false;
	protected Event event;
	private CheckBox templateTrigger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Account account = new Account();

		// these fields indicate that we will save this user as owner of event
		// and save his uid
		setUid = true;

		setContentView(R.layout.new_event);
		event = new Event();

		startCalendar.clear(Calendar.SECOND);
		endCalendar.clear(Calendar.SECOND);
		endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);

		dm = DataManagement.getInstance(this);
		dtUtils = new DateTimeUtils(this);

		// new GetAutoTask().execute();
		new GetContactsTask().execute(); // TODO investigate

		cv = new ContentValues();
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
						String nameColor = "calendarbubble_" + selectedColor + "_";
						int image = getResources().getIdentifier(nameColor, "drawable", "com.groupagendas.groupagenda");
						colorView.setImageResource(image);

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

		// type
		// typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
		// ArrayAdapter<CharSequence> adapterType =
		// ArrayAdapter.createFromResource(NewEventActivity.this,
		// R.array.type_labels,
		// android.R.layout.simple_spinner_item);
		// adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// typeSpinner.setAdapter(adapterType);
		// typeArray = getResources().getStringArray(R.array.type_values);
		// typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		// @Override
		// public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
		// long arg3) {
		// if (pos > 1) {
		// contactsBlock.setVisibility(View.VISIBLE);
		// } else {
		// contactsBlock.setVisibility(View.GONE);
		// }
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> arg0) {
		// }
		// });

		// Time
		String strTime = getIntent().getStringExtra(EXTRA_STRING_FOR_START_CALENDAR);
		if (strTime != null) {
			startCalendar = Utils.stringToCalendar(strTime, DataManagement.SERVER_TIMESTAMP_FORMAT);
			startCalendar.clear(Calendar.SECOND);
			endCalendar.setTime(startCalendar.getTime());
			endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);

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

		// Description
		descView = (EditText) findViewById(R.id.descView);

		// Address
		final LinearLayout addressPanel = (LinearLayout) findViewById(R.id.addressLine);
		// timezone
		timezoneSpinner = (Spinner) findViewById(R.id.timezoneSpinner);

		// country
		countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
		final ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(this, R.layout.search_dialog_item,
				CountryManager.getCountries(this));
		adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapterCountry);
		countryArray = CountryManager.getCountryValues(this);

		// String curentCountry =
		// dm.getmContext().getResources().getConfiguration().locale.getISO3Country();
		Locale newLocale = new Locale(account.getLanguage(), account.getCountry());
		String curentCountry = newLocale.getISO3Country();
		int i = 0;
		for (String tmpCountry : countryArray) {
			if (tmpCountry.equals(curentCountry)) {
				countrySpinner.setSelection(i);
				countrySelected = true;
				timezoneSpinner.setEnabled(true);
				String[] timezoneLabels = TimezoneManager.getTimezones(NewEventActivity.this, countryArray[i]);
				ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(NewEventActivity.this, R.layout.search_dialog_item,
						timezoneLabels);
				adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				timezoneSpinner.setAdapter(adapterTimezone);

				timezoneArray = TimezoneManager.getTimezonesValues(NewEventActivity.this, countryArray[i]);
			}
			i++;
		}

		LinearLayout countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Dialog dia = new SearchDialog(NewEventActivity.this, R.style.yearview_eventlist_title, adapterCountry, countrySpinner);
				dia.show();
			}
		});

		countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (pos == 0) {
					if (!countrySelected) {
						ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(NewEventActivity.this, R.layout.search_dialog_item,
								new String[0]);
						adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						timezoneSpinner.setAdapter(adapterTimezone);
						timezoneSpinner.setEnabled(false);
						timezoneArray = null;
					}
				} else {
					timezoneSpinner.setEnabled(true);
					String[] timezoneLabels = TimezoneManager.getTimezones(NewEventActivity.this, countryArray[pos]);
					ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(NewEventActivity.this, R.layout.search_dialog_item,
							timezoneLabels);
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);

					timezoneArray = TimezoneManager.getTimezonesValues(NewEventActivity.this, countryArray[pos]);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
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

		// contacts
		Data.selectedContacts = new ArrayList<Contact>();
		contactsButton = (Button) findViewById(R.id.contactsButton);
		contactsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Data.newEventPar = true;
				Data.showSaveButtonInContactsForm = true;
				startActivity(new Intent(NewEventActivity.this, ContactsActivity.class));
			}
		});

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
	}

	@Override
	public void onResume() {
		// if (Data.selectedContacts != null &&
		// !Data.selectedContacts.isEmpty()) {
		// LinearLayout invitedPersonList = (LinearLayout)
		// findViewById(R.id.invited_person_list);
		// invitedPersonList.removeAllViews();
		// final LayoutInflater inflater = (LayoutInflater)
		// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//
		// contactsButton.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
		//
		// for (int i = 0, l = Data.selectedContacts.size(); i < l; i++) {
		// Contact contact = Data.selectedContacts.get(i);
		// final View view =
		// inflater.inflate(R.layout.event_invited_person_entry,
		// invitedPersonList, false);
		// if (l == 1) {
		// view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
		// } else {
		// if (i == l - 1)
		// view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
		// else
		// view.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
		// }
		// Invited invited = new Invited();
		// invited.name = contact.name;
		// invited.email = contact.email;
		// invited.status_id = 4;
		// invitedPersonList.addView(getInvitedView(invited, inflater, view,
		// dm.getContext()));
		// }
		// } else {
		contactsButton.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
		LinearLayout invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
		invitedPersonList.removeAllViews();
		// }

		super.onResume();
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
		if (!templateTrigger.isChecked()) {
			Toast.makeText(this, R.string.saving_new_event, Toast.LENGTH_LONG).show();
			try {
				new NewEventTask().execute().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, R.string.saving_new_template, Toast.LENGTH_LONG).show();
			try {
				new NewTemplateTask().execute().get();
				Toast.makeText(this, R.string.new_event_saved, Toast.LENGTH_LONG).show();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
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
					event = dm.getTemplateFromLocalDb(Data.templateInUse);
					setUIValues(event);
					Data.templateInUse = 0;
				}
			}
		});
		templateListDialog.show();
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
			setEventData(event);
			event.setCreatedMillisUtc(Calendar.getInstance().getTimeInMillis()); // set
																					// create
																					// time

			int testEvent = event.isValid();
			if (testEvent == 0) {
				EventManagement.createNewEvent(NewEventActivity.this, event);
				Data.selectedContacts.clear(); // TODO should it be handled this
												// way?
				return true;
			} else {
				errorStr = setErrorStr(testEvent);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
				Toast.makeText(NewEventActivity.this, R.string.new_event_saved, Toast.LENGTH_LONG).show();
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

	// @Override
	// protected Dialog onCreateDialog(int id) {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// switch (id) {
	// case DIALOG_ERROR:
	// builder.setMessage(errorStr).setCancelable(false)
	// .setPositiveButton(getString(R.string.ok), new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.dismiss();
	// }
	// });
	// break;
	// case CHOOSE_CONTACTS_DIALOG:
	// builder.setTitle(getString(R.string.choose_contacts))
	// .setMultiChoiceItems(titles, selections, new
	// DialogSelectionClickHandler())
	// .setPositiveButton(getString(R.string.ok), new
	// DialogButtonClickHandler());
	// break;
	// }
	// return builder.create();
	// }

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
		final boolean is24h = !CalendarSettings.isUsing_AM_PM();
		// Setup TimePicker
		mDateTimePicker.setIs24HourView(is24h);

		// Update demo TextViews when the "OK" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDateTimePicker.clearFocus();
				boolean timeSet = false;
				switch (id) {
				case DIALOG_START:
					startCalendar = mDateTimePicker.getCalendar();
					startView.setText(dtUtils.formatDateTime(startCalendar.getTime()));
					if (!startCalendar.before(endCalendar)) {
						endCalendar = Calendar.getInstance();
						endCalendar.setTime(mDateTimePicker.getCalendar().getTime());
						endCalendar.add(Calendar.MINUTE, DEFAULT_EVENT_DURATION_IN_MINS);
						endView.setText(dtUtils.formatDateTime(endCalendar.getTime()));
					}
					timeSet = true;
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
				case EventActivity.REMINDER1:
					reminder1time = mDateTimePicker.getCalendar();
					break;
				case EventActivity.REMINDER2:
					reminder2time = mDateTimePicker.getCalendar();
					break;
				case EventActivity.REMINDER3:
					reminder3time = mDateTimePicker.getCalendar();
					break;
				}
				if (!timeSet) {
					view.setText(dtUtils.formatDateTime(mDateTimePicker.getCalendar().getTime()));
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
				dm.createTemplate(event);
				Data.selectedContacts.clear();
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
	 * timezone and country, start�end time, reminder and alarm field values
	 * aren't updated.
	 * 
	 * @author meska.lt@gmail.com
	 * @param data
	 *            � Event object with values set.
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
		String timeFormat;
		Account account = new Account();
		if (account.getSetting_ampm() == 1) {
			timeFormat = getResources().getString(R.string.hour_event_view_time_format_AMPM);
		} else {
			timeFormat = getResources().getString(R.string.hour_event_view_time_format);
		}
		view = (EditText) findViewById(R.id.startView);
		if (view != null)
			view.setText(Utils.formatCalendar(data.getStartCalendar()) + " " + Utils.formatCalendar(data.getStartCalendar(), timeFormat));

		view = (EditText) findViewById(R.id.endView);
		if (view != null)
			view.setText(Utils.formatCalendar(data.getEndCalendar()) + " " + Utils.formatCalendar(data.getEndCalendar(), timeFormat));

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
		view = (EditText) findViewById(R.id.reminder1);
		if (view != null)
			view.setText(data.getReminder1().getTime().toString());

		view = (EditText) findViewById(R.id.reminder2);
		if (view != null)
			view.setText(data.getReminder2().getTime().toString());

		view = (EditText) findViewById(R.id.reminder3);
		if (view != null)
			view.setText(data.getReminder3().getTime().toString());

		/* Set event's alarms */
		view = (EditText) findViewById(R.id.alarm1);
		if (view != null)
			view.setText(data.getAlarm1().getTime().toString());

		view = (EditText) findViewById(R.id.alarm2);
		if (view != null)
			view.setText(data.getAlarm2().getTime().toString());

		view = (EditText) findViewById(R.id.alarm3);
		if (view != null)
			view.setText(data.getAlarm3().getTime().toString());

	}
}
