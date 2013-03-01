package com.groupagendas.groupagenda.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.interfaces.Colored;
import com.groupagendas.groupagenda.templates.Template;
import com.groupagendas.groupagenda.utils.TimezoneUtils;

public class Event implements Colored {
private static final String DEFAULT_TITLE = "";
private static final String TIMESTAMP_FORMAT = DataManagement.SERVER_TIMESTAMP_FORMAT;

	public static final String DEFAULT_COLOR = "99D9EA";
	public static final String DEFAULT_TEXT_COLOR = "FFFFFF";
	public static final String DEFAULT_ICON = "";
	public static final String DEFAULT_COLOR_ATTENDING = "3F48CC";
	public static final String DEFAULT_COLOR_MAYBE = "ED1C24";

	private static final String EMPTY_ENTRY = "";
	
	public static final String NOTE = "p";
	public static final String SHARED_EVENT = "r";
	public static final String TELEPHONE_CALL = "t";
	public static final String SHARED_NOTE = "n";
	public static final String OPEN_EVENT = "o";
	public static final String NATIVE_EVENT = "native";

	private static final String DEFAULT_TYPE = NOTE;
	private static final String DEFAULT_DESCRIPTION = "";
	
	private long internalID;
	private int event_id;
	private int user_id;
	private int status;
	private int creator_contact_id;
	
	private int attendant_1_count; 
	private int attendant_2_count;
	private int attendant_0_count;
	private int attendant_4_count;
	
	private boolean uploadedToServer = true;
	private boolean isNative = false;
	private boolean is_sports_event;	
	private boolean is_owner;
	private boolean is_all_day;
	private boolean birthday = false;
	
	
	private String type;
	private String creator_fullname;
	private String title;
	private String icon;
	private String color;
//	private String textColor;//2012-10-24
	private String displayColor;//2012-10-24
	private String description_;
	private String location;
	private String accomodation;
	private String cost;
	private String take_with_you;
	private String go_by;
	
	private String country;
	private String city;
	private String street;
	private String zip;
	
	private String timezone;
	private Calendar startCalendar;     //EVENT START TIME. CALENDAR IS IN USER TIMEZONE.
	private Calendar endCalendar;
	
	
	
	private Calendar reminder1 = null;
	private Calendar reminder2 = null;
	private Calendar reminder3 = null;
	private Calendar alarm1 = null;
	private boolean alarm1fired = false;
	private Calendar alarm2 = null;
	private boolean alarm2fired = false;
	private Calendar alarm3 = null;
	private boolean alarm3fired = false;
	
	private long created_millis_utc;
	private long modified_millis_utc;
	

	private ArrayList<Invited> invited = null;
	private long[] assigned_contacts = null;
//	private long[] assigned_groups = null;
	
	private int message_count = 0;
	private int new_message_count = 0;
	private long last_message_date_time = 0;
	private Invited myInvite;
	private String poll;
	private String selected_event_polls_time;
	private String events_day;
	
	private String event_day_start;
	private String event_day_end;
	
