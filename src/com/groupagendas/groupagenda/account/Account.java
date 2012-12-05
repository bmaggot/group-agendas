package com.groupagendas.groupagenda.account;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.Utils;

// TODO document class
public class Account {
	SharedPreferences prefs;
	Editor prefsEditor;
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
/**
 * @deprecated soon to be removed.
 */
	public Account() {
		prefs = Data.getmContext().getSharedPreferences("ACCOUNT_DATA", 0);
		prefsEditor = prefs.edit();
	}
	
	public Account(Context context) {
		prefs = context.getSharedPreferences("ACCOUNT_DATA", 0);
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
		public static final String DATA_UPDATE_TIMESTAMP = "latest_update";
		public static final String NEED_UPDATE = "need_update";
		public static final String PUSH_ID = "push_id";
		public static final String SESSION_ID = "session_id";

		public static final String SHOW_NATIVE_CALENDARS = "show_native_calendars";
		public static final String SHOW_GA_CALENDARS = "show_ga_calendars";

		public static final String SHOW_BIRTHDAYS_CALENDARS = "show_birthdays_calendars";
		
		public static final String LAST_TIME_CONNECTED_IN_MS = "last_time_connected_in_ms"; 
		
		public static final String RESPONSES = "responses"; 
		public static final String RESPONSES_BADGE = "responsesBadge"; 
	}


	public String getResponses(){
		return prefs.getString(Account.AccountMetaData.RESPONSES, "");
	}
	
	public void setResponses(String responses){
		prefsEditor.putString(Account.AccountMetaData.RESPONSES, responses);
		prefsEditor.commit();
	}
	
	public String getResponsesBadge(){
		return prefs.getString(Account.AccountMetaData.RESPONSES_BADGE, "");
	}
	
	public void setResponsesBadge(String responsesBadge){
		prefsEditor.putString(Account.AccountMetaData.RESPONSES_BADGE, responsesBadge);
		prefsEditor.commit();
	}

	public long getLatestUpdateUnixTimestamp(){
		return prefs.getLong(Account.AccountMetaData.DATA_UPDATE_TIMESTAMP, 0);
	}
	
	public void setLatestUpdateTime(Calendar updateTime){
		prefsEditor.putLong(Account.AccountMetaData.DATA_UPDATE_TIMESTAMP, updateTime.getTimeInMillis() / 1000);
		prefsEditor.commit();
	}

	public String getSessionId(){
		return prefs.getString(Account.AccountMetaData.SESSION_ID, null);
	}
	
	public void setSessionId(String id){
		prefsEditor.putString(Account.AccountMetaData.SESSION_ID, id);
		prefsEditor.commit();
	}
	

	public int getUser_id() {
		return prefs.getInt(Account.AccountMetaData.U_ID, 0);
	}

	public String getName() {
		return prefs.getString(Account.AccountMetaData.NAME, "");
	}

	public String getLastname() {
		return prefs.getString(Account.AccountMetaData.LASTNAME, "");
	}

	public String getFullname() {
		return prefs.getString(Account.AccountMetaData.FULLNAME, "");
	}

	public Calendar getBirthdate() {
		long birthdate = prefs.getLong(Account.AccountMetaData.BIRTHDATE, 0);
		Calendar birthdateCalendar = Utils.createCalendar(birthdate, getTimezone());

		return birthdateCalendar;
	}

	public String getSex() {
		return prefs.getString(Account.AccountMetaData.SEX, "null");
	}

	public String getEmail() {
		return prefs.getString(Account.AccountMetaData.EMAIL, "");
	}

	public String getEmail2() {
		return prefs.getString(Account.AccountMetaData.EMAIL2, "");
	}

	public String getEmail3() {
		return prefs.getString(Account.AccountMetaData.EMAIL3, "");
	}

	public String getEmail4() {
		return prefs.getString(Account.AccountMetaData.EMAIL4, "");
	}

	public String getPhone1() {
		return prefs.getString(Account.AccountMetaData.PHONE1, "");
	}

	public String getPhone2() {
		return prefs.getString(Account.AccountMetaData.PHONE2, "");
	}

