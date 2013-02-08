package com.groupagendas.groupagenda.address;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.SaveDeletedData;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.events.EventActivity.StaticTimezones;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.StringValueUtils;

public class AddressBookInfoActivity extends Activity {

	private Address address;

	private TextWatcher watcher;
	private Button saveButton;
	Button deleteButton;
	private boolean changesMade = false;
	private ProgressBar pb;
	private EditText titleView;
	private EditText cityView;
	private EditText streetView;
	private EditText zipView;
	private LinearLayout titleBlock;
	boolean action;
	boolean fill_info;
	private TextView countryView;
	private TextView timezoneView;
	private LinearLayout countrySpinnerBlock;
	private LinearLayout timezoneSpinnerBlock;
	protected CountriesAdapter countriesAdapter = null;
	protected TimezonesAdapter timezonesAdapter = null;
	protected ArrayList<StaticTimezones> countriesList = null;
	private ArrayList<StaticTimezones> filteredCountriesList;
	private int timezoneInUse = 0;
	Account account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.address_book_show);

		if (watcher == null) {
			watcher = new GenericTextWatcher();
		}
		account = new Account(AddressBookInfoActivity.this);
		pb = (ProgressBar) findViewById(R.id.progress);
		TextView topTextView = (TextView) findViewById(R.id.topText);
		countryView = (TextView) findViewById(R.id.countryView);
		titleView = (EditText) findViewById(R.id.titleView);
		cityView = (EditText) findViewById(R.id.cityView);
		streetView = (EditText) findViewById(R.id.streetView);
		zipView = (EditText) findViewById(R.id.zipView);
		timezoneView = (TextView) findViewById(R.id.timezoneView);
		saveButton = (Button) findViewById(R.id.save_button);
		deleteButton = (Button) findViewById(R.id.address_delete);
		titleBlock = (LinearLayout) findViewById(R.id.titleSpinnerBlock);
		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		timezoneSpinnerBlock = (LinearLayout) findViewById(R.id.timezoneSpinnerBlock);

		String[] cities;
		String[] countries;
		String[] countries2;
		String[] country_codes;
		String[] timezones;
		String[] altnames;

		cities = getResources().getStringArray(R.array.city);
		countries = getResources().getStringArray(R.array.countries);
		countries2 = getResources().getStringArray(R.array.countries2);
		country_codes = getResources().getStringArray(R.array.country_codes);
		timezones = getResources().getStringArray(R.array.timezones);
		altnames = getResources().getStringArray(R.array.timezone_altnames);
		
		countriesList = new ArrayList<StaticTimezones>(cities.length);
		for (int i = 0; i < cities.length; i++) {
			StaticTimezones temp = new EventActivity().new StaticTimezones();

			temp.id = StringValueUtils.valueOf(i);
			temp.city = cities[i];
			temp.country = countries[i];
			temp.country2 = countries2[i];
			temp.country_code = country_codes[i];
			temp.timezone = timezones[i];
			temp.altname = altnames[i];

			countriesList.add(temp);
		}
		// if (countriesList != null) {
			countriesAdapter = new CountriesAdapter(AddressBookInfoActivity.this, R.layout.search_dialog_item, countriesList);
			timezonesAdapter = new TimezonesAdapter(AddressBookInfoActivity.this, R.layout.search_dialog_item, countriesList);
		// }

		action = getIntent().getBooleanExtra("action", true);
		fill_info = getIntent().getBooleanExtra("fill_info", false);

		if (action) {
			long selectedAddressId = Long.parseLong(getIntent().getStringExtra("addressId"));
			String where = AddressProvider.AMetaData.AddressesMetaData._ID + " = " + selectedAddressId;
			address = AddressManagement.getAddressFromLocalDb(AddressBookInfoActivity.this, where);

			if (address.getTimezone().length() > 0) {
				for (StaticTimezones entry : countriesList) {
					if (entry.timezone.equalsIgnoreCase(address.getTimezone()))
						timezoneInUse = Integer.parseInt(entry.id);
				}
				if (timezoneInUse > 0) {
					filteredCountriesList = new ArrayList<StaticTimezones>();

					for (StaticTimezones tz : countriesList) {
						if (tz.country_code.equalsIgnoreCase(address.getCountry())) {
							filteredCountriesList.add(tz);
						}
					}

					timezonesAdapter = new TimezonesAdapter(AddressBookInfoActivity.this, R.layout.search_dialog_item,
							filteredCountriesList);
					timezonesAdapter.notifyDataSetChanged();

					timezoneView.setText(countriesList.get(timezoneInUse).altname);
					countryView.setText(countriesList.get(timezoneInUse).country2);
				}

			}

			topTextView.setText(address.getTitle());
			// countryView.setText(address.getCountry_name());
			cityView.setText(address.getCity());
			streetView.setText(address.getStreet());
			zipView.setText(address.getZip());
			// timezoneView.setText(address.getTimezone());
		} else {
			address = new Address();
			topTextView.setText(getString(R.string.new_address));
			deleteButton.setVisibility(View.GONE);
			titleBlock.setVisibility(View.VISIBLE);
			if(!fill_info){
				String tmz = account.getTimezone();
				for (StaticTimezones item : countriesList) {
					if (item.timezone.equalsIgnoreCase(tmz)) {
						timezoneInUse = Integer.parseInt(item.id);
						countryView.setText(countriesList.get(timezoneInUse).country2);
						continue;
					}
				}
				timezoneView.setText(account.getTimezone());
			} else {
				for (StaticTimezones entry : countriesList) {
					if (entry.timezone.equalsIgnoreCase(EventActivity.timezoneView.getText().toString()))
						timezoneInUse = Integer.parseInt(entry.id);
				}
				if(timezoneInUse == 0){
					for (StaticTimezones entry : countriesList) {
						if (entry.country2.equalsIgnoreCase(EventActivity.countryView.getText().toString()))
							timezoneInUse = Integer.parseInt(entry.id);
					}
				}
				cityView.setText(EventActivity.cityView.getText());
				streetView.setText(EventActivity.streetView.getText());
				zipView.setText(EventActivity.zipView.getText());
				timezoneView.setText(countriesList.get(timezoneInUse).altname);
				countryView.setText(countriesList.get(timezoneInUse).country2);
				changesMade = true;
			}
		}

		saveButton.setEnabled(false);
		titleView.addTextChangedListener(watcher);
		cityView.addTextChangedListener(watcher);
		streetView.addTextChangedListener(watcher);
		zipView.addTextChangedListener(watcher);
		timezoneView.addTextChangedListener(watcher);
		countryView.addTextChangedListener(watcher);

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (action) {
					new UpdateAddressTask().execute();
				} else {
					if (titleView.getText().length() == 0) {
						Toast toast = Toast.makeText(AddressBookInfoActivity.this, getString(R.string.address_create_info),
								Toast.LENGTH_LONG);
						toast.show();
					} else {
						new CreateAddressTask().execute();
					}
				}
			}
		});

		deleteButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(0);
			}
		});

		countryView = (TextView) findViewById(R.id.countryView);
		countrySpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(AddressBookInfoActivity.this);
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
						countryView.setText(countriesList.get(timezoneInUse).country2);
						address.setCountry(countriesList.get(timezoneInUse).country_code);

						filteredCountriesList = new ArrayList<StaticTimezones>();

						for (StaticTimezones tz : countriesList) {
							if (tz.country_code.equalsIgnoreCase(address.getCountry())) {
								filteredCountriesList.add(tz);
							}
						}

						timezonesAdapter = new TimezonesAdapter(AddressBookInfoActivity.this, R.layout.search_dialog_item,
								filteredCountriesList);
						timezonesAdapter.notifyDataSetChanged();

						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						address.setTimezone(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

		timezoneView = (TextView) findViewById(R.id.timezoneView);
		timezoneSpinnerBlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dia1 = new Dialog(AddressBookInfoActivity.this);
				dia1.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dia1.setContentView(R.layout.search_dialog);

				ListView diaList = (ListView) dia1.findViewById(R.id.dialog_list);
				diaList.setAdapter(timezonesAdapter);
				timezonesAdapter.notifyDataSetChanged();

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
							if (timezonesAdapter != null)
								timezonesAdapter.getFilter().filter(s);
						}
					}
				};

				searchView.addTextChangedListener(filterTextWatcher);

				diaList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
						timezoneInUse = Integer.parseInt(view.getTag().toString());
						countryView.setText(countriesList.get(timezoneInUse).country2);
						address.setCountry(countriesList.get(timezoneInUse).country_code);
						timezoneView.setText(countriesList.get(timezoneInUse).altname);
						address.setTimezone(countriesList.get(timezoneInUse).timezone);
						dia1.dismiss();
					}
				});
				dia1.show();
			}
		});

	}

	class DeleteAddressTask extends AsyncTask<Void, Boolean, Boolean> {
		@Override
		protected Boolean doInBackground(Void... type) {
			String res = AddressManagement.setAddressBookEntries(AddressBookInfoActivity.this, address, AddressManagement.DELETE);
			if (!res.contentEquals("true")) {
				SaveDeletedData offlineDeletedAddresses1 = new SaveDeletedData(AddressBookInfoActivity.this);
				offlineDeletedAddresses1.addAddressForLaterDelete(address.getId());				
			} 
			
			AddressManagement.deleteAddressFromLocalDb(AddressBookInfoActivity.this, address.getId());
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Toast toast = Toast.makeText(AddressBookInfoActivity.this, "", Toast.LENGTH_LONG);
			if (result) {
				toast.setText(getString(R.string.address_deleted));
			} else {
				toast.setText(EventManagement.getError());
			}
			toast.show();

			super.onPostExecute(result);
			finish();
		}

	}

	class UpdateAddressTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			address.setCity(cityView.getText().toString());
			address.setStreet(streetView.getText().toString());
			address.setZip(zipView.getText().toString());
			address.setCountry_name(countryView.getText().toString());
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			String res = AddressManagement.setAddressBookEntries(AddressBookInfoActivity.this, address, AddressManagement.UPDATE);
			if (res.contentEquals("true")) {
				address.setUploadedToServer(true);
			} else {
				address.setUploadedToServer(false);
			}
			AddressManagement.updateAddressInLocalDb(AddressBookInfoActivity.this, address);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
			} else {
				showDialog(0);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}

	}

	class CreateAddressTask extends AsyncTask<Event, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			Account acc = new Account(AddressBookInfoActivity.this);
			saveButton.setText(getString(R.string.saving));
			address.setTitle(titleView.getText().toString());
			address.setCity(cityView.getText().toString());
			address.setStreet(streetView.getText().toString());
			address.setZip(zipView.getText().toString());
			address.setUser_id(acc.getUser_id());
			address.setState("");
			address.setCountry_name(countryView.getText().toString());
			address.setTimezone(countriesList.get(timezoneInUse).timezone);
			address.setCountry(countriesList.get(timezoneInUse).country_code);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... events) {
			int address_id = 0;
			
			if(DataManagement.networkAvailable){
				String res = AddressManagement.setAddressBookEntries(AddressBookInfoActivity.this, address, AddressManagement.CREATE);
				try {
					address_id = Integer.parseInt(res);
				} catch (Exception e) {
					//return false;
				}
			}
			
			if (address_id > 0) {
				address.setUploadedToServer(true);
			} else {
				address.setUploadedToServer(false);
			}
			address.setId(address_id);
			AddressManagement.insertAddressInLocalDb(AddressBookInfoActivity.this, address);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
			} else {
				showDialog(0);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}

	}

	private class GenericTextWatcher implements TextWatcher {

		private String oldText = null;

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			oldText = charSequence.toString();
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
			if (!editable.toString().equalsIgnoreCase(oldText)) {
				changesMade = true;
				saveButton.setEnabled(changesMade);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case 0:
			builder.setMessage(getString(R.string.sure_delete)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							new DeleteAddressTask().execute();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			break;
		}
		return builder.create();
	}

	@Override
	public void onBackPressed() {
		if (changesMade) {
			final Toast toast = Toast.makeText(AddressBookInfoActivity.this, getString(R.string.address_create_info), Toast.LENGTH_LONG);
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(this.getResources().getString(R.string.save_your_changes))
					.setMessage(this.getResources().getString(R.string.do_you_want_to_save_your_changes))
					.setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (action) {
								new UpdateAddressTask().execute();
								dialog.dismiss();
							} else {
								if (titleView.getText().length() > 0) {
									new CreateAddressTask().execute();
									dialog.dismiss();
								} else {
									toast.show();
								}
							}
						}

					}).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}

					}).setCancelable(false).show();
		} else {
			super.onBackPressed();
		}
	}

}
