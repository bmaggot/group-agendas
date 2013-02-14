package com.groupagendas.groupagenda.contacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;
import com.groupagendas.groupagenda.utils.TimezoneUtils;
import com.groupagendas.groupagenda.utils.TimezoneUtils.StaticTimezone;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactEditActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	
	private class GenericTextWatcher implements TextWatcher{

		private String oldText = null;
		
	    @Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	    	oldText = charSequence.toString();
	    }
	    @Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
	    
	    @Override
		public void afterTextChanged(Editable editable) {
	    	if(!editable.toString().equalsIgnoreCase(oldText)){
	    		changesMade = true;
//	    		saveButton.setEnabled(changesMade);
	    		}
	        }
	}
	private TextWatcher watcher;

	private String ERROR_STRING = "";
	private final int ERROR_DIALOG = 0;
	private final int CROP_IMAGE_DIALOG = 1;
	private final int CHOOSE_GROUPS_DIALOG = 2;
	private final int BIRTHDATE_DIALOG = 3;

	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private static final int LEAST_YEAR_VALUE = 1900;

	private Contact editedContact;

	private Button groupsButton;
	private ImageView imageView;
	// private CheckBox removeImage;

	private TextView titleView;
	private EditText nameView;
	private EditText lastnameView;
	private EditText emailView;
	private EditText phoneView;
	private EditText phonecodeView;

	private TextView birthdateView;
	private LinearLayout birthdateButton;
	private Calendar birthdateCalendar;

	private TextView countryView;

	private EditText cityView;
	private EditText streetView;
	private EditText zipView;
	private Spinner visibilitySpinner;
	private Spinner canAddNotesSpinner;

	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	private boolean ACTION_EDIT = true;
	private boolean DATA_LOADED = false;

	private String[] visibilityValues;
	private String[] canAddNotesValues;

	private DateTimeUtils dtUtils;
	private CountriesAdapter countriesAdapter;
	private LinearLayout countrySpinnerBlock;
	private int timezoneInUse;
	private View pb;
	private Account account;

	public static ArrayList<Group> selectedGroups;

	private ToggleButton notifyContactToggle;
	
	public static boolean changesMade = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contact_edit);
		pb = findViewById(R.id.progress);
		phonecodeView = (EditText) findViewById(R.id.phone_code);
		countryView = (TextView) findViewById(R.id.countryView);

		account = new Account(this);

		String tmz = account.getTimezone();
		final List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(this);
		for (StaticTimezone item : countriesList) {
			if (item.timezone.equalsIgnoreCase(tmz)) {
				timezoneInUse = Integer.parseInt(item.id);
				countryView.setText(countriesList.get(timezoneInUse).country);
				phonecodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
				// TODO: what?
				continue;
			}
		}

		dtUtils = new DateTimeUtils(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (watcher == null) {
			watcher = new GenericTextWatcher();
		}

		// setContentView(R.layout.contact_edit);
		Intent intent = getIntent();

		if (editedContact == null) {
			editedContact = new Contact();
		}

		// GET ACTION
		ACTION_EDIT = intent.getBooleanExtra("action", true);
		if (ACTION_EDIT && !DATA_LOADED) {
			try {
				editedContact = new GetContactTask().execute(intent.getIntExtra("contact_id", 0)).get();
			} catch (InterruptedException e) {
				Log.e("onResume()", "Failed getting contact's data." + e.getMessage());
			} catch (ExecutionException e) {
				Log.e("onResume()", "Failed getting contact's data. " + e.getMessage());
			}
		} else {
			// LinearLayout ll = (LinearLayout)
			// findViewById(R.id.remove_image_ll);
			// ll.setVisibility(View.GONE);
		}

		nameView = (EditText) findViewById(R.id.name);
		
		pb = findViewById(R.id.progress);
		lastnameView = (EditText) findViewById(R.id.lastname);
		
		titleView = (TextView) findViewById(R.id.title);
		emailView = (EditText) findViewById(R.id.email);
		
		phoneView = (EditText) findViewById(R.id.phone);
		
		phonecodeView = (EditText) findViewById(R.id.phone_code);
		
		visibilityValues = getResources().getStringArray(R.array.visibility_values);
		visibilitySpinner = (Spinner) findViewById(R.id.visibility);
//		visibilitySpinner.setOnItemSelectedListener(this);
		canAddNotesValues = getResources().getStringArray(R.array.yes_no_values);
		canAddNotesSpinner = (Spinner) findViewById(R.id.can_add_note_response);
		canAddNotesSpinner.setSelection(1);
//		canAddNotesSpinner.setOnItemSelectedListener(this);
		groupsButton = (Button) findViewById(R.id.groupsButton);
		imageView = (ImageView) findViewById(R.id.contact_image);

		birthdateView = (TextView) findViewById(R.id.birthdate);
		
		birthdateButton = (LinearLayout) findViewById(R.id.birthdateLayout);
		birthdateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(BIRTHDATE_DIALOG);
			}
		});

		cityView = (EditText) findViewById(R.id.city);
		
		streetView = (EditText) findViewById(R.id.street);
		
		zipView = (EditText) findViewById(R.id.zip);
		
		final List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(this);
		countriesAdapter = new CountriesAdapter(ContactEditActivity.this, R.layout.search_dialog_item, countriesList);

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countryView = (TextView) findViewById(R.id.countryView);
		
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(ContactEditActivity.this, R.style.yearview_eventlist);
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
						phonecodeView.setText("+" + countriesList.get(timezoneInUse).call_code);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		displaySelectedGroups();

		notifyContactToggle = (ToggleButton) findViewById(R.id.notifyContactToggleButton);
		notifyContactToggle.setChecked(true);
		notifyContactToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changesMade = true;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sendbutton:
			if (ACTION_EDIT) {
				try {
					if (new EditContactTask().execute(editedContact).get())
						finish();
				} catch (InterruptedException e) {
					Log.e("edit contact", "failed executing edit contact" + e.getMessage());
				} catch (ExecutionException e) {
					Log.e("edit contact", "failed executing edit contact" + e.getMessage());
				}
			} else {
				try {
					if (new CreateContactTask().execute(editedContact).get())
						finish();
				} catch (InterruptedException e) {
					Log.e("create contact", "failed executing create contact" + e.getMessage());
				} catch (ExecutionException e) {
					Log.e("create contact", "failed executing create contact" + e.getMessage());
				}
			}
			break;
		case R.id.contact_image:
			// showDialog(CROP_IMAGE_DIALOG);
			break;
		case R.id.groupsButton:
			Intent i = new Intent(ContactEditActivity.this, ContactsActivity.class);
			i.putExtra(ContactsActivity.TASK_MODE_KEY, ContactsActivity.TASK_MODE_SELECTION);
			i.putExtra(ContactsActivity.LIST_MODE_KEY, ContactsActivity.LIST_MODE_GROUPS);
			i.putExtra(ContactsActivity.DESTINATION_KEY, ContactsActivity.DEST_CONTACT_EDIT);
			Data.showSaveButtonInContactsForm = true;
			// TODO Data.eventForSavingNewInvitedPersons = event;
			startActivity(i);
			break;
		}
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			birthdateCalendar.set(Calendar.YEAR, year);
			birthdateCalendar.set(Calendar.MONTH, monthOfYear);
			birthdateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			birthdateView.setText(dtUtils.formatDate(birthdateCalendar));
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case BIRTHDATE_DIALOG:
			if ((editedContact.birthdate != null) && (!editedContact.birthdate.equalsIgnoreCase(""))
					&& (!editedContact.birthdate.equalsIgnoreCase("null"))) {
				birthdateCalendar = Utils
						.stringToCalendar(this, editedContact.birthdate, DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
			} else {
				birthdateCalendar = Calendar.getInstance();
			}

			if (birthdateCalendar.get(Calendar.YEAR) < LEAST_YEAR_VALUE) {
				birthdateCalendar.set(Calendar.YEAR, LEAST_YEAR_VALUE);
			}

			return new DatePickerDialog(this, mDateSetListener, birthdateCalendar.get(Calendar.YEAR),
					birthdateCalendar.get(Calendar.MONTH), birthdateCalendar.get(Calendar.DAY_OF_MONTH));
		case ERROR_DIALOG:
			builder.setMessage(ERROR_STRING).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			break;
		case CHOOSE_GROUPS_DIALOG:
			builder.setTitle(getString(R.string.choose_contacts))
					.setMultiChoiceItems(titles, selections, new DialogSelectionClickHandler())
					.setPositiveButton(getString(R.string.ok), new DialogButtonClickHandler());
			break;
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
								+ StringValueUtils.valueOf(System.currentTimeMillis()) + ".jpg"));

						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

						try {
							intent.putExtra("return-data", true);

							startActivityForResult(intent, PICK_FROM_CAMERA);
						} catch (ActivityNotFoundException e) {
							Reporter.reportError(getApplicationContext(), this.getClass().toString(), Thread.currentThread()
									.getStackTrace()[2].getMethodName().toString(), e.getMessage());
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

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected Contact doInBackground(Integer... id) {
			editedContact = ContactManagement.getContactFromLocalDb(ContactEditActivity.this, id[0], 0);

			ArrayList<Group> groups = ContactManagement.getGroupsFromLocalDb(ContactEditActivity.this, null);
			getGroupsList(groups, false);

			return editedContact;
		}

		@Override
		protected void onPostExecute(Contact result) {
			if (result.image && (result.image_bytes != null)) {
				Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(result.image_bytes, 0, result.image_bytes.length),
						120, 120);
				if (bitmap != null)
					imageView.setImageBitmap(bitmap);
			} else {
				imageView.setImageResource(R.drawable.group_icon);
			}

			nameView.setText(result.name);
			titleView.setText(result.name);
			if (result.lastname != null && !result.lastname.equals("null")) {
				lastnameView.setText(result.lastname);
			}
			if (result.email != null && !result.email.equals("null"))
				emailView.setText(result.email);
			if ((result.phone1 != null) && !result.phone1.equals("null") && (result.phone1.length() > 0))
				phoneView.setText(result.phone1);
			if ((result.phone1_code != null) && !result.phone1_code.equals("null") && (result.phone1_code.length() > 0))
				phonecodeView.setText(result.phone1_code);

			if (!result.birthdate.equals("")) {
				birthdateCalendar = Utils.stringToCalendar(getApplicationContext(), result.birthdate,
						DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
				if (birthdateCalendar.get(Calendar.YEAR) < LEAST_YEAR_VALUE) {
					birthdateCalendar.set(Calendar.YEAR, LEAST_YEAR_VALUE);
				}
				birthdateView.setText(dtUtils.formatDate(birthdateCalendar));
			}

			if (result.country.length() > 0) {
				List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(getApplicationContext());
				for (StaticTimezone entry : countriesList) {
					if (entry.country_code.equalsIgnoreCase(result.country))
						timezoneInUse = Integer.parseInt(entry.id);
				}

				countryView.setText(countriesList.get(timezoneInUse).country);
			}

			if (!result.city.equals("null"))
				cityView.setText(result.city);
			if (!result.street.equals("null"))
				streetView.setText(result.street);
			if (!result.zip.equals("null"))
				zipView.setText(result.zip);

			if (!result.visibility.equals("null")) {
				if (result.visibility.equalsIgnoreCase("n"))
					visibilitySpinner.setSelection(0, true);
				else if (result.visibility.equalsIgnoreCase("f"))
					visibilitySpinner.setSelection(1, true);
				else if (result.visibility.equalsIgnoreCase("l"))
					visibilitySpinner.setSelection(2, true);
			}
			visibilitySpinner.setOnItemSelectedListener(ContactEditActivity.this);
			if (result.can_add_note != null && !result.can_add_note.equals("null") && result.can_add_note.equalsIgnoreCase("y")) {
				canAddNotesSpinner.setSelection(0, true);
			} else {
				canAddNotesSpinner.setSelection(1, true);
			}
			canAddNotesSpinner.setOnItemSelectedListener(ContactEditActivity.this);
			DATA_LOADED = true;

			pb.setVisibility(View.GONE);
			setWatchersToAllFields();
			super.onPostExecute(result);
		}

	}

	class EditContactTask extends AsyncTask<Contact, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			TextView saveButton = (TextView) findViewById(R.id.sendbutton);

			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Contact... contacts) {
			boolean check = true;
			String temp = "";
			ContentValues cv = new ContentValues();

			// name
			temp = nameView.getText().toString();
			if (temp.length() <= 0) {
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

			temp = phonecodeView.getText().toString();
			editedContact.phone1_code = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE, temp);

			if (birthdateCalendar != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT, Locale.getDefault());
				editedContact.birthdate = sdf.format(birthdateCalendar.getTime());
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, editedContact.birthdate);
			} else {
				editedContact.birthdate = "";
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, editedContact.birthdate);
			}

			// this should be safe...
			List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(null);
			editedContact.country = countriesList.get(timezoneInUse).country_code;
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

			temp = visibilityValues[visibilitySpinner.getSelectedItemPosition()];
			editedContact.visibility = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, editedContact.visibility);

			temp = canAddNotesValues[canAddNotesSpinner.getSelectedItemPosition()];
			editedContact.can_add_note = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.CAN_ADD_NOTE, editedContact.can_add_note);
			// groups
			Map<String, String> contactGroupsMap = new HashMap<String, String>();
			if (editedContact.groups == null) {
				contactGroupsMap = new HashMap<String, String>();
			} else {
				if (editedContact.groups.isEmpty()) {
					contactGroupsMap = new HashMap<String, String>();
				}
				contactGroupsMap = editedContact.groups;
			}
			if (selectedGroups != null) {
				int i = 0;
				Map<String, String> selectedGroupsMap = new HashMap<String, String>();
				editedContact.groups = selectedGroupsMap;
				if (contactGroupsMap != null && contactGroupsMap.isEmpty()) {
					for (String s : contactGroupsMap.keySet()) {
						if (!s.contentEquals("")) {
							Group g = ContactManagement.getGroupFromLocalDb(getApplicationContext(),
									Integer.valueOf(contactGroupsMap.get(s)), 0);
							if (g != null) {
								ContactManagement.updateGroupOnLocalDb(getApplicationContext(), g, editedContact.contact_id, false);
								ContactManagement.editGroupOnRemoteDb(getApplicationContext(), g, editedContact.contact_id, false);
							}
						}
					}
				}
				if (contactGroupsMap != null && !contactGroupsMap.isEmpty()) {
					for (String s : contactGroupsMap.keySet()) {
						if (!s.contentEquals("")) {
							Group g = ContactManagement.getGroupFromLocalDb(getApplicationContext(),
									Integer.valueOf(contactGroupsMap.get(s)), 0);
							if (g != null) {
								ContactManagement.updateGroupOnLocalDb(getApplicationContext(), g, editedContact.contact_id, false);
								ContactManagement.editGroupOnRemoteDb(getApplicationContext(), g, editedContact.contact_id, false);
							}
						}
					}
				}
				if (!selectedGroups.isEmpty()) {
					for (Group g : selectedGroups) {
						editedContact.groups.put(StringValueUtils.valueOf(i), StringValueUtils.valueOf(g.group_id));
						ContactManagement.updateGroupOnLocalDb(getApplicationContext(), g, editedContact.contact_id, true);
						ContactManagement.editGroupOnRemoteDb(getApplicationContext(), g, editedContact.contact_id, true);
						i++;
					}
				}

				selectedGroups = null;
			}

			cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(getApplicationContext(), editedContact.groups));

			// image
			// editedContact.remove_image = removeImage.isChecked();
			// if (removeImage.isChecked()) {
			// cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, false);
			// cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL,
			// "");
			// cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL,
			// "");
			// cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES,
			// "");
			// cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE,
			// removeImage.isChecked());
			// } else {
			// cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES,
			// editedContact.image_bytes);
			// }

			if (check) {
				Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI + "/" + editedContact.contact_id);
				getContentResolver().update(uri, cv, null, null);
				editedContact.modified = Calendar.getInstance().getTimeInMillis();

				check = ContactManagement.editContactOnRemoteDb(getApplicationContext(), editedContact);

				if(check){
					editedContact.setUploadedToServer(true);
				} else {
					editedContact.setUploadedToServer(false);
				}
				ContactManagement.updateContactOnLocalDb(ContactEditActivity.this, editedContact);
				if (!editedContact.birthdate.contentEquals("")) {
					ContactManagement.updateBirthdayOnLocalDb(ContactEditActivity.this, editedContact);
				}
			}

			return check;
		}

		@Override
		protected void onPostExecute(Boolean results) {
			// if (results) {
			pb.setVisibility(View.GONE);
			ContactEditActivity.this.finish();
			// } else {
			// ERROR_STRING = DataManagement.getError();
			// showDialog(ERROR_DIALOG);
			// }
			super.onPostExecute(results);
		}

	}

	class CreateContactTask extends AsyncTask<Contact, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			TextView saveButton = (TextView) findViewById(R.id.sendbutton);

			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Contact... groups) {
			boolean check = true;
			String temp = "";
			ContentValues cv = new ContentValues();

			// name
			temp = nameView.getText().toString();
			if (temp.length() <= 0) {
				check = false;
				ERROR_STRING = getString(R.string.name_is_required);
			}
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, temp);
			editedContact.name = temp;

			temp = lastnameView.getText().toString();
			editedContact.lastname = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, temp);

			editedContact.contact_id = ((int) Calendar.getInstance().getTimeInMillis()) * -1;

			temp = emailView.getText().toString();
			editedContact.email = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, temp);

			temp = phoneView.getText().toString();
			editedContact.phone1 = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, temp);

			temp = phonecodeView.getText().toString();
			editedContact.phone1_code = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE, temp);

			if (birthdateCalendar != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT, Locale.getDefault());
				editedContact.birthdate = sdf.format(birthdateCalendar.getTime());
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, editedContact.birthdate);
			} else {
				editedContact.birthdate = "";
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, editedContact.birthdate);
			}
			// this should be safe...
			List<StaticTimezone> countriesList = TimezoneUtils.getTimezones(null);
			editedContact.country = countriesList.get(timezoneInUse).country_code;
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

			temp = visibilityValues[visibilitySpinner.getSelectedItemPosition()];
			editedContact.visibility = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, editedContact.visibility);

			temp = canAddNotesValues[canAddNotesSpinner.getSelectedItemPosition()];
			editedContact.can_add_note = temp;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.CAN_ADD_NOTE, editedContact.can_add_note);

			// created modified times
			editedContact.created = Calendar.getInstance().getTimeInMillis();
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.CREATED, editedContact.created);
			editedContact.modified = editedContact.created;
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED, editedContact.modified);

			// groups
			if (selectedGroups != null) {
				editedContact.groups = new HashMap<String, String>();
				for (int i = 0; i < selectedGroups.size(); i++) {
					Group g = selectedGroups.get(i);
					editedContact.groups.put(StringValueUtils.valueOf(i), StringValueUtils.valueOf(g.group_id));
					// ContactManagement.updateGroupOnLocalDb(getApplicationContext(),
					// g, editedContact.contact_id);
				}
			}
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(getApplicationContext(), editedContact.groups));

			if (editedContact.image_bytes != null) {
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, editedContact.image_bytes);
				cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, true);
			}

			if (check) {
				check = ContactManagement.insertContact(ContactEditActivity.this, editedContact, notifyContactToggle.isChecked());
			}
			DataManagement.synchronizeWithServer(getApplicationContext(), null, new Account(getApplicationContext()).getLatestUpdateUnixTimestamp());

			return check;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pb.setVisibility(View.GONE);
			Intent intent = new Intent(C2DMReceiver.REFRESH_CONTACT_LIST);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
			if (result) {
				onBackPressed();
			} else {
				TextView saveButton = (TextView) findViewById(R.id.sendbutton);
				saveButton.setText(getString(R.string.save));
				showDialog(ERROR_DIALOG);
			}
			super.onPostExecute(result);
		}
	}

	class GetGroupsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			groupsButton.setEnabled(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Group> groups = ContactManagement.getGroupsFromLocalDb(ContactEditActivity.this, null);
			getGroupsList(groups, true);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pb.setVisibility(View.GONE);
			groupsButton.setEnabled(true);
		}
	}

	private void getGroupsList(ArrayList<Group> groups, boolean isFalse) {
		int l = groups.size();
		titles = new CharSequence[l];
		ids = new int[l];
		selections = new boolean[l];

		for (int i = 0; i < l; i++) {
			Group g = groups.get(i);
			titles[i] = g.title;
			ids[i] = g.group_id;
			if (isFalse || editedContact.groups == null) {
				selections[i] = false;
			} else {
				selections[i] = editedContact.groups.containsValue(StringValueUtils.valueOf(g.group_id));
			}
		}
	}

	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		@Override
		public void onClick(DialogInterface dialog, int clicked, boolean selected) {
			selections[clicked] = selected;
		}
	}

	private class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int clicked) {
			switch (clicked) {
			case DialogInterface.BUTTON_POSITIVE:
				editedContact.groups = new HashMap<String, String>();
				if (ids != null) {
					for (int i = 0, l = ids.length; i < l; i++) {
						if (selections[i]) {
							editedContact.groups.put(StringValueUtils.valueOf(i), StringValueUtils.valueOf(ids[i]));
						}
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
				final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>(list.size());
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
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		changesMade = true;
		switch (parent.getId()) {
		case R.id.visibility:
			editedContact.visibility = visibilityValues[pos];
			break;
		case R.id.can_add_note_response:
			editedContact.can_add_note = canAddNotesValues[pos];
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	private void displaySelectedGroups() {
		LayoutInflater mInflater = LayoutInflater.from(ContactEditActivity.this);
		LinearLayout groupsList = (LinearLayout) findViewById(R.id.groupsList);

		groupsButton = (Button) findViewById(R.id.groupsButton);

		if (groupsList.getChildCount() > 0) {
			groupsList.removeAllViews();
		}

		if (selectedGroups != null) {
			int groupAmount = selectedGroups.size();

			if (groupAmount > 0) {
				groupsButton.setBackgroundResource(R.drawable.contact_edit_invitegroup_button_notalone);

				for (int iterator = 0; iterator < groupAmount; iterator++) {
					String groupTitle = selectedGroups.get(iterator).title;
					TextView entry = (TextView) mInflater.inflate(R.layout.contact_edit_invited_entry, null);
					entry.setText(groupTitle);

					if (iterator == (groupAmount - 1)) {
						entry.setBackgroundResource(R.drawable.contact_edit_invitegroup_entry_last_background);
					}

					groupsList.addView(entry);
				}
			}
		} else {
			groupsButton.setBackgroundResource(R.drawable.event_icon_placeholder);
		}
	}
	
	@Override
	public void onBackPressed() {
		if(changesMade){
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(this.getResources().getString(R.string.save_your_changes))
				.setMessage(this.getResources().getString(R.string.do_you_want_to_save_your_changes))
				.setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(ACTION_EDIT){
							try {
								if (new EditContactTask().execute(editedContact).get())
									finish();
							} catch (InterruptedException e) {
								Log.e("edit contact", "failed executing edit contact" + e.getMessage());
							} catch (ExecutionException e) {
								Log.e("edit contact", "failed executing edit contact" + e.getMessage());
							}
						} else {
							try {
								if (new CreateContactTask().execute(editedContact).get())
									finish();
							} catch (InterruptedException e) {
								Log.e("create contact", "failed executing create contact" + e.getMessage());
							} catch (ExecutionException e) {
								Log.e("create contact", "failed executing create contact" + e.getMessage());
							}
						}
						dialog.dismiss();
					}

				}).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedGroups = null;
		                dialog.dismiss();
		                finish();
					}

				}).setCancelable(false).show();
		} else {
			selectedGroups = null;
			super.onBackPressed();
		}
	}
	
	public void setWatchersToAllFields(){
		nameView.addTextChangedListener(watcher);
		lastnameView.addTextChangedListener(watcher);
		emailView.addTextChangedListener(watcher);
		phoneView.addTextChangedListener(watcher);
		phonecodeView.addTextChangedListener(watcher);
		birthdateView.addTextChangedListener(watcher);
		cityView.addTextChangedListener(watcher);
		streetView.addTextChangedListener(watcher);
		zipView.addTextChangedListener(watcher);
		countryView.addTextChangedListener(watcher);
	}
	
}
