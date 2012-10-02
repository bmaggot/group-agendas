package com.groupagendas.groupagenda.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.Invited;
import com.groupagendas.groupagenda.utils.Utils;


public class EventManagement {
	private static final String CLASS_NAME = "EventManagement.class";
	private static SimpleDateFormat day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
	private static SimpleDateFormat month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
	private static String user_timezone = CalendarSettings.getTimeZone();
	
	
	private static final String GET_EVENTS_FROM_REMOTE_DB_URL = "mobile/events_list";
	
	
	private static final String TOKEN = "token";
	private static final String CATEGORY = "category";
	
	
	public static boolean networkAvailable = true; //TODO remove hardcode
	
	////////////////////////////METHODS THAT ARE USED BY UI////////////////////////////////////////////////////////////////////
	
	/**
	 * @author justinas.marcinka@gmail.com Method creates event in both remote
	 *         and local databases. If there is no connectivity to remote DB,
	 *         event should be created only in local DB and saved task to upload
	 *         data when available.
	 * @param event
	 */
	public static void createNewEvent(Context context, Event event) {

		if (networkAvailable) {//TODO TEST
			int id = createEventInRemoteDb(event);

			if (id > 0) {
				event.setEvent_id(id);
				event.setUploadedToServer(true);
			} else {
				// TODO report error
			}
		} else {
			event.setEvent_id(0);
			event.setUploadedToServer(false);
		}	
		
		insertEventToLocalDB(context, event);

	}

	/**
	 * @author justinas.marcinka@gmail.com Method deletes event from both remote
	 *         and local databases. If there is no connectivity to remote DB,
	 *         event should be deleted only in local DB and saved task to delete
	 *         data from remote db when available.
	 * @param event
	 */
	public static void deleteEvent(Context context, int event_id) {
		Boolean deletedFromRemote = false;
		if (networkAvailable) {
			 deletedFromRemote = removeEvent(event_id);
		}

		if (!deletedFromRemote){
			//TODO add delete event task to tasks list for server remote
		}	
		
		deleteEventFromLocalDb(context, event_id);	
	}
	
	
	
	/**
	 * @author justinas.marcinka@gmail.com Updates event in both local and
	 *         remote db with given info
	 * @param event
	 */
	public static void updateEvent(Context context, Event event) {
		if (networkAvailable) {
			event.setUploadedToServer(editEvent(event));
		} else {
			event.setUploadedToServer(false);
		}	
		updateEventInLocalDb(context, event);
	}
	
	
	public static final int TM_EVENTS_FROM_GIVEN_DATE = 0;
	public static final int TM_EVENTS_ON_GIVEN_DAY = 1;
	public static final int TM_EVENTS_ON_GIVEN_MONTH = 2;
	public static final int TM_EVENTS_ON_GIVEN_YEAR = 3;

