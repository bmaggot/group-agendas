package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.importer.ImportActivity;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.OfflineData;
import com.groupagendas.groupagenda.events.EventActivity;
import com.makeramen.segmented.SegmentedRadioGroup;

public class ContactsActivity extends ListActivity implements OnCheckedChangeListener {
	public ArrayList<Contact> selectedContacts = new ArrayList<Contact>();
	public ArrayList<Group> selectedGroups = new ArrayList<Group>();

	private SegmentedRadioGroup segmentedButtons;
	private ArrayList<Contact> contacts;
	private ArrayList<Group> groups;

	private final String CONTACTS_TASK = "contactsTask";
	private final String GROUPS_TASK = "groupsTask";
	private String CURRENT_TASK = CONTACTS_TASK;

	/*
	 * 0 - Contacts 1 - Groups
	 */
	private int CONTACTS_LIST = 0;
	private int GROUPS_LIST = 1;
	private int CURRENT_LIST = CONTACTS_LIST;

	// private ProgressBar pb;

	private GestureDetector mGestureDetector;

	private static float sideIndexX; // Side index coordinates within
	private static float sideIndexY; //
	private int sideIndexHeight; // height of side index
	private int indexListSize; // number of items in the side index
	private int displayedIndexListSize; // number of visible items in the SI
	private TreeMap<Integer, String> indexList = null; // list with items for
														// side index

	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;

	private LinearLayout sideIndex;
	private ListView contactList;
	private EditText searchView;

	private ContactsAdapter cAdapter;
	private GroupsAdapter gAdapter;

	private Button importButton;

	private int ACTIVITY_MODE = 0;
	private static final int LISTING_MODE = 0;
	private static final int SELECTION_MODE = 1;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
		sideIndexHeight = sideIndex.getHeight();
		sideIndex.removeAllViews();

		if (indexList != null) {
			indexListSize = indexList.size();

			TextView tmpTV = null;

			int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
			int tmpIndexListSize = indexListSize;

			while (tmpIndexListSize > indexMaxSize) {
				tmpIndexListSize = tmpIndexListSize / 2;
			}

			double delta = 0;

			if (tmpIndexListSize != 0)
				delta = indexListSize / tmpIndexListSize;
			else
				delta = 0;

			String tmpLetter = null;

			for (double i = 1; i <= indexListSize - 1; i = i + delta) {
				int raidytesSk = Integer.parseInt(indexList.keySet().toArray()[(int) i - 1].toString());

				tmpLetter = indexList.get(raidytesSk);
				tmpTV = new TextView(this);
				tmpTV.setText(tmpLetter);
				tmpTV.setGravity(Gravity.CENTER);
				tmpTV.setPadding(5, 0, 0, 0);
				tmpTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1);
				tmpTV.setLayoutParams(params);
				sideIndex.addView(tmpTV);
			}

			displayedIndexListSize = tmpIndexListSize;

			sideIndex.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// now you know coordinates of touch
					sideIndexX = event.getX();
					sideIndexY = event.getY();

					// and can display a proper item it country list
					displayListItem(0);

