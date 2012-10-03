package com.groupagendas.groupagenda.events;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Invited {
	public static final int REJECTED = 0;
	public static final int ACCEPTED = 1;
	public static final int MAYBE = 2;
	public static final int NEW_INVITATION = 4;

	/** Invited person's id (if person exists in user's contact list) */
	private int my_contact_id;
	
	/** Invited person's fullname */
	private String name;
	
	/** Invited person's id (if person exists in other user's contact list
	 * and is not registered in GroupAgendas) */
	private int gcid;
	
	/** Invited person's GroupAgendas user id */
	private int guid;
	
	/** Invitation status */
	private int status;

	public int getMy_contact_id() {
		return my_contact_id;
	}

	public String getName() {
		return name;
	}

	public int getGcid() {
		return gcid;
	}

	public int getGuid() {
		return guid;
	}

	public int getStatus() {
		return status;
	}

	public void setMy_contact_id(int my_contact_id) {
		this.my_contact_id = my_contact_id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGcid(int gcid) {
		this.gcid = gcid;
	}

	public void setGuid(int guid) {
		this.guid = guid;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public Invited() {
	}
	
	public Invited(JSONObject input) {
		try {
			setName(input.getString("gname"));
		} catch (JSONException e) {
			Log.e("Invited(JSONObject input)", "Failed getting name");
		}
		
		try {
			setGcid(input.getInt("gcid"));
		} catch (JSONException e) {
			Log.e("Invited(JSONObject input)", "Failed getting gcid");
		}
		
		try {
			setGuid(input.getInt("guid"));
		} catch (JSONException e) {
			Log.e("Invited(JSONObject input)", "Failed getting guid");
		}
		
		try {
			setStatus(input.getInt("status"));
		} catch (JSONException e) {
			Log.e("Invited(JSONObject input)", "Failed getting status");
		}
		
		try {
			setMy_contact_id(input.getInt("my_contact_id"));
		} catch (JSONException e) {
			Log.e("Invited(JSONObject input)", "Failed getting my_contact_id");
		}
	}
	
	public String toString() {
		String result = "";
		
		return result;
	}
}