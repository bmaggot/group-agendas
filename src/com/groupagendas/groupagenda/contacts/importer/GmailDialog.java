package com.groupagendas.groupagenda.contacts.importer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.error.report.Reporter;

public class GmailDialog extends Dialog {
	private Activity context;
	private Button launchOauth;
	private Button clearCredentials;
	private Button getContacts;
	private static SharedPreferences prefs;
	
	public GmailDialog(final Activity context, int styleResId) {
		super(context, styleResId);

		this.context = context;
		GmailDialog.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.setContentView(R.layout.contacts_import_gmail);
		this.setTitle(R.string.contact_import_dialog_gmail_title);

		launchOauth = (Button) findViewById(R.id.contact_import_button_gauth);
		clearCredentials = (Button) findViewById(R.id.contact_import_button_clear_auth);
		getContacts = (Button) findViewById(R.id.contact_import_button_gimp_launch);

		launchOauth.setOnClickListener(new View.OnClickListener() {
			/**
			 * This method is used for launching helper class
			 * RequestTokenActivity's threads in phone browser's window. If
			 * process was finished properly user's credentials should be saved
			 * into shared preferences and user should be able to use
			 * makeSecuredReqTask.
			 **/
			@Override
			public void onClick(View v) {
				Toast.makeText(GmailDialog.this.context, R.string.contact_import_dialog_launching_auth, Toast.LENGTH_SHORT);
				Data.returnedFromContactAuth = true;
				GmailDialog.this.context.startActivityForResult(new Intent().setClass(v.getContext(), RequestTokenActivity.class), 1);
			}
		});

		clearCredentials.setOnClickListener(new View.OnClickListener() {
			/**
			 * This method is used for clearing user's credentials. It's called
			 * manually. After process is finished public trigger is set tu True
			 * and import activity gets closed.
			 **/
			@Override
			public void onClick(View v) {
				clearCredentials();
				Data.credentialsClear = true;
				dismiss();
			}
		});

		getContacts.setOnClickListener(new View.OnClickListener() {
			/**
			 * This method is used for launching retrieving contacts from Gmail
			 * account method and updating "console's" status
			 * (console.setText(..)). After import statistics are retrieved and
			 * set in public variable in Data class. Afterwards import activity
			 * gets closed.
			 **/
			@Override
			public void onClick(View v) {
				Toast.makeText(GmailDialog.this.context, R.string.contact_import_dialog_starting_gimp, Toast.LENGTH_LONG);
				Data.importStats = getContacts();
				Toast.makeText(GmailDialog.this.context, R.string.contact_import_dialog_finished_gimp, Toast.LENGTH_SHORT);
				Data.returnedFromContactImport = true;
				dismiss();
				context.finish();
			}
		});
	}

