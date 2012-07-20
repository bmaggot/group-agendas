package com.bog.calendar.app.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bog.calendar.app.model.EventListAdapter;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

/**
 * This class is a calendar widget for displaying and selecting dates. The range
 * of dates supported by this calendar is configurable. A user can select a date
 * by taping on it and can scroll and fling the calendar to a desired date.
 *
 * @attr ref android.R.styleable#CalendarView_showWeekNumber
 * @attr ref android.R.styleable#CalendarView_firstDayOfWeek
 * @attr ref android.R.styleable#CalendarView_minDate
 * @attr ref android.R.styleable#CalendarView_maxDate
 * @attr ref android.R.styleable#CalendarView_shownWeekCount
 * @attr ref android.R.styleable#CalendarView_selectedWeekBackgroundColor
 * @attr ref android.R.styleable#CalendarView_focusedMonthDateColor
 * @attr ref android.R.styleable#CalendarView_unfocusedMonthDateColor
 * @attr ref android.R.styleable#CalendarView_weekNumberColor
 * @attr ref android.R.styleable#CalendarView_weekSeparatorLineColor
 * @attr ref android.R.styleable#CalendarView_selectedDateVerticalBar
 * @attr ref android.R.styleable#CalendarView_weekDayTextAppearance
 * @attr ref android.R.styleable#CalendarView_dateTextAppearance
 */
@Widget
public class CalendarView extends FrameLayout {

    /**
     * Tag for logging.
     */
    private static final String LOG_TAG = CalendarView.class.getSimpleName();

    /**
     * Default value whether to show week number.
     */
    private static final boolean DEFAULT_SHOW_WEEK_NUMBER = true;

    /**
     * The number of milliseconds in a day.e
     */
    private static final long MILLIS_IN_DAY = 86400000L;

    /**
     * The number of day in a week.
     */
    private static final int DAYS_PER_WEEK = 7;

    /**
     * The number of milliseconds in a week.
     */
    private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;

    /**
     * String for parsing dates.
     */
    private static final String DATE_FORMAT = "MM/dd/yyyy";

    /**
     * The default minimal date.
     */
    @SuppressWarnings("unused")
	private static final String DEFAULT_MIN_DATE = "01/01/1900";

    /**
     * The default maximal date.
     */
    @SuppressWarnings("unused")
	private static final String DEFAULT_MAX_DATE = "01/01/2100";

    private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;

    private static final int DEFAULT_DATE_TEXT_SIZE = 14;

    private static final int UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH = 6;

    private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;

    private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 0;

    private static final int UNSCALED_BOTTOM_BUFFER = 20;

    private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;

    private static final int DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID = -1;

    private final int mWeekSeperatorLineWidth;

    private final int mDateTextSize;

    @SuppressWarnings("unused")
	private final int mSelectedDateVerticalBarWidth;

    private final int mSelectedWeekBackgroundColor;

    private int mFocusedMonthDateColor;

    private int mUnfocusedMonthDateColor;

    private final int mWeekSeparatorLineColor;

    private final int mWeekNumberColor;

    private final int weekDayTextAppearanceResId;

    /**
     * The top offset of the weeks list.
     */
    @SuppressWarnings("unused")
	private int mListScrollTopOffset = 0;

    /**
     * The visible height of a week view.
     */
    @SuppressWarnings("unused")
	private int mWeekMinVisibleHeight = 12;

    /**
     * The visible height of a week view.
     */
    private int mBottomBuffer = 20;

    /**
     * The number of shown weeks.
     */
    private int mShownWeekCount;

    /**
     * Flag whether to show the week number.
     */
    private boolean mShowWeekNumber;

    /**
     * Flag whether to show the week day names.
     */
    private boolean mShowDayHeader = true;

    /**
     * Flag whether to show the month name.
     */
    private boolean mShowMonthNameHeader = false;

    /**
     * The number of day per week to be shown.
     */
    private int mDaysPerWeek = 7;

    /**
     * The adapter for the weeks list.
     */
    private WeeksAdapter mAdapter;

    /**
     * The weeks list.
     */
    private ListView mListView;

    /**
     * The name of the month to display.
     */
    private TextView mMonthName;

    /**
     * The header with week day names.
     */
    private ViewGroup mDayNamesHeader;

    /**
     * Cached labels for the week names header.
     */
    private String[] mDayLabels;

    /**
     * The first day of the week.
     */
    private int mFirstDayOfWeek;

    /**
     * Which month should be displayed/highlighted [0-11].
     */
    private int mCurrentMonthDisplayed;

