package com.groupagendas.groupagenda;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Contacts.Settings;
import android.provider.Contacts.SettingsColumns;

public class SaveDeletedData {
	SharedPreferences prefs;
	Editor prefsEditor;
	
	public SaveDeletedData(Context context) {
		prefs = context.getSharedPreferences("SAVE_DELETED_DATA", 0);
	    prefsEditor = prefs.edit();
	}
	
	public static class SDMetaData {
		public static final String DELETED_EVENTS = "deleted_events";
		public static final String DELETED_CONTACTS = "deleted_contacts";
		public static final String DELETED_GROUPS = "deleted-groups";
		public static final String SEPARATOR = ",";
	}
	
	public void addEventForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_EVENTS, external_id);
	}
	
	public void addContactForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_CONTACTS, external_id);
	}
	
	public void addGroupForLaterDelete(int external_id){
		addIdToPrefs(SDMetaData.DELETED_GROUPS, external_id);
	}
	
	private void addIdToPrefs(String columnName, int external_id) {
		String entry = prefs.getString(columnName, "");
		if (entry.length() == 0){
			entry+= external_id;
		}else {
			entry+= SDMetaData.SEPARATOR + external_id;
		}
			
		prefsEditor.putString(columnName, entry);
		prefsEditor.commit();
	}
	
	public void clear(){
	    SharedPreferences.Editor editor = prefs.edit();

	    editor.clear();
	    editor.commit();	
	}
	
	
	public String getDELETED_EVENTS() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_EVENTS, "");
	}
	public String getDELETED_CONTACTS() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_CONTACTS,"");
	}
	public String getDELETED_GROUPS() {
		return prefs.getString(SaveDeletedData.SDMetaData.DELETED_GROUPS, "");
	}
	
	
	
}