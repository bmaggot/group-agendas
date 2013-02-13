package com.groupagendas.groupagenda.settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Prefs.CalendarElements;
import com.groupagendas.groupagenda.utils.Utils;

public class CalendarSettingsActivity extends Activity implements OnClickListener {
	private Button saveButton;

	private ToggleButton am_pmToggle;

	private Spinner defaultviewSpinner;
	private String[] defaultviewArray;

	private Spinner dateformatSpinner;
	private String[] dateformatArray;

	private DataManagement dm;
	private ProgressBar pb;

	private TextView morningStartView;
	private TextView morningEndView;
	private TextView afternoonStartView;
	private TextView afternoonEndView;
	private TextView eveningStartView;
	private TextView eveningEndView;
	
	private Prefs prefs;

	private TextView destination;
	
	public void setDestination(TextView view) {
		this.destination = view;
	}
	
	private int mHour = 8;
	private int mMinute = 30;
	
	private int[] daytimes = new int[6];
	
	private OnTimeSetListener mOnTimeSetListener = new OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String time = "";
			Calendar tmpCal = Calendar.getInstance();
			Account acc = new Account(CalendarSettingsActivity.this);
			int timeInMillis;
			
			SimpleDateFormat sdf;
			
			mHour = hourOfDay;
			mMinute = minute;
			timeInMillis = (mHour*3600 + mMinute*60)*1000;
			
			tmpCal.set(Calendar.HOUR_OF_DAY, mHour);
			tmpCal.set(Calendar.MINUTE, mMinute);
			
			if (acc.getSetting_ampm() == 1) {
				sdf = new SimpleDateFormat("hh:mm a", Locale.US);
			} else {
				sdf = new SimpleDateFormat("HH:mm", Locale.US);
			}
			
