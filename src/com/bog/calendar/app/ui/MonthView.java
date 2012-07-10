package com.bog.calendar.app.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.view.View;

import com.bog.calendar.app.model.CEvent;
import com.bog.calendar.app.model.EventsHelper;
import com.groupagendas.groupagenda.R;

public class MonthView extends View {
    protected static final String TAG = "CalendarView";
    protected static int CELL_WIDTH = 0;
    protected static int CELL_HEIGHT = 0;
    protected static int TITLE_CELL_HEIGHT = 40;
    protected static float CELL_TEXT_SIZE = 20;
    protected Calendar mRightNow = null;
    protected MonthCell mToday = null;
    protected MonthCell[][] mCells = null;
    protected OnCellTouchListener mOnCellTouchListener = null;
    protected OnMonthViewChangeListener mOnMonthViewChangeListener = null;
    protected MonthDisplayHelper mHelper;
    protected boolean isShowWeekNumbers = true;
    protected boolean isShowWeekDayTitles = true;
    protected boolean isShowMonthNameTitle = false;
    protected boolean isShowBorders = true;
    protected boolean isShowNonCurrentMonthDays = true;
    protected boolean isShowEventPoints = true;
    protected boolean isShowEventPointsSimple = false;
    protected Paint weekTitlePaint;
    protected String[] weekTitle;

    protected Calendar currentDay = null;
    protected MonthCell currentCell = null;
    protected List<CEvent> monthEventsList = null;
    protected int borderWidth = 0;
    protected int weekNumberCellBackground = Color.WHITE;
    protected int weekNumberTextColor = Color.BLACK;

    public interface OnCellTouchListener {
        public void onTouch(MonthCell cell);
    }

    public interface OnMonthViewChangeListener {
        public void onChange();
    }

    public MonthView(Context context) {
        this(context, null);
        mRightNow = Calendar.getInstance();
        weekNumberTextColor = getResources().getColor(R.color.monthWeekTextColor);
        initCalendarView();
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mRightNow = Calendar.getInstance();
        initCalendarView();
    }

