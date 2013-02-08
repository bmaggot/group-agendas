package com.groupagendas.groupagenda.templates;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;

import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;

public class Template {
	private static final String DEFAULT_TITLE = "Untitled";
	private static final String DEFAULT_COLOR = "99D9EA";
	
	private long internalID;
	private int template_id;
	
	private boolean uploadedToServer = true;
	private boolean is_sports_event;	
	private boolean is_all_day;
	
	private String type;
	private String template_title;
	private String title;
	private String icon;
	private String color;
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
	private Calendar startCalendar;
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
	private Invited myInvite;
	
	private int timezoneInUse;
	
	public long getInternalID() {
		return internalID;
	}
	public int getTemplate_id() {
		return template_id;
	}
	public boolean isUploadedToServer() {
		return uploadedToServer;
	}
	public boolean is_sports_event() {
		return is_sports_event;
	}
	public boolean is_all_day() {
		return is_all_day;
	}
	public String getTemplate_title() {
		if ((template_title != null) && (template_title.length() > 1)) {
			return template_title;
		} else {
			return DEFAULT_TITLE + "\tid: " + getTemplate_id();
		}
	}
	public String getType() {
		return type;
	}
	public String getTitle() {
		if ((title != null) && (title.length() > 1)) {
			return title;
		} else {
			return DEFAULT_TITLE + "\tid: "  + template_id;
		}
	}
	public String getIcon() {
		if (icon == null || icon.equalsIgnoreCase("null")) {
			this.icon = "";
		}
		return icon;
	}
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @return resource id of icon image for this event. If event has no icon, 0 will be returned
	 */
	public int getIconId(Context context) {
		return context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
	}
	