	public String getPhone3() {
		return prefs.getString(Account.AccountMetaData.PHONE3, "");
	}
	
	public String getPushId() {
		return prefs.getString(Account.AccountMetaData.PUSH_ID, "");
	}

	public boolean getImage() {
		return prefs.getBoolean(Account.AccountMetaData.IMAGE, false);
	}

	public byte[] image_bytes = null;

	public String getImage_url() {
		return prefs.getString(Account.AccountMetaData.IMAGE_URL, "");
	}

	public String getImage_thumb_url() {
		return prefs.getString(Account.AccountMetaData.IMAGE_THUMB_URL, "");
	}

	public int getRemove_image() {
		return prefs.getInt(Account.AccountMetaData.REMOVE_IMAGE, 0);
	}

	public String getCountry() {
		return prefs.getString(Account.AccountMetaData.COUNTRY,
				Data.getmContext().getResources().getStringArray(R.array.countries)[275]);
	}

	public String getCity() {
		return prefs.getString(Account.AccountMetaData.CITY, "");
	}

	public String getStreet() {
		return prefs.getString(Account.AccountMetaData.STREET, "");
	}

	public String getZip() {
		return prefs.getString(Account.AccountMetaData.ZIP, "");
	}

	public String getTimezone() {
		return prefs.getString(Account.AccountMetaData.TIMEZONE, "Europe/London");
	}

	// TODO wtfis public String local_time;
	public String getLocal_time() {
		return prefs.getString(Account.AccountMetaData.LOCAL_TIME, "");
	}

	public String getLanguage() {
		return prefs.getString(Account.AccountMetaData.LANGUAGE, "english");
	}

	public String getSetting_default_view() {
		return prefs.getString(Account.AccountMetaData.SETTING_DEFAULT_VIEW, "M");
	}

	public String getSetting_date_format() {
		return prefs.getString(Account.AccountMetaData.SETTING_DATE_FORMAT, DataManagement.SERVER_TIMESTAMP_FORMAT);
	}

	public int getSetting_ampm() {
		return prefs.getInt(Account.AccountMetaData.SETTING_AMPM, 0);
	}

	public String getGoogle_calendar_link() {
		return prefs.getString(Account.AccountMetaData.GOOGLE_CALENDAR_LINK, "");
	}

	public String getColor_my_event() {
		return prefs.getString(Account.AccountMetaData.COLOR_MY_EVENT, "000000");
	}

	public String getColor_attending() {
		return prefs.getString(Account.AccountMetaData.COLOR_ATTENDING, "000000");
	}

	public String getColor_pending() {
		return prefs.getString(Account.AccountMetaData.COLOR_PENDING, "000000");
	}

	public String getColor_invitation() {
		return prefs.getString(Account.AccountMetaData.COLOR_INVITATION, "000000");
	}

	public String getColor_notes() {
		return prefs.getString(Account.AccountMetaData.COLOR_NOTES, "000000");
	}

	public String getColor_birthday() {
		return prefs.getString(Account.AccountMetaData.COLOR_BIRTHDAY, "000000");
	}

	public Calendar getCreated() {
		long millis = prefs.getLong(Account.AccountMetaData.CREATED, 0);
		return Utils.createCalendar(millis, getTimezone());
	}

	public Calendar getModified() {
		long millis = prefs.getLong(Account.AccountMetaData.MODIFIED, 0);
		return Utils.createCalendar(millis, getTimezone());
	}

	public int getNeed_update() {
		return prefs.getInt(Account.AccountMetaData.NEED_UPDATE, 0);
	}

	public boolean getShow_native_calendars() {
		return prefs.getBoolean(Account.AccountMetaData.SHOW_NATIVE_CALENDARS, false);
	}
	
	public boolean getShow_ga_calendars(){
		return prefs.getBoolean(Account.AccountMetaData.SHOW_GA_CALENDARS, true);
	}
	
	public boolean getShow_birthdays_calendars(){
		return prefs.getBoolean(Account.AccountMetaData.SHOW_BIRTHDAYS_CALENDARS, true);
	}
	
