package com.groupagendas.groupagenda.account;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import net.londatiga.android.CropOption;
import net.londatiga.android.CropOptionAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.events.EventActivity.StaticTimezones;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.LanguageCodeGetter;
import com.groupagendas.groupagenda.utils.Utils;

public class AccountActivity extends Activity implements OnClickListener {

	// image
	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private final int CROP_IMAGE_DIALOG = 4;

	private ProgressBar pb;

	// Fields
	private EditText nameView;
	private EditText lastnameView;

	// TODO implement ability tu change primary email.
	// private EditText emailView;
	private EditText phone1View;
	private EditText phone1CodeView;
	private EditText phone2View;
	private EditText phone2CodeView;
	private EditText phone3View;
	private EditText phone3CodeView;

	// Birthdate
	private EditText birthdateView;
	private Button birthdateButton;
	private final int BIRTHDATE_DIALOG = 0;
	private int mYear = 1970;
	private int mMonth = 0;
	private int mDay = 1;

	// Sex
	private Spinner sexSpinner;
	private String[] sexArray;

	// Country
	private TextView countryView;

	// timezone
	private TextView timezoneView;
	private EditText cityView;
	private EditText streetView;
	private EditText zipView;

	// image
	private ImageView accountImage;
	private CheckBox removeImage;

	private final int DIALOG_ERROR = 5;
	private String errorStr;

	private ArrayList<StaticTimezones> countriesList = new ArrayList<StaticTimezones>();
	private ArrayList<StaticTimezones> filteredCountriesList = new ArrayList<StaticTimezones>();
	private CountriesAdapter countriesAdapter = null;
	private TimezonesAdapter timezonesAdapter = null;
	private int timezoneInUse = 0;
	private LinearLayout countrySpinnerBlock;
	private LinearLayout timezoneSpinnerBlock;
	private Spinner languageSpinner;
	private String[] languageArray;

	private EditText email1View;
	private EditText email2View;
	private EditText email3View;
	private EditText email4View;
	
	private TextView email1_verifiedView;
	private TextView email2_verifiedView;
	private TextView email3_verifiedView;
	private TextView email4_verifiedView;
	
