package com.groupagendas.groupagenda.registration;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.events.EventActivity.StaticTimezones;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.PrefixReceiver;
import com.groupagendas.groupagenda.utils.Utils;

public class RegistrationActivity extends Activity {

	private Spinner languageSpinner;
	private String[] languageArray;
	private EditText countryView;
	private EditText timezoneView;
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

	private DataManagement dm;

	private Boolean statement = false;
	private ProgressBar pb;
	private CheckBox chkStatement;
	private AlertDialog mDialog;
	private String localCountry;
	private String defaultEmailAddress;

	private final int DIALOG_SUCCESS = 0;
	private final int DIALOG_ERROR = 1;
	private ArrayList<StaticTimezones> countriesList;
	private CountriesAdapter countriesAdapter;
	private TimezonesAdapter timezonesAdapter;
	private int timezoneInUse = 0;
	private View timezoneSpinnerBlock;
	private View countrySpinnerBlock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		Locale usersLocale = getApplicationContext().getResources().getConfiguration().locale;
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String userPhoneNo;
		String localLanguage;
		
		localCountry = usersLocale.getISO3Country();

		if (tm.getLine1Number() != null)
			userPhoneNo = tm.getLine1Number().toString();
		else
			userPhoneNo = "";
		
		localLanguage = usersLocale.getDisplayLanguage(usersLocale).toString();

		pb = (ProgressBar) findViewById(R.id.progress);

		dm = DataManagement.getInstance(this);

		languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
		ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this, R.array.language_labels,
				android.R.layout.simple_spinner_item);
		adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(adapterLanguage);
		languageArray = getResources().getStringArray(R.array.language_values);
		languageSpinner.setSelection(getMyCountry(languageArray, localLanguage));

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
			// TODO OMG WHAT HAVE I DONE AGAIN?! :|
			StaticTimezones temp = new EventActivity().new StaticTimezones();
			
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
			countriesAdapter = new CountriesAdapter(RegistrationActivity.this, R.layout.search_dialog_item, countriesList);
			timezonesAdapter = new TimezonesAdapter(RegistrationActivity.this, R.layout.search_dialog_item, countriesList);
		}

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (EditText) findViewById(R.id.countryView);
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
						countryView.setText(countriesList.get(timezoneInUse).country);
						timezoneView.setText(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
		timezoneView = (EditText) findViewById(R.id.timezoneView);
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
						countryView.setText(countriesList.get(timezoneInUse).country);
						timezoneView.setText(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});
		
		for (StaticTimezones temp : countriesList) {
			if (temp.country_code.equalsIgnoreCase(localCountry)) {
				timezoneInUse = Integer.parseInt(temp.id);
				countryView.setText(countriesList.get(timezoneInUse).country);
				timezoneView.setText(countriesList.get(timezoneInUse).timezone);
			}
		}

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
		
		phonecodeView = (EditText) findViewById(R.id.phonecodeView);
		try {
			Data.localPrefix = new PrefixReceiver().execute(localCountry).get();
		} catch (InterruptedException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		} catch (ExecutionException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		
		if (Data.localPrefix != null) {
			phonecodeView.setText("+" + Data.localPrefix);
		}
				
		phoneView = (EditText) findViewById(R.id.phoneView);
		if (!userPhoneNo.equalsIgnoreCase(""))
			phoneView.setText(userPhoneNo);			
		
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
				String displayText = "Statement";
				String positiveButtonText = "Agree";
				String negativeButtonText = "Disagree";
				mDialog = new AlertDialog.Builder(RegistrationActivity.this).setTitle(getResources().getString(R.string.app_name))
						.setMessage(displayText).setIcon(R.drawable.ga_logo)
						.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								statement = true;
								chkStatement.setChecked(true);

							}
						}).setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								statement = false;
								chkStatement.setChecked(false);
							}
						}).show();

				WindowManager.LayoutParams layoutParams = mDialog.getWindow().getAttributes();
				layoutParams.dimAmount = 0.9f;
				mDialog.getWindow().setAttributes(layoutParams);
				mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			}
		});

		registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new OnClickListener() {
			private LinearLayout timezoneSpinnerBlock;
			private LinearLayout countrySpinnerBlock;

			@Override
			public void onClick(View v) {
				if (statement) {
					boolean check = true;
					String fieldValue;

					// Password confirm
					fieldValue = confirmView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.confirm));
					} else {
						if (!fieldValue.equals(passwordView.getText().toString())) {
							check = false;
							errorStr = getString(R.string.invalid_confirm);
						}
					}

					// Password
					fieldValue = passwordView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.password));
					}

					// PhoneCode
					fieldValue = phonecodeView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.phone_code));
					}

					// Phone
					fieldValue = phoneView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.phone));
					}

					// Email
					fieldValue = emailView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.email));
					} else {
						if (!Utils.checkEmail(fieldValue)) {
							errorStr = getString(R.string.invalid_email);
						}
					}

					// Lastname
					fieldValue = lastnameView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.lastname));
					}

					// Name
					fieldValue = nameView.getText().toString();
					if (fieldValue.equals("")) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.name));
					}

					// Address
					fieldValue = streetField.getText().toString();
					if ((!fieldValue.equals("") || (fieldValue != null)) && (fieldValue.length() < 3)) {
						check = false;
						errorStr = getString(R.string.registration_error_val_too_short, getString(R.string.registration_address_street_holder));
					}

					fieldValue = cityField.getText().toString();
					if ((!fieldValue.equals("") || (fieldValue != null)) && (fieldValue.length() < 2)) {
						check = false;
						errorStr = getString(R.string.registration_error_val_too_short, getString(R.string.registration_address_zip_holder));
					}

					fieldValue = zipCodeField.getText().toString();
					if ((!fieldValue.equals("") || (fieldValue != null)) && (fieldValue.length() < 3)) {
						check = false;
						errorStr = getString(R.string.registration_error_val_too_short, getString(R.string.registration_address_city_holder));
					}

					timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);
					timezoneView = (EditText) findViewById(R.id.timezoneView);
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
									countryView.setText(countriesList.get(timezoneInUse).country);
									timezoneView.setText(countriesList.get(timezoneInUse).timezone);
									dia1.dismiss();
								}
							});
							dia1.show();
						}
					});

					countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
					countryView = (EditText) findViewById(R.id.countryView);
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
									countryView.setText(countriesList.get(timezoneInUse).country);
									timezoneView.setText(countriesList.get(timezoneInUse).timezone);
									dia1.dismiss();
								}
							});
							dia1.show();
						}
					});

					if (check) {
						new RegistrationTask().execute();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
						builder.setMessage(errorStr).setTitle(getString(R.string.error)).setCancelable(false)
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
			}
		});
	}

	private int getMyCountry (String[] countryList, String myCountryCode) {
		int countryPosition = 0;

		for (int iterator = 0; iterator < countryList.length; iterator++) {
			if (countryList[iterator].equalsIgnoreCase(myCountryCode)) {
					countryPosition = iterator;
			}
		}
		return countryPosition; 
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
			String country = countryView.getText().toString();
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

			return dm.registerAccount(language, country, timezone, sex, name, lastname, email, phonecode, phone, password, city, street, streetNo, zipCode);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pb.setVisibility(View.INVISIBLE);
			if (result) {
				showDialog(DIALOG_SUCCESS);
			} else {
				errorStr = dm.getError();
				showDialog(DIALOG_ERROR);
			}
			super.onPostExecute(result);
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
