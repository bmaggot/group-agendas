package com.groupagendas.groupagenda.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.groupagendas.groupagenda.DataManagement;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Prefs;

@SuppressWarnings("unused") 
class Data {

	private static String pushId;
	private static Prefs prefs;
	private static SharedPreferences _prefs;
	private static DataManagement _instance = null;

	private static Context mContext;

	private static String ERROR = null;

	private boolean loadAccountData = false;
	private boolean loadContactsData = false;
	private boolean loadGroupsData = false;
	private boolean loadEventsData = false;

	private static String DEFAULT_SERVER_URL = "http://159.253.136.156/";

	private static String DEFAULT_EMAIL = "";
	private static String DEFAULT_PASSWORD = "";
	private static String DEFAULT_COLOR_CODE = "#FFFFFF";

	private static String DEFAULT_TIMEZONE = null;

	private static Editor _editor = null;

	private static String token = null;
	private static String timezone = null;
	private static String serverUrl = DEFAULT_SERVER_URL;
	private static String email = DEFAULT_EMAIL;
	private static String password = DEFAULT_PASSWORD;
	private static String emailKey = null;
	private static String errorDescription = null;
	private static String errorCategory = null;
	private static boolean logged;
	private static int user_id;
	private static String colorMyEvent, colorAttending, colorPending, colorInvitation, colorNotes;
	
	private static Account account;
	private static ArrayList<Contact> contacts;
	private static ArrayList<Group> groups;
	private static ArrayList<Event> events;
}
