package com.groupagendas.groupagenda;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.verification.SmsVerificationCodeActivity;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.agenda.AgendaView;
import com.groupagendas.groupagenda.calendar.dayandweek.DayWeekView;
import com.groupagendas.groupagenda.calendar.listnsearch.ListnSearchView;
import com.groupagendas.groupagenda.calendar.minimonth.MiniMonthView;
import com.groupagendas.groupagenda.calendar.month.MonthView;
import com.groupagendas.groupagenda.calendar.year.YearView;
import com.groupagendas.groupagenda.chat.ChatThreadFragment;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsActivity;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.settings.CalendarSettingsFragment;
import com.groupagendas.groupagenda.utils.LanguageCodeGetter;
import com.groupagendas.groupagenda.utils.Utils;

@SuppressLint("ParserError")
public class NavbarActivity extends FragmentActivity {

	private DataManagement dm;

	private ProgressDialog progressDialog;

	private QuickAction qa;

	private ActionItem list_search;
	private ActionItem go_date;
	// private ActionItem year;
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

	// private EditText searchView;
	// private EntryAdapter entryAdapter;
	private ViewState viewState;

	private boolean dataLoaded = false;
	private int loadPhase = 0;

	private boolean resumeDayWeekView;

	private int dayWeekViewShowDays;

	private Account acc;

	public static boolean showInvites = false;
	public static boolean notResponses = true;
	public static boolean uptadeResponresBadge = true;
	public static boolean ifResponsesFirstTime = true;
	public static int newResponsesBadges = 0;
	public static String newPhoneNumber = "newphonenumber";
	public static boolean showVerificationDialog = false;
	public static ArrayList<Event> pollsList;
	public static ArrayList<Event> pollsListToShow = new ArrayList<Event>();
	//public static ArrayList<JSONObject> selectedPollTime = new ArrayList<JSONObject>();
	public static ArrayList<Event> pollsListToDelete = new ArrayList<Event>();
	
	public static ProgressDialog loadingProgressDialog;
	public static boolean smthClicked = false;

	private int mYear = 1970;
	private int mMonth = 0;
	private int mDay = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dm = DataManagement.getInstance(this);
		setContentView(R.layout.actnavbar);

		ifResponsesFirstTime = true;
		notResponses = true;
		
