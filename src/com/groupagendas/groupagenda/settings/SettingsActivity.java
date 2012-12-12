package com.groupagendas.groupagenda.settings;

import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.android.c2dm.C2DMessaging;
import com.groupagendas.groupagenda.GroupAgendasActivity;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountActivity;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.https.WebService;

public class SettingsActivity extends ListActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.settings);
		
		String[] settings = new String[]{
				getString(R.string.my_account),
				getString(R.string.calendar_settings),
				getString(R.string.auto_color),
				getString(R.string.auto_icon),
				getString(R.string.more_emails),
				getString(R.string.whats_new),
				getString(R.string.provide_feedback),
				getString(R.string.write_review)
		};
		
		setListAdapter(new SettingsAdapter(this, settings));
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(NavbarActivity.showVerificationDialog){
			Account mAccount = new Account(this);
			
			if(!mAccount.getPhone1().contentEquals("") && !mAccount.getPhone1().contentEquals("null") && !mAccount.getPhone1_verified()){
				NavbarActivity.showDialogForPhoneVerification(SettingsActivity.this, mAccount.getPhone1(), "1");
			}
			if(!mAccount.getPhone2().contentEquals("") && !mAccount.getPhone2().contentEquals("null") && !mAccount.getPhone2_verified()){
				NavbarActivity.showDialogForPhoneVerification(SettingsActivity.this, mAccount.getPhone2(), "2");
			}
			if(!mAccount.getPhone3().contentEquals("") && !mAccount.getPhone3().contentEquals("null") && !mAccount.getPhone3_verified()){
				NavbarActivity.showDialogForPhoneVerification(SettingsActivity.this, mAccount.getPhone3(), "3");
			}
			NavbarActivity.showVerificationDialog = false;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch(position){
		case 0:
			startActivity(new Intent(SettingsActivity.this, AccountActivity.class));
		break;
		case 1:
			startActivity(new Intent(SettingsActivity.this, CalendarSettingsActivity.class));
		break;
		case 2:
			startActivity(new Intent(SettingsActivity.this, AutoColorActivity.class));
		break;
		case 3:
			startActivity(new Intent(SettingsActivity.this, AutoIconActivity.class));
		break;
		case 4:
			startActivity(new Intent(SettingsActivity.this, MoreEmailsActivity.class));
			break;
		case 5:
			startActivity(new Intent(SettingsActivity.this, WhatsnewActivity.class));
			break;
		case 6:
			sendEmail();
		break;
		case 7:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
		break;
		}
	}
	
	private void sendEmail(){
		Intent i = new Intent(Intent.ACTION_SENDTO);
		i.setType("plain/text");
		i.setData(Uri.parse("mailto:feedback@groupagendas.com"));
		startActivity(Intent.createChooser(i, getString(R.string.send_mail)));
		
	}
	
	public void logout(View v){
		new UnsubscribeFromPush().execute();
		SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);
		Editor prefEditor = prefs.edit();
		
		if (prefs.getBoolean("stay_logged_in", false))
			prefEditor.putBoolean("stay_logged_in", false);
		prefEditor.putBoolean("logged", false);
		
		prefEditor.commit();
		
		Intent intent = new Intent(SettingsActivity.this, GroupAgendasActivity.class);
		intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
		startActivity(intent);
	}
	
	private class UnsubscribeFromPush extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				C2DMessaging.unregister(SettingsActivity.this);
				WebService webService = new WebService();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/push/unsubscribe");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				Account account = new Account(SettingsActivity.this);
				reqEntity.addPart("device_uuid", new StringBody(account.getPushId(), Charset.forName("UTF-8")));

				post.setEntity(reqEntity);

				HttpResponse rp = webService.getResponseFromHttpPost(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						object.getInt("");
					}
				}
			} catch (Exception ex) {
				Reporter.reportError(getApplicationContext(), DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
			}
			return null;
		}
		
	}
	
}