	// TODO DOCUMENTATION ON CLEAR CREDENTIALS
	private void clearCredentials() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.commit();
	}

	// TODO DOCUMENTATION ON ISOAUTHSUCCESSFUL
	public static boolean isOAuthSuccessful() {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, null);
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, null);
		if (token != null && secret != null)
			return true;
		else
			return false;
	}

	// TODO DOCUMENTATION ON GET CONTACTS
	private int[] getContacts() {
		String jsonOutput = "";
		int[] importStats = new int[3];
		int importedContactAmount = 0;
		int unimportedContactAmount = 0;
		int totalEntries = 0;

		try {
			Object[] params = { C.GET_CONTACTS_FROM_GOOGLE_REQUEST, getConsumer(GmailDialog.prefs) };
			jsonOutput = new makeSecuredReqTask().execute(params).get();
			JSONObject jsonResponse = new JSONObject(jsonOutput);
			JSONObject m = (JSONObject) jsonResponse.get("feed");
			JSONArray entries = m.getJSONArray("entry");

			totalEntries = entries.length();

			for (int i = 0; i < totalEntries; i++) {
				Contact tempContact = new Contact();
				JSONObject entry = entries.getJSONObject(i);

				try {
					JSONObject fullName = entry.getJSONObject("gd$name");

					try {
						if (fullName.getString("gd$givenName") != null && fullName.getString("gd$givenName").length() > 0) {
							tempContact.name = fullName.getJSONObject("gd$givenName").getString("$t");
						}
					} catch (JSONException e) {
						Log.e("Contact given name import", e.getMessage());
						tempContact.name = "";
					}

					try {
						if (fullName.getString("gd$familyName") != null && fullName.getString("gd$familyName").length() > 0) {
							tempContact.lastname = fullName.getJSONObject("gd$familyName").getString("$t");
						}
					} catch (JSONException e) {
						Log.e("Contact last name import", e.getMessage());
						tempContact.lastname = "";
					}
				} catch (JSONException e) {
					Log.e("Contact full name import", e.getMessage());
				}

				try {
					JSONObject jEmail = entry.getJSONArray("gd$email").getJSONObject(0);
					if ((jEmail.getString("address") != null) && (jEmail.getString("address").length() > 0)) {
						tempContact.email = jEmail.getString("address");
					}
				} catch (JSONException e) {
					Log.e("Contact email import", e.getMessage());
					tempContact.email = "";
				}

				try {
					JSONObject phoneNo = entry.getJSONArray("gd$phoneNumber").getJSONObject(0);
					if ((phoneNo.getString("$t") != null) && (phoneNo.getString("$t").length() > 0)) {
						tempContact.phone1 = phoneNo.getString("$t");
					}
				} catch (JSONException e) {
					Log.e("Contact phone import", e.getMessage());
					tempContact.phone1 = "";
				}

				try {
					JSONObject birthday = entry.getJSONObject("gContact$birthday");
					if (birthday.getString("when") != null && birthday.getString("when").length() > 0) {
						tempContact.birthdate = birthday.getString("when");
					}
				} catch (JSONException e) {
					Log.e("Contact birthday import", e.getMessage());
					tempContact.birthdate = "";
				}

				try {
					JSONObject structuredPostalAddress = entry.getJSONArray("gd$structuredPostalAddress").getJSONObject(0);

					try {
						if ((structuredPostalAddress.getString("gd$country") != null)
								&& (structuredPostalAddress.getString("gd$country").length() > 0)) {
							String jCountry = structuredPostalAddress.getJSONObject("gd$country").getString("$t");
							tempContact.country = jCountry;
						}
					} catch (JSONException e) {
						Log.e("Contact country import", e.getMessage());
						tempContact.country = "";
					}

					try {
						if ((structuredPostalAddress.getString("gd$city") != null)
								&& (structuredPostalAddress.getString("gd$city").length() > 0)) {
							String jCity = structuredPostalAddress.getJSONObject("gd$city").getString("$t");
							tempContact.city = jCity;
						}
					} catch (JSONException e) {
						Log.e("Contact city import", e.getMessage());
						tempContact.city = "";
					}

					try {
						if ((structuredPostalAddress.getString("gd$street") != null)
								&& (structuredPostalAddress.getString("gd$street").length() > 0)) {
							String jStreet = structuredPostalAddress.getJSONObject("gd$street").getString("$t");
							tempContact.street = jStreet;
						}
					} catch (JSONException e) {
						Log.e("Contact street import", e.getMessage());
						tempContact.street = "";
					}

					try {
						if ((structuredPostalAddress.getString("gd$postcode") != null)
								&& (structuredPostalAddress.getString("gd$postcode").length() > 0)) {
							String postCode = structuredPostalAddress.getJSONObject("gd$postcode").getString("$t");
							tempContact.zip = postCode;
						}
					} catch (JSONException e) {
						Log.e("Contact postcode import", e.getMessage());
						tempContact.zip = "";
					}
				} catch (JSONException e) {
					Log.e("Contact structured postal address import", e.getMessage());
					Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e.getMessage());
				}

				tempContact.visibility = "n";

				Object[] submitParams = { this, tempContact };
				if (new createContactTask().execute(submitParams).get()) {
					importedContactAmount++;
				} else {
					unimportedContactAmount++;
				}
			}
			importStats[0] = importedContactAmount;
			importStats[1] = unimportedContactAmount;
			importStats[2] = totalEntries;
		} catch (Exception e) {
			Log.e(C.TAG, "Error executing request", e);
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e.getMessage());
		}
		return importStats;
	}

	private OAuthConsumer getConsumer(SharedPreferences prefs) {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(C.CONSUMER_KEY, C.CONSUMER_SECRET);
		consumer.setTokenWithSecret(token, secret);
		return consumer;
	}

	private String makeSecuredReq(String url, OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		Log.i(C.TAG, "Requesting URL : " + url);
		consumer.sign(request);
		HttpResponse response = httpclient.execute(request);
		Log.i(C.TAG, "Statusline : " + response.getStatusLine());
		InputStream data = response.getEntity().getContent();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
		String responeLine;
		StringBuilder responseBuilder = new StringBuilder();
		while ((responeLine = bufferedReader.readLine()) != null) {
			responseBuilder.append(responeLine);
			Log.i(C.TAG, "Response : " + responseBuilder.toString());
		}
		return responseBuilder.toString();
	}

	private class makeSecuredReqTask extends AsyncTask<Object, Void, String> {
		String url;
		OAuthConsumer consumer;

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(Object... objects) {
			String result = "";

			this.url = objects[0].toString();
			this.consumer = (OAuthConsumer) objects[1];

			try {
				result = makeSecuredReq(url, consumer);
			} catch (Exception e) {
				e.printStackTrace(); // TODO Auto-generated catch block
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
		}
	}

	/**
	 * This is a private class for embedding networking process which stores imported contact
	 * in remote database. 
	 * 
	 * @author meska.lt@gmail.com
	 */
	private class createContactTask extends AsyncTask<Object, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Object... params) {
			return ContactManagement.insertContact((Contact) params[1]);
		}
	}
	
	// TODO Documentation pending.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (Data.returnedFromContactAuth) {
				clearCredentials();
				Data.credentialsClear = true;
				dismiss();
			} else if (Data.returnedFromContactImport) {
				Data.returnedFromContactImport = true;
				clearCredentials();
				Data.credentialsClear = true;
				dismiss();
			} else {
				dismiss();
			}
		}
		return true;
	}
	
}
