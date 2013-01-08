package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.importer.ImportActivity;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventActivity;
import com.makeramen.segmented.SegmentedRadioGroup;

public class ContactsActivity extends ListActivity implements OnCheckedChangeListener {
	public ArrayList<Contact> selectedContacts = new ArrayList<Contact>();
	public ArrayList<Group> selectedGroups = new ArrayList<Group>();

	private SegmentedRadioGroup segmentedButtons;
	private ArrayList<Contact> contacts;
	private ArrayList<Group> groups;

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
	private Button clearButton;
	
	private TextView listTitle;

	/*
	 * 0 - Contacts 1 - Groups
	 */
	public static final int LIST_MODE_CONTACTS = 0;
	public static final int LIST_MODE_GROUPS = 1;
	private int LIST_MODE = LIST_MODE_CONTACTS;

	public static final int TASK_MODE_LISTING = 0;
	public static final int TASK_MODE_SELECTION = 1;
	private int TASK_MODE = TASK_MODE_LISTING;
	
	public static final int DEST_EVENT_ACTIVITY = 1;
	public static final int DEST_CONTACT_EDIT = 2;
	public static final int DEST_GROUP_EDIT = 3;
	private int DESTINATION = 0;
	
	public static final String TASK_MODE_KEY = "TASK_MODE";
	public static final String LIST_MODE_KEY = "LIST_MODE";
	public static final String DESTINATION_KEY = "DESTINATION";

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
				tmpTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
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
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(C2DMReceiver.REFRESH_CONTACT_LIST));
		initUI();
		clearButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchView.setText("");
			}
		});

		sideIndex.setVisibility(View.GONE);

		searchView.addTextChangedListener(filterTextWatcher);

		if (getIntent().getExtras() != null) {
			TASK_MODE = getIntent().getExtras().getInt(TASK_MODE_KEY);
			LIST_MODE = getIntent().getExtras().getInt(LIST_MODE_KEY);
			DESTINATION = getIntent().getExtras().getInt(DESTINATION_KEY);
		} else {
			LIST_MODE = preferences.getInt(LIST_MODE_KEY, LIST_MODE);
			TASK_MODE = preferences.getInt(TASK_MODE_KEY, TASK_MODE);
		}
		
		switch (TASK_MODE) {
		case TASK_MODE_SELECTION:
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			getListView().setItemsCanFocus(false);
			
			if (LIST_MODE == LIST_MODE_GROUPS) {
				segmentedButtons.setVisibility(View.INVISIBLE);
				listTitle.setText(R.string.groups);
				listTitle.setVisibility(View.VISIBLE);
			} else {
				segmentedButtons.setVisibility(View.VISIBLE);
				listTitle.setVisibility(View.INVISIBLE);
			}

			if (EventActivity.selectedContacts == null)
				EventActivity.selectedContacts = new ArrayList<Contact>();

			selectedContacts = EventActivity.selectedContacts;
			
			
			switch (DESTINATION) {
			case DEST_EVENT_ACTIVITY:
				if (EventActivity.selectedGroups == null)
					EventActivity.selectedGroups = new ArrayList<Group>();

				selectedGroups = EventActivity.selectedGroups;
				break;
			case DEST_CONTACT_EDIT:
				if (ContactEditActivity.selectedGroups == null)
					ContactEditActivity.selectedGroups = new ArrayList<Group>();

				selectedGroups = ContactEditActivity.selectedGroups;
				break;
			default:
				break;
			}

			importButton.setText(R.string.contact_invite_save_button);
			importButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ArrayList<Contact> selectedContact = null;
					ArrayList<Group> selectedGroup = null;
					if (LIST_MODE == LIST_MODE_CONTACTS) {
						selectedContact = cAdapter.getSelected();

						if (selectedContact != null) {
							switch (DESTINATION) {
							case DEST_EVENT_ACTIVITY:
								EventActivity.selectedContacts = selectedContact;
								for(Contact c : selectedContact){
									if(!c.isUploadedToServer()){
										Toast.makeText(getApplicationContext(), "can't "+c.name+" "+"invite", Toast.LENGTH_SHORT).show();
									}
								}
								break;
							case DEST_GROUP_EDIT:
								GroupEditActivity.selectedContacts = selectedContact;
								GroupEditActivity.changesMade = true;
								break;
							default:
								break;
							}
						}
						ContactsActivity.this.finish();
					} else if (LIST_MODE == LIST_MODE_GROUPS) {
						selectedGroup = gAdapter.getSelected();
						if (selectedGroup != null) {
							switch (DESTINATION) {
							case DEST_EVENT_ACTIVITY:
								EventActivity.selectedGroups = selectedGroup;
								break;
							case DEST_CONTACT_EDIT:
								ContactEditActivity.selectedGroups = selectedGroup;
								ContactEditActivity.changesMade = true;
								break;
							default:
								break;
							}
						}
						ContactsActivity.this.finish();
					}
	
	//					finish();
				}
			});

			break;
		case TASK_MODE_LISTING:
			getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);

			importButton.setText(R.string.contact_import_button);
			importButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(DataManagement.networkAvailable){
						startActivity(new Intent(ContactsActivity.this, ImportActivity.class));
					} else {
						showToast(getResources().getString(R.string.internet_connection_required), Toast.LENGTH_LONG);
					}
				}
			});
			break;

		default:
			TASK_MODE = TASK_MODE_LISTING;
			LIST_MODE = LIST_MODE_CONTACTS;
			break;
		}

		if (LIST_MODE == LIST_MODE_CONTACTS) {
			contacts = ContactManagement.getContactsFromLocalDb(ContactsActivity.this, null);
			cAdapter = new ContactsAdapter(contacts, this, selectedContacts);
			contactList.setAdapter(cAdapter);
			cAdapter.notifyDataSetChanged();

			showImportStats();

			if (contacts.size() > 10) {
				indexList = createIndex();
				sideIndex.setVisibility(View.VISIBLE);
			}
		} else if (LIST_MODE == LIST_MODE_GROUPS) {
			groups = ContactManagement.getGroupsFromLocalDb(ContactsActivity.this, null);
			gAdapter = new GroupsAdapter(groups, this, selectedGroups);
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
		if(TASK_MODE != TASK_MODE_SELECTION){
			editor.putInt(LIST_MODE_KEY, LIST_MODE);
			editor.putInt(TASK_MODE_KEY, TASK_MODE);
			editor.commit();
			searchView.removeTextChangedListener(filterTextWatcher);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contacts);

		// Toast.makeText(this, "Loading contacts... wait",
		// Toast.LENGTH_LONG).show();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		editor.remove(TASK_MODE_KEY);
		editor.remove(LIST_MODE_KEY);
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
				if (LIST_MODE == LIST_MODE_CONTACTS) {
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
		if(TASK_MODE != TASK_MODE_SELECTION){
			editor.putInt(LIST_MODE_KEY, LIST_MODE);
			editor.putInt(TASK_MODE_KEY, TASK_MODE);
			editor.commit();
			searchView.removeTextChangedListener(filterTextWatcher);
		}
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
		switch (TASK_MODE) {
		case TASK_MODE_SELECTION:
			if (LIST_MODE == LIST_MODE_CONTACTS) {
				cAdapter.toggleSelected(Integer.parseInt(v.getTag().toString()));
				v = cAdapter.getView(position, v, parent);
			} else if (LIST_MODE == LIST_MODE_GROUPS) {
				gAdapter.toggleSelected(Integer.parseInt(v.getTag().toString()));
				v = gAdapter.getView(position, v, parent);
			}
			break;
		case TASK_MODE_LISTING:
			Integer c_id = 0;
			if (LIST_MODE == LIST_MODE_CONTACTS) {
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
			} else if (LIST_MODE == LIST_MODE_GROUPS) {
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
				LIST_MODE = LIST_MODE_CONTACTS;

				// Toast.makeText(this,
				// getString(R.string.waiting_for_contacts_load),
				// Toast.LENGTH_SHORT).show();
				setListAdapter(cAdapter);

				if ((contacts != null) && (contacts.size() > 10))
					sideIndex.setVisibility(View.VISIBLE);

				// editor.putString("ContactsActivityTask", CONTACTS_TASK);
				editor.putInt(LIST_MODE_KEY, LIST_MODE_CONTACTS);
				editor.commit();
				break;

			case R.id.groups:
				LIST_MODE = LIST_MODE_GROUPS;
				// Toast.makeText(this,
				// getString(R.string.waiting_for_groups_load),
				// Toast.LENGTH_SHORT).show();
				
				groups = ContactManagement.getGroupsFromLocalDb(ContactsActivity.this, null);
				gAdapter = new GroupsAdapter(groups, this, selectedGroups);
				contactList.setAdapter(gAdapter);
				gAdapter.notifyDataSetChanged();

				// editor.putString("ContactsActivityTask", GROUPS_TASK);
				// editor.putInt("ContactsActivityList", LIST_MODE_GROUPS);
				editor.putInt(LIST_MODE_KEY, LIST_MODE_GROUPS);
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
		clearButton = (Button) findViewById(R.id.clear_button);
		sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
		segmentedButtons = (SegmentedRadioGroup) findViewById(R.id.segmentedButtons);
		listTitle = (TextView) findViewById(R.id.listTitle);
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
	
	public void showToast(String msg, int length){
		Toast.makeText(this, msg, length).show();
	}
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onResume();
		}
	};
}