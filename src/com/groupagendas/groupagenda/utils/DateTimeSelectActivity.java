package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.events.EventActivity.StaticTimezones;
import com.groupagendas.groupagenda.events.EventEditActivity;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;

public class DateTimeSelectActivity extends Activity implements OnClickListener {
	public static final String ACTIVITY_TARGET_KEY = "ACTIVITY_TARGET";
	public static final int TARGET_NEW_EVENT = 0;
	public static final int TARGET_EVENT_EDIT = 1;

	private static int ACTIVITY_TARGET = 0;

	private DateTimeUtils dtUtils;
	private LinearLayout timezoneSpinnerBlock;
	private LinearLayout countrySpinnerBlock;
	private TextView timezoneView;
	private TextView countryView;
	private TextView startDateView;
	private TextView endDateView;
	private TextView startTimeView;
	private TextView endTimeView;
	private ToggleButton allDayToggleButton;

	private Calendar startCalendar;
	private Calendar endCalendar;

	private Calendar targetCalendar;
	private TextView targetDateView;
	private TextView targetTimeView;
	
	private CountriesAdapter countriesAdapter;
	private TimezonesAdapter timezonesAdapter;
	private int timezoneInUse;
	protected ArrayList<StaticTimezones> countriesList;
	protected ArrayList<StaticTimezones> filteredCountriesList;
	
	private StartEndDateTimeSelectDialog dateTimeDialog;

	private void setTargets(Calendar targetCalendar, TextView targetDateView, TextView targetTimeView) {
		this.targetCalendar = targetCalendar;
		this.targetDateView = targetDateView;
		this.targetTimeView = targetTimeView;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			updateTargets();
		}

		private void updateTargets() {
			Calendar tmpCal1 = (Calendar) targetCalendar.clone();
			Calendar tmpCal2 = (Calendar) startCalendar.clone();
			

			tmpCal1.add(Calendar.HOUR_OF_DAY, 1);
			tmpCal2.add(Calendar.HOUR_OF_DAY, 1);
			
			if (targetCalendar.equals(endCalendar) && (targetCalendar.before(tmpCal2))) {
				targetCalendar.setTime(tmpCal2.getTime());
				endCalendar.setTime(tmpCal2.getTime());
			} else if (targetCalendar.equals(startCalendar) && (targetCalendar.after(endCalendar))) {
				endCalendar.setTime(tmpCal1.getTime());
				endDateView.setText(dtUtils.formatDate(endCalendar.getTime()));
				endTimeView.setText(dtUtils.formatTime(endCalendar));
			}
						
			targetDateView.setText(dtUtils.formatDate(targetCalendar.getTime()));
			targetTimeView.setText(dtUtils.formatTime(targetCalendar));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_time_select);

		Bundle intentExtras = getIntent().getExtras();
		int target = intentExtras.getInt(ACTIVITY_TARGET_KEY);

		dtUtils = new DateTimeUtils(getApplicationContext());
		startDateView = (TextView) findViewById(R.id.startDateView);
		endDateView = (TextView) findViewById(R.id.endDateView);
		startTimeView = (TextView) findViewById(R.id.startTimeView);
		endTimeView = (TextView) findViewById(R.id.endTimeView);
		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneView = (TextView) findViewById(R.id.timezoneView);
		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (TextView) findViewById(R.id.countryView);
		allDayToggleButton = (ToggleButton) findViewById(R.id.allDayToggleButton);

