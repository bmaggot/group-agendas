package com.groupagendas.groupagenda.events;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsAdapter;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.settings.SettingsActivity;
import com.groupagendas.groupagenda.utils.AgendaUtils;

public class EventsActivity extends ListActivity {

	private DataManagement dm;

	private QuickAction qa;
	// types
	private ActionItem shared_event;
	private ActionItem telephone;
	private ActionItem open_event;
	private ActionItem shared_note;
	private ActionItem private_note;
	// status
	private ActionItem new_invites;
	private ActionItem attending;
	private ActionItem rejected;
	private ActionItem pending;

	private EventsAdapter eventsAdapter;

	private TextView topView;
	private ProgressBar pb;

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

		setListAdapter(eventsAdapter);
		dm.loadEvents(this, eventsAdapter);
		
//		if (DataManagement.isLoadEventsData()) {
//			if (NavbarActivity.showInvites) {
//				openNewInvites();
//			}
//		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.events);

		pb = (ProgressBar) findViewById(R.id.progress);

		dm = DataManagement.getInstance(this);

		topView = (TextView) findViewById(R.id.topText);
		
		eventsAdapter = new EventsAdapter(dm.getEvents(), this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);
		Intent intent = new Intent(EventsActivity.this, EventActivity.class);
		Event event = (Event) getListAdapter().getItem(pos);
		intent.putExtra("event_id", event.event_id);
		intent.putExtra("type", event.type);
		intent.putExtra("isNative", event.isNative);
		startActivity(intent);
	}

	public void openNewInvites() {
		changeTitle(getString(R.string.status_4, AgendaUtils.newInvites));
		if (qa != null || !NavbarActivity.showInvites) {
			qa.dismiss();
			NavbarActivity.showInvites = false;
		}
		eventsAdapter.getFilter().filter("4");
		eventsAdapter.setFilter("4");
	}

	private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.btnStatus:

					// status
					new_invites = new ActionItem();
					new_invites.setTitle(getString(R.string.status_4, AgendaUtils.newInvites));
					new_invites.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							openNewInvites();
						}
					});

					rejected = new ActionItem();
					rejected.setTitle(getString(R.string.status_0));
					rejected.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.status_0));
							qa.dismiss();
							eventsAdapter.getFilter().filter("0");
							eventsAdapter.setFilter("0");
						}
					});

					attending = new ActionItem();
					attending.setTitle(getString(R.string.status_1));
					attending.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.status_1));
							qa.dismiss();
							eventsAdapter.getFilter().filter("1");
							eventsAdapter.setFilter("1");
						}
					});

					pending = new ActionItem();
					pending.setTitle(getString(R.string.status_2));
					pending.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.status_2));
							qa.dismiss();
							eventsAdapter.getFilter().filter("2");
							eventsAdapter.setFilter("2");
						}
					});

					qa = new QuickAction(buttonView);
					qa.addActionItem(new_invites);
					qa.addActionItem(rejected);
					qa.addActionItem(attending);
					qa.addActionItem(pending);
					qa.show();
					buttonView.setChecked(false);
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
							eventsAdapter.getFilter().filter(getString(R.string.r_type));
							eventsAdapter.setFilter(getString(R.string.r_type));

						}
					});

					telephone = new ActionItem();
					telephone.setTitle(getString(R.string.t_type));
					telephone.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.t_type));
							qa.dismiss();
							eventsAdapter.getFilter().filter(getString(R.string.t_type));
							eventsAdapter.setFilter(getString(R.string.t_type));
						}
					});

					open_event = new ActionItem();
					open_event.setTitle(getString(R.string.o_type));
					open_event.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.o_type));
							qa.dismiss();
							eventsAdapter.getFilter().filter(getString(R.string.o_type));
							eventsAdapter.setFilter(getString(R.string.o_type));
						}
					});

					shared_note = new ActionItem();
					shared_note.setTitle(getString(R.string.n_type));
					shared_note.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.n_type));
							qa.dismiss();
							eventsAdapter.getFilter().filter(getString(R.string.n_type));
							eventsAdapter.setFilter(getString(R.string.n_type));
						}
					});

					private_note = new ActionItem();
					private_note.setTitle(getString(R.string.p_type));
					private_note.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							changeTitle(getString(R.string.p_type));
							qa.dismiss();
							eventsAdapter.getFilter().filter(getString(R.string.p_type));
							eventsAdapter.setFilter(getString(R.string.p_type));
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
