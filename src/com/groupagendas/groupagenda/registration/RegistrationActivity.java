package com.groupagendas.groupagenda.registration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.events.EventActivity.StaticTimezones;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;

public class RegistrationActivity extends Activity {
	private static String POLICY_URL = "http://www.groupagendas.com/info/privacy";

	private Spinner languageSpinner;
	private String[] languageArray;
	private TextView countryView;
	private TextView timezoneView;
	private Spinner sexSpinner;
	private String[] sexArray;
	private EditText nameView;
	private EditText lastnameView;
	private EditText emailView;
	private EditText phonecodeView;
	private EditText phoneView;
	private EditText passwordView;
	private EditText confirmView;
	private EditText zipCodeField;
	private EditText streetField;
	private EditText streetNoField;
	private EditText cityField;

	private Button registerButton;
	private Button statementsButton;

	private String errorStr;

	private ProgressBar pb;
	private String localCountry;
	private String defaultEmailAddress;

	private static final int DIALOG_SUCCESS = 0;
	private static final int DIALOG_ERROR = 1;
	private ArrayList<StaticTimezones> countriesList;
	private ArrayList<StaticTimezones> filteredCountriesList;
	private CountriesAdapter countriesAdapter;
	private TimezonesAdapter timezonesAdapter;
	private int timezoneInUse = 0;
	private View timezoneSpinnerBlock;
	private View countrySpinnerBlock;
	private String localLanguage;
	private CheckBox chkStatement;
	
	private String dateFormat;
	private boolean ampm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		String userPhoneNo;

		String[] cities;
		String[] countries;
		String[] countries2;
		String[] country_codes;
		String[] timezones;
		String[] altnames;
		String[] call_codes;
		countriesList = new ArrayList<StaticTimezones>();

		cities = getResources().getStringArray(R.array.city);
		countries = getResources().getStringArray(R.array.countries);
		countries2 = getResources().getStringArray(R.array.countries2);
		country_codes = getResources().getStringArray(R.array.country_codes);
		timezones = getResources().getStringArray(R.array.timezones);
		altnames = getResources().getStringArray(R.array.timezone_altnames);
		call_codes = getResources().getStringArray(R.array.call_codes);
		for (int i = 0; i < cities.length; i++) {
			// TODO OMG WHAT HAVE I DONE AGAIN?! :|
			StaticTimezones temp = new EventActivity().new StaticTimezones();

			temp.id = "" + i;
			temp.city = cities[i];
			temp.country = countries[i];
			temp.country2 = countries2[i];
			temp.country_code = country_codes[i];
			temp.timezone = timezones[i];
			temp.altname = altnames[i];
			temp.call_code = call_codes[i];

			countriesList.add(temp);
		}
		if (countriesList != null) {
			countriesAdapter = new CountriesAdapter(RegistrationActivity.this, R.layout.search_dialog_item, countriesList);
		}

		Locale usersLocale = getApplicationContext().getResources().getConfiguration().locale;
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Locale simLocale = new Locale(tm.getSimCountryIso(), tm.getSimCountryIso());
		
		//dateformat
		SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getDateFormat(getApplicationContext());
		dateFormat = simpleDateFormat.toLocalizedPattern();
		simpleDateFormat.getCalendar().get(Calendar.AM_PM);
		
		//ampm
		if(DateFormat.is24HourFormat(getApplicationContext())){
			ampm = false;
		} else {
			ampm = true;
		}

		//locallanguage
		if(simLocale.getDisplayLanguage() != null && !simLocale.getDisplayLanguage().equals("")){
			localLanguage = simLocale.getDisplayLanguage();
		} else {
			localLanguage = usersLocale.getDisplayLanguage(usersLocale).toString();
		}
		
		//localcountry
		if(simLocale.getISO3Country() != null && !simLocale.getISO3Country().equals("")){
			localCountry = simLocale.getISO3Country();
		} else {
			localCountry = usersLocale.getISO3Country();
		}

		//phonenumber
		if (tm.getLine1Number() != null)
			userPhoneNo = tm.getLine1Number().toString();
		else
			userPhoneNo = "";

		pb = (ProgressBar) findViewById(R.id.progress);
		phoneView = (EditText) findViewById(R.id.phoneView);
		phonecodeView = (EditText) findViewById(R.id.phonecodeView);
		countryView = (TextView) findViewById(R.id.countryView);
		timezoneView = (TextView) findViewById(R.id.timezoneView);
		String countryCode = "";

		if (!userPhoneNo.equalsIgnoreCase(""))
			phoneView.setText(userPhoneNo);