		switch (target) {
		case TARGET_NEW_EVENT:
			ACTIVITY_TARGET = TARGET_NEW_EVENT;
			startDateView.setText(dtUtils
					.formatDate(NewEventActivity.startCalendar.getTime()));
			startTimeView.setText(dtUtils.formatTime(NewEventActivity.startCalendar));
			startCalendar = (Calendar) NewEventActivity.startCalendar.clone();
			endDateView.setText(dtUtils.formatDate(NewEventActivity.endCalendar
					.getTime()));
			endTimeView.setText(dtUtils.formatTime(NewEventActivity.endCalendar));
			endCalendar = (Calendar) NewEventActivity.endCalendar.clone();
			countriesList = NewEventActivity.countriesList;
			filteredCountriesList = NewEventActivity.filteredCountriesList;
			timezoneInUse = NewEventActivity.timezoneInUse;
			timezonesAdapter = NewEventActivity.timezonesAdapter;
			countriesAdapter = NewEventActivity.countriesAdapter;
			
			allDayToggleButton.setChecked(NewEventActivity.event.is_all_day());
			countryView.setText(countriesList.get(timezoneInUse).country2);
			timezoneView.setText(countriesList.get(timezoneInUse).altname);
			
			countrySpinnerBlock.setOnClickListener(this);
			timezoneSpinnerBlock.setOnClickListener(this);
			break;
		case TARGET_EVENT_EDIT:
			ACTIVITY_TARGET = TARGET_EVENT_EDIT;
			startDateView.setText(dtUtils
					.formatDate(EventEditActivity.startCalendar.getTime()));
			startTimeView.setText(dtUtils.formatTime(EventEditActivity.startCalendar));
			startCalendar = (Calendar) EventEditActivity.startCalendar.clone();
			endDateView.setText(dtUtils.formatDate(EventEditActivity.endCalendar
					.getTime()));
			endTimeView.setText(dtUtils.formatTime(EventEditActivity.endCalendar));
			endCalendar = (Calendar) EventEditActivity.endCalendar.clone();
			countriesList = EventEditActivity.countriesList;
			filteredCountriesList = EventEditActivity.filteredCountriesList;
			timezoneInUse = EventEditActivity.timezoneInUse;
			timezonesAdapter = EventEditActivity.timezonesAdapter;
			countriesAdapter = EventEditActivity.countriesAdapter;
			
			allDayToggleButton.setChecked(EventEditActivity.event.is_all_day());
			countryView.setText(countriesList.get(timezoneInUse).country2);
			timezoneView.setText(countriesList.get(timezoneInUse).altname);
			
			countrySpinnerBlock.setOnClickListener(this);
			timezoneSpinnerBlock.setOnClickListener(this);
			break;
		default:
			ACTIVITY_TARGET = TARGET_NEW_EVENT;
			break;
		}

		startDateView.setOnClickListener(this);
		endDateView.setOnClickListener(this);
		startTimeView.setOnClickListener(this);
		endTimeView.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		int timeInMillis;
		int mHour;
		int mMinute;

		int id = v.getId();