	//TODO implement event missing attributes 
	/* public String confirmed;
	 * public Calendar createdCalendar;
	 * public Calendar modifiedCalendar;
	 * public int total_invited;
	 * public int sport_team_id;
	 * public String sport_event_type;
	 * public String sport_location;
	 * public String sport_field;
	 * public String sport_opponent;
	 * public String sport_referee;
	 * public String sport_time_assembly;
	 * public String sport_time_arrival;
	 * public String sport_arrival_address;
	 * public String sport_start_return_trip;
	 * public String sport_time_return;
	 * public int poll_voted_count;
	 * public int poll_pending_count;
	 * public int poll_rejected_count;
	 * public int poll_invited_count;
	 * public int org_id;
	 * public String repeat_group;
	 * public String repeat_data;
	 * public String created_local;
	 * public String modified_local;
	 * public String wp; (work private)
	 * */
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		SimpleDateFormat df = new SimpleDateFormat(TIMESTAMP_FORMAT);
		Calendar c;
		if ((c = startCalendar) != null) {
			sb.append('\n').append("Start time:").append(df.format(c.getTime()));
		}
		if ((c = endCalendar) != null) {
			sb.append('\n').append("End time:").append(df.format(c.getTime()));
		}
		return sb.toString();
	}
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @return resource id of colour bubble image for this event. If event color is not set, id of white bubble will be returned 
	 */
	public int getColorBubbleId(Context context){
		
		String color = this.color;
		
		if (color == null) color = "";
		if (color.equalsIgnoreCase("null")) color = "";
		
		String bubbletitle = "calendarbubble_" + color + "_";
		
		int imgID = context.getResources().getIdentifier(bubbletitle, "drawable", context.getPackageName());
		return imgID;
	}

	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @return resource id of icon image for this event. If event has no icon, 0 will be returned
	 */
	public int getIconId(Context context) {
		if (getIcon() == DEFAULT_ICON) return 0;
		return context.getResources().getIdentifier(this.icon, "drawable", context.getPackageName());
	}

	@Override
	public String getColor() {
		if (color == null || color.equalsIgnoreCase("null")) {
				this.color = DEFAULT_COLOR;
		}

		return color;
	}
	
	public void setColor(String color) {
		this.color = DEFAULT_COLOR;
		if (color != null && (color.matches("[a-fA-F0-9]{6,8}")))
			if (!color.equalsIgnoreCase("null")) {
				this.color = color;
			}
		
	}
		
		    public String getTextColor() {
		    	String displayColor = getDisplayColor(); 
		      	int r = Integer.parseInt(displayColor.substring(0, 2), 16);
		    	int g = Integer.parseInt(displayColor.substring(2, 4), 16);
		    	int b = Integer.parseInt(displayColor.substring(4, displayColor.length()), 16);
		
		        int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
		        return (yiq >= 143) ? "000000" : "FFFFFF"; // original 128
//			if (textColor == null || textColor.equalsIgnoreCase("null")) {
//					this.textColor = DEFAULT_TEXT_COLOR;
//			}
//
//			return textColor;
		}
		
//		public void setTextColor(String textColor) {
//			this.textColor = DEFAULT_TEXT_COLOR;
//			if (textColor != null && (textColor.matches("[a-fA-F0-9]{6,8}")))
//				if (!textColor.equalsIgnoreCase("null")) this.textColor = textColor;
//		
//		}
		
		    
		/**
		 * Get displayed color.
		 * 
		 * @author meska.lt@gmail.com
		 * @return Color of event to be displayed
		 * @since 2012-11-07
		 */
		public String getDisplayColor() {
			String color = DEFAULT_COLOR;
			
			if (!(displayColor == null) && !(displayColor.equalsIgnoreCase("null"))) {
				color = displayColor;
			} else {
				if (getColor().equals(DEFAULT_COLOR)) {
					if (!getType().equals(NOTE) || !getType().equals(NATIVE_EVENT)) {
						if (status == Invited.ACCEPTED) {
							color = DEFAULT_COLOR_ATTENDING;
						} else if (!birthday) {
							color = DEFAULT_COLOR_MAYBE;
						} else {
							color = DEFAULT_COLOR;
						}
					} else {
						color = DEFAULT_COLOR;
					}
					
				} else {
					if (!getType().equals(NOTE) || !getType().equals(NATIVE_EVENT)) {
						if (status == Invited.ACCEPTED) {
							color = DEFAULT_COLOR_ATTENDING;
						} else if (!birthday) {
							color = DEFAULT_COLOR_MAYBE;
						} else {
							color = DEFAULT_COLOR;
						}
					} else {
						color = getColor();
					}
				}
			}
			
			return color;
		}
		
		public void setDisplayColor(String displayColor) {
			this.displayColor = DEFAULT_COLOR;
			if (displayColor != null && (displayColor.matches("[a-fA-F0-9]{6,8}")))
				if (!displayColor.equalsIgnoreCase("null")) this.displayColor = displayColor;
		
		}
		
	
