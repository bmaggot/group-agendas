package com.groupagendas.groupagenda.contacts.importer;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;

public class PhoneContactImport {
	public static int[] importPhoneContacts(Context context) {
		String id = "";
		int[] importStats = new int[3];
		int importedContactAmount = 0;
		int unimportedContactAmount = 0;
		int totalEntries = 0;

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		totalEntries = cur.getCount();
		ArrayList<Contact> phoneContacts = new ArrayList<Contact>(totalEntries);

		while (cur.moveToNext()) {
			Contact phoneContact2 = new Contact();
			id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			Cursor cur1 = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
			String displayName = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String[] nameArray;
			String firstName = "";
			String lastName = "";

			if (displayName.matches("[0-9,A-Z,a-z]*\\s[0-9,A-Z,a-z]*")) {
				nameArray = displayName.split("\\s");
				firstName = nameArray[0];
				if (nameArray.length <= 2) {
					lastName = nameArray[1];
				} else if (nameArray.length > 2) {
					for (int i = 1; i < nameArray.length; i++) {
						lastName = lastName + nameArray[i];
					}
				}
			} else {
				firstName = displayName;
				lastName = "";
			}

			phoneContact2.name = firstName;
			phoneContact2.lastname = lastName;

			Cursor phone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
			if (phone != null) {
				if (phone.moveToFirst())
					phoneContact2.phone1 = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				phone.close();
			}

			if (cur1 != null) {
				if (cur1.moveToFirst())
					phoneContact2.email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				cur1.close();
			}

			cur1 = context.getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + id, null, null);
			if (cur1 != null) {
				if (cur1.moveToFirst()) {
					phoneContact2.city = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
					phoneContact2.street = cur1
							.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
					phoneContact2.zip = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
				}
				cur1.close();
			}
			try {
				Cursor bdc = cr.query(android.provider.ContactsContract.Data.CONTENT_URI, new String[] { Event.DATA },
						android.provider.ContactsContract.Data.CONTACT_ID + " = " + id + " AND " + Data.MIMETYPE + " = '"
								+ Event.CONTENT_ITEM_TYPE + "' AND " + Event.TYPE + " = " + Event.TYPE_BIRTHDAY, null,
						android.provider.ContactsContract.Data.DISPLAY_NAME);
				while (bdc.moveToNext()) {
					phoneContact2.birthdate = bdc.getString(0);
				}
			} catch (Exception e) {
				Log.i("PhoneContactImport", "No BDay");
			}
			phoneContacts.add(phoneContact2);
		}
		cur.close();
		
		if (totalEntries > 0)
			ContactManagement.bulkInsertContactsToRemoteDb(context, phoneContacts);
		
		DataManagement.synchronizeWithServer(context, null, new Account(context).getLatestUpdateUnixTimestamp());
		importStats[0] = importedContactAmount;
		importStats[1] = unimportedContactAmount;
		importStats[2] = totalEntries;

		return importStats;
	}

}
