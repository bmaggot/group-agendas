package com.groupagendas.groupagenda.events;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.settings.SettingsActivity;

public class EventsActivity extends ListActivity {

	public enum FilterState {
		SHARED_EVENTS, PHONE_CALLS, OPEN_EVENTS, SHARED_NOTES, PRIVATE_NOTES,
		NEW_INVITES, ACCEPTED, REJECTED, MAYBE, ALL

	}

	private QuickAction qa;
	// types
	private ActionItem shared_event;
	private ActionItem telephone;
	private ActionItem open_event;
	private ActionItem shared_note;
	private ActionItem private_note;
	// may be needed in case Rob wants ;] private ActionItem all_types;
	// status
	private ActionItem maybe;
	private ActionItem new_invites;
	private ActionItem attending;
	private ActionItem rejected;
	//private ActionItem all_statuses;
	
	protected FilterState filterState = FilterState.ALL;

	private EventsAdapter eventsAdapter;

	private TextView topView;

	@Override
	public void onResume() {
		super.onResume();
		RadioButton radioButton;
		radioButton = (RadioButton) findViewById(R.id.btnStatus);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnType);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnSettings);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnNewevent);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnContacts);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		
		setListAdapter(eventsAdapter);
		try {
			EventManagement.loadEvents(this, eventsAdapter);
		} catch (Exception e) {
			Log.e("EventsActivity", "load events failed on resume");
		}
		
		//filter events to new invites if there are such
		if(NavbarActivity.showInvites && eventsAdapter.getNewInvitesCount() != 0){
			filterState = FilterState.NEW_INVITES;
			filterEventsByStatus(Invited.PENDING);
			NavbarActivity.showInvites = false;
			changeTitle(getString(R.string.status_new_invites_count, eventsAdapter.getNewInvitesCount()));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.events);

		topView = (TextView) findViewById(R.id.topText);
		
		eventsAdapter = new EventsAdapter(new ArrayList<Event>(), this);
		eventsAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				
				if (new_invites != null)
					new_invites.setTitle(getString(R.string.status_new_invites_count, eventsAdapter.getNewInvitesCount()));
                if (filterState != FilterState.ALL){
                	if (filterState == FilterState.NEW_INVITES) changeTitle(getString(R.string.status_new_invites_count, eventsAdapter.getNewInvitesCount()));			
    				
//        			switch (filterState){
//        			case ACCEPTED:
//        				filterEventsByStatus(Invited.ACCEPTED);
//        				break;
//        			case REJECTED:
//        				filterEventsByStatus(Invited.REJECTED);
//        				break;
//        			case MAYBE:
//        				filterEventsByStatus(Invited.MAYBE);
//        				break;
//        			case NEW_INVITES:
//        				filterEventsByStatus(Invited.PENDING);
//        				break;
//        			case SHARED_NOTES:
//        				filterEventsByType(getString(R.string.n_type));
//        				break;
//        			case OPEN_EVENTS:
//        				filterEventsByType(getString(R.string.o_type));
//        				break;
//        			case PHONE_CALLS:
//        				filterEventsByType(getString(R.string.t_type));
//        				break;
//        			case SHARED_EVENTS:
//        				filterEventsByType(getString(R.string.r_type));
//        				break;
//        			}
//					
				} //else
