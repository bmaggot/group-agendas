package com.groupagendas.groupagenda.calendar.agenda;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.groupagendas.groupagenda.CustomAnimator;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.GenericSwipeAnimator;
import com.groupagendas.groupagenda.calendar.cache.AgendaViewCache;
import com.groupagendas.groupagenda.contacts.birthdays.BirthdayManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.NativeCalendarReader;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class AgendaView extends AbstractCalendarView {

	private static final int FRAMES_PER_ROW = 2;
	private Calendar shownDate;
	private static final int TABLE_ROWS_COUNT = 3;
	public static final int SHOWN_DAYS_COUNT = 7;
	public boolean stillLoading = true;

	private QuickAction qa;
	private ActionItem New;
	private Calendar cal = Calendar.getInstance();

	List<AgendaFrame> daysList = new ArrayList<AgendaFrame>(SHOWN_DAYS_COUNT);

	private TableLayout agendaTable;

	public AgendaView(Context context) {
		this(context, null);
	}

	public AgendaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaultWmNames();
	}

	@Override
	protected void setTopPanel() {
		StringBuilder title = new StringBuilder(getResources().getString(R.string.week));
		title.append(' ').append(shownDate.get(Calendar.WEEK_OF_YEAR));
		getTopPanelTitle().setText(title);
	}

	@Override
	public void goPrev() {
		if (stillLoading)
			return;
		
		ViewParent actNavBar = getParent();
		if (!(actNavBar instanceof CustomAnimator)) {
			GenericSwipeAnimator.startAnimation(this, false, new Runnable() {
				@Override
				public void run() {
					shownDate.add(Calendar.DATE, -SHOWN_DAYS_COUNT);
					setTopPanel();
					setDaysTitles();
					updateEventLists();
				}
			});
			return;
		}
		
		CustomAnimator ca = (CustomAnimator) actNavBar;
		if (!ca.setupAnimator(AgendaViewCache.getInstance(), selectedDate, mInflater, true)) {
			// Log.w(getClass().getSimpleName(), "Attempt to setup an active animator?!");
			return;
		}
	}

	@Override
	public void goNext() {
		if (stillLoading)
			return;
		
		ViewParent actNavBar = getParent();
		if (!(actNavBar instanceof CustomAnimator)) {
			GenericSwipeAnimator.startAnimation(this, true, new Runnable() {
				@Override
				public void run() {
					shownDate.add(Calendar.DATE, SHOWN_DAYS_COUNT);
					setTopPanel();
					setDaysTitles();
					updateEventLists();
				}
			});
			return;
		}
		
		CustomAnimator ca = (CustomAnimator) actNavBar;
		if (!ca.setupAnimator(AgendaViewCache.getInstance(), selectedDate, mInflater, false)) {
			// Log.w(getClass().getSimpleName(), "Attempt to setup an active animator?!");
			return;
		}
	}

	@Override
	public void refresh(Calendar from) {
		if (stillLoading)
			return;
		
		setDaysTitles();
		updateEventLists();
	}

	@Override
	public void setupView() {
		agendaTable = (TableLayout) findViewById(R.id.agenda_table);
		agendaTable.setOnTouchListener(createListener(swipeGestureDetector));
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0f);

		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(VIEW_WIDTH / FRAMES_PER_ROW, VIEW_HEIGHT / TABLE_ROWS_COUNT, 1.0f);

		// Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT - 1; i++) {
			row = (TableRow) mInflater.inflate(R.layout.calendar_agenda_row, null);
			addWorkingDay(row, cellLp);
			addWorkingDay(row, cellLp);
			agendaTable.addView(row, rowLp);
		}
		// Add last row
		row = (TableRow) mInflater.inflate(R.layout.calendar_agenda_row, null);
		addWorkingDay(row, cellLp);
		addWeekend(row, cellLp);
		agendaTable.addView(row, rowLp);

		setDaysTitles();
		updateEventLists();

	}

	private void setDaysTitles() {
		int day = 0;
		for (AgendaFrame frame : daysList) {
			TextView dayTitle = (TextView) frame.getDayContainer().findViewById(R.id.agenda_day_title);
			Calendar tmp = (Calendar) shownDate.clone();
			tmp.add(Calendar.DATE, day);
			day++;

			StringBuilder title = new StringBuilder(WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1]);
			title.append(", ").append(MonthNames[tmp.get(Calendar.MONTH)]);
			title.append(' ').append(tmp.get(Calendar.DAY_OF_MONTH));
			title.append(", ").append(tmp.get(Calendar.YEAR));

			dayTitle.setText(title);
			if (Utils.isToday(tmp)) {
				dayTitle.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			} else {
				dayTitle.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			}
		}

	}

	private void addWeekend(TableRow row, android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout weekEndFrame = new LinearLayout(getContext());
		weekEndFrame.setOrientation(VERTICAL);

		LinearLayout saturday = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_day_container, null);
		weekEndFrame.addView(saturday, params);

		ListView lv = (ListView) saturday.findViewById(R.id.agenda_day_entries);
		final TextView titleTime = (TextView) saturday.findViewById(R.id.agenda_day_title);
		View v = mInflater.inflate(R.layout.calendar_agenda_entry_blank, null);
		lv.addFooterView(v);

		daysList.add(new AgendaFrame(saturday, getContext()));
		addEventLikeIphone(v, titleTime);

		LinearLayout sunday = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_day_container, null);
		weekEndFrame.addView(sunday, params);

		ListView lv2 = (ListView) sunday.findViewById(R.id.agenda_day_entries);
		final TextView titleTime2 = (TextView) sunday.findViewById(R.id.agenda_day_title);
		View v2 = mInflater.inflate(R.layout.calendar_agenda_entry_blank, null);
		lv2.addFooterView(v2);

		daysList.add(new AgendaFrame(sunday, getContext()));
		addEventLikeIphone(v2, titleTime2);

		row.addView(weekEndFrame, cellLp);
	}

	private void addWorkingDay(TableRow row, android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout workingDayFrame = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_day_container, null);
		row.addView(workingDayFrame, cellLp);

		ListView lv = (ListView) workingDayFrame.findViewById(R.id.agenda_day_entries);
		final TextView titleTime = (TextView) workingDayFrame.findViewById(R.id.agenda_day_title);
		View v = mInflater.inflate(R.layout.calendar_agenda_entry_blank, null);
		lv.addFooterView(v);

		daysList.add(new AgendaFrame(workingDayFrame, getContext()));

		addEventLikeIphone(v, titleTime);
	}

	private void addEventLikeIphone(View v, final TextView titleTime) {
		New = new ActionItem();
		New.setTitle(getResources().getString(R.string.New));
		New.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				Calendar calEnd = (Calendar) cal.clone();
				calEnd.add(Calendar.MINUTE, 30);
				Intent intent = new Intent(getContext(), NewEventActivity.class);
				intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_START_CALENDAR,
						Utils.formatCalendar(cal, DataManagement.SERVER_TIMESTAMP_FORMAT));
				intent.putExtra(NewEventActivity.EXTRA_STRING_FOR_END_CALENDAR,
						Utils.formatCalendar(calEnd, DataManagement.SERVER_TIMESTAMP_FORMAT));
				getContext().startActivity(intent);
			}
		});

		v.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				String date = titleTime.getText().toString();
				final String[] splitDate = date.split(",");
				String year = splitDate[2].replace(" ", "");
				String[] splitMonth = splitDate[1].split(" ");
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(splitMonth[2]));
				cal.set(Calendar.YEAR, Integer.parseInt(year));
				cal.set(Calendar.HOUR_OF_DAY, 12);
				cal.set(Calendar.MINUTE, 0);
				cal.clear(Calendar.MILLISECOND);

				for (int i = 0; i < 12; i++) {
					if (splitMonth[1].contains(MonthNames[i])) {
						cal.set(Calendar.MONTH, i);
						break;
					}
				}

				//TODO netrinti!
