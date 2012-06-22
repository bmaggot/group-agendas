package com.groupagendas.groupagenda.settings;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.groupagendas.groupagenda.GroupAgendasActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.AccountActivity;
import com.groupagendas.groupagenda.utils.Prefs;

public class SettingsActivity extends ListActivity{
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
		Prefs prefs = new Prefs(this);
		prefs.setLogged(false);
		prefs.save();
		Intent intent = new Intent(SettingsActivity.this, GroupAgendasActivity.class);
		intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
		startActivity(intent);
	}
	
}