	public void setUser_id(int user_id) {
		prefsEditor.putInt(Account.AccountMetaData.U_ID, user_id);
		prefsEditor.commit();
	}

	public void setName(String name) {
		prefsEditor.putString(Account.AccountMetaData.NAME, name);
		prefsEditor.commit();
	}

	public void setLastname(String lastname) {
		prefsEditor.putString(Account.AccountMetaData.LASTNAME, lastname);
		prefsEditor.commit();
	}

	public void setFullname(String fullname) {
		prefsEditor.putString(Account.AccountMetaData.FULLNAME, fullname);
		prefsEditor.commit();
	}

	public void setBirthdate(Calendar birthdate) {
		long millis = Utils.millisToUnixTimestamp(birthdate.getTimeInMillis());
		prefsEditor.putLong(Account.AccountMetaData.BIRTHDATE, millis);
		prefsEditor.commit();
	}

	public void setSex(String sex) {
		prefsEditor.putString(Account.AccountMetaData.SEX, sex);
		prefsEditor.commit();
	}

	public void setEmail(String email, int fieldNo) {
		switch (fieldNo) {
		case 0:
			prefsEditor.putString(Account.AccountMetaData.EMAIL, email);
		case 2:
			prefsEditor.putString(Account.AccountMetaData.EMAIL2, email);
		case 3:
			prefsEditor.putString(Account.AccountMetaData.EMAIL3, email);
		case 4:
			prefsEditor.putString(Account.AccountMetaData.EMAIL4, email);
		}
		prefsEditor.commit();
	}

	public void setPhone(String phone, int fieldNo) {
		switch (fieldNo) {
		case 1:
			prefsEditor.putString(Account.AccountMetaData.PHONE1, phone);
		case 2:
			prefsEditor.putString(Account.AccountMetaData.PHONE2, phone);
		case 3:
			prefsEditor.putString(Account.AccountMetaData.PHONE3, phone);
		}
		prefsEditor.commit();
	}

	public void setPushId(String pushId) {
		prefsEditor.putString(Account.AccountMetaData.PUSH_ID, pushId);
		prefsEditor.commit();
	}
	
	public void setImage(Boolean image) {
		prefsEditor.putBoolean(Account.AccountMetaData.IMAGE, image);
		prefsEditor.commit();
	}

	public void setImage_url(String image_url) {
		prefsEditor.putString(Account.AccountMetaData.IMAGE_URL, image_url);
		prefsEditor.commit();
	}

	public void setImage_thumb_url(String image_thumb_url) {
		prefsEditor.putString(Account.AccountMetaData.IMAGE_THUMB_URL, image_thumb_url);
		prefsEditor.commit();
	}

	public void setRemove_image(int remove_image) {
		prefsEditor.putInt(Account.AccountMetaData.REMOVE_IMAGE, remove_image);
		prefsEditor.commit();
	}

	public void setCountry(String country) {
		prefsEditor.putString(Account.AccountMetaData.COUNTRY, country);
		prefsEditor.commit();
	}

	public void setCity(String city) {
		prefsEditor.putString(Account.AccountMetaData.CITY, city);
		prefsEditor.commit();
	}

	public void setStreet(String street) {
		prefsEditor.putString(Account.AccountMetaData.STREET, street);
		prefsEditor.commit();
	}

	public void setZip(String zip) {
		prefsEditor.putString(Account.AccountMetaData.ZIP, zip);
		prefsEditor.commit();
	}

	public void setTimezone(String timezone) {
		prefsEditor.putString(Account.AccountMetaData.TIMEZONE, timezone);
		prefsEditor.commit();
	}

	public void setLocal_time(String local_time) {
		prefsEditor.putString(Account.AccountMetaData.LOCAL_TIME, local_time);
		prefsEditor.commit();
	}

	public void setLanguage(String language) {
		prefsEditor.putString(Account.AccountMetaData.LANGUAGE, language);
		prefsEditor.commit();
	}

	public void setSetting_default_view(String setting_default_view) {
		prefsEditor.putString(Account.AccountMetaData.SETTING_DEFAULT_VIEW, setting_default_view);
		prefsEditor.commit();
	}