			time = sdf.format(tmpCal.getTime());
			setDaytime(destination, timeInMillis);
			destination.setText(time);
		}
	};
	
	private boolean setDaytime(View target, int valueInMillis) {
		boolean success = false;
		int targetId = target.getId();
		
		switch (targetId) {
		case R.id.morningStartView:
			daytimes[0] = valueInMillis;
			success = true;
			break;
		case R.id.morningEndView:
			daytimes[1] = valueInMillis;
			success = true;
			break;
		case R.id.afternoonStartView:
			daytimes[2] = valueInMillis;
			success = true;
			break;
		case R.id.afternoonEndView:
			daytimes[3] = valueInMillis;
			success = true;
			break;
		case R.id.eveningStartView:
			daytimes[4] = valueInMillis;
			success = true;
			break;
		case R.id.eveningEndView:
			daytimes[5] = valueInMillis;
			success = true;
			break;
		default:
			break;
		}
		
		return success;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_calendar);

		prefs = new Prefs(this);

		dm = DataManagement.getInstance(this);
		pb = (ProgressBar) findViewById(R.id.progress);

		new GetAccountFromDBTask().execute();

		am_pmToggle = (ToggleButton) findViewById(R.id.am_pm);
		am_pmToggle.setChecked(CalendarSettings.isUsing_AM_PM(getApplicationContext()));

		defaultviewSpinner = (Spinner) findViewById(R.id.defaultviewSpinner);
		ArrayAdapter<CharSequence> adapterDefaultview = ArrayAdapter.createFromResource(this, R.array.agenda_views_labels,
				android.R.layout.simple_spinner_item);
		adapterDefaultview.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultviewSpinner.setAdapter(adapterDefaultview);
		defaultviewArray = getResources().getStringArray(R.array.agenda_views_values);
		String dw = CalendarSettings.getDefaultView(getApplicationContext());
		for (int i = 0; i < defaultviewArray.length; i++) {
			if (dw.equalsIgnoreCase(defaultviewArray[i])) {
				defaultviewSpinner.setSelection(i);
				break;
			}
		}

		dateformatSpinner = (Spinner) findViewById(R.id.dateformatSpinner);
		ArrayAdapter<CharSequence> adapterDateformat = ArrayAdapter.createFromResource(this, R.array.date_format_values,
				android.R.layout.simple_spinner_item);
		adapterDateformat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dateformatSpinner.setAdapter(adapterDateformat);
		dateformatArray = getResources().getStringArray(R.array.date_format_values);
		String df = CalendarSettings.getDateFormat(getApplicationContext());
		for (int i = 0; i < dateformatArray.length; i++) {
			if (df.equalsIgnoreCase(dateformatArray[i])) {
				dateformatSpinner.setSelection(i);
				break;
			}
		}
		saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new SaveTask().execute();
			}
		});
		
		morningStartView = (TextView) findViewById(R.id.morningStartView);
		morningEndView = (TextView) findViewById(R.id.morningEndView);
		afternoonStartView = (TextView) findViewById(R.id.afternoonStartView);
		afternoonEndView = (TextView) findViewById(R.id.afternoonEndView);
		eveningStartView = (TextView) findViewById(R.id.eveningStartView);
		eveningEndView = (TextView) findViewById(R.id.eveningEndView);
		
		morningStartView.setOnClickListener(this);
		morningEndView.setOnClickListener(this);
		afternoonStartView.setOnClickListener(this);
		afternoonEndView.setOnClickListener(this);
		eveningStartView.setOnClickListener(this);
		eveningEndView.setOnClickListener(this);
	}

	class SaveTask extends AsyncTask<Void, Boolean, Boolean> implements CalendarElements {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Account account = new Account(CalendarSettingsActivity.this);

			int am_pm = am_pmToggle.isChecked() ? 1 : 0;
			String dateformat = dateformatArray[dateformatSpinner.getSelectedItemPosition()];
			String defaultview = defaultviewArray[defaultviewSpinner.getSelectedItemPosition()];
			
			prefs.setValue(SETTING_MORNING_START, ""+daytimes[0]);
			prefs.setValue(SETTING_MORNING_END, ""+daytimes[1]);
			prefs.setValue(SETTING_AFTERNOON_START, ""+daytimes[2]);
			prefs.setValue(SETTING_AFTERNOON_END, ""+daytimes[3]);
			prefs.setValue(SETTING_EVENING_START, ""+daytimes[4]);
			prefs.setValue(SETTING_EVENING_END, ""+daytimes[5]);
			
			prefs.save();

			dm.changeCalendarSettings(getApplicationContext(), am_pm, defaultview, dateformat);
			//TODO this is temporary workaround for current account update. We should not store data in RAM, but get data from sqlite via providers when needed.
			CalendarSettings.setDateFormat(getApplicationContext(), dateformat);
			CalendarSettings.setUsing_AM_PM(getApplicationContext(), am_pmToggle.isChecked());
			account.setSetting_default_view(defaultview);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
			}
			super.onPostExecute(result);
		}

	}

	private void feelFields(Account account) {
		if (account.getSetting_ampm() == 1) {
			am_pmToggle.setChecked(true);
		} else {
			am_pmToggle.setChecked(false);
		}

		if (!account.getSetting_default_view().equals("null")) {
			int pos = Utils.getArrayIndex(defaultviewArray, account.getSetting_default_view());
			defaultviewSpinner.setSelection(pos);
		}

		if (!account.getSetting_date_format().equals("null")) {
			int pos = Utils.getArrayIndex(dateformatArray, account.getSetting_date_format());
			dateformatSpinner.setSelection(pos);
		}

		daytimes[0] = CalendarSettings.getMorningStart(CalendarSettingsActivity.this);
		daytimes[1] = CalendarSettings.getMorningEnd(CalendarSettingsActivity.this);
		daytimes[2] = CalendarSettings.getAfternoonStart(CalendarSettingsActivity.this);
		daytimes[3] = CalendarSettings.getAfternoonEnd(CalendarSettingsActivity.this);
		daytimes[4] = CalendarSettings.getEveningStart(CalendarSettingsActivity.this);
		daytimes[5] = CalendarSettings.getEveningEnd(CalendarSettingsActivity.this);
		
		morningStartView.setText(getDaytime(0));
		morningEndView.setText(getDaytime(1));
		afternoonStartView.setText(getDaytime(2));
		afternoonEndView.setText(getDaytime(3));
		eveningStartView.setText(getDaytime(4));
		eveningEndView.setText(getDaytime(5));

		prefs.save();
	}

	private String getDaytime(int i) {
		Calendar tmpCal = Calendar.getInstance();
		DateTimeUtils dtUtils = new DateTimeUtils(CalendarSettingsActivity.this);
		int timeInMillis = daytimes[i] / 1000;
		int mHour = timeInMillis / 3600;
		int mMinute = timeInMillis % 3600 / 60;
		tmpCal.set(Calendar.HOUR_OF_DAY, mHour);
		tmpCal.set(Calendar.MINUTE, mMinute);
		
		return dtUtils.formatTime(tmpCal);
	}

	class GetAccountFromDBTask extends AsyncTask<Void, Account, Account> {

		@Override
		protected void onPreExecute() {

			pb.setVisibility(View.VISIBLE);

			super.onPreExecute();
		}

		@Override
		protected Account doInBackground(Void... args) {
			return (new Account(CalendarSettingsActivity.this));
		}

		@Override
		protected void onPostExecute(Account account) {
			feelFields(new Account(CalendarSettingsActivity.this));
			new GetAccountTask().execute();
			super.onPostExecute(account);
		}

	}

	class GetAccountTask extends AsyncTask<Void, Account, Account> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Account doInBackground(Void... args) {
			return dm.getAccountFromRemoteDb(CalendarSettingsActivity.this);
		}

		@Override
		protected void onPostExecute(Account account) {
			if (account != null) {
				feelFields(account);
			}

			pb.setVisibility(View.GONE);
			super.onPostExecute(account);
		}

	}

	@Override
	public void onClick(View v) {
		Account mAccount = new Account(CalendarSettingsActivity.this);
		int timeInMillis;
		int id = v.getId();
		switch (id) {
		case R.id.morningStartView:
			timeInMillis = daytimes[0] / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			setDestination((TextView) v);
			new TimePickerDialog(CalendarSettingsActivity.this, mOnTimeSetListener, mHour, mMinute, !(mAccount.getSetting_ampm() == 1)).show();
			break;
		case R.id.morningEndView:
			timeInMillis = daytimes[1] / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			setDestination((TextView) v);
			new TimePickerDialog(CalendarSettingsActivity.this, mOnTimeSetListener, mHour, mMinute, !(mAccount.getSetting_ampm() == 1)).show();
			break;
		case R.id.afternoonStartView:
			timeInMillis = daytimes[2] / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			setDestination((TextView) v);
			new TimePickerDialog(CalendarSettingsActivity.this, mOnTimeSetListener, mHour, mMinute, !(mAccount.getSetting_ampm() == 1)).show();
			break;
		case R.id.afternoonEndView:
			timeInMillis = daytimes[3] / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			setDestination((TextView) v);
			new TimePickerDialog(CalendarSettingsActivity.this, mOnTimeSetListener, mHour, mMinute, !(mAccount.getSetting_ampm() == 1)).show();
			break;
		case R.id.eveningStartView:
			timeInMillis = daytimes[4] / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			setDestination((TextView) v);
			new TimePickerDialog(CalendarSettingsActivity.this, mOnTimeSetListener, mHour, mMinute, !(mAccount.getSetting_ampm() == 1)).show();
			break;
		case R.id.eveningEndView:
			timeInMillis = daytimes[5] / 1000;
			mHour = timeInMillis / 3600;
			mMinute = timeInMillis % 3600 / 60;
			setDestination((TextView) v);
			new TimePickerDialog(CalendarSettingsActivity.this, mOnTimeSetListener, mHour, mMinute, !(mAccount.getSetting_ampm() == 1)).show();
			break;
		default:
			break;
		}
		
	}
}
