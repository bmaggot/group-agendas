package com.groupagendas.groupagenda.contacts;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactInfoActivity extends Activity {
	private TableLayout table;
	private TableRow row;
	private TableRow.LayoutParams params;
	private LinearLayout.LayoutParams paramsD;

	private Intent intent;

	private Toast toast;
	private Contact mContact;

	private final int DELETE_DIALOG = 0;
	private int timezoneInUse = 0;
	private String[] country_codes;
	private String[] country_titles;

	@Override
	public void onResume() {
		super.onResume();
		this.setContentView(R.layout.contact_info);

		table = (TableLayout) findViewById(R.id.table);
		country_codes = getResources().getStringArray(R.array.country_codes);
		country_titles = getResources().getStringArray(R.array.countries);

		new GetContactTask().execute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new DateTimeUtils(this);

		intent = getIntent();

		params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 10, 0, 10);

		paramsD = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 1);
	}

	class GetContactTask extends AsyncTask<Void, Contact, Contact> {

		private static final int LEAST_YEAR_VALUE = 1900;

		@Override
		protected Contact doInBackground(Void... type) {
			Contact contact = ContactManagement.getContactFromLocalDb(ContactInfoActivity.this, intent.getIntExtra("contactId", 0),
					intent.getLongExtra("contactCreated", 0));
			return contact;
		}

		@Override
		protected void onPostExecute(Contact contact) {
			if (contact != null) {

				mContact = contact;

				table.removeAllViews();

				// Image
				if (contact.image && contact.image_bytes != null) {
					ImageView imageView = (ImageView) findViewById(R.id.image);
					Bitmap bitmap = Utils.getResizedBitmap(
							BitmapFactory.decodeByteArray(contact.image_bytes, 0, contact.image_bytes.length), 120, 120);
					if (bitmap != null)
						imageView.setImageBitmap(bitmap);
				}
				// Name
				TextView nameView = (TextView) findViewById(R.id.name);
				nameView.setText(contact.name);
				// Lastname
				TextView lastnameView = (TextView) findViewById(R.id.lastname);
				if (contact.lastname != null && !contact.lastname.equals("null"))
					lastnameView.setText(contact.lastname);

				// Email
				if (contact.email != null && !contact.email.equals("null")) {
					setTableRow(getString(R.string.email), contact.email);
				}
				// Phone
				String phone_full = "";
				if (contact.phone1_code != null && !contact.phone1_code.equals("null") && (contact.phone1_code.length() > 0)) {
					phone_full += contact.phone1_code + "\t";
				}
				if (contact.phone1 != null && !contact.phone1.equals("null")) {
					phone_full += contact.phone1;
				}
				if (phone_full != null && phone_full.length() > 0) {
					setTableRow(getString(R.string.phone), phone_full);
				}
				// Birth date
				if (contact.birthdate != null && !contact.birthdate.equals("")) {
					Calendar birthdateCalendar = Utils.stringToCalendar(getApplicationContext(), contact.birthdate,
							DataManagement.ACCOUNT_BIRTHDATE_TIMESTAMP_FORMAT);
					if (birthdateCalendar.get(Calendar.YEAR) < LEAST_YEAR_VALUE) {
						birthdateCalendar.set(Calendar.YEAR, LEAST_YEAR_VALUE);
					}
					DateTimeUtils dtUtils = new DateTimeUtils(ContactInfoActivity.this);
					setTableRow(getString(R.string.birthday), dtUtils.formatDate(birthdateCalendar.getTime()));
				}
				// Country
				if (contact.country != null && !contact.country.equals("null")) {

					if (contact.country.length() > 0) {
						for (int iterator = 0; iterator < country_codes.length; iterator++) {
							if (country_codes[iterator].equalsIgnoreCase(contact.country)) {
								timezoneInUse = iterator;
							}
						}
					}

					setTableRow(getString(R.string.country), country_titles[timezoneInUse]);
				}
				// City
				if (contact.city != null && !contact.city.equals("null") && contact.city.length() > 0) {
					setTableRow(getString(R.string.city), contact.city);
				}
				// Street
				if (contact.street != null && !contact.street.equals("null") && contact.street.length() > 0) {
					setTableRow(getString(R.string.street), contact.street);
				}
				// ZIP code
				if (contact.zip != null && !contact.zip.equals("null") && contact.zip.length() > 0) {
					setTableRow(getString(R.string.zip), contact.zip);
				}
				// agenda visibility
				if (contact.visibility != null && !contact.visibility.equals("n")) {
					setTableRow(getString(R.string.agenda_visibility),
							contact.visibility.equals("f") ? getResources().getStringArray(R.array.visibility_labels)[1] : getResources()
									.getStringArray(R.array.visibility_labels)[2]);
				}

				if (contact.can_add_note != null && contact.can_add_note.length() > 0) {
					setTableRow(getString(R.string.allow_to_add_note_in_agenda), contact.can_add_note.equals("y") ? getResources()
							.getString(R.string.yes) : getResources().getString(R.string.no));
				}

				// Groups
				String groupsList = "";
				if (contact.groups != null) {
					if (contact.groups.size() > 0) {
						String groupsIds = "";

						for (String id : contact.groups.values()) {
							if (groupsIds.length() < 1) {
								groupsIds += id;
							} else {
								groupsIds += ", " + id;
							}
						}

						groupsList = getGroupTitles(groupsIds);
					}
				}
				setTableRow(getString(R.string.groups), groupsList);
			}
		}

	}

	class DeleteContactTask extends AsyncTask<Void, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(Void... type) {
			ContactManagement.deleteContact(ContactInfoActivity.this, mContact);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			toast = Toast.makeText(ContactInfoActivity.this, "", Toast.LENGTH_SHORT);
			toast.setText(getString(R.string.contact_deleted));
			toast.show();
			super.onPostExecute(result);
			onBackPressed();
		}

	}

	private void setTableRow(String field, String value) {
		TextView tvLeft = new TextView(this);
		tvLeft.setPadding(3, 3, 3, 3);
		tvLeft.setTextColor(Color.parseColor("#01a2b5"));
		tvLeft.setTextSize(18);
		tvLeft.setText(field);

		TextView tvRight = new TextView(this);
		tvRight.setPadding(3, 3, 3, 10);
		tvRight.setTextColor(Color.parseColor("#000000"));
		tvRight.setText(value);

		row = new TableRow(this);
		row.setLayoutParams(params);

		row.addView(tvLeft);
		row.addView(tvRight);

		table.addView(row);

		View divider = new View(this);
		divider.setBackgroundColor(Color.parseColor("#FF909090"));
		divider.setLayoutParams(paramsD);
		table.addView(divider);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case DELETE_DIALOG:
			builder.setMessage(getString(R.string.sure_delete)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							new DeleteContactTask().execute();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_contact:
			Intent contactEdit = new Intent(ContactInfoActivity.this, ContactEditActivity.class);
			if (mContact != null) {
				contactEdit.putExtra("contact_id", mContact.contact_id);
			}

			startActivity(contactEdit);
			return true;
		case R.id.delete_contact:
			showDialog(DELETE_DIALOG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private String getGroupTitles(String ids) {
		Cursor cur;
		String result = "";

		Uri uri = ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI;
		String[] projection = { ContactsProvider.CMetaData.GroupsMetaData.TITLE };
		String selection = ContactsProvider.CMetaData.GroupsMetaData.G_ID + " IN (" + ids + ")";

		cur = getContentResolver().query(uri, projection, selection, null, null);

		if ((cur != null) && (cur.getCount() > 0)) {
			if (cur.moveToFirst()) {
				while (!cur.isAfterLast()) {
					if (result.length() < 1) {
						result += cur.getString(cur.getColumnIndex(projection[0]));
					} else {
						result += "\n" + cur.getString(cur.getColumnIndex(projection[0]));
					}
					cur.moveToNext();
				}
			}
		} else {
			Log.i("GetGroupTitlesTask", "Query didn't return any entry");
		}

		cur.close();

		return result;
	}

}