	public void setSetting_date_format(String setting_date_format) {
		if (!setting_date_format.equals("null")) {
			setting_date_format = setting_date_format.replace("mm", "MM");
			prefsEditor.putString(Account.AccountMetaData.SETTING_DATE_FORMAT, setting_date_format);
		} else {
			prefsEditor.putString(Account.AccountMetaData.SETTING_DATE_FORMAT, Account.DEFAULT_DATE_FORMAT);
		}
		prefsEditor.commit();
	}

	public void setSetting_ampm(int setting_ampm) {
		prefsEditor.putInt(Account.AccountMetaData.SETTING_AMPM, setting_ampm);
		prefsEditor.commit();
	}

	public void setGoogle_calendar_link(String google_calendar_link) {
		prefsEditor.putString(Account.AccountMetaData.GOOGLE_CALENDAR_LINK, google_calendar_link);
		prefsEditor.commit();
	}

	public void setColor_my_event(String color_my_event) {
		prefsEditor.putString(Account.AccountMetaData.COLOR_MY_EVENT, color_my_event);
		prefsEditor.commit();
	}

	public void setColor_attending(String color_attending) {
		prefsEditor.putString(Account.AccountMetaData.COLOR_MY_EVENT, color_attending);
		prefsEditor.commit();
	}

	public void setColor_pending(String color_pending) {
		prefsEditor.putString(Account.AccountMetaData.COLOR_MY_EVENT, color_pending);
		prefsEditor.commit();
	}

	public void setColor_invitation(String color_invitation) {
		prefsEditor.putString(Account.AccountMetaData.COLOR_MY_EVENT, color_invitation);
		prefsEditor.commit();
	}

	public void setColor_notes(String color_notes) {
		prefsEditor.putString(Account.AccountMetaData.COLOR_MY_EVENT, color_notes);
		prefsEditor.commit();
	}

	public void setColor_birthday(String color_birthday) {
		prefsEditor.putString(Account.AccountMetaData.COLOR_MY_EVENT, color_birthday);
		prefsEditor.commit();
	}

	public void setCreated(long created) {
		prefsEditor.putLong(Account.AccountMetaData.CREATED, created);
		prefsEditor.commit();
	}

	public void setModified(long modified) {
		prefsEditor.putLong(Account.AccountMetaData.MODIFIED, modified);
		prefsEditor.commit();
	}

	public void setNeed_update(int need_update) {
		prefsEditor.putInt(Account.AccountMetaData.NEED_UPDATE, need_update);
		prefsEditor.commit();
	}
	
	public void setShow_native_calendars(boolean state) {
		prefsEditor.putBoolean(Account.AccountMetaData.SHOW_NATIVE_CALENDARS, state);
		prefsEditor.commit();
	}
	
	public void setShow_ga_calendars(boolean state) {
		prefsEditor.putBoolean(Account.AccountMetaData.SHOW_GA_CALENDARS, state);
		prefsEditor.commit();
	}

	public void setShow_birthdays_calendars(boolean state) {
		prefsEditor.putBoolean(Account.AccountMetaData.SHOW_BIRTHDAYS_CALENDARS, state);
		prefsEditor.commit();
	}

	public void clearLatestUpdateTime() {
		prefsEditor.remove(AccountMetaData.DATA_UPDATE_TIMESTAMP);
		prefsEditor.commit();
	}
	
	public long getLastTimeConnectedToWeb() {
		return prefs.getLong(Account.AccountMetaData.LAST_TIME_CONNECTED_IN_MS,
				0);
	}

	public void setLastTimeConnectedToWeb(Calendar updateTime) {
		prefsEditor.putLong(Account.AccountMetaData.LAST_TIME_CONNECTED_IN_MS, updateTime.getTimeInMillis());
		prefsEditor.commit();
	}
	
	public void clearLastTimeConnectedToweb() {
		prefsEditor.remove(AccountMetaData.LAST_TIME_CONNECTED_IN_MS);
		prefsEditor.commit();
	}
	
	
}