		RadioGroup radiogroup = (RadioGroup) this.findViewById(R.id.radiogroup);
		android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) radiogroup.getLayoutParams();
		params.height = Math.round(getResources().getInteger(R.integer.NAVBAR_HEIGHT) * getResources().getDisplayMetrics().density);

		if (savedInstanceState == null) { // if no selectedDate will be restored
											// we create today's date
			selectedDate = Utils.createNewTodayCalendar();
		}

		restoreMe(savedInstanceState);
		acc = new Account(this);
		// TODO move data loading to approporiate class (Login activity
		// maybe...)
		if (acc.getLatestUpdateUnixTimestamp() > 0) {
			new DataSyncTask().execute();
		} else {
			if (!dataLoaded && (progressDialog == null)){
				new DownLoadAllDataTask().execute();
			}
		}
	}

	@Override
	public void onResume() {
		ifResponsesFirstTime = true;
		notResponses = true;
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter(C2DMReceiver.REFRESH_CHAT_MESSAGES_BADGE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter(C2DMReceiver.REFRESH_INVITE_BADGE));
		calendarContainer = (FrameLayout) findViewById(R.id.calendarContainer);

		super.onResume();
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(NavbarActivity.this);
		}
		RadioButton radioButton;
		radioButton = (RadioButton) findViewById(R.id.btnCalendar);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		radioButton = (RadioButton) findViewById(R.id.btnCalendarSetings);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		radioButton = (RadioButton) findViewById(R.id.btnChatThreads);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		// radioButton = (RadioButton) findViewById(R.id.btnContacts);
		// radioButton.setChecked(false);
		// radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		radioButton = (RadioButton) findViewById(R.id.btnEvents);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnNewevent);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

		// badges
		newEventBadge();
		new ResponsesBadgeSyncTask().execute();
		newMessageBadge();
		// end badges

		if (dataLoaded)
			switchToView();

	}

	private void newEventBadge() {
		TextView logo = (TextView) findViewById(R.id.textLogo);

		ContentResolver cr = getApplicationContext().getContentResolver();
		String where = EventsProvider.EMetaData.EventsMetaData.STATUS + " = " + Invited.PENDING + " AND "
				+ EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + " > strftime('%s000', 'now')";
		Cursor cur = cr.query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);
		int new_invites = cur.getCount() + newResponsesBadges;

		if (new_invites > 0) {
			logo.setVisibility(View.VISIBLE);
			logo.setText("" + new_invites);
		} else {
			logo.setVisibility(View.GONE);
		}

		cur.close();
	}

	private void newMessageBadge() {
		TextView message = (TextView) findViewById(R.id.textChat);

		ContentResolver cr = getApplicationContext().getContentResolver();
		String[] projection = { "SUM(" + EventsProvider.EMetaData.EventsMetaData.NEW_MESSAGES_COUNT + ") AS `sum`" };
		String where2 = EMetaData.EventsMetaData.MESSAGES_COUNT + " > 0 AND " + EMetaData.EventsMetaData.STATUS + " != " + Invited.REJECTED;
		Cursor cur2 = cr.query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, projection, where2, null, null);
		cur2.moveToFirst();
		int new_messages = cur2.getInt(cur2.getColumnIndex("sum"));

		if (new_messages > 0) {
			message.setVisibility(View.VISIBLE);
			message.setText("" + new_messages);
		} else {
			message.setVisibility(View.GONE);
		}

		cur2.close();
	}
	
	public void newResponsesBadge(){		
		Account acc = new Account(getApplicationContext());
		int newResponses = 0;
		String tempResponses = "";
		if (DataManagement.networkAvailable){
			tempResponses = ""+EventManagement.getResponsesFromRemoteDb(getApplicationContext());
			acc.setResponses(tempResponses);
		} else{
			tempResponses = acc.getResponses();
		}
		try {
			JSONObject object = new JSONObject(tempResponses);
			newResponses = object.getInt("count");
			if(uptadeResponresBadge){
				acc.setResponsesBadge(""+newResponses);
				newResponsesBadges = newResponses;
			} else {
				newResponsesBadges += newResponses;
			}
			uptadeResponresBadge = false;
		} catch (Exception ex) {
			Log.e("Badge JSON err", ex.getMessage());
		}
		
		
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isDataLoaded", dataLoaded);
		outState.putInt("loadPhase", loadPhase);
		outState.putString("viewState", "" + viewState);
		if (calendarContainer.getChildAt(0) instanceof AbstractCalendarView) {
			selectedDate = ((AbstractCalendarView) calendarContainer.getChildAt(0)).getDateToResume();
		}
		if (viewState == ViewState.DAY || viewState == ViewState.WEEK) {
			outState.putBoolean("resumeDayWeekView", true);
			outState.putInt("dayWeekViewShowDays", DayWeekView.getDaysToShow());
		}

		String dateStr = new SimpleDateFormat(DataManagement.SERVER_TIMESTAMP_FORMAT).format(selectedDate.getTime());
		outState.putString("selectedDate", dateStr);
	}

	private void restoreMe(Bundle state) {

		if (state != null) {
			dataLoaded = state.getBoolean("isDataLoaded");
			loadPhase = state.getInt("loadPhase");
			viewState = ViewState.getValueByString(state.getString("viewState"));
			selectedDate = Utils.stringToCalendar(getApplicationContext(), state.getString("selectedDate"),
					DataManagement.SERVER_TIMESTAMP_FORMAT);
			selectedDate.setFirstDayOfWeek(CalendarSettings.getFirstDayofWeek());
			if (viewState == ViewState.DAY || viewState == ViewState.WEEK) {
				resumeDayWeekView = state.getBoolean("resumeDayWeekView");
				dayWeekViewShowDays = state.getInt("dayWeekViewShowDays");
			}
		}
	}

	private void switchToView() {

		Resources res = this.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration config = res.getConfiguration();
		config.locale = new Locale(LanguageCodeGetter.getLanguageCode(new Account(this).getLanguage()));
		res.updateConfiguration(config, dm);
		this.initQAitems();

		if (viewState == null)
			viewState = ViewState.getValueByString(CalendarSettings.getDefaultView(this));

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
		case CHAT_THREADS:
			showChatFragment();
			break;
		case CALENDAR_SETTINGS:
			showCalendarSettingsFragment();
			break;
		default:
			showMonthView();
			break;
		}

	}

	private void showGoToDateView() {
		if (selectedDate.get(Calendar.YEAR) > 1969) {
			mYear = selectedDate.get(Calendar.YEAR);
		} else {
			mYear = 1970;
		}
		mMonth = selectedDate.get(Calendar.MONTH);
		mDay = selectedDate.get(Calendar.DAY_OF_MONTH);
		
		new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay).show();
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDate();
		}
	};

	private void updateDate() {
		String dayStr = mYear + "-" + (mMonth+1) + "-" + mDay; // TODO hujov format date
		selectedDate = Utils.stringToCalendar(getApplicationContext(), dayStr + " 00:00:00", DataManagement.SERVER_TIMESTAMP_FORMAT);
		selectedDate.setFirstDayOfWeek(CalendarSettings.getFirstDayofWeek());
		switchToView();
	}

	private void showListSearchView() {
		calendarContainer.removeAllViews();
		mInflater.inflate(R.layout.calendar_listnsearch, calendarContainer);
		ListnSearchView view = (ListnSearchView) calendarContainer.getChildAt(0);
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
				DayWeekView view = (DayWeekView) calendarContainer.getChildAt(0);
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

	// class GetAllEventsTask extends
	// AsyncTask<Void, ArrayList<Item>, ArrayList<Item>> {
	//
	// @Override
	// protected ArrayList<Item> doInBackground(Void... arg0) {
	// ArrayList<Item> items = new ArrayList<Item>();
	// ArrayList<Event> events =
	// EventManagement.getEventsFromLocalDb(NavbarActivity.this, true);
	//
	// String time = "1970-01-01";
	//
	// for (int i = 0, l = events.size(); i < l; i++) {
	// final Event event = events.get(i);
	// // temprorary workaround to build. Solution: remove this unused class
	// final String newtime = "";//Utils.formatDateTime(
	// // event.my_time_start,
	// // DataManagement.SERVER_TIMESTAMP_FORMAT,
	// // "EEE, dd MMMM yyyy");
	// if (!time.equals(newtime)) {
	// time = newtime;
	// items.add(new SectionItem(time));
	// }
	//
	// items.add(new EntryItem(event));
	// }
	//
	// return items;
	// }
	//
	// @Override
	// protected void onPostExecute(ArrayList<Item> items) {
	// calendarContainer.removeAllViews();
	// View view = mInflater.inflate(R.layout.calendar_all,
	// calendarContainer);
	// ListView listView = (ListView) view.findViewById(R.id.listView);
	// entryAdapter = new EntryAdapter(NavbarActivity.this, items);
	// listView.setAdapter(entryAdapter);
	//
	// searchView = (EditText) view.findViewById(R.id.search);
	// searchView.addTextChangedListener(filterTextWatcher);
	// super.onPostExecute(items);
	// }
	//
	// }
	//
	// private TextWatcher filterTextWatcher = new TextWatcher() {
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// if (s != null && entryAdapter != null) {
	// entryAdapter.getFilter().filter(s);
	// }
	// }
	//
	// };

	private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.btnCalendar:
					qa = new QuickAction(buttonView);
					qa.addActionItem(list_search);
					// qa.addActionItem(year);
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
				case R.id.btnCalendarSetings:
					viewState = ViewState.CALENDAR_SETTINGS;
					showCalendarSettingsFragment();
					break;
				case R.id.btnChatThreads:
					viewState = ViewState.CHAT_THREADS;
					showChatFragment();
					break;
				case R.id.btnEvents:
					new StartEventsActivity().execute();
					break;
				case R.id.btnNewevent:
					new StartNewEventActivity().execute();
					break;
				}
			}
		}
	};
	
	private class StartEventsActivity extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			smthClicked = true;
			progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.loading));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			showInvites = true;
			startActivity(new Intent(NavbarActivity.this, EventsActivity.class));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			progressDialog.dismiss();
			smthClicked = false;
		}
		
	}
	
	private class StartNewEventActivity extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			smthClicked = true;
			progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.loading));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Intent intent = new Intent(NavbarActivity.this, NewEventActivity.class);
			View view = calendarContainer.getChildAt(0);
			if (view instanceof AbstractCalendarView) {
				Calendar cal = ((AbstractCalendarView) view).getDateToResume();
				intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR,
						Utils.formatCalendar(cal, DataManagement.SERVER_TIMESTAMP_FORMAT));
			}
			startActivity(intent);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			progressDialog.dismiss();
			smthClicked = false;
		}
		
	}

	public void showChatFragment() {
		Fragment chatFragment = ChatThreadFragment.newInstance();
		FragmentTransaction ft = NavbarActivity.this.getSupportFragmentManager().beginTransaction();
		calendarContainer.removeAllViews();
		ft.add(R.id.calendarContainer, chatFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
	}

	public void showCalendarSettingsFragment() {
		Fragment calendarSttingsFragment = CalendarSettingsFragment.newInstance();
		FragmentTransaction ft = NavbarActivity.this.getSupportFragmentManager().beginTransaction();
		calendarContainer.removeAllViews();
		ft.add(R.id.calendarContainer, calendarSttingsFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
	}

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
		if (viewState.equals(ViewState.CALENDAR_SETTINGS) || viewState.equals(ViewState.CHAT_THREADS)) {
			Account account = new Account(this);
			viewState = ViewState.getValueByString(account.getSetting_default_view());
			this.onResume();
		} else {
			moveTaskToBack(true);
		}
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
			AbstractCalendarView view = (AbstractCalendarView) calendarContainer.getChildAt(0);
			return view.getSwipeGestureDetector().onTouchEvent(ev);
		}
		return false;
	}

	public void setAlarmsToAllEvents() {
		// AlarmReceiver alarm = new AlarmReceiver();
		// TODO Justui V. implement and remove from navbar activity
		// for (Event event : Data.getEvents()) {
		// if (!event.alarm1fired && !event.alarm1.equals("null")) {
		// alarm.SetAlarm(getApplicationContext(),
		// Utils.stringToCalendar(event.alarm1,
		// DataManagement.SERVER_TIMESTAMP_FORMAT).getTimeInMillis(), event, 1);
		// }
		// if (!event.alarm2fired && !event.alarm2.equals("null")) {
		// alarm.SetAlarm(getApplicationContext(),
		// Utils.stringToCalendar(event.alarm2,
		// DataManagement.SERVER_TIMESTAMP_FORMAT).getTimeInMillis(), event, 2);
		// }
		// if (!event.alarm3fired && !event.alarm3.equals("null")) {
		// alarm.SetAlarm(getApplicationContext(),
		// Utils.stringToCalendar(event.alarm3,
		// DataManagement.SERVER_TIMESTAMP_FORMAT).getTimeInMillis(), event, 3);
		// }
		// }
	}

	private class DownLoadAllDataTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

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

				switch (loadPhase) {
				case 0:
					// TODO remove

					loadPhase++;
					total = 10;
					publishProgress(total);
				case 1: // Load account
					Account acc = new Account(NavbarActivity.this);
	
					if (DataManagement.networkAvailable) {
						String ssid = acc.getSessionId();
						acc.clearRemoteAccountData();
						acc.setSessionId(ssid);
						dm.getAccountFromRemoteDb(NavbarActivity.this);
						loadPhase++;
						total = 20;
						publishProgress(total);
					} 
					// NativeCalendarImporter.readCalendar(dm.getmContext());
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

				case 4: // Load event templates
					// if (DataManagement.networkAvailable)
					// dm.getTemplates();
					// else
					// dm.getTemplateFromLocalDb(0);
					loadPhase++;
					total = 60;
					publishProgress(total);

				case 5: // Load events
					if (DataManagement.networkAvailable){
						EventManagement.getEventsFromRemoteDb(NavbarActivity.this, "");
						pollsList = EventManagement.getPollEventsFromLocalDb(NavbarActivity.this);
						//acc.setResponses(""+EventManagement.getResponsesFromRemoteDb(getApplicationContext()));
					}
					loadPhase++;
					total = 80;
					publishProgress(total);

				case 6: // Load chat threads if network available
					if (DataManagement.networkAvailable) {
						dm.getAddressesFromRemoteDb(getApplicationContext());
						ChatManagement.getAllChatMessagesFromRemoteDb(NavbarActivity.this);
					}
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
				// progressDialog.setMessage(getString(R.string.loading_templates));
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

			if(!acc.getPhone1().contentEquals("") && !acc.getPhone1().contentEquals("null") && !acc.getPhone1_verified()){
				showDialogForPhoneVerification(NavbarActivity.this, acc.getPhone1_code()+acc.getPhone1(), "1", "true");
			}
			if(!acc.getPhone2().contentEquals("") && !acc.getPhone2().contentEquals("null") && !acc.getPhone2_verified()){
				showDialogForPhoneVerification(NavbarActivity.this, acc.getPhone2_code()+acc.getPhone2(), "2", "true");
			}
			if(!acc.getPhone3().contentEquals("") && !acc.getPhone3().contentEquals("null") && !acc.getPhone3_verified()){
				showDialogForPhoneVerification(NavbarActivity.this, acc.getPhone3_code()+acc.getPhone3(), "3", "true");
			}
			
			acc.setLatestUpdateTime(Calendar.getInstance());
			progressDialog.dismiss();
			dataLoaded = true;
			switchToView();

			setAlarmsToAllEvents();
			new ResponsesBadgeSyncTask().execute();
		}
	}

	private class DataSyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			DataManagement.synchronizeWithServer(NavbarActivity.this, this, acc.getLatestUpdateUnixTimestamp());
			if (DataManagement.networkAvailable)
				dm.getAccountFromRemoteDb(NavbarActivity.this);
			pollsList = EventManagement.getPollEventsFromLocalDb(NavbarActivity.this);
