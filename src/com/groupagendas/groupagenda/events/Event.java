package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.groupagendas.groupagenda.R;

import android.content.ContentProviderResult;
import android.content.Context;




public class Event extends Object{
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
	
	@Override
	public String toString(){
		String text = this.title;
		return text;
	}
	
	public int getColorBubbleId(Context context){
		
		String color = this.color;
		
		if (color == null) color = "";
		if (color.equalsIgnoreCase("null")) color = "";
		
		String bubbletitle = "calendarbubble_" + color + "_";
		
		int imgID = context.getResources().getIdentifier(bubbletitle, "drawable", context.getPackageName());
		return imgID;
	}

	public int getIconId(Context context) {
		return context.getResources().getIdentifier(this.icon, "drawable", context.getPackageName());
	}

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
	

	
}
