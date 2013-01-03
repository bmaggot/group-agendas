package com.groupagendas.groupagenda.calendar.month;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.calendar.adapters.MonthAdapter;
import com.groupagendas.groupagenda.contacts.birthdays.BirthdayManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.NativeCalendarReader;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class MonthView extends AbstractCalendarView {

	// private static final int WEEK_TITLE_WIDTH_DP = 0;
	private final int TABLE_ROW_HEIGHT = Math.round(44 * densityFactor);
	private Calendar firstShownDate;
	ArrayList<MonthDayFrame> daysList = new ArrayList<MonthDayFrame>();
	private TableLayout monthTable;
	private ListView eventsList;
	private MonthAdapter eventsAdapter;
	private int FRAME_WIDTH;
	protected boolean redrawBubbles = true; // indicates whether to redraw color
											// bubbles
	public boolean stillLoading = true;

//	private MonthSwipeListener swipeListener;
//	private GestureDetector swipeDetector;
	private LinearLayout calendarTable;
	private LocalTouchListener localHero;

	public MonthView(Context context) {
		this(context, null);
	}

	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);

		localHero = new LocalTouchListener(this);
	}

	@Override
	protected void instantiateTopPanelBottomLine() {
		LinearLayout calendarTopPanelBottomLine = new LinearLayout(getContext());
		LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		calendarTopPanelBottomLine.setOrientation(LinearLayout.HORIZONTAL);
		calendarTopPanelBottomLine.setLayoutParams(params);
		getTopPanelBottomLine().addView(calendarTopPanelBottomLine);

	}

	@Override
	protected void setTopPanel() {
		int FRAMES_PER_ROW = selectedDate.getMaximum(Calendar.DAY_OF_WEEK);
		int FRAME_WIDTH = VIEW_WIDTH / FRAMES_PER_ROW;

		String title = MonthNames[selectedDate.get(Calendar.MONTH)];
		title += " ";
		title += selectedDate.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);
		this.getTopPanelTitle().setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		this.getTopPanelTitle().setPadding(0, 4, 0, 0);

		LinearLayout bottomBar = (LinearLayout) getTopPanelBottomLine()
				.getChildAt(0);
		bottomBar.removeAllViews();

		TextView entry = (TextView) mInflater.inflate(
				R.layout.calendar_top_bar_bottomline_entry, null);
		entry.setText(R.string.week_title);
		entry.setEms(2);
		bottomBar.addView(entry);

		Calendar tmp = (Calendar) firstShownDate.clone();

		// add view for every day
		int daysPerWeek = firstShownDate.getActualMaximum(Calendar.DAY_OF_WEEK);
		for (int i = 0; i < daysPerWeek; i++) {
			entry = (TextView) mInflater.inflate(
					R.layout.calendar_top_bar_bottomline_entry, null);

			LayoutParams cellP = new LayoutParams(FRAME_WIDTH,
					LayoutParams.WRAP_CONTENT, 1.0f);
			entry.setLayoutParams(cellP);

			String text = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) - 1];
			tmp.add(Calendar.DATE, 1);
			entry.setText(text);
			entry.setGravity(Gravity.CENTER);
			bottomBar.addView(entry);
		}
	}

	@Override
	public void goPrev() {
		if (!stillLoading) {
			redrawBubbles = true;
			selectedDate.add(Calendar.MONTH, -1);
			updateShownDate();
			setTopPanel();
			paintTable(selectedDate);
			setDayFrames();
			updateEventLists();
		}
	}

	@Override
	public void goNext() {
		if (!stillLoading) {
			redrawBubbles = true;
			selectedDate.add(Calendar.MONTH, 1);
			updateShownDate();
			setTopPanel();
			paintTable(selectedDate);

			setDayFrames();
			updateEventLists();
		}
	}

	@Override
	public void setupView() {
		monthTable = (TableLayout) findViewById(R.id.month_table);
		paintTable(selectedDate);
		setDayFrames();

		RelativeLayout topPanel = (RelativeLayout) findViewById(R.id.calendar_navbar);
		topPanel.setOnTouchListener(localHero);

		calendarTable = (LinearLayout) findViewById(R.id.calendar_month_table);
		calendarTable.setOnTouchListener(localHero);

		eventsAdapter = new MonthAdapter(getContext(), null, am_pmEnabled,
				sortedEvents, selectedDate);
		eventsList = (ListView) findViewById(R.id.month_list);
		eventsList.setAdapter(eventsAdapter);
		updateEventLists();
	}

	@Override
	protected void updateEventLists() {
		ArrayList<Event> eventsList = TreeMapUtils.getEventsFromTreemap(
				selectedDate, sortedEvents);
		eventsAdapter.setList(eventsList);
		eventsAdapter.setSelectedDate(selectedDate, sortedEvents);
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		updateShownDate();
	}

	@Override
	public Calendar getDateToResume() {
		return selectedDate;
	}

	private void setDayFrames() {
		new UpdateEventsInfoTask().execute();
		Calendar tmp = (Calendar) firstShownDate.clone();
		for (MonthDayFrame frame : daysList) {
			String title = "" + tmp.get(Calendar.DATE);

			frame.setDayTitle(title);

			MonthCellState state = MonthCellState.DEFAULT;
			if (selectedDate.get(Calendar.MONTH) != tmp.get(Calendar.MONTH)) {
				state = MonthCellState.OTHER_MONTH;
			} else {
				if (Utils.isToday(tmp))
					state = MonthCellState.TODAY;
				if (Utils.isSameDay(tmp, selectedDate))
					state = MonthCellState.SELECTED;
			}

			frame.setState(state);

			tmp.add(Calendar.DATE, 1);
		}
	}

	private void paintTable(Calendar date) {
		monthTable.removeAllViews();
		daysList = new ArrayList<MonthDayFrame>();
		int FRAMES_PER_ROW = date.getMaximum(Calendar.DAY_OF_WEEK);
		int TABLE_ROWS_COUNT = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		FRAME_WIDTH = VIEW_WIDTH / FRAMES_PER_ROW;

		LinearLayout month_weeknumbers_container = (LinearLayout) findViewById(R.id.month_weeknumbers_container);
		month_weeknumbers_container.removeAllViews();

		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0f);

		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(FRAME_WIDTH,
				TABLE_ROW_HEIGHT, 1.0f);

		// Adding rows
		TableRow row;
		Calendar tmp = (Calendar) firstShownDate.clone();
		for (int i = 0; i < TABLE_ROWS_COUNT; i++) {

			TextView weekNum = (TextView) mInflater.inflate(
					R.layout.calendar_month_week_container, null);
			weekNum.setText("" + tmp.get(Calendar.WEEK_OF_YEAR));
			weekNum.setHeight(TABLE_ROW_HEIGHT);
			weekNum.setBackgroundResource(R.drawable.calendar_month_day_inactive);
			month_weeknumbers_container.addView(weekNum);

			row = (TableRow) mInflater.inflate(R.layout.calendar_month_row,
					null);

			for (int j = 0; j < FRAMES_PER_ROW; j++) {
				addDay(row, cellLp);
				tmp.add(Calendar.DATE, 1);
			}

			monthTable.addView(row, rowLp);
		}
	}

	private void addDay(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
		MonthDayFrame dayFrame = (MonthDayFrame) mInflater.inflate(
				R.layout.calendar_month_day_container, null);

		row.addView(dayFrame, cellLp);
		daysList.add(dayFrame);

		dayFrame.setOnTouchListener(localHero);
	}

	private void updateShownDate() {
		firstShownDate = (Calendar) selectedDate.clone();

		Utils.setCalendarToFirstDayOfMonth(firstShownDate);
		Utils.setCalendarToFirstDayOfWeek(firstShownDate);
	}

	private class UpdateEventsInfoTask extends
			AbstractCalendarView.UpdateEventsInfoTask {

		protected void onPostExecute(Void result) {
			updateEventLists();
			Calendar tmp = (Calendar) firstShownDate.clone();
			for (MonthDayFrame frame : daysList) {
				if (!frame.hasBubbles) {
					frame.DrawColourBubbles(TreeMapUtils.getEventsFromTreemap(
							tmp, sortedEvents), FRAME_WIDTH);
				}
				tmp.add(Calendar.DATE, 1);
			}
			stillLoading = false;
		}

		protected void onPreExecute() {
			stillLoading = true;
		}

		@Override
		protected Cursor queryProjectionsFromLocalDb(Calendar date) {
			return EventManagement.createEventProjectionByDateFromLocalDb(
					context, EventProjectionForDisplay, date, 0,
					EventManagement.TM_EVENTS_ON_GIVEN_MONTH, null, true);
		}

		@Override
		protected ArrayList<Event> queryNativeEvents() {
			return NativeCalendarReader.readNativeCalendarEventsForAMonth(
					context, selectedDate);
		}

		@Override
		protected ArrayList<Event> queryBirthdayEvents() {
			Calendar endTime = (Calendar) selectedDate.clone();
			endTime.add(Calendar.MONTH, 1);
			endTime.add(Calendar.DAY_OF_YEAR, -1);
			return BirthdayManagement.readBirthdayEventsForTimeInterval(
					context, selectedDate.getTimeInMillis(),
					endTime.getTimeInMillis());
		}
	}

	private class LocalTouchListener implements OnTouchListener {
		private static final int SWIPE_MIN_DISTANCE = 30;
		private static final int SWIPE_MAX_OFF_PATH = 160;
//		private static final int SWIPE_THRESHOLD_VELOCITY = 20;

		protected MonthView parentView;
		float lastTouchY = 0;
		float lastTouchX = 0;
		float firstTouchY = 0;
		float firstTouchX = 0;
		int activePointerID;
		
		int ACTION = 0;
		final int ACTION_CLICK = 1;
		final int ACTION_SWIPE_LTR = 2;
		final int ACTION_SWIPE_RTL = 3;
		
		public LocalTouchListener(MonthView parent) {
//			Debug.waitForDebugger(); // TODO remove debugger call
			parentView = parent;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			
			switch(action) {
			case MotionEvent.ACTION_UP:
				float dX = 0;
				float dY = 0;
			    
			    lastTouchX = event.getX();
				lastTouchY = event.getY();				
				dX = lastTouchX - firstTouchX;
				dY = lastTouchY - firstTouchY;
			     
			    if (((firstTouchX == event.getX(activePointerID)) && (firstTouchY == event.getY(activePointerID)))){
		            ACTION = ACTION_CLICK;
		        } else {
		        	if (dX > 0) {
		        		if ((Math.abs(dX) > SWIPE_MIN_DISTANCE) && (Math.abs(dY) < SWIPE_MAX_OFF_PATH)) {
		        			ACTION = ACTION_SWIPE_LTR;
		        		} else {
		                	ACTION = ACTION_CLICK;
		                }
			        } else if (dX < 0) {
		        		if ((Math.abs(dX) > SWIPE_MIN_DISTANCE) && (Math.abs(dY) < SWIPE_MAX_OFF_PATH)) {
		        			ACTION = ACTION_SWIPE_RTL;
		        		} else {
		                	ACTION = ACTION_CLICK;
		                }
	                } else {
	                	ACTION = ACTION_CLICK;
	                }
		        }
			    
			    onSwipe(v, event);
				break;
			case MotionEvent.ACTION_DOWN:
				firstTouchX = event.getX();
				firstTouchY = event.getY();
				activePointerID = event.getPointerId(0);
				
				break;
			case MotionEvent.ACTION_MOVE:
			    final int pointerIndex = event.findPointerIndex(activePointerID);
		        final float x = event.getX(pointerIndex);
		        final float y = event.getY(pointerIndex);

		        lastTouchX = x;
		        lastTouchY = y;
				break;
			default:
				break;
			}
			
			return true;
		}
		
		protected void onSwipe(View v, MotionEvent event) {
			switch (ACTION) {
			case ACTION_SWIPE_LTR:
				parentView.goPrev();
				break;
				
			case ACTION_SWIPE_RTL:
				parentView.goNext();
				break;
				
			case ACTION_CLICK:
				if (v instanceof MonthDayFrame) {
					if (!stillLoading) {
						MonthDayFrame frame = (MonthDayFrame) v;
						int clickedDayPos = daysList.indexOf(frame);
	
						Calendar clickedDate = (Calendar) firstShownDate.clone();
						clickedDate.add(Calendar.DATE, clickedDayPos);
	
						if (!frame.isSelected()) {
							selectedDate = clickedDate;
							updateShownDate();
	
							if (frame.isOtherMonth()) {
								redrawBubbles = true;
	
								setTopPanel();
								paintTable(selectedDate);
							}
	
							setDayFrames(); // TODO optimize: now all day frames are redrawn
							updateEventLists();
						}
					}
				}
				break;
				
			default:
				break;
			}
		}
	}
}