		switch (id) {
		case R.id.startDateView:
			this.setTargets(startCalendar, startDateView, startTimeView);
			dateTimeDialog = new StartEndDateTimeSelectDialog(this, StartEndDateTimeSelectDialog.SECTION_DATE, targetCalendar, mDateSetListener);
			dateTimeDialog.show();
			break;
			
		case R.id.startTimeView:
			this.setTargets(startCalendar, startDateView, startTimeView);
			dateTimeDialog = new StartEndDateTimeSelectDialog(this, StartEndDateTimeSelectDialog.SECTION_TIME, targetCalendar, mDateSetListener);
			dateTimeDialog.show();
			break;
			
		case R.id.endDateView:
			this.setTargets(endCalendar, endDateView, endTimeView);
			dateTimeDialog = new StartEndDateTimeSelectDialog(this, StartEndDateTimeSelectDialog.SECTION_DATE, endCalendar, mDateSetListener);
			dateTimeDialog.show();
			break;
			
		case R.id.endTimeView:
			this.setTargets(endCalendar, endDateView, endTimeView);
			dateTimeDialog = new StartEndDateTimeSelectDialog(this, StartEndDateTimeSelectDialog.SECTION_TIME, endCalendar, mDateSetListener);
			dateTimeDialog.show();
			break;
			
		case R.id.saveButton:
			switch (ACTIVITY_TARGET) {
			case TARGET_NEW_EVENT:
				NewEventActivity.setCalendar(startCalendar, endCalendar);
				NewEventActivity.setTimezone(timezoneInUse);
				NewEventActivity.filteredCountriesList = filteredCountriesList;
				NewEventActivity.event.setIs_all_day(allDayToggleButton.isChecked());
				finish();
				break;
			case TARGET_EVENT_EDIT:
				EventEditActivity.setCalendar(startCalendar, endCalendar);
				EventEditActivity.setTimezone(timezoneInUse);
				EventEditActivity.filteredCountriesList = filteredCountriesList;
				EventEditActivity.event.setIs_all_day(allDayToggleButton.isChecked());
				finish();
				break;
			default:
				finish();
				break;
			}
			
		case R.id.button_morning:
			timeInMillis = CalendarSettings.getMorningStart(this) / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			startCalendar.set(Calendar.HOUR_OF_DAY, mHour);
			startCalendar.set(Calendar.MINUTE, mMinute);
			
			timeInMillis = CalendarSettings.getMorningEnd(this) / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			endCalendar.set(Calendar.HOUR_OF_DAY, mHour);
			endCalendar.set(Calendar.MINUTE, mMinute);
			
			startTimeView.setText(dtUtils.formatTime(startCalendar));
			endTimeView.setText(dtUtils.formatTime(endCalendar));
			break;
			
		case R.id.button_afternoon:
			timeInMillis = CalendarSettings.getAfternoonStart(this) / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			startCalendar.set(Calendar.HOUR_OF_DAY, mHour);
			startCalendar.set(Calendar.MINUTE, mMinute);
			
			timeInMillis = CalendarSettings.getAfternoonEnd(this) / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			endCalendar.set(Calendar.HOUR_OF_DAY, mHour);
			endCalendar.set(Calendar.MINUTE, mMinute);
			
			startTimeView.setText(dtUtils.formatTime(startCalendar));
			endTimeView.setText(dtUtils.formatTime(endCalendar));
			break;
			
		case R.id.button_evening:
			timeInMillis = CalendarSettings.getEveningStart(this) / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			startCalendar.set(Calendar.HOUR_OF_DAY, mHour);
			startCalendar.set(Calendar.MINUTE, mMinute);
			
			timeInMillis = CalendarSettings.getEveningEnd(this) / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			endCalendar.set(Calendar.HOUR_OF_DAY, mHour);
			endCalendar.set(Calendar.MINUTE, mMinute);
			
			startTimeView.setText(dtUtils.formatTime(startCalendar));
			endTimeView.setText(dtUtils.formatTime(endCalendar));
			break;
			
		case R.id.timezoneSpinnerBlock:
			final Dialog dia1 = new Dialog(DateTimeSelectActivity.this);
			dia1.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dia1.setContentView(R.layout.search_dialog);

			ListView diaList = (ListView) dia1.findViewById(R.id.dialog_list);
			diaList.setAdapter(timezonesAdapter);
			timezonesAdapter.notifyDataSetChanged();

			EditText searchView = (EditText) dia1
					.findViewById(R.id.dialog_search);

			TextWatcher filterTextWatcher = new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if (s != null) {
						if (timezonesAdapter != null)
							timezonesAdapter.getFilter().filter(s);
					}
				}
			};

			searchView.addTextChangedListener(filterTextWatcher);

			diaList.setOnItemClickListener(new OnItemClickListener() {


				@Override
				public void onItemClick(AdapterView<?> arg0, View view,
						int pos, long arg3) {
					timezoneInUse = Integer.parseInt(view.getTag().toString());
					countryView.setText(countriesList.get(timezoneInUse).country2);

					filteredCountriesList = new ArrayList<StaticTimezones>();

					for (StaticTimezones tz : countriesList) {
						if (tz.country_code.equalsIgnoreCase(countriesList.get(timezoneInUse).country_code)) {
							filteredCountriesList.add(tz);
						}
					}

					timezonesAdapter = new TimezonesAdapter(
							DateTimeSelectActivity.this, R.layout.search_dialog_item,
							filteredCountriesList);
					timezonesAdapter.notifyDataSetChanged();

					timezoneView.setText(countriesList.get(timezoneInUse).altname);
					dia1.dismiss();
				}
			});
			dia1.show();
			break;
		case R.id.countrySpinnerBlock:
			final Dialog dia = new Dialog(DateTimeSelectActivity.this);
			dia.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dia.setContentView(R.layout.search_dialog);

			ListView diaList1 = (ListView) dia.findViewById(R.id.dialog_list);
			diaList1.setAdapter(countriesAdapter);
			countriesAdapter.notifyDataSetChanged();

			EditText searchView1 = (EditText) dia.findViewById(R.id.dialog_search);

			TextWatcher filterTextWatcher1 = new TextWatcher() {
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

			searchView1.addTextChangedListener(filterTextWatcher1);

			diaList1.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
					timezoneInUse = Integer.parseInt(view.getTag().toString());
					countryView.setText(countriesList.get(timezoneInUse).country2);

					filteredCountriesList = new ArrayList<StaticTimezones>();

					for (StaticTimezones tz : countriesList) {
						if (tz.country_code.equalsIgnoreCase(countriesList.get(timezoneInUse).country_code)) {
							filteredCountriesList.add(tz);
						}
					}

					timezonesAdapter = new TimezonesAdapter(DateTimeSelectActivity.this, R.layout.search_dialog_item, filteredCountriesList);
					timezonesAdapter.notifyDataSetChanged();

					timezoneView.setText(countriesList.get(timezoneInUse).altname);
					dia.dismiss();
				}
			});
			dia.show();
			break;
		default:
			break;
		}
	}
}
