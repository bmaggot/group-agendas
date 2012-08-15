package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.importer.ImportActivity;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
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

//	private ProgressBar pb;

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

	private EditText searchView;

	private ContactsAdapter cAdapter;
	private GroupsAdapter gAdapter;
	
	
	private Button importButton;
	


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		final ListView listView = (ListView) findViewById(R.id.list);
		LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
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
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
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

	public void onResume() {
		super.onResume();
		LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);

		CURRENT_LIST = preferences.getInt("ContactsActivityList", CURRENT_LIST);
		CURRENT_TASK = preferences.getString("ContactsActivityTask", CURRENT_TASK);
		sideIndex.setVisibility(View.GONE);
		
		if (CURRENT_TASK.equals(CONTACTS_TASK)) {
			setListAdapter(cAdapter);

			//TO DO: put this shit into the right place. \m/
			if (dm.loadContacts(this, cAdapter) > 10) {
				indexList = createIndex();
				sideIndex.setVisibility(View.VISIBLE);
			}
		} else if (CURRENT_TASK.equals(GROUPS_TASK)) {
			setListAdapter(gAdapter);
			dm.loadGroups(this, gAdapter);
		}
		
		importButton = (Button) findViewById(R.id.import_button);
		importButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ContactsActivity.this, ImportActivity.class));
			}
		});
		
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

//		Toast.makeText(this, "Loading contacts... wait", Toast.LENGTH_LONG).show();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		editor.remove("ContactsActivityTask");
		editor.remove("ContactsActivityList");
		editor.commit();

		dm = DataManagement.getInstance(this);

		segmentedButtons = (SegmentedRadioGroup) findViewById(R.id.segmentedButtons);
		segmentedButtons.setOnCheckedChangeListener(this);

//		pb = (ProgressBar) findViewById(R.id.progress);

		searchView = (EditText) findViewById(R.id.search);
		searchView.addTextChangedListener(filterTextWatcher);

		mGestureDetector = new GestureDetector(this, new SideIndexGestureListener());
		
		cAdapter = new ContactsAdapter(dm.getContacts(), this);
		gAdapter = new GroupsAdapter(dm.getGroups(), this);
		
		
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
		ArrayList<Contact> contactList = dm.getContacts();
		
		if (contactList != null) {
			TreeMap<Integer, String> tmpIndexList = new TreeMap<Integer, String>();

			String currentLetter = null;
			String strItem = null;

			for (int j = 0; j < contactList.size(); j++) {
				strItem = contactList.get(j).name.toUpperCase();
				if (Character.isLetter(strItem.charAt(0))) {
					currentLetter = strItem.substring(0, 1);

					if (!tmpIndexList.containsValue(currentLetter)) {
						tmpIndexList.put(j, currentLetter);
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

	public void onListItemClick(ListView parent, View v, int position, long id) {
		v.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		v.setDrawingCacheEnabled(false);
		if (Data.newEventPar){
			if (CURRENT_LIST == CONTACTS_LIST) {
				if (Data.selectedContacts.size() > 0){
					for (int i = 0; i < Data.selectedContacts.size(); i++){
						if (Data.selectedContacts.get(i).contact_id == dm.getContacts().get(position).contact_id){
							Data.selectedContacts.remove(i);
							v.setBackgroundColor(Color.WHITE);
							break;
						} else {
							Data.selectedContacts.add(dm.getContacts().get(position));
							v.setBackgroundColor(Color.LTGRAY);
							break;
						}
					}
				} else {
					Data.selectedContacts.add(dm.getContacts().get(position));
					v.setBackgroundColor(Color.LTGRAY);
				}
				
				//Intent contactIntent = new Intent(ContactsActivity.this, ContactInfoActivity.class);
				//StringBuilder sb = new StringBuilder(dm.getContacts().get(position).name).append(" ").append(dm.getContacts().get(position).lastname);
				//contactIntent.putExtra("contactName", sb.toString());
				//contactIntent.putExtra("contactId", dm.getContacts().get(position).contact_id);
				//startActivity(contactIntent);
			} else if (CURRENT_LIST == GROUPS_LIST) {
				Intent groupIntent = new Intent(ContactsActivity.this, GroupContactsActivity.class);
				groupIntent.putExtra("groupName", dm.getGroups().get(position).title);
				groupIntent.putExtra("groupId", dm.getGroups().get(position).group_id);
				startActivity(groupIntent);
			}
		} else {
			if (CURRENT_LIST == CONTACTS_LIST) {
				Intent contactIntent = new Intent(ContactsActivity.this, ContactInfoActivity.class);
				StringBuilder sb = new StringBuilder(dm.getContacts().get(position).name).append(" ").append(dm.getContacts().get(position).lastname);
				contactIntent.putExtra("contactName", sb.toString());
				contactIntent.putExtra("contactId", dm.getContacts().get(position).contact_id);
				startActivity(contactIntent);
			} else if (CURRENT_LIST == GROUPS_LIST) {
				Intent groupIntent = new Intent(ContactsActivity.this, GroupContactsActivity.class);
				groupIntent.putExtra("groupName", dm.getGroups().get(position).title);
				groupIntent.putExtra("groupId", dm.getGroups().get(position).group_id);
				startActivity(groupIntent);
			}
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
//					Toast.makeText(this, getString(R.string.waiting_for_contacts_load), Toast.LENGTH_SHORT).show();
					setListAdapter(cAdapter);

					if (dm.loadContacts(this, cAdapter) > 10)
						sideIndex.setVisibility(View.VISIBLE);

					editor.putString("ContactsActivityTask", CONTACTS_TASK);
					editor.putInt("ContactsActivityList", CONTACTS_LIST);
					editor.commit();
					break;
				
				case R.id.groups:
					CURRENT_LIST = GROUPS_LIST;
					CURRENT_TASK = GROUPS_TASK;
//					Toast.makeText(this, getString(R.string.waiting_for_groups_load), Toast.LENGTH_SHORT).show();
					setListAdapter(gAdapter);
					dm.loadGroups(this, gAdapter);

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
		

	public void displayListItem (int state) {
		int itemPosition = 0;

		// compute number of pixels for every side index item
		double pixelPerDisplayedItem = 0;
		int factor = 0;

		switch (state) {
		case 0:
			if(displayedIndexListSize != 0){
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
		if (itemPosition <= indexListSize)
			indexMin = Integer.parseInt(indexList.keySet().toArray()[(itemPosition - 1) * factor].toString());

		ListView listView = (ListView) getListView();
		listView.setSelection(indexMin);
	}
}