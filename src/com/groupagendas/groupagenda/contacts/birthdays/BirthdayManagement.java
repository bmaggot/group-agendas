package com.groupagendas.groupagenda.contacts.birthdays;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;

import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.StringValueUtils;

public class BirthdayManagement {
	public static ArrayList<Event> readBirthdayEventsForTimeInterval(
			Context context, long startTime, long endTime) {
		String form = "MM";
		SimpleDateFormat sdf = new SimpleDateFormat(form);
		SimpleDateFormat sdfy = new SimpleDateFormat(form);
		String selection = generateInForBirthdayMonths(Integer.parseInt(sdf.format(startTime)), 1);
		
		Cursor cursor = context.getContentResolver()
				.query(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI,
						new String[] { 
						ContactsProvider.CMetaData.BirthdaysMetaData.B_ID, 
						ContactsProvider.CMetaData.BirthdaysMetaData.TITLE,
						ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE,
						ContactsProvider.CMetaData.BirthdaysMetaData.COUNTRY,
						ContactsProvider.CMetaData.BirthdaysMetaData.TIMEZONE,
						ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID}, selection,
						null, null);
		

		ArrayList<Event> birthdayEvents = new ArrayList<Event>();
		if (cursor != null) {
			birthdayEvents.ensureCapacity(cursor.getCount());
			while (cursor.moveToNext()) {
				Event event = new Event();
				Calendar calendar = Calendar.getInstance();
				String age = "";
				String yearForm = "yyyy";
				sdfy = new SimpleDateFormat(yearForm);
				
				event.setInternalID(Long.valueOf(cursor.getString(5)));
				event.setCountry(cursor.getString(3));
				event.setTimezone(cursor.getString(4));
				String[] date = cursor.getString(2).split("-");
				age = StringValueUtils.valueOf(Integer.parseInt(sdfy.format(startTime))-Integer.parseInt(date[0]));
				event.setTitle(cursor.getString(1)+" (Age: "+ age +")");
				if(Integer.parseInt(sdf.format(startTime)) == 12 && Integer.parseInt(date[1]) == 1){
					calendar.set(Integer.parseInt(sdfy.format(startTime)) + 1, Integer.parseInt(date[1])-1,Integer.parseInt(date[2]));
				} else {
					calendar.set(Integer.parseInt(sdfy.format(startTime)), Integer.parseInt(date[1])-1,Integer.parseInt(date[2]));
				}
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
				event.setIcon("iconbd");
				
				birthdayEvents.add(event);
			}
			cursor.close();
		}
		return birthdayEvents;
	}
	
	public static String generateInForBirthdayMonths(int month, int radius){
		if(month == 12){
			return ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM + " IN ('11', '12', '01')";
		} else if(month == 1){
			return ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM + " IN ('12', '01', '02')";
		} else {
			return ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM +" IN ('" + ((month-radius) < 10 ?  "0" + (month-radius) : (month-radius)) + "', '" + (month < 10 ?  "0" + month : month) +"', '" + ((month+radius) < 10 ?  "0" + (month+radius) : (month+radius)) + "')";
		}
	}
}