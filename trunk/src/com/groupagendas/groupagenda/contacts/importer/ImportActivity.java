package com.groupagendas.groupagenda.contacts.importer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;

public class ImportActivity extends Activity {

	TextView console;
	GmailDialog gimp;
	private boolean disableBackButton = false;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_import);
		pd = new ProgressDialog(ImportActivity.this);

		console = (TextView) findViewById(R.id.text_console);
		console.setText(R.string.contact_import_dialog_welcome_message);

		LinearLayout phoneImportButton = (LinearLayout) findViewById(R.id.contact_import_button_phone);
		LinearLayout gmailImportButton = (LinearLayout) findViewById(R.id.contact_import_button_gimp);

		gimp = new GmailDialog(ImportActivity.this, R.style.yearview_eventlist);

		phoneImportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				disableBackButton = true;
				new PhoneImport().execute(ImportActivity.this);
				disableBackButton = false;
			}
		});

		gmailImportButton.setOnClickListener(new View.OnClickListener() {
			/**
			 * This method is used for launching retrieving contacts from Gmail
			 * account method and updating "console's" status
			 * (console.setText(..)). After import statistics are retrieved and
			 * set in public variable in Data class. Afterwards import activity
			 * gets closed.
			 **/
			@Override
			public void onClick(View v) {
				gimp.show();
			}
		});

	}

	/**
	 * 
	 **/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			finish();
		}
	}

	@Override
	protected void onResume() {
		if (Data.returnedFromContactAuth) {
			Data.returnedFromContactAuth = false;

			if (GmailDialog.isOAuthSuccessful()) {
				console.setText("Authentication was successful, try getting the contacts");
				gimp.show();
			} else {
				console.setText("You aren't authenticated. Click on the authentication button.");
				gimp.show();
			}

		} else if (Data.returnedFromContactImport) {
			console.setText(R.string.contact_import_dialog_finished_gimp);
			finish();
		}

		super.onResume();
	}

	public class PhoneImport extends AsyncTask<Activity, Void, Void> {

		@Override
		protected void onPreExecute() {
			pd.setMessage(getResources().getString(R.string.import_phone_contacts));
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Void doInBackground(Activity... params) {
			// import sim contacts
			Activity activity = params[0];
			Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
			while (phones.moveToNext()) {
				String displayName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				String[] nameArray;
				String firstName = "";
				String lastName = "";
				String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				Contact phoneContact = new Contact();
				if (displayName.matches("[0-9,A-Z,a-z]*\\s[0-9,A-Z,a-z]*")) {
					nameArray = displayName.split("\\s");
					firstName = nameArray[0];
					if (nameArray.length <= 2) {
						lastName = nameArray[1];
					} else if (nameArray.length > 2) {
						for (int i = 1; i < nameArray.length; i++) {
							lastName = lastName + nameArray[i];
						}
					}
				} else {
					firstName = displayName;
					lastName = "";
				}

				phoneContact.name = firstName;
				phoneContact.lastname = lastName;
				phoneContact.phone1 = phoneNumber;
				DataManagement.getInstance(activity).createContact(phoneContact);
			}
			phones.close();
			// import phone contacts
			Cursor phoneContacts = activity.getContentResolver()
					.query(RawContacts.CONTENT_URI,
							new String[] { RawContacts._ID, RawContacts.ACCOUNT_TYPE, ContactsContract.Contacts.DISPLAY_NAME,
									ContactsContract.CommonDataKinds.Email._ID },
							RawContacts.ACCOUNT_TYPE + " <> 'com.android.contacts.sim' " + " AND " + RawContacts.ACCOUNT_TYPE
									+ " == 'com.google' ", null, null);
			while (phoneContacts.moveToNext()) {
				String displayName = phoneContacts.getString(phoneContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String rawContactId = phoneContacts.getString(phoneContacts.getColumnIndex(RawContacts._ID));
				String[] nameArray;
				String firstName = "";
				String lastName = "";
				Contact phoneContact = new Contact();
				if (displayName.matches("[0-9,A-Z,a-z]*\\s[0-9,A-Z,a-z]*")) {
					nameArray = displayName.split("\\s");
					firstName = nameArray[0];
					if (nameArray.length <= 2) {
						lastName = nameArray[1];
					} else if (nameArray.length > 2) {
						for (int i = 1; i < nameArray.length; i++) {
							lastName = lastName + nameArray[i];
						}
					}
				} else {
					firstName = displayName;
					lastName = "";
				}
				phoneContact.name = firstName;
				if (!displayName.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})")) {

				}
				phoneContact.email = getContactEmail(rawContactId, (ImportActivity) activity, phoneContact);
				phoneContact.phone1 = getContactPhoneNumber(rawContactId, (ImportActivity) activity, phoneContact);
				phoneContact.lastname = lastName;
				DataManagement.getInstance(activity).createContact(phoneContact);
			}
			phoneContacts.close();
//			TODO update contacts list in local DB
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			pd.dismiss();
			Toast.makeText(ImportActivity.this, getResources().getString(R.string.import_phone_contacts_done), Toast.LENGTH_LONG);
		}

		public String getContactEmail(String id, ImportActivity activity, Contact contact) {
			String email = "";
			Cursor emailCur = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID + " = ?", new String[] { id }, null);
			while (emailCur.moveToNext()) {
				email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			}
			emailCur.close();
			return email;
		}

		public String getContactPhoneNumber(String id, ImportActivity activity, Contact contact) {
			String phone = "";
			Cursor pCur = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = ?", new String[] { id }, null);
			while (pCur.moveToNext()) {
				phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
			}
			pCur.close();
			return phone;
		}
	}

	@Override
	public void onBackPressed() {
		if (!disableBackButton) {
			super.onBackPressed();
		}
	}
}