package com.groupagendas.groupagenda.account;

import java.util.Calendar;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.utils.Utils;

// TODO document class
public class Account {
	SharedPreferences prefs;
	Editor prefsEditor;
	
	public Account() {
		prefs = Data.getmContext().getSharedPreferences("ACCOUNT_DATA", 0);
		prefsEditor = prefs.edit();
	}
	
	// TODO document metadata
	public static class AccountMetaData {
		public static final String U_ID = "user_id";
		
		public static final String NAME = "name";
		public static final String LASTNAME = "lastname";
		public static final String FULLNAME = "fullname";

		public static final String BIRTHDATE = "birthdate";
		public static final String SEX = "sex";
		
		public static final String EMAIL = "email";
		public static final String EMAIL2 = "email2";
		public static final String EMAIL3 = "email3";
		public static final String EMAIL4 = "email4";
		public static final String PHONE1 = "phone1";
		public static final String PHONE2 = "phone2";
		public static final String PHONE3 = "phone3";
		
		public static final String IMAGE = "image";
		public static final String IMAGE_BYTES = "image_bytes";
		public static final String IMAGE_URL = "image_url";
		public static final String IMAGE_THUMB_URL = "image_thumb_url";
		public static final String REMOVE_IMAGE = "remove_image";
		
		public static final String COUNTRY = "country";
		public static final String CITY = "city";
		public static final String STREET = "street";
		public static final String ZIP = "zip";
		
		public static final String TIMEZONE = "timezone";
		public static final String LOCAL_TIME = "local_time";
		public static final String LANGUAGE = "language";
		
		public static final String SETTING_DEFAULT_VIEW = "setting_default_view";
		public static final String SETTING_DATE_FORMAT = "setting_date_format";
		public static final String SETTING_AMPM = "setting_ampm";
		
		public static final String GOOGLE_CALENDAR_LINK = "google_calendar_link";
		
		public static final String COLOR_MY_EVENT = "color_my_event";
		public static final String COLOR_ATTENDING = "color_attending";
		public static final String COLOR_PENDING = "color_pending";
		public static final String COLOR_INVITATION = "color_invitation";
		public static final String COLOR_NOTES = "color_notes";
		public static final String COLOR_BIRTHDAY = "color_birthday";
		
		public static final String CREATED = "created";
		public static final String MODIFIED = "modified";
		public static final String NEED_UPDATE = "need_update";
	}
	
	public int getUser_id() {
		return prefs.getInt(AccountMetaData.U_ID, 0);
	}
	
	public String getName() {
		return prefs.getString(AccountMetaData.NAME, "");
	}
	
	public String getLastname() {
		return prefs.getString(AccountMetaData.LASTNAME, "");
	}
	
	public String getFullname() {
		return prefs.getString(AccountMetaData.FULLNAME, "");
	}
	
	public Calendar getBirthdate() {
		long birthdate = prefs.getLong(AccountMetaData.BIRTHDATE, 0);
		Calendar birthdateCalendar = Utils.createCalendar(birthdate, getTimezone());
		
		return birthdateCalendar;
	}
	
	public String getSex() {
		return prefs.getString(AccountMetaData.SEX, "null");
	}
	
	public String getEmail() {
		return prefs.getString(AccountMetaData.EMAIL, "");
	}
	
	public String getEmail2() {
		return prefs.getString(AccountMetaData.EMAIL2, "");
	}
	
	public String getEmail3() {
		return prefs.getString(AccountMetaData.EMAIL3, "");
	}
	
	public String getEmail4() {
		return prefs.getString(AccountMetaData.EMAIL4, "");
	}
	
	public String getPhone1() {
		return prefs.getString(AccountMetaData.PHONE1, "");
	}
	
	public String getPhone2() {
		return prefs.getString(AccountMetaData.PHONE2, "");
	}
	
	public String getPhone3() {
		return prefs.getString(AccountMetaData.PHONE3, "");
	}
	
	public boolean getImage() {
		return prefs.getBoolean(AccountMetaData.IMAGE, false);
	}
	
	public byte[] image_bytes = null;
	
	public String getImage_url() {
		return prefs.getString(AccountMetaData.IMAGE_URL, "");
	}
	
	public String getImage_thumb_url() {
		return prefs.getString(AccountMetaData.IMAGE_THUMB_URL, "");
	}
	
	public int getRemove_image() {
		return prefs.getInt(AccountMetaData.REMOVE_IMAGE, 0);
	}
	
