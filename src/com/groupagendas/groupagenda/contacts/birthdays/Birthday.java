package com.groupagendas.groupagenda.contacts.birthdays;

import android.content.Context;
import android.database.Cursor;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsProvider;

public class Birthday {
	private String birthday;
	private String birthday_id;
	private String name;
	private String lastName;
	private String contact_id;
	private String country;
	private String timezone;

	public Birthday(Context context, Contact contact) {
		Account acc = new Account(context);
		setContact_id(String.valueOf(contact.contact_id));
		setName(contact.name);
		setLastName(contact.lastname);
		setBirthday(contact.birthdate);
		setCountry(contact.country);
		setTimezone(acc.getTimezone());

	}
	
	public Birthday(Context context, Cursor cur){
		this.contact_id = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID));
		this.birthday_id = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.BirthdaysMetaData.B_ID));
		this.birthday = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE));
		this.country = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.BirthdaysMetaData.COUNTRY));
		this.timezone = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.BirthdaysMetaData.TIMEZONE));
		this.name = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.BirthdaysMetaData.TITLE));
		this.lastName = "";
	}
	
	public String getBirthdayId() {
		return birthday_id;
	}

	public String getBirthday() {
		return birthday;
	}

	public String getName() {
		return name;
	}

	public String getLastName() {
		return lastName;
	}

	public String getContact_id() {
		return contact_id;
	}

	public String getCountry() {
		return country;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setContact_id(String contact_id) {
		this.contact_id = contact_id;
	}

}
