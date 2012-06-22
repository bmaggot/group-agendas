package com.groupagendas.groupagenda.account;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.londatiga.android.CropOption;
import net.londatiga.android.CropOptionAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.groupagendas.groupagenda.DataManagement;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.Utils;

public class AccountActivity extends Activity implements OnClickListener{
	
	// image
	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private final int CROP_IMAGE_DIALOG = 4;
	
	private DataManagement dm;
	private ProgressBar pb;
	
	private Account mAccount;
	
	// Fields
	private EditText nameView;
	private EditText lastnameView;

	private EditText phone1View;
	private EditText phone2View;
	private EditText phone3View;
	
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
	private Spinner countrySpinner;
	private String[] countryArray;
	
	// timezone
	private Spinner timezoneSpinner;
	private String[] timezoneArray;
	
	private EditText cityView;
	private EditText streetView;
	private EditText zipView;
	
	// image
	private ImageView accountImage;
	private CheckBox removeImage;
	
	private final int DIALOG_ERROR   = 5;
	private String errorStr;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.account);
		
		dm = DataManagement.getInstance(this);
		pb = (ProgressBar) findViewById(R.id.progress);

		new GetAccountFromDBTask().execute();

		nameView = (EditText) findViewById(R.id.nameView);
		lastnameView = (EditText) findViewById(R.id.lastnameView);

		phone1View = (EditText) findViewById(R.id.phone1View);
		phone2View = (EditText) findViewById(R.id.phone2View);
		phone3View = (EditText) findViewById(R.id.phone3View);
		
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
		ArrayAdapter<CharSequence> adapterSex = ArrayAdapter.createFromResource(this, R.array.sex_labels, android.R.layout.simple_spinner_item);
		adapterSex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sexSpinner.setAdapter(adapterSex);
		sexArray = getResources().getStringArray(R.array.sex_values);
		
		// Timezone
		timezoneSpinner = (Spinner) findViewById(R.id.timezoneSpinner);

		// Country
		countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
		ArrayAdapter<String> adapterCountry =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CountryManager.getCountries(this)) ;
		adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapterCountry);
		countryArray = CountryManager.getCountryValues(this);
		countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if(pos == 0){
					ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(AccountActivity.this, android.R.layout.simple_spinner_item, new String[0]) ;
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);
					timezoneSpinner.setEnabled(false);
					timezoneArray = null;
				}else{
					timezoneSpinner.setEnabled(true);
					String[] timezoneLabels = TimezoneManager.getTimezones(AccountActivity.this, countryArray[pos]);
					ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(AccountActivity.this, android.R.layout.simple_spinner_item, timezoneLabels) ;
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);
					
					timezoneArray = TimezoneManager.getTimezonesValues(AccountActivity.this, countryArray[pos]);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		cityView = (EditText) findViewById(R.id.cityView);
		streetView = (EditText) findViewById(R.id.streetView);
		zipView = (EditText) findViewById(R.id.zipView);
		
		// Image
		accountImage = (ImageView) findViewById(R.id.accountImage);
		accountImage.setOnClickListener(this);
		removeImage = (CheckBox) findViewById(R.id.removeImage);
		
	}

	private void feelFields(Account account) {
		mAccount = account;
		nameView.setText(account.name);
		lastnameView.setText(account.fullname.replace(account.name + " ", ""));

		if (account.phone1 != null && !account.phone1.equals("null"))
			phone1View.setText(account.phone1);
		if (account.phone2 != null && !account.phone2.equals("null"))
			phone2View.setText(account.phone2);
		if (account.phone3 != null && !account.phone3.equals("null"))
			phone3View.setText(account.phone3);

		if (account.birthdate != null && !account.birthdate.equals("null")) {
			final Calendar c = Utils.stringToCalendar(account.birthdate, "yyyy-MM-dd");
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);

			updateBirthdate();
		}
		
		// sex
		if (account.sex != null && !account.sex.equals("null")){
			int pos = Utils.getArrayIndex(sexArray, account.sex);
			sexSpinner.setSelection(pos);
		}
		
		// country
		if (account.country != null && !account.country.equals("null")){
			int pos = Utils.getArrayIndex(countryArray, account.country);
			countrySpinner.setSelection(pos);
		}
		
		// timezone
		if (account.timezone != null && !account.timezone.equals("null") && timezoneArray != null){
			int pos = Utils.getArrayIndex(timezoneArray, account.timezone);
			timezoneSpinner.setSelection(pos);
		}
		
		if (account.city != null && !account.city.equals("null"))
			cityView.setText(account.city);
		if (account.street != null && !account.street.equals("null"))
			streetView.setText(account.street);
		if (account.zip != null && !account.zip.equals("null"))
			zipView.setText(account.zip);
		
		// image
		if (account.image){
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
	
	public void saveAccount(View view){
		String temp;
		ContentValues values = new ContentValues();
		
		values.put(AccountProvider.AMetaData.AccountMetaData.A_ID, mAccount.user_id);
		
		// name, fullname
		String name = nameView.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.NAME, name);
		mAccount.name = name;
		StringBuilder fullname = new StringBuilder(name).append(" ").append(lastnameView.getText().toString());
		values.put(AccountProvider.AMetaData.AccountMetaData.FULLNAME, fullname.toString());
		mAccount.fullname = fullname.toString();
		
		// Phones
		temp = phone1View.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.PHONE1, temp);
		mAccount.phone1 = temp;
		temp = phone2View.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.PHONE2, temp);
		mAccount.phone2 = temp;
		temp = phone3View.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.PHONE3, temp);
		mAccount.phone3 = temp;
		
		// Date
		temp = birthdateView.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.BIRTHDATE, temp);
		mAccount.birthdate = temp;
		
		// Sex
		int pos = (int)sexSpinner.getSelectedItemId();
		values.put(AccountProvider.AMetaData.AccountMetaData.SEX, sexArray[pos]);
		mAccount.sex = sexArray[pos];
		
		// Country 
		pos = (int)countrySpinner.getSelectedItemId();
		values.put(AccountProvider.AMetaData.AccountMetaData.COUNTRY, countryArray[pos]);
		mAccount.country = countryArray[pos];
		
		// Timezone
		pos = (int)timezoneSpinner.getSelectedItemId();
		values.put(AccountProvider.AMetaData.AccountMetaData.TIMEZONE, timezoneArray[pos]);
		mAccount.timezone = timezoneArray[pos];
		
		// City, Street, Zip
		temp = cityView.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.CITY, temp);
		mAccount.city = temp;
		
		temp = streetView.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.STREET, temp);
		mAccount.street = temp;
		
		temp = zipView.getText().toString();
		values.put(AccountProvider.AMetaData.AccountMetaData.ZIP, temp);
		mAccount.zip = temp;
		
		// Image
		boolean isRemoveImage = removeImage.isChecked();
		
		
		if(isRemoveImage){
			values.put(AccountProvider.AMetaData.AccountMetaData.IMAGE, false);
			values.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_URL, "");
			values.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_THUMB_URL, "");
			values.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_BYTES, "");
			values.put(AccountProvider.AMetaData.AccountMetaData.REMOVE_IMAGE, 1);
		}else{
			values.put(AccountProvider.AMetaData.AccountMetaData.IMAGE, true);
			values.put(AccountProvider.AMetaData.AccountMetaData.IMAGE_BYTES, mAccount.image_bytes);
			values.put(AccountProvider.AMetaData.AccountMetaData.REMOVE_IMAGE, 0);
		}
		
		StringBuilder where = new StringBuilder(AccountProvider.AMetaData.AccountMetaData.A_ID).append(" = ").append(mAccount.user_id);
		getContentResolver().update(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, values, where.toString(), null);
		
		new EditAccountTask().execute();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_ERROR:
			builder.setMessage(errorStr)
			   .setTitle(getString(R.string.error))
		       .setCancelable(false)
		       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
							e.printStackTrace();
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

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateBirthdate();
		}
	};

	private void updateBirthdate() {
		birthdateView.setText(new StringBuilder().append(mYear).append("-").append(mMonth + 1).append("-").append(mDay));
	}
	
	class GetAccountFromDBTask extends AsyncTask<Void, Account, Account> {

		protected void onPreExecute() {

			pb.setVisibility(View.VISIBLE);

			super.onPreExecute();
		}

		protected Account doInBackground(Void... args) {
			return dm.getAccountFromDb();
		}

		protected void onPostExecute(Account account) {
			if (account != null)
				feelFields(account);
			new GetAccountTask().execute();
			super.onPostExecute(account);
		}

	}

	class GetAccountTask extends AsyncTask<Void, Account, Account> {

		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		protected Account doInBackground(Void... args) {
			return dm.getAccountInfo();
		}

		protected void onPostExecute(Account account) {
			if (account != null) {
				feelFields(account);
			} else {
				// TODO show error
			}

			pb.setVisibility(View.GONE);
			super.onPostExecute(account);
		}

	}
	
	class EditAccountTask extends AsyncTask<Void, Boolean, Boolean>{
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		protected Boolean doInBackground(Void... args) {
			return dm.updateAccount(mAccount, removeImage.isChecked());
		}

		protected void onPostExecute(Boolean result) {
			
			if(!result){
				ContentValues values = new ContentValues();
				values.put(AccountProvider.AMetaData.AccountMetaData.NEED_UPDATE, 1);
				
				StringBuilder where = new StringBuilder(AccountProvider.AMetaData.AccountMetaData.A_ID).append(" = ").append(mAccount.user_id);
				getContentResolver().update(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, values, where.toString(), null);
			}
			
			finish();
			pb.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
				mAccount.image_bytes = baos.toByteArray();
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
}
