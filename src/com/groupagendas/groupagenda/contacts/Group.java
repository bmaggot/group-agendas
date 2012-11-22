package com.groupagendas.groupagenda.contacts;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;

import com.groupagendas.groupagenda.utils.MapUtils;


public class Group extends Object{
	public int group_id;
	public String title;
	
	public Long created;
	public Long modified;
	public String deleted;
	
	public boolean image;
	public String image_thumb_url;
	public String image_url;
	public byte[] image_bytes = null;
	public boolean remove_image = false;
	
	public int contact_count;
	public Map<String, String> contacts = null;
	private int internal_id;
	
	public Group() {
		super();
	}
	
	public int getInternal_id() {
		return internal_id;
	}

	public void setInternal_id(int internal_id) {
		this.internal_id = internal_id;
	}
	
	public Group(Context context, Cursor cur) {
		//this.internal_id = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData._ID));
		this.group_id = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
		this.title = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.TITLE));
		this.created = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CREATED));
		this.modified = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED));
		this.deleted = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.DELETED));

		if (cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE)).equals("1")) {
			this.image = true;
		} else {
			this.image = false;
		}

		this.image_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL));
		this.image_thumb_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL));
		this.image_bytes = cur.getBlob(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES));
		if (cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE)).equals("1")) {
			this.remove_image = true;
		} else {
			this.remove_image = false;
		}


		this.contact_count = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT));

		String resp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS));
		if (resp != null) {
			this.contacts = MapUtils.stringToMap(context, cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS)));
		}
	}
}
