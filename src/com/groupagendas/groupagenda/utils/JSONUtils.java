package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.address.Address;
import com.groupagendas.groupagenda.address.AddressManagement;
import com.groupagendas.groupagenda.alarm.Alarm;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData.AlarmsMetaData;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.metadata.MetaUtils;
import com.groupagendas.groupagenda.metadata.impl.AddressMetaData.AddressTable;
import com.groupagendas.groupagenda.templates.Template;
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;

public class JSONUtils {

	public static ArrayList<Event> JSONArrayToEventArray(Context context, JSONArray eventChanges) {
		ArrayList<Event> result =  new ArrayList<Event>();
		if (eventChanges != null){
			for (int i = 0; i < eventChanges.length(); i++){
				JSONObject o = eventChanges.optJSONObject(i);
				if (o != null) result.add(JSONUtils.createEventFromJSON(context, o));
			}
		}
		return result;
	}

	public static long[] JSONArrayToLongArray(JSONArray jsonArr) {
		long[] result;
		if (jsonArr != null) { 
			result =  new long[jsonArr.length()];
			for (int i = 0; i < result.length; i++) 
				result[i] = jsonArr.optLong(i);
		} else {
			result =  new long[0];
		}
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
		public static Event createEventFromJSON(Context context, JSONObject e) {
			Event event = new Event();
			long unixTimestamp;
			try {
				event.setEvent_id(e.getInt(EventManagement.EVENT_ID));
				event.setTimezone(e.getString(EventManagement.TIMEZONE));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
				// EVENT TIME START
	
//				unixTimestamp = e.optLong(EventManagement.TIME_START_UTC);
//				if (unixTimestamp == 0) return null;
//				event.setStartCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
				try {
					if(e.getString(EventManagement.TYPE).contentEquals("v")){
						event.setStartCalendar(Utils.stringToCalendar(context, "0", DataManagement.SERVER_TIMESTAMP_FORMAT));
						event.setEndCalendar(Utils.stringToCalendar(context, "2100-01-01 00:00:00", DataManagement.SERVER_TIMESTAMP_FORMAT));
					} else {
						event.setStartCalendar(Utils.stringToCalendar(context, e.getString("time_start"), DataManagement.SERVER_TIMESTAMP_FORMAT));
						event.setEndCalendar(Utils.stringToCalendar(context, e.getString("time_end"), DataManagement.SERVER_TIMESTAMP_FORMAT));
					}
				} catch (JSONException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				// EVENT TIME END
//					unixTimestamp = e.optLong(EventManagement.TIME_END_UTC);
//					if (unixTimestamp == 0) return null;
//				event.setEndCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			
			
			
	
	
				event.setUser_id(e.optInt(EventManagement.USER_ID));
			
				event.setStatus(e.optInt(EventManagement.STATUS));
	
	
				event.setIs_owner(e.optInt(EventManagement.IS_OWNER) == 1);
	
			
				event.setType(e.optString(EventManagement.TYPE));
		
	
		
				event.setTitle(e.optString(EventManagement.TITLE));
			
				event.setIcon(e.optString(EventManagement.ICON));
			
		
				event.setColor(e.optString(EventManagement.COLOR));
				event.setDisplayColor(e.optString(EventManagement.DISPLAY_COLOR));
			
				event.setDescription(e.optString(EventManagement.DESCRIPTION));
			
			
				event.setLocation(e.optString(EventManagement.LOCATION));
			
				event.setAccomodation(e.optString(EventManagement.ACCOMODATION));
			
				event.setCost(e.optString(EventManagement.COST));
			
			try {
				event.setTake_with_you(e.getString(EventManagement.TAKE_WITH_YOU));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setGo_by(e.getString(EventManagement.GO_BY));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setCountry(e.getString(EventManagement.COUNTRY));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setCity(e.getString(EventManagement.CITY));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setStreet(e.getString(EventManagement.STREET));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setZip(e.getString(EventManagement.ZIP));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
	
			// reminders
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_REMINDER_1);
			if (unixTimestamp > 0) event.setReminder1(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_REMINDER_2);
			if (unixTimestamp > 0) event.setReminder2(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
			unixTimestamp = e.optLong(EventManagement.ABSOLUTE_REMINDER_3);
			if (unixTimestamp > 0) event.setReminder3(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), EventManagement.user_timezone));
	
			try {
				event.setAlarm1fired(e.getString(EventManagement.ALARM_1_FIRED));
			} catch (JSONException e1) {
	//			System.out.println("LONG PARSE FAILED");
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
			try {
				event.setAlarm2fired(e.getString(EventManagement.ALARM_2_FIRED));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
		
			try {
				event.setAlarm3fired(e.getString(EventManagement.ALARM_3_FIRED));
			} catch (JSONException e1) {
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
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
				Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e1.getMessage());
			}
	
				event.setCreator_contact_id(e.optInt(EventManagement.CREATOR_CONTACT_ID));
			
			try {
				String jsonstring = e.getString(EventManagement.INVITED);
			
				ArrayList<Invited> invites = new ArrayList<Invited>();
				createInvitedListFromJSONArrayString(context, jsonstring, invites);
				event.setInvited(invites);
			} catch (JSONException e1) {
				event.setInvited(new ArrayList<Invited>());
			}
	
				event.setMessage_count(e.optInt(EventManagement.MESSAGE_COUNT));
				event.setNew_message_count(e.optInt(EventManagement.NEW_MESSAGE_COUNT));
				event.setLast_message_date_time(e.optLong(EventManagement.MESSAGE_LAST_TIMESTAMP));
				
				try {
					event.setPoll(e.getString(EventManagement.POLL));
					String jsonArraySelectedTime = e.getString(EventManagement.POLL);
					ArrayList<JSONObject> selectedPollTime = new ArrayList<JSONObject>();
					if(jsonArraySelectedTime != null && !jsonArraySelectedTime.contentEquals("null")){
						final JSONArray jsonArray = new JSONArray(jsonArraySelectedTime);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject pollThread = jsonArray.getJSONObject(i);
							if(pollThread.getString("response").contentEquals("1")){
								selectedPollTime.add(pollThread);
							}
						}
					}
					event.setSelectedEventPollsTime(StringValueUtils.valueOf(selectedPollTime));
				} catch (JSONException e1) {
					Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e1.getMessage());
				}
				
			return event;
		}

	public static Invited createInvitedListFromJSONArrayString(Context context,
			String jsonArrayString, ArrayList<Invited> invites) throws JSONException {
		JSONArray jsonArray = new JSONArray(jsonArrayString);
		Account acc = new Account(context);
		int id = acc.getUser_id();
		Invited myInvite = null;
		int count = jsonArray.length();
		// if (invites == null)
		//	invites = new ArrayList<Invited>(count);
		// else
		if (invites != null)
			invites.ensureCapacity(count);
		for (int i = 0; i < count; i++) {
			JSONObject e = jsonArray.getJSONObject(i);
			Invited o = JSONUtils.createInvitedFromJSONObject(e);
			if (invites != null)
				invites.add(o);
			if (id == o.getGuid())
				myInvite = o;
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

	public static ArrayList<Contact> JSONArrayToContactsArray(JSONArray contactChanges, Context context) {
		ArrayList<Contact> result =  new ArrayList<Contact>();
		if (contactChanges != null){
			for (int i = 0; i < contactChanges.length(); i++){
				JSONObject o = contactChanges.optJSONObject(i);
				if (o != null) result.add(createContactFromJSONObject(o, context));
			}
		}
		return result ;
	}

	public static Contact createContactFromJSONObject(JSONObject c, Context context) {
		Contact contact = new Contact();

		String lastKey = null;
		try {
			contact.contact_id = c.getInt(lastKey = ContactsProvider.CMetaData.ContactsMetaData.C_ID);
			contact.lid = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.LID);
			contact.name = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.NAME));
			contact.lastname = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.LASTNAME));
			contact.fullname = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.FULLNAME));
			contact.email = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
			contact.phone1 = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.PHONE));
			contact.phone1_code = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE));
			contact.birthdate = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE));
			contact.country = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.COUNTRY));
			contact.city = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.CITY));
			contact.street = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.STREET));
			contact.zip = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.ZIP));
			contact.visibility = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY));
			contact.visibility2 = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY2));
			contact.image = c.getBoolean(lastKey = ContactsProvider.CMetaData.ContactsMetaData.IMAGE);
			contact.image_thumb_url = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL));
			contact.image_url = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL));
			if (contact.image_url.length() > 0) {
				try {
					contact.image_bytes = Utils.imageToBytes(contact.image_url, context);
				} catch(Exception e) {
					Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_bytes.");
				}
			}
			contact.created = c.getLong(lastKey = ContactsProvider.CMetaData.ContactsMetaData.CREATED);
			contact.modified = c.getLong(lastKey = ContactsProvider.CMetaData.ContactsMetaData.MODIFIED);
			contact.reg_user_id = c.optInt(lastKey = ContactsProvider.CMetaData.ContactsMetaData.REG_USER_ID);
			contact.agenda_view = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW));
			contact.agenda_view2 = emptyIfNullOrNull(
					c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW2));
			contact.can_add_note = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.CAN_ADD_NOTE);
			
			contact.time_start = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.TIME_START);
			contact.time_end = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.TIME_END);
			contact.all_day = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.ALL_DAY);
			contact.display_time_end = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.DISPLAY_TIME_END);
			contact.type = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.TYPE);
			contact.title = c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.TITLE);
			

			// TODO investigate REGISTERED meaning.
			contact.registered = emptyIfNullOrNull(c.getString(lastKey = "is_reg"));
			{
				String color = emptyIfNullOrNull(c.getString(lastKey = ContactsProvider.CMetaData.ContactsMetaData.COLOR));
				if (color.length() == 0)
					color = "000000";
				contact.setColor(color);
			}
			{
				String gr = emptyIfNullOrNull(c.getString(lastKey = "groups"));
				if (gr.length() > 0) {
					JSONArray groups = c.getJSONArray("groups");
					if (groups != null) {
						Map<String, String> set = new HashMap<String, String>();
						for (int j = 0, l = groups.length(); j < l; j++) {
							set.put(StringValueUtils.valueOf(j), groups.getString(j));
						}
						contact.groups = set;
					}
				}
			}
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting " + lastKey);
			if (lastKey.equals(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL))
				contact.image = false;
		}
		return contact;
	}

	public static ArrayList<Group> JSONArrayToGroupsArray(JSONArray groupChanges, Context context) {
		ArrayList<Group> result =  new ArrayList<Group>();
		if (groupChanges != null){
			for (int i = 0; i < groupChanges.length(); i++){
				JSONObject o = groupChanges.optJSONObject(i);
				if (o != null) result.add(createGroupFromJSONObject(o, context));
			}
		}
		return result ;
	}
	
	public static ArrayList<Alarm> JSONArrayToAlarmArray(JSONArray alarmsJSON, Context context){
		ArrayList<Alarm> result = new ArrayList<Alarm>();
		if(alarmsJSON != null){
			for (int i = 0; i < alarmsJSON.length(); i++) {
				JSONObject o = alarmsJSON.optJSONObject(i);
				if(o != null){
					result.add(createAlarmFromJSONObject(o, context));
				}
			}
		}
		return result;
	}

	private static Group createGroupFromJSONObject(JSONObject g, Context context) {
		Group group = new Group();
		String temp;

		try {
			group.group_id = g.getInt(ContactsProvider.CMetaData.GroupsMetaData.G_ID);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting id.");
		}

		try {
			temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.TITLE);
			if (temp != null && !temp.equals("null"))
				group.title = temp;
			else
				group.title = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting title.");
		}

		try {
			group.created = g.getLong(ContactsProvider.CMetaData.GroupsMetaData.CREATED);
		} catch (JSONException e) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting created.");
		}

		try {
			group.modified = g.getLong(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED);
		} catch (JSONException e) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting modified.");
		}

		try {
			temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.DELETED);
			if (temp != null && !temp.equals("null"))
				group.deleted = temp;
			else
				group.deleted = "";
		} catch (JSONException e) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting phone deleted.");
		}

		try {
			group.image = g.getBoolean(ContactsProvider.CMetaData.GroupsMetaData.IMAGE);
		} catch (JSONException e) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting image.");
		}

		try {
			temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL);
			if (temp != null && !temp.equals("null"))
				group.image_thumb_url = temp;
			else
				group.image_thumb_url = "";
		} catch (JSONException e) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting image_thumb_url.");
		}

		try {
			temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL);
			if (temp != null && !temp.equals("null")) {
				group.image_url = temp;
				try {
					group.image_bytes = Utils.imageToBytes(group.image_url, context);
				} catch (Exception e) {
					Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_bytes.");
				}
			} else
				group.image_url = "";
		} catch (JSONException e) {
			group.image = false;
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting image_url & image_bytes.");
		}

		try {
			group.contact_count = g.getInt(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT);
		} catch (JSONException e) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting contact_count.");
		}

		try {
			if (!g.getString("contacts").equals("null") && g.getString("contacts") != null) {
				try {
					JSONArray contacts = g.getJSONArray("contacts");
					if (contacts != null) {
						Map<String, String> set = new HashMap<String, String>();
						for (int j = 0, l = contacts.length(); j < l; j++) {
							set.put(StringValueUtils.valueOf(j), contacts.getString(j));
						}
						group.contacts = set;
					}
				} catch (JSONException e) {
//			TODO		Log.e("getGroupsFromRemoteDb(conta)", "Contacts were null.");
				}
			}
		} catch (JSONException e) {
//			TODO Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting contacts.");
		}
		return group;

	}
	
	public static Address createAddressFromJSON(Context context, JSONObject e) {
		try {
			return MetaUtils.createFromJSON(e, AddressTable.class, Address.class);
		} catch (JSONException e1) {
			Reporter.reportError(context, AddressManagement.class.getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		return new Address();
	}
	
	public static Alarm createAlarmFromJSONObject(JSONObject jsonObject, Context context) {
		Alarm alarm = new Alarm();
		alarm.setUserId(jsonObject.optInt(AlarmsMetaData.USER_ID));
		alarm.setEventId(jsonObject.optInt(AlarmsMetaData.EVENT_ID));
		alarm.setAlarmTimestamp(Utils.unixTimestampToMilis(jsonObject.optLong(AlarmsMetaData.TIMESTAMP)));
		alarm.setOffset(jsonObject.optLong(AlarmsMetaData.OFFSET));
		alarm.setSent(jsonObject.optBoolean(AlarmsMetaData.SENT));
		alarm.setAlarm_id(alarm.getEventId() + "_" + alarm.getAlarmTimestamp());
		return alarm;
	}

	public static Template createTemplateFromJSON(Context context, JSONObject e) {
		Template template = new Template();

		try {
			template.setTemplate_id(e.getInt(TemplatesMetaData.T_ID));
			template.setTimezone(e.getString(TemplatesMetaData.TIMEZONE));
		} catch (JSONException e1) {
			Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
			try {
				template.setStartCalendar(Utils.stringToCalendar(context, e.getString(TemplatesMetaData.TIME_START), DataManagement.SERVER_TIMESTAMP_FORMAT));
				template.setEndCalendar(Utils.stringToCalendar(context, e.getString(TemplatesMetaData.TIME_END), DataManagement.SERVER_TIMESTAMP_FORMAT));
			} catch (JSONException e2) {
				Log.i("createTemplateFromJSON(context, JSONObject)", "Failed parsing start/end time for template " + template.getTemplate_id());
			}
			template.setTimezoneInUse(e.optInt(TemplatesMetaData.TIMEZONE_IN_USE));
			
			template.setTitle(e.optString(TemplatesMetaData.TITLE));
			template.setTemplate_title(e.optString(TemplatesMetaData.T_TITLE));
			template.setIcon(e.optString(TemplatesMetaData.ICON));
			template.setColor(e.optString(TemplatesMetaData.COLOR));
			template.setDescription_(e.optString(TemplatesMetaData.DESC));
		
		
			template.setLocation(e.optString(EventManagement.LOCATION));
			template.setAccomodation(e.optString(EventManagement.ACCOMODATION));
			template.setCost(e.optString(EventManagement.COST));
			template.setTake_with_you(e.optString(EventManagement.TAKE_WITH_YOU));
			template.setGo_by(e.optString(EventManagement.GO_BY));

			template.setCountry(e.optString(EventManagement.COUNTRY));
			template.setCity(e.optString(EventManagement.CITY));
			template.setStreet(e.optString(EventManagement.STREET));
			template.setZip(e.optString(EventManagement.ZIP));

			template.setIs_all_day(e.optInt(EventManagement.IS_ALL_DAY) == 1);

			try {
				String jsonstring = e.getString(EventManagement.INVITED);
			
				ArrayList<Invited> invites = new ArrayList<Invited>();
				createInvitedListFromJSONArrayString(context, jsonstring, invites);
				template.setInvited(invites);
			} catch (JSONException e1) {
				template.setInvited(new ArrayList<Invited>());
			}
			
			template.setUploadedToServer(true);

		return template;
	}

	public static ArrayList<Template> JSONArrayToTemplatesArray(JSONArray templateChanges,
			Context context) {
		ArrayList<Template> result =  new ArrayList<Template>();
		if (templateChanges != null){
			for (int i = 0; i < templateChanges.length(); i++){
				JSONObject o = templateChanges.optJSONObject(i);
				if (o != null) result.add(JSONUtils.createTemplateFromJSON(context, o));
			}
		}
		return result;
	}

	public static String emptyIfNullOrNull(String value) {
		if (value == null || value.equals("null"))
			return "";
		return value;
	}
}
