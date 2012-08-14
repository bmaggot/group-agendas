package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.w3c.dom.UserDataHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class NewEventActivity extends Activity {
	public static final String EXTRA_STRING_FOR_START_CALENDAR = "strTime";
	public static final int DEFAULT_EVENT_DURATION_IN_MINS = 30;
	
	
	private DataManagement dm;
	private DateTimeUtils dtUtils;

	private ProgressBar pb;
	private Button saveButton;

	private ImageView iconView;
	private ImageView colorView;

	private EditText titleView;

	private Spinner typeSpinner;
	private String[] typeArray;

	private final int DIALOG_START = 0;
	private Calendar startCalendar = Calendar.getInstance();
	private EditText startView;
	private Button startButton;

	private final int DIALOG_END = 1;
	private Calendar endCalendar = Calendar.getInstance();
	private EditText endView;
	private Button endButton;

	private EditText descView;

	private Spinner countrySpinner;
	private String[] countryArray;
	private EditText cityView;
	private EditText streetView;
	private EditText zipView;
	private Spinner timezoneSpinner;
	private String[] timezoneArray;

	private EditText locationView;
	private EditText gobyView;
	private EditText takewithyouView;
	private EditText costView;
	private EditText accomodationView;

	private LinearLayout contactsBlock;
	private Button contactsButton;
	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	private Event event;

	private String errorStr = "";
	private final int DIALOG_ERROR = 0;
	private final int CHOOSE_CONTACTS_DIALOG = 1;

	private Prefs prefs;

	ContentValues cv;
	
	private ArrayList<AutoColorItem> autoColors = null;
	private ArrayList<AutoIconItem> autoIcons = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_event);

		dm = DataManagement.getInstance(this);
		dtUtils = new DateTimeUtils(this);
		
		new GetAutoTask().execute();
		new GetContactsTask().execute();

		cv = new ContentValues();
		prefs = new Prefs(this);
		
		event = new Event();

		pb = (ProgressBar) findViewById(R.id.progress);
		saveButton = (Button) findViewById(R.id.saveButton);

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
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						if(iconsValues[position].equals("noicon")){
							iconView.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
						}else{
							event.icon = iconsValues[position];
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
						event.color = colorsValues[position];
						cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.color);

						String nameColor = "calendarbubble_" + event.color + "_";
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
		titleView.setEnabled(false);
		titleView.addTextChangedListener(filterTextWatcher);

		// type
		typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
		ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(NewEventActivity.this, R.array.type_labels,
				android.R.layout.simple_spinner_item);
		adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(adapterType);
		typeArray = getResources().getStringArray(R.array.type_values);
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (pos > 1) {
					contactsBlock.setVisibility(View.VISIBLE);
				} else {
					contactsBlock.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// Time
		String strTime = getIntent().getStringExtra(EXTRA_STRING_FOR_START_CALENDAR);
		if(strTime != null){
			startCalendar = Utils.stringToCalendar(strTime, Utils.date_format);
//			startCalendar = dtUtils.stringDateToCalendar(strTime);
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
		// timezone
		timezoneSpinner = (Spinner) findViewById(R.id.timezoneSpinner);
		
		// country
		countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
		ArrayAdapter<String> adapterCountry =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CountryManager.getCountries(this)) ;
		adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapterCountry);
		countryArray = CountryManager.getCountryValues(this);
		countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if(pos == 0){
					ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(NewEventActivity.this, android.R.layout.simple_spinner_item, new String[0]) ;
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);
					timezoneSpinner.setEnabled(false);
					timezoneArray = null;
				}else{
					timezoneSpinner.setEnabled(true);
					String[] timezoneLabels = TimezoneManager.getTimezones(NewEventActivity.this, countryArray[pos]);
					ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(NewEventActivity.this, android.R.layout.simple_spinner_item, timezoneLabels) ;
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);
					
					timezoneArray = TimezoneManager.getTimezonesValues(NewEventActivity.this, countryArray[pos]);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		// city
		cityView = (EditText) findViewById(R.id.cityView);
		// street
		streetView = (EditText) findViewById(R.id.streetView);
		// zip
		zipView = (EditText) findViewById(R.id.zipView);		
		
		// Details
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
		contactsBlock = (LinearLayout) findViewById(R.id.contactsBlock);
		contactsButton = (Button) findViewById(R.id.contactsButton);
		contactsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Data.newEventPar = true;
				
				
				startActivity(new Intent(NewEventActivity.this, ContactsActivity.class));
				
				//showDialog(CHOOSE_CONTACTS_DIALOG);
				
				//if (Data.selectedContacts.size() > );
			}
		});
	}
	
	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s !=null){
				if(event.icon ==null || event.icon.equals("null")){
					for(int i=0, l=autoIcons.size(); i<l; i++){
						final AutoIconItem autoIcon = autoIcons.get(i); 
						if(s.toString().contains(autoIcon.keyword)){
							event.icon = autoIcon.icon;
							int iconId = getResources().getIdentifier(autoIcon.icon, "drawable", "com.groupagendas.groupagenda");
							iconView.setImageResource(iconId);
						}
					}
				}
				
				if(event.color ==null || event.color.equals("null") || event.color.equals("")){
					for(int i=0, l=autoColors.size(); i<l; i++){
						final AutoColorItem autoColor = autoColors.get(i); 
						if(s.toString().contains(autoColor.keyword)){
							event.color = autoColor.color;
							String nameColor = "calendarbubble_"+autoColor.color+"_";
							int image = getResources().getIdentifier(nameColor, "drawable", "com.groupagendas.groupagenda");
							colorView.setImageResource(image);
						}
					}
				}
			}
		}

	};
	
	
	public void saveEvent(View v) {
		new NewEventTask().execute();
	}

	class NewEventTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			boolean success = false;
			boolean check = true;
			String temp = "";
			
			// timezone
			if(timezoneArray != null){
				temp = timezoneArray[timezoneSpinner.getSelectedItemPosition()];
				event.timezone = temp;
				cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE, temp);
			}else{
				check = false;
				errorStr = getString(R.string.timezone_required);
			}
			
			// description
			temp = descView.getText().toString();
			if (temp.length() <= 0) {
				check = false;
				errorStr = getString(R.string.desc_is_required);
			}
			event.description_ = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, temp);

			// title
			temp = titleView.getText().toString();
			if (temp.length() <= 0) {
				check = false;
				errorStr = getString(R.string.title_is_required);
			}
			event.title = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, temp);
			
			cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.icon);
			
			cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.color);
			
			temp = typeArray[typeSpinner.getSelectedItemPosition()];
			event.type = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, temp);
			
			

			Account user = dm.getAccount();
			
			
			
			
			
			if(startCalendar.getTimeInMillis() < endCalendar.getTimeInMillis()){	
				
				event.startCalendar = startCalendar;
				event.endCalendar = endCalendar;
				
//				if (!user.timezone.equalsIgnoreCase(event.timezone)){
//					event.setLocalCalendars();
//				}else {
//					event.localStartCalendar = startCalendar;
//					event.localEndCalendar = endCalendar;
//				}
					
					
			
				
				
				event.my_time_start = dtUtils.formatDateTimeToDefault(startCalendar.getTime());
				event.my_time_end = dtUtils.formatDateTimeToDefault(endCalendar.getTime());
				cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START, event.my_time_start);
				cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END, event.my_time_end);
			} else {
				check = false;
				errorStr = getString(R.string.start_equals_end);
			}

			
			event.country = countryArray[countrySpinner.getSelectedItemPosition()];
			cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY, event.country);

			temp = zipView.getText().toString();
			event.zip = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP, temp);

			temp = cityView.getText().toString();
			event.city = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.CITY, temp);

			temp = streetView.getText().toString();
			event.street = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.STREET, temp);

			temp = locationView.getText().toString();
			event.location = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION, temp);

			temp = gobyView.getText().toString();
			event.go_by = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY, temp);

			temp = takewithyouView.getText().toString();
			event.take_with_you = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU, temp);

			temp = costView.getText().toString();
			event.cost = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.COST, temp);

			temp = accomodationView.getText().toString();
			event.accomodation = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION, temp);

			// owner
			cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, 1);
			// user_id
			cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, prefs.getUserId());

			if (check) {
				success = dm.createEvent(event);

				if (!success) {
					cv.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 2);
				}

				getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, cv);
				dm.putEventIntoTreeMap(event);
			}
			Data.selectedContacts = new ArrayList<Contact>();
			return check;
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
		case DIALOG_ERROR:
			builder.setMessage(errorStr).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			break;
		case CHOOSE_CONTACTS_DIALOG:
			builder.setTitle(getString(R.string.choose_contacts)).setMultiChoiceItems(titles, selections, new DialogSelectionClickHandler())
					.setPositiveButton(getString(R.string.ok), new DialogButtonClickHandler());
			break;
		}
		return builder.create();
	}

	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		public void onClick(DialogInterface dialog, int clicked, boolean selected) {
			selections[clicked] = selected;
		}
	}

	private class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int clicked) {
			switch (clicked) {
			case DialogInterface.BUTTON_POSITIVE:
				ArrayList<Integer> list = new ArrayList<Integer>();

				for (int i = 0, l = ids.length; i < l; i++) {
					if (selections[i]) {
						list.add(ids[i]);
					}
				}

				event.assigned_contacts = new int[list.size()];

				for (int i = 0, l = list.size(); i < l; i++) {
					event.assigned_contacts[i] = list.get(i);
				}
				break;
			}
		}
	}
	
	class GetAutoTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			autoColors = dm.getAutoColors();
			autoIcons  = dm.getAutoIcons();
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
			ArrayList<Contact> contacts = dm.getContactsFromLocalDb("");
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

	private void showDateTimeDialog(final EditText view, final int id) {
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

		// Check is system is set to use 24h time (this doesn't seem to work as
		// expected though)
		final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
		final boolean is24h = !(timeS == null || timeS.equals("12"));

		// Update demo TextViews when the "OK" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimePicker.clearFocus();
				switch (id) {
				case DIALOG_START:
					startCalendar = mDateTimePicker.getCalendar();
					break;
				case DIALOG_END:
					endCalendar = mDateTimePicker.getCalendar();
					break;
				}
				view.setText(dtUtils.formatDateTime(mDateTimePicker.getCalendar().getTime()));
				mDateTimeDialog.dismiss();
			}
		});

		// Cancel the dialog when the "Cancel" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimeDialog.cancel();
			}
		});

		// Reset Date and Time pickers when the "Reset" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimePicker.reset();
			}
		});

		// Setup TimePicker
		mDateTimePicker.setIs24HourView(is24h);
		// No title on the dialog window
		mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Set the dialog content view
		mDateTimeDialog.setContentView(mDateTimeDialogView);
		// Display the dialog
		mDateTimeDialog.show();
	}
	
	
		
	

}