    public MonthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRightNow = Calendar.getInstance();
        initCalendarView();
    }

    private void initCalendarView() {
        mCells = new MonthCell[6][isShowWeekNumbers ? 8 : 7];
        mHelper = new MonthDisplayHelper(mRightNow.get(Calendar.YEAR), mRightNow.get(Calendar.MONTH), Calendar.MONDAY);
        weekTitle = getResources().getStringArray(R.array.week_days_title);
        weekTitlePaint = new Paint();
        weekTitlePaint.setColor(getResources().getColor(R.color.monthWeekTextColor));
        weekTitlePaint.setTextAlign(Paint.Align.CENTER);
        weekTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        weekTitlePaint.setAntiAlias(true);
        weekTitlePaint.setTextSize(CELL_TEXT_SIZE);
        borderWidth = getResources().getDimensionPixelSize(R.dimen.cellLineWidth);
        clearSelectedDay();
    }

    /**
     * Set new current month.
     *
     * @param year  year
     * @param month month
     */
    public void setCurrentMonth(int year, int month) {
        mRightNow = Calendar.getInstance();
        mRightNow.set(Calendar.YEAR, year);
        mRightNow.set(Calendar.MONTH, month);
        mRightNow.set(Calendar.DAY_OF_MONTH, 1);
        initCalendarView();
        initCells();
        refresh();
    }

    public static Calendar generateCalendarDay(Calendar currentMonth, _calendar day) {
        if (day.thisMonth) {
            Calendar curDay = (Calendar) currentMonth.clone();
            curDay.set(Calendar.DAY_OF_MONTH, day.day);
            return curDay;
        } else {
            Calendar curDay = (Calendar) currentMonth.clone();
            curDay.set(Calendar.YEAR, 0);
            curDay.set(Calendar.MONTH, 0);
            curDay.set(Calendar.DAY_OF_MONTH, day.day);
            return curDay;
        }
    }

    private void initCells() {
        _calendar tmp[][] = new _calendar[6][7];

        for (int i = 0; i < tmp.length; i++) {
            int n[] = mHelper.getDigitsForRow(i);
            for (int d = 0; d < n.length; d++) {
                if (mHelper.isWithinCurrentMonth(i, d))
                    tmp[i][d] = new _calendar(n[d], true);
                else
                    tmp[i][d] = new _calendar(n[d]);

            }
        }

        Calendar today = Calendar.getInstance();
        int thisDay = 0;
        mToday = null;
        if (mHelper.getYear() == today.get(Calendar.YEAR) && mHelper.getMonth() == today.get(Calendar.MONTH)) {
            thisDay = today.get(Calendar.DAY_OF_MONTH);
        }
        // build cells
        int titleCellHeight = (isShowWeekDayTitles ? TITLE_CELL_HEIGHT : 0);
        Rect bound = new Rect(isShowWeekNumbers ? CELL_WIDTH : 0, titleCellHeight,
                isShowWeekNumbers ? (CELL_WIDTH * 2) : CELL_WIDTH, CELL_HEIGHT + titleCellHeight);
        int weekNumber = 0;
        for (int week = 0; week < mCells.length; week++) {
            for (int day = 0; day < mCells[week].length; day++) {
                if (isShowWeekNumbers && day == 7) { //week number cell
                    Calendar cal = Calendar.getInstance();
                    int firstDayOfWeekInMonth = 1 + week * 7;  //calc first day of every week in this month
                    cal.set(Calendar.MONTH, mHelper.getMonth());
                    cal.set(Calendar.YEAR, mHelper.getYear());
                    int maxDays = cal.getMaximum(Calendar.DAY_OF_MONTH);
                    boolean isCurrentMonth = firstDayOfWeekInMonth <= maxDays;
                    if (isCurrentMonth) {
                        cal.set(Calendar.DAY_OF_MONTH, week * 7);
                        weekNumber = cal.get(Calendar.WEEK_OF_YEAR);
                    } else {
                        weekNumber++;
                    }
                    //draw Week Number in first(left) cell
                    bound.left = 0;
                    bound.right = CELL_WIDTH;
                    mCells[week][day] = new WeekCell(getContext(), weekNumber, new Rect(bound), CELL_TEXT_SIZE);
                    ((WeekCell) mCells[week][day]).setFontColor(weekNumberTextColor);
                    ((WeekCell) mCells[week][day]).setBgColor(weekNumberCellBackground);
                } else { //in week cell
                    if (tmp[week][day].thisMonth) {
                        if (mHelper.getWeekStartDay() == Calendar.MONDAY) {
                            if (day == 5 || day == 6) {
                                mCells[week][day] = new HolidayCell(getContext(),
                                        generateCalendarDay(mRightNow, tmp[week][day]),
                                        new Rect(bound),
                                        CELL_TEXT_SIZE);
                            } else {
                                mCells[week][day] = new MonthCell(getContext(),
                                        generateCalendarDay(mRightNow, tmp[week][day]),
                                        new Rect(bound),
                                        CELL_TEXT_SIZE);
                            }
                        } else {
                            if (day == 0 || day == 6) {
                                mCells[week][day] = new HolidayCell(getContext(),
                                        generateCalendarDay(mRightNow, tmp[week][day]),
                                        new Rect(bound),
                                        CELL_TEXT_SIZE);
                            } else {
                                mCells[week][day] = new MonthCell(getContext(),
                                        generateCalendarDay(mRightNow, tmp[week][day]),
                                        new Rect(bound),
                                        CELL_TEXT_SIZE);
                            }
                        }
                    } else {
                        mCells[week][day] = new InactiveCell(getContext(),
                                generateCalendarDay(mRightNow, tmp[week][day]), new Rect(bound), CELL_TEXT_SIZE);
                        mCells[week][day].setVisible(isShowNonCurrentMonthDays);
                    }

                    bound.offset(CELL_WIDTH, 0); // move to next column

                    // get today
                    if (tmp[week][day].day == thisDay && tmp[week][day].thisMonth) {
                        mToday = mCells[week][day];
                        mToday.setBold(true);
                    }
                }
                mCells[week][day].setDrawBorders(isShowBorders);
                mCells[week][day].setDrawEventPoints(isShowEventPoints);
                mCells[week][day].setEventPointsSimple(isShowEventPointsSimple);
            }
            bound.offset(0, CELL_HEIGHT); // move to next row and first column
            bound.left = isShowWeekNumbers ? CELL_WIDTH : 0;
            bound.right = isShowWeekNumbers ? (CELL_WIDTH * 2) : CELL_WIDTH;
        }
        setDayEvents();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("Month", "Width = " + width + " Height = " + height);
        int titleCellHeight = (isShowWeekDayTitles ? TITLE_CELL_HEIGHT : 0);
        CELL_WIDTH = width / (isShowWeekNumbers ? 8 : 7);
        CELL_HEIGHT = (height - titleCellHeight + borderWidth * 2) / 6;
        CELL_TEXT_SIZE = CELL_WIDTH / 3.5f;
        if (CELL_TEXT_SIZE < 13.0f) { //do not set too small font, make size equal cell size
            CELL_TEXT_SIZE = Math.min(CELL_HEIGHT * 0.7f, CELL_WIDTH * 0.7f);
        }
        weekTitlePaint.setTextSize(CELL_TEXT_SIZE);
        initCells();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setTimeInMillis(long milliseconds) {
        mRightNow.setTimeInMillis(milliseconds);
        initCalendarView();
        initCells();
        refresh();
    }

    public int getYear() {
        return mHelper.getYear();
    }

    public int getMonth() {
        return mHelper.getMonth();
    }

    public void nextMonth() {
        clearSelectedDay();
        mHelper.nextMonth();
        mRightNow.set(Calendar.MONTH, mHelper.getMonth());
        mRightNow.set(Calendar.YEAR, mHelper.getYear());
        initCells();
        refresh();
    }

    public void previousMonth() {
        clearSelectedDay();
        mHelper.previousMonth();
        mRightNow.set(Calendar.MONTH, mHelper.getMonth());
        mRightNow.set(Calendar.YEAR, mHelper.getYear());
        initCells();
        refresh();
    }

    public void refresh() {
        invalidate();
        if (mOnMonthViewChangeListener != null) {
            mOnMonthViewChangeListener.onChange();
        }
    }

    public void clearSelectedDay() {
        for (MonthCell[] week : mCells) {
            for (MonthCell day : week) {
                if (day != null) {
                    day.setSelected(false);
                }
            }
        }
        currentDay = null;
        currentCell = null;
        refresh();
    }

    public boolean firstDay(int day) {
        return day == 1;
    }

    public boolean lastDay(int day) {
        return mHelper.getNumberOfDaysInMonth() == day;
    }

    public void goToday() {
        clearSelectedDay();
        Calendar cal = Calendar.getInstance();
        mHelper = new MonthDisplayHelper(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        initCells();
        currentCell = mToday;
        currentDay = mToday.getCurrentDay();
        refresh();
    }

    public Calendar getDate() {
        return mRightNow;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnCellTouchListener != null) {
            for (MonthCell[] week : mCells) {
                for (MonthCell day : week) {
                    if (day.hitTest((int) event.getX(), (int) event.getY())) {
                        mOnCellTouchListener.onTouch(day);
                        currentCell = day;
                        currentDay = day.getCurrentDay();
                        refresh();
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Cell touch listener.
     *
     * @param listener listener
     */
    public void setOnCellTouchListener(OnCellTouchListener listener) {
        mOnCellTouchListener = listener;
    }

    /**
     * Set month change listener. Will be executed when month data changed.
     *
     * @param listener listener
     */
    public void setmOnMonthViewChangeListener(OnMonthViewChangeListener listener) {
        this.mOnMonthViewChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw background
        super.onDraw(canvas);
//        mWeekTitle.draw(canvas);

        // draw cells
        for (MonthCell[] week : mCells) {
            int index = 0;
            float offset = isShowWeekNumbers ? 1.5f : 0.5f;
            if (week != null) {
                for (MonthCell day : week) {
                    if (day != null) {
                        if (isShowWeekDayTitles && index < (weekTitle.length)) { //week title
                            canvas.drawText(weekTitle[index],
                                    (offset * CELL_WIDTH) + CELL_WIDTH * index, TITLE_CELL_HEIGHT / 2, weekTitlePaint);
                            index++;
                        }
                        if (isShowMonthNameTitle) { //month title
                            final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY;
                            final long millis = currentDay.getTimeInMillis();
                            String monthName = DateUtils.formatDateRange(getContext(), millis, millis, flags);
                            canvas.drawText(monthName, getWidth() / 2, CELL_TEXT_SIZE, weekTitlePaint);
                        }
                        day.draw(canvas);
                    }
                }
            }
        }

        // draw today
//		if(mDecoration!=null && mToday!=null) {
//			mDecoration.draw(canvas);
//		}
    }


    /**
     * Get current month events list.
     *
     * @return events list
     */
    public List<CEvent> getMonthEventsList() {
        return monthEventsList;
    }

    /**
     * Add events to month. Will be added only this month events.
     *
     * @param eventsList events list
     */
    public void setMonthEventsList(List<CEvent> eventsList) {
        this.monthEventsList = new ArrayList<CEvent>();
        for (CEvent cEvent : eventsList) {
            if (EventsHelper.isEventInThisMonth(cEvent, mRightNow)) { //add only events from current month
                this.monthEventsList.add(cEvent);
            }
        }
    }

    private void setDayEvents() {
        if (mCells != null) {
            for (int week = 0; week < mCells.length; week++) {
                for (int day = 0; day < mCells[week].length; day++) {
                    if (day < 7) {
                        mCells[week][day].setDayEventsList(this.monthEventsList);
                    }
                }
            }
        }
    }

    /**
     * Get all events from selected day.
     *
     * @return events list
     */
    public List<CEvent> getCurrentDayEventsList() {
        if (currentCell != null) {
            return currentCell.getDayEventsList();
        }
        return null;
    }


    public class InactiveCell extends MonthCell {
        protected Paint hiderCellPaint = new Paint();

        public InactiveCell(Context context, Calendar calendarDay, Rect rect, float s) {
            super(context, calendarDay, rect, s);
            mPaint.setColor(Color.LTGRAY);
            hiderCellPaint = new Paint();
            hiderCellPaint.setColor(context.getResources().getColor(R.color.monthHolidayHiderColor));
            hiderCellPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (isVisible) {
                canvas.drawRect(mBound.left, mBound.top, mBound.right, mBound.bottom, hiderCellPaint);
            }
        }
    }

    private class HolidayCell extends MonthCell {

        public HolidayCell(Context context, Calendar calendarDay, Rect rect, float s) {
            super(context, calendarDay, rect, s);
            mPaint.setColor(context.getResources().getColor(R.color.monthHolidayTextColor));
        }
    }

    private class WeekCell extends MonthCell {
        private int weekNum;
        private Paint mWeekPaint;
        private Paint bgPaint;
        private int fontColor = Color.WHITE;
        private int bgColor = Color.WHITE;
        private int dx, dy;

        public WeekCell(Context context, int weekNum, Rect rect, float s) {
            super(context, Calendar.getInstance(), rect, s);
            this.weekNum = weekNum;
            mWeekPaint = new Paint(mPaint);
            fontColor = context.getResources().getColor(R.color.monthWeekTextColor);
            mWeekPaint.setColor(fontColor);
            mWeekPaint.setTextAlign(Paint.Align.CENTER);
            bgPaint = new Paint();
            bgPaint.setColor(bgColor);
            bgPaint.setStyle(Paint.Style.FILL);
            setDrawBorders(false);
            dx = (int) mPaint.measureText(String.valueOf(mDayOfMonth)) / 2;
            dy = (int) (-mPaint.ascent() + mPaint.descent()) / 2;
        }

        @Override
        public void draw(Canvas canvas) {
            if (isVisible) {
                canvas.drawRect(mBound.left, mBound.top, mBound.right, mBound.bottom, bgPaint);
                canvas.drawText(String.valueOf(weekNum), mBound.centerX(), mBound.centerY() + dy, mWeekPaint);
            }
        }

        public void setBgColor(int color) {
            this.bgColor = color;
            bgPaint.setColor(bgColor);
        }

        public void setFontColor(int color) {
            this.fontColor = color;
            mWeekPaint.setColor(fontColor);
        }
    }

    class _calendar {
        public int day;
        public boolean thisMonth;

        public _calendar(int d, boolean b) {
            day = d;
            thisMonth = b;
        }

        public _calendar(int d) {
            this(d, false);
        }
    }

    public boolean isShowWeekNumbers() {
        return isShowWeekNumbers;
    }

    public void setShowWeekNumbers(boolean showWeekNumbers) {
        isShowWeekNumbers = showWeekNumbers;
    }

    public boolean isShowWeekDayTitles() {
        return isShowWeekDayTitles;
    }

    public void setShowWeekDayTitles(boolean showWeekDayTitles) {
        isShowWeekDayTitles = showWeekDayTitles;
    }

    public boolean isShowBorders() {
        return isShowBorders;
    }

    public void setShowBorders(boolean showBorders) {
        isShowBorders = showBorders;
    }

    public boolean isShowNonCurrentMonthDays() {
        return isShowNonCurrentMonthDays;
    }

    public void setShowNonCurrentMonthDays(boolean showNonCurrentMonthDays) {
        isShowNonCurrentMonthDays = showNonCurrentMonthDays;
    }

    public boolean isShowEventPoints() {
        return isShowEventPoints;
    }

    public void setShowEventPoints(boolean showEventPoints) {
        isShowEventPoints = showEventPoints;
    }

    public int getWeekNumberCellBackground() {
        return weekNumberCellBackground;
    }

    public void setWeekNumberCellBackground(int weekNumberCellBackground) {
        this.weekNumberCellBackground = weekNumberCellBackground;
    }

    public int getWeekNumberTextColor() {
        return weekNumberTextColor;
    }

    public void setWeekNumberTextColor(int weekNumberTextColor) {
        this.weekNumberTextColor = weekNumberTextColor;
    }

    public boolean isShowEventPointsSimple() {
        return isShowEventPointsSimple;
    }

    public void setShowEventPointsSimple(boolean showEventPointsSimple) {
        isShowEventPointsSimple = showEventPointsSimple;
    }
}