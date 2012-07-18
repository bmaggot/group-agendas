package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;




public class Event extends Object{
	
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
	public String color;
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
	
	public Calendar startCalendar;
	public Calendar endCalendar;
	
	public String reminder1;
	public String reminder2;
	public String reminder3;
	
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
	
	
}