//    			{
//    				changeTitle(getString(R.string.events));
//    			}
			}

		

		});
	
	
	}
	private void filterEventsByType(String type) {
		eventsAdapter.getFilter().filter(type);
		eventsAdapter.setFilter(type);
		
	}

	private void filterEventsByStatus(int status) {
		eventsAdapter.getFilter().filter("" + status);
		eventsAdapter.setFilter("" + status);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);
		Intent intent = new Intent(EventsActivity.this, EventEditActivity.class);
		Event event = (Event) getListAdapter().getItem(pos);
		intent.putExtra("event_id", event.getInternalID());
		intent.putExtra("type", event.getType());
		intent.putExtra("isNative", event.isNative());
		startActivity(intent);
	}

	private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.btnStatus:

					// status
					new_invites = new ActionItem();
					new_invites.setTitle(getString(R.string.status_new_invites_count, eventsAdapter.getNewInvitesCount()));
					new_invites.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							EventManagement.loadEvents(EventsActivity.this, eventsAdapter);
							changeTitle(getString(R.string.status_new_invites_count, eventsAdapter.getNewInvitesCount()));
							qa.dismiss();
							filterEventsByStatus(Invited.PENDING);
							filterState = FilterState.NEW_INVITES;
							
						}
					});
					


					rejected = new ActionItem();
					rejected.setTitle(getString(R.string.status_not_attending));
					rejected.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							EventManagement.loadEvents(EventsActivity.this, eventsAdapter);
							changeTitle(getString(R.string.status_not_attending));
							qa.dismiss();
							filterEventsByStatus(Invited.REJECTED);
							filterState = FilterState.REJECTED;
						}
					});

					attending = new ActionItem();
					attending.setTitle(getString(R.string.status_attending));
					attending.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							EventManagement.loadEvents(EventsActivity.this, eventsAdapter);
							changeTitle(getString(R.string.status_attending));
							qa.dismiss();
							filterEventsByStatus(Invited.ACCEPTED);
							filterState = FilterState.ACCEPTED;
						
						}
					});

					maybe = new ActionItem();
					maybe.setTitle(getString(R.string.status_maybe));
					maybe.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							EventManagement.loadEvents(EventsActivity.this, eventsAdapter);
							changeTitle(getString(R.string.status_maybe));
							qa.dismiss();
							filterEventsByStatus(Invited.MAYBE);
							filterState = FilterState.MAYBE;
						}
					});

					qa = new QuickAction(buttonView);
					qa.addActionItem(new_invites);
					qa.addActionItem(rejected);
					qa.addActionItem(attending);
					qa.addActionItem(maybe);
					qa.show();
					buttonView.setChecked(false);
					break;
					
				case R.id.btnContacts:
					Data.newEventPar = false;
					startActivity(new Intent(EventsActivity.this,
							ContactsActivity.class));
					break;
					
				case R.id.btnType:

					// types
					shared_event = new ActionItem();
					shared_event.setTitle(getString(R.string.r_type));
					shared_event.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.r_type));
							qa.dismiss();
							filterEventsByType(getString(R.string.r_type));
							filterState = FilterState.SHARED_EVENTS;

						}
					});

					
//					telephone = new ActionItem();
//					telephone.setTitle(getString(R.string.t_type));
//					telephone.setOnClickListener(new OnClickListener() {
//
//						@Override
//						public void onClick(View v) {
//							changeTitle(getString(R.string.t_type));
//							qa.dismiss();
//							eventsAdapter.getFilter().filter(getString(R.string.t_type));
//							eventsAdapter.setFilter(getString(R.string.t_type));
//						}
//					});

					open_event = new ActionItem();
					open_event.setTitle(getString(R.string.o_type));
					open_event.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.o_type));
							qa.dismiss();
							filterState = FilterState.OPEN_EVENTS;
							filterEventsByType(getString(R.string.o_type));
						}
					});

//					shared_note = new ActionItem();
//					shared_note.setTitle(getString(R.string.n_type));
//					shared_note.setOnClickListener(new OnClickListener() {
//
//						@Override
//						public void onClick(View v) {
//							changeTitle(getString(R.string.n_type));
//							qa.dismiss();
//							eventsAdapter.getFilter().filter(getString(R.string.n_type));
//							eventsAdapter.setFilter(getString(R.string.n_type));
//						}
//					});

					private_note = new ActionItem();
					private_note.setTitle(getString(R.string.p_type));
					private_note.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							filterState = FilterState.PRIVATE_NOTES;
							changeTitle(getString(R.string.p_type));
							qa.dismiss();
							filterEventsByType(getString(R.string.p_type));
						}
					});

					qa = new QuickAction(buttonView);
					qa.addActionItem(shared_event);
					qa.addActionItem(telephone);
					qa.addActionItem(open_event);
					qa.addActionItem(shared_note);
					qa.addActionItem(private_note);
					qa.show();
					buttonView.setChecked(false);
					break;
				case R.id.btnSettings:
					startActivity(new Intent(EventsActivity.this, SettingsActivity.class));
					break;
				case R.id.btnNewevent:
					startActivity(new Intent(EventsActivity.this, NewEventActivity.class));
					break;
				}
			}
		}
	};

	private void changeTitle(String text) {
		topView.setText(text);
	}
}
