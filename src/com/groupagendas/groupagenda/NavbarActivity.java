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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import at.bartinger.list.item.EntryAdapter;
import at.bartinger.list.item.EntryItem;
import at.bartinger.list.item.Item;
import at.bartinger.list.item.SectionItem;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.agenda.AgendaView;
import com.groupagendas.groupagenda.calendar.dayandweek.DayWeekView;
import com.groupagendas.groupagenda.calendar.listnsearch.ListnSearchView;
import com.groupagendas.groupagenda.calendar.minimonth.MiniMonthView;
import com.groupagendas.groupagenda.calendar.month.MonthView;
import com.groupagendas.groupagenda.calendar.year.YearView;
import com.groupagendas.groupagenda.chat.ChatThreadActivity;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsActivity;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.templates.TemplatesProvider;
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

	private boolean dataLoaded = false;
	private int loadPhase = 0;

	private boolean resumeDayWeekView;

	private int dayWeekViewShowDays;

	private Account acc;

	public static boolean showInvites = false;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dm = DataManagement.getInstance(this);

		setContentView(R.layout.actnavbar);

		RadioGroup radiogroup = (RadioGroup) this.findViewById(R.id.radiogroup);
		android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) radiogroup
				.getLayoutParams();
		params.height = Math.round(getResources().getInteger(
				R.integer.NAVBAR_HEIGHT)
				* getResources().getDisplayMetrics().density);

		if (savedInstanceState == null) { // if no selectedDate will be restored
											// we create today's date
			selectedDate = Utils.createNewTodayCalendar();
		}

		restoreMe(savedInstanceState);
		acc = new Account(this);

	 if (acc.getLatestUpdateUnixTimestamp() > 0){
			new DataSyncTask().execute();
		}	else {
		if (!dataLoaded && (progressDialog == null))
				new DownLoadAllDataTask().execute();
		}
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//
		// Intent intent = getIntent();
		// if(intent.getBooleanExtra("load_data", false)){
		// showDialog(PROGRESS_DIALOG);
		// TODO wtf is this shit?! (onCreate issue)
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
				selectedDate = Utils.createNewTodayCalendar();
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
				selectedDate = Utils.createNewTodayCalendar();
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
				selectedDate = Utils.createNewTodayCalendar();
				viewState = ViewState.WEEK;
				showWeekView();
			}
		});

		day = new ActionItem();
		day.setTitle(getString(R.string.day));
		day.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				selectedDate = Utils.createNewTodayCalendar();
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
				selectedDate = Utils.createNewTodayCalendar();
				viewState = ViewState.AGENDA;
				showAgendaView();
			}
		});

		mini_month = new ActionItem();
		mini_month.setTitle(getString(R.string.mini_month));
		mini_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				selectedDate = Utils.createNewTodayCalendar();
				viewState = ViewState.MINI_MONTH;
				showMiniMonthView();
			}
		});

		today = new ActionItem();
		today.setTitle(getString(R.string.today));
		today.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				selectedDate = Utils.createNewTodayCalendar();
				switchToView();

			}
		});

	}

	@Override
	public void onResume() {
		calendarContainer = (FrameLayout) findViewById(R.id.calendarContainer);
		
		super.onResume();
		RadioButton radioButton;
		radioButton = (RadioButton) findViewById(R.id.btnCalendar);
		radioButton.setChecked(false);
		radioButton
				.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnChatThreads);
		radioButton.setChecked(false);
		radioButton
				.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnContacts);
		radioButton.setChecked(false);
		radioButton
				.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnEvents);
		radioButton.setChecked(false);
		radioButton
				.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnNewevent);
		radioButton.setChecked(false);
		radioButton
				.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		if (dataLoaded)
			switchToView();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isDataLoaded", dataLoaded);
		outState.putInt("loadPhase", loadPhase);
		outState.putString("viewState", "" + viewState);
		if (calendarContainer.getChildAt(0) instanceof AbstractCalendarView) {
			selectedDate = ((AbstractCalendarView) calendarContainer
					.getChildAt(0)).getDateToResume();
		}
		if (viewState == ViewState.DAY || viewState == ViewState.WEEK) {
			outState.putBoolean("resumeDayWeekView", true);
			outState.putInt("dayWeekViewShowDays", DayWeekView.getDaysToShow());
		}

		String dateStr = new SimpleDateFormat(
				DataManagement.SERVER_TIMESTAMP_FORMAT).format(selectedDate
				.getTime());
		outState.putString("selectedDate", dateStr);
	}

	private void restoreMe(Bundle state) {

		if (state != null) {
			dataLoaded = state.getBoolean("isDataLoaded");
			loadPhase = state.getInt("loadPhase");
			viewState = ViewState
					.getValueByString(state.getString("viewState"));
			selectedDate = Utils.stringToCalendar(
					state.getString("selectedDate"),
					DataManagement.SERVER_TIMESTAMP_FORMAT);
			selectedDate
					.setFirstDayOfWeek(CalendarSettings.getFirstDayofWeek());
			if (viewState == ViewState.DAY || viewState == ViewState.WEEK) {
				resumeDayWeekView = state.getBoolean("resumeDayWeekView");
				dayWeekViewShowDays = state.getInt("dayWeekViewShowDays");
			}
		}
	}

	private void switchToView() {

		if (viewState == null)
			viewState = ViewState.getValueByString(CalendarSettings
					.getDefaultView());

		switch (viewState) {
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
		default:
			showMonthView();
			break;
		}

	}

	private void showGoToDateView() {
		final Dialog mDateTimeDialog = new Dialog(this);
		final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater()
				.inflate(R.layout.date_time_dialog, null);
		final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView
				.findViewById(R.id.DateTimePicker);
		Calendar c = Calendar.getInstance();
		mDateTimePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));

		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mDateTimePicker.clearFocus();
						String dayStr = new SimpleDateFormat("yyyy-MM-dd")
								.format(mDateTimePicker.getCalendar().getTime());
						selectedDate = Utils.stringToCalendar(dayStr
								+ " 00:00:00",
								DataManagement.SERVER_TIMESTAMP_FORMAT);
						selectedDate.setFirstDayOfWeek(CalendarSettings
								.getFirstDayofWeek());
						mDateTimeDialog.dismiss();
						switchToView();
					}
				});

		((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mDateTimeDialog.cancel();
					}
				});

		((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime))
				.setOnClickListener(new OnClickListener() {

					@Override
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
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_listnsearch, calendarContainer);
		ListnSearchView view = (ListnSearchView) calendarContainer
				.getChildAt(0);
		view.init();
	}

	private void showWeekView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_week, calendarContainer);
		if (calendarContainer.getChildAt(0) instanceof DayWeekView) {
			DayWeekView view = (DayWeekView) calendarContainer.getChildAt(0);
			int daysToShow = 0; // if we want to resume with default shown days
								// number we call init with param 0
			if (this.resumeDayWeekView) {
				daysToShow = this.dayWeekViewShowDays;
				resumeDayWeekView = false;
			} else {
				DayWeekView.setDaysToShow(DayWeekView.DEFAULT_DAYS_SHOWN);
			}
			view.init(selectedDate, daysToShow);
		}

	}

	private void showMiniMonthView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_mm, calendarContainer);
		MiniMonthView view = (MiniMonthView) calendarContainer.getChildAt(0);
		view.init(selectedDate);

	}

	private void showYearView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_year, calendarContainer);
		YearView view = (YearView) calendarContainer.getChildAt(0);
		view.init(selectedDate);
	}

	private void showAgendaView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_agenda, calendarContainer);
		AgendaView view = (AgendaView) calendarContainer.getChildAt(0);
		view.init(selectedDate);

	}

	private void showDayView() {
		if (!resumeDayWeekView) { // if there is no need to restore pinching
									// state we show only one day
			calendarContainer.removeAllViews();
			mInflater.inflate(R.layout.calendar_week, calendarContainer);
			if (calendarContainer.getChildAt(0) instanceof DayWeekView) {
				DayWeekView view = (DayWeekView) calendarContainer
						.getChildAt(0);
				int daysToShow = 1;
				DayWeekView.setDaysToShow(daysToShow);
				view.init(selectedDate, daysToShow);
			}
		} else
			showWeekView(); // else we call show weekView, which shows restored
							// number of days

	}

	private void showMonthView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_month, calendarContainer);
		MonthView view = (MonthView) calendarContainer.getChildAt(0);
		view.init(selectedDate);

	}

	class GetAllEventsTask extends
			AsyncTask<Void, ArrayList<Item>, ArrayList<Item>> {

		@Override
		protected ArrayList<Item> doInBackground(Void... arg0) {
			ArrayList<Item> items = new ArrayList<Item>();
			ArrayList<Event> events = EventManagement.getEventsFromLocalDb(NavbarActivity.this, true);

			String time = "1970-01-01";

			for (int i = 0, l = events.size(); i < l; i++) {
				final Event event = events.get(i);
//TODO temprorary workaround to build. Solution: remove this unused class
				final String newtime = "";//Utils.formatDateTime(
//						event.my_time_start,
//						DataManagement.SERVER_TIMESTAMP_FORMAT,
//						"EEE, dd MMMM yyyy");
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
			View view = mInflater.inflate(R.layout.calendar_all,
					calendarContainer);
			ListView listView = (ListView) view.findViewById(R.id.listView);
			entryAdapter = new EntryAdapter(NavbarActivity.this, items);
			listView.setAdapter(entryAdapter);

			searchView = (EditText) view.findViewById(R.id.search);
			searchView.addTextChangedListener(filterTextWatcher);
			super.onPostExecute(items);
		}

	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (s != null && entryAdapter != null) {
				entryAdapter.getFilter().filter(s);
			}
		}

	};

	private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.btnCalendar:
					qa = new QuickAction(buttonView);
					qa.addActionItem(list_search);
					qa.addActionItem(year);
					qa.addActionItem(month);
					qa.addActionItem(mini_month);
					qa.addActionItem(agenda);
					qa.addActionItem(week);
					qa.addActionItem(day);
					qa.addActionItem(go_date);
					qa.addActionItem(today);

					qa.show();
					buttonView.setChecked(false);
					break;
				case R.id.btnChatThreads:
					startActivity(new Intent(NavbarActivity.this, ChatThreadActivity.class));
					break;
				case R.id.btnContacts:
					Data.newEventPar = false;
					startActivity(new Intent(NavbarActivity.this,
							ContactsActivity.class));
					break;
				case R.id.btnEvents:
					showInvites = true;
					startActivity(new Intent(NavbarActivity.this,
							EventsActivity.class));
					break;
				case R.id.btnNewevent:
					startActivity(new Intent(NavbarActivity.this,
							NewEventActivity.class));
					break;
				}
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		if (viewState == ViewState.DAY || viewState == ViewState.WEEK) {
			this.resumeDayWeekView = true;
			this.dayWeekViewShowDays = DayWeekView.getDaysToShow();
		}

	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	public FrameLayout getCalendarContainer() {
		return calendarContainer;
	}

	public Calendar getSelectedDate() {
		return selectedDate;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		if (calendarContainer.getChildAt(0) instanceof AbstractCalendarView) {
			AbstractCalendarView view = (AbstractCalendarView) calendarContainer
					.getChildAt(0);
			return view.getSwipeGestureDetector().onTouchEvent(ev);
		}
		return false;
	}
	
	public void setAlarmsToAllEvents(){
//		AlarmReceiver alarm = new AlarmReceiver();
		//TODO Justui V
//		for (Event event : Data.getEvents()) {
//			if (!event.alarm1fired && !event.alarm1.equals("null")) {
//				alarm.SetAlarm(getApplicationContext(), Utils.stringToCalendar(event.alarm1, DataManagement.SERVER_TIMESTAMP_FORMAT).getTimeInMillis(), event, 1);
//			}
//			if (!event.alarm2fired && !event.alarm2.equals("null")) {
//				alarm.SetAlarm(getApplicationContext(), Utils.stringToCalendar(event.alarm2, DataManagement.SERVER_TIMESTAMP_FORMAT).getTimeInMillis(), event, 2);
//			}
//			if (!event.alarm3fired && !event.alarm3.equals("null")) {
//				alarm.SetAlarm(getApplicationContext(), Utils.stringToCalendar(event.alarm3, DataManagement.SERVER_TIMESTAMP_FORMAT).getTimeInMillis(), event, 3);
//			}
//		}
	}
	
	private class DownLoadAllDataTask extends AsyncTask<Void, Integer, Void> {

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

				if (!Data.needToClearData)
					loadPhase++;
				
				switch (loadPhase) {
					case 0:
						// Delete old data
						getContentResolver().delete(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI,"", null);
						getContentResolver().delete(TemplatesProvider.TMetaData.TemplatesMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, "", null);
						getContentResolver().delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, "", null);
//						getContentResolver().delete(EventsProvider.EMetaData.InvitedMetaData.CONTENT_URI, "", null);
						getContentResolver().getType(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI);

						loadPhase++;
						total = 10;
						publishProgress(total);
					case 1: // Load account
						if (DataManagement.networkAvailable) 
							dm.getAccountFromRemoteDb(NavbarActivity.this);
						else
							new Account(NavbarActivity.this);
//						NativeCalendarImporter.readCalendar(dm.getmContext());
						loadPhase++;
						total = 20;
						publishProgress(total);
					case 2:// Load contacts
						if (DataManagement.networkAvailable) 
							ContactManagement.getContactsFromRemoteDb(NavbarActivity.this, null);
						else
							ContactManagement.getContactsFromLocalDb(NavbarActivity.this, null);
						loadPhase++;
						total = 40;
						publishProgress(total);
					case 3:// Load groups
						if (DataManagement.networkAvailable) 
							ContactManagement.getGroupsFromRemoteDb(NavbarActivity.this, null);
						else
							ContactManagement.getGroupsFromLocalDb(NavbarActivity.this, null);
						loadPhase++;
						total = 50;
						publishProgress(total);

					case 4: // Load event templates TODO load ALL templates while offline.
						if (DataManagement.networkAvailable) 
							dm.getTemplates();
						else
							dm.getTemplateFromLocalDb(0);
						loadPhase++;
						total = 60;
						publishProgress(total);
						
					case 5: // Load events
						if (DataManagement.networkAvailable)
							EventManagement.getEventsFromRemoteDb(NavbarActivity.this, "");
						loadPhase++;
						total = 80;
						publishProgress(total);

					case 6: // Load chat threads if network available TODO load offline
						if (DataManagement.networkAvailable)
//							dm.getChatThreads();
						dm.getAddressesFromRemoteDb();
						loadPhase++;
						total = 100;
						publishProgress(total);
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
			case 0:
				progressDialog.setMessage(getString(R.string.loading_data));
				break;
			case 10:
				progressDialog.setMessage(getString(R.string.loading_account));
				break;
			case 20:
				progressDialog.setMessage(getString(R.string.loading_contacts));
				break;
			case 40:
				progressDialog.setMessage(getString(R.string.loading_groups));
				break;
			case 50:
//				progressDialog.setMessage(getString(R.string.loading_templates));
				break;
			case 60:
				progressDialog.setMessage(getString(R.string.loading_events));
				break;
			case 80:
				progressDialog.setMessage(getString(R.string.loading_chat));
				break;
			case 100:
				progressDialog.setMessage(getString(R.string.loading_complete));
				break;
			}
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(Void result) {
			
			acc.setLatestUpdateTime(Calendar.getInstance());
			progressDialog.dismiss();
			dataLoaded = true;
			switchToView();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			
//			setAlarmsToAllEvents(); TODO Justui V.
		}
	}
	
	private class DataSyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			DataManagement.synchronizeWithServer(NavbarActivity.this, this, acc.getLatestUpdateUnixTimestamp());
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			
			acc.setLatestUpdateTime(Calendar.getInstance());
//			progressDialog.dismiss();
			dataLoaded = true;
			switchToView();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			
//			setAlarmsToAllEvents(); TODO Justui V.
		}

	}

}