    /**
     * Used for tracking during a scroll.
     */
    @SuppressWarnings("unused")
	private long mPreviousScrollPosition;

    /**
     * Used for tracking which direction the view is scrolling.
     */
    @SuppressWarnings("unused")
	private boolean mIsScrollingUp = false;

    /**
     * The previous scroll state of the weeks ListView.
     */
    @SuppressWarnings("unused")
	private int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * The current scroll state of the weeks ListView.
     */
    @SuppressWarnings("unused")
	private int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * Listener for changes in the selected day.
     */
    private OnDateChangeListener mOnDateChangeListener;

    /**
     * Temporary instance to avoid multiple instantiations.
     */
    private Calendar mTempDate;
    
    private Calendar selectedDate = null;

    /**
     * The first day of the focused month.
     */
    private Calendar mFirstDayOfMonth;

    /**
     * The last day of the focused month.
     */
    private Calendar mLastDayOfMonth;

    /**
     * Date format for parsing dates.
     */
    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

    /**
     * The current locale.
     */
    private Locale mCurrentLocale;

    private View weekNumBgPanel;
    
    //TODO
    private boolean isDrawEventsPoint = false;
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private ListView selectedDayEventsList = null;

    /**
     * The callback used to indicate the user changes the date.
     */
    public interface OnDateChangeListener {

        /**
         * Called upon change of the selected day.
         *
         * @param view       The view associated with this listener.
         * @param year       The year that was set.
         * @param month      The month that was set [0-11].
         * @param dayOfMonth The day of the month that was set.
         */
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth);
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        // initialization based on locale
        setCurrentLocale(Locale.getDefault());
        TypedArray attributes;
        if (defStyle != 0) {
            attributes = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyle, 0);
        } else {
            attributes = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
        }
        mShowWeekNumber = attributes.getBoolean(R.styleable.CalendarView_showWeekNumber, DEFAULT_SHOW_WEEK_NUMBER);
        mFirstDayOfWeek = attributes.getInt(R.styleable.CalendarView_firstDayOfWeek, 1);
        mShownWeekCount = attributes.getInt(R.styleable.CalendarView_shownWeekCount, DEFAULT_SHOWN_WEEK_COUNT);
        mSelectedWeekBackgroundColor = attributes.getColor(R.styleable.CalendarView_selectedWeekBackgroundColor, 0);
        mFocusedMonthDateColor = attributes.getColor(R.styleable.CalendarView_focusedMonthDateColor, Color.RED);
        mUnfocusedMonthDateColor = attributes.getColor(R.styleable.CalendarView_unfocusedMonthDateColor, Color.BLACK);
        mWeekSeparatorLineColor = attributes.getColor(R.styleable.CalendarView_weekSeparatorLineColor, Color.TRANSPARENT);
        mWeekNumberColor = attributes.getColor(R.styleable.CalendarView_weekNumberColor, Color.BLACK);
        int dateTextAppearanceResId = attributes.getResourceId(R.styleable.CalendarView_dateTextAppearance, R.style.TextAppearance_Small);
        TypedArray dateTextAppearance = context.obtainStyledAttributes(dateTextAppearanceResId, R.styleable.TextAppearance);
        mDateTextSize = dateTextAppearance.getDimensionPixelSize(R.styleable.TextAppearance_textSize, DEFAULT_DATE_TEXT_SIZE);
        dateTextAppearance.recycle();

        weekDayTextAppearanceResId = attributes.getResourceId(R.styleable.CalendarView_weekDayTextAppearance, DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID);
        attributes.recycle();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mWeekMinVisibleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_WEEK_MIN_VISIBLE_HEIGHT, displayMetrics);
        mListScrollTopOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_LIST_SCROLL_TOP_OFFSET, displayMetrics);
        mBottomBuffer = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_BOTTOM_BUFFER, displayMetrics);
        mSelectedDateVerticalBarWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH, displayMetrics);
        mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, displayMetrics);

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View content = layoutInflater.inflate(R.layout.calendar_view, null, false);
        addView(content);

        mListView = (ListView) content.findViewById(R.id.list);
        mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names);
