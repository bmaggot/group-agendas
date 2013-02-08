package com.groupagendas.groupagenda;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SaveDeletedData {
	SharedPreferences prefs;
	Editor prefsEditor;
	
	public SaveDeletedData(Context context) {
		prefs = context.getSharedPreferences("SAVE_DELETED_DATA", Context.MODE_PRIVATE);
	    prefsEditor = prefs.edit();
	}
	
	public static class SDMetaData {
		public static final String DELETED_EVENTS = "deleted_events";
		public static final String DELETED_TEMPLATES = "deleted-templates";
		public static final String DELETED_CONTACTS = "deleted_contacts";
		public static final String DELETED_GROUPS = "deleted-groups";
		public static final String DELETED_ADDRESSES = "deleted-addresses";
		public static final String SEPARATOR = ",";
	}
	
	public void addEventForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_EVENTS, external_id);
	}
	
	public void addTemplateForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_TEMPLATES, external_id);
	}
	
	public boolean addContactForLaterDelete(int external_id){
		String entry = prefs.getString(SaveDeletedData.SDMetaData.DELETED_CONTACTS, "");
		
		if (entry.length() == 0) {
			entry += external_id;
		} else {
			entry += SDMetaData.SEPARATOR + external_id;
		}
			
		prefsEditor.putString(SaveDeletedData.SDMetaData.DELETED_CONTACTS, entry);
		return prefsEditor.commit();
	}
	
	public void addGroupForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_GROUPS, external_id);
	}
	
	private boolean addIdToPrefs(String columnName, int external_id) {
		String entry = prefs.getString(columnName, "");
		
		if (entry.length() == 0) {
			entry += external_id;
		} else {
			entry += SDMetaData.SEPARATOR + external_id;
		}
			
		prefsEditor.putString(columnName, entry);
		return prefsEditor.commit();
	}
	
	public void addAddressForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_ADDRESSES, external_id);
	}
	
	public void clear(int state){

	    switch (state) {
		case 1:
			prefsEditor.putString(SaveDeletedData.SDMetaData.DELETED_CONTACTS, "");
			break;
		case 2:
			prefsEditor.putString(SaveDeletedData.SDMetaData.DELETED_GROUPS, "");
			break;
		case 3:
			prefsEditor.putString(SaveDeletedData.SDMetaData.DELETED_EVENTS, "");
			break;
		case 4:
			prefsEditor.putString(SaveDeletedData.SDMetaData.DELETED_ADDRESSES, "");
			break;
		case 5:
			prefsEditor.putString(SaveDeletedData.SDMetaData.DELETED_TEMPLATES, "");
			break;
		}
	    
	    prefsEditor.commit();	
	}
	
	
	public String getDELETED_EVENTS() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_EVENTS, "");
	}
	
	public String getDELETED_CONTACTS() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_CONTACTS, "");
	}
	
	public String getDELETED_GROUPS() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_GROUPS, "");
	}
	public String getDELETED_ADDRESSES() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_ADDRESSES, "");
	}
	
	public String getDELETED_TEMPLATES() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_TEMPLATES, "");
	}
	
}