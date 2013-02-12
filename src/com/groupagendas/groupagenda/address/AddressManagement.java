package com.groupagendas.groupagenda.address;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.groupagendas.groupagenda.SaveDeletedData;
import com.groupagendas.groupagenda.SaveDeletedData.SDMetaData;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.CharsetUtils;
import com.groupagendas.groupagenda.utils.JSONUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;

public class AddressManagement {

	public static final String CLASS_NAME = "AddressManagement.class";
	public static final String DATA = "data";

	public static final String ADDRESS_ID = "id";
	public static final String USER_ID = "user_id";
	public static final String TITLE = "title";
	public static final String STREET = "street";
	public static final String CITY = "city";
	public static final String ZIP = "zip";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String TIMEZONE = "timezone";
	public static final String COUNTRY_NAME = "country_name";

	public static final int ID_INTERNAL = 0;
	public static final int ID_EXTERNAL = 1;

	public static final int CREATE = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;

	public static void getAddressBookFromRemoteDb(Context context) {
		boolean success = false;
		String error = null;
		Address address = null;
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_get");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		CharsetUtils.addAllParts(reqEntity, "session", account.getSessionId(), "token", Data.getToken(context));

		post.setEntity(reqEntity);
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("error");
						Log.e("getAddressBookList - error: ", error);
					} else {
						JSONArray gs = object.getJSONArray(DATA);
						int count = gs.length();
						for (int i = 0; i < count; i++) {
							JSONObject g = gs.getJSONObject(i);
							address = JSONUtils.createAddressFromJSON(context, g);
							if (address != null) {
								address.setUploadedToServer(true);
								context.getContentResolver().insert(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI,
										createCVforAddressTable(address));
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			Log.e("getAddressBookFromRemoteDb", "er");
		}
	}

	protected static ContentValues createCVforAddressTable(Address address) {
		ContentValues cv = new ContentValues();
		if (address.getId() != 0) {
			cv.put(AddressProvider.AMetaData.AddressesMetaData.A_ID, address.getId());
		} else {
			address.setId((((int) Calendar.getInstance().getTimeInMillis()) * -1));
			cv.put(AddressProvider.AMetaData.AddressesMetaData.A_ID, address.getId());
		}
		cv.put(AddressProvider.AMetaData.AddressesMetaData.USER_ID, address.getUser_id());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.TITLE, address.getTitle());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.STREET, address.getStreet());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.CITY, address.getCity());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.ZIP, address.getZip());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.STATE, address.getState());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.COUNTRY, address.getCountry());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.TIMEZONE, address.getTimezone());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.COUNTRY_NAME, address.getCountry_name());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.UPLOADED_SUCCESSFULLY, address.isUploadedToServer() ? 1 : 0);
		return cv;
	}

	public static ArrayList<Address> getAddressesFromLocalDb(Context context, String where) {
		Cursor cur;
		Address temp;

		cur = context.getContentResolver().query(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, null, where, null, null);
		if (cur.getCount() < 1) {
			Log.i("getAddressFromLocalDb()", "Empty or no response from local db.");
		}
		ArrayList<Address> addresses = new ArrayList<Address>(cur.getCount());

		while (cur.moveToNext()) {
			temp = createAddressFromCursor(cur);
			addresses.add(temp);
		}

		cur.close();

		return addresses;
	}

	private static Address createAddressFromCursor(Cursor cursor) {
		Address address = new Address();

		address.setIdInternal(cursor.getInt(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData._ID)));
		address.setId(cursor.getInt(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.A_ID)));
		address.setUser_id(cursor.getInt(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.USER_ID)));
		address.setTitle(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.TITLE)));
		address.setStreet(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.STREET)));
		address.setCity(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.CITY)));
		address.setZip(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.ZIP)));
		address.setState(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.STATE)));
		address.setCountry(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.COUNTRY)));
		address.setTimezone(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.TIMEZONE)));
		address.setCountry_name(cursor.getString(cursor.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.COUNTRY_NAME)));
		address.setUploadedToServer(1 == cursor.getInt(cursor.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.UPLOADED_SUCCESSFULLY)));

		return address;
	}

	public static Address getAddressFromLocalDb(Context context, String where) {
		Address item = new Address();
		Cursor cur = context.getContentResolver().query(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, null, where, null, null);
		if (cur.moveToFirst()) {
			item = createAddressFromCursor(cur);
		}
		cur.close();
		return item;
	}

	/**
	 * Create, edit, delete address in remote DB.
	 * 
	 * @param context
	 * @param address
	 *            - address to create, edit, delete
	 * @param param
	 *            - param to create, edit, delete address (params:
	 *            AddressManagement.CREATE, AddressManagement.UPDATE,
	 *            AddressManagement.DELETE )
	 * @return address id when CREATING. Success when EDITING, DELETING.
	 */
	public static String setAddressBookEntries(Context context, Address address, int param) {
		boolean success = false;
		String error = null;
		String result = "";
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_set");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		CharsetUtils.addAllParts(reqEntity, "session", account.getSessionId(), "token", Data.getToken(context));

		switch (param) {
		case UPDATE:
			CharsetUtils.addAllParts(reqEntity, "id", address.getId(), "title", address.getTitle());
			break;
		case CREATE:
			CharsetUtils.addPart(reqEntity, "title", address.getTitle());
			break;
		case DELETE:
			CharsetUtils.addPart(reqEntity, "id", address.getId());
			break;
		}

		if (param != DELETE) {
			CharsetUtils.addAllParts(reqEntity, "street", address.getStreet(), "city", address.getCity(),
					"zip", address.getZip(), "state", address.getState(), "country", address.getCountry(),
					"timezone", address.getTimezone());
		}

		post.setEntity(reqEntity);
		try {
			HttpResponse rp = webService.getResponseFromHttpPost(post);

			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String resp = EntityUtils.toString(rp.getEntity());
				if (resp != null) {
					JSONObject object = new JSONObject(resp);
					success = object.getBoolean("success");

					if (success == false) {
						error = object.getString("reason");
						Log.e("setAddressBookEntries - error: ", error);
						result = StringValueUtils.valueOf(success);
					} else {
						if (param == CREATE) {
							result = object.getString("id");
						} else {
							result = StringValueUtils.valueOf(success);
						}
					}
				}

			}
		} catch (Exception ex) {
			Log.e("setAddressBookEntries", "er");
		}

		return result;
	}

	public static void deleteAddressFromLocalDb(Context context, int address_id) {
		String where = AddressProvider.AMetaData.AddressesMetaData.A_ID + "=" + address_id;
		context.getContentResolver().delete(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, where, null);
	}
	
	public static void deleteAllAddressFromLocalDb(Context context) {
		context.getContentResolver().delete(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, null, null);
	}

	public static void updateAddressInLocalDb(Context context, Address address) {
		String where = AddressProvider.AMetaData.AddressesMetaData._ID + "=" + address.getIdInternal();
		context.getContentResolver().update(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, createCVforAddressTable(address),
				where, null);
	}

	public static void insertAddressInLocalDb(Context context, Address address) {
		context.getContentResolver().insert(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, createCVforAddressTable(address));
	}

	public static void uploadOfflineAddresses(Context context) {
		String projection[] = null;
		Uri uri = AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI;
		String where = AddressProvider.AMetaData.AddressesMetaData.UPLOADED_SUCCESSFULLY + " = '0'";
		Cursor result = context.getContentResolver().query(uri, projection, where, null, null);
		while (result.moveToNext()) {
			Address address = createAddressFromCursor(result);

			String res = AddressManagement.setAddressBookEntries(context, address, AddressManagement.UPDATE);
			if (res.contentEquals("true")) {
				address.setUploadedToServer(true);
				AddressManagement.updateAddressInLocalDb(context, address);
			} else {
				String res2 = AddressManagement.setAddressBookEntries(context, address, AddressManagement.CREATE);
				int address_id = 0;
				try {
					address_id = Integer.parseInt(res2);
				} catch (Exception e) {

				}
				if (address_id > 0) {
					address.setId(address_id);
					address.setUploadedToServer(true);
					AddressManagement.updateAddressInLocalDb(context, address);
				}
			}
		}
		result.close();
		
		SaveDeletedData offlineDeletedAddresses = new SaveDeletedData(context);
		String offlineDeleted = offlineDeletedAddresses.getDELETED_ADDRESSES();
		String[] ids = offlineDeleted.split(SDMetaData.SEPARATOR);
		if (!ids[0].equals("")) {
			for (int i = 0; i < ids.length; i++) {
				int id = Integer.parseInt(ids[i]);
				Address address = new Address();
				address.setId(id);
				AddressManagement.setAddressBookEntries(context, address, AddressManagement.DELETE);
			}

		}
		offlineDeletedAddresses.clear(4);
	}

}
