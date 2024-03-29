package com.bog.calendar.app.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;

/**
 * Operations with Events.
 */
public class EventsHelper {
    public static final long MILLISECONDS_IN_MINUTE = 1000 * 60;
    public static final long MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60;

    /**
     * Sort events by start time to draw from morning to evening order
     *
     * @param eventList list to sort
     */
    public static void sortEventsByStartTime(List<CEvent> eventList) {
        Collections.sort(eventList);
    }

    /**
     * Calculate event offset in milliseconds from 24:00.
     *
     * @param globalTimeInMillis offset
     * @return milliseconds from midnight
     */
    public static long getNumberOfMillisecondsFromMidnight(long globalTimeInMillis) {
        java.util.Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(globalTimeInMillis);
        return cal.get(java.util.Calendar.HOUR_OF_DAY) * EventsHelper.MILLISECONDS_IN_HOUR +
                cal.get(java.util.Calendar.MINUTE) * EventsHelper.MILLISECONDS_IN_MINUTE;
    }

    /**
     * Convert milliseconds length to pixel size based on 'one hour' event height.
     *
     * @param milliseconds event length in milliseconds
     * @param pixelsInHour '1 hour' length event height
     * @return pixels size
     */
    public static int timeToOffsetConvert(long milliseconds, int pixelsInHour) {
        float resultOffset = 0.0f;
        int hours = 0;
        if (milliseconds > MILLISECONDS_IN_HOUR) {
            hours = (int) (milliseconds / MILLISECONDS_IN_HOUR);    //number of full hours in time period
            milliseconds = (int) (milliseconds - hours * MILLISECONDS_IN_HOUR); //number of millis left
        }
        resultOffset = pixelsInHour * ((float) milliseconds / MILLISECONDS_IN_HOUR);
        return (int) resultOffset + (hours * pixelsInHour);
    }

    /**
     * Make group event from Events list.
     *
     * @param eventList list
     * @return group object
     */
    public static EventsGroup eventsListToGroupConvert(List<CEvent> eventList) {
        return new EventsGroup(eventList);
    }

    /**
     * get Minimal Start time from all events in group.
     *
     * @param eventsGroup input eventsGroup
     * @return min start time
     */
    public static long getMinStartTime(EventsGroup eventsGroup) {
        if (eventsGroup != null) {
            List<CEvent> events = eventsGroup.getEventList();
            sortEventsByStartTime(events);
            return events.get(0).getStartTime(); //after sorting minimal time will be in first element.
        }
        return -1;
    }

    /**
     * get Maximal Start time from all events in group.
     *
     * @param eventsGroup input eventsGroup
     * @return max end time
     */
    public static long getMaxEndTime(EventsGroup eventsGroup) {
        if (eventsGroup != null) {
            long maxEndTime = 0;
            for (CEvent event : eventsGroup.getEventList()) {
                if (event.getEndTime() > maxEndTime) {
                    maxEndTime = event.getEndTime();
                }
            }
            return maxEndTime;
        }
        return -1;
    }

