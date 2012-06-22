package com.bog.calendar.app.model;

/**
 * Base object for single and group events.
 */
public abstract class BaseEvent {
    protected long startTime;             //event start time in milliseconds
    protected long endTime;               //event end time in milliseconds
    protected boolean grouped = false;    //flag that event already added to group

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    /**
     * Return true if event start & end time intersects with current.
     * -----\s1\****EVENT1****\e1\---------------
     * --------------\s2\****EVENT2****\e2\----
     *
     * @param event event to compare
     * @return true if intersects
     */
    public boolean isEventIntersectWith(BaseEvent event) {
        //-----------------\\********EVENT****\\------------
        //-------\\*****this.EVENT*****\\-----------------------
        boolean isStartTimeInsideCurrentEvent = (event.getStartTime() >= this.getStartTime() && event.getStartTime() < this.getEndTime());

        //--\\***********EVENT****\\------------
        //-------\\*****this.EVENT*****\\-------
        boolean isEndTimeInsideCurrentEvent = (event.getEndTime() >= this.getStartTime() && event.getEndTime() < this.getEndTime());

        //--\\***********EVENT************\\----
        //-------\\*****this.EVENT*****\\-------
        boolean isEventIncludeCurrentEvent = (event.getStartTime() <= this.getStartTime() && event.getEndTime() >= this.getEndTime());

        //-------\\*****this.EVENT*****\\******EVENT*********\\-----
        boolean isStartTimeTouchCurrentEvent = event.getStartTime() == this.getEndTime(); //new event touch current event at the end


        //-------\\***********EVENT*********\\------------
        //-------\\*****this.EVENT*****\\-------
        boolean isStartTimeEqualsCurrentEvent = event.getEndTime() == this.getEndTime();
        //--\\***********EVENT*********\\------------
        //-------\\*****this.EVENT*****\\-------
        boolean isEndTimeEqualsCurrentEvent = event.getEndTime() == this.getEndTime();

        return (isStartTimeInsideCurrentEvent || isEndTimeInsideCurrentEvent
                || isEventIncludeCurrentEvent || isStartTimeTouchCurrentEvent
                || isStartTimeEqualsCurrentEvent || isEndTimeEqualsCurrentEvent);
    }
}
