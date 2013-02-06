package com.groupagendas.groupagenda.address;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.JSONUtils;

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

	
	public static void getAddressBookFromRemoteDb(Context context) {
		boolean success = false;
		String error = null;
		Address address = null;
		Account account = new Account(context);
		WebService webService = new WebService(context);
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/adressbook_get");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("getAddressBookFromRemoteDb(contactIds)", "Failed adding token to entity");
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
						error = object.getString("error");
						Log.e("getAddressBookList - error: ", error);
					} else {
						JSONArray gs = object.getJSONArray(DATA);
						int count = gs.length();
						if (count > 0) {
							for (int i = 0; i < count; i++) {
								JSONObject g = gs.getJSONObject(i);
								address = JSONUtils.createAddressFromJSON(context, g);
								if(address != null){
									context.getContentResolver().insert(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, createCVforAddressTable(address));
								}
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
		cv.put(AddressProvider.AMetaData.AddressesMetaData.A_ID, address.getId());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.USER_ID, address.getUser_id());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.TITLE, address.getTitle());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.STREET, address.getStreet());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.CITY, address.getCity());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.ZIP, address.getZip());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.STATE, address.getState());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.COUNTRY, address.getCountry());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.TIMEZONE, address.getTimezone());
		cv.put(AddressProvider.AMetaData.AddressesMetaData.COUNTRY_NAME, address.getCountry_name());
		return cv;
	}
	
	public static ArrayList<Address> getAddressFromLocalDb(Context context, String where) {
		Cursor cur;
		Address temp;

		cur = context.getContentResolver().query(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, null, where, null, null);
		if (cur.getCount() < 1) {
			Log.i("getAddressFromLocalDb()", "Empty or no response from local db.");
		}
		ArrayList<Address> addresses = new ArrayList<Address>(cur.getCount());

		while (cur.moveToNext()) {
			temp = new Address();

			temp.setId(cur.getInt(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.A_ID)));
			temp.setUser_id(cur.getInt(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.USER_ID)));
			temp.setTitle(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.TITLE)));
			temp.setStreet(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.STREET)));
			temp.setCity(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.CITY)));
			temp.setZip(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.ZIP)));
			temp.setState(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.STATE)));
			temp.setCountry(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.COUNTRY)));
			temp.setTimezone(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.TIMEZONE)));
			temp.setCountry_name(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.COUNTRY_NAME)));

			addresses.add(temp);
		}

		cur.close();

		return addresses;
	}
	
	public static final int ID_INTERNAL = 0;
	public static final int ID_EXTERNAL = 1;

	public static Address getAddressFromLocalDb(Context context, long ID, int id_mode) {
		Address item = new Address();
		Uri uri;

		switch (id_mode) {
		case (ID_INTERNAL):
			uri = AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI;
			break;
		case (ID_EXTERNAL):
			uri = AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI_EXTERNAL_ID;
			break;
		default:
			throw new IllegalStateException("method getAddressFromLocalDB: Unknown id mode");
		}

		uri = Uri.parse(uri + "/" + ID);
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		if (cur.moveToFirst()) {
			item.setId(cur.getInt(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.A_ID)));
			item.setUser_id(cur.getInt(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.USER_ID)));
			item.setTitle(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.TITLE)));
			item.setStreet(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.STREET)));
			item.setCity(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.CITY)));
			item.setZip(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.ZIP)));
			item.setState(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.STATE)));
			item.setCountry(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.COUNTRY)));
			item.setTimezone(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.TIMEZONE)));
			item.setCountry_name(cur.getString(cur.getColumnIndex(AddressProvider.AMetaData.AddressesMetaData.COUNTRY_NAME)));
		}
		cur.close();
		return item;
	}
	
	
	public static final int CREATE = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
	/**
	 * Create, edit, delete address in remote DB.
	 * 
	 * @param context
	 * @param address - address to create, edit, delete
	 * @param param - param to create, edit, delete address (params: AddressManagement.CREATE, AddressManagement.UPDATE, AddressManagement.DELETE )
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

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding token to entity");
		}
		
		switch (param){
		case UPDATE:
			try {
				reqEntity.addPart("id", new StringBody(""+address.getId(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				Log.e("setAddressBookEntries", "Failed adding address id to entity");
			}
			
			try {
				reqEntity.addPart("title", new StringBody(address.getTitle(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				Log.e("setAddressBookEntries", "Failed adding address title to entity");
			}
			break;
		case CREATE:			
			try {
				reqEntity.addPart("title", new StringBody(address.getTitle(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				Log.e("setAddressBookEntries", "Failed adding address title to entity");
			}
			break;
		case DELETE:
			try {
				reqEntity.addPart("id", new StringBody(""+address.getId(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				Log.e("setAddressBookEntries", "Failed adding address id to entity");
			}
			break;
		}
		
		
		try {
			reqEntity.addPart("street", new StringBody(address.getStreet(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding address street to entity");
		}
		
		try {
			reqEntity.addPart("city", new StringBody(address.getCity(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding address city to entity");
		}
		
		try {
			reqEntity.addPart("zip", new StringBody(address.getZip(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding address zip to entity");
		}
		
		try {
			reqEntity.addPart("state", new StringBody(address.getState(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding address state to entity");
		}
		
		try {
			reqEntity.addPart("country", new StringBody(address.getCountry(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding address country to entity");
		}
		
		try {
			reqEntity.addPart("timezone", new StringBody(address.getTimezone(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("setAddressBookEntries", "Failed adding address timezone to entity");
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
						error = object.getString("error");
						Log.e("setAddressBookEntries - error: ", error);
					} else {
						if(param == CREATE){
							result = object.getString("id");
						} else {
							result = ""+success;
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
	
	public static void updateAddressInLocalDb(Context context, Address address) {
		String where = AddressProvider.AMetaData.AddressesMetaData.A_ID + "=" + address.getId();
		context.getContentResolver().update(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, createCVforAddressTable(address), where, null);
	}
	
	public static void insertAddressInLocalDb(Context context, Address address) {
		context.getContentResolver().insert(AddressProvider.AMetaData.AddressesMetaData.CONTENT_URI, createCVforAddressTable(address));
	}

}
