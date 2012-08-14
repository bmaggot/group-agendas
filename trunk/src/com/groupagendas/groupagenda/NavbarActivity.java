package com.groupagendas.groupagenda;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import at.bartinger.list.item.EntryAdapter;
import at.bartinger.list.item.EntryItem;
import at.bartinger.list.item.Item;
import at.bartinger.list.item.SectionItem;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.bog.calendar.app.model.CalendarMonth;
import com.bog.calendar.app.model.CalendarYear;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.calendar.day.DayView;
import com.groupagendas.groupagenda.calendar.week.WeekView;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsActivity;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.AgendaUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

@SuppressLint("ParserError")
public class NavbarActivity extends Activity {

	private DataManagement dm;

	private ProgressDialog progressDialog;

	private QuickAction qa;

	private ActionItem list_search;
	private ActionItem go_date;
	private ActionItem year;
	private ActionItem month;
	private ActionItem week;
	private ActionItem day;
	private ActionItem agenda;
	private ActionItem mini_month;
	private ActionItem today;
	
	private Calendar selectedDate = null;

	static final int PROGRESS_DIALOG = 0;
	// private ProgressThread progressThread;

	private FrameLayout calendarContainer;
	private LayoutInflater mInflater;

	private EditText searchView;
	private EntryAdapter entryAdapter;
	private ViewState viewState;

	private Prefs prefs;

	private boolean dataLoaded = false;
	private int loadPhase = 0;

	public static boolean showInvites = false;

	private class LoadViewTask extends AsyncTask<Void, Integer, Void> {
	
		@Override
		protected void onPreExecute() {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

			// Create a new progress dialog
			progressDialog = new ProgressDialog(NavbarActivity.this);
			// Set the progress dialog to display a horizontal progress bar
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			// Set the dialog message to 'Loading application View, please
			// wait...'
			progressDialog.setMessage(getString(R.string.loading_data));
			// This dialog can't be canceled by pressing the back key
			progressDialog.setCancelable(false);
			// This dialog isn't indeterminate
			progressDialog.setIndeterminate(false);
			// The maximum number of items is 100
			progressDialog.setMax(100);
			// Set the current progress to zero
			progressDialog.setProgress(0);
			// Display the progress dialog
			progressDialog.show();

		}

		// The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) {
		
	
			synchronized (this) {
				
				int total = 0;
			

				if (Data.needToClearData) {
					switch (loadPhase) {
					case 0:
						// Delete old data
						getContentResolver().delete(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, "", null);
						getContentResolver().getType(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI);
						Data.clearData();

						loadPhase++;
						total = 20;
						publishProgress(total);
					case 1: // Load account
						dm.getAccountFromRemoteDb();
						loadPhase++;
						total = 40;
						publishProgress(total);
					case 2:// Load contacts
						dm.getContactsFromRemoteDb(null);
						loadPhase++;
						total = 60;
						publishProgress(total);
					case 3:// Load groups
						dm.getGroupsFromRemoteDb();
						loadPhase++;
						total = 80;
						publishProgress(total);

					case 4: // Load events
						dm.getEventsFromRemoteDb("");
						loadPhase++;
						total = 100;
						publishProgress(total);
					}
				} else {

				}
			}
			return null;
		}