//			if (DataManagement.networkAvailable){
//				acc.setResponses(""+EventManagement.getResponsesFromRemoteDb(getApplicationContext()));
//			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
//			if(!acc.getPhone1().contentEquals("") && !acc.getPhone1().contentEquals("null") && !acc.getPhone1_verified()){
//				showDialogForPhoneVerification(NavbarActivity.this, acc.getPhone1_code()+acc.getPhone1(), "1", "true");
//			}
//			if(!acc.getPhone2().contentEquals("") && !acc.getPhone2().contentEquals("null") && !acc.getPhone2_verified()){
//				showDialogForPhoneVerification(NavbarActivity.this, acc.getPhone2_code()+acc.getPhone2(), "2", "true");
//			}
//			if(!acc.getPhone3().contentEquals("") && !acc.getPhone3().contentEquals("null") && !acc.getPhone3_verified()){
//				showDialogForPhoneVerification(NavbarActivity.this, acc.getPhone3_code()+acc.getPhone3(), "3", "true");
//			}
			
			acc.setLatestUpdateTime(Calendar.getInstance());
			dataLoaded = true;
			switchToView();

			setAlarmsToAllEvents();
		}

	}
	
	private class ResponsesBadgeSyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			newResponsesBadge();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			newEventBadge();
			
		}

	}

	public void initQAitems() {
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		list_search = new ActionItem();
		list_search.setTitle(getString(R.string.list_search));
		list_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewState = ViewState.LIST_SEARCH;
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

		// year = new ActionItem();
		// year.setTitle(getString(R.string.year));
		// year.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// qa.dismiss();
		// selectedDate = Utils.createNewTodayCalendar();
		// viewState = ViewState.YEAR;
		// showYearView();
		// }
		// });

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

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//acc.setResponses(""+EventManagement.getResponsesFromRemoteDb(getApplicationContext()));
			new ResponsesBadgeSyncTask().execute();
			newEventBadge();
			newMessageBadge();
		}
	};
	
	public static void showDialogForPhoneVerification(final Context context, String number, final String number_id, final String sendConfirmationCode){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.phone_number_verification);
		builder.setMessage(context.getString(R.string.verification_dialog) + " " + number);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.approve, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(context, SmsVerificationCodeActivity.class);
				intent.putExtra(newPhoneNumber, number_id);
				intent.putExtra("send_confirmation_code", sendConfirmationCode);
				context.startActivity(intent);
				
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	
}