    /**
     * Check is event start in currentMonthDate month.
     *
     * @param event            event obj
     * @param currentMonthDate month to compare start date
     * @return true if events starts in entered month
     */
    public static boolean isEventInThisMonth(CEvent event, Calendar currentMonthDate) {
        long startTime = event.getStartTime();
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startTime);
        return (startCal.get(Calendar.YEAR) == currentMonthDate.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == currentMonthDate.get(Calendar.MONTH));
    }

    /**
     * Check is event start in currentMonthDate day.
     *
     * @param event            event obj
     * @param currentMonthDate day to compare start date
     * @return true if events starts in entered day
     */
    public static boolean isEventInThisDay(CEvent event, Calendar currentMonthDate) {
        if (event == null || currentMonthDate == null) {
            return false;
        }
        long startTime = event.getStartTime();
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startTime);
        return (startCal.get(Calendar.YEAR) == currentMonthDate.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == currentMonthDate.get(Calendar.MONTH) &&
                startCal.get(Calendar.DAY_OF_MONTH) == currentMonthDate.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Move allDay events to separate list.
     *
     * @param eventList input list
     * @return allday events list, also input list modified - allday events removed
     */
    public static List<CEvent> extractAllDayEvents(List<CEvent> eventList) {
        List<CEvent> allDayEvents = new ArrayList<CEvent>();
        for (CEvent event : eventList) {
            if (event.isAllDay()) {
                allDayEvents.add(event);
            }
        }
        //remove allDayEvents from old list
        for (CEvent event : allDayEvents) {
            eventList.remove(event);
        }
        return allDayEvents;
    }

    /**
     * Compare events in list and create groups intersected events.
     *
     * @param eventList events to analyze
     * @return list of single events and intersected groups.
     */
    public static List<BaseEvent> findEventsIntersects(List<CEvent> eventList) {
        List<BaseEvent> combinedEvents = new ArrayList<BaseEvent>();
        sortEventsByStartTime(eventList);
        //compare all events with all in list
        for (int i = 0; i < eventList.size(); i++) {
            CEvent event = eventList.get(i);
            if (!event.isGrouped()) {
                EventsGroup intersectedEventsGroup = new EventsGroup();
                intersectedEventsGroup.addEvent(event);//add self
                for (int j = 0; j < eventList.size(); j++) {
                    CEvent comparedEvent = eventList.get(j);
                    if (event == comparedEvent) {
                        continue; //skip self
                    }
                    if (intersectedEventsGroup.isEventIntersectWith(comparedEvent)) { //find intersects with group
                        if (!intersectedEventsGroup.contains(comparedEvent) && !comparedEvent.isGrouped()) {
                            comparedEvent.setGrouped(true);
                            intersectedEventsGroup.addEvent(comparedEvent);
                        }
                    }

                }
                if (intersectedEventsGroup.getSize() > 1) {
                    combinedEvents.add(intersectedEventsGroup);  //if was found any intersected events add them inside group
                } else {  //if was not found intersects add event as single item(not group)
                    if (!combinedEvents.contains(event)) {
                        combinedEvents.add(event);
                    }
                }
            }
        }
        return combinedEvents;
    }

    /**
     * Create event for testing purposes.
     *
     * @param name        event name
     * @param description event description
     * @param startTime   event start time string like "yyyy-MM-dd HH:mm:ss"
     * @param endTime     event end time string like "yyyy-MM-dd HH:mm:ss"
     * @return event object
     */
    public static CEvent generateEvent(Context context, Event e) {
        
        CEvent event = new CEvent();
        event.setId(e.getEvent_id());
        event.setType(e.getType());
        event.setName(e.getTitle());
        event.setDescription(e.getDescription());
        event.setNative(e.isNative());
        
//        if(e.color != null && !e.color.equals("null") && e.color.length() > 1){
        	event.setColor(Integer.parseInt(e.getColor().replace("#", ""), 16)+0xFF000000);
//        }else{
//        	event.setColor(Color.GRAY);
//        }
        
        event.setIcon(e.getIcon());
               
        try{    	
        	Calendar startEventTime = (Calendar) e.getStartCalendar().clone();
            event.setStartTime(startEventTime.getTimeInMillis());
            
            Calendar endEventTime = (Calendar) e.getEndCalendar().clone();
            event.setEndTime(endEventTime.getTimeInMillis());
        	
            return event;
        } catch (Exception ex) {
        	Reporter.reportError(context, EventsHelper.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
            return null;
        }
    }
    
    public static CEvent generateDayEvent(Context context, int id, String type, String name, String description, String startTime, String endTime) {
        try {
            CEvent event = new CEvent();
            
            event.setId(id);
            event.setType(type);
            event.setName(name);
            event.setDescription(description);
            Calendar startEventTime = Calendar.getInstance();
            String hourStr = startTime.split(":")[0];
            String minStr = startTime.split(":")[1];
            int startHour = Integer.parseInt(hourStr);
            int startMin = Integer.parseInt(minStr);
            startEventTime.set(Calendar.HOUR_OF_DAY, startHour);
            startEventTime.set(Calendar.MINUTE, startMin);
            event.setStartTime(startEventTime.getTimeInMillis());
            Calendar endEventTime = Calendar.getInstance();
            hourStr = endTime.split(":")[0];
            minStr = endTime.split(":")[1];
            int endHour = Integer.parseInt(hourStr);
            int endMin = Integer.parseInt(minStr);
            endEventTime.set(Calendar.HOUR_OF_DAY, endHour);
            endEventTime.set(Calendar.MINUTE, endMin);
            event.setEndTime(endEventTime.getTimeInMillis());
            return event;
        } catch (Exception ex) {
        	Reporter.reportError(context, EventsHelper.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), ex.getMessage());
            return null;
        }
    }
}