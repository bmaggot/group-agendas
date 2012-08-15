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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;


public class ImportActivity extends Activity {

		
	private SharedPreferences prefs;
	TextView console;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_import);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        console = (TextView) findViewById(R.id.text_console);

        Button launchOauth = (Button) findViewById(R.id.contact_import_button_gauth);
        Button clearCredentials = (Button) findViewById(R.id.contact_import_button_clear_auth);
        Button getContacts = (Button) findViewById(R.id.contact_import_button_gimp);
        
        launchOauth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startActivity(new Intent().setClass(v.getContext(), RequestTokenActivity.class));
            	
            }
        });

        clearCredentials.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	clearCredentials();
        		Data.credentialsClear = true;
            	finish();
            }
        });
        
        getContacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Data.importStats = getContacts();
            	Data.returnedFromContactImport = true;
            	finish();
            }
        });
   
    }

	private int[] getContacts() {
		String jsonOutput = "";
		int[] importStats = new int[3]; 
		int importedContactAmount = 0;
		int unimportedContactAmount = 0;
		int totalEntries = 0;

		try {
        	jsonOutput = makeSecuredReq(C.GET_CONTACTS_FROM_GOOGLE_REQUEST,getConsumer(this.prefs));
         	JSONObject jsonResponse = new JSONObject(jsonOutput);
        	JSONObject m = (JSONObject)jsonResponse.get("feed");
        	JSONArray entries =(JSONArray)m.getJSONArray("entry");
        	
        	totalEntries = entries.length();
        	
        	for (int i=0 ; i < totalEntries; i++) {
        		Contact tempContact = new Contact();
        		JSONObject entry = entries.getJSONObject(i);
// TODO YEAH-BAT'! HARDCODE CATCH 'EM ALL, bleat'.         		
        		try {
            		JSONObject fullName = entry.getJSONObject("gd$name");
            		
            		try {
		        		if (fullName.getString("gd$givenName")!=null && fullName.getString("gd$givenName").length()>0) {
		        			tempContact.name = fullName.getJSONObject("gd$givenName").getString("$t");
		        		}
            		} catch (JSONException e) {
            			Log.e("Contact given name import", e.getMessage());
	        			tempContact.name = "";
            		}
            		
            		try {
		        		if (fullName.getString("gd$familyName")!=null && fullName.getString("gd$familyName").length()>0) {
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
	        		if (birthday.getString("when")!=null && birthday.getString("when").length()>0) {
	        			tempContact.birthdate = birthday.getString("when");
	        		}
        		} catch (JSONException e) {
        			Log.e("Contact birthday import", e.getMessage());
        			tempContact.birthdate = "";
        		}
        		
        		try {
        			JSONObject structuredPostalAddress = entry.getJSONArray("gd$structuredPostalAddress").getJSONObject(0);
        			
        			try {
		        		if ((structuredPostalAddress.getString("gd$country") != null) && (structuredPostalAddress.getString("gd$country").length() > 0)) {
		        			String jCountry = structuredPostalAddress.getJSONObject("gd$country").getString("$t");
		        			tempContact.country = jCountry;
		        		}
            		} catch (JSONException e) {
            			Log.e("Contact country import", e.getMessage());
	        			tempContact.country = "";
            		}

        			try {
		        		if ((structuredPostalAddress.getString("gd$city") != null) && (structuredPostalAddress.getString("gd$city").length() > 0)) {
		        			String jCity = structuredPostalAddress.getJSONObject("gd$city").getString("$t");
		        			tempContact.city = jCity;
		        		}
            		} catch (JSONException e) {
            			Log.e("Contact city import", e.getMessage());
	        			tempContact.city = "";
            		}
	        		
        			try {
		        		if ((structuredPostalAddress.getString("gd$street") != null) && (structuredPostalAddress.getString("gd$street").length() > 0)) {
		        			String jStreet = structuredPostalAddress.getJSONObject("gd$street").getString("$t");
		        			tempContact.street = jStreet;
		        		}
            		} catch (JSONException e) {
            			Log.e("Contact street import", e.getMessage());
	        			tempContact.street = "";
            		}

        			try {
		        		if ((structuredPostalAddress.getString("gd$postcode") != null) && (structuredPostalAddress.getString("gd$postcode").length() > 0)) {
		        			String postCode = structuredPostalAddress.getJSONObject("gd$postcode").getString("$t");
		        			tempContact.zip = postCode;
		        		}
            		} catch (JSONException e) {
            			Log.e("Contact postcode import", e.getMessage());
	        			tempContact.zip = "";
            		}
	    		} catch (JSONException e) {
    				Log.e("Contact structured postal address import", e.getMessage());
    				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
	    		}
        		
        		tempContact.visibility = "n";
        		
        		if (DataManagement.getInstance(this).createContact(tempContact)) {
        			importedContactAmount++;
        		} else {
        			unimportedContactAmount++;
        		}
        	}
        	importStats[0] = importedContactAmount;
        	importStats[1] = unimportedContactAmount;
        	importStats[2] = totalEntries;
		} catch (Exception e) {
			Log.e(C.TAG, "Error executing request",e);
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		return importStats;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isOAuthSuccessful()) {
//			OAuth successful, try getting the contacts
    		console.setText("OAuth successful, try getting the contacts");
    	} else {
    		console.setText("OAuth failed, no tokens, Click on the Do OAuth Button.");
    	}
	}
	
    private void clearCredentials() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.commit();
	}
    
    private boolean isOAuthSuccessful() {
    	String token = prefs.getString(OAuth.OAUTH_TOKEN, null);
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, null);
		if (token != null && secret != null)
			return true;
		else 
			return false;
    }

	
	private OAuthConsumer getConsumer(SharedPreferences prefs) {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(C.CONSUMER_KEY, C.CONSUMER_SECRET);
		consumer.setTokenWithSecret(token, secret);
		return consumer;
	}
	
	private String makeSecuredReq(String url,OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
    	HttpGet request = new HttpGet(url);
    	Log.i(C.TAG,"Requesting URL : " + url);
    	consumer.sign(request);
    	HttpResponse response = httpclient.execute(request);
    	Log.i(C.TAG,"Statusline : " + response.getStatusLine());
    	InputStream data = response.getEntity().getContent();
    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
        String responeLine;
        StringBuilder responseBuilder = new StringBuilder();
        while ((responeLine = bufferedReader.readLine()) != null) {
        	responseBuilder.append(responeLine);
            Log.i(C.TAG,"Response : " + responseBuilder.toString());
        }
        return responseBuilder.toString();
	}	
}