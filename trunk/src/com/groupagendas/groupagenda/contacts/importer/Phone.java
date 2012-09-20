package com.groupagendas.groupagenda.contacts.importer;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.RawContacts;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.importer.phone.Importer;
import com.groupagendas.groupagenda.data.DataManagement;

public class Phone {

	public void importContactsFromPhone(ImportActivity activity) {
		//import sim contacts
		Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		while (phones.moveToNext()) {
			String displayName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String[] nameArray;
			String firstName = "";
			String lastName = "";
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
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
			//email
//			int contactId = Integer.parseInt(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
//			Cursor emailsCur = activity.getContentResolver().query(Email.CONTENT_URI, null,
//                    Email.CONTACT_ID + " = " + contactId, null, null);
//
//			String email = "";
//			while (emailsCur.moveToNext()) {
//				email = emailsCur.getString(emailsCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
//			}
//			emailsCur.close();
			
			phoneContact.name = firstName;
			phoneContact.lastname = lastName;
//			phoneContact.email = email;
			phoneContact.phone1 = phoneNumber;
			Object[] array = { activity, phoneContact };
			new Importer().execute(array);
		}
		phones.close();
		//import phone contacts
		Cursor phoneContacts = activity.getContentResolver().query(RawContacts.CONTENT_URI,
				new String[] { RawContacts._ID, RawContacts.ACCOUNT_TYPE, ContactsContract.Contacts.DISPLAY_NAME },
				RawContacts.ACCOUNT_TYPE + " <> 'com.android.contacts.sim' " + " AND " + RawContacts.ACCOUNT_TYPE + " == 'com.google' ",
				null, null);
		while(phoneContacts.moveToNext()){
			String displayName = phoneContacts.getString(phoneContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
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
			System.out.println(firstName);
			phoneContact.lastname = lastName;
			Object[] array = { activity, phoneContact };
			new Importer().execute(array);
		}
		phoneContacts.close();
		DataManagement.getInstance(activity).getContactsFromRemoteDb(null);
		activity.onBackPressed();
	}
}
