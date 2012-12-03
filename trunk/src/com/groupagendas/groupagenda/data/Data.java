package com.groupagendas.groupagenda.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.chat.ChatThreadObject;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.utils.Prefs;

public class Data {

	private static String pushId;
	private static Prefs prefs;
	private static SharedPreferences _prefs;
	private static DataManagement _instance = null;

	private static Context mContext;

	public static String ERROR = null;
	public final static String CONNECTION_ERROR = "Connection refused";

	private static boolean loadAccountData = false;
	private static boolean loadContactsData = false;
	private static boolean loadGroupsData = false;
	private static boolean loadEventsData = false;

	private static final String DEFAULT_SERVER_URL = "http://dev.groupagendas.com/";

	private static String DEFAULT_EMAIL = "";
	private static String DEFAULT_PASSWORD = "";
	private static String DEFAULT_COLOR_CODE = "#FFFFFF";

	private static String DEFAULT_TIMEZONE = null;

	private static Editor _editor = null;

//	private static String timezone = null;
	private static String serverUrl = DEFAULT_SERVER_URL;
	private static String email = DEFAULT_EMAIL;
	private static String password = DEFAULT_PASSWORD;
	private static String emailKey = null;
	private static String errorDescription = null;
	private static String errorCategory = null;
	private static boolean logged;
	private static int user_id;
	private static String colorMyEvent, colorAttending, colorPending, colorInvitation, colorNotes;


//	private static TreeMap<Calendar, ArrayList<Event>> sortedEvents;
	private static ArrayList<ChatMessageObject> chatMessages = new ArrayList<ChatMessageObject>();
	private static ArrayList<ChatThreadObject> chatThreads = new ArrayList<ChatThreadObject>();

	public static boolean needToClearData = true;
	public static String localPrefix = "";

	public static boolean newEventPar = false;
	public static boolean showSaveButtonInContactsForm = false;
	public static Event eventForSavingNewInvitedPersons = null;

	// Contact import parameters
	public static boolean returnedFromContactImport = false;
	public static boolean returnedFromContactAuth = false;
	public static boolean credentialsClear = false;
	public static int[] importStats = null;
	
	// Template load parameter
	public static int templateInUse = 0;

	protected static String getPushId() {
		return pushId;
	}

	protected static void setPushId(String pushId) {
		Data.pushId = pushId;
	}

	protected static Prefs getPrefs() {
		return prefs;
	}

	protected static void setPrefs(Prefs prefs) {
		Data.prefs = prefs;
	}

	protected static SharedPreferences get_prefs() {
		return _prefs;
	}

	protected static void set_prefs(SharedPreferences _prefs) {
		Data._prefs = _prefs;
	}

	protected static DataManagement get_instance() {
		return _instance;
	}

	protected static void set_instance(DataManagement _instance) {
		Data._instance = _instance;
	}

	public static Context getmContext() {
		return mContext;
	}

	protected static void setmContext(Context mContext) {
		Data.mContext = mContext;
	}

	protected static String getERROR() {
		return ERROR;
	}

	protected static void setERROR(String eRROR) {
		ERROR = eRROR;
	}

	protected static String getConnectionError() {
		return CONNECTION_ERROR;
	}

	protected static boolean isLoadAccountData() {
		return loadAccountData;
	}

	protected static void setLoadAccountData(boolean loadAccountDataN) {
		loadAccountData = loadAccountDataN;
	}

	protected static boolean isLoadContactsData() {
		return loadContactsData;
	}

	protected static void setLoadContactsData(boolean loadContactsDataN) {
		loadContactsData = loadContactsDataN;
	}

	protected static boolean isLoadGroupsData() {
		return loadGroupsData;
	}

	protected static void setLoadGroupsData(boolean loadGroupsDataN) {
		loadGroupsData = loadGroupsDataN;
	}

	protected static boolean isLoadEventsData() {
		return loadEventsData;
	}

	protected static void setLoadEventsData(boolean loadEventsDataN) {
		loadEventsData = loadEventsDataN;
	}

	protected static String getDEFAULT_EMAIL() {
		return DEFAULT_EMAIL;
	}

	protected static void setDEFAULT_EMAIL(String dEFAULT_EMAIL) {
		DEFAULT_EMAIL = dEFAULT_EMAIL;
	}

	protected static String getDEFAULT_PASSWORD() {
		return DEFAULT_PASSWORD;
	}

	protected static void setDEFAULT_PASSWORD(String dEFAULT_PASSWORD) {
		DEFAULT_PASSWORD = dEFAULT_PASSWORD;
	}

	protected static String getDEFAULT_COLOR_CODE() {
		return DEFAULT_COLOR_CODE;
	}

	protected static void setDEFAULT_COLOR_CODE(String dEFAULT_COLOR_CODE) {
		DEFAULT_COLOR_CODE = dEFAULT_COLOR_CODE;
	}

	protected static String getDEFAULT_TIMEZONE() {
		return DEFAULT_TIMEZONE;
	}

	protected static void setDEFAULT_TIMEZONE(String dEFAULT_TIMEZONE) {
		DEFAULT_TIMEZONE = dEFAULT_TIMEZONE;
	}

