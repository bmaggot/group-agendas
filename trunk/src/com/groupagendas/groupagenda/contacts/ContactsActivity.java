package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.groupagendas.groupagenda.DataManagement;
import com.groupagendas.groupagenda.R;
import com.makeramen.segmented.SegmentedRadioGroup;

public class ContactsActivity extends ListActivity implements OnCheckedChangeListener {

	private SegmentedRadioGroup segmentedButtons;

	private DataManagement dm;

	private final String CONTACTS_TASK = "contactsTask";
	private final String GROUPS_TASK = "groupsTask";
	private String CURRENT_TASK = CONTACTS_TASK;

	/*
	 * 0 - Contacts 1 - Groups
	 */
	private int CONTACTS_LIST = 0;
	private int GROUPS_LIST = 1;
	private int CURRENT_LIST = CONTACTS_LIST;

	private ArrayList<Contact> contacts = null;
	private ArrayList<Group> groups = null;

	private ProgressBar pb;

	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;
	
	private EditText searchView;
	
	private ContactsAdapter cAdapter;
	private GroupsAdapter gAdapter;

	public void onResume() {
		super.onResume();
		CURRENT_LIST = preferences.getInt("ContactsActivityList", CURRENT_LIST);
		CURRENT_TASK = preferences.getString("ContactsActivityTask", CONTACTS_TASK);

		new GetContactsFromDBTask().execute(CURRENT_TASK);

	}

	public void onPause() {
		super.onPause();
		editor.putString("ContactsActivityTask", CURRENT_TASK);
		editor.putInt("ContactsActivityList", CURRENT_LIST);
		editor.commit();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contacts);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		editor.remove("ContactsActivityTask");
		editor.remove("ContactsActivityList");
		editor.commit();

		dm = DataManagement.getInstance(this);

		segmentedButtons = (SegmentedRadioGroup) findViewById(R.id.segmentedButtons);
		segmentedButtons.setOnCheckedChangeListener(this);

		pb = (ProgressBar) findViewById(R.id.progress);
		
		searchView = (EditText) findViewById(R.id.search);
		searchView.addTextChangedListener(filterTextWatcher);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contacts_menu, menu);
		return true;
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s !=null){
				if (CURRENT_LIST == CONTACTS_LIST){
					if(cAdapter != null){
						cAdapter.getFilter().filter(s);
					}
				}else{
					if(gAdapter != null){
						gAdapter.getFilter().filter(s);
					}
				}
			}
		}

	};
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    searchView.removeTextChangedListener(filterTextWatcher);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_contact:
			Intent contactEdit = new Intent(ContactsActivity.this, ContactEditActivity.class);
			contactEdit.putExtra("action", false);
			startActivity(contactEdit);
			return true;
		case R.id.add_group:
			Intent groupEdit = new Intent(ContactsActivity.this, GroupEditActivity.class);
			groupEdit.putExtra("action", false);
			startActivity(groupEdit);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {

		if (CURRENT_LIST == CONTACTS_LIST) {
			Intent contactIntent = new Intent(ContactsActivity.this, ContactInfoActivity.class);
			StringBuilder sb = new StringBuilder(contacts.get(position).name).append(" ").append(contacts.get(position).lastname);
			contactIntent.putExtra("contactName", sb.toString());
			contactIntent.putExtra("contactId", contacts.get(position).contact_id);
			startActivity(contactIntent);
		} else if (CURRENT_LIST == GROUPS_LIST) {
			Intent groupIntent = new Intent(ContactsActivity.this, GroupContactsActivity.class);
			groupIntent.putExtra("groupName", groups.get(position).title);
			groupIntent.putExtra("groupId", groups.get(position).group_id);
			startActivity(groupIntent);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group == segmentedButtons) {
			switch (checkedId) {
			case R.id.contacts:
				CURRENT_LIST = CONTACTS_LIST;
				if (contacts != null) {
					cAdapter = new ContactsAdapter(contacts, ContactsActivity.this);
					setListAdapter(cAdapter);
					new GetContactsTask().execute(CONTACTS_TASK);
				} else {
					new GetContactsFromDBTask().execute(CONTACTS_TASK);
				}
				editor.putString("ContactsActivityTask", CONTACTS_TASK);
				editor.putInt("ContactsActivityList", CONTACTS_LIST);
				editor.commit();
				break;
			case R.id.groups:
				CURRENT_LIST = GROUPS_LIST;
				if (groups != null) {
					gAdapter = new GroupsAdapter(groups, ContactsActivity.this);
					setListAdapter(gAdapter);
					new GetContactsTask().execute(GROUPS_TASK);
				} else {
					new GetContactsFromDBTask().execute(GROUPS_TASK);
				}
				editor.putString("ContactsActivityTask", GROUPS_TASK);
				editor.putInt("ContactsActivityList", GROUPS_LIST);
				editor.commit();
				break;
			}
		}
	}

	class GetContactsFromDBTask extends AsyncTask<String, String, String> {

		protected void onPreExecute() {

			pb.setVisibility(View.VISIBLE);

			super.onPreExecute();
		}

		protected String doInBackground(String... type) {
			CURRENT_TASK = type[0];
			if (type[0].equals(CONTACTS_TASK)) {
				contacts = dm.getContactsFromDb("");
			} else if (type[0].equals(GROUPS_TASK)) {
				groups = dm.getGroupsFromDb();
			}

			return type[0];
		}

		protected void onPostExecute(String result) {
			if (result.equals(CONTACTS_TASK)) {
				if (contacts != null) {
					cAdapter = new ContactsAdapter(contacts, ContactsActivity.this);
					setListAdapter(cAdapter);
					new GetContactsTask().execute(CURRENT_TASK);
				}
			} else if (result.equals(GROUPS_TASK)) {
				if (groups != null) {
					gAdapter = new GroupsAdapter(groups, ContactsActivity.this);
					setListAdapter(gAdapter);
					new GetContactsTask().execute(CURRENT_TASK);
				}
			}
			super.onPostExecute(result);
		}

	}

	class GetContactsTask extends AsyncTask<String, String, String> {

		protected void onPreExecute() {

			pb.setVisibility(View.VISIBLE);

			super.onPreExecute();
		}

		protected String doInBackground(String... type) {
			CURRENT_TASK = type[0];
			if (type[0].equals(CONTACTS_TASK)) {
				ArrayList<Contact> contacts_ = dm.getContactList(null);
				if(contacts_ != null)
					contacts = contacts_;
			} else if (type[0].equals(GROUPS_TASK)) {
				ArrayList<Group> groups_ = dm.getGroupList();
				if(groups_ != null)
					groups = groups_;
			}

			return type[0];
		}

		protected void onPostExecute(String result) {
			if (result.equals(CONTACTS_TASK)) {
				if (contacts != null && CURRENT_LIST == CONTACTS_LIST) {
					cAdapter.setItems(contacts);
					cAdapter.notifyDataSetChanged();
				}
			} else if (result.equals(GROUPS_TASK)) {
				if (groups != null && CURRENT_LIST == GROUPS_LIST) {
					gAdapter.setItems(groups);
					gAdapter.notifyDataSetChanged();
				}
			}
			pb.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}
}