	/**
	 * @author justinas.marcinka@gmail.com Gets events projections from local
	 *         database, according to given date and time mode.
	 * @param projection
	 *            columns to get from events provider
	 * @param date
	 *            date on which events are selected according to eventTimeMode
	 * @param daysToSelect
	 *            selected time range end in days. Used only with timeMode
	 *            TM_EVENTS_FROM_GIVEN_DATE. If set 0, all events from given
	 *            date are selected
	 * @param eventTimeMode
	 *            available time modes:<br>
	 *            DataManagement.TM_EVENTS_FROM_GIVEN_DATE<br>
	 *            DataManagement.TM_EVENTS_ON_GIVEN_DAY <br>
	 *            DataManagement.TM_EVENTS_ON_GIVEN_MONTH<br>
	 *            DataManagement.TM_EVENTS_ON_GIVEN_YEAR<br>
	 * @param sortOrder
	 *            sort order. if null, default will be used
	 * @param context 
	 * @return cursor holding projections that met selection criteria. Caller
	 *         has to set Event objects after return
	 */
	public static Cursor createEventProjectionByDateFromLocalDb(Context context, String[] projection, Calendar date, int daysToSelect, int eventTimeMode,
			String sortOrder, boolean filterRejected) {
		day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
		month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
		String where;
		
		Uri uri;
		if (date != null) {
			switch (eventTimeMode) {
			case TM_EVENTS_FROM_GIVEN_DATE:
				
				if (daysToSelect > 0) {
					uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;
					Calendar tmpStart = (Calendar) date.clone();
					Calendar tmpEnd = (Calendar) date.clone();
					tmpEnd.add(Calendar.DATE, daysToSelect - 1);
					StringBuilder sb = new StringBuilder("(");
					sb.append('\'');
					sb.append(day_index_formatter.format(tmpStart.getTime()));
					sb.append('\'');
					tmpStart.add(Calendar.DATE, 1);
					while (!tmpStart.after(tmpEnd)) {
						sb.append(',');
						sb.append('\'');
						sb.append(day_index_formatter.format(tmpStart.getTime()));
						sb.append('\'');
						tmpStart.add(Calendar.DATE, 1);
					}
					sb.append(")");
					String inStringDay = sb.toString();
					// TODO optimisation by using months column
					where = EventsProvider.EMetaData.EventsIndexesMetaData.DAY + " IN " + inStringDay;

				} else {
					uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
					where = EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS + ">" + date.getTimeInMillis();
				}

			
				break;
			case TM_EVENTS_ON_GIVEN_DAY:

				uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;
				where = EventsProvider.EMetaData.EventsIndexesMetaData.DAY + " = '" + day_index_formatter.format(date.getTime()) + "'";
				break;
			case TM_EVENTS_ON_GIVEN_MONTH:
				uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;
				where = EventsProvider.EMetaData.EventsIndexesMetaData.MONTH + " = '" + month_index_formatter.format(date.getTime()) + "'" ;
				break;
			case TM_EVENTS_ON_GIVEN_YEAR:
				uri = EventsProvider.EMetaData.EVENTS_ON_DATE_URI;

				Calendar tmp = (Calendar) date.clone();
				Utils.setCalendarToFirstDayOfYear(tmp);
				StringBuilder sb = new StringBuilder("(");
				sb.append('\'');
				sb.append(month_index_formatter.format(tmp.getTime()));
				sb.append('\'');
				tmp.add(Calendar.MONTH, 1);
				for (int i = 0; i < 11; i++) {
					sb.append(',');
					sb.append('\'');
					sb.append(month_index_formatter.format(tmp.getTime()));
					sb.append('\'');
					tmp.add(Calendar.MONTH, 1);
				}
				sb.append(")");
				String inString = sb.toString();

				where = EventsProvider.EMetaData.EventsIndexesMetaData.MONTH + " IN " + inString;
				break;

			default:
				throw new IllegalStateException("Wrong event Time mode for projection");
			}
		} else {
			where = null;
			uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
		}
		
		String rejectedFilter = " AND " + EventsProvider.EMetaData.EventsMetaData.STATUS + "!=" + Event.REJECTED;
		if (filterRejected) where += rejectedFilter;
		
		return context.getContentResolver().query(uri, projection, where, null, sortOrder);

	}
	
	
	
	/**
	 * Gets events from remote Database and writes them to local DB.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param eventCategory
	 *            API category. if empty, gets all events
	 * @return
	 */
	public static void getEventsFromRemoteDb(Context context, String eventCategory) {
		boolean success = false;
		ArrayList<Event> events = new ArrayList<Event>();
		Event event = null;

		try {
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + GET_EVENTS_FROM_REMOTE_DB_URL);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
			reqEntity.addPart(CATEGORY, new StringBody(eventCategory));
			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						// // error = object.getString("error");
					} else {
						JSONArray es = object.getJSONArray("events");
						int count = es.length();
						for (int i = 0; i < count; i++) {
							JSONObject e = es.getJSONObject(i);

							event = createEventFromJSON(e);
							if (event != null) {
								insertEventToLocalDB(context, event);
								if (event.getStatus() != Event.REJECTED)
									events.add(event);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					ex.getMessage());
		}

//		TODO handle birthdays if (contactsBirthdays != null && !contactsBirthdays.isEmpty()) {
//			events.addAll(contactsBirthdays);
//		}
	}
	