	protected static Editor get_editor() {
		return _editor;
	}

	protected static void set_editor(Editor _editor) {
		Data._editor = _editor;
	}

	@Deprecated
	public static String getToken() {
		SharedPreferences sPrefs = mContext.getSharedPreferences("LATEST_CREDENTIALS", 0);
		String response = sPrefs.getString("token", "");
		return response;
	}
	public static String getToken(Context context) {
		SharedPreferences sPrefs = context.getSharedPreferences("LATEST_CREDENTIALS", 0);
		String response = sPrefs.getString("token", "");
		return response;
	}

	protected static void setToken(String token) {
		Editor editor = mContext.getSharedPreferences("LATEST_CREDENTIALS", 0).edit();
		editor.putString("token", token);
		editor.commit();
	}

//	protected static String getTimezone() {
//		return timezone;
//	}

//	protected static void setTimezone(String timezone) {
//		Data.timezone = timezone;
//	}

	public static String getServerUrl() {
		return serverUrl;
	}

	protected static void setServerUrl(String serverUrl) {
		Data.serverUrl = serverUrl;
	}

	protected static String getEmail() {
		return email;
	}

	protected static void setEmail(String email) {
		Data.email = email;
	}

	protected static String getPassword() {
		return password;
	}

	protected static void setPassword(String password) {
		Data.password = password;
	}

	protected static String getEmailKey() {
		return emailKey;
	}

	protected static void setEmailKey(String emailKey) {
		Data.emailKey = emailKey;
	}

	protected static String getErrorDescription() {
		return errorDescription;
	}

	protected static void setErrorDescription(String errorDescription) {
		Data.errorDescription = errorDescription;
	}

	protected static String getErrorCategory() {
		return errorCategory;
	}

	protected static void setErrorCategory(String errorCategory) {
		Data.errorCategory = errorCategory;
	}

	protected static boolean isLogged() {
		return logged;
	}

	protected static void setLogged(boolean logged) {
		Data.logged = logged;
	}

	protected static int getUser_id() {
		return user_id;
	}

	protected static void setUser_id(int user_id) {
		Data.user_id = user_id;
	}

	protected static String getColorMyEvent() {
		return colorMyEvent;
	}

	protected static void setColorMyEvent(String colorMyEvent) {
		Data.colorMyEvent = colorMyEvent;
	}

	protected static String getColorAttending() {
		return colorAttending;
	}

	protected static void setColorAttending(String colorAttending) {
		Data.colorAttending = colorAttending;
	}

	protected static String getColorPending() {
		return colorPending;
	}

	protected static void setColorPending(String colorPending) {
		Data.colorPending = colorPending;
	}

	protected static String getColorInvitation() {
		return colorInvitation;
	}

	protected static void setColorInvitation(String colorInvitation) {
		Data.colorInvitation = colorInvitation;
	}

	protected static String getColorNotes() {
		return colorNotes;
	}

	protected static void setColorNotes(String colorNotes) {
		Data.colorNotes = colorNotes;
	}

	/**
	 * @deprecated Create new object and get value afterwards.
	 * @since 2012-09-29
	 */
//	public static Account getAccount() {
//		return account;
//	}

	/**
	 * @deprecated Create new object and set value afterwards.
	 * @since 2012-09-29
	 */
//	protected static void setAccount(Account account) {
//		Data.account = account;
//	}

	/**
	 * @deprecated Use ContactManagement.getContactsFromLocalDb(null) instead.
	 * @since 2012-09-29
	 */
//	public static ArrayList<Contact> getContacts() {
//		return contacts;
//	}

	/**
	 * @deprecated Use {@link ContactManagement.insertContact(Contact contact)} instead.
	 * @since 2012-09-29
	 */
//	public static void setContacts(ArrayList<Contact> contacts) {
//		Data.contacts = contacts;
//	}

	/**
	 * @deprecated Use ContactManagement.getGroupsFromLocalDb(null) instead.
	 * @since 2012-09-29
	 */
//	protected static ArrayList<Group> getGroups() {
//		return groups;
//	}

	/**
	 * @deprecated Use {@link ContactManagement.insertGroup(Group group)} instead.
	 * @since 2012-09-29
	 */
//	protected static void setGroups(ArrayList<Group> groups) {
//		Data.groups = groups;
//	}




	public static ArrayList<ChatMessageObject> getChatMessages() {
		return chatMessages;
	}

	public static void setChatMessages(ArrayList<ChatMessageObject> chatMessages) {
		Data.chatMessages = chatMessages;
	}

	public static ArrayList<ChatThreadObject> getChatThreads() {
		return chatThreads;
	}

	public static void setChatThreads(ArrayList<ChatThreadObject> chatThreads) {
		Data.chatThreads = chatThreads;
	}

	protected static void setUserId(int id) {
		if (_editor == null)
			return;
		_editor.putInt("userid", id);
	}

	public boolean isNeedToClearData() {
		return needToClearData;
	}


	public void setNeedToClearData(boolean needToClearData) {
		Data.needToClearData = needToClearData;
	}

	public static void save() {
		if (_editor == null)
			return;
		_editor.commit();
	}

}