//        mMonthName = (TextView) content.findViewById(R.id.month_name);
        weekNumBgPanel = content.findViewById(R.id.week_num_bg_panel);

        // go to today or whichever is close to today min or max date
        mTempDate.setTimeInMillis(System.currentTimeMillis());

        setDate(mTempDate.getTimeInMillis(), false, true);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        mListView.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return mListView.isEnabled();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Sets whether to show the week number.
     *
     * @param showWeekNumber True to show the week number.
     */
    public void setShowWeekNumber(boolean showWeekNumber) {
        if (mShowWeekNumber == showWeekNumber) {
            return;
        }
        mShowWeekNumber = showWeekNumber;
        mAdapter.notifyDataSetChanged();
        setUpHeader(weekDayTextAppearanceResId);
    }

    /**
     * Gets whether to show the week number.
     *
     * @return True if showing the week number.
     */
    public boolean getShowWeekNumber() {
        return mShowWeekNumber;
    }

    /**
     * Gets the first day of week.
     *
     * @return The first day of the week conforming to the {@link CalendarView}
     *         APIs.
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     * @see Calendar#SUNDAY
     */
    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Sets the first day of week.
     *
     * @param firstDayOfWeek The first day of the week conforming to the
     *                       {@link CalendarView} APIs.
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     * @see Calendar#SUNDAY
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        if (mFirstDayOfWeek == firstDayOfWeek) {
            return;
        }
        mFirstDayOfWeek = firstDayOfWeek;
        mAdapter.init();
        mAdapter.notifyDataSetChanged();
        setUpHeader(weekDayTextAppearanceResId);
    }

    /**
     * Sets the listener to be notified upon selected date change.
     *
     * @param listener The listener to be notified.
     */
    public void setOnDateChangeListener(OnDateChangeListener listener) {
        mOnDateChangeListener = listener;
    }

    /**
     * Gets the selected date in milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @return The selected date.
     */
    public long getDate() {
        return mAdapter.mSelectedDate.getTimeInMillis();
    }
    
    
    
    public void setSelectedDate(Calendar c){
    	if(selectedDate == null) selectedDate = Calendar.getInstance();
    	selectedDate.setTimeInMillis(c.getTimeInMillis());
    }
    
    /**
     * Sets the selected date in milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param date The selected date.
     * @throws IllegalArgumentException of the provided date is before the
     *                                  minimal or after the maximal date.
     * @see #setDate(long, boolean, boolean)
     */
    //TODO
    public void setDate(long date, boolean events, ListView view) {
    	isDrawEventsPoint = events;
    	selectedDayEventsList = view;
    	
    	if(events){
    		EventListAdapter eventListAdapter = new EventListAdapter(getContext(), getActualEvents(selectedDate));
        	selectedDayEventsList.setAdapter(eventListAdapter);
    	}
    	
        setDate(date, false, false);
    }

    /**
     * Sets the selected date in milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param date    The date.
     * @param animate Whether to animate the scroll to the current date.
     * @param center  Whether to center the current date even if it is already visible.
     * @throws IllegalArgumentException of the provided date is before the
     *                                  minimal or after the maximal date.
     */
    public void setDate(long date, boolean animate, boolean center) {
        mTempDate.setTimeInMillis(date);
        mFirstDayOfMonth.setTimeInMillis(mTempDate.getTimeInMillis());
        mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        mLastDayOfMonth.setTimeInMillis(mTempDate.getTimeInMillis());
        mLastDayOfMonth.set(Calendar.DAY_OF_MONTH, mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        setUpHeader(weekDayTextAppearanceResId);
        setUpListView();
        setUpAdapter();

        goTo(mFirstDayOfMonth, animate, true, center);
    }
    
    public ArrayList<Event> getActualEvents(Calendar c){
    	ArrayList<Event> events = new ArrayList<Event>();
    	
    	if(isDrawEventsPoint){
    		for(int i=0, l=mEvents.size(); i<l; i++){
    			Event e = mEvents.get(i);
    			if(c.after(e.startCalendar) && c.before(e.endCalendar)){
    	    		events.add(e);
    	    	}
    		}
    	}
    	return events;
    }
    
    public void setEvents(ArrayList<Event> events){
    	mEvents = events;
    }
    
    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }

        mCurrentLocale = locale;

        mTempDate = getCalendarForLocale(mTempDate, locale);
        mFirstDayOfMonth = getCalendarForLocale(mFirstDayOfMonth, locale);
        mLastDayOfMonth = getCalendarForLocale(mFirstDayOfMonth, locale);
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale      The locale.
     */
    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    /**
     * @return True if the <code>firstDate</code> is the same as the <code>
     *         secondDate</code>.
     */
    @SuppressWarnings("unused")
	private boolean isSameDate(Calendar firstDate, Calendar secondDate) {
        if (firstDate != null && secondDate != null) {
            return (firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR)
                    && firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR));
        } else {
            return false;
        }
    }

    /**
     * Creates a new adapter if necessary and sets up its parameters.
     */
    private void setUpAdapter() {
        mAdapter = new WeeksAdapter(getContext());
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (mOnDateChangeListener != null) {
                    Calendar selectedDay = mAdapter.getSelectedDay();
                    mOnDateChangeListener.onSelectedDayChange(CalendarView.this,
                            selectedDay.get(Calendar.YEAR),
                            selectedDay.get(Calendar.MONTH),
                            selectedDay.get(Calendar.DAY_OF_MONTH));
                }
            }
        });
        mListView.setAdapter(mAdapter);
        //mAdapter.setHighlightedDay(Calendar.getInstance());
        // refresh the view with the new parameters
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the strings to be used by the header.
     */
    private void setUpHeader(int weekDayTextAppearanceResId) {
        mDayLabels = new String[mDaysPerWeek];
        for (int i = mFirstDayOfWeek, count = mFirstDayOfWeek + mDaysPerWeek; i < count; i++) {
            int calendarDay = (i > Calendar.SATURDAY) ? i - Calendar.SATURDAY : i;
            mDayLabels[i - mFirstDayOfWeek] = DateUtils.getDayOfWeekString(calendarDay, DateUtils.LENGTH_SHORTER);
        }

        if (mShowDayHeader) {
            mDayNamesHeader.setVisibility(View.VISIBLE);
            TextView label = (TextView) mDayNamesHeader.getChildAt(0);
            if (mShowWeekNumber) {
                label.setVisibility(View.VISIBLE);
            } else {
                label.setVisibility(View.GONE);
            }
            for (int i = 1, count = mDayNamesHeader.getChildCount(); i < count; i++) {
                label = (TextView) mDayNamesHeader.getChildAt(i);
                if (weekDayTextAppearanceResId > -1) {
                    label.setTextAppearance(getContext(), weekDayTextAppearanceResId);
                }
                if (i < mDaysPerWeek + 1) {
                    label.setText(mDayLabels[i - 1]);
                    label.setVisibility(View.VISIBLE);
                } else {
                    label.setVisibility(View.GONE);
                }
            }
        } else {
            mDayNamesHeader.setVisibility(View.GONE);
        }
        mDayNamesHeader.invalidate();
    }

    /**
     * Sets all the required fields for the list view.
     */
    private void setUpListView() {
        // Configure the listview
        mListView.setDivider(null);
        mListView.setItemsCanFocus(true);
        mListView.setVerticalScrollBarEnabled(false);
        // Make the scrolling behavior nicer
//        mListView.setFriction(mFriction);
//        mListView.setVelocityScale(mVelocityScale);
    }

    /**
     * This moves to the specified time in the view. If the time is not already
     * in range it will move the list so that the first of the month containing
     * the time is at the top of the view. If the new time is already in view
     * the list will not be scrolled unless forceScroll is true. This time may
     * optionally be highlighted as selected as well.
     *
     * @param date        The time to move to.
     * @param animate     Whether to scroll to the given time or just redraw at the
     *                    new location.
     * @param setSelected Whether to set the given time as selected.
     * @param forceScroll Whether to recenter even if the time is already
     *                    visible.
     * @throws IllegalArgumentException of the provided date is before the
     *                                  range start of after the range end.
     */
    private void goTo(Calendar date, boolean animate, boolean setSelected, boolean forceScroll) {
        // Find the first and last entirely visible weeks
        int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
        View firstChild = mListView.getChildAt(0);
        if (firstChild != null && firstChild.getTop() < 0) {
            firstFullyVisiblePosition++;
        }
        int lastFullyVisiblePosition = firstFullyVisiblePosition + mShownWeekCount - 1;
        if (firstChild != null && firstChild.getTop() > mBottomBuffer) {
            lastFullyVisiblePosition--;
        }
        if (setSelected) {
            mAdapter.setSelectedDay(date);
        }
        setMonthDisplayed(mFirstDayOfMonth);
        mListView.setSelectionFromTop(0, 0);
    }

    /**
     * Parses the given <code>date</code> and in case of success sets
     * the result to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
    @SuppressWarnings("unused")
	private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    /**
     * Sets the month displayed at the top of this view based on time. Override
     * to add custom events when the title is changed.
     *
     * @param calendar A day in the new focus month.
     */
    private void setMonthDisplayed(Calendar calendar) {
        final int newMonthDisplayed = calendar.get(Calendar.MONTH);
        if (mCurrentMonthDisplayed != newMonthDisplayed) {
            mCurrentMonthDisplayed = newMonthDisplayed;
            mAdapter.setFocusMonth(mCurrentMonthDisplayed);
            final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY;
            final long millis = calendar.getTimeInMillis();
            String newMonthName = DateUtils.formatDateRange(getContext(), millis, millis, flags);
//            mMonthName.setText(newMonthName);
//            mMonthName.invalidate();
        }
    }

    /**
     * @return Returns the number of weeks between the current <code>date</code>
     *         and the <code>mMinDate</code>.
     */
    private int getWeeksSinceMinDate(Calendar date) {
        if (date.before(mFirstDayOfMonth)) {
            throw new IllegalArgumentException("fromDate: " + mFirstDayOfMonth.getTime()
                    + " does not precede toDate: " + date.getTime());
        }
        long endTimeMillis = date.getTimeInMillis()
                + date.getTimeZone().getOffset(date.getTimeInMillis());
        long startTimeMillis = mFirstDayOfMonth.getTimeInMillis()
                + mFirstDayOfMonth.getTimeZone().getOffset(mFirstDayOfMonth.getTimeInMillis());
        long dayOffsetMillis = (mFirstDayOfMonth.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
                * MILLIS_IN_DAY;
        return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
    }

    /**
     * <p>
     * This is a specialized adapter for creating a list of weeks with
     * selectable days. It can be configured to display the week number, start
     * the week on a given day, show a reduced number of days, or display an
     * arbitrary number of weeks at a time.
     * </p>
     */
    private class WeeksAdapter extends BaseAdapter implements OnTouchListener {

        @SuppressWarnings("unused")
		private int mSelectedWeek;

        private GestureDetector mGestureDetector;

        private int mFocusedMonth;

        private Calendar mSelectedDate = null;
        private Calendar mHighlightedDay = null;

        private int mTotalWeekCount;
        Context mContext;

        public WeeksAdapter(Context context) {
            mContext = context;
            mGestureDetector = new GestureDetector(mContext, new CalendarGestureListener());
            init();
        }

        /**
         * Set up the gesture detector and selected time
         */
        private void init() {
            mSelectedWeek = getWeeksSinceMinDate(mFirstDayOfMonth);
            mTotalWeekCount = getWeeksSinceMinDate(mLastDayOfMonth);
            if (mFirstDayOfMonth.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek
                    || mLastDayOfMonth.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
                mTotalWeekCount++;
            }
//            mShownWeekCount = mTotalWeekCount;
        }

        /**
         * Updates the selected day and related parameters.
         *
         * @param selectedDay The time to highlight
         */
        public void setSelectedDay(Calendar selectedDay) {
            if (selectedDay != null) {
                mSelectedDate = Calendar.getInstance();
                mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
                mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
                mFocusedMonth = mSelectedDate.get(Calendar.MONTH);
            } else {
                mSelectedDate = null;
            }
            notifyDataSetChanged();
        }

        /**
         * Updates the selected day and related parameters.
         *
         * @param highlightedDate The time to highlight
         */
        public void setHighlightedDay(Calendar highlightedDate) {
            if (highlightedDate != null) {
                mHighlightedDay = Calendar.getInstance();
                mHighlightedDay.setTimeInMillis(highlightedDate.getTimeInMillis());
            } else {
                mHighlightedDay = null;
            }
            notifyDataSetChanged();
        }

        /**
         * @return The selected day of month.
         */
        public Calendar getSelectedDay() {
            return mSelectedDate;
        }

        @Override
        public int getCount() {
            return mTotalWeekCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WeekView weekView = null;
            if (convertView != null) {
                weekView = (WeekView) convertView;
            } else {
                weekView = new WeekView(mContext);
                android.widget.AbsListView.LayoutParams params =
                        new android.widget.AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                weekView.setLayoutParams(params);
                weekView.setClickable(true);
                weekView.setOnTouchListener(this);
            }

            int highlightedDay = mHighlightedDay != null ? mHighlightedDay.get(Calendar.DAY_OF_MONTH) : -1;
            weekView.init(position, highlightedDay, mFocusedMonth);

            return weekView;
        }

        /**
         * Changes which month is in focus and updates the view.
         *
         * @param month The month to show as in focus [0-11]
         */
        public void setFocusMonth(int month) {
            if (mFocusedMonth == month) {
                return;
            }
            mFocusedMonth = month;
            notifyDataSetChanged();
        }
        //TODO
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mListView.isEnabled() && mGestureDetector.onTouchEvent(event)) {
                WeekView weekView = (WeekView) v;
                // if we cannot find a day for the given location we are done
                if (!weekView.getDayFromLocation(event.getX(), mTempDate)) {
                    return true;
                }
                // it is possible that the touched day is outside the valid range
                // we draw whole weeks but range end can fall not on the week end
                if (mTempDate.before(mFirstDayOfMonth) || mTempDate.after(mLastDayOfMonth)) {
                    return true;
                }
                onDateTapped(mTempDate);
                return true;
            }
            return false;
        }

        /**
         * Maintains the same hour/min/sec but moves the day to the tapped day.
         *
         * @param day The day that was tapped
         */
        private void onDateTapped(Calendar day) {
            setHighlightedDay(day);
            setMonthDisplayed(day);            

            if(selectedDayEventsList != null && isDrawEventsPoint){
            	EventListAdapter eventListAdapter = new EventListAdapter(getContext(), getActualEvents(day));
            	selectedDayEventsList.setAdapter(eventListAdapter);
            	setSelectedDate(day);
            }
        }

        /**
         * This is here so we can identify single tap events and set the
         * selected day correctly
         */
        class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        }
    }

    /**
     * <p>
     * This is a dynamic view for drawing a single week. It can be configured to
     * display the week number, start the week on a given day, or show a reduced
     * number of days. It is intended for use as a single view within a
     * ListView. See {@link WeeksAdapter} for usage.
     * </p>
     */
    private class WeekView extends View {

        private final Rect mTempRect = new Rect();

        private final Paint mDrawPaint = new Paint();

        private final Paint mMonthNumDrawPaint = new Paint();
        
        private final Paint eventPointPaint = new Paint();

        // Cache the number strings so we don't have to recompute them each time
        private String[] mDayNumbers;

        // Quick lookup for checking which days are in the focus month
        private boolean[] mFocusDay;

        // The first day displayed by this item
        private Calendar mFirstDay;

        // The month of the first day in this week
        private int mMonthOfFirstWeekDay = -1;

        // The month of the last day in this week
        private int mLastWeekDayMonth = -1;

        // The position of this week, equivalent to weeks since the week of Jan
        // 1st, 1900
        private int mWeek = -1;

        // Quick reference to the width of this view, matches parent
        private int mWidth;

        // The height this view should draw at in pixels, set by height param
        private int mHeight;

        // If this view contains the selected day
        private boolean mHasSelectedDay = false;

        // Which day is selected [0-6] or -1 if no day is selected
        private int mSelectedDay = -1;

        // The number of days + a spot for week number if it is displayed
        private int mNumCells;

        // The left edge of the selected day
        private int mSelectedLeft = -1;

        // The right edge of the selected day
        private int mSelectedRight = -1;

        public WeekView(Context context) {
            super(context);

            mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
                    .getPaddingBottom()) / mShownWeekCount;

            // Sets up any standard paints that will be used
            setPaintProperties();
        }

        /**
         * Initializes this week view.
         *
         * @param weekNumber   The number of the week this view represents. The
         *                     week number is a zero based index of the weeks since
         * @param selectedDay  The selected day of the month, -1 if no selected day.
         * @param focusedMonth The month that is currently in focus i.e.
         *                     highlighted.
         */
        public void init(int weekNumber, int selectedDay, int focusedMonth) {
            mSelectedDay = selectedDay;
            mHasSelectedDay = mSelectedDay != -1;
            mNumCells = mShowWeekNumber ? mDaysPerWeek + 1 : mDaysPerWeek;
            mWeek = weekNumber;
            mTempDate.setTimeInMillis(mFirstDayOfMonth.getTimeInMillis());

            mTempDate.add(Calendar.WEEK_OF_YEAR, mWeek);
            mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);

            // Allocate space for caching the day numbers and focus values
            mDayNumbers = new String[mNumCells];
            mFocusDay = new boolean[mNumCells];

            // If we're showing the week number calculate it based on Monday
            int i = 0;
            if (mShowWeekNumber) {
                mDayNumbers[0] = Integer.toString(mTempDate.get(Calendar.WEEK_OF_YEAR));
                i++;
            }

            // Now adjust our starting day based on the start day of the week
            int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
            mTempDate.add(Calendar.DAY_OF_MONTH, diff);

            mFirstDay = (Calendar) mTempDate.clone();
            mMonthOfFirstWeekDay = mTempDate.get(Calendar.MONTH);

            for (; i < mNumCells; i++) {
                //mFocusDay[i] = (mTempDate.get(Calendar.MONTH) == focusedMonth && mTempDate.get(Calendar.DAY_OF_MONTH) == mSelectedDay);
//            	mFocusDay[i] = (mTempDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) && mTempDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) && mTempDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH));
            	// do not draw dates outside the valid range to avoid user confusion
                if (mTempDate.before(mFirstDayOfMonth) || mTempDate.after(mLastDayOfMonth)) {
                    mDayNumbers[i] = "";
                } else {
                    mDayNumbers[i] = Integer.toString(mTempDate.get(Calendar.DAY_OF_MONTH));
                }
                mTempDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            // We do one extra add at the end of the loop, if that pushed us to
            // new month undo it
            if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
                mTempDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            mLastWeekDayMonth = mTempDate.get(Calendar.MONTH);

            updateSelectionPositions();
        }

        /**
         * Sets up the text and style properties for painting.
         */
        private void setPaintProperties() {
            mDrawPaint.setFakeBoldText(false);
            mDrawPaint.setAntiAlias(true);
            mDrawPaint.setTextSize(mDateTextSize);
            mDrawPaint.setColor(mUnfocusedMonthDateColor);
            mDrawPaint.setStyle(Style.FILL);

            mMonthNumDrawPaint.setFakeBoldText(true);
            mMonthNumDrawPaint.setAntiAlias(true);
            mMonthNumDrawPaint.setTextSize(mDateTextSize);
            mMonthNumDrawPaint.setColor(mUnfocusedMonthDateColor);
            mMonthNumDrawPaint.setStyle(Style.FILL);
            mMonthNumDrawPaint.setTextAlign(Align.CENTER);
            
            eventPointPaint.setColor(getResources().getColor(R.color.monthEventPointColor));
            eventPointPaint.setStyle(Paint.Style.FILL);
            eventPointPaint.setAntiAlias(true);
        }

        /**
         * Returns the month of the first day in this week.
         *
         * @return The month the first day of this view is in.
         */
        @SuppressWarnings("unused")
		public int getMonthOfFirstWeekDay() {
            return mMonthOfFirstWeekDay;
        }

        /**
         * Returns the month of the last day in this week
         *
         * @return The month the last day of this view is in
         */
        @SuppressWarnings("unused")
		public int getMonthOfLastWeekDay() {
            return mLastWeekDayMonth;
        }

        /**
         * Returns the first day in this view.
         *
         * @return The first day in the view.
         */
        @SuppressWarnings("unused")
		public Calendar getFirstDay() {
            return mFirstDay;
        }

        /**
         * Calculates the day that the given x position is in, accounting for
         * week number.
         *
         * @param x The x position of the touch event.
         * @return True if a day was found for the given location.
         */
        public boolean getDayFromLocation(float x, Calendar outCalendar) {
            int dayStart = mShowWeekNumber ? mWidth / mNumCells : 0;
            if (x < dayStart || x > mWidth) {
                outCalendar.clear();
                return false;
            }
            // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
            int dayPosition = (int) ((x - dayStart) * mDaysPerWeek
                    / (mWidth - dayStart));
            outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
            outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            drawBackground(canvas);
            drawWeekNumbers(canvas);
            drawWeekSeparators(canvas);
        }

        /**
         * This draws the selection highlight if a day is selected in this week.
         *
         * @param canvas The canvas to draw on
         */
        private void drawBackground(Canvas canvas) {
            if (!mHasSelectedDay) {
                return;
            }
            mDrawPaint.setColor(mSelectedWeekBackgroundColor);

            mTempRect.top = mWeekSeperatorLineWidth;
            mTempRect.bottom = mHeight;
            mTempRect.left = mShowWeekNumber ? mWidth / mNumCells : 0;
            mTempRect.right = mSelectedLeft - 2;
            canvas.drawRect(mTempRect, mDrawPaint);

            mTempRect.left = mSelectedRight + 3;
            mTempRect.right = mWidth;
            canvas.drawRect(mTempRect, mDrawPaint);
        }

        /**
         * Draws the week and month day numbers for this week.
         *
         * @param canvas The canvas to draw on
         */
        private void drawWeekNumbers(Canvas canvas) {
            float textHeight = mDrawPaint.getTextSize();
            int y = (int) ((mHeight + textHeight) / 2) - mWeekSeperatorLineWidth;
            int nDays = mNumCells;

            mDrawPaint.setTextAlign(Align.CENTER);
            int i = 0;
            int divisor = 2 * nDays;
            // point radius
            int radius = (mWidth / divisor)/17;
            
            if (mShowWeekNumber) {
                mDrawPaint.setColor(mWeekNumberColor);
                int x = mWidth / divisor;
                weekNumBgPanel.setLayoutParams(new LayoutParams(x * 2, ViewGroup.LayoutParams.FILL_PARENT));
                weekNumBgPanel.setVisibility(View.VISIBLE);
                canvas.drawText(mDayNumbers[0], x, y, mDrawPaint);
                i++;
            }
            
            Calendar c = Calendar.getInstance();            
            c.setTimeInMillis(getDate());
            
            for (; i < nDays; i++) {
                mMonthNumDrawPaint.setColor(mFocusDay[i] ? mFocusedMonthDateColor : mUnfocusedMonthDateColor);
                int x = (2 * i + 1) * mWidth / divisor;
                canvas.drawText(mDayNumbers[i], x, y, mMonthNumDrawPaint);
                //TODO
                if(isDrawEventsPoint){
                	if(!mDayNumbers[i].equals("")){
                    	int row = 0;
                    	int col = 0;
                    	int q = 1;
                    	
                    	c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mDayNumbers[i]));
                    	
                    	for(int j=1, l=mEvents.size(); j<=l; j++){
                    		Event e = mEvents.get(j-1);
                    		
                    		if(c.after(e.startCalendar) && c.before(e.endCalendar)){
                    			if(q%8 == 0){ 
                        			row = row+1;
                        			col = col+1;
                        		}
                        		
                        		int xx = x-(mWidth / divisor)+(((q+2)-(row*8))*(radius*3));
                        		int yy = y-(mHeight/2)+(col*(radius*3));
                        		
                        		if(q<=23){
                        			if(e.color != null && !e.color.equals("null") && !e.color.equals("")){
                        				try {
                        					eventPointPaint.setColor(Integer.parseInt(e.color, 16)+0xFF000000);
										} catch (Exception e2) {
											eventPointPaint.setColor(getResources().getColor(R.color.monthEventPointColor));
										}
                        			}else{
                        				eventPointPaint.setColor(getResources().getColor(R.color.monthEventPointColor));
                        			}
                        			
                        			drawEventPoints(canvas, xx, yy, radius);
                        		}
                        		
                        		q++;
                    		}  		
                    	}
                    }
                }   
            }
        }
        
        public void drawEventPoints(Canvas canvas, int x, int y, int radius) {
            canvas.drawCircle(x, y, radius, eventPointPaint);
        }
        
        
        /**
         * Draws a horizontal line for separating the weeks.
         *
         * @param canvas The canvas to draw on.
         */
        private void drawWeekSeparators(Canvas canvas) {
            // If it is the topmost fully visible child do not draw separator line
            int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
            if (mListView.getChildAt(0).getTop() < 0) {
                firstFullyVisiblePosition++;
            }
            if (firstFullyVisiblePosition == mWeek) {
                return;
            }
            mDrawPaint.setColor(mWeekSeparatorLineColor);
            mDrawPaint.setStrokeWidth(mWeekSeperatorLineWidth);
            float x = mShowWeekNumber ? mWidth / mNumCells : 0;
            canvas.drawLine(x, 0, mWidth, 0, mDrawPaint);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mWidth = w;
            mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
                    .getPaddingBottom()) / mShownWeekCount;
            updateSelectionPositions();
        }

        /**
         * This calculates the positions for the selected day lines.
         */
        private void updateSelectionPositions() {
            if (mHasSelectedDay) {
                int selectedPosition = mSelectedDay - mFirstDayOfWeek;
                if (selectedPosition < 0) {
                    selectedPosition += 7;
                }
                if (mShowWeekNumber) {
                    selectedPosition++;
                }
                mSelectedLeft = selectedPosition * mWidth / mNumCells;
                mSelectedRight = (selectedPosition + 1) * mWidth / mNumCells;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
        }
    }
    
    
    public boolean isShowDayHeader() {
        return mShowDayHeader;
    }

    public void setShowDayHeader(boolean mShowDayHeader) {
        this.mShowDayHeader = mShowDayHeader;
        setUpHeader(weekDayTextAppearanceResId);
    }

    public boolean isShowMonthNameHeader() {
        return mShowMonthNameHeader;
    }

    public void setShowMonthNameHeader(boolean mShowMonthNameHeader) {
//        this.mShowMonthNameHeader = mShowMonthNameHeader;
//        if (mMonthName != null) {
//            if (mShowMonthNameHeader) {
//                mMonthName.setVisibility(View.VISIBLE);
//            } else {
//                mMonthName.setVisibility(View.GONE);
//            }
//        }
    }

    public int getFocusedMonthDateColor() {
        return mFocusedMonthDateColor;
    }

    public void setFocusedMonthDateColor(int mFocusedMonthDateColor) {
        this.mFocusedMonthDateColor = mFocusedMonthDateColor;
        invalidate();
    }

    public int getUnfocusedMonthDateColor() {
        return mUnfocusedMonthDateColor;
    }

    public void setUnfocusedMonthDateColor(int mUnfocusedMonthDateColor) {
        this.mUnfocusedMonthDateColor = mUnfocusedMonthDateColor;
        invalidate();
    }
}