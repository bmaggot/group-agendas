package com.groupagendas.groupagenda.calendar.month;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.CustomAnimator;
import com.groupagendas.groupagenda.EventActivityOnClickListener.TestCondition;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.GenericSwipeAnimator;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.calendar.adapters.MonthAdapter;
import com.groupagendas.groupagenda.contacts.birthdays.BirthdayManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.NativeCalendarReader;
import com.groupagendas.groupagenda.utils.StringValueUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class MonthView extends AbstractCalendarView {

	private static final int MINIMUM_SHOWN_EVENT_ROWS = 4;
	// private static final int WEEK_TITLE_WIDTH_DP = 0;
	private final int TABLE_ROW_HEIGHT = Math.round(44 * densityFactor);
	private Calendar firstShownDate;
	private ListView eventsList;
	private MonthAdapter eventsAdapter;
	private int FRAME_WIDTH;

//	private MonthSwipeListener swipeListener;
//	private GestureDetector swipeDetector;
	private LinearLayout calendarTable;
	private LocalTouchListener localHero;
	private LinearLayout wrapper;

	private TableLayout dayTable;
	List<MonthDayFrame> dayFrames = new ArrayList<MonthDayFrame>(43);
	public boolean stillLoading = true;

	public MonthView(Context context) {
		this(context, null);
	}
	
	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaultWmNames();

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

		StringBuilder title = new StringBuilder(MonthNames[selectedDate.get(Calendar.MONTH)]);
		title.append(' ').append(selectedDate.get(Calendar.YEAR));
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

	public void goPrev(Calendar from, boolean useGivenDate) {
		if (stillLoading)
			return;
		
		ViewParent actNavBar = getParent();
		if (!(actNavBar instanceof CustomAnimator)) {
			GenericSwipeAnimator.startAnimation(this, true, new Runnable() {
				@Override
				public void run() {
					selectedDate.add(Calendar.MONTH, -1);
					updateShownDate();
					setTopPanel();
					repaintTable(selectedDate, dayTable, dayFrames);
					setDayFrames(dayFrames);
					// updateEventLists();
				}
			});
			return;
		}
		
		CustomAnimator ca = (CustomAnimator) actNavBar;
		if (!ca.setupAnimator(from, mInflater, true, useGivenDate)) {
			// Log.w(getClass().getSimpleName(), "Attempt to setup an active animator?!");
			return;
		}
	}

	@Override
	public void goPrev() {
		goPrev(getSelectedDate(), false);
	}

	public void goNext(Calendar from, boolean useGivenDate) {
		if (stillLoading)
			return;
		
		ViewParent actNavBar = getParent();
		if (!(actNavBar instanceof CustomAnimator)) {
			GenericSwipeAnimator.startAnimation(this, true, new Runnable() {
				@Override
				public void run() {
					selectedDate.add(Calendar.MONTH, 1);
					updateShownDate();
					setTopPanel();
					repaintTable(selectedDate, dayTable, dayFrames);
					setDayFrames(dayFrames);
					// updateEventLists();
				}
			});
			return;
		}
		
		CustomAnimator ca = (CustomAnimator) actNavBar;
		if (!ca.setupAnimator(from, mInflater, false, useGivenDate)) {
			// Log.w(getClass().getSimpleName(), "Attempt to setup an active animator?!");
			return;
		}
	}

	@Override
	public void goNext() {
		goNext(getSelectedDate(), false);
	}

	@Override
	public void setupView() {
		dayTable = (TableLayout) findViewById(R.id.month_table);
		repaintTable(selectedDate, dayTable, dayFrames);
		// setDayFrames();

		RelativeLayout topPanel = (RelativeLayout) findViewById(R.id.calendar_navbar);
		topPanel.setOnTouchListener(localHero);

		calendarTable = (LinearLayout) findViewById(R.id.calendar_month_table);
		calendarTable.setOnTouchListener(localHero);

		eventsAdapter = new MonthAdapter(getContext(), null, am_pmEnabled,
				sortedEvents, selectedDate).setInvokeCondition(new TestCondition() {
					@Override
					public boolean test() {
						return !stillLoading;
					}
				});
		eventsList = (ListView) findViewById(R.id.month_list);
		fillBottomSpace();
		eventsList.setAdapter(eventsAdapter);
		// updateEventLists();
		
		setDayFrames(dayFrames);
	}

	private void fillBottomSpace() {
		eventsList.removeFooterView(wrapper);
		float density = getResources().getDisplayMetrics().density;
		android.widget.AbsListView.LayoutParams lParamsW = new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LayoutParams lParamsD = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.round(1*density));
		LayoutParams lParamsV = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.round(40*density));
		wrapper = new LinearLayout(getContext());
		int i = Math.round(MINIMUM_SHOWN_EVENT_ROWS * density);
		
		wrapper.setLayoutParams(lParamsW);
		wrapper.setOrientation(LinearLayout.VERTICAL);
		
		if ((sortedEvents != null) && (selectedDate != null)) {
			Collection<Event> evts = TreeMapUtils.getEventsFromTreemap(selectedDate, sortedEvents);
			if (evts != null) {
				i = i - evts.size();
			}
		}
		
		for (int j = 0; j < i; j++) {
			View v = mInflater.inflate(R.layout.calendar_month_event_list_placeholder, null);
			View d = new View(getContext());
			v.setLayoutParams(lParamsV);
			v.setClickable(false);
			d.setLayoutParams(lParamsD);
			d.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			wrapper.addView(v);
			wrapper.addView(d);
			wrapper.setClickable(false);
			wrapper.setFocusable(false);
		}
		
		eventsList.addFooterView(wrapper);
	}

	@Override
	protected void updateEventLists() {
		ArrayList<Event> eventsList = TreeMapUtils.getEventsFromTreemap(
				selectedDate, sortedEvents);
		eventsAdapter.setList(eventsList);
		eventsAdapter.setSelectedDate(selectedDate, sortedEvents);
		fillBottomSpace();
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

	private void setDayFrames(List<MonthDayFrame> frames) {
		setDayFrames(dayFrames, dayTable, dayTable);
	}

	private void setDayFrames(List<MonthDayFrame> frames, TableLayout oldLayout, TableLayout newLayout) {
		Calendar tmp = (Calendar) firstShownDate.clone();
		for (MonthDayFrame frame : frames) {
			String title = StringValueUtils.valueOf(tmp.get(Calendar.DATE));

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
		new UpdateEventsInfoTask(oldLayout, newLayout, frames).execute();
	}

	private void repaintTable(Calendar date, TableLayout table, List<MonthDayFrame> frames) {
		table.removeAllViews();
		frames.clear();
		
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
			weekNum.setText(StringValueUtils.valueOf(tmp.get(Calendar.WEEK_OF_YEAR)));
			weekNum.setHeight(TABLE_ROW_HEIGHT);
			weekNum.setBackgroundResource(R.drawable.calendar_month_day_inactive);
			month_weeknumbers_container.addView(weekNum);

			row = (TableRow) mInflater.inflate(R.layout.calendar_month_row, null);

			for (int j = 0; j < FRAMES_PER_ROW; j++) {
				addDay(row, cellLp, frames);
				tmp.add(Calendar.DATE, 1);
			}

			table.addView(row, rowLp);
		}
	}

	private void addDay(TableRow row,
			android.widget.TableRow.LayoutParams cellLp,
			List<MonthDayFrame> frames) {
		MonthDayFrame dayFrame = (MonthDayFrame) mInflater.inflate(
				R.layout.calendar_month_day_container, null);

		frames.add(dayFrame);
		row.addView(dayFrame, cellLp);

		dayFrame.setOnTouchListener(localHero);
	}

	private void updateShownDate() {
		firstShownDate = (Calendar) selectedDate.clone();

		Utils.setCalendarToFirstDayOfMonth(firstShownDate);
		Utils.setCalendarToFirstDayOfWeek(firstShownDate);
	}
	
	@Override
	public void setupDelegates() {
		super.setupDelegates();
	}
	
	public void redrawInheritedDate() {
//		MonthViewCache.getInstance().inheritDay(from, selectedDate);
		int selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH);
		for (MonthDayFrame mdf : dayFrames) {
			if (mdf.isOtherMonth())
				continue;
			
			if (mdf.getDayNo() == selectedDay) {
				mdf.setState(MonthCellState.SELECTED);
			} else if (mdf.isSelected()) {
				Calendar tmp = (Calendar) selectedDate.clone();
				tmp.set(Calendar.DAY_OF_MONTH, mdf.getDayNo());
				mdf.setState(Utils.isToday(tmp) ? MonthCellState.TODAY : MonthCellState.DEFAULT);
			}
		}
	}
	
	public void refresh(Calendar from) {
		if (stillLoading)
			return;
		
//		Log.d("MVC", "==== START ==== MV refresh (random events)");
		
		MonthViewCache.getInstance().inheritDay(from, selectedDate);
		
		updateShownDate();
		setTopPanel();
		List<MonthDayFrame> frames;
		TableLayout newLayout;
		if (getParent() instanceof CustomAnimator)
		{
			frames = new ArrayList<MonthDayFrame>(43);
			newLayout = new TableLayout(getContext());
			newLayout.setLayoutParams(dayTable.getLayoutParams());
		} else {
			frames = dayFrames;
			newLayout = dayTable;
		}
		repaintTable(selectedDate, newLayout, frames);
		setDayFrames(frames, dayTable, newLayout);
//		Log.d("MVC", "===== END ===== MV refresh (random events)");
		// updateEventLists();
	}
	
	@Override
	public void onAnimationEnd() {
		super.onAnimationEnd();
		
		ViewParent parent = getParent();
		if (parent instanceof CustomAnimator) {
			((CustomAnimator) parent).onAnimationEnd(this);
		}
	}
	
	private class UpdateEventsInfoTask extends
			AbstractCalendarView.UpdateEventsInfoTask {
		private final TableLayout oldLayout;
		private final TableLayout newLayout;
		private final List<MonthDayFrame> frames;
		
		public UpdateEventsInfoTask(TableLayout oldLayout, TableLayout newLayout, List<MonthDayFrame> frames) {
			this.oldLayout = oldLayout;
			this.newLayout = newLayout;
			this.frames = frames;
		}
		
		@Override
		protected void onPostExecute(Void result) {
//			Calendar calendar = Calendar.getInstance();
//			if (newLayout != oldLayout) {
//				Log.w("MVC", "== S onPostExec == MV refresh (RANDOM EVENTS)");
//			}
			
			updateEventLists();
			Calendar tmp = (Calendar) firstShownDate.clone();
			for (MonthDayFrame frame : frames) {
				// Log.d("FRAME", "HAS: " + frame.hasBubbles());
				if (!frame.hasBubbles) {
					frame.DrawColourBubbles(TreeMapUtils.getEventsFromTreemap(
							tmp, sortedEvents), FRAME_WIDTH);
				}
				tmp.add(Calendar.DATE, 1);
			}
			stillLoading = false;
			DataManagement.monthViewRunning = false;
			if (newLayout != oldLayout) {
				ViewGroup parent = (ViewGroup) oldLayout.getParent();
				parent.removeView(oldLayout);
				parent.invalidate();
				dayTable = newLayout;
				dayFrames = frames;
				parent.addView(newLayout);
//				Log.w("MVC", "== E onPostExec == MV refresh (RANDOM EVENTS)");
			}
//			Log.e("onPostExecute", Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis()+"");
		}

		@Override
		protected void onPreExecute() {
			stillLoading = true;
			DataManagement.monthViewRunning = true;
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
		private static final int SWIPE_MAX_OFF_PATH = 200;
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
				
			    final float xTouch;
		    	final float yTouch;
			    if (event.getHistorySize() == 0) {
			        xTouch = event.getX();
			        yTouch = event.getY();
			    } else {
			    	 xTouch = event.getX(activePointerID);
				     yTouch = event.getY(activePointerID);
			    }
			    
			    if (((firstTouchX == xTouch) && (firstTouchY == yTouch))){
		            ACTION = ACTION_CLICK;
		        } else {
		        	if (dX > 0) {
		        		if ((Math.abs(dX) > SWIPE_MIN_DISTANCE) && (Math.abs(dY) < (SWIPE_MAX_OFF_PATH * getResources().getDisplayMetrics().density))) {
		        			ACTION = ACTION_SWIPE_LTR;
		        		} else {
		                	ACTION = ACTION_CLICK;
		                }
			        } else if (dX < 0) {
		        		if ((Math.abs(dX) > SWIPE_MIN_DISTANCE) && (Math.abs(dY) < (SWIPE_MAX_OFF_PATH * getResources().getDisplayMetrics().density))) {
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
			    final float x;
		    	final float y;
			    if (pointerIndex > 0) {
			        x = event.getX(pointerIndex);
			        y = event.getY(pointerIndex);
			    } else {
			    	x = event.getX();
			    	y = event.getY();
			    }

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
				Calendar old = parentView.getSelectedDate();
				if (event == null) {
					MonthDayFrame frame = (MonthDayFrame) v;
					int clickedDayPos = dayFrames.indexOf(frame);
					old = (Calendar) firstShownDate.clone();
					old.add(Calendar.DATE, clickedDayPos);
				}
				parentView.goPrev(old, true);
				break;
				
			case ACTION_SWIPE_RTL:
				old = parentView.getSelectedDate();
				if (event == null) {
					MonthDayFrame frame = (MonthDayFrame) v;
					int clickedDayPos = dayFrames.indexOf(frame);
					old = (Calendar) firstShownDate.clone();
					old.add(Calendar.DATE, clickedDayPos);
				}
				parentView.goNext(old, true);
				break;
				
			case ACTION_CLICK:
				if (v instanceof MonthDayFrame) {
					if (!stillLoading) {
						MonthDayFrame frame = (MonthDayFrame) v;
						int clickedDayPos = dayFrames.indexOf(frame);

						Calendar clickedDate = (Calendar) firstShownDate.clone();
						clickedDate.add(Calendar.DATE, clickedDayPos);

						if (!frame.isSelected()) {
							if (frame.isOtherMonth()) {
								ViewParent parent = parentView.getParent();
								if (parent instanceof CustomAnimator) {
									ACTION = (clickedDayPos < 7) ? ACTION_SWIPE_LTR : ACTION_SWIPE_RTL;
									onSwipe(v, null);
									return;
								}
							}

//							Log.d("MVC", "==== START ==== MV day click (bugless)");

							selectedDate = clickedDate;
							updateShownDate();

							if (frame.isOtherMonth()) {
								setTopPanel();
								repaintTable(selectedDate, dayTable, dayFrames);
							}

							setDayFrames(dayFrames); // TODO optimize: now all day frames are redrawn
							// updateEventLists();
//							Log.d("MVC", "==== END ==== MV day click (bugless)");
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