	public String getCountry() {
		return prefs.getString(AccountMetaData.COUNTRY, Data.getmContext().getResources().getStringArray(R.array.country_values)[78]);
	}
	
	public String getCity() {
		return prefs.getString(AccountMetaData.CITY, "");
	}
	
	public String getStreet() {
		return prefs.getString(AccountMetaData.STREET, "");
	}
	
	public String getZip() {
		return prefs.getString(AccountMetaData.ZIP, "");
	}
	
	public String getTimezone() {
		return prefs.getString(AccountMetaData.TIMEZONE, Data.getmContext().getResources().getStringArray(R.array.timezones)[172]);
	}
	
// TODO wtfis public String local_time;
	public String getLocal_time() {
		return prefs.getString(AccountMetaData.LOCAL_TIME, "");
	}
	
	public String getLanguage() {
		return prefs.getString(AccountMetaData.LANGUAGE, Data.getmContext().getResources().getStringArray(R.array.language_values)[78]);
	}
	
	public String getSetting_default_view() {
		return prefs.getString(AccountMetaData.SETTING_DEFAULT_VIEW, "M");
	}
	
	public String getSetting_date_format() {
		return prefs.getString(AccountMetaData.SETTING_DATE_FORMAT, "yyyy-MM-dd");
	}
	public int getSetting_ampm() {
		return prefs.getInt(AccountMetaData.SETTING_AMPM, 0);
	}
	
	public String getGoogle_calendar_link() {
		return prefs.getString(AccountMetaData.GOOGLE_CALENDAR_LINK, "");
	}
	
	public String getColor_my_event() {
		return prefs.getString(AccountMetaData.COLOR_MY_EVENT, "000000");
	}
	
	public String getColor_attending() {
		return prefs.getString(AccountMetaData.COLOR_ATTENDING, "000000");
	}
	
	public String getColor_pending() {
		return prefs.getString(AccountMetaData.COLOR_PENDING, "000000");
	}
	
	public String getColor_invitation() {
		return prefs.getString(AccountMetaData.COLOR_INVITATION, "000000");
	}
	
	public String getColor_notes() {
		return prefs.getString(AccountMetaData.COLOR_NOTES, "000000");
	}
	
	public String getColor_birthday() {
		return prefs.getString(AccountMetaData.COLOR_BIRTHDAY, "000000");
	}
	
	public Calendar getCreated() {
		long millis = prefs.getLong(AccountMetaData.CREATED, 0);
		return Utils.createCalendar(millis, getTimezone());
	}
	
	public Calendar getModified() {
		long millis = prefs.getLong(AccountMetaData.MODIFIED, 0);
		return Utils.createCalendar(millis, getTimezone());
	}
	
	public int getNeed_update() {
		return prefs.getInt(AccountMetaData.NEED_UPDATE, 0);
	}
	
	public void setUser_id(int user_id) {
		prefsEditor.putInt(AccountMetaData.U_ID, user_id);
		prefsEditor.commit();
	}
	
	public void setName(String name) {
		prefsEditor.putString(AccountMetaData.NAME, name);
		prefsEditor.commit();
	}
	
	public void setLastname(String lastname) {
		prefsEditor.putString(AccountMetaData.LASTNAME, lastname);
		prefsEditor.commit();
	}
	
	public void setFullname(String fullname) {
		prefsEditor.putString(AccountMetaData.FULLNAME, fullname);
		prefsEditor.commit();
	}
	
	public void setBirthdate(Calendar birthdate) {
		long millis = Utils.millisToUnixTimestamp(birthdate.getTimeInMillis());
		prefsEditor.putLong(AccountMetaData.BIRTHDATE, millis);
		prefsEditor.commit();
	}
	
	public void setSex(String sex) {
		prefsEditor.putString(AccountMetaData.SEX, sex);
		prefsEditor.commit();
	}
	
	public void setEmail(String email, int fieldNo) {
		switch (fieldNo) {
			case 0:
				prefsEditor.putString(AccountMetaData.EMAIL, email);
			case 2:
				prefsEditor.putString(AccountMetaData.EMAIL2, email);
			case 3:
				prefsEditor.putString(AccountMetaData.EMAIL3, email);
			case 4:
				prefsEditor.putString(AccountMetaData.EMAIL4, email);
		}
		prefsEditor.commit();
	}
	
