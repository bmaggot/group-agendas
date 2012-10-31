package com.groupagendas.groupagenda.calendar.importer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Utils;


public class NativeCalendarImporter {
	public static ArrayList<String> nameOfEvent = new ArrayList<String>();
	public static ArrayList<String> startDates = new ArrayList<String>();
	public static ArrayList<String> endDates = new ArrayList<String>();
	public static ArrayList<String> descriptions = new ArrayList<String>();

	public static void readCalendar(Context context) {
	    Cursor cursor;
	    Account account = new Account(context);
	    
	    if (Integer.parseInt(Build.VERSION.SDK) >= 8 ){
	      cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"), new String[]{ "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" }, null, null, null);
	    } 
	    else{
	        cursor = context.getContentResolver().query(Uri.parse("content://calendar/events"), new String[]{ "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" }, null, null, null);
	    }
	    cursor.moveToFirst();
	    String CNames[] = new String[cursor.getCount()];

	    for (int i = 0; i < CNames.length; i++) {
	    	SimpleDateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
	    	Event event = new Event();
	    	event.setTitle(cursor.getString(1));
	    	event.setDescription(cursor.getString(2));
	    	event.setStartCalendar(Utils.stringToCalendar(context, writeFormat.format(new Date(Long.parseLong(cursor.getString(3)))), DataManagement.SERVER_TIMESTAMP_FORMAT));
	    	event.getStartCalendar().getTime();
	    	event.getStartCalendar().clear(Calendar.SECOND);
	    	event.getStartCalendar().getTime();
	    	event.setEndCalendar(Utils.stringToCalendar(context, writeFormat.format(new Date(Long.parseLong(cursor.getString(4)))), DataManagement.SERVER_TIMESTAMP_FORMAT));
	    	event.getEndCalendar().clear(Calendar.SECOND);
	    	event.setLocation(cursor.getString(5));
	    	event.setTimezone(account.getTimezone());
	    	event.setBirthday(true);
	    	EventManagement.createEventInRemoteDb(context, event); //TODO O kaip su lokalia?
	    	cursor.moveToNext();
	    }
	}
}