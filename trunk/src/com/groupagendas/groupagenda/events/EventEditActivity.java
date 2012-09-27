package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventsAdapter.ViewHolder;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.EventStatusUpdater;
import com.groupagendas.groupagenda.utils.InviteDialog;
import com.groupagendas.groupagenda.utils.SearchDialog;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class EventEditActivity extends EventActivity {
	
	private Button deleteButton;
	private int event_id;
	private TextView topText;

	private LinearLayout addressLine;
	private LinearLayout detailsLine;

	private View responsePanel;
	private LinearLayout invitesColumn;
	protected final static int DELETE_DIALOG = 1;
	private boolean remindersShown = false;

	private ArrayList<AutoIconItem> autoIcons = null;

	private Intent intent;

	private boolean addressPanelVisible = true;
	private boolean detailsPanelVisible = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_edit);

		dm = DataManagement.getInstance(this);
		dtUtils = new DateTimeUtils(this);
		responsePanel = findViewById(R.id.response_to_invitation);
		intent = getIntent();

		pb = (ProgressBar) findViewById(R.id.progress);
		saveButton = (Button) findViewById(R.id.save_button);
		deleteButton = (Button) findViewById(R.id.event_delete);
		topText = (TextView) findViewById(R.id.topText);

		String typeStr = "";
		if (intent.getStringExtra("type") != null) {
			typeStr = new StringBuilder(intent.getStringExtra("type")).append("_type").toString();
		}
		int typeId = getResources().getIdentifier(typeStr, "string", "com.groupagendas.groupagenda");

		if (topText != null && typeId != 0) {
			topText.setText(getString(typeId));
		}
	}

	@Override
	public void onResume() {
		int invitedListSize = 0;
		Button chatMessengerButton = (Button) findViewById(R.id.messenger_button);
		Button inviteButton = (Button) findViewById(R.id.invite_button);
		final Context mContext = dm.getContext();

		event_id = intent.getIntExtra("event_id", 0);

		if ((event_id > 0)) {
			event = dm.getEventFromLocalDb(event_id);
			chatMessengerButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (event_id > 0) {
						Intent intent = new Intent(EventEditActivity.this, ChatMessageActivity.class);
						intent.putExtra("event_id", event_id);
						startActivity(intent);
					}
				}
			});
		}
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
		if (event != null && event.reminder1 != null) {
			reminder1.setText(event.reminder1);
		} else {
			reminder1.setText("");
		}
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
		if (event != null && event.reminder2 != null) {
			reminder2.setText(event.reminder2);
		} else {
			reminder2.setText("");
		}
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
		if (event != null && event.reminder3 != null) {
			reminder3.setText(event.reminder3);
		} else {
			reminder3.setText("");
		}
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
		if (event.is_owner) {
			saveButton.setVisibility(View.VISIBLE);
			saveButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					saveEvent(v);
				}
			});
			
			deleteButton.setVisibility(View.VISIBLE);
			deleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showDialog(DELETE_DIALOG);
				}
			});
		} else {
			saveButton.setVisibility(View.GONE);
		}
		if (event.invited != null && !event.invited.isEmpty()) {
			invitedListSize = event.invited.size();
		}

		if (invitedListSize == 0) {
			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_standalone);
		} else if (invitesColumn == null) {
			invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
			invitesColumn.setVisibility(View.VISIBLE);
			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
			LinearLayout invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (int i = 0, l = event.invited.size(); i < l; i++) {
				final Invited invited = event.invited.get(i);

				final View view = inflater.inflate(R.layout.event_invited_person_entry, invitedPersonList, false);
				if (l == 1) {
					view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
				} else {
					if (i == l - 1)
						view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
					else
						view.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
				}
				boolean setEmailRed = false;
				if(!invited.inMyList && invited.guid > 0){
					invited.email = this.getResources().getString(R.string.add_to_cantact_list);
					setEmailRed = true;
					view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Dialog dia = new InviteDialog(EventEditActivity.this, R.style.yearview_eventlist, invited);
							dia.show();
						}
					});
				}

				invitedPersonList.addView(getInvitedView(invited, inflater, view, mContext, setEmailRed));
			}
			if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
				for (Contact contact : Data.selectedContacts) {
					final View view = inflater.inflate(R.layout.event_invited_person_entry, invitedPersonList, false);
					Invited invited = new Invited();
					invited.name = contact.name;
					invited.email = contact.email;
					invited.status_id = 4;
					invitedPersonList.addView(getInvitedView(invited, inflater, view, mContext, false));
				}
			}
		} else if (invitesColumn != null) {
			invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
			invitesColumn.setVisibility(View.VISIBLE);
			inviteButton.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
			LinearLayout invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
			invitedPersonList.removeAllViews();
			final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (int i = 0, l = event.invited.size(); i < l; i++) {
				final Invited invited = event.invited.get(i);

				final View view = inflater.inflate(R.layout.event_invited_person_entry, invitedPersonList, false);
				if (l == 1) {
					view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
				} else {
					if (i == l - 1 && Data.selectedContacts.isEmpty())
						view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
					else
						view.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
				}
				boolean setEmailRed = false;
				if(!invited.inMyList && invited.guid > 0){
					invited.email = this.getResources().getString(R.string.add_to_cantact_list);
					setEmailRed = true;
					view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Dialog dia = new InviteDialog(EventEditActivity.this, R.style.yearview_eventlist, invited);
							dia.show();
						}
					});
				}

				invitedPersonList.addView(getInvitedView(invited, inflater, view, mContext, setEmailRed));
			}
			if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
				for (int i = 0, l = Data.selectedContacts.size(); i < l; i++) {
					boolean needToShow = true;
					Contact contact = Data.selectedContacts.get(i);
					final View view = inflater.inflate(R.layout.event_invited_person_entry, invitedPersonList, false);
					if (l == 1) {
						view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
					} else {
						if (i == l - 1)
							view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
						else
							view.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
					}
					Invited invited = new Invited();
					invited.name = contact.name;
					for (Invited displayedInvited : event.invited) {
						if (displayedInvited != null && contact != null && displayedInvited.email != null
								&& !displayedInvited.email.equals("null") && displayedInvited.email.equals(contact.email)) {
							needToShow = false;
						}
					}
					invited.email = contact.email;
					invited.status_id = 4;
					if (needToShow) {
						invitedPersonList.addView(getInvitedView(invited, inflater, view, mContext, false));
					}
				}
			}
		}

		inviteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Data.newEventPar = true;
				Data.showSaveButtonInContactsForm = true;
				Data.eventForSavingNewInvitedPersons = event;
				startActivity(new Intent(EventEditActivity.this, ContactsActivity.class));
			}
		});

		final View holder = findViewById(R.id.response_to_invitation);
		if (holder != null) {
			final TextView myButton_status = (TextView) findViewById(R.id.status);
			final TextView myButton_yes = (TextView) findViewById(R.id.button_yes);
			final TextView myButton_maybe = (TextView) findViewById(R.id.button_maybe);
			final TextView myButton_no = (TextView) findViewById(R.id.button_no);

			myButton_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					boolean success = dm.changeEventStatus(event.event_id, "1");
					TextView myStatus = (TextView) findViewById(99999).findViewById(R.id.invited_status);

					myButton_yes.setVisibility(View.INVISIBLE);
					myButton_maybe.setVisibility(View.VISIBLE);
					myButton_no.setVisibility(View.VISIBLE);
					myButton_status.setText(mContext.getString(R.string.status_1));
					editDb(event.event_id, 1, success);
					event.status = 1;

					if (myStatus != null)
						myStatus.setText(R.string.status_1);
				}
			});

			myButton_maybe.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					boolean success = dm.changeEventStatus(event.event_id, "2");
					TextView myStatus = (TextView) findViewById(99999).findViewById(R.id.invited_status);

					myButton_yes.setVisibility(View.VISIBLE);
					myButton_maybe.setVisibility(View.INVISIBLE);
					myButton_no.setVisibility(View.VISIBLE);
					myButton_status.setText(mContext.getString(R.string.status_2));
					editDb(event.event_id, 2, success);
					event.status = 2;

					if (myStatus != null)
						myStatus.setText(R.string.status_2);
				}
			});

			myButton_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					boolean success = dm.changeEventStatus(event.event_id, "0");
					TextView myStatus = (TextView) findViewById(99999).findViewById(R.id.invited_status);

					myButton_yes.setVisibility(View.VISIBLE);
					myButton_maybe.setVisibility(View.VISIBLE);
					myButton_no.setVisibility(View.INVISIBLE);
					myButton_status.setText(mContext.getString(R.string.status_0));
					editDb(event.event_id, 0, success);
					event.status = 0;

					if (myStatus != null)
						myStatus.setText(R.string.status_0);
				}
			});
		}

		if (event_id > 0) {
			new GetEventTask().execute(event_id);
		}

		super.onResume();

		final LinearLayout addressPanel = (LinearLayout) findViewById(R.id.addressLine);
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
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Data.selectedContacts.clear();
		}
		finish();
		return true;
	}

	public View getInvitedView(Invited invited, LayoutInflater inflater, View view, Context mContext, boolean setEmailRed) {
		final TextView nameView = (TextView) view.findViewById(R.id.invited_fullname);
		nameView.setText(invited.name);

		final TextView emailView = (TextView) view.findViewById(R.id.invited_available_email);
		emailView.setText(invited.email);
		if(setEmailRed){
			emailView.setTextColor(Color.GREEN);
		}

		final TextView statusView = (TextView) view.findViewById(R.id.invited_status);

		switch (invited.status_id) {
		case 0:
			statusView.setText(mContext.getString(R.string.status_0));
			break;
		case 1:
			statusView.setText(mContext.getString(R.string.status_1));
			break;
		case 2:
			statusView.setText(mContext.getString(R.string.status_2));
			break;
		case 4:
			statusView.setText(mContext.getString(R.string.new_invite));
			break;
		}

		if (invited.me) {
			view.setTag("my_event_status");
			view.setId(99999);
		}

		return view;
	}

	class GetEventTask extends AsyncTask<Integer, Event, Event> {
		final DataManagement dm = DataManagement.getInstance(getParent());
		final Context mContext = dm.getContext();

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Event doInBackground(Integer... ids) {
//			autoColors = dm.getAutoColors(); TODO remove shit
			autoIcons = dm.getAutoIcons();

			if (intent.getBooleanExtra("isNative", false)) {
				return dm.getNativeCalendarEvent(ids[0]);
			} else {
				return dm.getEventFromLocalDb(ids[0]);
			}
		}

		@Override
		protected void onPostExecute(final Event result) {
			event = result;
			// icon
			final String[] iconsValues = getResources().getStringArray(R.array.icons_values);
			iconView = (ImageView) findViewById(R.id.iconView);
			if (result.icon != null && !result.icon.equals("null")) {
				int iconId = getResources().getIdentifier(result.icon, "drawable", "com.groupagendas.groupagenda");
				iconView.setImageResource(iconId);
			}

			if (result.is_owner) {
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
									event.icon = "";
									iconView.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
								} else {
									event.icon = iconsValues[position];
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
			}

			// color
			final String[] colorsValues = getResources().getStringArray(R.array.colors_values);
			colorView = (ImageView) findViewById(R.id.colorView);
			// if (result.color != null && !result.color.equals("null")) { TODO some more shit
			// String nameColor = "calendarbubble_" + result.color + "_";
			int image = result.getColorBubbleId(mContext);
			colorView.setImageResource(image);
			// }

			if (result.is_owner) {
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
								event.setColor(colorsValues[position]);
								// String nameColor = "calendarbubble_" + TODO and more shit
								// event.color + "_";
								int image = event.getColorBubbleId(getBaseContext());// getResources().getIdentifier(nameColor,
																						// "drawable",
																						// "com.groupagendas.groupagenda");
								colorView.setImageResource(image);
								dialog.dismiss();
							}
						});

						dialog.show();
					}
				});
			}

			// title
			titleView = (EditText) findViewById(R.id.title);
			titleView.setText(result.title);
			if (!result.is_owner) {
				titleView.setEnabled(false);
			} else {
				titleView.addTextChangedListener(filterTextWatcher);
			}

			// type TODO DEAD-CODE
			// typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
			// ArrayAdapter<CharSequence> adapterType =
			// ArrayAdapter.createFromResource(EventActivity.this,
			// R.array.type_labels,
			// android.R.layout.simple_spinner_item);
			// adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// typeSpinner.setAdapter(adapterType);
			// typeArray = getResources().getStringArray(R.array.type_values);
			//
			// if (result.type != null && !result.type.equals("null")) {
			// int pos = Utils.getArrayIndex(typeArray, result.type);
			// typeSpinner.setSelection(pos);
			// if (!result.is_owner)
			// typeSpinner.setEnabled(false);
			// }

			// Time
			startView = (EditText) findViewById(R.id.startView);
			startButton = (Button) findViewById(R.id.startButton);
			startButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showDateTimeDialog(startView, DIALOG_START);
				}
			});
			// start
			if (result.getStartCalendar() != null) {
				startView.setText(Utils.formatCalendar(result.getStartCalendar()));
				startCalendar = (Calendar) result.getStartCalendar().clone();
			}

			if (!result.is_owner) {
				startView.setEnabled(false);
				startButton.setEnabled(false);
			}
			// end
			endView = (EditText) findViewById(R.id.endView);
			endButton = (Button) findViewById(R.id.endButton);
			endButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showDateTimeDialog(endView, DIALOG_END);
				}
			});

			if (result.getEndCalendar() != null) {
				endView.setText(Utils.formatCalendar(result.getEndCalendar()));
				endCalendar = (Calendar) result.getEndCalendar().clone();
			}

			if (!result.is_owner) {
				endView.setEnabled(false);
				endButton.setEnabled(false);
			}

			// Description
			descView = (EditText) findViewById(R.id.descView);
			if (result.description_ != null && !result.description_.equals("null")) {
				LinearLayout parent = (LinearLayout) descView.getParent();
				parent.setVisibility(View.VISIBLE);
				descView.setText(result.description_);
			}

			// Address
			// TODO DEAD-CODE
			// addressLine = (LinearLayout) findViewById(R.id.addressLine);
			// addressLine.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// if(addressPanelVisible){
			// hideAddressPanel(ad);
			// } else {
			// showAddressPanel();
			// }
			// }
			// });
			// timezone
			timezoneSpinner = (Spinner) findViewById(R.id.timezoneSpinner);
			if (result.is_owner)
				showView(timezoneSpinner, addressLine);
			// country
			countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
			final ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(EventEditActivity.this, R.layout.search_dialog_item, CountryManager.getCountries(EventEditActivity.this));
			adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			countrySpinner.setAdapter(adapterCountry);
			countryArray = CountryManager.getCountryValues(EventEditActivity.this);
			
			LinearLayout countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock); 
			countrySpinnerBlock.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Dialog dia = new SearchDialog(EventEditActivity.this, R.style.yearview_eventlist_title, adapterCountry, countrySpinner);
					dia.show();				
				}
			});

			
			countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {

					if (pos == 0) {
						ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(EventEditActivity.this,  R.layout.search_dialog_item, new String[0]);
						adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						timezoneSpinner.setAdapter(adapterTimezone);
						timezoneSpinner.setEnabled(false);
						timezoneArray = null;
					} else {
						timezoneSpinner.setEnabled(true);
						// timezone
						String[] timezoneLabels = TimezoneManager.getTimezones(EventEditActivity.this, countryArray[pos]);
						ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(EventEditActivity.this,  R.layout.search_dialog_item, timezoneLabels);
						adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						timezoneSpinner.setAdapter(adapterTimezone);
						timezoneArray = TimezoneManager.getTimezonesValues(EventEditActivity.this, countryArray[pos]);

						if (result.timezone != null && !result.timezone.equals("null")) {
							pos = Utils.getArrayIndex(timezoneArray, result.timezone);
							timezoneSpinner.setSelection(pos);
							showView(timezoneSpinner, addressLine);
							if (!result.is_owner)
								timezoneSpinner.setEnabled(false);
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

			if (result.country != null && !result.country.equals("null")) {
				int pos = Utils.getArrayIndex(countryArray, result.country);
				countrySpinner.setSelection(pos);
				showView(countrySpinner, addressLine);
				if (!result.is_owner)
					countrySpinner.setEnabled(false);
			}
			if (result.is_owner)
				showView(countrySpinner, addressLine);

			// city
			cityView = (EditText) findViewById(R.id.cityView);
			if (result.city != null && !result.city.equals("null")) {
				cityView.setText(result.city);
				showView(cityView, addressLine);
				if (!result.is_owner)
					cityView.setEnabled(false);
			}
			if (result.is_owner)
				showView(cityView, addressLine);

			// street
			streetView = (EditText) findViewById(R.id.streetView);
			if (result.street != null && !result.street.equals("null")) {
				streetView.setText(result.street);
				showView(streetView, addressLine);
				if (!result.is_owner)
					streetView.setEnabled(false);
			}
			if (result.is_owner)
				showView(streetView, addressLine);

			// zip
			zipView = (EditText) findViewById(R.id.zipView);
			if (result.zip != null && !result.zip.equals("null")) {
				zipView.setText(result.zip);
				showView(zipView, addressLine);
				if (!result.is_owner)
					zipView.setEnabled(false);
			}
			if (result.is_owner)
				showView(zipView, addressLine);

			// // Details
			// detailsLine = (LinearLayout) findViewById(R.id.detailsLine);
			// detailsLine.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// if(detailsPanelVisible){
			// hideDetailsPanel();
			// } else {
			// showDetailsPanel();
			// }
			// }
			// });
			// location
			locationView = (EditText) findViewById(R.id.locationView);
			if (result.location != null && !result.location.equals("null")) {
				locationView.setText(result.location);
				showView(locationView, detailsLine);
				if (!result.is_owner)
					locationView.setEnabled(false);
			}
			if (result.is_owner)
				showView(locationView, detailsLine);
			// Go by
			gobyView = (EditText) findViewById(R.id.gobyView);
			if (result.go_by != null && !result.go_by.equals("null")) {
				gobyView.setText(result.go_by);
				showView(gobyView, detailsLine);
				if (!result.is_owner)
					gobyView.setEnabled(false);
			}
			if (result.is_owner)
				showView(gobyView, detailsLine);

			// Take with you
			takewithyouView = (EditText) findViewById(R.id.takewithyouView);
			if (result.take_with_you != null && !result.take_with_you.equals("null")) {
				takewithyouView.setText(result.take_with_you);
				showView(takewithyouView, detailsLine);
				if (!result.is_owner)
					takewithyouView.setEnabled(false);
			}
			if (result.is_owner)
				showView(takewithyouView, detailsLine);

			// Cost
			costView = (EditText) findViewById(R.id.costView);
			if (result.cost != null && !result.cost.equals("null")) {
				costView.setText(result.cost);
				showView(costView, detailsLine);
				if (!result.is_owner)
					costView.setEnabled(false);
			}
			if (result.is_owner)
				showView(costView, detailsLine);

			// Accomodation
			accomodationView = (EditText) findViewById(R.id.accomodationView);
			if (result.accomodation != null && !result.accomodation.equals("null")) {
				accomodationView.setText(result.accomodation);
				showView(accomodationView, detailsLine);
				if (!result.is_owner)
					accomodationView.setEnabled(false);
			}
			if (result.is_owner)
				showView(accomodationView, detailsLine);

			final ViewHolder holder = new ViewHolder();
			holder.status = (TextView) findViewById(R.id.status);
			holder.button_yes = (TextView) findViewById(R.id.button_yes);
			holder.button_maybe = (TextView) findViewById(R.id.button_maybe);
			holder.button_no = (TextView) findViewById(R.id.button_no);

			responsePanel.setVisibility(View.VISIBLE);

			switch (event.status) {
			case 0:
				holder.status.setText(mContext.getString(R.string.status_0));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.INVISIBLE);
				break;
			case 1:
				holder.status.setText(mContext.getString(R.string.status_1));
				holder.button_yes.setVisibility(View.INVISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			case 2:
				holder.status.setText(mContext.getString(R.string.status_2));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.INVISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			case 4:
				holder.status.setText(mContext.getString(R.string.new_invite));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			}

			pb.setVisibility(View.INVISIBLE);
			super.onPostExecute(result);
		}

	}

	private void editDb(int event_id, int status, boolean success) {
		Object[] array = { event_id, status, success, dm };
		new EventStatusUpdater().execute(array);
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s != null) {
				if (event.icon == null || event.icon.equals("null") || event.icon.equals("")) {
					for (int i = 0, l = autoIcons.size(); i < l; i++) {
						final AutoIconItem autoIcon = autoIcons.get(i);
						if (s.toString().contains(autoIcon.keyword)) {
							event.icon = autoIcon.icon;
							int iconId = getResources().getIdentifier(autoIcon.icon, "drawable", "com.groupagendas.groupagenda");
							iconView.setImageResource(iconId);
						}
					}
				}
				// if (event.color == null || event.color.equals("null") ||
				// event.color.equals("")) {
				// for (int i = 0, l = autoColors.size(); i < l; i++) {
				// final AutoColorItem autoColor = autoColors.get(i);
				// if (s.toString().contains(autoColor.keyword)) {
				// event.setColor(autoColor.color);
				// String nameColor = "calendarbubble_" + autoColor.color + "_";
				// int image = getResources().getIdentifier(nameColor,
				// "drawable", "com.groupagendas.groupagenda");
				// colorView.setImageResource(image);
				// }
				// }
				// }
			}
		}

	};

	public void saveEvent(View v) {
		new UpdateEventTask().execute(event);
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
			boolean success = false;
//			boolean check = true;
//			String temp = "";
			
			setEventData(event);
			int testEvent = event.isValid();
			if (testEvent == 0) {
				dm.updateEventInLocalDb (event);
//				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + event.event_id);
//				getContentResolver().update(uri, cv, null, null);
				success = dm.editEvent(event);
				try {
					dm.updateEventByIdFromRemoteDb(event.event_id); //TODO dublicate insert to localDB also problem when OFFLINE MODE
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (!success) {
					//TODO lines with status 4 should be uptdated
//					cv = new ContentValues();
//					cv.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 4);
//					getContentResolver().update(uri, cv, null, null);
				}
				return true;
			}else {
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

			if (event_id > 0) {
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + event_id);

				Boolean result = dm.removeEvent(event_id);

				if (result) {
					getContentResolver().delete(uri, null, null);
				} else if (DataManagement.getCONNECTION_ERROR().equals(dm.getError())) {
					result = true;
					ContentValues values = new ContentValues();
					values.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 3);

					getContentResolver().update(uri, values, null, null);
				}
				return result;
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Toast toast = Toast.makeText(EventEditActivity.this, "", Toast.LENGTH_LONG);
			if (result) {
				toast.setText(getString(R.string.event_deleted));
			} else {
				toast.setText(dm.getError());
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
								endCalendar.setTime(mDateTimePicker
										.getCalendar().getTime());
								endCalendar
										.add(Calendar.MINUTE,
												NewEventActivity.DEFAULT_EVENT_DURATION_IN_MINS);
								endView.setText(dtUtils
										.formatDateTime(endCalendar.getTime()));
							}
					timeSet = true;
					break;
				case DIALOG_END:
					endCalendar = mDateTimePicker.getCalendar();
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
		LinearLayout locationViewBlock = (LinearLayout) findViewById(R.id.locationBlock);
		locationViewBlock.setVisibility(View.VISIBLE);

		LinearLayout gobyViewBlock = (LinearLayout) findViewById(R.id.go_byBlock);
		gobyViewBlock.setVisibility(View.VISIBLE);

		LinearLayout takewithyouViewBlock = (LinearLayout) findViewById(R.id.take_with_youBlock);
		takewithyouViewBlock.setVisibility(View.VISIBLE);

		LinearLayout costViewBlock = (LinearLayout) findViewById(R.id.costBlock);
		costViewBlock.setVisibility(View.VISIBLE);

		LinearLayout accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationBlock);
		accomodationViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideDetailsPanel(LinearLayout addressPanel, LinearLayout detailsPanel) {
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
}