	public void setPhone(String phone, int fieldNo) {
		switch (fieldNo) {
			case 1:
				prefsEditor.putString(AccountMetaData.PHONE1, phone);
			case 2:
				prefsEditor.putString(AccountMetaData.PHONE2, phone);
			case 3:
				prefsEditor.putString(AccountMetaData.PHONE3, phone);
		}
		prefsEditor.commit();
	}
	
	public void setImage(Boolean image) {
		prefsEditor.putBoolean(AccountMetaData.IMAGE, image);
		prefsEditor.commit();
	}
	
	public void setImage_url(String image_url) {
		prefsEditor.putString(AccountMetaData.IMAGE_URL, image_url);
		prefsEditor.commit();
	}
	
	public void setImage_thumb_url(String image_thumb_url) {
		prefsEditor.putString(AccountMetaData.IMAGE_THUMB_URL, image_thumb_url);
		prefsEditor.commit();
	}
	
	public void setRemove_image(int remove_image) {
		prefsEditor.putInt(AccountMetaData.REMOVE_IMAGE, remove_image);
		prefsEditor.commit();
	}
	
	public void setCountry(String country) {
		prefsEditor.putString(AccountMetaData.COUNTRY, country);
		prefsEditor.commit();
	}
	
	public void setCity(String city) {
		prefsEditor.putString(AccountMetaData.CITY, city);
		prefsEditor.commit();
	}
	
	public void setStreet(String street) {
		prefsEditor.putString(AccountMetaData.STREET, street);
		prefsEditor.commit();		
	}
	
	public void setZip(String zip) {
		prefsEditor.putString(AccountMetaData.ZIP, zip);
		prefsEditor.commit();		
	}
	
	public void setTimezone(String timezone) {
		prefsEditor.putString(AccountMetaData.TIMEZONE, timezone);
		prefsEditor.commit();		
	}
	
	public void setLocal_time(String local_time) {
		prefsEditor.putString(AccountMetaData.LOCAL_TIME, local_time);
		prefsEditor.commit();
	}
	
	public void setLanguage(String language) {
		prefsEditor.putString(AccountMetaData.LANGUAGE, language);
		prefsEditor.commit();
	}
	
	public void setSetting_default_view(String setting_default_view) {
		prefsEditor.putString(AccountMetaData.SETTING_DEFAULT_VIEW, setting_default_view);
		prefsEditor.commit();		
	}
	
	public void setSetting_date_format(String setting_date_format) {
		prefsEditor.putString(AccountMetaData.SETTING_DATE_FORMAT, setting_date_format);
		prefsEditor.commit();
	}
	
	public void setSetting_ampm(String setting_ampm) {
		prefsEditor.putString(AccountMetaData.SETTING_AMPM, setting_ampm);
		prefsEditor.commit();
	}
	
	public void setGoogle_calendar_link(String google_calendar_link) {
		prefsEditor.putString(AccountMetaData.GOOGLE_CALENDAR_LINK, google_calendar_link);
		prefsEditor.commit();		
	}
	
	public void setColor_my_event(String color_my_event) {
		prefsEditor.putString(AccountMetaData.COLOR_MY_EVENT, color_my_event);
		prefsEditor.commit();
	}
	
	public void setColor_attending(String color_attending) {
		prefsEditor.putString(AccountMetaData.COLOR_MY_EVENT, color_attending);
		prefsEditor.commit();
	}
	
	public void setColor_pending(String color_pending) {
		prefsEditor.putString(AccountMetaData.COLOR_MY_EVENT, color_pending);
		prefsEditor.commit();
	}

	public void setColor_invitation(String color_invitation) {
		prefsEditor.putString(AccountMetaData.COLOR_MY_EVENT, color_invitation);
		prefsEditor.commit();
	}

	public void setColor_notes(String color_notes) {
		prefsEditor.putString(AccountMetaData.COLOR_MY_EVENT, color_notes);
		prefsEditor.commit();
	}

	public void setColor_birthday(String color_birthday) {
		prefsEditor.putString(AccountMetaData.COLOR_MY_EVENT, color_birthday);
		prefsEditor.commit();
	}

	public void setCreated(long created) {
		prefsEditor.putLong(AccountMetaData.CREATED, created);
		prefsEditor.commit();
	}
	
	public void setModified(long modified) {
		prefsEditor.putLong(AccountMetaData.MODIFIED, modified);
		prefsEditor.commit();
	}
	
	public void setNeed_update(int need_update) {
		prefsEditor.putInt(AccountMetaData.NEED_UPDATE, need_update);
		prefsEditor.commit();
	}
}