		for (StaticTimezones temp : countriesList) {
			if (temp.country_code.equalsIgnoreCase(localCountry)) {
				timezoneInUse = Integer.parseInt(temp.id);
				countryView.setText(countriesList.get(timezoneInUse).country2);
				timezoneView.setText(countriesList.get(timezoneInUse).timezone);
				phonecodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
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
		
		timezonesAdapter = new TimezonesAdapter(RegistrationActivity.this, R.layout.search_dialog_item, filteredCountriesList);
		timezonesAdapter.notifyDataSetChanged();
		
	}

	@Override
	public void onResume() {
		super.onResume();

		chkStatement = (CheckBox) findViewById(R.id.chk_statement);
		languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
		ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this, R.array.language_labels,
				android.R.layout.simple_spinner_item);
		adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(adapterLanguage);
		languageArray = getResources().getStringArray(R.array.language_values);
		languageSpinner.setSelection(getMyLanguage(languageArray, localLanguage));

		phonecodeView = (EditText) findViewById(R.id.phonecodeView);

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (TextView) findViewById(R.id.countryView);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(RegistrationActivity.this);
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
						String countryCode = countriesList.get(timezoneInUse).country_code;
						
						filteredCountriesList = new ArrayList<StaticTimezones>();
						
						for (StaticTimezones tz : countriesList) {
							if (tz.country_code.equalsIgnoreCase(countryCode)) {
								filteredCountriesList.add(tz);
							}
						}
						
						timezonesAdapter = new TimezonesAdapter(RegistrationActivity.this, R.layout.search_dialog_item, filteredCountriesList);
						timezonesAdapter.notifyDataSetChanged();
						
						timezoneView.setText(countriesList.get(timezoneInUse).timezone);
						phonecodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneView = (TextView) findViewById(R.id.timezoneView);
		timezoneSpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(RegistrationActivity.this);
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
						timezoneView.setText(countriesList.get(timezoneInUse).timezone);
						phonecodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		sexSpinner = (Spinner) findViewById(R.id.sexSpinner);
		ArrayAdapter<CharSequence> adapterSex = ArrayAdapter.createFromResource(this, R.array.sex_labels,
				android.R.layout.simple_spinner_item);
		adapterSex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sexSpinner.setAdapter(adapterSex);
		sexArray = getResources().getStringArray(R.array.sex_values);

		nameView = (EditText) findViewById(R.id.nameView);
		lastnameView = (EditText) findViewById(R.id.lastnameView);
		emailView = (EditText) findViewById(R.id.emailView);

		if (AccountManager.get(getApplicationContext()).getAccountsByType("com.google").length > 0) {
			defaultEmailAddress = AccountManager.get(getApplicationContext()).getAccountsByType("com.google")[0].name;
			if ((defaultEmailAddress != null) && (!defaultEmailAddress.equals("")))
				emailView.setText(defaultEmailAddress);
		}

		passwordView = (EditText) findViewById(R.id.passwordView);
		confirmView = (EditText) findViewById(R.id.confirmView);
		zipCodeField = (EditText) findViewById(R.id.registration_zip);
		streetNoField = (EditText) findViewById(R.id.registration_street_no);
		streetField = (EditText) findViewById(R.id.registration_street);
		cityField = (EditText) findViewById(R.id.registration_city);

		chkStatement = (CheckBox) findViewById(R.id.chk_statement);

		statementsButton = (Button) findViewById(R.id.statementButton);
		statementsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent policy = new Intent(Intent.ACTION_VIEW);
				policy.setData(Uri.parse(POLICY_URL));
				startActivity(policy);
			}
		});

		registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String valRes = validateFields();
				if (valRes == null) {
					new RegistrationTask().execute();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
					builder.setMessage(valRes).setTitle(getString(R.string.error)).setCancelable(false)
							.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
		Log.e("Registerationas",  "Default dateformat: " + dateFormat + " AmPm setting: " + ampm);
	}

	private int getMyLanguage(String[] countryList, String myLanguage) {
		int countryPosition = 0;

		for (int iterator = 0; iterator < countryList.length; iterator++) {
			if (countryList[iterator].equalsIgnoreCase(myLanguage)) {
				countryPosition = iterator;
			}
		}
		return countryPosition;
	}

	// TODO write validation documentation.
	private String validateFields() {
		StringBuilder sb = new StringBuilder();
		String validationRes = null;
		String temp;

		if (!chkStatement.isChecked()) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[0]);
		}

		if (nameView.getText().length() < 3) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[1]);
		}

		if (lastnameView.getText().length() < 2) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[2]);
		}

		if (countryView.getText().length() < 3 || timezoneView.getText().length() < 3) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[3]);
		}

		temp = emailView.getText().toString();
		if (temp.length() < 6 || !temp.contains("@")) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[4]);
		}

		temp = phoneView.getText().toString();
		if (temp.length() < 5) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[5]);
		}

		temp = phonecodeView.getText().toString();
		if (temp.length() < 1 || temp.length() > 4) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[6]);
		}

		temp = passwordView.getText().toString();
		if (temp.length() < 4 || temp.length() > 20) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[7]);
		}

		if (!confirmView.getText().toString().equals(temp)) {
			sb.append(getResources().getStringArray(R.array.registration_msgs)[8]);
		}

		if (sb.length() > 1)
			validationRes = sb.toString();

		return validationRes;
	}

	class RegistrationTask extends AsyncTask<Void, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			String language = languageArray[languageSpinner.getSelectedItemPosition()];
			String country = countriesList.get(timezoneInUse).country_code;
			String timezone = timezoneView.getText().toString();
			String sex = sexArray[sexSpinner.getSelectedItemPosition()];
			String name = nameView.getText().toString();
			String lastname = lastnameView.getText().toString();
			String email = emailView.getText().toString();
			String phonecode = phonecodeView.getText().toString();
			String phone = phoneView.getText().toString();
			String password = passwordView.getText().toString();
			String street = streetField.getText().toString();
			String streetNo = streetNoField.getText().toString();
			String zipCode = zipCodeField.getText().toString();
			String city = cityField.getText().toString();

			return DataManagement.registerAccount(language, country, timezone, sex, name, lastname, email, phonecode, phone, password,
					city, street, streetNo, zipCode);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pb.setVisibility(View.INVISIBLE);
			if (result) {
				showDialog(DIALOG_SUCCESS);
			} else {
				if (errorStr == null)
					errorStr = new String();

				errorStr = getResources().getStringArray(R.array.registration_msgs)[9] + DataManagement.getError();
				showDialog(DIALOG_ERROR);
			}
			// super.onPostExecute(result);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
		switch (id) {
		case DIALOG_ERROR:
			builder.setMessage(errorStr).setTitle(getString(R.string.error)).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_SUCCESS:
			builder.setMessage(getString(R.string.need_activation)).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							onBackPressed();
						}
					});
			break;
		}
		return builder.create();
	}
}
