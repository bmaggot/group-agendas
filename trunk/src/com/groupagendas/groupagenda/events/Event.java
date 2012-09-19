package com.groupagendas.groupagenda.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.interfaces.Colored;
import com.groupagendas.groupagenda.utils.Utils;

import android.content.Context;




public class Event extends Object implements Colored {
private static final String DEFAULT_TITLE = "";
private static final String TIMESTAMP_FORMAT = DataManagement.SERVER_TIMESTAMP_FORMAT;

private static final String DEFAULT_REMINDER = "null";

private static final int REMINDER_1 = 1;
private static final int REMINDER_2 = 2;
private static final int REMINDER_3 = 3;

//	TODO set all default fields and getters
	public static String DEFAULT_COLOR = "21C0DB";
	
	public int event_id;
	public int user_id;
	
	public boolean isNative = false;
	
	public boolean is_sports_event;	
	public int status;
	public boolean is_owner;
	public String type;
	
	public String creator_fullname;
    public int creator_contact_id;
	
	public String title;
	public String icon;
	
	
	private String color;
	public String description_;
	
	public String location;
	public String accomodation;
	
	public String cost;
	public String take_with_you;
	public String go_by;
	
	public String country;
	public String city;
	public String street;
	public String zip;
	
	public String timezone;
	public String time_start;
	public String time_end;
	public String time;
	public String my_time_start;
	public String my_time_end;
	
	private Calendar startCalendar;
	private Calendar endCalendar;
	
	
	
	public String reminder1;
	public String reminder2;
	public String reminder3;
	public String alarm1 = "null";
	public boolean alarm1fired = false;
	public String alarm2 = "null";
	public boolean alarm2fired = false;
	public String alarm3 = "null";
	public boolean alarm3fired = false;
	
	public String created;
	public String modified;
	
	public int attendant_1_count; 
	public int attendant_2_count;
	public int attendant_0_count;
	public int attendant_4_count;
	
	public int[] assigned_contacts = null;
	public int[] assigned_groups = null;
	public ArrayList<Invited> invited = null;
	
	public boolean is_all_day;
	public boolean birthday = false;
	
	@Override
	public String toString(){
		String text = this.title;
		return text;
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
		return context.getResources().getIdentifier(this.icon, "drawable", context.getPackageName());
	}

	@Override
	public String getColor() {
		if(color == null || color.equalsIgnoreCase("null")){
			this.color = DEFAULT_COLOR;
		}
		return color;
	}
	
	public void setColor(String color) {
		this.color = DEFAULT_COLOR;
		
		if (color != null)
			if (!color.equalsIgnoreCase("null")) this.color = color;
		
	}
/**
 * @author justinas.marcinka@gmail.com
 * @return true if this event has icon set
 */
	public boolean hasIcon() {
		if (icon == null) return false;
		if (icon.equalsIgnoreCase("null")) return false;
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
		this.startCalendar = startCalendar;
		this.startCalendar.clear(Calendar.SECOND);
		this.startCalendar.clear(Calendar.MILLISECOND);
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
		this.endCalendar = endCalendar;
		this.endCalendar.clear(Calendar.SECOND);
		this.endCalendar.clear(Calendar.MILLISECOND);
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
		if (validateTitle(title) != 0) return DEFAULT_TITLE;
		return title;
	}
	public String getZip() {
		return zip;
	}
	public String getTimezone() {
		return timezone;
	}
	public String getDescription() {
		return description_;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	public int getEvent_id() {
		return event_id;
	}
	public boolean isNative() {
		return isNative;
	}
	public boolean isIs_sports_event() {
		return is_sports_event;
	}
	public int getStatus() {
		return status;
	}
	public boolean isIs_owner() {
		return is_owner;
	}
	public String getCreator_fullname() {
		return creator_fullname;
	}
	public int getCreator_contact_id() {
		return creator_contact_id;
	}
	public String getTitle() {
		return title;
	}
	public String getIcon() {
		return icon;
	}
	public String getDescription_() {
		return description_;
	}
	public String getLocation() {
		return location;
	}
	public String getAccomodation() {
		return accomodation;
	}
	public String getCost() {
		return cost;
	}
	public String getTake_with_you() {
		return take_with_you;
	}
	public String getGo_by() {
		return go_by;
	}
	public String getCountry() {
		return country;
	}
	public String getCity() {
		return city;
	}
	public String getStreet() {
		return street;
	}
	public String getTime() {
		return time;
	}
	public String getReminder1() {
		return reminder1;
	}
	public String getReminder2() {
		return reminder2;
	}
	public String getReminder3() {
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

	public boolean isIs_all_day() {
		return is_all_day;
	}
	public boolean isBirthday() {
		return birthday;
	}
	
		
	public void setReminder1(Calendar reminder1) {
		this.reminder1 = createReminderString(REMINDER_1, reminder1);
	}

	public void setReminder2(Calendar reminder2) {
		this.reminder2 = createReminderString(REMINDER_2, reminder2);
	}

	public void setReminder3(Calendar reminder3) {
		this.reminder3 = createReminderString(REMINDER_3, reminder3);
	}


/**
 * Creates new string for reminder with given id. 
 * @param id identifier for reminder.
 * @see Event.REMINDER_1 Event.REMINDER_2 Event.REMINDER_3
 * @param reminder time in Calendar format
 * @return "null" if it's not in future or is same as already defined reminder. Else, returns string representation for given time
 *  
 */
	private String createReminderString(int id, Calendar reminder) {
		
		if (reminder == null) return DEFAULT_REMINDER;
		
		if (reminder.after(Calendar.getInstance())) //if it is not in future, return "null"
			return DEFAULT_REMINDER;
		
		String reminderStr  = new SimpleDateFormat(TIMESTAMP_FORMAT).format(reminder.getTime());
		
		if (reminderStr.equalsIgnoreCase(reminder1) && id != REMINDER_1) return DEFAULT_REMINDER;
		if (reminderStr.equalsIgnoreCase(reminder2) && id != REMINDER_2) return DEFAULT_REMINDER;
		if (reminderStr.equalsIgnoreCase(reminder3) && id != REMINDER_3) return DEFAULT_REMINDER;
		
		return reminderStr;
	}
	
	
	

	
}