	/////////////////////////////////////////////////////METHODS THAT WORK WITH LOCAL DB//////////////////////////////////////////////////////
	
	
	/**
	 * Inserts given event to local DB. Rows that correspond to given ID are overwritten.
	 * @param context
	 * @param event
	 */
	protected static void insertEventToLocalDB(Context context, Event event) {

		// 1. ADD EVENT details to events table
		ContentValues cv = createCVforEventsTable(event);
		context.getContentResolver().insert(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, cv);
		// 2. INSERT EVENT day indexes into events_days table
		insertEventToDayIndexTable(context, event);
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * @param context 
	 * @param event
	 */
//TODO javadoc	
	public static void updateEventInLocalDb(Context context, Event event) {
		ContentValues cv = createCVforEventsTable(event);

		int ID = event.getEvent_id();
		long createTime = event.getCreatedUtc();
		cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, ID);
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS, createTime);
		Uri uri;
		String where = null;
			// 1 update event in events table
			if (ID > 0) {
				uri = Uri
						.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI
								+ "/" + event.getEvent_id());
			} else {
				uri = EventsProvider.EMetaData.EventsMetaData.CONTENT_URI;
				where = EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS + "=" + event.getCreatedUtc();
			}
			
			context.getContentResolver().update(uri, cv, null, null);

			// 2 TODO get event from local db and compare if start and end times
			// differ
			boolean eventTimeChanged = true; // temprorary
			String[] projection = {EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS};
