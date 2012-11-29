package com.groupagendas.groupagenda.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.SaveDeletedData;
import com.groupagendas.groupagenda.SaveDeletedData.SDMetaData;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactEditActivity;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.contacts.birthdays.Birthday;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactManagement {

	/**
	 * Get all contact entries from remote database.
	 * 
	 * Executes a call to remote database and retrieves all contact entries from
	 * it. While retrieving, each contact entry is stored in SQLite database.
	 * 
	 * @author meska.lt@gmail.com
	 * @return ArrayList of Contact objects got from response.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	// TODO MESKAI: naudoti metoda JSONUtils'uose kurti kontakta is JSON'o
	public static void getContactsFromRemoteDb(Context context, HashSet<Integer> groupIds) {
		boolean success = false;
		String error = null;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_list");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		if (groupIds != null) {
			Iterator<Integer> it = groupIds.iterator();
			while (it.hasNext()) {
				try {
					reqEntity.addPart("group_id[]", new StringBody(String.valueOf(it.next()), Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
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
						Log.e("getContactList - error: ", error);
					} else {
						JSONArray cs = object.getJSONArray("contacts");
						int count = cs.length();
						if (count > 0) {
							for (int i = 0; i < count; i++) {
								JSONObject c = cs.getJSONObject(i);
								Contact contact = new Contact();
								String temp;

								try {
									contact.contact_id = c.getInt(ContactsProvider.CMetaData.ContactsMetaData.C_ID);
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting id.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.NAME);
									if (temp != null && !temp.equals("null"))
										contact.name = temp;
									else
										contact.name = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting name.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME);
									if (temp != null && !temp.equals("null"))
										contact.lastname = temp;
									else
										contact.lastname = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting lastname.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.EMAIL);
									if (temp != null && !temp.equals("null"))
										contact.email = temp;
									else
										contact.email = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting email.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.PHONE);
									if (temp != null && !temp.equals("null"))
										contact.phone1 = temp;
									else
										contact.phone1 = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting phone number.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE);
									if (temp != null && !temp.equals("null"))
										contact.phone1_code = temp;
									else
										contact.phone1_code = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting phone code.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE);
									if (temp != null && !temp.equals("null"))
										contact.birthdate = temp;
									else
										contact.birthdate = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting birthdate.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY);
									if (temp != null && !temp.equals("null"))
										contact.country = temp;
									else
										contact.country = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting country.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.CITY);
									if (temp != null && !temp.equals("null"))
										contact.city = temp;
									else
										contact.city = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting city.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.STREET);
									if (temp != null && !temp.equals("null"))
										contact.street = temp;
									else
										contact.street = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting street.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.ZIP);
									if (temp != null && !temp.equals("null"))
										contact.zip = temp;
									else
										contact.zip = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting zip.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY);
									if (temp != null && !temp.equals("null"))
										contact.visibility = temp;
									else
										contact.visibility = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting visibility.");
								}

								try {
									contact.image = c.getBoolean(ContactsProvider.CMetaData.ContactsMetaData.IMAGE);
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL);
									if (temp != null && !temp.equals("null"))
										contact.image_thumb_url = temp;
									else
										contact.image_thumb_url = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_thumb_url.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL);
									if (temp != null && !temp.equals("null")) {
										contact.image_url = temp;
										try {
											contact.image_bytes = Utils.imageToBytes(contact.image_url);
										} catch (Exception e) {
											Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_bytes.");
										}
									} else
										contact.image_url = "";
								} catch (JSONException e) {
									contact.image = false;
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_url and/or image_bytes.");
								}

								try {
									contact.created = c.getLong(ContactsProvider.CMetaData.ContactsMetaData.CREATED);
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting created.");
								}

								try {
									contact.modified = c.getLong(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED);
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting modified.");
								}

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW);
									if (temp != null && !temp.equals("null"))
										contact.agenda_view = temp;
									else
										contact.agenda_view = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting agenda_view.");
								}

								// TODO investigate REGISTERED meaning.
								// try {
								// temp =
								// c.getString(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED);
								// if (temp != null && !temp.equals("null"))
								// contact.registered = temp;
								// else
								// contact.registered = "";
								// } catch (JSONException e) {
								// Log.e("getContactsFromRemoteDb(contactIds)",
								// "Failed getting registered.");
								// }

								try {
									temp = c.getString(ContactsProvider.CMetaData.ContactsMetaData.COLOR);
									if (temp != null && !temp.equals("null"))
										contact.setColor(temp);
									else
										contact.setColor("000000");
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting color.");
								}

								try {
									if (!c.getString("groups").equals("null") && c.getString("groups") != null) {
										try {
											JSONArray groups = c.getJSONArray("groups");
											if (groups != null) {
												Map<String, String> set = new HashMap<String, String>();
												for (int j = 0, l = groups.length(); j < l; j++) {
													set.put(String.valueOf(j), groups.getString(j));
												}
												contact.groups = set;
											}
										} catch (JSONException e) {
											Log.e("getContactsFromRemoteDb(contactIds)", "Groups were null.");
										}
									}
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting groups.");
								}

								insertContactToLocalDb(context, contact, 0);
								if (contact.birthdate != null && contact.birthdate.length() == 10) {
									Birthday birthday = new Birthday(context, contact);
									insertBirthdayToLocalDb(context, birthday, contact.contact_id);
								}
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			Log.e("getContactsFromRemoteDb(contactIds)", ex.getMessage());
		}

		Data.setLoadContactsData(false);
	}

	/**
	 * Get all contact entries from local database.
	 * 
	 * Executes a call to SQLite database and retrieves all contact entries from
	 * it.
	 * 
	 * @author meska.lt@gmail.com
	 * @param where
	 *            - DOCUMENTATION PENDING
	 * @return ArrayList of Contact objects got from response.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static ArrayList<Contact> getContactsFromLocalDb(Context context, String where) {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Cursor cur;
		Contact temp;

		cur = context.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, null, where, null, null);

		if (cur.moveToFirst()) {
			while (!cur.isAfterLast()) {
				temp = new Contact();

				temp.contact_id = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
				temp.name = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.NAME));
				temp.lastname = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME));

				temp.email = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
				temp.phone1 = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.PHONE));
				temp.phone1_code = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE));

				temp.birthdate = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE));

				temp.country = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY));
				temp.city = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CITY));
				temp.street = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.STREET));
				temp.zip = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.ZIP));

				temp.visibility = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY));

				String resp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE));
				if (resp != null) {
					if (resp.equals("1"))
						temp.image = true;
					else
						temp.image = false;
				} else {
					temp.image = false;
				}

				temp.image_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL));
				temp.image_thumb_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL));
				temp.image_bytes = cur.getBlob(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES));
				if (cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE)).equals("1")) {
					temp.remove_image = true;
				} else {
					temp.remove_image = false;
				}

				temp.agenda_view = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW));
				temp.registered = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED));

				temp.created = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.CREATED));
				temp.modified = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED));

				resp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.GROUPS));
				if (resp != null) {
					temp.groups = MapUtils.stringToMap(context, resp);
				}

				temp.setColor(cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.COLOR)));

				contacts.add(temp);

				cur.moveToNext();
			}
		} else {
			Log.i("getContactsFromLocalDb()", "Empty or no response from local db.");
		}

		cur.close();

		return contacts;
	}

	public static int insertContactToRemoteDb(Context context, Contact contact, int id) {
		int destination_id = 0;
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_create");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertContactToRemoteDb(group[id=" + contact.contact_id + "], " + id + ")", "Failed adding token to entity.");
		}

		if (contact.name != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.NAME, new StringBody(contact.name, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group[id=" + contact.contact_id + "], " + id + ")", "Failed adding name to entity.");
			}
		} else {
			Log.e("insertContactToRemoteDb(group[id=" + contact.contact_id + "], " + id + ")", "Failed getting name from Contact object.");
		}

		if (contact.lastname != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME,
						new StringBody(contact.lastname, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding lastname to entity.");
			}
		}

		if (contact.email != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.EMAIL,
						new StringBody(contact.email, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding email to entity.");
			}
		}

		if (contact.phone1 != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.PHONE,
						new StringBody(contact.phone1, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding phone1 to entity.");
			}
		}

		if (contact.phone1_code != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE,
						new StringBody(contact.phone1_code, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding phone1_code to entity.");
			}
		}

		if (contact.birthdate != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE,
						new StringBody(contact.birthdate, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding birthdate to entity.");
			}
		}

		if (contact.country != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY,
						new StringBody(contact.country, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding country to entity.");
			}
		}

		if (contact.city != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.CITY, new StringBody(contact.city, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding city to entity.");
			}
		}

		if (contact.street != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.STREET,
						new StringBody(contact.street, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding street to entity.");
				e.printStackTrace();
			}
		}

		if (contact.zip != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.ZIP, new StringBody(contact.zip, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding zip to entity.");
			}
		}

		if (contact.visibility != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY,
						new StringBody(contact.visibility, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding visibility to entity.");
			}
		}

		if (contact.image) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, new StringBody("1", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding image to entity.");
			}
		}

		if (contact.image_url != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL,
						new StringBody(contact.image_url, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding image_url to entity.");
			}
		}

		if (contact.image_thumb_url != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL, new StringBody(contact.image_thumb_url,
						Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding image_thumb_url to entity.");
			}
		}

		if (contact.image_bytes != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES,
						new StringBody(contact.image_bytes + "", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding image_bytes to entity.");
			}
		}

		if (contact.remove_image) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, new StringBody("1", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding remove_image to entity.");
			}
		}

		if (contact.agenda_view != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW,
						new StringBody(contact.agenda_view, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding agenda_view to entity.");
			}
		}

		if (contact.registered != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED,
						new StringBody(contact.registered, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding registered to entity.");
			}
		}

		if (contact.created > 0) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.CREATED,
						new StringBody(contact.created + "", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding created to entity.");
			}
		}

		if (contact.modified > 0) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED,
						new StringBody(contact.modified + "", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding modified to entity.");
			}
		}
		
		if (contact.visibility != null){
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, new StringBody(contact.visibility + "", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding modified to entity.");
			}
		} else {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, new StringBody(context.getResources().getStringArray(R.array.visibility_values)[0], Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding modified to entity.");
			}
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.COLOR,
					new StringBody(contact.getColor(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding color to entity.");
		}

		Map<String, String> groups = contact.groups;
		if (groups != null) {
			for (int i = 0, l = groups.size(); i < l; i++) {
				try {
					if (groups.get("" + i) != null) {
						reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.GROUPS + "[]", new StringBody(groups.get("" + i),
								Charset.forName("UTF-8")));
					}
				} catch (UnsupportedEncodingException e) {
					Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed adding group to entity.");
				}
			}
		}

		post.setEntity(reqEntity);
		try {
			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (success) {
							destination_id = object.getInt("contact_id");
							DataManagement.synchronizeWithServer(context, null, account.getLatestUpdateUnixTimestamp());
							Log.i("createContact - success", "" + success);
						}

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("createContact - error: ", Data.getERROR());
							return -1;
						}
					}
				}
			}
		} catch (Exception ex) {
			Log.e("insertContactToRemoteDb(group, " + id + ")", "Failed executing POST request.");
			return 0;
		}
		return destination_id;
	}

	/**
	 * Insert a contact into local database.
	 * 
	 * Executes a call to SQLite database that creates a contact entry from data
	 * submitted. Currently image_bytes[] isn't stored in the database.
	 * 
	 * @author meska.lt@gmail.com
	 * @param contact
	 *            - Contact object containing validated contact data.
	 * @param id
	 *            - ID corresponding integer.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static void insertContactToLocalDb(Context context, Contact contact, int id) {
		ContentValues cv = new ContentValues();

		if (id > 0)
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.C_ID, id);
		else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.C_ID, contact.contact_id);
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, contact.name);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, contact.lastname);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, contact.email);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, contact.phone1);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE, contact.phone1_code);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, contact.birthdate);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, contact.country);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.CITY, contact.city);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.STREET, contact.street);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.ZIP, contact.zip);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, contact.visibility);

		if (contact.image) {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL, contact.image_url);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL, contact.image_thumb_url);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, contact.image_bytes);
		if (contact.remove_image) {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW, contact.agenda_view);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED, contact.registered);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.CREATED, contact.created);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED, contact.modified);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(context, contact.groups));

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.COLOR, contact.getColor());

		try {
			context.getContentResolver().insert(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, cv);
		} catch (SQLiteException e) {
			Log.e("insertContactToLocalDb(contact, " + id + ")", e.getMessage());
		}
	}

	public static boolean insertContact(Context context, Contact contact) {
		boolean success = false;
		int destination_id = 0;

		if (DataManagement.networkAvailable) {
			destination_id = insertContactToRemoteDb(context, contact, 0);
		}

		if (destination_id > 0) {
			success = true;
			insertContactToLocalDb(context, contact, destination_id);
			if (contact.birthdate != null && contact.birthdate.length() == 10) {
				Birthday birthday = new Birthday(context, contact);
				insertBirthdayToLocalDb(context, birthday, destination_id);
			}
			if (ContactEditActivity.selectedGroups != null) {
				for (Group g : ContactEditActivity.selectedGroups) {
					ContactManagement.updateGroupOnLocalDb(context, g, destination_id, true);
					ContactManagement.editGroupOnRemoteDb(context, g, destination_id, true);
				}
				ContactEditActivity.selectedGroups = null;
			}
		}

		if (destination_id == 0) {
			success = true;
			insertContactToLocalDb(context, contact, 0);
			if (contact.birthdate != null && contact.birthdate.length() == 10) {
				Birthday birthday = new Birthday(context, contact);
				insertBirthdayToLocalDb(context, birthday, contact.contact_id);
			}
			if (ContactEditActivity.selectedGroups != null) {
				for (Group g : ContactEditActivity.selectedGroups) {
					ContactManagement.updateGroupOnLocalDb(context, g, contact.contact_id, true);
					ContactManagement.editGroupOnRemoteDb(context, g, contact.contact_id, true);
				}
				ContactEditActivity.selectedGroups = null;
			}
		}

		return success;
	}

	public static void insertBirthdayToLocalDb(Context context, Birthday birthday, int id) {
		ContentValues cv = new ContentValues();

		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.B_ID, birthday.getBirthdayId());

		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.TITLE, birthday.getName() + " " + birthday.getLastName());
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE, birthday.getBirthday());

		String[] date = birthday.getBirthday().split("-");
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM_DD, date[1] + "-" + date[2]);
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM, date[1]);

		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID, id);
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.COUNTRY, birthday.getCountry());

		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.TIMEZONE, birthday.getTimezone());

		try {
			context.getContentResolver().insert(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI, cv);
		} catch (SQLiteException e) {
			Log.e("insertBirthdayToLocalDb(birthday, " + birthday.getBirthdayId() + ")", e.getMessage());
		}
	}

	/**
	 * Update contact entry's ID in local database.
	 * 
	 * Executes a call to SQLite database and updating contact entry's (that
	 * contains corresponding creation time submitted) ID.
	 * 
	 * @author meska.lt@gmail.com
	 * @param created
	 *            - Long UNIX timestamp, destination's creation date.
	 * @param id
	 *            - Contact's ID.
	 * @return Update's state. True if update succeeded.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static boolean updateContactIdInLocalDb(Context context, long internal_id, int id) {
		ContentValues cv = new ContentValues();
		boolean success = false;
		int queryResult = 0;
		String where = ContactsProvider.CMetaData.ContactsMetaData._ID + "=" + internal_id;

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.C_ID, id);

		try {
			queryResult = context.getContentResolver().update(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, cv, where, null);
		} catch (SQLiteException e) {
			Log.e("updateContactIdInLocalDb(" + internal_id + ", " + id + ")", e.getMessage());
		}

		if (queryResult == 1)
			success = true;

		return success;
	}

	/**
	 * Get contact's object from local database.
	 * 
	 * Executes a call to SQLite database and retrieving contact entry's data.
	 * 
	 * @author meska.lt@gmail.com
	 * @param id
	 *            - Contact's ID.
	 * @return Contact object.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static Contact getContactFromLocalDb(Context context, int id, long created) {
		Cursor cur = null;
		Contact temp = null;

		if (id > 0) {
			Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI + "/" + id);
			cur = context.getContentResolver().query(uri, null, null, null, null);
			if (cur.moveToFirst()) {
				temp = new Contact(context, cur);
				cur.close();
			}
		} else {
			if (created > 0) {
				String where = ContactsProvider.CMetaData.ContactsMetaData.CREATED + "=" + created;
				cur = context.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, null, where, null, null);
				if (cur.moveToFirst())
					temp = new Contact(context, cur);
				cur.close();
			}
		}
		if (cur != null) {
			cur.close();
		}
		return temp;
	}

	public static Birthday getBirthdayFromLocalDb(Context context, int id) {
		Cursor cur;
		Birthday temp = null;
		String where = ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID + "=" + id;
		cur = context.getContentResolver().query(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI, null, where, null, null);
		if (cur.moveToFirst())
			temp = new Birthday(context, cur);
		cur.close();

		return temp;
	}

	/**
	 * Get all group entries from local database.
	 * 
	 * Executes a call to SQLite database and retrieves all group entries from
	 * it.
	 * 
	 * @author meska.lt@gmail.com
	 * @param where
	 *            - DOCUMENTATION PENDING
	 * @return ArrayList of Contact objects got from response.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static ArrayList<Group> getGroupsFromLocalDb(Context context, String where) {
		ArrayList<Group> contacts = new ArrayList<Group>();
		Cursor cur;
		Group temp;

		cur = context.getContentResolver().query(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, null, where, null, null);

		if (cur.moveToFirst()) {
			while (!cur.isAfterLast()) {
				temp = new Group();

				temp.group_id = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
				temp.title = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.TITLE));
				temp.created = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CREATED));
				temp.modified = cur.getLong(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED));
				temp.deleted = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.DELETED));

				String resp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE));
				if (resp != null) {
					if (resp.equals("1"))
						temp.image = true;
					else
						temp.image = false;
				} else {
					temp.image = false;
				}

				temp.image_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL));
				temp.image_thumb_url = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL));
				temp.image_bytes = cur.getBlob(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES));

				String resp2 = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE));
				if (resp2 != null) {
					if (resp2.equals("1")) {
						temp.remove_image = true;
					} else {
						temp.remove_image = false;
					}
				} else {
					temp.remove_image = false;
				}

				temp.contact_count = cur.getInt(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT));

				resp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS));
				if (resp != null) {
					temp.contacts = MapUtils.stringToMap(context, resp);
				}

				contacts.add(temp);

				cur.moveToNext();
			}
		} else {
			Log.i("getGroupsFromLocalDb()", "Empty or no response from local db.");
		}

		cur.close();

		return contacts;
	}

	/**
	 * Update group entry's ID in local database.
	 * 
	 * Executes a call to SQLite database and updating group entry's (that
	 * contains corresponding creation time submitted) ID.
	 * 
	 * @author meska.lt@gmail.com
	 * @param created
	 *            - Long UNIX timestamp, destination's creation date.
	 * @param id
	 *            - Group's ID.
	 * @return Update's state. True if update succeeded.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static boolean updateGroupIdInLocalDb(Context context, int internalId, int id) {
		ContentValues cv = new ContentValues();
		boolean success = false;
		int queryResult = 0;
		String where = ContactsProvider.CMetaData.GroupsMetaData._ID + "=" + internalId;

		cv.put(ContactsProvider.CMetaData.GroupsMetaData.G_ID, id);

		try {
			queryResult = context.getContentResolver().update(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, cv, where, null);
		} catch (SQLiteException e) {
			Log.e("updateGroupIdInLocalDb(" + internalId + ", " + id + ")", e.getMessage());
		}

		if (queryResult == 1)
			success = true;

		return success;
	}

	public static boolean insertGroup(Context context, Group group) {
		boolean success = false;
		int destination_id = 0;

		destination_id = insertGroupToRemoteDb(context, group, 0);

		if (destination_id > 0) {
			success = true;
			insertGroupToLocalDb(context, group, destination_id);
			int max_key = 0;

			if (group.contacts != null) {

				for (String s : group.contacts.keySet()) {
					Contact c = ContactManagement.getContactFromLocalDb(context, Integer.parseInt(group.contacts.get(s)), 0);

					if (c.groups != null) {
						for (String key : c.groups.keySet()) {
							int temp = Integer.parseInt(key);
							if (temp > max_key) {
								max_key = temp;
							}
						}
						c.groups.put("" + (max_key + 1), "" + destination_id);
					} else {
						c.groups = new HashMap<String, String>();
						c.groups.put("" + max_key, "" + destination_id);
					}

					ContactManagement.updateContactOnLocalDb(context, c);
				}
			}
		}

		if (destination_id == 0) {
			success = true;
			insertGroupToLocalDb(context, group, 0);
			int max_key = 0;

			if (group.contacts != null) {

				for (String s : group.contacts.keySet()) {
					Contact c = ContactManagement.getContactFromLocalDb(context, Integer.parseInt(group.contacts.get(s)), 0);

					if (c.groups != null) {
						for (String key : c.groups.keySet()) {
							int temp = Integer.parseInt(key);
							if (temp > max_key) {
								max_key = temp;
							}
						}
						c.groups.put("" + (max_key + 1), "" + group.group_id);
					} else {
						c.groups = new HashMap<String, String>();
						c.groups.put("" + max_key, "" + group.group_id);
					}

					ContactManagement.updateContactOnLocalDb(context, c);
				}
			}
		}

		return success;
	}

	/**
	 * Insert a group into local database.
	 * 
	 * Executes a call to SQLite database that creates a group entry from data
	 * submitted.
	 * 
	 * @author meska.lt@gmail.com
	 * @param group
	 *            - Group object containing validated group data.
	 * @param id
	 *            - ID corresponding integer.
	 * @since 2012-09-29
	 * @version 0.1
	 */
	public static void insertGroupToLocalDb(Context context, Group group, int id) {
		ContentValues cv = new ContentValues();

		if (id > 0)
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.G_ID, id);
		else
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.G_ID, group.group_id);

		cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, group.title);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.CREATED, group.created);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED, group.modified);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.DELETED, group.deleted);

		if (group.image) {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, group.image_url);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, group.image_thumb_url);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, group.image_bytes);
		if (group.remove_image) {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, group.contact_count);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(context, group.contacts));

		try {
			context.getContentResolver().insert(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, cv);
		} catch (SQLiteException e) {
			Log.e("insertGroupToLocalDb(group, " + id + ")", e.getMessage());
		}
	}

	/**
	 * Update a group into local database.
	 * 
	 * @author audrius6@gmail.com
	 * @param group
	 *            - Group object containing validated group data.
	 * @param contactID
	 *            - contact insert to group.
	 * @param insert
	 *            - if true insert contact to group, if false remove contact
	 *            from group.
	 * @since 2012-11-21
	 * @version 0.1
	 */
	public static boolean updateGroupOnLocalDb(Context context, Group group, int contactID, boolean insert) {
		ContentValues cv = new ContentValues();

		if (contactID > 0) {
			Map<String, String> map = new HashMap<String, String>();
			int contact_count = group.contact_count;
			int target = -1;
			int max_key = 0;
			map = group.contacts;

			if (map == null) {
				map = new HashMap<String, String>();
			}

			for (String s : map.keySet()) {
				int temp = Integer.parseInt(s);
				if (temp > max_key) {
					max_key = temp;
				}
				if ((map.get(s) != null) && (map.get(s).equalsIgnoreCase("" + contactID))) {
					target = temp;
					if (!insert) {
						map.remove(s);
						cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, (contact_count - 1));
						cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(context, map));
						target = -1;
					}
					break;
				}
			}

			if (insert && target == -1) {
				map.put("" + (max_key + 1), "" + contactID);
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, (contact_count + 1));
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(context, map));
				target = -1;
			} else {
				if (insert) {
					cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, contact_count);
					cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(context, map));
					target = -1;
				}
			}
		} else {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, group.contact_count);
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(context, group.contacts));
		}

		if (group.group_id > 0)
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.G_ID, group.group_id);

		cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, group.title);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.CREATED, group.created);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED, group.modified);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.DELETED, group.deleted);

		if (group.image) {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, group.image_url);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, group.image_thumb_url);
		cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, group.image_bytes);
		if (group.remove_image) {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, "0");
		}

		String where = ContactsProvider.CMetaData.GroupsMetaData.G_ID + "=" + group.group_id;
		try {
			context.getContentResolver().update(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, cv, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("insertGroupToLocalDb(contact, " + group.group_id + ")", e.getMessage());
			return false;
		}
	}

	public static int insertGroupToRemoteDb(Context context, Group group, int id) {
		String temp;
		int destination_id = 0;
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_create");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group[id=" + group.group_id + "], " + id + ")", "Failed adding token to entity.");
		}

		try {
			reqEntity
					.addPart(ContactsProvider.CMetaData.GroupsMetaData.G_ID, new StringBody("" + group.group_id, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding group_id to entity.");
		}

		if (group.title != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.TITLE, new StringBody(group.title, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertGroupToRemoteDb(group[id=" + group.group_id + "], " + id + ")", "Failed adding title to entity.");
			}
		} else {
			Log.e("insertGroupToRemoteDb(group[id=" + group.group_id + "], " + id + ")", "Failed getting title from Group object.");
		}

		if (group.image)
			temp = "1";
		else
			temp = "0";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding image to entity.");
		}

		if (group.image_url != null)
			temp = group.image_url;
		else
			temp = "";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding image_url to entity.");
		}

		if (group.image_thumb_url != null)
			temp = group.image_thumb_url;
		else
			temp = "";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding image_thumb_url to entity.");
		}

		if (group.image_bytes != null)
			temp = group.image_bytes.toString();
		else
			temp = "";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding image_bytes to entity.");
		}

		if (group.remove_image)
			temp = "1";
		else
			temp = "0";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding remove_image to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT,
					new StringBody("" + group.contact_count, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding contacts_count to entity.");
		}

		Map<String, String> contacts = group.contacts;
		if (contacts != null) {
			for (String s : contacts.keySet()) {
				try {
					reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS + "[]",
							new StringBody(contacts.get(s), Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding contact to entity.");
				}
			}
		} else {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS + "[]", new StringBody("", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed adding contact to entity.");
			}
		}

		post.setEntity(reqEntity);
		try {
			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (success)
							destination_id = object.getInt("group_id");
						Log.e("createGroup - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("createGroup - error: ", Data.getERROR());
						}
					}
				}
			}
		} catch (Exception ex) {
			Log.e("insertGroupToRemoteDb(group, " + id + ")", "Failed executing POST request.");
			return 0;
		}
		return destination_id;
	}

	/**
	 * Edit a group on remote database.
	 * 
	 * @author audrius6@gmail.com
	 * @param group
	 *            - Group object containing validated group data.
	 * @param contactID
	 *            - contact insert to group.
	 * @param insert
	 *            - if true insert contact to group, if false remove contact
	 *            from group.
	 * @since 2012-11-21
	 * @version 0.1
	 */
	public static boolean editGroupOnRemoteDb(Context context, Group group, int contactID, boolean insert) {
		String temp;
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_edit");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		Map<String, String> map = new HashMap<String, String>();
		if (contactID > 0) {
			int target = -1;
			int max_key = 0;
			map = group.contacts;

			if (map == null) {
				map = new HashMap<String, String>();
			}

			for (String s : map.keySet()) {
				int temp2 = Integer.parseInt(s);
				if (temp2 > max_key) {
					max_key = temp2;
				}
				if ((map.get(s) != null) && (map.get(s).equalsIgnoreCase("" + contactID))) {
					target = temp2;
					if (!insert) {
						map.remove(s);
						group.contact_count -= 1;
						target = -1;
					}
					break;
				}
			}

			if (insert && target == -1) {
				map.put("" + (max_key + 1), "" + contactID);
				group.contact_count += 1;
				target = -1;
			}
		} else {
			map = group.contacts;
		}

		Map<String, String> contacts = map;
		if (contacts != null) {
			for (String s : contacts.keySet()) {
				try {
					reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS + "[]",
							new StringBody(contacts.get(s), Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding contact to entity.");
				}
			}
		} else {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS + "[]", new StringBody("", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding contact to entity.");
			}
		}

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group[id=" + group.group_id + "])", "Failed adding token to entity.");
		}

		try {
			reqEntity
					.addPart(ContactsProvider.CMetaData.GroupsMetaData.G_ID, new StringBody("" + group.group_id, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding group_id to entity.");
		}

		if (group.title != null) {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.TITLE, new StringBody(group.title, Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("editGroupOnRemoteDb(group[id=" + group.group_id + "])", "Failed adding title to entity.");
			}
		} else {
			Log.e("editGroupOnRemoteDb(group[id=" + group.group_id + "])", "Failed getting title from Group object.");
		}

		if (group.image)
			temp = "1";
		else
			temp = "0";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding image to entity.");
		}

		if (group.image_url != null)
			temp = group.image_url;
		else
			temp = "";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding image_url to entity.");
		}

		if (group.image_thumb_url != null)
			temp = group.image_thumb_url;
		else
			temp = "";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding image_thumb_url to entity.");
		}

		if (group.image_bytes != null)
			temp = group.image_bytes.toString();
		else
			temp = "";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding image_bytes to entity.");
		}

		if (group.remove_image)
			temp = "1";
		else
			temp = "0";
		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, new StringBody(temp, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding remove_image to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT,
					new StringBody("" + group.contact_count, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding contacts_count to entity.");
		}

		post.setEntity(reqEntity);
		try {
			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						Log.e("editGroup - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("editGroup - error: ", Data.getERROR());
						}
					}
				}
			} else {
				Log.i("editGroupOnRemoteDb(group" + group.group_id + ")", "No internet connection.");
			}
		} catch (Exception ex) {
			Log.e("editGroupToRemoteDb(group, " + group.group_id + ")", "Failed executing POST request.");
			return false;
		}
		return success;
	}

	/**
	 * Get group's object from local database.
	 * 
	 * Executes a call to SQLite database and retrieving group entry's data.
	 * 
	 * @author meska.lt@gmail.com
	 * @param id
	 *            - Group's ID.
	 * @return Group object.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static Group getGroupFromLocalDb(Context context, int id, long created) {
		Cursor cur;
		Group temp = null;

		if (id > 0) {
			Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI + "/" + id);
			cur = context.getContentResolver().query(uri, null, null, null, null);
			if (cur.moveToFirst())
				temp = new Group(context, cur);
			cur.close();
		} else {
			if (created > 0) {
				String where = ContactsProvider.CMetaData.GroupsMetaData.CREATED + "=" + created;
				cur = context.getContentResolver().query(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, null, where, null, null);
				if (cur.moveToFirst())
					temp = new Group(context, cur);
				cur.close();
			}
		}

		return temp;
	}

	/**
	 * Get all groups entries from remote database.
	 * 
	 * Executes a call to remote database and retrieves all contact entries from
	 * it.
	 * 
	 * @author meska.lt@gmail.com
	 * @return ArrayList of Contact objects got from response.
	 * @since 2012-09-28
	 * @version 0.1
	 */
	public static void getGroupsFromRemoteDb(Context context, HashSet<Integer> contactIds) {
		boolean success = false;
		String error = null;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_list");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			Log.e("getGroupsFromRemoteDb(contactIds)", "Failed adding token to entity");
		}

		if (contactIds != null) {
			Iterator<Integer> it = contactIds.iterator();
			while (it.hasNext()) {
				try {
					reqEntity.addPart("contact_id[]", new StringBody(String.valueOf(it.next()), Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					Log.e("getGroupsFromRemoteDb(contactIds)", "Failed adding groupId to entity");
				}
			}
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
						Log.e("getGroupList - error: ", error);
					} else {
						JSONArray gs = object.getJSONArray("groups");
						int count = gs.length();
						if (count > 0) {
							for (int i = 0; i < count; i++) {
								JSONObject g = gs.getJSONObject(i);
								Group group = new Group();
								String temp;

								try {
									group.group_id = g.getInt(ContactsProvider.CMetaData.GroupsMetaData.G_ID);
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting id.");
								}

								try {
									temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.TITLE);
									if (temp != null && !temp.equals("null"))
										group.title = temp;
									else
										group.title = "";
								} catch (JSONException e) {
									Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting title.");
								}

								try {
									group.created = g.getLong(ContactsProvider.CMetaData.GroupsMetaData.CREATED);
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting created.");
								}

								try {
									group.modified = g.getLong(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED);
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting modified.");
								}

								try {
									temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.DELETED);
									if (temp != null && !temp.equals("null"))
										group.deleted = temp;
									else
										group.deleted = "";
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting phone deleted.");
								}

								try {
									group.image = g.getBoolean(ContactsProvider.CMetaData.GroupsMetaData.IMAGE);
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting image.");
								}

								try {
									temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL);
									if (temp != null && !temp.equals("null"))
										group.image_thumb_url = temp;
									else
										group.image_thumb_url = "";
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting image_thumb_url.");
								}

								try {
									temp = g.getString(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL);
									if (temp != null && !temp.equals("null")) {
										group.image_url = temp;
										try {
											group.image_bytes = Utils.imageToBytes(group.image_url);
										} catch (Exception e) {
											Log.e("getContactsFromRemoteDb(contactIds)", "Failed getting image_bytes.");
										}
									} else
										group.image_url = "";
								} catch (JSONException e) {
									group.image = false;
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting image_url & image_bytes.");
								}

								try {
									group.contact_count = g.getInt(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT);
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting contact_count.");
								}

								try {
									if (!g.getString("contacts").equals("null") && g.getString("contacts") != null) {
										try {
											JSONArray contacts = g.getJSONArray("contacts");
											if (contacts != null) {
												Map<String, String> set = new HashMap<String, String>();
												for (int j = 0, l = contacts.length(); j < l; j++) {
													set.put(String.valueOf(j), contacts.getString(j));
												}
												group.contacts = set;
											}
										} catch (JSONException e) {
											Log.e("getGroupsFromRemoteDb(conta)", "Contacts were null.");
										}
									}
								} catch (JSONException e) {
									Log.e("getGroupsFromRemoteDb(contactIds)", "Failed getting contacts.");
								}

								insertGroupToLocalDb(context, group, 0);
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			Log.e("getContactsFromRemoteDb(contactIds)", ex.getMessage());
		}

		Data.setLoadContactsData(false);
	}

	// TODO removeContactFromRemoteDb(int id) documentation
	public static boolean removeContactFromRemoteDb(Context context, int id) {
		boolean success = false;
		String error = null;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_remove");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.C_ID,
					new StringBody(String.valueOf(id), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("removeContactFromRemoteDb(" + id + ")", "Failed adding contact's ID to entity.");
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("removeContactFromRemoteDb(" + id + ")", "Failed adding user's token to entity.");
		}

		post.setEntity(reqEntity);
		try {
			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						if (success) {
							Log.i("removeContactFromRemoteDb(" + id + ")", "Successfully removed contact!");
						} else {
							error = object.getString("error");
							Log.e("removeContactFromRemoteDb(" + id + ")", "Failed removing contact. " + error);
						}
					} else
						Log.e("removeContactFromRemoteDb(" + id + ")", "No response from server.");
				}
			}
		} catch (Exception ex) {
			Log.e("removeContactFromRemoteDb(" + id + ")", "Failed executing HTTP POST.");
			success = false;
		}
		return success;
	}

	// TODO removeContactFromLocalDb(int id) documentation
	public static boolean removeContactFromLocalDb(Context context, int id) {
		String where = ContactsProvider.CMetaData.ContactsMetaData.C_ID + "=" + id;
		try {
			context.getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("removeContactFromLocalDb(contact, " + id + ")", e.getMessage());
			return false;
		}
	}

	// TODO kad nebutu tokios gedos...
	public static boolean removeContactFromLocalDbByInternalId(Context context, int id) {
		String where = ContactsProvider.CMetaData.ContactsMetaData._ID + "=" + id;
		try {
			context.getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("removeContactFromLocalDb(contact, " + id + ")", e.getMessage());
			return false;
		}
	}

	// TODO removeBirthdayFromLocalDb(int id) documentation
	public static boolean removeBirthdayFromLocalDb(Context context, int id) {
		String where = ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID + "=" + id;
		try {
			context.getContentResolver().delete(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("removeBirthdayFromLocalDb(contact, " + id + ")", e.getMessage());
			return false;
		}
	}

	// TODO editContactOnRemoteDb(Contact c) documentation
	public static boolean editContactOnRemoteDb(Context context, Contact c) {
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_edit");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		if (!c.remove_image) {
			if (c.image_bytes != null) {
				ByteArrayBody bab = new ByteArrayBody(c.image_bytes, ContactsProvider.CMetaData.ContactsMetaData.IMAGE);
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, bab);
			}
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE,
						new StringBody(String.valueOf("0"), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding remove_image to entity.");
			}
		} else {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE,
						new StringBody(String.valueOf("1"), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding remove_image to entity.");
			}
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding token to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.C_ID,
					new StringBody(String.valueOf(c.contact_id), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding contact_id to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.NAME, new StringBody(c.name, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding name to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, new StringBody(c.lastname, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding lastname to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, new StringBody(c.email, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding email to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.PHONE, new StringBody(c.phone1, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding phone1 to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE,
					new StringBody(c.phone1_code, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding phone1 to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, new StringBody(c.birthdate, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding birthdate to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, new StringBody(c.country, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding country to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.CITY, new StringBody(c.city, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding city to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.STREET, new StringBody(c.street, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding street to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.ZIP, new StringBody(c.zip, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding zip to entity.");
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY,
					new StringBody(c.visibility, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding visibility to entity.");
		}

		Map<String, String> groups = c.groups;
		if (groups != null) {
			for (int i = 0, l = groups.size(); i < l; i++) {
				try {
					if (groups.get("" + i) != null) {
						reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.GROUPS + "[]", new StringBody(groups.get("" + i),
								Charset.forName("UTF-8")));
					}
				} catch (UnsupportedEncodingException e) {
					Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding group to entity.");
				}
			}
		} else {
			try {
				reqEntity.addPart(ContactsProvider.CMetaData.ContactsMetaData.GROUPS + "[]", new StringBody("", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				Log.e("editContactOnRemoteDb(contact[contact_id=" + c.contact_id + "])", "Failed adding group to entity.");
			}
		}

		post.setEntity(reqEntity);
		try {
			if (DataManagement.networkAvailable) {
				HttpResponse rp = webService.getResponseFromHttpPost(post);

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");

						success = object.getBoolean("success");

						Log.e("editContact - success", "" + success);

						if (success == false) {
							Data.setERROR(object.getJSONObject("error").getString("reason"));
							Log.e("editContact - error: ", Data.getERROR());
						}
					}
				}
			} else {
				Log.i("editContactOnRemoteDb(" + c.contact_id + ")", "No internet connection.");
			}
		} catch (Exception ex) {
			Log.e("editContactOnRemoteDb(" + c.contact_id + ")", "Failed executing HTTP POST.");
			return false;
		}
		return success;
	}

	/**
	 * Update a contact on local database.
	 * 
	 * Executes a call to SQLite database that updates a contact entry from data
	 * submitted. Currently image_bytes[] isn't stored in the database.
	 * 
	 * @author meska.lt@gmail.com
	 * @param contact
	 *            - Contact object containing validated contact data.
	 * @param id
	 *            - ID corresponding integer.
	 * @since 2012-10-07
	 * @version 0.1
	 */
	public static boolean updateContactOnLocalDb(Context context, Contact contact) {
		ContentValues cv = new ContentValues();

		if (contact.contact_id > 0)
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.C_ID, contact.contact_id);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, contact.name);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, contact.lastname);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, contact.email);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, contact.phone1);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE, contact.phone1_code);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, contact.birthdate);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, contact.country);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.CITY, contact.city);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.STREET, contact.street);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.ZIP, contact.zip);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, contact.visibility);

		if (contact.image) {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL, contact.image_url);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL, contact.image_thumb_url);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, contact.image_bytes);
		if (contact.remove_image) {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW, contact.agenda_view);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED, contact.registered);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.CREATED, contact.created);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED, contact.modified);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(context, contact.groups));

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.COLOR, contact.getColor());

		String where = ContactsProvider.CMetaData.ContactsMetaData.C_ID + "=" + contact.contact_id;
		try {
			context.getContentResolver().update(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, cv, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("insertContactToLocalDb(contact, " + contact.contact_id + ")", e.getMessage());
			return false;
		}
	}

	public static boolean updateBirthdayOnLocalDb(Context context, Contact contact) {
		ContentValues cv = new ContentValues();
		Birthday birthday = getBirthdayFromLocalDb(context, contact.contact_id);

		if (birthday != null && Integer.valueOf(birthday.getBirthdayId()) > 0)
			cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.B_ID, birthday.getBirthdayId());

		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.TITLE, contact.name + " " + contact.lastname);
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE, contact.birthdate);

		String[] date = contact.birthdate.split("-");
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM_DD, date[1] + "-" + date[2]);
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.BIRTHDATE_MM, date[1]);

		if (birthday != null) {
			cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID, birthday.getContact_id());
		}
		cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.COUNTRY, contact.country);

		if (birthday != null) {
			cv.put(ContactsProvider.CMetaData.BirthdaysMetaData.TIMEZONE, birthday.getTimezone());
		}

		String where = ContactsProvider.CMetaData.BirthdaysMetaData.CONTACT_ID + "=" + contact.contact_id;
		try {
			context.getContentResolver().update(ContactsProvider.CMetaData.BirthdaysMetaData.CONTENT_URI, cv, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("insertBirthdayToLocalDb(birthday, " + birthday.getBirthdayId() + ")", e.getMessage());
			return false;
		}
	}

	/**
	 * Method works with local db: rewrites changed contacts data and deletes
	 * contacts that have been removed from remote db.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param contactChanges
	 *            ArrayList that contains contacts that have been changed
	 * @param deletedContactsIDs
	 *            Array that contains ids for contacts that were deleted in
	 *            remote db
	 */
	public static void syncContacts(Context context, ArrayList<Contact> contactChanges, long[] deletedContactsIDs) {

		StringBuilder sb;

		if (!contactChanges.isEmpty()) {
			sb = new StringBuilder();
			for (Contact e : contactChanges) {
				sb.append(e.contact_id);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			bulkDeleteContacts(context, sb.toString());
			bulkInsertContactsToLocalDb(context, contactChanges);

		}

		if (deletedContactsIDs.length > 0) {
			sb = new StringBuilder();
			for (int i = 0; i < deletedContactsIDs.length; i++) {
				sb.append(deletedContactsIDs[i]);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			bulkDeleteContacts(context, sb.toString());
		}

	}

	private static void bulkInsertContactsToLocalDb(Context context, ArrayList<Contact> contactChanges) {

		for (Contact c : contactChanges) {
			insertContactToLocalDb(context, c, 0);
		}
		// TODO implement batch
		// ArrayList<ContentProviderOperation> operations = new
		// ArrayList<ContentProviderOperation>();
		// for (Contact c : contactChanges){
		// Builder op =
		// ContentProviderOperation.newInsert(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI).withValues(createCVFromContact(c));
		// operations.add(op.build());
		// }
		// try {
		// context.getContentResolver().applyBatch(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI.toString(),
		// operations);
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (OperationApplicationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
	
	public static void bulkInsertContactsToRemoteDb(Context context, ArrayList<Contact> contactChanges){
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_create_batch");

		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    
	    try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

	    try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    int i = 0;
		for(Contact c : contactChanges){
			if(c.name != null){
				try {
					reqEntity.addPart("contacts["+ i +"][" + ContactsProvider.CMetaData.ContactsMetaData.NAME +"]", new StringBody(c.name, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(c.email != null){
				try {
					reqEntity.addPart("contacts["+ i +"][" + ContactsProvider.CMetaData.ContactsMetaData.EMAIL +"]", new StringBody(c.email, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(c.country != null){
				try {
					reqEntity.addPart("contacts["+ i +"][" + ContactsProvider.CMetaData.ContactsMetaData.COUNTRY +"]", new StringBody(c.country, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(c.phone1 != null){
				try {
					reqEntity.addPart("contacts["+ i +"][" + ContactsProvider.CMetaData.ContactsMetaData.PHONE +"]", new StringBody(c.phone1, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(c.birthdate != null){
				try {
					reqEntity.addPart("contacts["+ i +"][" + ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE +"]", new StringBody(c.birthdate, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			try {
				reqEntity.addPart("contacts["+ i +"][local_id]", new StringBody(c.getInternal_id()+"", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			i++;
		}
		try {
			post.setEntity(reqEntity);
			@SuppressWarnings("unused")
			HttpResponse httpResponse = webService.getResponseFromHttpPost(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static ContentValues createCVFromContact(Context context, Contact contact) {
		ContentValues cv = new ContentValues();

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.C_ID, contact.contact_id);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.NAME, contact.name);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.LASTNAME, contact.lastname);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.EMAIL, contact.email);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE, contact.phone1);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.PHONE_CODE, contact.phone1_code);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.BIRTHDATE, contact.birthdate);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.COUNTRY, contact.country);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.CITY, contact.city);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.STREET, contact.street);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.ZIP, contact.zip);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.VISIBILITY, contact.visibility);

		if (contact.image) {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_URL, contact.image_url);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_THUMB_URL, contact.image_thumb_url);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.IMAGE_BYTES, contact.image_bytes);
		if (contact.remove_image) {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, "1");
		} else {
			cv.put(ContactsProvider.CMetaData.ContactsMetaData.REMOVE_IMAGE, "0");
		}

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.AGENDA_VIEW, contact.agenda_view);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.REGISTERED, contact.registered);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.CREATED, contact.created);
		cv.put(ContactsProvider.CMetaData.ContactsMetaData.MODIFIED, contact.modified);

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.GROUPS, MapUtils.mapToString(context, contact.groups));

		cv.put(ContactsProvider.CMetaData.ContactsMetaData.COLOR, contact.getColor());
		return cv;
	}

	/**
	 * Method works with local db: rewrites changed groups data and deletes
	 * groups that have been removed from remote db.
	 * 
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param groupChanges
	 *            ArrayList that contains groups that have been changed
	 * @param deletedGroupIDs
	 *            Array that contains ids for groups that were deleted in remote
	 *            db
	 */
	public static void syncGroups(Context context, ArrayList<Group> groupChanges, long[] deletedGroupsIDs) {

		StringBuilder sb;

		if (!groupChanges.isEmpty()) {
			sb = new StringBuilder();
			for (Group e : groupChanges) {
				sb.append(e.group_id);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			bulkDeleteGroups(context, sb.toString());
			bulkInsertGroups(context, groupChanges);

		}

		if (deletedGroupsIDs.length > 0) {
			sb = new StringBuilder();
			for (int i = 0; i < deletedGroupsIDs.length; i++) {
				sb.append(deletedGroupsIDs[i]);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			bulkDeleteGroups(context, sb.toString());
		}

	}

	private static void bulkInsertGroups(Context context, ArrayList<Group> groupChanges) {

		for (Group g : groupChanges) {
			insertGroupToLocalDb(context, g, 0);
		}

		// TODO implement batch operation for better performance

	}

	private static void bulkDeleteContacts(Context context, String IDs) {
		String where;
		StringBuilder sb = new StringBuilder(ContactsProvider.CMetaData.ContactsMetaData.C_ID);
		sb.append(" IN (");
		sb.append(IDs);
		sb.append(')');
		where = sb.toString();
		context.getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, where, null);
	}

	private static void bulkDeleteGroups(Context context, String IDs) {
		String where;
		StringBuilder sb = new StringBuilder(ContactsProvider.CMetaData.GroupsMetaData.G_ID);
		sb.append(" IN (");
		sb.append(IDs);
		sb.append(')');
		where = sb.toString();
		context.getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, where, null);
	}

	/**
	 * Copy other user's contact.
	 * 
	 * Executes a call to remote database and requests other user's contact
	 * entry copy.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2012-10-23
	 * @param context
	 * @param guid
	 *            - GroupAgendas user's ID
	 * @param req
	 *            - Trigger if contact's details should be requested.
	 */
	public static void requestContactCopy(Context context, int guid, boolean req) {
		try {
			Account account = new Account(context);
			WebService webService = new WebService();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_copy");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			try {
				reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}

			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
			reqEntity.addPart("guid", new StringBody("" + guid, Charset.forName("UTF-8")));
			if (req) {
				reqEntity.addPart("req_details", new StringBody("1", Charset.forName("UTF-8")));
			}
			post.setEntity(reqEntity);
			if (DataManagement.networkAvailable) {
				@SuppressWarnings("unused")
				HttpResponse rp = webService.getResponseFromHttpPost(post);
			}
		} catch (Exception e) {

		}
	}

	public static void deleteContact(Context context, Contact contact) {
		Boolean deletedFromRemote = false;
		Boolean insertedIntoQueue = false;
		if (DataManagement.networkAvailable) {
			deletedFromRemote = removeContactFromRemoteDb(context, contact.contact_id);
		}

		if (!deletedFromRemote) {
			SaveDeletedData offlineDeletedContacts = new SaveDeletedData(context);
			insertedIntoQueue = offlineDeletedContacts.addContactForLaterDelete(contact.contact_id);
		}

		if (insertedIntoQueue) {
			removeContactFromLocalDbByInternalId(context, contact.getInternal_id());
			Map<String, String> map = new HashMap<String, String>();
			map = contact.groups;
			if (map != null) {
				for (int i = 0; i < map.size(); i++) {
					map.get("" + i);
					Group g = getGroupFromLocalDb(context, Integer.valueOf(map.get("" + i)), 0);
					if (g != null) {
						updateGroupOnLocalDb(context, g, contact.contact_id, false);
						editGroupOnRemoteDb(context, g, contact.contact_id, false);
					}

				}
			}
			// removeContactFromLocalDb(context, contact.contact_id);
			if (contact.birthdate != null && contact.birthdate.length() == 10) {
				removeBirthdayFromLocalDb(context, contact.contact_id);
			}
		}
	}

	public static void uploadOfflineContact(Context context) {
		Account account = new Account(context);
		String projection[] = null;
		Uri uri = ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI;
		String where = (ContactsProvider.CMetaData.ContactsMetaData.MODIFIED + ">" + account.getLastTimeConnectedToWeb());
		Cursor result = context.getContentResolver().query(uri, projection, where, null, null);// result
																								// nieko
																								// neismeta
		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				Contact c = new Contact(context, result);

				if (c.contact_id != 0) {
					int destination_id = insertContactToRemoteDb(context, c, 0);
					if (destination_id >= 0) {
						updateContactIdInLocalDb(context, c.getInternal_id(), destination_id);

						if (c.groups != null) {
							for (String key : c.groups.keySet()) {
								if (!key.contentEquals("")) {
									Group g = getGroupFromLocalDb(context, Integer.parseInt(c.groups.get(key)), 0);
									editGroupOnRemoteDb(context, g, 0, true);
								}
							}
						}

						c.contact_id = destination_id;
					} else {
						removeContactFromLocalDbByInternalId(context, c.getInternal_id());
					}
					boolean edited = editContactOnRemoteDb(context, c);
					if (edited) {
						updateContactOnLocalDb(context, c);
						if (!c.birthdate.contentEquals("")) {
							updateBirthdayOnLocalDb(context, c);
						}
					}

				}
				result.moveToNext();
			}
		}
		SaveDeletedData offlineDeletedContacts = new SaveDeletedData(context);
		String offlineDeleted = offlineDeletedContacts.getDELETED_CONTACTS();
		String[] ids = offlineDeleted.split(SDMetaData.SEPARATOR);
		if (ids[0] != "") {
			for (int i = 0; i < ids.length; i++) {
				int id = Integer.parseInt(ids[i]);
				removeContactFromRemoteDb(context, id);
			}

		}
		offlineDeletedContacts.clear(1);
		result.close();
	}

	public static boolean removeGroupFromRemoteDb(Context context, int groupId) {
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/group_remove");

		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			reqEntity.addPart("group_id", new StringBody(groupId + "", Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		post.setEntity(reqEntity);
		if (DataManagement.networkAvailable) {
			try {
				HttpResponse rp = webService.getResponseFromHttpPost(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object;
						object = new JSONObject(resp);
						success = object.getBoolean("success");
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	public static void removeGroupFromLocalDb(Context context, int groupId) {
		String where = ContactsProvider.CMetaData.GroupsMetaData.G_ID + "=" + groupId;
		Group group = ContactManagement.getGroupFromLocalDb(context, groupId, 0);

		if (group.contacts != null) {

			for (String s : group.contacts.keySet()) {
				Contact c = ContactManagement.getContactFromLocalDb(context, Integer.parseInt(group.contacts.get(s)), 0);
				if (c != null) {
					if (c.groups != null) {
						Set<String> keySet = c.groups.keySet();
						for (String g : keySet) {
							if (c.groups.get(g).equalsIgnoreCase("" + groupId)) {
								c.groups.remove(g);
								ContactManagement.updateContactOnLocalDb(context, c);
								break;
							}
						}
					}
				}
			}
		}
		try {
			context.getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, where, null);
		} catch (SQLiteException e) {
			Log.e("removeContactFromLocalDb(contact, " + groupId + ")", e.getMessage());
		}
	}

	public static boolean removeGroupFromLocalDbByInternalId(Context context, int id) {
		String where = ContactsProvider.CMetaData.GroupsMetaData._ID + "=" + id;
		try {
			context.getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, where, null);
			return true;
		} catch (SQLiteException e) {
			Log.e("removeContactFromLocalDb(contact, " + id + ")", e.getMessage());
			return false;
		}
	}

	public static void removeGroup(Context context, int groupId) {
		Boolean deletedFromRemote = false;
		if (DataManagement.networkAvailable) {
			deletedFromRemote = removeGroupFromRemoteDb(context, groupId);
		}

		if (!deletedFromRemote) {
			SaveDeletedData offlineDeletedGroups = new SaveDeletedData(context);
			offlineDeletedGroups.addGroupForLaterDelete(groupId);

		}

		removeGroupFromLocalDb(context, groupId);
	}

	public static boolean updateGroupInRemoteDb(Context context, Group group) {
		boolean success = false;
		Account account = new Account(context);
		WebService webService = new WebService();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/groups_edit");

		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("session", new StringBody(account.getSessionId(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			reqEntity.addPart("token", new StringBody(Data.getToken(context), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			reqEntity.addPart("group_id", new StringBody(group.group_id + "", Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			reqEntity.addPart("title", new StringBody(group.title, Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			reqEntity.addPart("remove_image", new StringBody(group.remove_image ? "1" : "0", Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			reqEntity.addPart(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT,
					new StringBody("" + group.contacts.size(), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e("editGroupOnRemoteDb(group, " + group.group_id + ")", "Failed adding contacts_count to entity.");
		}

		if (group.contacts != null && !group.contacts.isEmpty()) {
			for (String contactId : group.contacts.values()) {
				try {
					reqEntity.addPart("contacts[]", new StringBody(contactId, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

		post.setEntity(reqEntity);
		if (DataManagement.networkAvailable) {
			try {
				HttpResponse rp = webService.getResponseFromHttpPost(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object;
						object = new JSONObject(resp);
						success = object.getBoolean("success");
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return success;
	}

	public static void updateGroupInLocalDb() {

	}

	public static void updateGroup() {

	}

	public static void uploadOfflineCreatedGroups(Context context) {
		Account account = new Account(context);
		String projection[] = null;
		Uri uri = ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI;
		String where = ContactsProvider.CMetaData.GroupsMetaData.MODIFIED + ">" + account.getLastTimeConnectedToWeb();
		Cursor result = context.getContentResolver().query(uri, projection, where, null, null);
		if (result.moveToFirst()) {
			while (!result.isAfterLast()) {
				Group group = new Group(context, result);

				if (group.group_id != 0) {
					boolean check = updateGroupInRemoteDb(context, group);
					int destination_id = group.group_id;
					if (!check) {
						destination_id = insertGroupToRemoteDb(context, group, 0);
					}
					if (destination_id >= 0) {
						updateGroupIdInLocalDb(context, group.getInternal_id(), destination_id);
						int max_key = 0;
						if (group.contacts != null) {

							for (String s : group.contacts.keySet()) {
								Contact c = ContactManagement.getContactFromLocalDb(context, Integer.parseInt(group.contacts.get(s)), 0);

								if (c.groups != null) {
									Set<String> keySet = c.groups.keySet();
									for (String g : keySet) {
										if (c.groups.get(g).equalsIgnoreCase("" + group.group_id)) {
											c.groups.remove(g);
											ContactManagement.updateContactOnLocalDb(context, c);
											break;
										}
									}
								}

								if (c.groups != null) {
									for (String key : c.groups.keySet()) {
										int temp = Integer.parseInt(key);
										if (temp > max_key) {
											max_key = temp;
										}
									}
									c.groups.put("" + (max_key + 1), "" + destination_id);
								} else {
									c.groups = new HashMap<String, String>();
									c.groups.put("" + max_key, "" + destination_id);
								}

								ContactManagement.updateContactOnLocalDb(context, c);
							}
						}
						group.group_id = destination_id;
					} else {
						removeGroupFromLocalDbByInternalId(context, group.getInternal_id());
					}
					boolean edited = false;
					if (DataManagement.networkAvailable) {
						edited = updateGroupInRemoteDb(context, group);
					}
					if (edited) {
						updateGroupOnLocalDb(context, group, 0, false);
					}

				}
				result.moveToNext();
			}
			result.close();
		}
		SaveDeletedData offlineDeletedGroups = new SaveDeletedData(context);
		String offlineDeleted = offlineDeletedGroups.getDELETED_GROUPS();
		String[] ids = offlineDeleted.split(SDMetaData.SEPARATOR);
		if (ids[0] != "") {
			for (int i = 0; i < ids.length; i++) {
				int id = Integer.parseInt(ids[i]);
				removeGroupFromRemoteDb(context, id);
			}
		}
		offlineDeletedGroups.clear(2);
		result.close();
	}

}