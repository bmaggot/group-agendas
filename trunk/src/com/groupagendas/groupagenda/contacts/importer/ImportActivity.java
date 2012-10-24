package com.groupagendas.groupagenda.contacts.importer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;

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
				new PhoneImport().execute();
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

	public class PhoneImport extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			pd.setMessage(getResources().getString(R.string.import_phone_contacts));
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			PhoneContactImport.importPhoneContacts(ImportActivity.this);
//			SimContactImport.ImportSimContacts(ImportActivity.this);
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			pd.dismiss();
			Toast.makeText(ImportActivity.this, getResources().getString(R.string.import_phone_contacts_done), Toast.LENGTH_LONG);
		}

		
	}

	@Override
	public void onBackPressed() {
		if (!disableBackButton) {
			super.onBackPressed();
		}
	}
}