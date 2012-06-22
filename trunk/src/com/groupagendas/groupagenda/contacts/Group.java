package com.groupagendas.groupagenda.contacts;

import java.util.Map;


public class Group extends Object{
	public int group_id;
	public String title;
	
	public String created;
	public String modified;
	public String deleted;
	
	public boolean image;
	public String image_thumb_url;
	public String image_url;
	public byte[] image_bytes = null;
	public boolean remove_image = false;
	
	public int contact_count;
	public Map<String, String> contacts = null;
}