					return false;
				}
			});
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event != null) {
			if (mGestureDetector.onTouchEvent(event)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		initUI();

		CURRENT_LIST = preferences.getInt("ContactsActivityList", CURRENT_LIST);
		CURRENT_TASK = preferences.getString("ContactsActivityTask", CURRENT_TASK);

		sideIndex.setVisibility(View.GONE);

		searchView.addTextChangedListener(filterTextWatcher);

		if (getIntent().getExtras() != null)
			ACTIVITY_MODE = getIntent().getExtras().getInt("ACTIVITY_MODE");
		else
			ACTIVITY_MODE = LISTING_MODE;

		switch (ACTIVITY_MODE) {
		case SELECTION_MODE:
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			getListView().setItemsCanFocus(false);

			if (EventActivity.selectedContacts == null)
				EventActivity.selectedContacts = new ArrayList<Contact>();

			selectedContacts = EventActivity.selectedContacts;

			importButton.setText(R.string.contact_invite_save_button);
			importButton.setOnClickListener(new OnClickListener() {

				// @Override
				// public void onClick(View v) {
				// SparseBooleanArray selection =
				// getListView().getCheckedItemPositions();
				//
				// if (selection != null) {
				// for (int i=0; i<selection.size(); i++) {
				// if (selection.get(selection.keyAt(i))) {
				// int contains = -1;
				// Contact c = (Contact) cAdapter.getItem(selection.keyAt(i));
				//
				// for (int j = 0; j < EventActivity.selectedContacts.size();
				// j++) {
				// if (EventActivity.selectedContacts.get(j).contact_id ==
				// c.contact_id)
				// contains = j;
				// }
				//
				// if (contains < 0) {
				// EventActivity.selectedContacts.add(c);
				// } else {
				// EventActivity.selectedContacts.remove(contains);
				// }
				// }
				// }
				// }
				//
				// finish();
				// }
				@Override
				public void onClick(View v) {
					ArrayList<Contact> selected = cAdapter.getSelected();

					if (selected != null) {
						// for (int i=0; i<selected.size(); i++) {
						// int contains = -1;
						// Contact c = selected.get(i);
						//
						// for (int j = 0; j <
						// EventActivity.selectedContacts.size(); j++) {
						// if (EventActivity.selectedContacts.get(j).contact_id
						// == c.contact_id)
						// contains = j;
						// }
						//
						// if (contains < 0) {
						// EventActivity.selectedContacts.add(c);
						// } else {
						// EventActivity.selectedContacts.remove(contains);
						// }
						// }
						EventActivity.selectedContacts = selected;
					}

					finish();
				}
			});

			break;
		case LISTING_MODE:
			getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);

			importButton.setText(R.string.contact_import_button);
			importButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(new Intent(ContactsActivity.this, ImportActivity.class));
				}
			});
			break;
		}

		if (CURRENT_TASK.equals(CONTACTS_TASK)) {
			contacts = ContactManagement.getContactsFromLocalDb(ContactsActivity.this, null);
			cAdapter = new ContactsAdapter(contacts, this, selectedContacts);
			contactList.setAdapter(cAdapter);
			cAdapter.notifyDataSetChanged();

			showImportStats();

			if (contacts.size() > 10) {
				indexList = createIndex();
				sideIndex.setVisibility(View.VISIBLE);
			}
		} else if (CURRENT_TASK.equals(GROUPS_TASK)) {
			groups = ContactManagement.getGroupsFromLocalDb(ContactsActivity.this, null);
			gAdapter = new GroupsAdapter(groups, this);
			contactList.setAdapter(gAdapter);
			gAdapter.notifyDataSetChanged();
		}

		if (Data.credentialsClear) {
			Toast.makeText(this.getApplicationContext(), "Successfully cleared import credentials.", Toast.LENGTH_LONG).show();
			Data.credentialsClear = false;
		}

		if (Data.newEventPar) {
			selectedContacts.clear();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		editor.putString("ContactsActivityTask", CURRENT_TASK);
		editor.putInt("ContactsActivityList", CURRENT_LIST);
		editor.commit();
		searchView.removeTextChangedListener(filterTextWatcher);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contacts);

		// Toast.makeText(this, "Loading contacts... wait",
		// Toast.LENGTH_LONG).show();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		editor.remove("ContactsActivityTask");
		editor.remove("ContactsActivityList");
		editor.commit();

		segmentedButtons = (SegmentedRadioGroup) findViewById(R.id.segmentedButtons);
		segmentedButtons.setOnCheckedChangeListener(this);

		// pb = (ProgressBar) findViewById(R.id.progress);

		searchView = (EditText) findViewById(R.id.search);
		searchView.addTextChangedListener(filterTextWatcher);

		mGestureDetector = new GestureDetector(this, new SideIndexGestureListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contacts_menu, menu);
		return true;
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s != null) {
				if (CURRENT_LIST == CONTACTS_LIST) {
					if (cAdapter != null) {
						cAdapter.getFilter().filter(s);
					}
				} else {
					if (gAdapter != null) {
						gAdapter.getFilter().filter(s);
					}
				}
			}
		}
	};

	private TreeMap<Integer, String> createIndex() {
		ArrayList<Contact> contactList = contacts;

		if (contactList != null) {
			TreeMap<Integer, String> tmpIndexList = new TreeMap<Integer, String>();

			String currentLetter = null;
			String strItem = null;

			for (int j = 0; j < contactList.size(); j++) {
				strItem = contactList.get(j).name;
				if (strItem != null) {
					strItem = strItem.toUpperCase();
					if (Character.isLetter(strItem.charAt(0))) {
						currentLetter = strItem.substring(0, 1);

						if (!tmpIndexList.containsValue(currentLetter)) {
							tmpIndexList.put(j, currentLetter);
						}
					}
				}
			}

			return tmpIndexList;
		} else {
			return null;
		}
	}

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

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		switch (ACTIVITY_MODE) {
		case SELECTION_MODE:
			cAdapter.toggleSelected(Integer.parseInt(v.getTag().toString()));
			break;
		case LISTING_MODE:
			Integer c_id = 0;
			if (CURRENT_LIST == CONTACTS_LIST) {
				Intent contactIntent = new Intent(ContactsActivity.this, ContactInfoActivity.class);
				if (contacts.get(position).name != null && contacts.get(position).lastname != null) {
					StringBuilder sb = new StringBuilder(contacts.get(position).name).append(" ").append(contacts.get(position).lastname);
					contactIntent.putExtra("contactName", sb.toString());
				}
				if (v.getTag() != null) {
					c_id = Integer.parseInt(v.getTag().toString());
					contactIntent.putExtra("contactId", c_id);
					contactIntent.putExtra("contactCreated", contacts.get(position).created);
					startActivity(contactIntent);
				}
			} else if (CURRENT_LIST == GROUPS_LIST) {
				Intent groupIntent = new Intent(ContactsActivity.this, GroupContactsActivity.class);
				groupIntent.putExtra("groupName", groups.get(position).title);
				groupIntent.putExtra("groupId", groups.get(position).group_id);
				startActivity(groupIntent);
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
		if (group == segmentedButtons) {
			sideIndex.setVisibility(View.GONE);
			switch (checkedId) {
			case R.id.contacts:
				CURRENT_LIST = CONTACTS_LIST;
				CURRENT_TASK = CONTACTS_TASK;
				// Toast.makeText(this,
				// getString(R.string.waiting_for_contacts_load),
				// Toast.LENGTH_SHORT).show();
				setListAdapter(cAdapter);

				if (contacts.size() > 10)
					sideIndex.setVisibility(View.VISIBLE);

				editor.putString("ContactsActivityTask", CONTACTS_TASK);
				editor.putInt("ContactsActivityList", CONTACTS_LIST);
				editor.commit();
				break;

			case R.id.groups:
				CURRENT_LIST = GROUPS_LIST;
				CURRENT_TASK = GROUPS_TASK;
				// Toast.makeText(this,
				// getString(R.string.waiting_for_groups_load),
				// Toast.LENGTH_SHORT).show();
				groups = ContactManagement.getGroupsFromLocalDb(ContactsActivity.this, null);
				gAdapter = new GroupsAdapter(groups, this);
				contactList.setAdapter(gAdapter);
				gAdapter.notifyDataSetChanged();

				editor.putString("ContactsActivityTask", GROUPS_TASK);
				editor.putInt("ContactsActivityList", GROUPS_LIST);
				editor.commit();
				break;
			}
		}
	}

	class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// we know already coordinates of first touch
			// we know as well a scroll distance
			sideIndexX = sideIndexX - distanceX;
			sideIndexY = sideIndexY - distanceY;

			// when the user scrolls within our side index
			// we can show for every position in it a proper
			// item in the country list
			if (sideIndexX >= 0 && sideIndexY >= 0) {
				displayListItem(1);
			}

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	public void displayListItem(int state) {
		int itemPosition = 0;

		// compute number of pixels for every side index item
		double pixelPerDisplayedItem = 0;
		int factor = 0;

		switch (state) {
		case 0:
			if (displayedIndexListSize != 0) {
				factor = indexListSize / displayedIndexListSize;
			} else {
				factor = 1;
			}
			pixelPerDisplayedItem = (double) sideIndexHeight / displayedIndexListSize;
			break;
		case 1:
			if (indexListSize != 0) {
				factor = indexListSize / indexListSize;
			} else {
				factor = 1;
			}
			pixelPerDisplayedItem = (double) sideIndexHeight / indexListSize;
			break;
		}

		// compute the item index for given event position belongs to
		if ((sideIndexY % pixelPerDisplayedItem) > 0) {
			if (android.os.Build.VERSION.RELEASE.toString().startsWith("4")) {
				itemPosition = (int) (sideIndexY / pixelPerDisplayedItem);
			} else {
				itemPosition = (int) (sideIndexY / pixelPerDisplayedItem) + 1;
			}
		} else {
			itemPosition = (int) (sideIndexY / pixelPerDisplayedItem);
		}

		int indexMin = 0;
		if (itemPosition <= indexListSize && itemPosition > 0)
			indexMin = Integer.parseInt(indexList.keySet().toArray()[(itemPosition - 1) * factor].toString());

		ListView listView = getListView();
		listView.setSelection(indexMin);
	}

	public void initUI() {
		contactList = (ListView) findViewById(android.R.id.list);
		importButton = (Button) findViewById(R.id.import_button);
		sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
		segmentedButtons = (SegmentedRadioGroup) findViewById(R.id.segmentedButtons);
	}

	public void showImportStats() {
		if (Data.returnedFromContactImport) {
			if ((Data.importStats != null) && (Data.importStats[0] > 0)) {
				Toast.makeText(this.getApplicationContext(),
						"Successfully imported " + Data.importStats[0] + "/" + Data.importStats[2] + " contacts.", Toast.LENGTH_LONG)
						.show();
			}

			if ((Data.importStats != null) && (Data.importStats[1] > 0)) {
				Toast.makeText(this.getApplicationContext(),
						"Failed to import " + Data.importStats[1] + "/" + Data.importStats[2] + " contacts.", Toast.LENGTH_LONG).show();
			}

			Data.returnedFromContactImport = false;
		}
	}

	public class AddNewPersonsToEvent extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_invite_extra");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("token", new StringBody(Data.getToken(getApplicationContext())));
				if (Data.eventForSavingNewInvitedPersons != null) {
					reqEntity.addPart("event_id", new StringBody(String.valueOf(Data.eventForSavingNewInvitedPersons.getEvent_id())));
				}
				if (selectedContacts != null && !selectedContacts.isEmpty()) {
					for (int i = 0, l = selectedContacts.size(); i < l; i++) {
						reqEntity.addPart("contacts[]", new StringBody(String.valueOf(selectedContacts.get(i).contact_id)));
					}
				}
				if (selectedGroups != null && !selectedGroups.isEmpty()) {
					for (int i = 0, l = selectedGroups.size(); i < l; i++) {
						reqEntity.addPart("groups[]", new StringBody(String.valueOf(selectedGroups.get(i).group_id)));
					}
				}
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					HttpResponse rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (!success) {
								Log.e("Adding new contacts to event ERROR", object.getJSONObject("error").getString("reason"));
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/events_invite_extra", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {

			}
			return null;
		}

	}
}