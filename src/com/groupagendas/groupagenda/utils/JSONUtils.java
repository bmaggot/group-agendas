package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.Invited;

public class JSONUtils {

	public static ArrayList<Event> JSONArrayToEventArray(JSONArray eventChanges) {
		ArrayList<Event> result =  new ArrayList<Event>();
		if (eventChanges != null){
			for (int i = 0; i < eventChanges.length(); i++){
				JSONObject o = eventChanges.optJSONObject(i);
				if (o != null) result.add(JSONUtils.createEventFromJSON(o));
			}
		}
		return result;
	}

	public static long[] JSONArrayToLongArray(JSONArray jsonArr) {
		long[] result =  new long[jsonArr.length()];
		for (int i = 0; i < result.length; i++) 
			result[i] = jsonArr.optLong(i);
		return result;
	}

	/**
		 * Creates Event object from JSON object
		 * 
		 * @param e
		 *            JSON object
		 * @return new event object with all info set. WARNING: if not all required
		 *         fields are set, returns null <br>
		 *         Needed fields: event_id<br>
		 *         user_id<br>
		 *         status<br>
		 *         is_owner<br>
		 *         title<br>
		 *         timezone<br>
		 *         time_start_utc<br>
		 *         time_end_utc<br>
		 */
		public static Event createEventFromJSON(JSONObject e) {
			Event event = new Event();
			long unixTimestamp;
			try {
				event.setEvent_id(e.getInt(EventManagement.EVENT_ID));
				event.setTimezone(e.getString(EventManagement.TIMEZONE));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
				// EVENT TIME START
	
				unixTimestamp = e.optLong(EventManagement.TIME_START_UTC);
				if (unixTimestamp == 0) return null;
				event.setStartCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			
				// EVENT TIME END
					unixTimestamp = e.optLong(EventManagement.TIME_END_UTC);
					if (unixTimestamp == 0) return null;
				event.setEndCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			
			
			
	
	
				event.setUser_id(e.optInt(EventManagement.USER_ID));
			
				event.setStatus(e.optInt(EventManagement.STATUS));
	
	
				event.setIs_owner(e.optInt(EventManagement.IS_OWNER) == 1);
	
			
				event.setType(e.optString(EventManagement.TYPE));
		
	
		
				event.setTitle(e.optString(EventManagement.TITLE));
			
				event.setIcon(e.optString(EventManagement.ICON));
			
		
				event.setColor(e.optString(EventManagement.COLOR));
			
	
			
				event.setDescription(e.optString(EventManagement.DESCRIPTION));
			
			
				event.setLocation(e.optString(EventManagement.LOCATION));
			
				event.setAccomodation(e.optString(EventManagement.ACCOMODATION));
			
				event.setCost(e.optString(EventManagement.COST));
			
			try {
				event.setTake_with_you(e.getString(EventManagement.TAKE_WITH_YOU));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setGo_by(e.getString(EventManagement.GO_BY));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setCountry(e.getString(EventManagement.COUNTRY));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setCity(e.getString(EventManagement.CITY));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setStreet(e.getString(EventManagement.STREET));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setZip(e.getString(EventManagement.ZIP));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
	
			// reminders
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_REMINDER_1);
			if (unixTimestamp != 0) event.setReminder1(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_REMINDER_2);
			if (unixTimestamp != 0) event.setReminder2(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_REMINDER_3);
			if (unixTimestamp != 0) event.setReminder3(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			//alarms
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_ALARM_1);
			if (unixTimestamp != 0) event.setAlarm1(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_ALARM_2);
			if (unixTimestamp != 0) event.setAlarm2(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_ALARM_3);
			if (unixTimestamp != 0) event.setAlarm3(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
	
			try {
				event.setAlarm1fired(e.getString(EventManagement.ALARM_1_FIRED));
			} catch (JSONException e1) {
	//			System.out.println("LONG PARSE FAILED");
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setAlarm2fired(e.getString(EventManagement.ALARM_2_FIRED));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
		
			try {
				event.setAlarm3fired(e.getString(EventManagement.ALARM_3_FIRED));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
	
	
				
				event.setCreatedMillisUtc(Utils.unixTimestampToMilis(e.optLong(EventManagement.TIMESTAMP_CREATED)));
				event.setModifiedMillisUtc(Utils.unixTimestampToMilis(e.optLong(EventManagement.TIMESTAMP_MODIFIED)));
	
	
				event.setAttendant_0_count(e.optInt(EventManagement.ATTENDANT_0_COUNT));
				event.setAttendant_1_count(e.optInt(EventManagement.ATTENDANT_1_COUNT));
				event.setAttendant_2_count(e.optInt(EventManagement.ATTENDANT_2_COUNT));
				event.setAttendant_4_count(e.optInt(EventManagement.ATTENDANT_4_COUNT));
		
				event.setSports_event(e.optInt(EventManagement.IS_SPORTS_EVENT) == 1);
				event.setIs_all_day(e.optInt(EventManagement.IS_ALL_DAY) == 1);
	
			try {
				event.setCreator_fullname(e.getString(EventManagement.CREATOR_FULLNAME));
			} catch (JSONException e1) {
				Reporter.reportError(EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
	
				event.setCreator_contact_id(e.optInt(EventManagement.CREATOR_CONTACT_ID));
			
			try {
				String jsonstring = e.getString(EventManagement.INVITED);
			
				ArrayList<Invited> invites = new ArrayList<Invited>();
				createInvitedListFromJSONArrayString(jsonstring, invites);
				event.setInvited(invites);
			} catch (JSONException e1) {
				event.setInvited(new ArrayList<Invited>());
			}
	
				event.setMessage_count(e.optInt(EventManagement.MESSAGE_COUNT));
				event.setNew_message_count(e.optInt(EventManagement.NEW_MESSAGE_COUNT));
				event.setLast_message_date_time(e.optLong(EventManagement.MESSAGE_LAST_TIMESTAMP));
	
			return event;
		}

	public static Invited createInvitedListFromJSONArrayString(
	String jsonArrayString, ArrayList<Invited> invites) throws JSONException  {
	JSONArray jsonArray= new JSONArray(jsonArrayString);
	if (invites == null) invites = new ArrayList<Invited>();
	Account acc = new Account();
	int id = acc.getUser_id();
	Invited myInvite = null;
	int count = jsonArray.length();
	if (count > 0) {
		for (int i = 0; i < count; i++) {
			JSONObject e = jsonArray.getJSONObject(i);
			Invited o = JSONUtils.createInvitedFromJSONObject(e);
			invites.add(o);
			if (id == o.getGuid()) myInvite = o;
		}
	}
	
	return myInvite;
	}

	public static Invited createInvitedFromJSONObject(JSONObject input) {
			Invited item = new Invited();
			
	
			try {
				item.setName(input.getString(EventsProvider.EMetaData.InvitedMetaData.NAME));
			} catch (JSONException e) {
	//			Log.e("Invited(JSONObject input)", "Failed getting gname");
			}
			
				item.setGcid(input.optInt(EventsProvider.EMetaData.InvitedMetaData.GCID));
				item.setGuid(input.optInt(EventsProvider.EMetaData.InvitedMetaData.GUID));
				item.setStatus(input.optInt(EventsProvider.EMetaData.InvitedMetaData.STATUS));
				item.setMy_contact_id(input.optInt(EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID));
	
			return item;
		}

	public static ArrayList<Contact> JSONArrayToContactsArray(JSONArray contactChanges) {
		ArrayList<Contact> result =  new ArrayList<Contact>();
		// TODO Auto-generated method stub
		return result ;
	}

}
