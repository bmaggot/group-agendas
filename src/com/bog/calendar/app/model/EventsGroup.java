package com.bog.calendar.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Group Events object to group intersected events.
 */
public class EventsGroup extends BaseEvent implements Serializable, Comparable<EventsGroup> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8817657536333429582L;
	private List<CEvent> eventList = new ArrayList<CEvent>();

    public EventsGroup() {
    }

    public EventsGroup(List<CEvent> eventList) {
        this.eventList = eventList;
        EventsHelper.sortEventsByStartTime(this.eventList);
        calculateEventsGroupMinMaxTime();
    }

    public void addEvent(CEvent cEvent) {
        if (cEvent != null) {
            eventList.add(cEvent);
            calculateEventsGroupMinMaxTime();
        }
    }

    public boolean contains(CEvent cEvent) {
        return eventList.contains(cEvent);
    }

    public List<CEvent> getEventList() {
        return eventList;
    }

    public void setEventList(List<CEvent> eventList) {
        this.eventList = eventList;
    }

    /**
     * Calculate group startTime & endTime.
     * Group startTime - minimal time from all included events, group endTime - max time from all included events.
     */
    public void calculateEventsGroupMinMaxTime() {
        setStartTime(EventsHelper.getMinStartTime(this));
        setEndTime(EventsHelper.getMaxEndTime(this));
    }

    @Override
    public int compareTo(EventsGroup eventsGroup) {
        return 0;//TODO implement
    }

    public int getSize() {
        return this.eventList.size();
    }

    @Override
    /**
     * Debug help method.
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        boolean isFirst = true;
        for (CEvent cEvent : eventList) {
            if (!isFirst) {
                result.append(", ");  //do not add ","  before first item and after last item
            }
            result.append(cEvent.getName());
            isFirst = false;
        }
        result.append("[");
        result.append(new Date(startTime)).append(" - ").append(new Date(endTime));
        result.append("] ");
        result.append("]");
        return result.toString();
    }
}
