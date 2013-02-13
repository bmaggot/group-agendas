package com.groupagendas.groupagenda.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.groupagendas.groupagenda.R;

public class Prefs {
	// private static String DEFAULT_SERVER_URL =
	// "http://www.groupagendas.com/";
	private static String DEFAULT_SERVER_URL = "http://159.253.136.156/";

	private static String DEFAULT_EMAIL = "";
	private static String DEFAULT_PASSWORD = "";
	private static String DEFAULT_COLOR_CODE = "#FFFFFF";

	private String DEFAULT_TIMEZONE = null;

	// private static String DEFAULT_SERVER_URL = "http://ga.imprint.lt/";
	// private static String DEFAULT_SERVER_URL = "http://79.98.26.141/";

	private SharedPreferences _prefs = null;
	private Editor _editor = null;

	private String token = null;
	private String timezone = null;
	private String serverUrl = DEFAULT_SERVER_URL;
	private String email = DEFAULT_EMAIL;
	private String password = DEFAULT_PASSWORD;
	private String emailKey = null;
	private String errorDescription = null;
	private String errorCategory = null;
	private int user_id;
	private String colorMyEvent, colorAttending, colorPending, colorInvitation, colorNotes;

	public Prefs(Context context) {
		_prefs = context.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE);
		_editor = _prefs.edit();

		if (DEFAULT_TIMEZONE == null)
			DEFAULT_TIMEZONE = context.getResources().getStringArray(R.array.timezones)[0];
	}

	public String getValue(String key, String defaultValue) {
		if (this._prefs == null)
			return "";
		return _prefs.getString(key, defaultValue);
	}

	public void setValue(String key, String value) {
		if (this._editor == null)
			return;
		_editor.putString(key, value);
	}

	public String getToken() {
		if (this._prefs == null)
			return null;
		this.token = this._prefs.getString("token", null);
		return this.token;
	}

	public void setToken(String token) {
		if (this._editor == null)
			return;
		this._editor.putString("token", token);
	}

	public String getServerUrl() {
		if (this._prefs == null)
			return DEFAULT_SERVER_URL;
		this.serverUrl = this._prefs.getString("serverurl", DEFAULT_SERVER_URL);
		return this.serverUrl;
	}

	public void setServerUrl(String server) {
		if (this._editor == null)
			return;
		this._editor.putString("serverurl", server);
	}

	public String getEmail() {
		if (this._prefs == null)
			return DEFAULT_EMAIL;
		this.email = this._prefs.getString("email", DEFAULT_EMAIL);
		return this.email;
	}

	public void setEmail(String email) {
		if (this._editor == null)
			return;
		this._editor.putString("email", email);
	}

	public String getPassword() {
		if (this._prefs == null)
			return DEFAULT_PASSWORD;
		this.password = this._prefs.getString("password", DEFAULT_PASSWORD);
		return this.password;
	}

	public void setPassword(String pass) {
		if (this._editor == null)
			return;
		this._editor.putString("password", pass);
	}

	public String getEmailKey() {
		if (this._prefs == null)
			return null;
		this.emailKey = this._prefs.getString("emailkey", null);
		return this.emailKey;
	}

	public void setEmailKey(String key) {
		if (this._editor == null)
			return;
		this._editor.putString("emailkey", key);
	}

	public String getErrorDescription() {
		if (this._prefs == null)
			return null;
		this.errorDescription = this._prefs.getString("errordescription", null);
		return this.errorDescription;
	}

	public void setErrorDescription(String error) {
		if (this._editor == null)
			return;
		this._editor.putString("errordescription", error);
	}

	public String getErrorCategory() {
		if (this._prefs == null)
			return null;
		this.errorCategory = this._prefs.getString("errorcategory", null);
		return this.errorCategory;
	}

	public void setErrorCategory(String error) {
		if (this._editor == null)
			return;
		this._editor.putString("errorcategory", error);
	}

	public String getColorMyEvent() {
		if (this._prefs == null)
			return DEFAULT_COLOR_CODE;
		this.colorMyEvent = this._prefs.getString("colormyevent", DEFAULT_COLOR_CODE);
		return this.colorMyEvent;
	}

	public void setColorMyEvent(String color) {
		if (this._editor == null)
			return;
		this._editor.putString("colormyevent", color);
	}

	public String getColorAttending() {
		if (this._prefs == null)
			return DEFAULT_COLOR_CODE;
		this.colorAttending = this._prefs.getString("colorattending", DEFAULT_COLOR_CODE);
		return this.colorAttending;
	}

	public void setColorAttending(String color) {
		if (this._editor == null)
			return;
		this._editor.putString("colorattending", color);
	}

	public String getColorPending() {
		if (this._prefs == null)
			return DEFAULT_COLOR_CODE;
		this.colorPending = this._prefs.getString("colorpending", DEFAULT_COLOR_CODE);
		return this.colorPending;
	}

	public void setColorPending(String color) {
		if (this._editor == null)
			return;
		this._editor.putString("colorpending", color);
	}

	public String getColorInvitation() {
		if (this._prefs == null)
			return DEFAULT_COLOR_CODE;
		this.colorInvitation = this._prefs.getString("colorinvitation", DEFAULT_COLOR_CODE);
		return this.colorInvitation;
	}

	public void setColorInvitation(String color) {
		if (this._editor == null)
			return;
		this._editor.putString("colorinvitation", color);
	}

	public String getColorNotes() {
		if (this._prefs == null)
			return DEFAULT_COLOR_CODE;
		this.colorNotes = this._prefs.getString("colornotes", DEFAULT_COLOR_CODE);
		return this.colorNotes;
	}

	public void setColorNotes(String color) {
		if (this._editor == null)
			return;
		this._editor.putString("colornotes", color);
	}

	public int getUserId() {
		if (this._prefs == null)
			return 0;
		this.user_id = this._prefs.getInt("userid", 0);
		return this.user_id;
	}

	public void setUserId(int id) {
		if (this._editor == null)
			return;
		this._editor.putInt("userid", id);
	}

	public String getTimezone() {
		if (this._prefs == null)
			return DEFAULT_TIMEZONE;
		this.timezone = this._prefs.getString("timezone", DEFAULT_TIMEZONE);
		return this.timezone;
	}

	public void setTimezone(String tz) {
		if (this._editor == null)
			return;
		this._editor.putString("timezone", tz);
	}

	public void save() {
		if (this._editor == null)
			return;
		this._editor.commit();
	}
	
	public interface CalendarElements {		
		String SETTING_MORNING_START = "morning_start";
		String SETTING_MORNING_END = "morning_end";
		String SETTING_AFTERNOON_START = "afternoon_start";
		String SETTING_AFTERNOON_END = "afternoon_end";
		String SETTING_EVENING_START = "evening_start";
		String SETTING_EVENING_END = "evening_end";
	}
}
