package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.timezone.StringArrayListAdapter;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.SearchDialog;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class EventEditActivity extends EventActivity {

	private TextView topText;
	private Button deleteButton;

	private LinearLayout reminderBlock;
	private TextView setReminderTrigger;

	private LinearLayout reminder1container;
	private LinearLayout reminder2container;
	private LinearLayout reminder3container;
	private EditText reminder1;
	private EditText reminder2;
	private EditText reminder3;

	private LinearLayout alarmBlock;
	private TextView setAlarmTrigger;
	private LinearLayout alarm1container;
	private LinearLayout alarm2container;
	private LinearLayout alarm3container;
	private EditText alarm1;
	private EditText alarm2;
	private EditText alarm3;

	private long event_internal_id;

	private View responsePanel;
	
	protected final static int DELETE_DIALOG = 1;
	protected final static int MY_INVITED_ENTRY_ID = 99999;
	private boolean remindersShown = false;
	private boolean alarmsShown = false;

	// private ArrayList<AutoIconItem> autoIcons = null;

	private Intent intent;

	private Button chatMessengerButton;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_edit);
	}

	@Override
	public void onResume() {
		super.onResume();
		dtUtils = new DateTimeUtils(this);
		String[] array;
		countriesList = new ArrayList<String>();
		timezonesList = new ArrayList<String>();

		array = getResources().getStringArray(R.array.countries);
		for (String temp : array) {
			countriesList.add(temp);
		}
		if (countriesList != null)
			countriesAdapter = new StringArrayListAdapter(EventEditActivity.this, R.layout.search_dialog_item, countriesList);

		array = getResources().getStringArray(R.array.timezones);
		for (String temp : array) {
			timezonesList.add(temp);
		}
		if (timezonesList != null)
			timezonesAdapter = new StringArrayListAdapter(EventEditActivity.this, R.layout.search_dialog_item, timezonesList);

		initViewItems();
		hideAddressPanel();
		hideDetailsPanel();
		addressPanel.setVisibility(View.GONE);
		detailsPanel.setVisibility(View.GONE);
		addressDetailsPanel.setVisibility(View.VISIBLE);

		event_internal_id = intent.getLongExtra("event_id", 0); // TODO
																// implement
																// offline
		// mode event Edit
		if (event_internal_id > 0) {
			new GetEventTask().execute(event_internal_id);
		}
	}

	private void initViewItems() {

		pb = (ProgressBar) findViewById(R.id.progress);
		intent = getIntent();
		// Top text and SAVE Button
		topText = (TextView) findViewById(R.id.topText);

		String typeStr = "";
		if (intent.getStringExtra("type") != null) {
			typeStr = new StringBuilder(intent.getStringExtra("type")).append("_type").toString();
		}
		int typeId = getResources().getIdentifier(typeStr, "string", "com.groupagendas.groupagenda");

		if (topText != null && typeId != 0) {
			topText.setText(getString(typeId));
		}
		saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new UpdateEventTask().execute();
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
		countryView = (EditText) findViewById(R.id.countryView);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Dialog dia = new SearchDialog(EventEditActivity.this, android.R.drawable.dialog_frame, countriesAdapter, timezoneInUse);
				ListView diaList = (ListView) dia.findViewById(R.id.dialog_list);
				diaList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						if (arg1.getTag().toString() != null) {
							int position = Integer.parseInt(arg1.getTag().toString());
							if (position > 0) {
								timezoneInUse = position;
								countryView.setText(countriesList.get(timezoneInUse));
								timezoneView.setText(timezonesList.get(timezoneInUse));
							}
						}
					}
				});
				dia.show();
			}
		});

		// final ArrayAdapter<String> adapterCountry = new
		// ArrayAdapter<String>(EventEditActivity.this,
		// R.layout.search_dialog_item,
		// CountryManager.getCountries(EventEditActivity.this));
		// adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// countrySpinner.setAdapter(adapterCountry);
		// countryArray =
		// CountryManager.getCountryValues(EventEditActivity.this);
		// countrySpinnerBlock = (LinearLayout)
		// findViewById(R.id.countrySpinnerBlock);

		cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityView = (EditText) findViewById(R.id.cityView);
		streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetView = (EditText) findViewById(R.id.streetView);
		zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipView = (EditText) findViewById(R.id.zipView);
		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneView = (EditText) findViewById(R.id.timezoneView);
		timezoneSpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Dialog dia = new SearchDialog(EventEditActivity.this, android.R.drawable.dialog_frame, timezonesAdapter, timezoneInUse);
				ListView diaList = (ListView) dia.findViewById(R.id.dialog_list);
				diaList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						if (arg1.getTag().toString() != null) {
							int position = Integer.parseInt(arg1.getTag().toString());
							if (position > 0) {
								timezoneInUse = position;
								countryView.setText(countriesList.get(timezoneInUse));
								timezoneView.setText(timezonesList.get(timezoneInUse));
							}
						}
					}
				});
				dia.show();
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
		reminderBlock = (LinearLayout) findViewById(R.id.reminder_block);
		setReminderTrigger = (TextView) findViewById(R.id.setReminderTrigger);
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
		});

		// INVITES SECTION
		invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
		invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
		super.inviteButton = (Button) findViewById(R.id.invite_button);
		super.inviteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Data.newEventPar = true;
				Data.showSaveButtonInContactsForm = true;
				// TODO Data.eventForSavingNewInvitedPersons = event;
				startActivity(new Intent(EventEditActivity.this, ContactsActivity.class));
			}
		});

		responsePanel = findViewById(R.id.response_to_invitation);
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
	//
	// return view;
	// }

	class GetEventTask extends AsyncTask<Long, Event, Event> {
		final DataManagement dm = DataManagement.getInstance(getParent());
		final String[] iconsValues = getResources().getStringArray(R.array.icons_values);
		final SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected Event doInBackground(Long... ids) {
			// autoColors = dm.getAutoColors(); TODO implement or remove shit
			// autoIcons = dm.getAutoIcons();

			if (intent.getBooleanExtra("isNative", false)) {
				return dm.getNativeCalendarEvent(ids[0]);
			} else {
				return EventManagement.getEventFromLocalDb(EventEditActivity.this, ids[0]);
			}
		}

		@Override
		protected void onPostExecute(final Event result) {
			super.onPostExecute(result);
			if (result == null) {
				throw new IllegalStateException("EVENT NOT FOUND IN LOCAL DB!!!!!!");
			}

			event = result;
			// title

			titleView.setText(result.getTitle());
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
								if (iconsValues[position].equals("noicon")) {
									selectedIcon = Event.DEFAULT_ICON;
									iconView.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
								} else {
									selectedIcon = iconsValues[position];
									int iconId = getResources().getIdentifier(iconsValues[position], "drawable",
											"com.groupagendas.groupagenda");
									iconView.setImageResource(iconId);
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
								result.setColor(colorsValues[position]);
								int image = result.getColorBubbleId(getBaseContext());
								colorView.setImageResource(image);
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

			}
			colorView.setImageResource(result.getColorBubbleId(EventEditActivity.this));
			iconView.setImageResource(result.getIconId(EventEditActivity.this));
			

			// START AND END TIME
			if (result.getStartCalendar() != null) {
				startView.setText(Utils.formatCalendar(result.getStartCalendar()));
				startCalendar = (Calendar) result.getStartCalendar().clone();
			}
			if (result.getEndCalendar() != null) {
				endView.setText(Utils.formatCalendar(result.getEndCalendar()));
				endCalendar = (Calendar) result.getEndCalendar().clone();
			}

			if (result.getDescription().length() > 0) {
				LinearLayout parent = (LinearLayout) descView.getParent();
				parent.setVisibility(View.VISIBLE);
				descView.setText(result.getDescription());
			}

			if (result.getCity().length() > 0) {
				cityView.setText(result.getCity());
				showView(cityView, addressLine);
			}

			if (result.getStreet().length() > 0) {
				streetView.setText(result.getStreet());
				showView(streetView, addressLine);
			}
			if (result.getZip().length() > 0) {
				zipView.setText(result.getZip());
				showView(zipView, addressLine);
			}
			if (result.getLocation().length() > 0) {
				locationView.setText(result.getLocation());
				showView(locationView, detailsLine);
			}
			if (result.getGo_by().length() > 0) {
				gobyView.setText(result.getGo_by());
				showView(gobyView, detailsLine);
			}
			if (result.getTake_with_you().length() > 0) {
				takewithyouView.setText(result.getTake_with_you());
				showView(takewithyouView, detailsLine);
			}
			if (result.getCost().length() > 0) {
				costView.setText(result.getCost());
				showView(costView, detailsLine);
			}
			if (result.getAccomodation().length() > 0) {
				accomodationView.setText(result.getAccomodation());
				showView(accomodationView, detailsLine);
			}

			chatMessengerButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (event_internal_id > 0) {
						Intent intent = new Intent(EventEditActivity.this, ChatMessageActivity.class);
						intent.putExtra("event_id", event_internal_id);
						startActivity(intent);
					}
				}
			});
			Account account = new Account();
			if (result.getReminder1() != null) {
				reminder1.setText(Utils.formatCalendar(result.getReminder1(), account.getSetting_date_format()));
			} else {
				reminder1.setText("");
			}
			if (result.getReminder2() != null) {
				reminder2.setText(Utils.formatCalendar(result.getReminder2(), account.getSetting_date_format()));
			} else {
				reminder2.setText("");
			}
			if (result.getReminder3() != null) {
				reminder3.setText(Utils.formatCalendar(result.getReminder3(), account.getSetting_date_format()));
			} else {
				reminder3.setText("");
			}
			if (result.getAlarm1() != null) {
				alarm1.setText(Utils.formatCalendar(result.getAlarm1(), account.getSetting_date_format()));
			} else {
				alarm1.setText("");
			}
			if (result.getAlarm2() != null) {
				alarm2.setText(Utils.formatCalendar(result.getAlarm2(), account.getSetting_date_format()));
			} else {
				alarm2.setText("");
			}
			if (result.getAlarm3() != null) {
				alarm3.setText(Utils.formatCalendar(result.getAlarm3(), account.getSetting_date_format()));
			} else {
				alarm3.setText("");
			}

		showInvitesView();

			pb.setVisibility(View.INVISIBLE);
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

			event = setEventData(event);
			int testEvent = event.isValid();
			if (testEvent == 0) {
				EventManagement.updateEvent(EventEditActivity.this, event);
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
		case EventActivity.DIALOG_START:
			c = startCalendar;
			break;
		case EventActivity.DIALOG_END:
			c = endCalendar;
			break;
		}
		mDateTimePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateTimePicker.updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

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
						endCalendar.add(Calendar.MINUTE, NewEventActivity.DEFAULT_EVENT_DURATION_IN_MINS);
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
				if (timeSet) {
					Account account = new Account();
					view.setText(Utils.formatCalendar(mDateTimePicker.getCalendar(), account.getSetting_date_format()));
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

}