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
import android.graphics.drawable.Drawable;

import com.bog.calendar.app.model.CEvent;
import com.bog.calendar.app.model.EventsHelper;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.utils.StringValueUtils;

public class MonthCell {
    @SuppressWarnings("unused")
	private static final String TAG = "Cell";
    protected Context context;
    protected Rect mBound = null;
    protected int mYear = 0;
    protected int mMonth = 0;
    protected int mDayOfMonth = 1;    // from 1 to 31
    protected Calendar currentDay = null;
    protected Paint cellPaint = new Paint();
    protected Paint cellBgPaint = new Paint();
    protected Paint eventPointPaint = new Paint();
    protected Paint mPaint = new Paint(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
    protected boolean isDrawBorders = true;
    protected boolean isDrawEventPoints = true;
    protected boolean isEventPointsSimple = false;
    protected boolean isSelected = false;
    protected boolean isVisible = true;
    protected int textMargin;
    protected List<CEvent> dayEventsList = null;
    protected int dayEventsCount = 0;
    protected int numberOfEventPointByHorizontal = 6;
    protected float eventPointSize = 5; //radius depending on numberOfEventPointByHorizontal;
    protected int borderWidth = 0;
    protected int eventPointRadius = 0;
    protected Drawable selectedBg = null;

    public MonthCell(Context context, Calendar calendarDay, Rect rect, float textSize, boolean bold) {
        this.context = context;
        setCurrentDay(calendarDay);
        mBound = rect;
        cellPaint = new Paint();
        cellPaint.setColor(context.getResources().getColor(R.color.monthCellBorderColor));
        cellPaint.setStyle(Paint.Style.STROKE);
        cellPaint.setAntiAlias(true);
        cellPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.cellLineWidth));
        cellBgPaint = new Paint();
        cellBgPaint.setColor(Color.WHITE);
        cellBgPaint.setStyle(Paint.Style.FILL);
        eventPointPaint = new Paint();
        eventPointPaint.setColor(context.getResources().getColor(R.color.monthEventPointColor));
        eventPointPaint.setStyle(Paint.Style.FILL);
        eventPointPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setColor(context.getResources().getColor(R.color.monthTextColor));
        mPaint.setTextAlign(Paint.Align.RIGHT);
        borderWidth = context.getResources().getDimensionPixelSize(R.dimen.cellLineWidth);
        if (bold) {
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            mPaint.setTypeface(Typeface.DEFAULT);
        }

        textMargin = rect.width() / 6;
        setNumberOfEventPointByHorizontal(6);
        selectedBg = context.getResources().getDrawable(R.drawable.selected_month_cell_bg);
    }

    public MonthCell(Context context, Calendar currentDay, Rect rect, float textSize) {
        this(context, currentDay, rect, textSize, true);
    }

    public void draw(Canvas canvas) {
        if (isVisible) {
            if (isSelected) {
                selectedBg.setBounds(mBound);
                selectedBg.draw(canvas);
            } else {
                canvas.drawRect(mBound.left, mBound.top, mBound.right, mBound.bottom, cellBgPaint);
            }
            if (isDrawBorders) {
                canvas.drawRect(mBound.left, mBound.top, mBound.right, mBound.bottom, cellPaint);
            }
            if (isDrawEventPoints) {
                drawEventPoints(canvas);
            }
            canvas.drawText(StringValueUtils.valueOf(mDayOfMonth), mBound.right - textMargin, mBound.bottom - textMargin, mPaint);
        }
    }

    public void drawEventPoints(Canvas canvas) {
        int startLeft = mBound.left + eventPointRadius + borderWidth;
        int startTop = mBound.top + eventPointRadius + borderWidth;
        int currentLeft = startLeft;
        if (isEventPointsSimple) {
            if (dayEventsCount > 0) {
                canvas.drawCircle(mBound.left + eventPointRadius, mBound.bottom - eventPointRadius - textMargin, eventPointRadius, eventPointPaint);
            }
        } else {
            for (int i = 0; i < dayEventsCount; i++) {
                canvas.drawCircle(currentLeft, startTop, eventPointRadius, eventPointPaint);
                if (currentLeft + eventPointSize <= mBound.right) {
                    currentLeft += eventPointSize;
                } else {
                    currentLeft = startLeft;
                    startTop += eventPointSize;
                    if (startTop > mBound.bottom) { //do not draw points which not fit into cell
                        break;
                    }
                }
            }
        }
    }

    /**
     * Set current cell calendar day
     *
     * @param day calendar dey
     */
    public void setCurrentDay(Calendar day) {
        this.currentDay = day;
        if (day != null) {
            mYear = currentDay.get(Calendar.YEAR);
            mMonth = currentDay.get(Calendar.MONTH);
            mDayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH);
        }
    }

    public Calendar getCurrentDay() {
        return currentDay;
    }

    public int getDayOfMonth() {
        return mDayOfMonth;
    }

    public boolean hitTest(int x, int y) {
        boolean result = isVisible && mBound.contains(x, y);
        setSelected(result);
        return result;
    }

    public Rect getBound() {
        return mBound;
    }

    public void setBold(boolean bold) {
        if (bold) {
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            mPaint.setTypeface(Typeface.DEFAULT);
        }
    }

    public List<CEvent> getDayEventsList() {
        return dayEventsList;
    }

    public void setDayEventsList(List<CEvent> eventsList) {
        this.dayEventsList = new ArrayList<CEvent>();
        if (eventsList != null) {
            for (CEvent cEvent : eventsList) {
                if (EventsHelper.isEventInThisDay(cEvent, currentDay)) { //add only events from current day
                    this.dayEventsList.add(cEvent);
                }
            }
        }
        dayEventsCount = this.dayEventsList.size();
    }

    public int getDayEventsCount() {
        return dayEventsCount;
    }

    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(mDayOfMonth);
    	sb.append('(');
    	sb.append(mBound);
    	sb.append(')');
        return sb.toString();
    }

    public boolean isDrawBorders() {
        return isDrawBorders;
    }

    public void setDrawBorders(boolean drawBorders) {
        isDrawBorders = drawBorders;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setNumberOfEventPointByHorizontal(int numberOfEventPointByHorizontal) {
        this.numberOfEventPointByHorizontal = numberOfEventPointByHorizontal;
        this.eventPointSize = (mBound.width() - borderWidth * 2) / numberOfEventPointByHorizontal;  //Cell width/(count of points)
        eventPointRadius = (int) (eventPointSize / 2.0);
    }

    public boolean isDrawEventPoints() {
        return isDrawEventPoints;
    }

    public void setDrawEventPoints(boolean drawEventPoints) {
        isDrawEventPoints = drawEventPoints;
    }

    public boolean isEventPointsSimple() {
        return isEventPointsSimple;
    }

    public void setEventPointsSimple(boolean eventPointsSimple) {
        isEventPointsSimple = eventPointsSimple;
        if (isEventPointsSimple) {
            eventPointRadius = (mBound.width() - borderWidth * 2) / 5;
        }
    }
}