//			Cursor result = 
			// 3 Renew event data in time indexes TODO do only when event time
			// changed;

			if (eventTimeChanged) {
				where = EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID + "=" + event.getEvent_id();
				context.getContentResolver().delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, where, null);
				insertEventToDayIndexTable(context, event);
			}
		

	}

	
	//TODO javadoc
	private static void deleteEventFromLocalDb(Context context, int event_id) {
		String where;

		// 1. Deleting event from events table
		where = EventsProvider.EMetaData.EventsMetaData.E_ID + "=" + event_id;
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, where, null);

		// 2. Deleting event from events day indexes table
		where = EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID + "=" + event_id;
		context.getContentResolver().delete(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, where, null);

	}
		
	//TODO javadoc
	private static void insertEventToDayIndexTable(Context context, Event event) {
		Calendar eventDayStart = (Calendar) event.getStartCalendar().clone();
		eventDayStart.set(Calendar.HOUR_OF_DAY, 0);
		eventDayStart.set(Calendar.MINUTE, 0);
		eventDayStart.set(Calendar.SECOND, 0);
		eventDayStart.set(Calendar.MILLISECOND, 0);

		int event_id = event.getEvent_id();

		if (event.is_all_day) { // only one row is inserted
			insertEventDayIndexRow(context, event_id, eventDayStart);
		} else
			while (eventDayStart.before(event.getEndCalendar())) { // rows are
																	// inserted
																	// for each
																	// day that
																	// event
																	// lasts
				insertEventDayIndexRow(context, event_id, eventDayStart);
				eventDayStart.add(Calendar.DATE, 1);

			}
	}

	//TODO javadoc
	private static void insertEventDayIndexRow(Context context, int event_id, Calendar eventDayStart) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.EVENT_ID, event_id);
		Date time = eventDayStart.getTime();

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.DAY, day_index_formatter.format(time));

		cv.put(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH, month_index_formatter.format(time));
		context.getContentResolver().insert(EventsProvider.EMetaData.EventsIndexesMetaData.CONTENT_URI, cv);

	}

	//TODO javadoc
		public static Event getEventFromLocalDb(Context context, int event_id) {
			Event item = null;
			Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI + "/" + event_id);
			Cursor result = context.getContentResolver().query(uri, null, null, null, null);
			if (result.moveToFirst()) {
				item = createEventFromCursor(result);

				String assigned_contacts = result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS));
				if (assigned_contacts != null && !assigned_contacts.equals("null")) {
					if (assigned_contacts.length() == 0) item.setAssigned_contacts(new int[0]);
					else try {
						item.setAssigned_contacts(Utils.jsonStringToArray(assigned_contacts));
					} catch (JSONException e) {
						Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
								e.getMessage());
						item.setAssigned_contacts(new int[0]);
					}
				}

				String assigned_groups = item.getAssigned_groups_DB_entry();
				if (assigned_groups != null && !assigned_groups.equals("null")) {
					if (assigned_groups.length() == 0) item.setAssigned_groups(new int[0]);
					else try {
						item.setAssigned_groups(Utils.jsonStringToArray(assigned_groups));
					} catch (JSONException e) {
						Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
								e.getMessage());
						item.setAssigned_groups(new int[0]);
					}
				}

				String invitedJson = item.getInvited_DB_entry();
				if (invitedJson != null && !invitedJson.equals("null")) {
					ArrayList<Invited> invitedList = new ArrayList<Invited>();
					try {

						JSONArray arr = new JSONArray(invitedJson);
						if (arr.length() > 0) {
							

							for (int i = 0, l = arr.length(); i < l; i++) {
								JSONObject obj = arr.getJSONObject(i);

								final Invited invited = new Invited();

								try {
									invited.status_id = obj.getInt("status");

									if (invited.status_id == 4) {
										invited.status = context.getString(R.string.status_2);
									} else {
										String statusStr = new StringBuilder("status_").append(invited.status_id).toString();
										int statusId = context.getResources()
												.getIdentifier(statusStr, "string", "com.groupagendas.groupagenda");

										invited.status = context.getString(statusId);
									}
								} catch (JSONException ex) {
									Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName()
											.toString(), ex.getMessage());
								}

								try {
									Account acc = new Account();
									if (!obj.getString("my_contact_id").equals("null")) {
										invited.my_contact_id = obj.getInt("my_contact_id");
										Contact contact = ContactManagement.getContactFromLocalDb(context, invited.my_contact_id, 0);

										if (contact.email != null)
											invited.email = contact.email;
										else
											invited.email = "";

										if (contact.name != null && contact.lastname != null)
											invited.name = contact.name + " " + contact.lastname;
										else
											invited.name = " ";

										if (contact.contact_id > 0)
											invited.contactId = contact.contact_id;
										else
											getEventFromLocalDb(context, event_id);
									} else if (acc.getFullname().equals(obj.getString("gname"))) {
										invited.name = Data.getmContext().getString(R.string.you);
										invited.email = Data.getEmail();
										invited.me = true;
									} else {
										invited.name = obj.getString("gname");
										String tmp = obj.getString("gcid");
										if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
											invited.gcid = Integer.parseInt(tmp);
										} else {
											invited.gcid = 0;
										}
										tmp = obj.getString("guid");
										if (!tmp.equalsIgnoreCase("null") && tmp.matches("[0-9]*")) {
											invited.guid = Integer.parseInt(tmp);
										} else {
											invited.guid = 0;
										}
										tmp = obj.getString("my_contact_id");
										if (tmp.equalsIgnoreCase("null")) {
											invited.inMyList = false;
										} else {
											invited.inMyList = true;
										}
									}
								} catch (JSONException ex) {
									Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName()
											.toString(), ex.getMessage());
								}
								invitedList.add(invited);
							}

							item.setInvited(invitedList);
						}
					} catch (JSONException e) {
						Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
								e.getMessage());
					}
				}
			}
			result.close();
			return item;
		}	

		