		// Update the progress
		@Override
		protected void onProgressUpdate(Integer... values) {
			// set the current progress of the progress dialog
			progressDialog.setProgress(values[0]);
			switch (values[0]) {
			case 20:
				progressDialog.setMessage(getString(R.string.loading_accaunt));
				break;
			case 40:
				progressDialog.setMessage(getString(R.string.loading_contacts));
				break;
			case 60:
				progressDialog.setMessage(getString(R.string.loading_groups));
				break;
			case 80:
				progressDialog.setMessage(getString(R.string.loading_events));
				break;
			}
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(Void result) {

			progressDialog.dismiss();
			dataLoaded = true;
			switchToView();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dm = DataManagement.getInstance(this);
		
		setContentView(R.layout.actnavbar);
		if (savedInstanceState == null){     //if no selectedDate will be restored we create today's date
			String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			selectedDate = Utils.stringToCalendar(dayStr + " 00:00:00", Utils.date_format);
			selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);
		}
		
		restoreMe(savedInstanceState);
		
		if (!dataLoaded && (progressDialog == null)) {

			new LoadViewTask().execute();
		}

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		calendarContainer = (FrameLayout) findViewById(R.id.calendarContainer);

		//
		// Intent intent = getIntent();
		// if(intent.getBooleanExtra("load_data", false)){
		// showDialog(PROGRESS_DIALOG);
		// TODO
		// DataManagement.updateAppData(5);
		// }

		list_search = new ActionItem();
		list_search.setTitle(getString(R.string.list_search));
		list_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				showListSearchView();
			}
		});

		go_date = new ActionItem();
		go_date.setTitle(getString(R.string.go_to_date));
		go_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				showGoToDateView();
			}
		});

		year = new ActionItem();
		year.setTitle(getString(R.string.year));
		year.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				viewState = ViewState.YEAR;
				showYearView();
			}
		});

		month = new ActionItem();
		month.setTitle(getString(R.string.month));
		month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewState = ViewState.MONTH;
				qa.dismiss();
				showMonthView();
			}
		});

		week = new ActionItem();
		week.setTitle(getString(R.string.week));
		week.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				showWeekView();
			}
		});

		day = new ActionItem();
		day.setTitle(getString(R.string.day));
		day.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				viewState = ViewState.DAY;
				showDayView();
			}
		});

		agenda = new ActionItem();
		agenda.setTitle(getString(R.string.agenda));
		agenda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				showAgendaView();
			}
		});

		mini_month = new ActionItem();
		mini_month.setTitle(getString(R.string.mini_month));
		mini_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				showMiniMonthView();
			}
		});

		today = new ActionItem();
		today.setTitle(getString(R.string.today));
		today.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				showTodayView();

			}
		});

	}

	public void onResume() {
		super.onResume();
		RadioButton radioButton;
		radioButton = (RadioButton) findViewById(R.id.btnCalendar);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnCalendars);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnContacts);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnEvents);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnNewevent);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		if (dataLoaded)
			switchToView();
	}

	@Override
	  protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	     outState.putBoolean("isDataLoaded", dataLoaded);
	     outState.putString("loadPhase", Integer.toString(loadPhase));
	     outState.putString("viewState", "" + viewState);
	     String dateStr = new SimpleDateFormat(Utils.date_format).format(selectedDate.getTime());			
	     outState.putString("selectedDate", dateStr);
	  }
	
	 private void restoreMe(Bundle state) {
		    
		    if (state!=null) {
		      dataLoaded = state.getBoolean("isDataLoaded");
		      loadPhase = state.getInt("loadPhase");
		      viewState = ViewState.getValueByString(state.getString("viewState"));
		      selectedDate = Utils.stringToCalendar(state.getString("selectedDate"), Utils.date_format);
		      selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);  
		    }
		  }

	private void switchToView() {

		if (viewState == null) {
			if (dm.getAccount() != null) {
				String dw = dm.getAccount().setting_default_view;
				
//				TODO isn't that hardcode? :) DEFAULT VIEW SHOULD BE CONSTANT THAT IS SET SOMEWHERE ELSE....
				if (dw == null) dw = "m";
				else if(dw.equalsIgnoreCase("null")) dw = "m";
				
				viewState = ViewState.getValueByString(dw);
			} 
		}
		switch (viewState) {
		case TODAY:
			showTodayView();
			break;

		case DAY:
			showDayView();
			break;

		case WEEK:
			showWeekView();
			break;
		case MONTH:
			showMonthView();
			break;
		case MINI_MONTH:
			showMiniMonthView();
			break;
		case YEAR:
			showYearView();
			break;
		case AGENDA:
			showAgendaView();
			break;
		case GO_TO_DATE:
			showGoToDateView();
			break;
		case LIST_SEARCH:
			showListSearchView();
			break;
		}

	}

	private void showGoToDateView() {
		final Dialog mDateTimeDialog = new Dialog(this);
		final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
		final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
		Calendar c = Calendar.getInstance();
		mDateTimePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		
		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimePicker.clearFocus();
				String dayStr = new SimpleDateFormat("yyyy-MM-dd").format(mDateTimePicker.getCalendar().getTime());
				selectedDate = Utils.stringToCalendar(dayStr + " 00:00:00", Utils.date_format);
				selectedDate.setFirstDayOfWeek(Data.DEFAULT_FIRST_WEEK_DAY);
				mDateTimeDialog.dismiss();
				showDayView();
			}
		});
		
		((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimeDialog.cancel();
			}
		});
		
		((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimePicker.reset();
			}
		});
		
		mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDateTimeDialog.setContentView(mDateTimeDialogView);
		mDateTimePicker.hideTopBar();
		mDateTimeDialog.show();
	}

	private void showListSearchView() {
		new GetAllEventsTask().execute();

	}

	private void showTodayView() {
		Toast.makeText(NavbarActivity.this, getString(R.string.today), Toast.LENGTH_SHORT).show();

	}

	private void showWeekView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_week, calendarContainer);
		WeekView view = (WeekView) calendarContainer.getChildAt(0);
		view.init();

	}

	private void showMiniMonthView() {
		Toast.makeText(NavbarActivity.this, getString(R.string.mini_month), Toast.LENGTH_SHORT).show();

	}

	private void showYearView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_year, calendarContainer);
		new CalendarYear(NavbarActivity.this);

	}

	private void showAgendaView() {
		Toast.makeText(NavbarActivity.this, getString(R.string.agenda), Toast.LENGTH_SHORT).show();

	}

	private void showDayView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_day, calendarContainer);
		DayView view = (DayView) calendarContainer.getChildAt(0);
		view.init();

	}

	private void showMonthView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_month, calendarContainer);
		new CalendarMonth(NavbarActivity.this);

	}

	class GetAllEventsTask extends AsyncTask<Void, ArrayList<Item>, ArrayList<Item>> {

		@Override
		protected ArrayList<Item> doInBackground(Void... arg0) {
			ArrayList<Item> items = new ArrayList<Item>();
			ArrayList<Event> events = AgendaUtils.getActualEvents(NavbarActivity.this, dm.getEventsFromLocalDb());

			String time = "1970-01-01";

			for (int i = 0, l = events.size(); i < l; i++) {
				final Event event = events.get(i);

				final String newtime = Utils.formatDateTime(event.my_time_start, Utils.date_format, "EEE, dd MMMM yyyy");
				if (!time.equals(newtime)) {
					time = newtime;
					items.add(new SectionItem(time));
				}

				items.add(new EntryItem(event));
			}

			return items;
		}

		@Override
		protected void onPostExecute(ArrayList<Item> items) {
			calendarContainer.removeAllViews();
			View view = mInflater.inflate(R.layout.calendar_all, calendarContainer);
			ListView listView = (ListView) view.findViewById(R.id.listView);
			entryAdapter = new EntryAdapter(NavbarActivity.this, items);
			listView.setAdapter(entryAdapter);

			searchView = (EditText) view.findViewById(R.id.search);
			searchView.addTextChangedListener(filterTextWatcher);
			super.onPostExecute(items);
		}

	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s != null && entryAdapter != null) {
				entryAdapter.getFilter().filter(s);
			}
		}

	};

	private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.btnCalendar:
					qa = new QuickAction(buttonView);
					qa.addActionItem(list_search);
					qa.addActionItem(go_date);
					qa.addActionItem(year);
					qa.addActionItem(month);
					qa.addActionItem(week);
					qa.addActionItem(day);
					qa.addActionItem(agenda);
					qa.addActionItem(mini_month);
					qa.addActionItem(today);

					qa.show();
					buttonView.setChecked(false);
					break;
				case R.id.btnCalendars:
					startActivity(new Intent(NavbarActivity.this, CalendarsActivity.class));
					break;
				case R.id.btnContacts:
					startActivity(new Intent(NavbarActivity.this, ContactsActivity.class));
					break;
				case R.id.btnEvents:
					showInvites = true;
					startActivity(new Intent(NavbarActivity.this, EventsActivity.class));
					break;
				case R.id.btnNewevent:
					startActivity(new Intent(NavbarActivity.this, NewEventActivity.class));
					break;
				}
			}
		}
	};

	
	/** Nested class that performs progress calculations (counting) */
	// private class ProgressThread extends Thread {
	// Handler mHandler;
	// final static int STATE_DONE = 0;
	//
	// final static int STATE_RUNNING = 1;
	//
	// @SuppressWarnings("unused")
	// int mState;
	// int total;
	//
	// ProgressThread(Handler h) {
	// mHandler = h;
	// }
	//
	// public void run() {
	// mState = STATE_RUNNING;
	// total = 0;
	//
	// // Delete old data
	// getContentResolver().delete(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI,
	// "", null);
	// getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI,
	// "", null);
	// getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI,
	// "", null);
	// getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI,
	// "", null);
	// getContentResolver().getType(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI);
	// if(Data.needToClearData){
	// Data.clearData();
	// }
	//
	// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	// total = 20;
	// Message msg = mHandler.obtainMessage();
	// msg.arg1 = total;
	// mHandler.sendMessage(msg);
	//
	// // Load account
	// dm.getAccountFromRemoteDb();
	// total = 40;
	// msg = mHandler.obtainMessage();
	// msg.arg1 = total;
	// mHandler.sendMessage(msg);
	//
	// // Load contacts
	// dm.getContactsFromRemoteDb(null);
	// total = 60;
	// msg = mHandler.obtainMessage();
	// msg.arg1 = total;
	// mHandler.sendMessage(msg);
	//
	// // Load groups
	// dm.getGroupsFromRemoteDb();
	// total = 80;
	// msg = mHandler.obtainMessage();
	// msg.arg1 = total;
	// mHandler.sendMessage(msg);
	//
	// // Load events
	// dm.getEventsFromRemoteDb("");
	// total = 100;
	// msg = mHandler.obtainMessage();
	// msg.arg1 = total;
	// mHandler.sendMessage(msg);
	//
	// // View test = calendarContainer.getChildAt(0);
	// // switchToView();
	// }
	//
	// class initContactsScreen extends AsyncTask<Void, ArrayList<Contact>,
	// ArrayList<Item>>{
	//
	// @Override
	// protected ArrayList<Item> doInBackground(Void... params) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	//
	// }
	//
	// class initEventsScreen extends AsyncTask<Void, ArrayList<Event>,
	// ArrayList<Item>>{
	//
	// @Override
	// protected ArrayList<Item> doInBackground(Void... params) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	//
	// }
	//
	// /* sets the current state for the thread,
	// * used to stop the thread */
	// public void setState(int state) {
	// mState = state;
	// }
	// }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public FrameLayout getCalendarContainer() {
		return calendarContainer;
	}

	public Calendar getSelectedDate() {
		return selectedDate;
	}
	
	

}