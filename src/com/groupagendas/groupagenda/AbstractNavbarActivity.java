package com.groupagendas.groupagenda;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.agenda.AgendaView;
import com.groupagendas.groupagenda.calendar.dayandweek.DayWeekView;
import com.groupagendas.groupagenda.calendar.listnsearch.ListnSearchView;
import com.groupagendas.groupagenda.calendar.minimonth.MiniMonthView;
import com.groupagendas.groupagenda.calendar.month.MonthView;
import com.groupagendas.groupagenda.calendar.year.YearView;
import com.groupagendas.groupagenda.chat.ChatThreadActivity;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventsActivity;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.Utils;
import com.ptashek.widgets.datetimepicker.DateTimePicker;

public class AbstractNavbarActivity extends Activity{
	
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

	private FrameLayout calendarContainer;
	private LayoutInflater mInflater;

	private ViewState viewState;

	private boolean dataLoaded = false;
	private boolean resumeDayWeekView;

	private int dayWeekViewShowDays;

	public static boolean showInvites = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DataManagement.getInstance(this);

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

		new Account(this);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
					startActivity(new Intent(AbstractNavbarActivity.this, ChatThreadActivity.class));
					break;
				case R.id.btnContacts:
					Data.newEventPar = false;
					startActivity(new Intent(AbstractNavbarActivity.this, ContactsActivity.class));
					break;
				case R.id.btnEvents:
					showInvites = true;
					startActivity(new Intent(AbstractNavbarActivity.this, EventsActivity.class));
					break;
				case R.id.btnNewevent:
					startActivity(new Intent(AbstractNavbarActivity.this, NewEventActivity.class));
					break;
				}
			}
		}
	};
}