/////////////////////////////////////////////////////METHODS THAT WORK WITH RMOTE DB//////////////////////////////////////////////////////	
		
		/**
		 * Creates event in remote DB and returns event ID if success.
		 * 
		 * @param e
		 *            - event to create
		 * @return event id. If create failed, returns 0.
		 */
		public static int createEventInRemoteDb(Event e) {
			boolean success = false;

			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_create");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));

				if (e.icon != null)
					reqEntity.addPart("icon", new StringBody(e.icon));

				reqEntity.addPart("color", new StringBody(e.getColor()));

				reqEntity.addPart("title", new StringBody(e.title));

				reqEntity.addPart("timestamp_start_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis())));
				reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis())));

				if (e.description_ != null) {
					reqEntity.addPart("description", new StringBody(e.description_));
				} else {
					reqEntity.addPart("description", new StringBody(""));
				}

				if (e.country != null && e.country.length() > 0)
					reqEntity.addPart("country", new StringBody(e.country));
				if (e.city != null && e.city.length() > 0)
					reqEntity.addPart("city", new StringBody(e.city));
				if (e.street != null && e.street.length() > 0)
					reqEntity.addPart("street", new StringBody(e.street));
				if (e.zip != null && e.zip.length() > 0)
					reqEntity.addPart("zip", new StringBody(e.zip));
				reqEntity.addPart("timezone", new StringBody(e.timezone));

				if (e.location != null && e.location.length() > 0)
					reqEntity.addPart("location", new StringBody(e.location));
				if (e.go_by != null && e.go_by.length() > 0)
					reqEntity.addPart("go_by", new StringBody(e.go_by));
				if (e.take_with_you != null && e.take_with_you.length() > 0)
					reqEntity.addPart("take_with_you", new StringBody(e.take_with_you));
				if (e.cost != null && e.cost.length() > 0)
					reqEntity.addPart("cost", new StringBody(e.cost));
				if (e.accomodation != null && e.accomodation.length() > 0)
					reqEntity.addPart("accomodation", new StringBody(e.accomodation));

				if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
					e.assigned_contacts = new int[Data.selectedContacts.size()];
					int i = 0;
					for (Contact contact : Data.selectedContacts) {
						e.assigned_contacts[i] = contact.contact_id;
						i++;
					}
				}
				if (e.assigned_contacts != null) {
					for (int i = 0, l = e.assigned_contacts.length; i < l; i++) {
						reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.assigned_contacts[i])));
					}
				} else {
					reqEntity.addPart("contacts[]", new StringBody(""));
				}
				if (e.assigned_groups != null) {
					for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
						reqEntity.addPart("groups[]", new StringBody(String.valueOf(e.assigned_groups[i])));
					}
				} else {
					reqEntity.addPart("groups[]", new StringBody(""));
				}

				if (e.alarm1 != null) {
					reqEntity.addPart("alarm1", new StringBody(e.alarm1));
				}
				if (e.alarm2 != null) {
					reqEntity.addPart("alarm2", new StringBody(e.alarm2));
				}
				if (e.alarm3 != null) {
					reqEntity.addPart("alarm3", new StringBody(e.alarm3));
				}

				if (e.reminder1 != null) {
					reqEntity.addPart("reminder1", new StringBody(e.reminder1));
				}
				if (e.reminder2 != null) {
					reqEntity.addPart("reminder2", new StringBody(e.reminder2));
				}
				if (e.reminder3 != null) {
					reqEntity.addPart("reminder3", new StringBody(e.reminder3));
				}

				if (e.birthday) {
					reqEntity.addPart("bd", new StringBody("1"));
				}
				post.setEntity(reqEntity);

				if (networkAvailable) {
					HttpResponse rp = hc.execute(post);

					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							// Log.e("createEvent - resp", resp + "!!!");
							JSONObject object = new JSONObject(resp);
							success = object.getBoolean("success");

							// Log.e("createEvent - success", "" + success);

							if (!success) {
								Log.e("Create event error", object.getJSONObject("error").getString("reason"));
								return 0;
							} else {
								return object.getInt("event_id");
							}
						}
					} else {
						Log.e("createEvent - status", rp.getStatusLine().getStatusCode() + "");
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/events_create", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception ex) {
				Reporter.reportError(DataManagement.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
			}
			return 0;

		}
		
		private static boolean editEvent(Event e) {
			boolean success = false;

			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_edit");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(e.event_id)));

				reqEntity.addPart("event_type", new StringBody(e.type));

				reqEntity.addPart("icon", new StringBody(e.icon));
				reqEntity.addPart("color", new StringBody(e.getColor()));

				reqEntity.addPart("title", new StringBody(e.title));

				reqEntity.addPart("timestamp_start_utc",
						new StringBody("" + Utils.millisToUnixTimestamp(e.getStartCalendar().getTimeInMillis())));
				reqEntity.addPart("timestamp_end_utc", new StringBody("" + Utils.millisToUnixTimestamp(e.getEndCalendar().getTimeInMillis())));

				reqEntity.addPart("timezone", new StringBody(e.timezone));

				reqEntity.addPart("description", new StringBody(e.description_));

				reqEntity.addPart("country", new StringBody(e.country));
				reqEntity.addPart("zip", new StringBody(e.zip));
				reqEntity.addPart("city", new StringBody(e.city));
				reqEntity.addPart("street", new StringBody(e.street));
				reqEntity.addPart("location", new StringBody(e.location));

				reqEntity.addPart("go_by", new StringBody(e.go_by));
				reqEntity.addPart("take_with_you", new StringBody(e.take_with_you));
				reqEntity.addPart("cost", new StringBody(e.cost));
				reqEntity.addPart("accomodation", new StringBody(e.accomodation));

				reqEntity.addPart("alarm1", new StringBody(e.alarm1));
				reqEntity.addPart("alarm2", new StringBody(e.alarm2));
				reqEntity.addPart("alarm3", new StringBody(e.alarm3));

				// if(){
				reqEntity.addPart("reminder1", new StringBody(e.reminder1));
				reqEntity.addPart("reminder2", new StringBody(e.reminder2));
				reqEntity.addPart("reminder3", new StringBody(e.reminder3));

				if (Data.selectedContacts != null && !Data.selectedContacts.isEmpty()) {
					e.assigned_contacts = new int[Data.selectedContacts.size()];
					int i = 0;
					for (Contact contact : Data.selectedContacts) {
						e.assigned_contacts[i] = contact.contact_id;
						i++;
					}
				}
				if (e.assigned_contacts != null) {
					for (int i = 0, l = e.assigned_contacts.length; i < l; i++) {
						reqEntity.addPart("contacts[]", new StringBody(String.valueOf(e.assigned_contacts[i])));
					}
				} else {
					reqEntity.addPart("contacts[]", new StringBody(""));
				}

				if (e.assigned_groups != null) {
					for (int i = 0, l = e.assigned_groups.length; i < l; i++) {
						reqEntity.addPart("groups[]", new StringBody(String.valueOf(e.assigned_groups[i])));
					}
				} else {
					reqEntity.addPart("groups[]", new StringBody(""));
				}

				post.setEntity(reqEntity);

				if (networkAvailable) {
					HttpResponse rp = hc.execute(post);

					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							success = object.getBoolean("success");
							if (!success) {
								Log.e("Edit event ERROR", object.getJSONObject("error").getString("reason"));
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/events_edit", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception ex) {
				Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
				success = false;
			}

			return success;
		}
		
		public static boolean removeEvent(int id) {
			boolean success = false;

			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/events_remove");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(id)));

				post.setEntity(reqEntity);

				if (networkAvailable) {
					HttpResponse rp = hc.execute(post);

					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							String successStr = object.getString("success");

							if (successStr.equals("null")) {
								success = false;
							} else {
								success = object.getBoolean("success");
							}

							// Log.e("removeEvent - success", "" + success);

							if (success == false) {
								// array of errors!!!
								JSONObject errObj = object.getJSONObject("error");
								Data.setERROR(errObj.getString("reason"));
								Log.e("removeEvent - error: ", Data.getERROR());
							} else {
//								Data.getEvents().remove(getEventFromLocalDb(id));
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/events_remove", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception ex) {
				Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						ex.getMessage());
			}
			return success;
		}

		
////////////////////////////////////////////////////////////////////DB UTILITY METHODS////////////////////////////////////////////////////////////////////
//	TODO javadoc
	protected static ContentValues createCVforEventsTable(Event event) {
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsMetaData.E_ID, event.getEvent_id());
		cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID, event.getUser_id());
		cv.put(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY, event.isUploadedToServer()? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.STATUS, event.getStatus());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID, event.getCreator_contact_id());

		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT, event.getAttendant_1_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT, event.getAttendant_2_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT, event.getAttendant_0_count());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT, event.getAttendant_4_count());

		// native events are not held in GA local db so we do not put
		// Event.isNative
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT, event.is_sports_event() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, event.is_owner() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY, event.is_all_day() ? 1 : 0);
		cv.put(EventsProvider.EMetaData.EventsMetaData.IS_BIRTHDAY, event.isBirthday() ? 1 : 0);

		cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, event.getType());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME, event.getCreator_fullname());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, event.getTitle());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.getIcon());
		cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.getColor());
		cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, event.getDescription());
		cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION, event.getLocation());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION, event.getAccomodation());
		cv.put(EventsProvider.EMetaData.EventsMetaData.COST, event.getCost());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU, event.getTake_with_you());
		cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY, event.getGo_by());

		cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY, event.getCountry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CITY, event.getCity());
		cv.put(EventsProvider.EMetaData.EventsMetaData.STREET, event.getStreet());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP, event.getZip());

		// EVENT TIMES UTC
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE, event.getTimezone());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS, event.getStartCalendar().getTimeInMillis());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS, event.getEndCalendar().getTimeInMillis());

		// reminders
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.getReminder1());
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.getReminder2());
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.getReminder3());

		// TODO alarms DO SOMETHING WITH ALARM FIRED FIELDS
		cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM1, event.getAlarm1());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM2, event.getAlarm2());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ALARM3, event.getAlarm3());

		cv.put(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS, event.getCreatedUtc());
		cv.put(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS, event.getModifiedMillisUtc());

		cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS, event.getAssigned_contacts_DB_entry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS, event.getAssigned_groups_DB_entry());
		cv.put(EventsProvider.EMetaData.EventsMetaData.INVITED, event.getInvited_DB_entry());
		return cv;
	}

