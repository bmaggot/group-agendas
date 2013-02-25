package com.groupagendas.groupagenda.calendar;

/**
 * @author justinas.marcinka@gmail.com
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TreeMap;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.CustomAnimator;
import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;
import com.groupagendas.groupagenda.utils.TreeMapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public abstract class AbstractCalendarView extends LinearLayout {
	protected TreeMap<String, ArrayList<Event>> sortedEvents;
	protected String[] EventProjectionForDisplay = {
			EventsProvider.EMetaData.EventsMetaData.E_ID,
			EventsProvider.EMetaData.EventsMetaData._ID,
			EventsProvider.EMetaData.EventsMetaData.COLOR,
			EventsProvider.EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR,
			EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY,
			EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS,
			EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS,
			EventsProvider.EMetaData.EventsMetaData.ICON,
			EventsProvider.EMetaData.EventsMetaData.TITLE,
			EventsProvider.EMetaData.EventsMetaData.STATUS,
			EventsProvider.EMetaData.EventsMetaData.IS_OWNER, };
	protected GestureDetector swipeGestureDetector;

	protected Calendar selectedDate;

	public Calendar getSelectedDate() {
		return selectedDate;
	}

	ImageButton prevButton;
	ImageButton nextButton;
	Rect prevButtonBounds;
	Rect nextButtonBounds;
	protected TouchDelegate prevButtonDelegate;
	protected TouchDelegate nextButtonDelegate;
	protected TouchDelegateGroup touchDelegates;
	TextView topPanelTitle;
	private FrameLayout topPanelBottomLineFrame;
	
	protected final int DISPLAY_WIDTH;
	protected final int VIEW_WIDTH;
	protected final int VIEW_HEIGHT;
	protected final float densityFactor;
	{
		Resources r = getResources();
		DisplayMetrics dm = r.getDisplayMetrics();
		densityFactor = dm.density;
		final int headFoot = Math.round((r.getInteger(R.integer.CALENDAR_TOPBAR_HEIGHT) + r.getInteger(R.integer.NAVBAR_HEIGHT)) * densityFactor);
		
		/* Seems not necessary
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		if (wm == null) {
			assert (Build.VERSION.SDK_INT == 0 || isInEditMode());
		*/
			DISPLAY_WIDTH = dm.widthPixels;
			VIEW_HEIGHT = dm.heightPixels - headFoot;
		/* Fun fact: in ADT plugin, d.getWidth() is 0
		} else {
			Display d = wm.getDefaultDisplay();
			DISPLAY_WIDTH = d.getWidth();
			VIEW_HEIGHT = d.getHeight() - headFoot;
		}
		*/
		VIEW_WIDTH = DISPLAY_WIDTH;
	}

	protected LayoutInflater mInflater;

	protected boolean am_pmEnabled;
	protected String[] HourNames;
	protected String[] WeekDayNamesShort;
	protected String[] WeekDayNames;
	protected String[] MonthNames;

	protected DateTimeUtils dtUtils;

	protected abstract void setTopPanel(); // Sets up top panel title text in
											// every view differently

	public abstract void goPrev(); // switch to prev View

	public abstract void goNext(); // switch to next View

	public abstract void setupView(); // setup specific part of view

	protected abstract void updateEventLists();

	protected abstract void setupSelectedDate(Calendar initializationDate); // method
																			// to
																			// set
																			// up
																			// date
																			// that
																			// will
																			// be
																			// used
																			// for
																			// calendar

	public abstract Calendar getDateToResume(); // returns date that should be
												// saved in Activity instance
												// state

	/**
	 * @return puts Top Panel Bottom Line view into frame. Child classes which
	 *         need it, must override this method;
	 **/
	protected void instantiateTopPanelBottomLine() {
		return;
	}

	public AbstractCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		dtUtils = new DateTimeUtils(context);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		am_pmEnabled = CalendarSettings.isUsing_AM_PM(context);
		
		if (isInEditMode()) {
			HourNames = new String[24];
			Arrays.fill(HourNames, "XX");
		} else if (am_pmEnabled) {
			HourNames = getResources().getStringArray(R.array.hour_names_am_pm);
		} else {
			HourNames = getResources().getStringArray(R.array.hour_names);
		}
		
		/*{ In a real device, the values are equal; need proof-of-concept for side effects, if any
			Resources r = getResources();
			DisplayMetrics dm = r.getDisplayMetrics();
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			if (wm != null) {
				Display d = wm.getDefaultDisplay();
				Log.e("CalendarView", "WM Default Display width: " + d.getWidth() + " height: " + d.getHeight());
				Log.e("CalendarView", "DisplayMetrics px width: " + dm.widthPixels + " height: " + dm.heightPixels);
			}
		}*/
		
		// TODO Set calendar_top_bar height in code
		// RelativeLayout topPanel = (RelativeLayout)
		// this.findViewById(R.layout.calendar_top_bar);
		// LinearLayout.LayoutParams params = new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		// Math.round(getResources().getInteger(R.integer.CALENDAR_TOPBAR_HEIGHT)*
		// densityFactor));
		// topPanel.setLayoutParams(params);
		// topP
	}

	public AbstractCalendarView(Context context) {
		this(context, null);

	}

	public void refresh(Calendar previous) {
		// overridden by cached elements
	}

	@Override
	public void onAnimationEnd() {
		super.onAnimationEnd();
		
		ViewParent parent = getParent();
		if (parent instanceof CustomAnimator) {
			((CustomAnimator) parent).onAnimationEnd(this);
		}
	}

	public TextView getTopPanelTitle() {
		return topPanelTitle;
	}

	public FrameLayout getTopPanelBottomLine() {
		return topPanelBottomLineFrame;
	}

	public void init(Calendar initializationDate, boolean onScreen) {
		setupSelectedDate(initializationDate);
		setupTopPanel(onScreen);
		setUpSwipeGestureListener();
		setupView();
	}
	
	public void init(Calendar initializationDate) {
		init(initializationDate, true);
	}

	private final void setupTopPanel(boolean onScreen) {

		prevButton = (ImageButton) findViewById(R.id.prevView);
		nextButton = (ImageButton) findViewById(R.id.nextView);
		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
		topPanelBottomLineFrame = (FrameLayout) findViewById(R.id.top_bar_bottom_line_frame);
		instantiateTopPanelBottomLine();

		prevButtonBounds = new Rect();
		nextButtonBounds = new Rect();

		setTopPanel();

		prevButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goPrev();
			}
		});

		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goNext();
			}
		});

		if (onScreen)
			setupDelegates();

	}

	// requires view to be on screen
	public void setupDelegates() {
		ViewParent parent = prevButton.getParent();
		if (!(parent instanceof View))
			return;
		
		View topBar = (View) parent;
		if (topBar.getTouchDelegate() != null)
			return;
		
		// Debug.waitForDebugger();
		int[] tmpCoords = new int[2];
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		View calNavbar = findViewById(R.id.calendar_navbar);
		calNavbar.getLocationOnScreen(tmpCoords);
		prevButton.getHitRect(prevButtonBounds);
		prevButtonBounds.right = tmpCoords[0] + 100;
		prevButtonBounds.left = tmpCoords[0];
		prevButtonBounds.top = tmpCoords[1];
		prevButtonBounds.bottom = tmpCoords[1] + 50;
		prevButtonDelegate = new TouchDelegate(prevButtonBounds, prevButton);

		nextButton.getHitRect(nextButtonBounds);
		nextButtonBounds.right = tmpCoords[0] + screenWidth;
		nextButtonBounds.left = tmpCoords[0] + screenWidth - 100;
		nextButtonBounds.top = tmpCoords[1];
		nextButtonBounds.bottom = tmpCoords[1] + 50;
		nextButtonDelegate = new TouchDelegate(nextButtonBounds, nextButton);

		touchDelegates = new TouchDelegateGroup(calNavbar);
		touchDelegates.addTouchDelegate(prevButtonDelegate);
		touchDelegates.addTouchDelegate(nextButtonDelegate);
		
		topBar.setTouchDelegate(touchDelegates);
	}

	protected void setUpSwipeGestureListener() {
		if (swipeGestureDetector == null) {
			swipeGestureDetector = new GestureDetector(
					new SwipeOnGestureListener(this));
			this.setOnTouchListener(createListener(swipeGestureDetector));
		}
	}

	protected OnTouchListener createListener(final GestureDetector swipeGestureDetector) {
		return new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (swipeGestureDetector.onTouchEvent(event)) {
					return false;
				} else {
					return true;
				}
			}
		};
	}

	public GestureDetector getSwipeGestureDetector() {
		return swipeGestureDetector;
	}

	protected void initDefaultWmNames() {
		if (isInEditMode()) {
			WeekDayNames = new String[7];
			Arrays.fill(WeekDayNames, "Day");
			MonthNames = new String[12];
			Arrays.fill(MonthNames, "Mon");
		} else {
			WeekDayNames = getResources().getStringArray(R.array.week_days_short);
			MonthNames = getResources().getStringArray(R.array.month_names);
		}
	}

	/**
	 * Class for getting events display info from local database and displaying
	 * it.<br>
	 * Pay attention to override following methods:<br>
	 * - protected void onPostExecute(Void result)<br>
	 * - protected queryProjectionsFromLocalDb(Calendar date) <br>
	 * - protected ArrayList<Event> queryNativeEvents() <br>
	 * 
	 * @author justinas.marcinka@gmail.com
	 * 
	 */
	protected abstract class UpdateEventsInfoTask extends
			AsyncTask<Void, Integer, Void> {
		protected Context context = AbstractCalendarView.this.getContext();

		/**
		 * Returns event projection in: id, color, display color, icon, title,
		 * start and end calendars. Other fields are not initialized
		 * 
		 * @author justinas.marcinka@gmail.com
		 * @param date
		 * @return
		 */
		protected ArrayList<Event> getEventProjectionsForDisplay(Calendar date) {
			ArrayList<Event> list;

			Cursor result = queryProjectionsFromLocalDb(date);
			list = createEventsListFromCursor(result);
			result.close();
			return list;

		}

		/**
		 * Gets events from both local database and native calendar.
		 * 
		 * @author justinas.marcinka@gmail.com
		 */
		@Override
		protected final Void doInBackground(Void... params) {
			if(NavbarActivity.doUneedSleep){
				try{ Thread.sleep(1200); }catch(InterruptedException e){ e.printStackTrace(); }
			}
			// Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE);
			// Calendar calendar = Calendar.getInstance();
			long start = System.nanoTime();
			Account account = new Account(context);
			sortedEvents = new TreeMap<String, ArrayList<Event>>();
			if (account.getShow_ga_calendars()) {
				ArrayList<Event> events = getEventProjectionsForDisplay(selectedDate);
				ArrayList<Event> pollEvents = NavbarActivity.pollsList;
				if(pollEvents != null){
					for (Event event : pollEvents) {
						TreeMapUtils.putNewEventPollsIntoTreeMap(context, sortedEvents,
								event);
					}
				}
				if(events != null){
//					Log.d("ADDING GA EVENTS", "= START To map " + sortedEvents + ", total: " + events.size());
					for (Event event : events) {
						TreeMapUtils.putNewEventIntoTreeMap(context, sortedEvents,
								event);
					}
//					Log.d("ADDING GA EVENTS", "=== END To map " + sortedEvents + ", total: " + events.size());
				}
			}
			Log.e("End Loading GA", StringValueUtils.valueOf((System.nanoTime() - start) / 1000000));
			// Log.e("End Loading GA", Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis()+"");
			// calendar = Calendar.getInstance();
			start = System.nanoTime();
			if (account.getShow_native_calendars()) {
				ArrayList<Event> nativeEvents = queryNativeEvents();
				for (Event nativeEvent : nativeEvents) {
					TreeMapUtils.putNativeEventsIntoTreeMap(context, sortedEvents, nativeEvent);
				}
			}
			Log.e("End Loading NATIVE", StringValueUtils.valueOf((System.nanoTime() - start) / 1000000));
			// Log.e("End Loading NATIVE", Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis()+"");
			// calendar = Calendar.getInstance();
			start = System.nanoTime();
			if (account.getShow_birthdays_calendars()) {
				ArrayList<Event> birthdayEvents = queryBirthdayEvents();
				for (Event birthdayEvent : birthdayEvents) {
					TreeMapUtils.putNewEventIntoTreeMap(context, sortedEvents,
							birthdayEvent);
				}
			}
			Log.e("End Loading BIRTHDAYS", StringValueUtils.valueOf((System.nanoTime() - start) / 1000000));
			// Log.e("End Loading BIRTHDAYS", Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis()+"");
			/*
			 * if(account.getShow_ga_calendars()){ sortedEvents =
			 * TreeMapUtils.sortEvents(context,
			 * getEventProjectionsForDisplay(selectedDate)); }
			 * if(account.getShow_native_calendars()){ ArrayList<Event>
			 * nativeEvents = queryNativeEvents(); if(sortedEvents == null){
			 * sortedEvents = new TreeMap<Calendar, ArrayList<Event>>(); } else
			 * if(!account.getShow_ga_calendars()) { sortedEvents.clear(); }
			 * for(Event nativeEvent : nativeEvents){
			 * TreeMapUtils.putNewEventIntoTreeMap(context, sortedEvents,
			 * nativeEvent); } }
			 */
			// Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			return null;
		}

		private ArrayList<Event> createEventsListFromCursor(Cursor result) {
			ArrayList<Event> list = new ArrayList<Event>(result.getCount());
			while (result.moveToNext()) {
				Event eventProjection = new Event();
				eventProjection
						.setInternalID(result.getLong(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData._ID)));
				eventProjection
						.setEvent_id(result.getInt(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.E_ID)));
				eventProjection
						.setTitle(result.getString(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TITLE)));
				eventProjection
						.setIcon(result.getString(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.ICON)));
				eventProjection
						.setColor(result.getString(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.COLOR)));
				eventProjection
						.setDisplayColor(result.getString(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.EVENT_DISPLAY_COLOR))); // 2012-10-29
				
				
				if (!DataManagement.monthViewRunning) {
					String user_timezone = CalendarSettings.getTimeZone(context);
					long timeinMillis = result.getLong(result
							.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
					eventProjection.setStartCalendar(Utils.createCalendar(timeinMillis, user_timezone));
					timeinMillis = result.getLong(result
							.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
					eventProjection.setEndCalendar(Utils.createCalendar(timeinMillis, user_timezone));
				} else {
					if (result.getColumnIndex(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_TIME_START) > 0) {
						eventProjection.setEvent_day_start(result.getString(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_TIME_START)));
					}
					if (result.getColumnIndex(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_TIME_END) > 0) {
						eventProjection.setEvent_day_end(result.getString(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_TIME_END)));
					}
				}
				
				
				eventProjection
						.setIs_all_day(result.getInt(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY)) == 1);
				eventProjection
						.setStatus(result.getInt(result
								.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.STATUS)));
				if (result.getColumnIndex(EventsProvider.EMetaData.EventsIndexesMetaData.DAY) > 0) {
					eventProjection
							.setEvents_day(result.getString(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsIndexesMetaData.DAY)));
				}
				list.add(eventProjection);
			}
			return list;
		}

		/**
		 * Must override to properly query projections from local DB.<br>
		 * 
		 * @author justinas.marcinka@gmail.com
		 * @return cursor with events projections
		 */
		protected abstract Cursor queryProjectionsFromLocalDb(Calendar date);

		/**
		 * must override to query native events to display
		 * 
		 * @author justinas.marcinka@gmail.com
		 * @return
		 */
		protected abstract ArrayList<Event> queryNativeEvents();

		protected abstract ArrayList<Event> queryBirthdayEvents();

		@Override
		protected abstract void onPostExecute(Void result);

	}

	static class TouchDelegateGroup extends TouchDelegate {
		private static final Rect USELESS_RECT = new Rect();

		public TouchDelegateGroup(View delegateView) {
			super(USELESS_RECT, delegateView);
		}

		private ArrayList<TouchDelegate> mTouchDelegates;
		private TouchDelegate mCurrentTouchDelegate;

		public void addTouchDelegate(TouchDelegate touchDelegate) {
			if (mTouchDelegates == null) {
				mTouchDelegates = new ArrayList<TouchDelegate>();
			}
			mTouchDelegates.add(touchDelegate);
		}

		public void removeTouchDelegate(TouchDelegate touchDelegate) {
			if (mTouchDelegates != null) {
				mTouchDelegates.remove(touchDelegate);
				if (mTouchDelegates.isEmpty()) {
					mTouchDelegates = null;
				}
			}
		}

		public void clearTouchDelegates() {
			if (mTouchDelegates != null) {
				mTouchDelegates.clear();
			}
			mCurrentTouchDelegate = null;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			TouchDelegate delegate = null;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mTouchDelegates != null) {
					for (TouchDelegate touchDelegate : mTouchDelegates) {
						if (touchDelegate != null) {
							if (touchDelegate.onTouchEvent(event)) {
								mCurrentTouchDelegate = touchDelegate;
								return true;
							}
						}
					}
				}
				break;

			case MotionEvent.ACTION_MOVE:
				return false;
			case MotionEvent.ACTION_CANCEL:
				break;
			case MotionEvent.ACTION_UP:
				delegate = mCurrentTouchDelegate;
				mCurrentTouchDelegate = null;
				break;
			}

			return delegate == null ? false : delegate.onTouchEvent(event);
		}
	}

}