	public String getColor() {
		if (color == null || color.equalsIgnoreCase("null")) {
			this.color = DEFAULT_COLOR;
		}

		return color;
	}
	public String getDescription_() {
		if (description_ == null || description_.equalsIgnoreCase("null")) description_ = "";
		return description_;
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
		if(cost != null){
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
		if(city != null){
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
	public String getZip() {
		if(zip != null){
			return zip;
		} else {
			return "";
		}
	}
	public String getTimezone() {
		if(timezone != null){
			return timezone;
		} else {
			return "";
		}
	}
	public Calendar getStartCalendar() {
		return startCalendar;
	}
	public Calendar getEndCalendar() {
		return endCalendar;
	}
	public Calendar getReminder1() {
		return reminder1;
	}
	public Calendar getReminder2() {
		return reminder2;
	}
	public Calendar getReminder3() {
		return reminder3;
	}
	public Calendar getAlarm1() {
		return alarm1;
	}
	public boolean isAlarm1fired() {
		return alarm1fired;
	}
	public Calendar getAlarm2() {
		return alarm2;
	}
	public boolean isAlarm2fired() {
		return alarm2fired;
	}
	public Calendar getAlarm3() {
		return alarm3;
	}
	public boolean isAlarm3fired() {
		return alarm3fired;
	}
	public long getCreated_millis_utc() {
		return created_millis_utc;
	}
	public long getModified_millis_utc() {
		return modified_millis_utc;
	}
	public ArrayList<Invited> getInvited() {
		if (invited == null) invited = new ArrayList<Invited>();
		return invited;
	}
	public long[] getAssigned_contacts() {
		return assigned_contacts;
	}
	public Invited getMyInvite() {
		return myInvite;
	}
	public int getTimezoneInUse() {
		return timezoneInUse;
	}
	public void setInternalID(long internalID) {
		this.internalID = internalID;
	}
	public void setTemplate_id(int template_id) {
		this.template_id = template_id;
	}
	public void setUploadedToServer(boolean uploadedToServer) {
		this.uploadedToServer = uploadedToServer;
	}
	public void setIs_sports_event(boolean is_sports_event) {
		this.is_sports_event = is_sports_event;
	}
	public void setIs_all_day(boolean is_all_day) {
		this.is_all_day = is_all_day;
	}
	public void setTemplate_title(String template_title) {
		this.template_title = template_title;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public void setDescription_(String description_) {
		this.description_ = description_;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setAccomodation(String accomodation) {
		this.accomodation = accomodation;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public void setTake_with_you(String take_with_you) {
		this.take_with_you = take_with_you;
	}
	public void setGo_by(String go_by) {
		this.go_by = go_by;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public void setStartCalendar(Calendar startCalendar) {
		if(startCalendar != null){
			this.startCalendar = startCalendar;
			this.startCalendar.clear(Calendar.SECOND);
			this.startCalendar.clear(Calendar.MILLISECOND);
		}
	}
	public void setEndCalendar(Calendar endCalendar) {
		if(endCalendar != null){
			this.endCalendar = endCalendar;
			this.endCalendar.clear(Calendar.SECOND);
			this.endCalendar.clear(Calendar.MILLISECOND);
		}
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
	public void setAlarm1(Calendar alarm1) {
		this.alarm1 = alarm1;
	}
	public void setAlarm1fired(boolean alarm1fired) {
		this.alarm1fired = alarm1fired;
	}
	public void setAlarm2(Calendar alarm2) {
		this.alarm2 = alarm2;
	}
	public void setAlarm2fired(boolean alarm2fired) {
		this.alarm2fired = alarm2fired;
	}
	public void setAlarm3(Calendar alarm3) {
		this.alarm3 = alarm3;
	}
	public void setAlarm3fired(boolean alarm3fired) {
		this.alarm3fired = alarm3fired;
	}
	public void setCreated_millis_utc(long created_millis_utc) {
		this.created_millis_utc = created_millis_utc;
	}
	public void setModified_millis_utc(long modified_millis_utc) {
		this.modified_millis_utc = modified_millis_utc;
	}
	public void setInvited(ArrayList<Invited> invited) {
		this.invited = invited;
	}
	public void setAssigned_contacts(long[] assigned_contacts) {
		this.assigned_contacts = assigned_contacts;
	}
	public void setMyInvite(Invited myInvite) {
		this.myInvite = myInvite;
	}
	public void setTimezoneInUse(int timezoneInUse) {
		this.timezoneInUse = timezoneInUse;
	}
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		
		if(this.getTemplate_id() != 0){
			cv.put(TemplatesMetaData.T_ID, this.getTemplate_id());
		} else {
			this.setTemplate_id((int)Calendar.getInstance().getTimeInMillis());
			cv.put(TemplatesMetaData.T_ID, this.getTemplate_id());
		}

		cv.put(TemplatesMetaData.UPLOADED_SUCCESSFULLY, this.isUploadedToServer() ? 1 : 0);

		// native events are not held in GA local db so we do not put
		// Event.isNative
		cv.put(TemplatesMetaData.IS_SPORTS_EVENT, this.is_sports_event() ? 1 : 0);
		cv.put(TemplatesMetaData.IS_ALL_DAY, this.is_all_day() ? 1 : 0);

		cv.put(TemplatesMetaData.TITLE, this.getTitle());
//		System.out.println(event.getTitle());
		cv.put(TemplatesMetaData.ICON, this.getIcon());
		cv.put(TemplatesMetaData.COLOR, this.getColor());
		// cv.put(TemplatesMetaData.TEXT_COLOR,
		// event.getTextColor());//2012-10-24
		cv.put(TemplatesMetaData.DESC, this.getDescription_());
		cv.put(TemplatesMetaData.LOCATION, this.getLocation());
		cv.put(TemplatesMetaData.ACCOMODATION, this.getAccomodation());
		cv.put(TemplatesMetaData.COST, this.getCost());
		cv.put(TemplatesMetaData.TAKE_WITH_YOU, this.getTake_with_you());
		cv.put(TemplatesMetaData.GO_BY, this.getGo_by());

		cv.put(TemplatesMetaData.COUNTRY, this.getCountry());
		cv.put(TemplatesMetaData.CITY, this.getCity());
		cv.put(TemplatesMetaData.STREET, this.getStreet());
		cv.put(TemplatesMetaData.ZIP, this.getZip());

		// EVENT TIMES UTC
		cv.put(TemplatesMetaData.TIMEZONE, this.getTimezone());
		cv.put(TemplatesMetaData.TIMEZONE_IN_USE, this.getTimezoneInUse());
		if (this.getStartCalendar() != null)
			cv.put(TemplatesMetaData.TIME_START, this.getStartCalendar().getTimeInMillis());
		if (this.getEndCalendar() != null)
			cv.put(TemplatesMetaData.TIME_END, this.getEndCalendar().getTimeInMillis());

		// reminders
		if (this.getReminder1() != null)
			cv.put(TemplatesMetaData.REMINDER1, this.getReminder1().getTimeInMillis());
		if (this.getReminder2() != null)
			cv.put(TemplatesMetaData.REMINDER2, this.getReminder2().getTimeInMillis());
		if (this.getReminder3() != null)
			cv.put(TemplatesMetaData.REMINDER3, this.getReminder3().getTimeInMillis());

		// TODO alarms DO SOMETHING WITH ALARM FIRED FIELDS
		if (this.getAlarm1() != null)
			cv.put(TemplatesMetaData.ALARM1, this.getAlarm1().getTimeInMillis());
		if (this.getAlarm2() != null)
			cv.put(TemplatesMetaData.ALARM2, this.getAlarm2().getTimeInMillis());
		if (this.getAlarm3() != null)
			cv.put(TemplatesMetaData.ALARM3, this.getAlarm3().getTimeInMillis());

		cv.put(TemplatesMetaData.CREATED, this.getCreated_millis_utc());
		cv.put(TemplatesMetaData.MODIFIED, this.getModified_millis_utc());
		cv.put(TemplatesMetaData.INVITED, EventManagement.parseInvitedListToJSONArray(this.getInvited()));
		// TODO IMPLEMENT INVITED
		
		return cv;
	}
}
