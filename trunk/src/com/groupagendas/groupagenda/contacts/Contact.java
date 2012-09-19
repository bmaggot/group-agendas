package com.groupagendas.groupagenda.contacts;

import java.util.Map;

import com.groupagendas.groupagenda.interfaces.Colored;

public class Contact extends Object implements Colored {
	public int contact_id;
	public String name;
	public String lastname;
	public String email;
	public String phone1;
	public String birthdate;
	public String country;
	public String city;
	public String street;
	public String zip;
	public String visibility;
	
	public boolean image;
	public String image_url;
	public String image_thumb_url;
	public byte[] image_bytes = null;
	public boolean remove_image = false;
	
	public String created;
	public String modified;
	public String agenda_view;
	public String registered;
	public Map<String, String> groups = null;

	private String color;
	
	@Override
	public String getColor () {
		if (color != null) {
			return color;
		} else {
			return "00000000";
		}
	}
	
	public void setColor (String color) {
		this.color = color;
	}
}