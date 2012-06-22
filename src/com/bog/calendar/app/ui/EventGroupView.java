package com.bog.calendar.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bog.calendar.app.model.EventsGroup;
import com.bog.calendar.app.model.EventsHelper;
import com.groupagendas.groupagenda.R;

/**
 * UI representation of EventsGroup object
 */
public class EventGroupView extends LinearLayout {
    EventsGroup parentEventsGroup;
    private float minimalGroupHeight;
    private float currentGroupHeight;

    public EventGroupView(Context context) {
        super(context);
        this.setOrientation(HORIZONTAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
    }

    public EventGroupView(Context context, EventsGroup parentEventsGroup) {
        super(context);
        this.setOrientation(HORIZONTAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        setParentEventsGroup(parentEventsGroup);
    }

    public EventGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(HORIZONTAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
    }

    private void initComponent() {
        setClickable(false);
        minimalGroupHeight = getContext().getResources().getDimensionPixelSize(R.dimen.cell_height);
        currentGroupHeight = EventsHelper.timeToOffsetConvert(parentEventsGroup.getEndTime() - parentEventsGroup.getStartTime(), (int) minimalGroupHeight);
        setMinimumHeight((int) minimalGroupHeight);
        removeAllViews();
        for (int e = 0; e < parentEventsGroup.getSize(); e++) {
            EventView eventView = new EventView(getContext());
            eventView.setParentEvent(parentEventsGroup.getEventList().get(e));
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int) eventView.getCurrentEventHeight(), 1);
            params.topMargin = EventsHelper.timeToOffsetConvert(EventsHelper.getNumberOfMillisecondsFromMidnight(parentEventsGroup.getEventList().get(e).getStartTime()), (int) minimalGroupHeight);
            eventView.setLayoutParams(params);
            this.addView(eventView);
        }
    }

    public EventsGroup getParentEventsGroup() {
        return parentEventsGroup;
    }

    public void setParentEventsGroup(EventsGroup parentEventsGroup) {
        this.parentEventsGroup = parentEventsGroup;
        if (parentEventsGroup != null) {
            initComponent();
        }
    }
}