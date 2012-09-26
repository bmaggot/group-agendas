package com.groupagendas.groupagenda.settings;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;

public class CalendarSettingsActivity extends Activity {
	private Button saveButton;

	private ToggleButton am_pmToggle;

	private Spinner defaultviewSpinner;
	private String[] defaultviewArray;

	private Spinner dateformatSpinner;
	private String[] dateformatArray;

	private DataManagement dm;
	private ProgressBar pb;

	private Prefs prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_calendar);

		prefs = new Prefs(this);

		dm = DataManagement.getInstance(this);
		pb = (ProgressBar) findViewById(R.id.progress);

		new GetAccountFromDBTask().execute();

		am_pmToggle = (ToggleButton) findViewById(R.id.am_pm);
		am_pmToggle.setChecked(CalendarSettings.isUsing_AM_PM());

		defaultviewSpinner = (Spinner) findViewById(R.id.defaultviewSpinner);
		ArrayAdapter<CharSequence> adapterDefaultview = ArrayAdapter.createFromResource(this, R.array.agenda_views_labels,
				android.R.layout.simple_spinner_item);
		adapterDefaultview.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultviewSpinner.setAdapter(adapterDefaultview);
		defaultviewArray = getResources().getStringArray(R.array.agenda_views_values);
		String dw = CalendarSettings.getDefaultView();
		for (int i = 0; i < defaultviewArray.length; i++) {
			if (dw.equalsIgnoreCase(defaultviewArray[i])) {
				defaultviewSpinner.setSelection(i);
				break;
			}
		}

		dateformatSpinner = (Spinner) findViewById(R.id.dateformatSpinner);
		ArrayAdapter<CharSequence> adapterDateformat = ArrayAdapter.createFromResource(this, R.array.date_format_values,
				android.R.layout.simple_spinner_item);
		adapterDateformat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dateformatSpinner.setAdapter(adapterDateformat);
		dateformatArray = getResources().getStringArray(R.array.date_format_values);
		String df = CalendarSettings.getDateFormat();
		for (int i = 0; i < dateformatArray.length; i++) {
			if (df.equalsIgnoreCase(dateformatArray[i])) {
				dateformatSpinner.setSelection(i);
				break;
			}
		}
		saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new SaveTask().execute();
			}
		});
	}

	class SaveTask extends AsyncTask<Void, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean success = true;
			
			Account account = new Account();

			int am_pm = am_pmToggle.isChecked() ? 1 : 0;
			String am_pmStr = am_pmToggle.isChecked() ? "true" : "false";
			String dateformat = dateformatArray[dateformatSpinner.getSelectedItemPosition()];
			String defaultview = defaultviewArray[defaultviewSpinner.getSelectedItemPosition()];
			

			ContentValues values = new ContentValues();
			values.put(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM, am_pm);
			prefs.setValue(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM, am_pmStr);
			values.put(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT, dateformat);
			prefs.setValue(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT, dateformat);
			values.put(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, defaultview);
			prefs.setValue(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, defaultview);
			
			prefs.save();

			success = dm.changeCalendarSettings(am_pm, defaultview, dateformat);
			//TODO this is temporary workaround for current account update. We should not store data in RAM, but get data from sqlite via providers when needed.
			CalendarSettings.setDateFormat(dateformat);
			CalendarSettings.setUsing_AM_PM(am_pmToggle.isChecked());
			account.setSetting_default_view(defaultview);

			if (!success) {
				values.put(AccountProvider.AMetaData.AccountMetaData.NEED_UPDATE, 2);
			}
			getContentResolver().update(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, values, null, null);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
			}
			super.onPostExecute(result);
		}

	}

	private void feelFields(Account account) {
		
		if (account.getSetting_ampm() == 1) {
			am_pmToggle.setChecked(true);
		} else {
			am_pmToggle.setChecked(false);
		}

		if (!account.getSetting_default_view().equals("null")) {
			int pos = Utils.getArrayIndex(defaultviewArray, account.getSetting_default_view());
			defaultviewSpinner.setSelection(pos);
			prefs.setValue(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, account.getSetting_default_view());
		}

		if (!account.getSetting_date_format().equals("null")) {
			int pos = Utils.getArrayIndex(dateformatArray, account.getSetting_date_format());
			dateformatSpinner.setSelection(pos);
		}

		prefs.save();
	}

	class GetAccountFromDBTask extends AsyncTask<Void, Account, Account> {

		@Override
		protected void onPreExecute() {

			pb.setVisibility(View.VISIBLE);

			super.onPreExecute();
		}

		@Override
		protected Account doInBackground(Void... args) {
			return (new Account());
		}

		@Override
		protected void onPostExecute(Account account) {
			feelFields(new Account());
			new GetAccountTask().execute();
			super.onPostExecute(account);
		}

	}

	class GetAccountTask extends AsyncTask<Void, Account, Account> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Account doInBackground(Void... args) {
			return dm.getAccountFromRemoteDb();
		}

		@Override
		protected void onPostExecute(Account account) {
			if (account != null) {
				feelFields(account);
			}

			pb.setVisibility(View.GONE);
			super.onPostExecute(account);
		}

	}
}