//	TODO javadoc
	protected static Event createEventFromCursor(Cursor result) {
		Event item = new Event();
		long timeinMillis;
		
		item.setEvent_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID)));
		item.setUser_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.USER_ID)));
		item.setUploadedToServer(1 == result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY)));
		item.setStatus(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS)));
		item.setCreator_contact_id(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_CONTACT_ID)));

		item.setAttendant_0_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_1_COUNT)));
		item.setAttendant_2_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_2_COUNT)));
		item.setAttendant_0_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_0_COUNT)));
		item.setAttendant_4_count(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ATTENDANT_4_COUNT)));

		item.setSports_event(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_SPORTS_EVENT)) == 1);
		final int is_owner = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_OWNER));
		item.setIs_owner(is_owner == 1);

		item.setIs_all_day(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_ALL_DAY)) == 1);
		item.setBirthday(result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.IS_BIRTHDAY)) == 1);
		item.setNative(false); // native events are not stored in local DB, so
								// they cant be restored also

		item.setType(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TYPE)));
		item.setCreator_fullname(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATOR_FULLNAME)));
		item.setTitle(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TITLE)));
		item.setIcon(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ICON)));
		item.setColor(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COLOR)));
		item.setDescription(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.DESC)));

		item.setLocation(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.LOCATION)));
		item.setAccomodation(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION)));
		item.setCost(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COST)));
		item.setTake_with_you(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU)));
		item.setGo_by(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.GO_BY)));

		item.setCountry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.COUNTRY)));
		item.setCity(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CITY)));
		item.setStreet(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STREET)));
		item.setZip(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ZIP)));

		item.setTimezone(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIMEZONE)));

		
		timeinMillis = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
		item.setStartCalendar(Utils.createCalendar(timeinMillis, user_timezone));
		timeinMillis = result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
		item.setEndCalendar(Utils.createCalendar(timeinMillis, user_timezone));

		item.setReminder1(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER1)));
		item.setReminder2(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER2)));
		item.setReminder3(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.REMINDER3)));

		item.setCreatedMillisUtc(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.CREATED_UTC_MILLISECONDS)));
		item.setModifiedMillisUtc(result.getLong(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.MODIFIED_UTC_MILLISECONDS)));

		item.setAssigned_contacts_DB_entry(result.getString(result
				.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_CONTACTS)));
		item.setAssigned_groups_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.ASSIGNED_GROUPS)));
		item.setInvited_DB_entry(result.getString(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.INVITED)));

		return item;
	}	

	/**
	 * Creates Event object from JSON object
	 * 
	 * @param e
	 *            JSON object
	 * @return new event object with all info set. WARNING: if not all required
	 *         fields are set, returns null <br>
	 *         Needed fields: event_id<br>
	 *         user_id<br>
	 *         status<br>
	 *         is_owner<br>
	 *         title<br>
	 *         timezone<br>
	 *         time_start_utc<br>
	 *         time_end_utc<br>
	 */
	protected static Event createEventFromJSON(JSONObject e) {
		Event event = new Event();
		String timezone = CalendarSettings.getTimeZone();
		long unixTimestamp;
		
		day_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.DAY_COLUMN_FORMAT);
		month_index_formatter = new SimpleDateFormat(EventsProvider.EMetaData.EventsIndexesMetaData.MONTH_COLUMN_FORMAT);
		// critical event info. If fetch fails, return null
		try {
			event.setEvent_id(e.getInt("event_id"));
			event.setTimezone(e.getString("timezone"));
			// EVENT TIME START
			unixTimestamp = e.getLong("timestamp_start_utc");
			event.setStartCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), timezone));
			// EVENT TIME END
			unixTimestamp = e.getLong("timestamp_end_utc");
			event.setEndCalendar(Utils.createCalendar(Utils.unixTimestampToMilis(unixTimestamp), timezone));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage()); 
			System.out.println("JSON exceptionas");
		}

		try {
			event.setUser_id(e.getInt("user_id"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setStatus(e.getInt("status"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setIs_owner(e.getInt("is_owner") == 1);
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setType(e.getString("type"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setTitle(e.getString("title"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setIcon(e.getString("icon"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setColor(e.getString("color"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setDescription(e.getString("description"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setLocation(e.getString("location"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAccomodation(e.getString("accomodation"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCost(e.getString("cost"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setTake_with_you(e.getString("take_with_you"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setGo_by(e.getString("go_by"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCountry(e.getString("country"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCity(e.getString("city"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setStreet(e.getString("street"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setZip(e.getString("zip"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		// reminders
		try {
			event.setReminder1(e.getString("reminder1"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setReminder2(e.getString("reminder2"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setReminder3(e.getString("reminder3"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		// alarms
		try {
			event.setAlarm1(e.getString("alarm1"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAlarm1fired(e.getString("alarm1_fired"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAlarm2(e.getString("alarm2"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAlarm2fired(e.getString("alarm2_fired"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAlarm3(e.getString("alarm3"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAlarm3fired(e.getString("alarm3_fired"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			unixTimestamp = e.getLong("timestamp_created");
			event.setCreatedMillisUtc(unixTimestamp);
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setModifiedMillisUtc(e.getLong("timestamp_modified"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setAttendant_0_count(e.getInt("attendant_0_count"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAttendant_1_count(e.getInt("attendant_1_count"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setAttendant_2_count(e.getInt("attendant_2_count"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setAttendant_4_count(e.getInt("attendant_4_count"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setSports_event(e.getInt("is_sports_event") == 1);
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setIs_all_day(e.getInt("all_day") == 1);
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setCreator_fullname(e.getString("creator_fullname"));
		} catch (JSONException e1) {
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setCreator_contact_id(e.getInt("creator_contact_id"));
		} catch (JSONException e1) {
			event.setCreator_contact_id(0);
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		try {
			event.setAssigned_contacts_DB_entry(e.getString("assigned_contacts"));
		} catch (JSONException e1) {
			event.setAssigned_contacts_DB_entry("");
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setAssigned_groups_DB_entry(e.getString("assigned_groups"));
		} catch (JSONException e1) {
			event.setAssigned_contacts_DB_entry("");
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}
		try {
			event.setInvited_DB_entry(e.getString("invited"));
		} catch (JSONException e1) {
			event.setInvited_DB_entry("");
			Reporter.reportError(CLASS_NAME, Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
					e1.getMessage());
		}

		return event;
	}
	
}