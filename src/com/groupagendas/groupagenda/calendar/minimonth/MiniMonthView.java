package com.groupagendas.groupagenda.calendar.minimonth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import com.groupagendas.groupagenda.calendar.agenda.AgendaFrame;
import com.groupagendas.groupagenda.calendar.cache.MiniMonthViewCache;
import com.groupagendas.groupagenda.contacts.birthdays.BirthdayManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.NativeCalendarReader;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class MiniMonthView extends AbstractCalendarView {

	ArrayList<AgendaFrame> daysList = new ArrayList<AgendaFrame>(43);

	private TableLayout miniMonthTable;

	private boolean showWeekTitle = true;

	private QuickAction qa;
	private ActionItem New;
	private Calendar cal = Calendar.getInstance();

	private Calendar firstShownDate;

	private int FRAMES_PER_ROW;

	private int TABLE_ROWS_COUNT;
	public boolean stillLoading = true;

	public MiniMonthView(Context context) {
		this(context, null);
	}

	public MiniMonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initDefaultWmNames();
	}

	@Override
	protected void instantiateTopPanelBottomLine() {
		LinearLayout calendarTopPanelBottomLine = new LinearLayout(getContext());
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		calendarTopPanelBottomLine.setOrientation(LinearLayout.HORIZONTAL);
		calendarTopPanelBottomLine.setLayoutParams(params);
		getTopPanelBottomLine().addView(calendarTopPanelBottomLine);

	}

	@Override
	protected void setTopPanel() {
		int FRAMES_PER_ROW = selectedDate.getMaximum(Calendar.DAY_OF_WEEK);
		int FRAME_WIDTH = VIEW_WIDTH / FRAMES_PER_ROW;
		
		StringBuilder title = new StringBuilder(MonthNames[selectedDate.get(Calendar.MONTH)]);
		title.append(' ').append(selectedDate.get(Calendar.YEAR));
		getTopPanelTitle().setText(title);

		LinearLayout bottomBar = (LinearLayout) getTopPanelBottomLine().getChildAt(0);
		bottomBar.removeAllViews();

		TextView entry;
		Calendar tmp = (Calendar) firstShownDate.clone();
		// add view for every day

		for (int i = 0; i < FRAMES_PER_ROW; i++) {
			LayoutParams cellP = new LayoutParams(FRAME_WIDTH, LayoutParams.WRAP_CONTENT, 1.0f);

			switch (i) {
			case 0:
				if (showWeekTitle) {
					LinearLayout wrapper = (LinearLayout) mInflater.inflate(R.layout.calendar_topbar_bottomline_mm_entry, null);
					wrapper.setLayoutParams(cellP);
					TextView weekTitle = (TextView) wrapper.findViewById(R.id.mm_top_week_title);
					weekTitle.setText(R.string.week_title);
					entry = (TextView) wrapper.findViewById(R.id.mm_top_day_title);
					String text = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1];
					tmp.add(Calendar.DATE, 1);
					entry.setText(text);
					bottomBar.addView(wrapper);
				}
				break;
			default:
				entry = (TextView) mInflater.inflate(R.layout.calendar_top_bar_bottomline_entry, null);
				String text = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1];
				tmp.add(Calendar.DATE, 1);
				entry.setText(text);
				entry.setLayoutParams(cellP);
				bottomBar.addView(entry);
				break;
			}
		}
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
					int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
					selectedDate.add(Calendar.MONTH, -1);
					firstShownDate = updateShownDate();
					setTopPanel();
					if (LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
						paintTable(selectedDate);
					}
					setDaysTitles();
					updateEventLists();
				}
			});
			return;
		}
		
		CustomAnimator ca = (CustomAnimator) actNavBar;
		if (!ca.setupAnimator(MiniMonthViewCache.getInstance(), selectedDate, mInflater, true)) {
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
					int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
					selectedDate.add(Calendar.MONTH, 1);
					firstShownDate = updateShownDate();
					setTopPanel();
					if (LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
						paintTable(selectedDate);
					}
					setDaysTitles();
					updateEventLists();
				}
			});
			return;
		}
		
		CustomAnimator ca = (CustomAnimator) actNavBar;
		if (!ca.setupAnimator(MiniMonthViewCache.getInstance(), selectedDate, mInflater, false)) {
			// Log.w(getClass().getSimpleName(), "Attempt to setup an active animator?!");
			return;
		}
	}

	@Override
	public void refresh(Calendar from) {
		setDaysTitles();
		updateEventLists();
	}

	@Override
	public void setupView() {
		miniMonthTable = (TableLayout) findViewById(R.id.agenda_table);
		paintTable(selectedDate);
		setDaysTitles();
		updateEventLists();
	}

	private void paintTable(Calendar date) {
		miniMonthTable.removeAllViews();
		daysList.clear();

		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0f);

		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(VIEW_WIDTH / FRAMES_PER_ROW, VIEW_HEIGHT / TABLE_ROWS_COUNT, 1.0f);

		// Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT; i++) {
			row = (TableRow) mInflater.inflate(R.layout.calendar_mm_row, null);
			for (int j = 0; j < FRAMES_PER_ROW; j++)
				addDay(row, cellLp);
			miniMonthTable.addView(row, rowLp);
		}
	}

	private void addDay(TableRow row, TableRow.LayoutParams cellLp) {
		LinearLayout dayFrame = (LinearLayout) mInflater.inflate(R.layout.calendar_mm_day_container, null);
		row.addView(dayFrame, cellLp);

		ListView lv = (ListView) dayFrame.findViewById(R.id.agenda_day_entries);
		final TextView titleTime = (TextView) dayFrame.findViewById(R.id.agenda_day_title);
		TextView dayTitle = (TextView) dayFrame.findViewById(R.id.agenda_day_title);
		View v = mInflater.inflate(R.layout.calendar_agenda_entry_blank, null);
		lv.addFooterView(v);

		daysList.add(new AgendaFrame(dayFrame, getContext(), false));

		addEventLikeIphone(v, titleTime, dayTitle);
	}

	private void addEventLikeIphone(View v, final TextView titleTime, final TextView dayTitle) {
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
				Calendar temp = (Calendar) dayTitle.getTag();
				cal.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.YEAR, temp.get(Calendar.YEAR));
				cal.set(Calendar.MONTH, temp.get(Calendar.MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 12);
				cal.set(Calendar.MINUTE, 0);
				cal.clear(Calendar.MILLISECOND);

				// TODO netrinti!
				// if (cal.get(Calendar.MINUTE) > 30) {
				// cal.clear(Calendar.MINUTE);
				// cal.add(Calendar.HOUR_OF_DAY, 1);
				// } else {
				// cal.clear(Calendar.MINUTE);
				// }

				final TextView time = (TextView) v.findViewById(R.id.agenda_entry_blank_time_placeholder);
				time.setVisibility(View.GONE);
				{
					StringBuilder sbTime = new StringBuilder();
					sbTime.append(cal.get(Calendar.HOUR_OF_DAY));
					sbTime.append(':');
					sbTime.append(cal.get(Calendar.MINUTE));
					sbTime.append(0);
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

	private void setDaysTitles() {

		Calendar tmp = (Calendar) firstShownDate.clone();
		int firstDayOfWeek = tmp.getFirstDayOfWeek();

		for (AgendaFrame frame : daysList) {
			TextView dayTitle = (TextView) frame.getDayContainer().findViewById(R.id.agenda_day_title);
			TextView weekNum = (TextView) frame.getDayContainer().findViewById(R.id.agenda_week_title);
			String title = StringValueUtils.valueOf(tmp.get(Calendar.DATE));
			dayTitle.setText(title);
			if (Utils.isToday(tmp)) {
				dayTitle.setBackgroundColor(getResources().getColor(R.color.darker_gray));
				weekNum.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			} else {
				dayTitle.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
				weekNum.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			}

			if (tmp.get(Calendar.DAY_OF_WEEK) == firstDayOfWeek) {
				weekNum.setVisibility(VISIBLE);
				weekNum.setText(StringValueUtils.valueOf(tmp.get(Calendar.WEEK_OF_YEAR)));
			} else {
				frame.getDayContainer().findViewById(R.id.agenda_week_title).setVisibility(GONE);
			}

			dayTitle.setTag(tmp.clone());

			tmp.add(Calendar.DATE, 1);
		}

	}

	@Override
	protected void updateEventLists() {
		new UpdateEventsInfoTask().execute();
	}

	private Calendar updateShownDate() {
		Calendar tmp = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfMonth(tmp);
		Utils.setCalendarToFirstDayOfWeek(tmp);
		return tmp;
	}

	@Override
	public Calendar getDateToResume() {
		if (firstShownDate.get(Calendar.MONTH) != selectedDate.get(Calendar.MONTH)
				|| firstShownDate.get(Calendar.YEAR) != selectedDate.get(Calendar.YEAR)) {
			Calendar tmp = (Calendar) firstShownDate.clone();
			if (tmp.get(Calendar.DATE) != 1)
				tmp.add(Calendar.MONTH, 1);
			tmp.set(tmp.get(Calendar.YEAR), tmp.get(Calendar.MONTH), 1);
			return tmp;
		}
		return selectedDate;
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		FRAMES_PER_ROW = selectedDate.getMaximum(Calendar.DAY_OF_WEEK);
		TABLE_ROWS_COUNT = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		firstShownDate = updateShownDate();

	}

	private class UpdateEventsInfoTask extends AbstractCalendarView.UpdateEventsInfoTask {

		@Override
		protected void onPostExecute(Void result) {
			Calendar tmp = (Calendar) firstShownDate.clone();

			for (AgendaFrame frame : daysList) {
				if (tmp.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)) {
					frame.setEventList(TreeMapUtils.getEventsFromTreemap(tmp, sortedEvents));
				} else {
					List<Event> empty = Collections.emptyList();
					frame.setEventList(empty);
				}
				frame.UpdateList();
				tmp.add(Calendar.DATE, 1);
			}
			stillLoading = false;
		}

		@Override
		protected void onPreExecute() {
			stillLoading = true;
		}

		@Override
		protected Cursor queryProjectionsFromLocalDb(Calendar date) {
			return EventManagement.createEventProjectionByDateFromLocalDb(context, EventProjectionForDisplay, date, 0,
					EventManagement.TM_EVENTS_ON_GIVEN_MONTH, null, true);
		}

		@Override
		protected ArrayList<Event> queryNativeEvents() {
			return NativeCalendarReader.readNativeCalendarEventsForAMonth(context, selectedDate);
		}

		@Override
		protected ArrayList<Event> queryBirthdayEvents() {
			Calendar cal = (Calendar) selectedDate.clone();
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_YEAR, -1);
			return BirthdayManagement.readBirthdayEventsForTimeInterval(context, selectedDate.getTimeInMillis(), cal.getTimeInMillis());
		}

	}

}
