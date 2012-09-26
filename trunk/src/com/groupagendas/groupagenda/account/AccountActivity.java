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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.timezone.TimezoneManager;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.SearchDialog;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.account);
		
		dm = DataManagement.getInstance(this);
		pb = (ProgressBar) findViewById(R.id.progress);

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
		final ArrayAdapter<String> adapterCountry =  new ArrayAdapter<String>(this,  R.layout.search_dialog_item, CountryManager.getCountries(this)) ;
		adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapterCountry);
		
		LinearLayout countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock); 
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Dialog dia = new SearchDialog(AccountActivity.this, R.style.yearview_eventlist_title, adapterCountry, countrySpinner);
				dia.show();				
			}
		});
		
		countryArray = CountryManager.getCountryValues(this);
		countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if(pos == 0){
					ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(AccountActivity.this,  R.layout.search_dialog_item, new String[0]) ;
					adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					timezoneSpinner.setAdapter(adapterTimezone);
					timezoneSpinner.setEnabled(false);
					timezoneArray = null;
				}else{
					timezoneSpinner.setEnabled(true);
					String[] timezoneLabels = TimezoneManager.getTimezones(AccountActivity.this, countryArray[pos]);
					ArrayAdapter<String> adapterTimezone =  new ArrayAdapter<String>(AccountActivity.this,  R.layout.search_dialog_item, timezoneLabels) ;
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
	
	@Override
	public void onResume() {
		super.onResume();

		Account account = new Account();
		
		fillActivityFields(account);
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

		if (!account.getPhone1().equals("null"))
			phone1View.setText(account.getPhone1());
		
		if (!account.getPhone2().equals("null"))
			phone2View.setText(account.getPhone2());
		
		if (!account.getPhone3().equals("null"))
			phone3View.setText(account.getPhone3());

		if (account.getBirthdate() != null) {
			final Calendar c = Utils.stringToCalendar(account.getBirthdate().toString(), DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);

			updateBirthdate();
		}
		
		// sex
		if (!account.getSex().equals("null")){
			int pos = Utils.getArrayIndex(sexArray, account.getSex());
			sexSpinner.setSelection(pos);
		}
		
		// country
		if (!account.getCountry().equals("null")){
			int pos = Utils.getArrayIndex(countryArray, account.getCountry());
			countrySpinner.setSelection(pos);
		}
		
		// timezone
		if (!account.getTimezone().equals("null") && timezoneArray != null){
			int pos = Utils.getArrayIndex(timezoneArray, account.getTimezone());
			timezoneSpinner.setSelection(pos);
		}
		
		if (!account.getCity().equals("null"))
			cityView.setText(account.getCity());
		
		if (!account.getStreet().equals("null"))
			streetView.setText(account.getStreet());
		
		if (!account.getZip().equals("null"))
			zipView.setText(account.getZip());
		
		// image
		if (account.getImage() && account.image_bytes != null){
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
		Account mAccount = new Account();
		String temp;
		
		// name, fullname
		String name = nameView.getText().toString();
		mAccount.setName(name);
		StringBuilder fullname = new StringBuilder(name).append(" ").append(lastnameView.getText().toString());
		mAccount.setFullname(fullname.toString());
		mAccount.setLastname(lastnameView.getText().toString());
		
		// Phones
		temp = phone1View.getText().toString();
		mAccount.setPhone(temp, 1);
		temp = phone2View.getText().toString();
		mAccount.setPhone(temp, 2);
		temp = phone3View.getText().toString();
		mAccount.setPhone(temp, 3);
		
		// Date
		temp = birthdateView.getText().toString();
		Calendar birthdate = Utils.createCalendar(Long.parseLong(temp), mAccount.getTimezone());
		mAccount.setBirthdate(birthdate);
		
		// Sex
		int pos = (int) sexSpinner.getSelectedItemId();
		mAccount.setSex(sexArray[pos]);
		
		// Country 
		pos = (int) countrySpinner.getSelectedItemId();
		mAccount.setCountry(countryArray[pos]);
		
		// Timezone
		pos = (int)timezoneSpinner.getSelectedItemId();
		mAccount.setTimezone(timezoneArray[pos]);
		
		// City, Street, Zip
		temp = cityView.getText().toString();
		mAccount.setCity(temp);
		
		temp = streetView.getText().toString();
		mAccount.setStreet(temp);
		
		temp = zipView.getText().toString();
		mAccount.setZip(temp);
		
		// Image
		boolean isRemoveImage = removeImage.isChecked();
		
		
		if(isRemoveImage){
			mAccount.setImage(false);
			mAccount.setImage_url("");
			mAccount.setImage_thumb_url("");
			mAccount.image_bytes = null;
			mAccount.setRemove_image(1);
		}else{
			mAccount.setImage(true);
			mAccount.setRemove_image(0);
		}
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
							Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
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
		birthdateView.setText(new StringBuilder().append(mYear).append("-").append(mMonth + 1).append("-").append(mDay));
	}

	class EditAccountTask extends AsyncTask<Void, Boolean, Boolean>{
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... args) {
			return dm.updateAccount(removeImage.isChecked());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			if(!result){
				// TODO sumthing
//				ContentValues values = new ContentValues();
//				values.put(AccountProvider.AMetaData.AccountMetaData.NEED_UPDATE, 1);
//				
//				StringBuilder where = new StringBuilder(AccountProvider.AMetaData.AccountMetaData.A_ID).append(" = ").append(dm.getAccount().getUser_id());
//				getContentResolver().update(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, values, where.toString(), null);
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
				dm.getAccount().image_bytes = baos.toByteArray();
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
}
