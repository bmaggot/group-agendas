package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventsAdapter.ViewHolder;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.EventStatusUpdater;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class EventActivity extends Activity {
	private DataManagement dm;
	private DateTimeUtils dtUtils;
	
	private int event_id;
	
	private ProgressBar pb;
	private Button saveButton;
	private TextView topText;

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
	
	private LinearLayout addressLine;
	private Spinner countrySpinner;
	private String[] countryArray;
	private EditText cityView;
	private EditText streetView;
	private EditText zipView;
	private Spinner timezoneSpinner;
	private String[] timezoneArray;
	
	private LinearLayout detailsLine;
	private EditText locationView;
	private EditText gobyView;
	private EditText takewithyouView;
	private EditText costView;
	private EditText accomodationView;
	
	private View responsePanel;
	private LinearLayout invitesColumn;
	private LinearLayout invitedPersonList;
	
	private Event event;
	
	private String errorStr = "";
	private final int DIALOG_ERROR = 0;
	private final int DELETE_DIALOG = 1;
	
	private ArrayList<AutoColorItem> autoColors = null;
	private ArrayList<AutoIconItem> autoIcons = null;
	
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_edit);

		dm = DataManagement.getInstance(this);
		dtUtils = new DateTimeUtils(this);
		responsePanel = (View) findViewById(R.id.response_to_invitation);
		intent = getIntent();

		pb = (ProgressBar) findViewById(R.id.progress);
		saveButton = (Button) findViewById(R.id.saveButton);
		topText = (TextView) findViewById(R.id.topText);

		String typeStr = "";
		if(intent.getStringExtra("type") != null){
			typeStr = new StringBuilder(intent.getStringExtra("type")).append("_type").toString();
		}
		int typeId = getResources().getIdentifier(typeStr, "string", "com.groupagendas.groupagenda");

		if(topText != null && typeId != 0){
			topText.setText(getString(typeId));
		}
		
		event_id = intent.getIntExtra("event_id", 0);
		if (event_id > 0) {
			new GetEventTask().execute(event_id);
		} else{
			
		}
	}

	class GetEventTask extends AsyncTask<Integer, Event, Event> {
		
		final DataManagement dm = DataManagement.getInstance(getParent());
		final Context mContext = dm.getmContext();
		
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Event doInBackground(Integer... ids) {
			autoColors = dm.getAutoColors();
			autoIcons  = dm.getAutoIcons();
			
			if(intent.getBooleanExtra("isNative", false)){
				return dm.getNativeCalendarEvent(ids[0]);
			}else{
				return dm.getEventFromDb(ids[0]);
			}
		}

		@Override
		protected void onPostExecute(final Event result) {
			event = result;
			// icon
			final String[] iconsValues = getResources().getStringArray(R.array.icons_values); 
			iconView = (ImageView) findViewById(R.id.iconView);
			if (result.icon !=null && !result.icon.equals("null")) {
				int iconId = getResources().getIdentifier(result.icon, "drawable", "com.groupagendas.groupagenda");
				iconView.setImageResource(iconId);
			}

			if (result.is_owner) {
				iconView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Dialog dialog = new Dialog(EventActivity.this);
						dialog.setContentView(R.layout.list_dialog);
						dialog.setTitle(R.string.choose_icon);
						
						GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
					    gridview.setAdapter(new IconsAdapter(EventActivity.this, iconsValues));
					    
					    gridview.setOnItemClickListener(new OnItemClickListener() {
					        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					        	if(iconsValues[position].equals("noicon")){
									event.icon = "";
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
			}

			// color
			final String[] colorsValues = getResources().getStringArray(R.array.colors_values);
			colorView = (ImageView) findViewById(R.id.colorView);
			if (result.color !=null && !result.color.equals("null")) {
				String nameColor = "calendarbubble_"+result.color+"_";
				int image = getResources().getIdentifier(nameColor, "drawable", "com.groupagendas.groupagenda");
				colorView.setImageResource(image);
			}

			if (result.is_owner) {
				colorView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Dialog dialog = new Dialog(EventActivity.this);
						dialog.setContentView(R.layout.list_dialog);
						dialog.setTitle(R.string.choose_color);
						
						GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
					    gridview.setAdapter(new ColorsAdapter(EventActivity.this, colorsValues));
						
						gridview.setOnItemClickListener(new OnItemClickListener() {
					        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					        	event.color = colorsValues[position];
								String nameColor = "calendarbubble_"+event.color+"_";
								int image = getResources().getIdentifier(nameColor, "drawable", "com.groupagendas.groupagenda");
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
			if (!result.is_owner){
				titleView.setEnabled(false);
			}else{
				titleView.addTextChangedListener(filterTextWatcher);
			}
			
			// type
			typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
			ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(EventActivity.this, R.array.type_labels, android.R.layout.simple_spinner_item);
			adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			typeSpinner.setAdapter(adapterType);
			typeArray = getResources().getStringArray(R.array.type_values);
			
			if(result.type != null && !result.type.equals("null")){
				int pos = Utils.getArrayIndex(typeArray, result.type);
				typeSpinner.setSelection(pos);
				if(!result.is_owner) typeSpinner.setEnabled(false);
			}
			
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
			if(result.my_time_start != null && !result.my_time_start.equals("null")){
				startView.setText(dtUtils.formatDateTime(result.my_time_start));
				startCalendar = Utils.stringToCalendar(result.my_time_start, DateTimeUtils.DEFAULT_DATETIME);
			}
			
			if(!result.is_owner){
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
			
			if(result.my_time_end != null && !result.my_time_end.equals("null")){
				endView.setText(dtUtils.formatDateTime(result.my_time_end));
				endCalendar = Utils.stringToCalendar(result.my_time_end, DateTimeUtils.DEFAULT_DATETIME);
			}
			
			if(!result.is_owner){
				endView.setEnabled(false);
				endButton.setEnabled(false);
			}
			
			//Description
			descView = (EditText) findViewById(R.id.descView);
			if(result.description_ != null && !result.description_.equals("null")){
				LinearLayout parent = (LinearLayout) descView.getParent();
				parent.setVisibility(View.VISIBLE);
				descView.setText(result.description_);
				if(!result.is_owner) descView.setEnabled(false);
			}
			
			
			// Address
			addressLine = (LinearLayout) findViewById(R.id.addressLine);
				//timezone
			timezoneSpinner = (Spinner) findViewById(R.id.timezoneSpinner);
			if(result.is_owner) showView(timezoneSpinner, addressLine);
				// country
			countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
			ArrayAdapter<String> adapterCountry =  new ArrayAdapter<String>(EventActivity.this, android.R.layout.simple_spinner_item, CountryManager.getCountries(EventActivity.this)) ;
			adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			countrySpinner.setAdapter(adapterCountry);
			countryArray = CountryManager.getCountryValues(EventActivity.this);
			countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {

					if(pos == 0){
						ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(EventActivity.this, android.R.layout.simple_spinner_item, new String[0]) ;
						adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						timezoneSpinner.setAdapter(adapterTimezone);
						timezoneSpinner.setEnabled(false);
						timezoneArray = null;
					}else{
						timezoneSpinner.setEnabled(true);
						//timezone
						String[] timezoneLabels = TimezoneManager.getTimezones(EventActivity.this, countryArray[pos]);
						ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(EventActivity.this, android.R.layout.simple_spinner_item, timezoneLabels) ;
						adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						timezoneSpinner.setAdapter(adapterTimezone);
						timezoneArray = TimezoneManager.getTimezonesValues(EventActivity.this, countryArray[pos]);
						
						if(result.timezone != null && !result.timezone.equals("null")){
							pos = Utils.getArrayIndex(timezoneArray, result.timezone);
							timezoneSpinner.setSelection(pos);
							showView(timezoneSpinner, addressLine);
							if(!result.is_owner) timezoneSpinner.setEnabled(false);
						}
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
			
			if(result.country != null && !result.country.equals("null")){
				int pos = Utils.getArrayIndex(countryArray, result.country);
				countrySpinner.setSelection(pos);
				showView(countrySpinner, addressLine);
				if(!result.is_owner) countrySpinner.setEnabled(false);
			}
			if(result.is_owner) showView(countrySpinner, addressLine);
			
			   // city
			cityView = (EditText) findViewById(R.id.cityView);
			if(result.city != null && !result.city.equals("null")){
				cityView.setText(result.city);
				showView(cityView, addressLine);
				if(!result.is_owner) cityView.setEnabled(false);
			}
			if(result.is_owner) showView(cityView, addressLine);
			
			   // street
			streetView = (EditText) findViewById(R.id.streetView);
			if(result.street != null && !result.street.equals("null")){
				streetView.setText(result.street);
				showView(streetView, addressLine);
				if(!result.is_owner) streetView.setEnabled(false);
			}
			if(result.is_owner) showView(streetView, addressLine);
			
			   // zip
			zipView = (EditText) findViewById(R.id.zipView);
			if(result.zip != null && !result.zip.equals("null")){
				zipView.setText(result.zip);
				showView(zipView, addressLine);
				if(!result.is_owner) zipView.setEnabled(false);
			}
			if(result.is_owner) showView(zipView, addressLine);
			
			
			// Details
			detailsLine = (LinearLayout) findViewById(R.id.detailsLine);
			   // location
			locationView = (EditText) findViewById(R.id.locationView);
			if(result.location != null && !result.location.equals("null")){
				locationView.setText(result.location);
				showView(locationView, detailsLine);
				if(!result.is_owner) locationView.setEnabled(false);
			}
			if(result.is_owner) showView(locationView, detailsLine);
				// Go by
			gobyView = (EditText) findViewById(R.id.gobyView);
			if(result.go_by != null && !result.go_by.equals("null")){
				gobyView.setText(result.go_by);
				showView(gobyView, detailsLine);
				if(!result.is_owner) gobyView.setEnabled(false);
			}
			if(result.is_owner) showView(gobyView, detailsLine);
			
				// Take with you
			takewithyouView = (EditText) findViewById(R.id.takewithyouView);
			if(result.take_with_you != null && !result.take_with_you.equals("null")){
				takewithyouView.setText(result.take_with_you);
				showView(takewithyouView, detailsLine);
				if(!result.is_owner) takewithyouView.setEnabled(false);
			}
			if(result.is_owner) showView(takewithyouView, detailsLine);
			
				// Cost
			costView = (EditText) findViewById(R.id.costView);
			if(result.cost != null && !result.cost.equals("null")){
				costView.setText(result.cost);
				showView(costView, detailsLine);
				if(!result.is_owner) costView.setEnabled(false);
			}
			if(result.is_owner) showView(costView, detailsLine);
			
				// Accomodation
			accomodationView = (EditText) findViewById(R.id.accomodationView);
			if(result.accomodation != null && !result.accomodation.equals("null")){
				accomodationView.setText(result.accomodation);
				showView(accomodationView, detailsLine);
				if(!result.is_owner) accomodationView.setEnabled(false);
			}
			if(result.is_owner) showView(accomodationView, detailsLine);
			
			if(result.invited != null){
				invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
				invitesColumn.setVisibility(View.VISIBLE);
				
				invitedPersonList = (LinearLayout) findViewById(R.id.invited_person_list);
				
				final ViewHolder holder = new ViewHolder();
				holder.status = (TextView) findViewById(R.id.status);
				holder.button_yes = (TextView) findViewById(R.id.button_yes);
				holder.button_maybe = (TextView) findViewById(R.id.button_maybe);
				holder.button_no = (TextView) findViewById(R.id.button_no);
				
				responsePanel.setVisibility(View.VISIBLE);
				
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				for(int i=0, l=result.invited.size(); i<l; i++){
					final Invited invited = result.invited.get(i);
					
					final View view = inflater.inflate(R.layout.event_invited_person_entry, invitedPersonList, false);
					if (l == 1) {
						view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
					} else {
						if (i == l-1)
							view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
						else
							view.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
					}
					
					final TextView nameView = (TextView) view.findViewById(R.id.invited_fullname);
					nameView.setText(invited.name);
					
					final TextView emailView = (TextView) view.findViewById(R.id.invited_available_email);
					emailView.setText(invited.email);
					
					final TextView statusView = (TextView) view.findViewById(R.id.invited_status);
					
					switch(invited.status_id){
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
					
					invitesColumn.addView(view);
				}
				
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
				
				holder.button_yes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						boolean success = dm.changeEventStatus(event.event_id, "1");
						LinearLayout statusLayout = (LinearLayout) findViewById(R.id.invited_person_list);
						
						holder.button_yes.setVisibility(View.INVISIBLE);
						holder.button_maybe.setVisibility(View.VISIBLE);
						holder.button_no.setVisibility(View.VISIBLE);
						holder.status.setText(mContext.getString(R.string.status_1));
						editDb(event.event_id, 1, success);
						event.status = 1;
						for (int iterator = 0, childAmount = statusLayout.getChildCount(); iterator < childAmount; childAmount++) {
							TextView fullnameView = (TextView) statusLayout.getChildAt(iterator).findViewById(R.id.invited_fullname);
							if (fullnameView.getText().equals("You")) {
								TextView statusView = (TextView) statusLayout.getChildAt(iterator).findViewById(R.id.invited_status);
								statusView.setText(mContext.getString(R.string.status_1));
							}
						}
					}
				});
				
				holder.button_maybe.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						boolean success = dm.changeEventStatus(event.event_id, "2");
						LinearLayout statusLayout = (LinearLayout) findViewById(R.id.invited_person_list);
						
						holder.button_yes.setVisibility(View.VISIBLE);
						holder.button_maybe.setVisibility(View.INVISIBLE);
						holder.button_no.setVisibility(View.VISIBLE);
						holder.status.setText(mContext.getString(R.string.status_2));
						editDb(event.event_id, 2, success);
						event.status = 2;
						for (int iterator = 0, childAmount = statusLayout.getChildCount(); iterator < childAmount; childAmount++) {
							TextView fullnameView = (TextView) statusLayout.getChildAt(iterator).findViewById(R.id.invited_fullname);
							if (fullnameView.getText().equals("You")) {
								TextView statusView = (TextView) statusLayout.getChildAt(iterator).findViewById(R.id.invited_status);
								statusView.setText(mContext.getString(R.string.status_2));
							}
						}
					}
				});
				
				holder.button_no.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						boolean success = dm.changeEventStatus(event.event_id, "0");
						LinearLayout statusLayout = (LinearLayout) findViewById(R.id.invited_person_list);
						
						holder.button_yes.setVisibility(View.VISIBLE);
						holder.button_maybe.setVisibility(View.VISIBLE);
						holder.button_no.setVisibility(View.INVISIBLE);
						holder.status.setText(mContext.getString(R.string.status_0));
						editDb(event.event_id, 0, success);
						event.status = 0;
						for (int iterator = 0, childAmount = statusLayout.getChildCount(); iterator < childAmount; childAmount++) {
							TextView fullnameView = (TextView) statusLayout.getChildAt(iterator).findViewById(R.id.invited_fullname);
							if (fullnameView.getText().equals("You")) {
								TextView statusView = (TextView) statusLayout.getChildAt(iterator).findViewById(R.id.invited_status);
								statusView.setText(mContext.getString(R.string.status_0));
							}
						}
					}
				});
			}
			
			// Save
			LinearLayout saveBlock = (LinearLayout) findViewById(R.id.saveBlock);
			if(result.is_owner) saveBlock.setVisibility(View.VISIBLE);
			
			/////////////
			pb.setVisibility(View.INVISIBLE);
			super.onPostExecute(result);
		}
		
		private void editDb(int event_id, int status, boolean success){
			Object[] array = {event_id, status, success, dm};
			new EventStatusUpdater().execute(array);
		}
		
		
	}
	
	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s !=null){
				if(event.icon ==null || event.icon.equals("null") || event.icon.equals("")){
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
	
	public void saveEvent(View v){
		new UpdateEventTask().execute(event);
	}
	
	class UpdateEventTask extends AsyncTask<Event, Void, Boolean>{
		
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
			ContentValues cv = new ContentValues();
			
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
			if(temp.length() <= 0){
				check = false;
				errorStr = getString(R.string.desc_is_required);
			}
			event.description_ = temp;
			cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, temp);
			
			// title
			temp = titleView.getText().toString();
			if(temp.length() <= 0){
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
			
			event.my_time_start = dtUtils.formatDateTimeToDefault(startCalendar.getTime());
			cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_START, event.my_time_start);
			
			event.my_time_end = dtUtils.formatDateTimeToDefault(endCalendar.getTime());
			cv.put(EventsProvider.EMetaData.EventsMetaData.MY_TIME_END, event.my_time_end);
			
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
			
			if(check){
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+"/"+event.event_id);
				getContentResolver().update(uri, cv, null, null);
				success = dm.editEvent(event);
				
				if(!success){
					cv = new ContentValues();
					cv.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 4);
					getContentResolver().update(uri, cv, null, null);
				}
			}
			
			return check;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				finish();
			}else{
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
			builder.setMessage(errorStr)
		       .setCancelable(false)
		       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });
			break;
		case DELETE_DIALOG:
			builder.setMessage(getString(R.string.sure_delete)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new DeleteEventTask().execute();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			break;
		}
		return builder.create();
	}
	
	class DeleteEventTask extends AsyncTask<Void, Boolean, Boolean> {
		protected Boolean doInBackground(Void... type) {
			
			if(event_id > 0){
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+"/"+event_id);
				
				Boolean result = dm.removeEvent(event_id);
				
				if(result){
					getContentResolver().delete(uri, null, null);
				}else if(DataManagement.getCONNECTION_ERROR().equals(dm.getError())){
					result = true;
					ContentValues values = new ContentValues();
					values.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 3);
					
					getContentResolver().update(uri, values, null, null);
				}
				return result;
			}
			
			return false;
		}

		protected void onPostExecute(Boolean result) {
			Toast toast = Toast.makeText(EventActivity.this, "", Toast.LENGTH_LONG);
			if(result){
				toast.setText(getString(R.string.event_deleted));
			}else{
				toast.setText(dm.getError());
			}
			toast.show();
			
			super.onPostExecute(result);
			onBackPressed();
		}

	}
	
	private void showView(View view, LinearLayout line){
		line.setVisibility(View.VISIBLE);
		LinearLayout parent = (LinearLayout)view.getParent();
		parent.setVisibility(View.VISIBLE);
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
			c = startCalendar;
			break;
		case DIALOG_END:
			c = endCalendar;
			break;
		}
		mDateTimePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateTimePicker.updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		
		
		// Check is system is set to use 24h time (this doesn't seem to work as expected though)
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
