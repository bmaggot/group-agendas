package com.groupagendas.groupagenda.registration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.Utils;

public class RegisterationActivity extends Activity {

	private Spinner languageSpinner;
	private String[] languageArray;
	private Spinner countrySpinner;
	private String[] countryArray;
	private Spinner timezoneSpinner;
	private String[] timezoneArray;
	private Spinner sexSpinner;
	private String[] sexArray;
	private EditText nameView;
	private EditText lastnameView;
	private EditText emailView;
	private EditText phonecodeView;
	private EditText phoneView;
	private EditText passwordView;
	private EditText confirmView;

	private Button registerButton;
	private Button statementsButton;

	private String errorStr;

	private DataManagement dm;

	private Boolean statement = false;
	private ProgressBar pb;
	private CheckBox chkStatement;
	private AlertDialog mDialog;
	private String phonePrefix = null;

	private final int DIALOG_SUCCESS = 0;
	private final int DIALOG_ERROR = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		pb = (ProgressBar) findViewById(R.id.progress);

		dm = DataManagement.getInstance(this);

		languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
		ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this, R.array.language_labels,
				android.R.layout.simple_spinner_item);
		adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(adapterLanguage);
		languageArray = getResources().getStringArray(R.array.language_values);

		timezoneSpinner = (Spinner) findViewById(R.id.timezoneSpinner);

		countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
		ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				CountryManager.getCountries(this));
		adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapterCountry);
		countryArray = CountryManager.getCountryValues(this);
		countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (pos == 0) {
					ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(RegisterationActivity.this,
							android.R.layout.simple_spinner_item, new String[0]);
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);
					timezoneSpinner.setEnabled(false);
					timezoneArray = null;
					phonePrefix = null;
				} else {
					timezoneSpinner.setEnabled(true);
					String[] timezoneLabels = TimezoneManager.getTimezones(RegisterationActivity.this, countryArray[pos]);
					ArrayAdapter<String> adapterTimezone = new ArrayAdapter<String>(RegisterationActivity.this,
							android.R.layout.simple_spinner_item, timezoneLabels);
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);

					timezoneArray = TimezoneManager.getTimezonesValues(RegisterationActivity.this, countryArray[pos]);

					try {
						phonePrefix = dm.getPhonePrefix(countryArray[pos]);
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (phonePrefix != null) {
						phonecodeView.setText("+" + phonePrefix);
					}

				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
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
		phonecodeView = (EditText) findViewById(R.id.phonecodeView);
		phoneView = (EditText) findViewById(R.id.phoneView);
		passwordView = (EditText) findViewById(R.id.passwordView);
		confirmView = (EditText) findViewById(R.id.confirmView);

		chkStatement = (CheckBox) findViewById(R.id.chk_statement);

		statementsButton = (Button) findViewById(R.id.statementButton);
		statementsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String displayText = "Statement";
				String positiveButtonText = "Agree";
				String negativeButtonText = "Disagree";
				mDialog = new AlertDialog.Builder(RegisterationActivity.this).setTitle(getResources().getString(R.string.app_name))
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
			@Override
			public void onClick(View v) {
				if (statement) {
					boolean check = true;
					int selectedItem;
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

					// Sex
					selectedItem = sexSpinner.getSelectedItemPosition();
					if (selectedItem == 0) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.sex));
					}

					// Country
					selectedItem = countrySpinner.getSelectedItemPosition();
					if (selectedItem == 0) {
						check = false;
						errorStr = getString(R.string.field_is_required, getString(R.string.country));
					}

					if (check) {
						new RegistrationTask().execute();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(RegisterationActivity.this);
						builder.setMessage(errorStr).setTitle(getString(R.string.error)).setCancelable(false)
								.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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

	class RegistrationTask extends AsyncTask<Void, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			String language = languageArray[languageSpinner.getSelectedItemPosition()];
			String country = countryArray[countrySpinner.getSelectedItemPosition()];
			String timezone = timezoneArray[timezoneSpinner.getSelectedItemPosition()];
			String sex = sexArray[sexSpinner.getSelectedItemPosition()];
			String name = nameView.getText().toString();
			String lastname = lastnameView.getText().toString();
			String email = emailView.getText().toString();
			String phonecode = phonecodeView.getText().toString();
			String phone = phoneView.getText().toString();
			String password = passwordView.getText().toString();

			return dm.registerAccount(language, country, timezone, sex, name, lastname, email, phonecode, phone, password);
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
		AlertDialog.Builder builder = new AlertDialog.Builder(RegisterationActivity.this);
		switch (id) {
		case DIALOG_ERROR:
			builder.setMessage(errorStr).setTitle(getString(R.string.error)).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_SUCCESS:
			builder.setMessage(getString(R.string.need_activation)).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							onBackPressed();
						}
					});
			break;
		}
		return builder.create();
	}
}
