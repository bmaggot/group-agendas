package com.groupagendas.groupagenda.contacts.importer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ImportActivity extends Activity {
	private DataManagement dm = DataManagement.getInstance(this);

	TextView console;
	GmailDialog gimp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_import);

		console = (TextView) findViewById(R.id.text_console);
        console.setText(R.string.contact_import_dialog_welcome_message);

		LinearLayout phoneImportButton = (LinearLayout) findViewById(R.id.contact_import_button_phone);
		LinearLayout gmailImportButton = (LinearLayout) findViewById(R.id.contact_import_button_gimp);
		
		gimp = new GmailDialog(ImportActivity.this, R.style.yearview_eventlist);


		phoneImportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					new PhoneImport().execute(ImportActivity.this).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		});

		gmailImportButton.setOnClickListener(new View.OnClickListener() {
			/**
			 * This method is used for launching retrieving contacts from Gmail account
			 * method and updating "console's" status (console.setText(..)). After import
			 * statistics are retrieved and set in public variable in Data class. Afterwards
			 * import activity gets closed. 
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
	    if (resultCode==1) {
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
			
		} else if (Data.returnedFromContactImport){
			console.setText(R.string.contact_import_dialog_finished_gimp);
			finish();
		}
		
		super.onResume();
	}
}