/**
 * @author justinas.marcinka@gmail.com
 * @return true if this event has icon set
 */
	public boolean hasIcon() {
		
		if (getIcon().length() < 1) return false;
		return true;
	}

	public Calendar getStartCalendar() {
			return startCalendar;
	}

	/**
	 * Setter for field startCalendar. Ensures that date would be set yyyy-MM-dd hh:mm:00:00
	 * @author justinas.marcinka@gmail.com
	 * @param startCalendar
	 */
	public void setStartCalendar(Calendar startCalendar) {
		if(startCalendar != null){
			this.startCalendar = startCalendar;
			this.startCalendar.clear(Calendar.SECOND);
			this.startCalendar.clear(Calendar.MILLISECOND);
		}
	}

	public Calendar getEndCalendar() {
		return endCalendar;
	}

	/**
	 * Getter for field endCalendar. Ensures that date would be set yyyy-MM-dd hh:mm:00:0
	 * @author justinas.marcinka@gmail.com
	 * @param endCalendar
	 */
	public void setEndCalendar(Calendar endCalendar) {
		if(startCalendar != null){
			this.endCalendar = endCalendar;
			this.endCalendar.clear(Calendar.SECOND);
			this.endCalendar.clear(Calendar.MILLISECOND);
		}
	}
	
	/**
	 * Method checks if this event's data is valid. If there is some logical problems, it returns error code.
	 * @author justinas.marcinka@gmail.com
	 * @return Error code for event.                   
	 * Possible error codes:<br>  
	 * 0 - no error.<br> 
	 * 1 - event title not set.<br>
	 * 2 - event timezone is not set.<br>
	 * 3 - event end date or start date is not set.<br> 
	 * 4 - event end date is before start date.<br>   
	 * 5 - event duration equals 0, and this is not all day event.<br>   
	 */
	public int isValid(){
		
		int check;
		
		//Validating title
		check = validateTitle(this.title);
		if (check != 0) return check;
		
		check = validateTimezone(this.timezone);
		if (check != 0) return check;
		
		//Calendar fields validity check
		check = validateCalendars();
		if (check != 0) return check;
		
		return 0;
	}
	
	/**
	 * Method checks if this event's timezone is set.
	 * @author justinas.marcinka@gmail.com
	 * @param timezone 
	 * @return Error code for event.                   
	 * Possible error codes:<br>  
	 * 0 - no error.<br> 
	 * 2 - event timezone not set<br>
	 */
	private int validateTimezone(String timezone) {
		if (timezone == null || timezone.equalsIgnoreCase("null")) return 2;
		return 0;
	}
	
	/**
	 * Method checks if this event's title is set.
	 * @author justinas.marcinka@gmail.com
	 * @return Error code for event.                   
	 * Possible error codes:<br>  
	 * 0 - no error.<br> 
	 * 1 - event title not set<br>
	 */
	private int validateTitle(String title) {
		if (title == null || title.equalsIgnoreCase("null") || title.length() <= 0) return 1;
		return 0;
	}
	/**
	 * Method checks if this event's calendars are valid.
	 * @author justinas.marcinka@gmail.com
	 * @return Error code for event.                   
	 * Possible error codes:<br>  
	 * 0 - no error.<br> 
	 * 3 - event startCalendar or endCalendar is not set (null).<br>
	 * 4 - event end date is before start date.<br>   
	 * 5 - event duration equals 0, and this is not all day event.<br>    
	 */
	private int validateCalendars(){
		
				if (startCalendar == null || endCalendar == null)
					return 3; // if either of fields is not set
				else {
					if (!startCalendar.before(endCalendar)) {
						if (startCalendar.after(endCalendar))
							return 4; // if event start is later than end
						else if (!this.is_all_day)
							return 5; // if event start is equal as end and it is not
										// all day event (event duration is 0)
					}
				}
		return 0;
	}
	public int getUser_id() {
		return user_id;
	}
	
	public String getType() {
		
		if(type == null){
			type = DEFAULT_TYPE;
		}
		
			return type;
		
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @return value of this.title
	 */
	public String getActualTitle() {
		return title;
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @return value of this.title, if it's valid, else returns Event.DEFAULT_TITLE
	 */
	public String getValidTitle() {
		if (validateTitle(getActualTitle()) != 0) return DEFAULT_TITLE;
		return title;
	}
	public String getZip() {
		if(zip != null){
			return zip;
		} else {
			return "";
		}
	}
	public String getTimezone() {
		if(timezone !=null){
			return timezone;
		} else {
			return "";
		}
	}
	public String getDescription() {
		if (description_ == null || description_.equalsIgnoreCase("null")) description_ = DEFAULT_DESCRIPTION;
		return description_;
	}
	
	public void setTitle(String title) {
		this.title = DEFAULT_TITLE;
		if (title != null && !title.equalsIgnoreCase("null"))
		this.title = title;
	}
	public int getEvent_id() {
		return event_id;
	}
	public boolean isNative() {
		return isNative;
	}
	public boolean is_sports_event() {
		return is_sports_event;
	}
	public int getStatus() {
		return status;
	}
	public boolean is_owner() {
		return is_owner;
	}
	public String getCreator_fullname() {
		return creator_fullname;
	}
	public int getCreator_contact_id() {
		return creator_contact_id;
	}
	public String getTitle() {
		if(title == null){
			return DEFAULT_TITLE;
		}
		return title;
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * Returns icon filename without extension
	 * @return
	 */
	public String getIcon() {
		if (icon == null || icon.equalsIgnoreCase("null")) {
				this.icon = DEFAULT_ICON;
		}
		return icon;
	}
	public String getLocation() {
		if(location != null){
			return location;
		} else {
			return "";
		}
	}
	public String getAccomodation() {
		if(accomodation != null){
			return accomodation;
		} else {
			return "";
		}
	}
	public String getCost() {
		if (cost != null && !cost.equalsIgnoreCase("null")) {
			return cost;
		} else {
			return "";
		}
	}
	public String getTake_with_you() {
		if(take_with_you != null){
			return take_with_you;
		} else {
			return "";
		}
	}
	public String getGo_by() {
		if(go_by != null){
			return go_by;
		} else {
			return "";
		}
	}
	public String getCountry() {
		if(country != null){
			return country;
		} else {
			return "";
		}
	}
	public String getCity() {
		if (city != null) {
			return city;
		} else {
			return "";
		}
	}
	public String getStreet() {
		if(street != null){
			return street;
		} else {
			return "";
		}
	}
/**
 * Getter for reminder 1. Returns NULL if not set
 * @return
 */
	public Calendar getReminder1() {
		return reminder1;
	}
	/**
	 * Getter for reminder 2. Returns NULL if not set
	 * @return
	 */
	public Calendar getReminder2() {
		return reminder2;
	}
	/**
	 * Getter for reminder 3. Returns NULL if not set
	 * @return
	 */
	public Calendar getReminder3() {
		return reminder3;
	}

	public int getAttendant_1_count() {
		return attendant_1_count;
	}
	public int getAttendant_2_count() {
		return attendant_2_count;
	}
	public int getAttendant_0_count() {
		return attendant_0_count;
	}
	public int getAttendant_4_count() {
		return attendant_4_count;
	}

	public boolean is_all_day() {
		return is_all_day;
	}
	public boolean isBirthday() {
		return birthday;
	}
	
		
	public void setReminder1(Calendar reminder1) {
		this.reminder1 = reminder1;
	}

	public void setReminder2(Calendar reminder2) {
		this.reminder2 = reminder2;
	}

	public void setReminder3(Calendar reminder3) {
		this.reminder3 = reminder3;
	}


///**
// * Creates new string for reminder with given id. 
// * @param id identifier for reminder.
// * @see Event.REMINDER_1 Event.REMINDER_2 Event.REMINDER_3
// * @param reminder time in Calendar format
// * @return "null" if it's not in future or is same as already defined reminder. Else, returns string representation for given time
// *  
// */
//	private String createReminderString(int id, Calendar reminder) {
//		
//		if (reminder == null) return DEFAULT_REMINDER;
//		
//		if (reminder.after(Calendar.getInstance())) //if it is not in future, return "null"
//			return DEFAULT_REMINDER;
//		
//		String reminderStr  = new SimpleDateFormat(TIMESTAMP_FORMAT).format(reminder.getTime());
//		
//		if (reminderStr.equalsIgnoreCase(reminder1) && id != REMINDER_1) return DEFAULT_REMINDER;
//		if (reminderStr.equalsIgnoreCase(reminder2) && id != REMINDER_2) return DEFAULT_REMINDER;
//		if (reminderStr.equalsIgnoreCase(reminder3) && id != REMINDER_3) return DEFAULT_REMINDER;
//		
//		return reminderStr;
//	}
	
	
	
//	public void setAssigned_contacts(int[] assigned_contacts) {
//			this.assigned_contacts = assigned_contacts;
//		}	
//	public int[] getAssigned_contacts() {
//		if (assigned_contacts == null) assigned_contacts = parseAssignedContacts(getAssigned_contacts_DB_entry());
//		return assigned_contacts;
//	}
//	private int[] parseAssignedContacts(String assigned_contacts_DB_entry) {
//		return new int[0];
//	}
//	
	
	
	
//	public void setAssigned_groups(int[] assigned_groups) {
//		this.assigned_groups = assigned_groups;
//	}
//	
//	public int[] getAssigned_groups() {
//		if (assigned_groups == null) assigned_groups = parseAssignedGroups(getAssigned_groups_DB_entry());
//		return assigned_groups;
//	}
//	private int[] parseAssignedGroups(String assigned_groups_DB_entry) {
//		return new int[0];
//	}
	
	
	
	public void setInvited(ArrayList<Invited> invited) {
		this.invited = invited;
	}
	public ArrayList<Invited> getInvited() {
		if (invited == null)
			invited = new ArrayList<Invited>();
		return invited;
	}





//public void setAssigned_contacts_DB_entry(String assigned_contacts_DB_entry) {
//	this.assigned_contacts_DB_entry = assigned_contacts_DB_entry;
//}
//public void setAssigned_groups_DB_entry(String assigned_groups_DB_entry) {
//	this.assigned_groups_DB_entry = assigned_groups_DB_entry;
//}


public Calendar getAlarm1() {
	return alarm1;
}
public void setAlarm1(Calendar alarm1) {
	this.alarm1 = alarm1;
}
public boolean isAlarm1fired() {
	return alarm1fired;
}
public void setAlarm1fired(boolean alarm1fired) {
	this.alarm1fired = alarm1fired;
}

public void setAlarm1fired(String alarm1fired_string) {
	this.alarm1fired = isFired(alarm1fired_string);
}

private boolean isFired(String alarm_fired_string) {
		if (alarm_fired_string == null)
			return false;

		if (alarm_fired_string.matches("[0-9]*")) {
			return Integer.parseInt(alarm_fired_string) == 1;
		}
		
		return false;
}
public Calendar getAlarm2() {
	return alarm2;
}
public void setAlarm2(Calendar alarm2) {
	this.alarm2 = alarm2;
}
public boolean isAlarm2fired() {
	return alarm2fired;
}
public void setAlarm2fired(boolean alarm2fired) {
	this.alarm2fired = alarm2fired;
}
public void setAlarm2fired(String string) {
	this.alarm2fired = isFired(string);	
}

public Calendar getAlarm3() {
	return alarm3;
}
public void setAlarm3(Calendar alarm3) {
	this.alarm3 = alarm3;
}
public boolean isAlarm3fired() {
	return alarm3fired;
}
public void setAlarm3fired(boolean alarm3fired) {
	this.alarm3fired = alarm3fired;
}

public void setAlarm3fired(String string) {
	this.alarm3fired = isFired(string);	
}
public long getCreatedUtc() {
	return created_millis_utc;
}
public void setCreatedMillisUtc(long created) {
	this.created_millis_utc = created;
}
public long getModifiedMillisUtc() {
	return modified_millis_utc;
}
public void setModifiedMillisUtc(long modified) {
	this.modified_millis_utc = modified;
}
public void setEvent_id(int event_id) {
	this.event_id = event_id;
}
public void setUser_id(int user_id) {
	this.user_id = user_id;
}
public void setNative(boolean isNative) {
	this.isNative = isNative;
}
public void setSports_event(boolean is_sports_event) {
	this.is_sports_event = is_sports_event;
}
public void setStatus(int status) {
	this.status = status;
}
public void setIs_owner(boolean is_owner) {
	this.is_owner = is_owner;
}
public void setType(String type) {
	if (type != null && !type.equalsIgnoreCase("null")){
		this.type = type;
	} else {
		this.type = DEFAULT_TYPE;
	}
}
public void setCreator_fullname(String creator_fullname) {

	this.creator_fullname = EMPTY_ENTRY;
	if (creator_fullname != null && !creator_fullname.equalsIgnoreCase("null"))
	this.creator_fullname = creator_fullname;
}
public void setCreator_contact_id(int creator_contact_id) {
	this.creator_contact_id = creator_contact_id;
}
public void setIcon(String icon) {
		if (icon != null && !icon.equalsIgnoreCase("null"))
			this.icon = icon;
		else
			this.icon = DEFAULT_ICON;
}
public void setDescription(String description_) {
	if (description_ != null && !description_.equalsIgnoreCase("null"))
		this.description_ = description_;
	else
		this.description_ = DEFAULT_DESCRIPTION;
}
public void setLocation(String location) {
	if (location == null || location.equalsIgnoreCase("null"))
		this.location = EMPTY_ENTRY;
	else
		this.location = location;
}
public void setAccomodation(String accomodation) {
	if (accomodation == null || accomodation.equalsIgnoreCase("null"))
		this.accomodation = EMPTY_ENTRY;
	else
	this.accomodation = accomodation;
}
public void setCost(String cost) {
	this.cost = cost;
}
public void setTake_with_you(String take_with_you) {
	if (take_with_you == null || take_with_you.equalsIgnoreCase("null"))
		this.take_with_you = EMPTY_ENTRY;
	else this.take_with_you = take_with_you;
}
public void setGo_by(String go_by) {
	if (go_by == null || go_by.equalsIgnoreCase("null"))
		this.go_by = EMPTY_ENTRY;
	else this.go_by = go_by;
}
public void setCountry(String country) {
	if (country == null || country.equalsIgnoreCase("null"))
		this.country = EMPTY_ENTRY;
	else this.country = country;
}
public void setCity(String city) {
	if (city == null || city.equalsIgnoreCase("null"))
		this.city = EMPTY_ENTRY;
	else this.city = city;
}
public void setStreet(String street) {
	if (street == null || street.equalsIgnoreCase("null"))
		this.street = EMPTY_ENTRY;
	else this.street = street;
}
public void setZip(String zip) {
	if (zip == null || zip.equalsIgnoreCase("null"))
		this.zip = EMPTY_ENTRY;
	else this.zip = zip;
}
public void setTimezone(String timezone) {
	this.timezone = timezone;
}
//public void setTime(String time) {
//	this.time = time;
//}
//public void setReminder1(String reminder1) {
//	this.reminder1 = reminder1;
//}
//public void setReminder2(String reminder2) {
//	this.reminder2 = reminder2;
//}
//public void setReminder3(String reminder3) {
//	this.reminder3 = reminder3;
//}
public void setAttendant_1_count(int attendant_1_count) {
	this.attendant_1_count = attendant_1_count;
}
public void setAttendant_2_count(int attendant_2_count) {
	this.attendant_2_count = attendant_2_count;
}
public void setAttendant_0_count(int attendant_0_count) {
	this.attendant_0_count = attendant_0_count;
}
public void setAttendant_4_count(int attendant_4_count) {
	this.attendant_4_count = attendant_4_count;
}
public void setIs_all_day(boolean is_all_day) {
	this.is_all_day = is_all_day;
}
public void setBirthday(boolean birthday) {
	this.birthday = birthday;
}
//public String getAssigned_contacts_DB_entry() {
//	return assigned_contacts_DB_entry;
//}
//public String getAssigned_groups_DB_entry() {
//	return assigned_groups_DB_entry;
//}

public boolean isUploadedToServer() {
	return uploadedToServer;
}
public void setUploadedToServer(boolean bool) {
	uploadedToServer = bool;
}


public int getMessage_count() {
	return message_count;
}
public void setMessage_count(int new_message_count) {
	this.message_count = new_message_count;
}
public int getNew_message_count() {
	return new_message_count;
}
public void setNew_message_count(int new_message_count) {
	this.new_message_count = new_message_count;
}
public long getLast_message_date_time() {
	return last_message_date_time;
}
public void setLast_message_date_time(long last_message_date_time) {
	this.last_message_date_time = last_message_date_time;
}
public long getInternalID() {
	return internalID;
}
public void setInternalID(long internalID) {
	this.internalID = internalID;
}
public long[] getAssigned_contacts() {
	assigned_contacts = new long[getInvited().size()];
	int i = 0;
	for (Invited invite : invited) {
		assigned_contacts[i] = invite.getMy_contact_id();
		i++;
	}
	return assigned_contacts;
}
//public void setAssigned_contacts(long[] assigned_contacts) {
//	this.assigned_contacts = assigned_contacts;
//}
//public long[] getAssigned_groups() {
//	return assigned_groups;
//}
//public void setAssigned_groups(long[] assigned_groups) {
//	this.assigned_groups = assigned_groups;
//}
public void setMyInvite(Invited myInvite) {
	this.myInvite = myInvite;
}
public Invited getMyInvite() {
	return this.myInvite;
}	
public String getPoll() {
	return poll;
}
public void setPoll(String poll) {
	this.poll = poll;
}
public String getSelectedEventPollsTime() {
	return selected_event_polls_time;
}
public void setSelectedEventPollsTime(String selected_event_polls_time) {
	this.selected_event_polls_time = selected_event_polls_time;
}
public String getEvents_day() {
	return events_day;
}
public void setEvents_day(String events_day) {
	this.events_day = events_day;
}
public Template toTemplate(Context context) {
	Template template = new Template();
	
	template.setTitle(getActualTitle());
	template.setColor(getColor());
	template.setIcon(getIcon());
	
	template.setStartCalendar(getStartCalendar());
	template.setEndCalendar(getEndCalendar());
	template.setTimezone(getTimezone());
	
	template.setDescription_(getDescription());
	
	template.setCountry(getCountry());
	template.setCity(getCity());
	template.setStreet(getStreet());
	template.setZip(getZip());
	
	template.setLocation(getLocation());
	template.setGo_by(getGo_by());
	template.setTake_with_you(getTake_with_you());
	template.setCost(getCost());
	template.setAccomodation(getAccomodation());
	
	template.setInvited(getInvited());
	
	template.setTimezoneInUse(TimezoneUtils.getCountryPositionByCc(context, template.getCountry()));
	
	return template;	
}

	public String getEvent_day_start(Context context) {
		if (new Account(context).getSetting_ampm() == 1 && event_day_start != null && event_day_start.matches("[0-9]*:[0-9]* [P,A]M")) {
			return event_day_start;
		} else if (new Account(context).getSetting_ampm() == 0 && event_day_start != null && event_day_start.matches("[0-9]*:[0-9]*")) {
			return event_day_start;
		} else {
			return correctTimeForAMPM(context, event_day_start);
		}
	}

	public void setEvent_day_start(String event_day_start) {
		this.event_day_start = event_day_start;
	}

	public String getEvent_day_end(Context context) {
		if (new Account(context).getSetting_ampm() == 1 && event_day_end != null && event_day_end.matches("[0-9]*:[0-9]* [P,A]M")) {
			return event_day_end;
		} else if (new Account(context).getSetting_ampm() == 0 && event_day_end != null && event_day_end.matches("[0-9]*:[0-9]*")) {
			return event_day_end;
		} else {
			return correctTimeForAMPM(context, event_day_end);
		}
	}

	public void setEvent_day_end(String event_day_end) {
		this.event_day_end = event_day_end;
	}
	
	public String correctTimeForAMPM(Context context, String time) {
		if (time != null && !time.equals(EventsProvider.EMetaData.EventsIndexesMetaData.NOT_TODAY)) {
			String[] times = time.split(":");
			if (new Account(context).getSetting_ampm() == 1) {
				if (Integer.valueOf(times[0]) >= 12) {
					if(Integer.valueOf(times[0]) == 12){
						return times[0] + ":" + times[1] + " PM";
					} else {
						return Integer.valueOf(times[0]) - 12 + ":" + times[1] + " PM";
					}
				} else {
					if(Integer.valueOf(times[0]) == 00){
						return "12:" + times[1] + " AM";
					} else {
						return times[0] + ":" + times[1] + " AM";
					}
				}
			} else {
				if (times[1].matches("[0-9]* PM")) {
					if (Integer.valueOf(times[0]) != 12)
						times[0] = String.valueOf(Integer.valueOf(times[0]) + 12);
					times[1] = times[1].substring(0, times[1].lastIndexOf('P'));
				} else if (times[1].matches("[0-9]* AM")) {
					if (Integer.valueOf(times[0]) == 12)
						times[0] = "00";
					times[1] = times[1].substring(0, times[1].lastIndexOf('A'));
				}
				return times[0] + ":" + times[1];
			}
		} else {
			return time;
		}
	}
}
