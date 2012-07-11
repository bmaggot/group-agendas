package com.groupagendas.groupagenda.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactInfoActivity extends Activity {
	private TableLayout table;
	private TableRow row;
	private TableRow.LayoutParams params;
	private LinearLayout.LayoutParams paramsD;

	private DataManagement dm;
	private Intent intent;
	
	private Toast toast;
	private Contact mContact;

	private final int DELETE_DIALOG = 0;
	
	private DateTimeUtils dtUtils;
	
	public void onResume() {
		super.onResume();
		new GetGroupContactsTask().execute();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contact_info);

		dm = DataManagement.getInstance(this);
		dtUtils = new DateTimeUtils(this);

		intent = getIntent();

		table = (TableLayout) findViewById(R.id.table);

		params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 10, 0, 10);

		paramsD = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);

	}

	class GetGroupContactsTask extends AsyncTask<Void, Contact, Contact> {

		protected Contact doInBackground(Void... type) {
			Contact contact = dm.getContact(intent.getIntExtra("contactId", 0));
			return contact;
		}

		protected void onPostExecute(Contact contact) {
			if (contact != null) {

				mContact = contact;
				
				table.removeAllViews();
				
				// Image
				if (contact.image) {
					ImageView imageView = (ImageView) findViewById(R.id.image);
					Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(contact.image_bytes, 0, contact.image_bytes.length), 120, 120);
					imageView.setImageBitmap(bitmap);
				}
				// Name
				TextView nameView = (TextView) findViewById(R.id.name);
				nameView.setText(contact.name);
				// Lastname
				TextView lastnameView = (TextView) findViewById(R.id.lastname);
				if(contact.lastname != null && !contact.lastname.equals("null"))	lastnameView.setText(contact.lastname);
				
				// Email
				if (!contact.email.equals("null")) {
					setTableRow(getString(R.string.email), contact.email);
				}
				// Phone
				if (!contact.phone1.equals("null")) {
					setTableRow(getString(R.string.phone), contact.phone1);
				}
				// Birth date
				if (!contact.birthdate.equals("null")) {
					setTableRow(getString(R.string.birthday), dtUtils.formatDate(contact.birthdate));
				}
				// Country
				if (!contact.country.equals("null")) {
					int resId = getResources().getIdentifier(contact.country, "string", "com.groupagendas.groupagenda");
					if(resId > 0){
						setTableRow(getString(R.string.country), getString(resId));
					}
				}
				// City
				if (!contact.city.equals("null")) {
					setTableRow(getString(R.string.city), contact.city);
				}
				// Street
				if (!contact.street.equals("null")) {
					setTableRow(getString(R.string.street), contact.street);
				}
				// ZIP code
				if (!contact.zip.equals("null")) {
					setTableRow(getString(R.string.zip), contact.zip);
				}
			}

			super.onPostExecute(contact);
		}

	}

	class DeleteContactTask extends AsyncTask<Void, Boolean, Boolean> {
		protected Boolean doInBackground(Void... type) {
			
			Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI+"/"+mContact.contact_id);
			
			Boolean result = dm.removeContact(mContact.contact_id);
			
			if(result){
				getContentResolver().delete(uri, null, null);
			}else{
				ContentValues values = new ContentValues();
				values.put(ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE, 3);
				
				getContentResolver().update(uri, values, null, null);
			}
			
			return true;
		}

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
		tvRight.setPadding(3, 3, 3, 3);
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

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case DELETE_DIALOG:
			builder.setMessage(getString(R.string.sure_delete)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new DeleteContactTask().execute();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
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
			contactEdit.putExtra("contact_id", mContact.contact_id);
			startActivity(contactEdit);
			return true;
		case R.id.delete_contact:
			showDialog(DELETE_DIALOG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
