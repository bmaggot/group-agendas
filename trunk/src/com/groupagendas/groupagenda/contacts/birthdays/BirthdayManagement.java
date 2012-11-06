package com.groupagendas.groupagenda.contacts.birthdays;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;

import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.events.Event;

public class BirthdayManagement {
	static Cursor cursor;

	

	public static ArrayList<Event> readBirthdayEventsForTimeInterval(
			Context context, long startTime, long endTime) {
		ArrayList<Event> birthdayEvents = new ArrayList<Event>();
		String form = "MM";
		SimpleDateFormat sdf= new SimpleDateFormat(form);
		String selection = "";

		if (startTime > 0) {
			selection = ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM +" == '" + sdf.format(startTime) + "'";
		}
		
		cursor = context.getContentResolver()
				.query(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI,
						new String[] { 
						ContactsProvider.CMetaData.BirthdaysMetaData.B_ID, 
						ContactsProvider.CMetaData.BirthdaysMetaData.TITLE,
						ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE,
						ContactsProvider.CMetaData.BirthdaysMetaData.COUNTRY,
						ContactsProvider.CMetaData.BirthdaysMetaData.TIMEZONE,
						ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID}, selection,
						null, null);
		

		if (cursor != null) {

			if (cursor.moveToFirst()) {
				do {
					Event event = new Event();
					Calendar calendar = Calendar.getInstance();
					String age="";
					event.setInternalID(Long.valueOf(cursor.getString(5)));
					event.setCountry(cursor.getString(3));
					event.setTimezone(cursor.getString(4));
					String[] date = cursor.getString(2).split("-");
					age=""+(calendar.get(Calendar.YEAR)-Integer.parseInt(date[0]));
					event.setTitle(cursor.getString(1)+" (Age: "+ age +")");
					calendar.set(calendar.get(Calendar.YEAR), Integer.parseInt(date[1])-1,Integer.parseInt(date[2]));
					calendar.clear(Calendar.HOUR);
					calendar.clear(Calendar.HOUR_OF_DAY);
					calendar.clear(Calendar.MINUTE);
					calendar.clear(Calendar.SECOND);
					calendar.clear(Calendar.MILLISECOND);
					event.setStartCalendar(calendar);
					event.setEndCalendar(calendar);
					event.setType("Note");
					event.setIs_all_day(true);
					event.setBirthday(true);
					
					birthdayEvents.add(event);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return birthdayEvents;
	}
}