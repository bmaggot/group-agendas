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
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.Invited;

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
						event.setEndCalendar(Utils.stringToCalendar(context, "0", DataManagement.SERVER_TIMESTAMP_FORMAT));
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
						final JSONArray jsonArray= new JSONArray(jsonArraySelectedTime);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject pollThread = jsonArray.getJSONObject(i);
							if(pollThread.getString("response").contentEquals("1")){
								selectedPollTime.add(pollThread);
							}
						}
					}
					event.setSelectedEventPollsTime(""+selectedPollTime);
				} catch (JSONException e1) {
					Reporter.reportError(context, EventManagement.CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
							e1.getMessage());
				}
				
			return event;
		}

	public static Invited createInvitedListFromJSONArrayString(Context context,
	String jsonArrayString, ArrayList<Invited> invites) throws JSONException  {
	JSONArray jsonArray= new JSONArray(jsonArrayString);
	if (invites == null) invites = new ArrayList<Invited>();
	Account acc = new Account(context);
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

	private static Contact createContactFromJSONObject(JSONObject c, Context context) {
		Contact contact = new Contact();
		String temp;

		try {
			contact.contact_id = c.getInt(ContactsProvider.CMetaData.ContactsMetaData.C_ID);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting id.");
		}

		try {
			contact.lid = c.getString(ContactsProvider.CMetaData.ContactsMetaData.LID);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting lid.");
		}
		
		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.NAME);
			if (temp != null && !temp.equals("null"))
				contact.name = temp;
			else
				contact.name = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting name.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME);
			if (temp != null && !temp.equals("null"))
				contact.lastname = temp;
			else
				contact.lastname = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting lastname.");
		}
		
		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.FULLNAME);
			if (temp != null && !temp.equals("null"))
				contact.fullname = temp;
			else
				contact.fullname = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting fulname.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.EMAIL);
			if (temp != null && !temp.equals("null"))
				contact.email = temp;
			else
				contact.email = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting email.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.PHONE);
			if (temp != null && !temp.equals("null"))
				contact.phone1 = temp;
			else
				contact.phone1 = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting phone number.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE);
			if (temp != null && !temp.equals("null"))
				contact.phone1_code = temp;
			else
				contact.phone1_code = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting phone code.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE);
			if (temp != null && !temp.equals("null"))
				contact.birthdate = temp;
			else
				contact.birthdate = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting birthdate.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY);
			if (temp != null && !temp.equals("null"))
				contact.country = temp;
			else
				contact.country = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting country.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.CITY);
			if (temp != null && !temp.equals("null"))
				contact.city = temp;
			else
				contact.city = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting city.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.STREET);
			if (temp != null && !temp.equals("null"))
				contact.street = temp;
			else
				contact.street = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting street.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.ZIP);
			if (temp != null && !temp.equals("null"))
				contact.zip = temp;
			else
				contact.zip = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting zip.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY);
			if (temp != null && !temp.equals("null"))
				contact.visibility = temp;
			else
				contact.visibility = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting visibility.");
		}
		
		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY2);
			if (temp != null && !temp.equals("null"))
				contact.visibility2 = temp;
			else
				contact.visibility2 = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting visibility2.");
		}

		try {
			contact.image = c.getBoolean(ContactsProvider.CMetaData.ContactsMetaData.IMAGE);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL);
			if (temp != null && !temp.equals("null"))
				contact.image_thumb_url = temp;
			else
				contact.image_thumb_url = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_thumb_url.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL);
			if (temp != null && !temp.equals("null")) {
				contact.image_url = temp;
				try {
					contact.image_bytes = Utils.imageToBytes(contact.image_url, context);
				} catch(Exception e) {
					Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_bytes.");
				}
			} else
				contact.image_url = "";
		} catch (JSONException e) {
			contact.image = false;
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_url and/or image_bytes.");
		}

		try {
			contact.created = c.getLong(ContactsProvider.CMetaData.ContactsMetaData.CREATED);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting created.");
		}

		try {
			contact.modified = c.getLong(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting modified.");
		}
		
		try {
			contact.reg_user_id = c.getInt(ContactsProvider.CMetaData.ContactsMetaData.REG_USER_ID);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting reg_user_id.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW);
			if (temp != null && !temp.equals("null"))
				contact.agenda_view = temp;
			else
				contact.agenda_view = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting agenda_view.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW2);
			if (temp != null && !temp.equals("null"))
				contact.agenda_view2 = temp;
			else
				contact.agenda_view2 = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting agenda_view2.");
		}

		try {
			contact.can_add_note = c.getString(ContactsProvider.CMetaData.ContactsMetaData.CAN_ADD_NOTE);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting can_add_note.");
		}

		try {
			contact.time_start = c.getString(ContactsProvider.CMetaData.ContactsMetaData.TIME_START);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting time_start.");
		}

		try {
			contact.time_end = c.getString(ContactsProvider.CMetaData.ContactsMetaData.TIME_END);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting time_end.");
		}

		try {
			contact.all_day = c.getString(ContactsProvider.CMetaData.ContactsMetaData.ALL_DAY);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting all_day.");
		}

		try {
			contact.display_time_end = c.getString(ContactsProvider.CMetaData.ContactsMetaData.DISPLAY_TIME_END);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting display_time_end.");
		}

		try {
			contact.type = c.getString(ContactsProvider.CMetaData.ContactsMetaData.TYPE);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting type.");
		}

		try {
			contact.title = c.getString(ContactsProvider.CMetaData.ContactsMetaData.TITLE);
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting title.");
		}

		// TODO investigate REGISTERED meaning.
		try {
			temp = c.getString("is_reg");
			if (temp != null && !temp.equals("null"))
				contact.registered = temp;
			else
				contact.registered = "";
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting registered.");
		}

		try {
			temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.COLOR);
			if (temp != null && !temp.equals("null"))
				contact.setColor(temp);
			else
				contact.setColor("000000");
		} catch (JSONException e) {
			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting color.");
		}

		try {
			if (!c.getString("groups").equals("null") && c.getString("groups") != null) {
				try {
					JSONArray groups = c.getJSONArray("groups");
					if (groups != null) {
						Map<String, String> set = new HashMap<String, String>();
						for (int j = 0, l = groups.length(); j < l; j++) {
							set.put(String.valueOf(j), groups.getString(j));
						}
						contact.groups = set;
					}
				} catch (JSONException e) {
//					Log.e("getContactsFromRemoteDb(contactIds)", "Groups were null.");
				}
			}
		} catch (JSONException e) {
//			Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting groups.");
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
							set.put(String.valueOf(j), contacts.getString(j));
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

}