//				if (cal.get(Calendar.MINUTE) > 30) {
//					cal.clear(Calendar.MINUTE);
//					cal.add(Calendar.HOUR_OF_DAY, 1);
//				} else {
//					cal.clear(Calendar.MINUTE);
//				}

				final TextView time = (TextView) v.findViewById(R.id.agenda_entry_blank_time_placeholder);
				time.setVisibility(View.VISIBLE);
				{
					StringBuilder sbTime = new StringBuilder();
					sbTime.append(cal.get(Calendar.HOUR_OF_DAY));
					sbTime.append(':');
					sbTime.append(cal.get(Calendar.MINUTE));
					if (cal.get(Calendar.MINUTE) < 2)
						sbTime.append('0');
					time.setText(sbTime);
				}

				final TextView title = (TextView) v.findViewById(R.id.agenda_entry_blank_title_placeholder);
				title.setVisibility(View.VISIBLE);
				title.setText(R.string.new_event);
				final ImageView bubble = (ImageView) v.findViewById(R.id.agenda_entry_blank_color_placeholder);
				bubble.setVisibility(View.VISIBLE);
				bubble.setBackgroundDrawable(new BitmapDrawable(DrawingUtils.getColoredRoundRectangle(getContext(), 15,
						Event.DEFAULT_COLOR, true)));
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				int margins = DrawingUtils.convertDPtoPX(2);
				params.setMargins(margins, margins, 0, 0);
				bubble.setLayoutParams(params);

				qa = new QuickAction(title);
				qa.addActionItem(New);
				qa.show();

				qa.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {
						time.setVisibility(View.INVISIBLE);
						title.setVisibility(View.INVISIBLE);
						bubble.setVisibility(View.INVISIBLE);

					}
				});
				return false;
			}
		});
	}

	@Override
	protected void updateEventLists() {
		new UpdateEventsInfoTask().execute();

	}

	@Override
	public Calendar getDateToResume() {
		if (selectedDate.get(Calendar.WEEK_OF_YEAR) == shownDate.get(Calendar.WEEK_OF_YEAR))
			return selectedDate;
		return shownDate;
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		this.shownDate = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfWeek(this.shownDate);

	}

	private class UpdateEventsInfoTask extends AbstractCalendarView.UpdateEventsInfoTask {

		@Override
		protected void onPostExecute(Void result) {
			Calendar tmp = (Calendar) shownDate.clone();
			for (AgendaFrame frame : daysList) {
				frame.setEventList(TreeMapUtils.getEventsFromTreemap(getContext(), tmp, sortedEvents, false));
				tmp.add(Calendar.DATE, 1);
				frame.UpdateList();
			}
			stillLoading = false;

		}
		
		@Override
		protected void onPreExecute() {
			stillLoading = true;
		}

		@Override
		protected Cursor queryProjectionsFromLocalDb(Calendar date) {
			return EventManagement.createEventProjectionByDateFromLocalDb(context, EventProjectionForDisplay, shownDate, 7,
					EventManagement.TM_EVENTS_FROM_GIVEN_DATE, null, true);
		}

		@Override
		protected ArrayList<Event> queryNativeEvents() {
			return NativeCalendarReader.readNativeCalendarEventsForAFewDays(context, shownDate, SHOWN_DAYS_COUNT);
		}

		@Override
		protected ArrayList<Event> queryBirthdayEvents() {
			Calendar cal = (Calendar) shownDate.clone();
			cal.add(Calendar.DAY_OF_YEAR, SHOWN_DAYS_COUNT);
			return BirthdayManagement.readBirthdayEventsForTimeInterval(context, shownDate.getTimeInMillis(), cal.getTimeInMillis());
		}

	}

}
