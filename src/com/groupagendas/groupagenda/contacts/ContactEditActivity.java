package com.groupagendas.groupagenda.contacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import android.graphics.Bitmap.CompressFormat;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.groupagendas.groupagenda.DataManagement;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.utils.CountryManager;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactEditActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	
	private String ERROR_STRING = "";
	private final int ERROR_DIALOG = 0;
	private final int CROP_IMAGE_DIALOG = 1;
	private final int CHOOSE_GROUPS_DIALOG = 2;
	private final int BIRTHDATE_DIALOG = 3;

	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	private DataManagement dm;

	private Contact editedContact;

	private Button sendButton;
	private Button groupsButton;
	private ImageView imageView;
	private CheckBox removeImage;

	private EditText nameView;
	private EditText lastnameView;
	private EditText emailView;
	private EditText phoneView;
	
	private EditText birthdateView;
	private Button birthdateButton;
	private Calendar birthdateCalendar = Calendar.getInstance();
	
	private Spinner countrySpinner;
	private String[] countryArray;
	
	private EditText cityView;
	private EditText streetView;
	private EditText zipView;
	private Spinner visibilitySpinner;

	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	private boolean ACTION_EDIT = true;

	private String[] visibilityArray;
	
	private DateTimeUtils dtUtils;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contact_edit);

		dm = DataManagement.getInstance(this);
		
		dtUtils = new DateTimeUtils(this);

		Intent intent = getIntent();

		groupsButton = (Button) findViewById(R.id.groupsButton);
		groupsButton.setOnClickListener(this);

		sendButton = (Button) findViewById(R.id.sendbutton);
		sendButton.setOnClickListener(this);

		imageView = (ImageView) findViewById(R.id.contact_image);
		imageView.setOnClickListener(this);

		removeImage = (CheckBox) findViewById(R.id.remove_image);
		
		countryArray = CountryManager.getCountryValues(this);
		countrySpinner = (Spinner) findViewById(R.id.country);
		countrySpinner.setOnItemSelectedListener(this);
		

		visibilityArray = getResources().getStringArray(R.array.visibility_labels);
		visibilitySpinner = (Spinner) findViewById(R.id.visibility);
		visibilitySpinner.setOnItemSelectedListener(this);
		
		
		// GET ACTION
		ACTION_EDIT = intent.getBooleanExtra("action", true);

		if (ACTION_EDIT) {
			new GetContactTask().execute(intent.getIntExtra("contact_id", 0));
		} else {
			LinearLayout ll = (LinearLayout) findViewById(R.id.remove_image_ll);
			ll.setVisibility(View.GONE);

			editedContact = new Contact();

			new GetGroupsTask().execute();
			
			ArrayAdapter<String> adapterCountry =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CountryManager.getCountries(this)) ;
			adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			countrySpinner.setAdapter(adapterCountry);
			
			ArrayAdapter<CharSequence> adapterVis = ArrayAdapter.createFromResource(ContactEditActivity.this, R.array.visibility_labels, android.R.layout.simple_spinner_item);
			adapterVis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			visibilitySpinner.setAdapter(adapterVis);
		}

		nameView = (EditText) findViewById(R.id.name);
		lastnameView = (EditText) findViewById(R.id.lastname);
		emailView = (EditText) findViewById(R.id.email);
		phoneView = (EditText) findViewById(R.id.phone);
		birthdateView = (EditText) findViewById(R.id.birthdate);
		birthdateButton = (Button) findViewById(R.id.birthdateButton);
		birthdateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(BIRTHDATE_DIALOG);
			}
		});

		cityView = (EditText) findViewById(R.id.city);
		streetView = (EditText) findViewById(R.id.street);
		zipView = (EditText) findViewById(R.id.zip);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sendbutton:
			if (ACTION_EDIT) {
				new EditContactTask().execute(editedContact);
			} else {
				new CreateContactTask().execute(editedContact);
			}
			break;
		case R.id.contact_image:
			showDialog(CROP_IMAGE_DIALOG);
			break;
		case R.id.groupsButton:
			showDialog(CHOOSE_GROUPS_DIALOG);
			break;
		}
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                birthdateCalendar.set(Calendar.YEAR, year);
                birthdateCalendar.set(Calendar.MONTH, monthOfYear);
                birthdateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                birthdateView.setText(dtUtils.formatDate(birthdateCalendar.getTime()));
            }
        };
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case BIRTHDATE_DIALOG:
			return new DatePickerDialog(this, mDateSetListener, birthdateCalendar.get(Calendar.YEAR), birthdateCalendar.get(Calendar.MONTH), birthdateCalendar.get(Calendar.DAY_OF_MONTH));
		case ERROR_DIALOG:
			builder.setMessage(ERROR_STRING).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			break;
		case CHOOSE_GROUPS_DIALOG:
			builder.setTitle(getString(R.string.choose_contacts)).setMultiChoiceItems(titles, selections, new DialogSelectionClickHandler())
					.setPositiveButton(getString(R.string.ok), new DialogButtonClickHandler());
			break;
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
			break;
		}
		return builder.create();
	}

	class GetContactTask extends AsyncTask<Integer, Contact, Contact> {

		protected Contact doInBackground(Integer... id) {
			editedContact = dm.getContact(id[0]);

			ArrayList<Group> groups = dm.getGroupsFromDb();
			getGroupsList(groups, false);

			return editedContact;
		}

		protected void onPostExecute(Contact result) {
			if (result.image) {
				Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(result.image_bytes, 0, result.image_bytes.length), 120, 120);
				imageView.setImageBitmap(bitmap);
			} else {
				imageView.setImageResource(R.drawable.group_icon);
			}

			nameView.setText(result.name);
			if(result.lastname != null && !result.lastname.equals("null"))	lastnameView.setText(result.lastname);
			if(result.email != null && !result.email.equals("null"))	emailView.setText(result.email);
			if (!result.phone1.equals("null"))	phoneView.setText(result.phone1);
			
			if (!result.birthdate.equals("null")){
				birthdateView.setText(dtUtils.formatDate(result.birthdate));
				birthdateCalendar = Utils.stringToCalendar(result.birthdate, DateTimeUtils.DEFAULT_DATE);
			}
			
			
			ArrayAdapter<String> adapterCountry =  new ArrayAdapter<String>(ContactEditActivity.this, android.R.layout.simple_spinner_item, CountryManager.getCountries(ContactEditActivity.this)) ;
			adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			countrySpinner.setAdapter(adapterCountry);
			
			int pos = Utils.getArrayIndex(countryArray, result.country);
			countrySpinner.setSelection(pos);

			if (!result.city.equals("null"))
				cityView.setText(result.city);
			if (!result.street.equals("null"))
				streetView.setText(result.street);
			if (!result.zip.equals("null"))
				zipView.setText(result.zip);
			
			ArrayAdapter<CharSequence> adapterVis = ArrayAdapter.createFromResource(ContactEditActivity.this, R.array.visibility_labels, android.R.layout.simple_spinner_item);
			adapterVis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			visibilitySpinner.setAdapter(adapterVis);
			if (!result.visibility.equals("null")) {
				pos = Utils.getArrayIndex(visibilityArray, result.visibility);
				visibilitySpinner.setSelection(pos);
			}

			super.onPostExecute(result);
		}

	}

	class EditContactTask extends AsyncTask<Contact, Boolean, Boolean> {

		protected Boolean doInBackground(Contact... contacts) {
			boolean success = false;
			boolean check = true;
			String temp = "";
			ContentValues cv = new ContentValues();
			
			// name
			temp = nameView.getText().toString();
			if(temp.length() <= 0){
				check = false;
				ERROR_STRING = getString(R.string.name_is_required);
			}
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, temp);
			editedContact.name = temp;
			
			temp = lastnameView.getText().toString();
			editedContact.lastname = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, temp);
			
			temp = emailView.getText().toString();
			editedContact.email = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, temp);
			
			temp = phoneView.getText().toString();
			editedContact.phone1 = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, temp);
			
			editedContact.birthdate = dtUtils.formatDateToDefault(birthdateCalendar.getTime());
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, temp);
			
			editedContact.country = countryArray[countrySpinner.getSelectedItemPosition()];
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, editedContact.country);
			
			temp = cityView.getText().toString();
			editedContact.city = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.CITY, temp);

			temp = streetView.getText().toString();
			editedContact.street = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.STREET, temp);
			
			temp = zipView.getText().toString();
			editedContact.zip = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.ZIP, temp);
			
			temp = visibilityArray[visibilitySpinner.getSelectedItemPosition()];
			editedContact.visibility = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, temp);
			
			// groups
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(editedContact.groups));
			
			// image
			editedContact.remove_image = removeImage.isChecked();
			if(removeImage.isChecked()){
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, false);
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL, "");
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL, "");
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, "");
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, removeImage.isChecked());
			}else{
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, editedContact.image_bytes);
			}
			
			if(check){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI+"/"+editedContact.contact_id);
				getContentResolver().update(uri, cv, null, null);
				success = dm.editContact(editedContact);
				
				if(!success){
					cv = new ContentValues();
					cv.put(ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE, 1);
					getContentResolver().update(uri, cv, null, null);
				}
			}
			
			return check;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				onBackPressed();
			} else {
				ERROR_STRING = dm.getError();
				showDialog(ERROR_DIALOG);
			}
			super.onPostExecute(result);
		}

	}
	class CreateContactTask extends AsyncTask<Contact, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(Contact... groups) {
			boolean success = false;
			boolean check = true;
			String temp = "";
			ContentValues cv = new ContentValues();
			
			// name
			temp = nameView.getText().toString();
			if(temp.length() <= 0){
				check = false;
				ERROR_STRING = getString(R.string.name_is_required);
			}
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, temp);
			editedContact.name = temp;
			
			temp = lastnameView.getText().toString();
			editedContact.lastname = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, temp);
			
			temp = emailView.getText().toString();
			editedContact.email = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, temp);
			
			temp = phoneView.getText().toString();
			editedContact.phone1 = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, temp);
			
			temp = birthdateView.getText().toString();
			editedContact.birthdate = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, temp);
			
			editedContact.country = countryArray[countrySpinner.getSelectedItemPosition()];
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, editedContact.country);
			
			temp = cityView.getText().toString();
			editedContact.city = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.CITY, temp);

			temp = streetView.getText().toString();
			editedContact.street = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.STREET, temp);
			
			temp = zipView.getText().toString();
			editedContact.zip = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.ZIP, temp);
			
			temp = visibilityArray[visibilitySpinner.getSelectedItemPosition()];
			editedContact.visibility = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, temp);
			
			// groups
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(editedContact.groups));
			
			if(editedContact.image_bytes != null){
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, editedContact.image_bytes);
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE , true);
			}
			
			if(check){
				success = dm.createContact(editedContact);
				if(!success){
					cv.put(ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE, 2);
				}
				getContentResolver().insert(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, cv);
			}
			
			return check;
		}
		
		protected void onPostExecute(Boolean result) {
			if (result) {
				onBackPressed();
			} else {
				ERROR_STRING = dm.getError();
				showDialog(ERROR_DIALOG);
			}
			super.onPostExecute(result);
		}
	}
	class GetGroupsTask extends AsyncTask<Void, Void, Void> {
		protected void onPreExecute() {
			groupsButton.setEnabled(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Group> groups = dm.getGroupsFromDb();
			getGroupsList(groups, true);
			return null;
		}

		protected void onPostExecute(Void result) {
			groupsButton.setEnabled(true);
		}
	}

	private void getGroupsList(ArrayList<Group> groups, boolean isFalse) {
		int l = groups.size();
		titles = new CharSequence[l];
		ids = new int[l];
		selections = new boolean[l];

		for (int i = 0; i < l; i++) {
			titles[i] = groups.get(i).title;
			ids[i] = groups.get(i).group_id;
			if (isFalse || editedContact.groups == null) {
				selections[i] = false;
			} else {
				selections[i] = editedContact.groups.containsValue(String.valueOf(groups.get(i).group_id));
			}
		}
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
				editedContact.groups = new HashMap<String, String>();

				for (int i = 0, l = ids.length; i < l; i++) {
					if (selections[i]) {
						editedContact.groups.put(String.valueOf(i), String.valueOf(ids[i]));
					}
				}
				break;
			}
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
				imageView.setImageBitmap(photo);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				photo.compress(CompressFormat.PNG, 100, bos);
				editedContact.image_bytes = bos.toByteArray();
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


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		switch (parent.getId()) {
		case R.id.country:
			editedContact.country = countryArray[pos];
			break;
		case R.id.visibility:
			editedContact.visibility = visibilityArray[pos];
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}
