package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;

public class GroupContactsActivity extends ListActivity {

	private DataManagement dm;
	private Intent intent;

	private ArrayList<Contact> contacts;
	private ContactsAdapter cAdapter;
	
	private TextView groupNameView;
	private String groupName;
	
	private Toast toast;

	private final int DELETE_DIALOG = 0;
	
	private final int REQUEST_CODE = 1;
	
	@Override
	public void onResume() {
		super.onResume();
		new GetGroupContactsTask().execute();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.group_contacts);

		dm = DataManagement.getInstance(this);

		intent = getIntent();
		
		groupName = intent.getStringExtra("groupName");
		groupNameView = (TextView) findViewById(R.id.group_name);
		groupNameView.setText(groupName);

	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Intent intent = new Intent(this, ContactInfoActivity.class);
		StringBuilder sb = new StringBuilder(contacts.get(position).name).append(" ").append(contacts.get(position).lastname);
		intent.putExtra("contactName", sb.toString());
		intent.putExtra("contactId", contacts.get(position).contact_id);
		startActivity(intent);
	}

	class GetGroupContactsTask extends AsyncTask<Void, ArrayList<Contact>, ArrayList<Contact>> {
		
		@Override
		protected ArrayList<Contact> doInBackground(Void... type) {
			int id = intent.getIntExtra("groupId", 0);
			String where = ContactsProvider.CMetaData.ContactsMetaData.GROUPS+" LIKE '%="+id+"&%' OR "+ContactsProvider.CMetaData.ContactsMetaData.GROUPS+" LIKE '%="+id+"'";
			contacts = ContactManagement.getContactsFromLocalDb(where);
			return contacts;
		}

		@Override
		protected void onPostExecute(ArrayList<Contact> contacts) {
			if (contacts != null) {
				cAdapter = new ContactsAdapter(contacts, GroupContactsActivity.this);
				cAdapter.notifyDataSetChanged();
			}
			
			super.onPostExecute(contacts);
		}

	}
	
	class DeleteGroupTask extends AsyncTask<Void, Boolean, Boolean> {
		@Override
		protected Boolean doInBackground(Void... type) {
			
			int groupId = intent.getIntExtra("groupId", 0);
			
			if(groupId > 0){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI+"/"+groupId);
				
				Boolean result = dm.removeGroup(groupId);
				
				if(result){
					getContentResolver().delete(uri, null, null);
				}else{
//					ContentValues values = new ContentValues();
//					values.put(ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE, 3);					
//					getContentResolver().update(uri, values, null, null);
				}
			}
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			toast = Toast.makeText(GroupContactsActivity.this, "", Toast.LENGTH_SHORT);
			toast.setText(getString(R.string.group_deleted));
			toast.show();
			super.onPostExecute(result);
			onBackPressed();
		}

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
							new DeleteGroupTask().execute();
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
		inflater.inflate(R.menu.group_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_group:
			Intent groupEdit = new Intent(GroupContactsActivity.this, GroupEditActivity.class);
			groupEdit.putExtra("group_id", intent.getIntExtra("groupId", 0));
			groupEdit.putExtra("group_name", groupName);
			startActivityForResult(groupEdit, REQUEST_CODE);
			return true;
		case R.id.delete_group:
			showDialog(DELETE_DIALOG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == REQUEST_CODE) {
			groupName = intent.getStringExtra("group_name");
			groupNameView.setText(groupName);
		}
	}
}