	private ToggleButton notifyByEmail;
	private ToggleButton notifyByPush;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.setContentView(R.layout.account);
		final Account account = new Account(this);
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
			// TODO what have I done?!
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
			countriesAdapter = new CountriesAdapter(AccountActivity.this, R.layout.search_dialog_item, countriesList);
			timezonesAdapter = new TimezonesAdapter(AccountActivity.this, R.layout.search_dialog_item, countriesList);
		}

		email1View = (EditText) findViewById(R.id.email1View);
		email2View = (EditText) findViewById(R.id.email2View);
		email3View = (EditText) findViewById(R.id.email3View);
		email4View = (EditText) findViewById(R.id.email4View);
		
		email1_verifiedView = (TextView) findViewById(R.id.email1_unverified);
		email2_verifiedView = (TextView) findViewById(R.id.email2_unverified);
		email3_verifiedView = (TextView) findViewById(R.id.email3_unverified);
		email4_verifiedView = (TextView) findViewById(R.id.email4_unverified);
		
		languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
		ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this, R.array.language_labels,
				android.R.layout.simple_spinner_item);
		adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(adapterLanguage);
		languageArray = getResources().getStringArray(R.array.language_values);
		languageSpinner.setSelection(getMyLanguage(languageArray, account.getLanguage()));

		pb = (ProgressBar) findViewById(R.id.progress);

		nameView = (EditText) findViewById(R.id.nameView);
		lastnameView = (EditText) findViewById(R.id.lastnameView);

		// emailView = (EditText) findViewById(R.id.emailView);
		phone1View = (EditText) findViewById(R.id.phone1View);
		phone1CodeView = (EditText) findViewById(R.id.phonecode1View);
		phone2View = (EditText) findViewById(R.id.phone2View);
		phone2CodeView = (EditText) findViewById(R.id.phonecode2View);
		phone3View = (EditText) findViewById(R.id.phone3View);
		phone3CodeView = (EditText) findViewById(R.id.phonecode3View);

		// Birthdate
		birthdateView = (EditText) findViewById(R.id.birthdateView);
		birthdateButton = (Button) findViewById(R.id.birthdateButton);

		birthdateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(BIRTHDATE_DIALOG);
			}
		});

		// Sex
		sexSpinner = (Spinner) findViewById(R.id.sexSpinner);
		ArrayAdapter<CharSequence> adapterSex = ArrayAdapter.createFromResource(this, R.array.sex_labels,
				android.R.layout.simple_spinner_item);
		adapterSex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sexSpinner.setAdapter(adapterSex);
		sexArray = getResources().getStringArray(R.array.sex_values);

		cityView = (EditText) findViewById(R.id.cityView);
		streetView = (EditText) findViewById(R.id.streetView);
		zipView = (EditText) findViewById(R.id.zipView);

		// Image
		accountImage = (ImageView) findViewById(R.id.accountImage);
		accountImage.setOnClickListener(this);
		removeImage = (CheckBox) findViewById(R.id.removeImage);

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (TextView) findViewById(R.id.countryView);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(AccountActivity.this);
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

						timezonesAdapter = new TimezonesAdapter(AccountActivity.this, R.layout.search_dialog_item, filteredCountriesList);
						timezonesAdapter.notifyDataSetChanged();

						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						account.setTimezone(countriesList.get(timezoneInUse).timezone);
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
				final Dialog dia1 = new Dialog(AccountActivity.this);
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
						account.setCountry(countriesList.get(timezoneInUse).country_code);
						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						account.setTimezone(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		notifyByEmail = (ToggleButton) findViewById(R.id.notificationsByEmail);
		notifyByEmail.setChecked(true);
		
		notifyByPush = (ToggleButton) findViewById(R.id.notificationsByPush);
		notifyByPush.setChecked(true);
		
		
		notifyByEmail.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked && !notifyByPush.isChecked()) {
					notifyByPush.setChecked(true);
				}
			}
		});
		
		notifyByPush.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked && !notifyByEmail.isChecked()) {
					notifyByEmail.setChecked(true);
				}
			}
		});
		fillActivityFields(account);
	}

	// TODO code duplicate in Registration activity.
	private int getMyLanguage(String[] countryList, String myLanguage) {
		int countryPosition = 0;

		for (int iterator = 0; iterator < countryList.length; iterator++) {
			if (countryList[iterator].equalsIgnoreCase(myLanguage)) {
				countryPosition = iterator;
			}
		}
		return countryPosition;
	}

	private void fillActivityFields(Account account) {
		if (!account.getName().equals("null")) {
			nameView.setText(account.getName());
		} else {
			nameView.setText("");
		}

		if (!account.getLastname().equals("null")) {
			lastnameView.setText(account.getLastname());
		} else {
			lastnameView.setText("");
		}

		if (!account.getEmail1().equals("null"))
			email1View.setText(account.getEmail1());
		
		if (!account.getEmail1().equals("null") && (account.getEmail1().length() > 0) && !account.getEmail1_verified()) {
			email1_verifiedView.setVisibility(View.VISIBLE);
		}

		if (!account.getEmail2().equals("null"))
			email2View.setText(account.getEmail2());
		
		if (!account.getEmail2().equals("null") && (account.getEmail2().length() > 0) && !account.getEmail2_verified()) {
			email2_verifiedView.setVisibility(View.VISIBLE);
		}

		if (!account.getEmail3().equals("null"))
			email3View.setText(account.getEmail3());
		
		if (!account.getEmail3().equals("null") && (account.getEmail3().length() > 0) && !account.getEmail3_verified()) {
			email3_verifiedView.setVisibility(View.VISIBLE);
		}

		if (!account.getEmail4().equals("null"))
			email4View.setText(account.getEmail4());
		
		if (!account.getEmail4().equals("null") && (account.getEmail4().length() > 0) && !account.getEmail4_verified()) {
			email4_verifiedView.setVisibility(View.VISIBLE);
		}

		if (!account.getPhone1().equals("null")) {
			phone1View.setText(account.getPhone1());
		}

		if (!account.getPhone1_code().equals("null")) {
			phone1CodeView.setText(account.getPhone1_code());
		}

		if (!account.getPhone2().equals("null"))
			phone2View.setText(account.getPhone2());

		if (!account.getPhone2_code().equals("null")) {
			phone2CodeView.setText(account.getPhone2_code());
		}

		if (!account.getPhone3().equals("null"))
			phone3View.setText(account.getPhone3());

		if (!account.getPhone3_code().equals("null")) {
			phone3CodeView.setText(account.getPhone3_code());
		}

		if (account.getBirthdate() != null) {
//			final Calendar c = Utils.stringToCalendar(getApplicationContext(), account.getBirthdate().toString(), DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
			mYear = account.getBirthdate().get(Calendar.YEAR);
			mMonth = account.getBirthdate().get(Calendar.MONTH);
			mDay = account.getBirthdate().get(Calendar.DAY_OF_MONTH);
			
			updateBirthdate();
		}

		// sex
		if (!account.getSex().equals("null")) {
			int pos = Utils.getArrayIndex(sexArray, account.getSex());
			sexSpinner.setSelection(pos);
		}

		// country
		String country = account.getCountry(AccountActivity.this);
		if (country.length() > 0) {
			for (StaticTimezones entry : countriesList) {
				if (entry.country2.equalsIgnoreCase(country) || entry.country.equalsIgnoreCase(country)) {
					timezoneInUse = Integer.parseInt(entry.id);
				}
			}

			if (timezoneInUse > 0) {
				countryView.setText(countriesList.get(timezoneInUse).country2);
//				phone1CodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
//				phone2CodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
//				phone3CodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
			}
		}

		// timezone
		if (account.getTimezone().length() > 0) {
			for (StaticTimezones entry : countriesList) {
				if (entry.timezone.equalsIgnoreCase(account.getTimezone()))
					timezoneInUse = Integer.parseInt(entry.id);
			}
			// if (timezoneInUse > 0) {
			String countryCode = countriesList.get(timezoneInUse).country_code;

			filteredCountriesList = new ArrayList<StaticTimezones>();

			for (StaticTimezones tz : countriesList) {
				if (tz.country_code.equalsIgnoreCase(countryCode)) {
					filteredCountriesList.add(tz);
				}
			}

			timezonesAdapter = new TimezonesAdapter(AccountActivity.this, R.layout.search_dialog_item, filteredCountriesList);
			timezonesAdapter.notifyDataSetChanged();

			timezoneView.setText(countriesList.get(timezoneInUse).timezone);
			countryView.setText(countriesList.get(timezoneInUse).country2);
			// }
		}

		if (!account.getCity().equals("null"))
			cityView.setText(account.getCity());

		if (!account.getStreet().equals("null"))
			streetView.setText(account.getStreet());

		if (!account.getZip().equals("null"))
			zipView.setText(account.getZip());

		// image
		if (account.getImage() && account.image_bytes != null) {
			accountImage.setImageBitmap(BitmapFactory.decodeByteArray(account.image_bytes, 0, account.image_bytes.length));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.accountImage:
			showDialog(CROP_IMAGE_DIALOG);
			break;
		}
	}

	public void saveAccount(View view) {
		Account mAccount = new Account(this);
		String temp;
		String temp1; // TODO OPTIMIZE DAT!
		String temp2;

		// name, fullname
		String name = nameView.getText().toString();
		mAccount.setName(name);
		StringBuilder fullname = new StringBuilder(name).append(" ").append(lastnameView.getText().toString());
		mAccount.setFullname(fullname.toString());
		mAccount.setLastname(lastnameView.getText().toString());

		// Email
		// temp = emailView.getText().toString();
		// mAccount.setEmail(temp, 0);

		// Phones
		Debug.waitForDebugger(); // TODO remove debugger call
		temp1 = phone1View.getText().toString();
		if(!mAccount.getPhone1().contentEquals(temp1)){
			mAccount.setPhone(temp1, 1);
		}
		
		temp2 = phone1CodeView.getText().toString();
		if(!mAccount.getPhone1_code().contentEquals(temp2)) {
			mAccount.setPhone_code(temp2, 1);
		}
		
		if(!mAccount.getPhone1().contentEquals(temp1) || !mAccount.getPhone1_code().contentEquals(temp2)) {
			mAccount.setPhone_verified(false, 1);
		}

		temp1 = phone2View.getText().toString();
		if(!mAccount.getPhone2().contentEquals(temp1)){
			mAccount.setPhone(temp1, 2);
		}
		
		temp2 = phone2CodeView.getText().toString();
		if(!mAccount.getPhone2_code().contentEquals(temp2)) {
			mAccount.setPhone_code(temp2, 2);
		}
		
		if(!mAccount.getPhone2().contentEquals(temp1) || !mAccount.getPhone2_code().contentEquals(temp2)) {
			mAccount.setPhone_verified(false, 2);
		}

		temp1 = phone3View.getText().toString();
		if(!mAccount.getPhone3().contentEquals(temp1)){
			mAccount.setPhone(temp1, 3);
		}
		
		temp2 = phone3CodeView.getText().toString();
		if(!mAccount.getPhone3_code().contentEquals(temp2)) {
			mAccount.setPhone_code(temp2, 3);
		}
		
		if(!mAccount.getPhone3().contentEquals(temp1) || !mAccount.getPhone3_code().contentEquals(temp2)) {
			mAccount.setPhone_verified(false, 3);
		}

		// Date
		temp = birthdateView.getText().toString();
		Calendar birthdate = Utils.stringToCalendar(getApplicationContext(), temp, mAccount.getTimezone(), mAccount.getSetting_date_format());
		mAccount.setBirthdate(birthdate);

		// Sex
		int pos = (int) sexSpinner.getSelectedItemId();
		mAccount.setSex(sexArray[pos]);

		// Country
		mAccount.setCountry(countriesList.get(timezoneInUse).country_code);

		// Timezone
		mAccount.setTimezone(countriesList.get(timezoneInUse).timezone);

		// City, Street, Zip
		temp = cityView.getText().toString();
		mAccount.setCity(temp);

		temp = streetView.getText().toString();
		mAccount.setStreet(temp);

		temp = zipView.getText().toString();
		mAccount.setZip(temp);
		//language
		long selectedLanguage = languageSpinner.getSelectedItemId();
		String[] languagesArray = getResources().getStringArray(R.array.language_values);
		mAccount.setLanguage(languagesArray[(int) selectedLanguage]);
		Resources res = this.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration config = res.getConfiguration();
		config.locale = new Locale(LanguageCodeGetter.getLanguageCode(new Account(this).getLanguage()));
		res.updateConfiguration(config, dm);
		// Image
		boolean isRemoveImage = removeImage.isChecked();

		if (isRemoveImage) {
			mAccount.setImage(false);
			mAccount.setImage_url("");
			mAccount.setImage_thumb_url("");
			mAccount.image_bytes = null;
			mAccount.setRemove_image(1);
		} else {
			mAccount.setImage(true);
			mAccount.setRemove_image(0);
		}

		new EditAccountTask().execute();
		
		NavbarActivity.showVerificationDialog = true;
		
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		case BIRTHDATE_DIALOG:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		case CROP_IMAGE_DIALOG:
			final String[] items = new String[] { getString(R.string.take_from_camera), getString(R.string.select_from_gallery) };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
			builder.setTitle(getString(R.string.select_image));
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0) {
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_avatar_"
								+ String.valueOf(System.currentTimeMillis()) + ".jpg"));

						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

						try {
							intent.putExtra("return-data", true);

							startActivityForResult(intent, PICK_FROM_CAMERA);
						} catch (ActivityNotFoundException e) {
							Reporter.reportError(getApplicationContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName()
									.toString(), e.getMessage());
						}
					} else { // pick from file
						Intent intent = new Intent();

						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);

						startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action_using)), PICK_FROM_FILE);
					}
				}
			});
			return builder.create();
		default:
			break;
		}

		return super.onCreateDialog(id);
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateBirthdate();
		}
	};

	private void updateBirthdate() {
		mMonth++;
		birthdateView.setText(new StringBuilder().append(mYear).append("-").append(mMonth < 10 ? "0" + mMonth : mMonth).append("-").append(mDay < 10 ? "0" + mDay : mDay));
	}

	class EditAccountTask extends AsyncTask<Void, Boolean, Boolean> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... args) {
			return DataManagement.updateAccount(AccountActivity.this, removeImage.isChecked());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			pb.setVisibility(View.GONE);
			//finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Account account = new Account(this);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case PICK_FROM_CAMERA:
			doCrop();

			break;

		case PICK_FROM_FILE:
			mImageCaptureUri = data.getData();

			doCrop();

			break;

		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();

			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				accountImage.setImageBitmap(photo);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
				account.image_bytes = baos.toByteArray();
			}

			File f = new File(mImageCaptureUri.getPath());
			if (f.exists())
				f.delete();

			break;

		}
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, getString(R.string.can_not_find_image_crop_app), Toast.LENGTH_SHORT).show();

			return;
		} else {
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.choose_crop_app));
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
					}
				});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null, null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if(checkIfChangesMade()){
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(this.getResources().getString(R.string.save_your_changes))
				.setMessage(this.getResources().getString(R.string.do_you_want_to_save_your_changes))
				.setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveAccount(null);
						dialog.dismiss();
					}

				}).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
		                dialog.dismiss();
		                finish();
					}

				}).setCancelable(false).show();
		} else {
			super.onBackPressed();
		}
	}
	
	public boolean checkIfChangesMade(){
		boolean chagesMade = false;
		Account account = new Account(this);
		if(!account.getName().equals(nameView.getText().toString())){
			chagesMade = true;
		}
		if(!account.getLastname().equals(lastnameView.getText().toString())){
			chagesMade = true;
		}
		if(!account.getEmail().equals(email1View.getText().toString())){
			chagesMade = true;
		}
		//TODO email 2 3
		if(!account.getPhone1().equals("null") && !account.getPhone1().equals(phone1View.getText().toString())){
			chagesMade = true;
		}
		if(!account.getPhone2().equals("null") && !account.getPhone2().equals(phone2View.getText().toString())){
			chagesMade = true;
		}
		if(!account.getPhone3().equals("null") && !account.getPhone3().equals(phone3View.getText().toString())){
			chagesMade = true;
		}
		if(!birthdateView.getText().toString().equals("")){
			String[] birthdate = birthdateView.getText().toString().split("-");
			birthdateView.setText(birthdate[0] + "-" + Integer.valueOf(birthdate[1]) + "-" + Integer.valueOf(birthdate[2]));
		}
		if(!birthdateView.getText().toString().equals("")){
			Calendar tmp = Utils.stringToCalendar(getApplicationContext(), birthdateView.getText().toString(), account.getTimezone(), account.getSetting_date_format());
			if(account.getBirthdate().get(Calendar.YEAR) != tmp.get(Calendar.YEAR) && account.getBirthdate().get(Calendar.MONTH) != tmp.get(Calendar.MONTH) && account.getBirthdate().get(Calendar.DAY_OF_MONTH) != tmp.get(Calendar.DAY_OF_MONTH)){
				chagesMade = true;
			}
		}
		if(!account.getSex().equals("null") && !account.getSex().equals(sexArray[(int) sexSpinner.getSelectedItemId()].toString())){
			chagesMade = true;
		}
		if(!account.getCountry(AccountActivity.this).equals("null") && !account.getCountry(AccountActivity.this).equals(countriesList.get(timezoneInUse).country_code)){
			chagesMade = true;
		}
		if(!account.getTimezone().equals("null") && !account.getTimezone().equals(countriesList.get(timezoneInUse).timezone)){
			chagesMade = true;
		}
		if(!account.getCity().equals("null") && !account.getCity().equals(cityView.getText().toString())){
			chagesMade = true;
		}
		if(!account.getStreet().equals("null") && !account.getStreet().equals(streetView.getText().toString())){
			chagesMade = true;
		}
		if(!account.getZip().equals("null") && !account.getZip().equals(zipView.getText().toString())){
			chagesMade = true;
		}
		if(!account.getLanguage().equals("null") && !account.getLanguage().equals(getResources().getStringArray(R.array.language_values)[(int) languageSpinner.getSelectedItemId()])){
			chagesMade = true;
		}
		if(removeImage.isChecked()){
			chagesMade = true;
		}
		//TODO notifications implement
		return chagesMade;
	}
}
