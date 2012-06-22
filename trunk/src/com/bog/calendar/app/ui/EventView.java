package com.bog.calendar.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bog.calendar.app.model.CEvent;
import com.bog.calendar.app.model.EventsHelper;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.EventActivity;

/**
 * UI representation of Event object
 */
public class EventView extends TextView {
    private CEvent parentEvent;
    private int eventWidth = 0;
    private float minimalEventHeight;
    private float currentEventHeight;
    private int textColor = Color.WHITE;
    private OnClickListener clickListener;
    private int xPos = 0;
    private int yPos = 0;
    private int viewPadding = 7;


    public EventView(Context context) {
        super(context);
    }

    public EventView(Context context, CEvent parentEvent) {
        super(context);
        setParentEvent(parentEvent);
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CEvent getParentEvent() {
        return parentEvent;
    }

    public void setParentEvent(CEvent parentEvent) {
        this.parentEvent = parentEvent;
        if (parentEvent != null) {
            initComponent();
        }
    }

    private void initComponent() {
        setClickable(true);
        setOnClickListener(defaultClickListener);
        setBackgroundDrawable(UIBuildHelper.createRoundedBackgroundShape(getContext(), parentEvent.getColor(), 0xcc, 5));
        setTextColor(textColor);
        minimalEventHeight = getContext().getResources().getDimensionPixelSize(R.dimen.cell_height);
        currentEventHeight = EventsHelper.timeToOffsetConvert(parentEvent.getEndTime() - parentEvent.getStartTime(), (int) minimalEventHeight);
        //if text does not fit by height, do no draw text
        if ((getTextSize() + viewPadding * 2) < currentEventHeight) {
            setText(parentEvent.getDescription());
        }

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(eventWidth == 0 ? RelativeLayout.LayoutParams.FILL_PARENT : eventWidth, (int) currentEventHeight);
        yPos = EventsHelper.timeToOffsetConvert(EventsHelper.getNumberOfMillisecondsFromMidnight(parentEvent.getStartTime()), (int) minimalEventHeight);
        params.topMargin = yPos;
        setPadding(viewPadding, viewPadding, viewPadding, viewPadding);
        setLayoutParams(params);
    }

    private OnClickListener defaultClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
        	Intent intent = new Intent(getContext(), EventActivity.class);
    		intent.putExtra("event_id", parentEvent.getId());
    		intent.putExtra("type", parentEvent.getType());
    		getContext().startActivity(intent);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, (int) heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setMinimumHeight((int) currentEventHeight);
    }

    public int getEventWidth() {
        return eventWidth;
    }

    public void setEventWidth(int eventWidth) {
        this.eventWidth = eventWidth;
    }

    public float getMinimalEventHeight() {
        return minimalEventHeight;
    }

    public float getCurrentEventHeight() {
        return currentEventHeight;
    }
}