package com.groupagendas.groupagenda.contacts.importer;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.ContactManagement;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

public class SimContactImport {
	public static void ImportSimContacts(Context context){
		Cursor phoneContacts = context.getContentResolver()
				.query(RawContacts.CONTENT_URI,
						new String[] { RawContacts._ID, RawContacts.ACCOUNT_TYPE, ContactsContract.Contacts.DISPLAY_NAME, 
								ContactsContract.CommonDataKinds.Email._ID },
						RawContacts.ACCOUNT_TYPE + " == 'com.android.contacts.sim' " + " AND " + RawContacts.ACCOUNT_TYPE
								+ " != 'com.google' ", null, null);
		if (phoneContacts.moveToFirst()) {
			do {
				String displayName = phoneContacts.getString(phoneContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String rawContactId = phoneContacts.getString(phoneContacts.getColumnIndex(RawContacts._ID));
				String[] nameArray;
				String firstName = "";
				String lastName = ""; 
				Contact phoneContact = new Contact();
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
				phoneContact.name = firstName;
				if (!displayName.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})")) {

				}
				phoneContact.email = getContactEmail(rawContactId, context, phoneContact);
				phoneContact.phone1 = getContactPhoneNumber(rawContactId, context, phoneContact);
				phoneContact.lastname = lastName;
				ContactManagement.insertContact(context, phoneContact);
			} while (phoneContacts.moveToNext());
		}
		phoneContacts.close();
	}
	
	public static String getContactEmail(String id, Context context, Contact contact) {
		String email = "";
		Cursor emailCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID + " = ?", new String[] { id }, null);
		while (emailCur.moveToNext()) {
			email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		}
		emailCur.close();
		return email;
	}

	public static String getContactPhoneNumber(String id, Context context, Contact contact) {
		String phone = "";
		Cursor pCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = ?", new String[] { id }, null);
		while (pCur.moveToNext()) {
			phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
		}
		pCur.close();
		return phone;
	}

}
