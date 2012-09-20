package com.bog.calendar.app.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Color;


/**
 * Single Event object.
 */
public class CEvent extends BaseEvent implements Serializable, Comparable<CEvent> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm dd/MM");
	private int id;						// event id
	private String type;				// event type
    private String timeZone;            //event timezone id
    private boolean isAllDay;           //if full day event (no need startTime & endTime)
    private int color = Color.GRAY;     //display color
    private String icon;
    private String name;                //event name (short text)
    private String description;         //event description (long text)
    private long notificationBefore[];  //can be multiple notifications (milliseconds before start time)
    private boolean isNative;
    
    public int getId(){
    	return id;
    }
    
    public void setId(int id){
    	this.id = id;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
    
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean isNative) {
        this.isNative = isNative;
    }

    public long[] getNotificationBefore() {
        return notificationBefore;
    }

    public void setNotificationBefore(long[] notificationBefore) {
        this.notificationBefore = notificationBefore;
    }

    @Override
    /**
     * Debug help
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\'").append(name).append("\' ");
        result.append("[");
        result.append(dateFormatter.format(new Date(startTime))).append(" - ").append(dateFormatter.format(new Date(endTime)));
        result.append("] ");
        return result.toString();
    }

    @Override
    public int compareTo(CEvent input) {
        return new Long(this.startTime).compareTo(input.startTime);
    }
}
