package com.groupagendas.groupagenda.contacts;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;

import com.groupagendas.groupagenda.interfaces.Colored;
import com.groupagendas.groupagenda.utils.MapUtils;

public class Contact extends Object implements Colored {
	public int contact_id;
	public String name;
	public String lastname;
	public String email;
	public String phone1;
	public String phone1_code;
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
	
	public long created;
	public long modified;
	public String agenda_view;
	public String registered;
	public Map<String, String> groups = null;

	private String color;
	private int internal_id;
	
	@Override
	public String getColor () {
		if (color != null) {
			return color;
		} else {
			return "000000";
		}
	}
	
	public void setColor (String color) {
		this.color = color;
	}
	
	public Contact() {
		super();
	}
	
	public int getInternal_id() {
		return internal_id;
	}

	public void setInternal_id(int internal_id) {
		this.internal_id = internal_id;
	}

	public Contact(Context context, Cursor cur) {
		this.internal_id = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData._ID));
		this.contact_id = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
		this.name = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.NAME));
		this.lastname = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME));

		this.email = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
		this.phone1 = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.PHONE));
		this.phone1_code = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE));

		this.birthdate = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE));

		this.country = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY));
		this.city = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CITY));
		this.street = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.STREET));
		this.zip = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.ZIP));

		this.visibility = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY));

		if (cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE)).equals("1")) {
			this.image = true;
		} else {
			this.image = false;
		}

		this.image_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL));
		this.image_thumb_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL));
		this.image_bytes = cur.getBlob(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES));
		if (cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE)).equals("1")) {
			this.remove_image = true;
		} else {
			this.remove_image = false;
		}

		this.agenda_view = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW));
		this.registered = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED));

		this.created = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CREATED));
		this.modified = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED));

		String resp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.GROUPS));
		if (resp != null) {
			this.groups = MapUtils.stringToMap(context,cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.GROUPS)));
		}
		
		this.setColor(cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COLOR)));
	}
}