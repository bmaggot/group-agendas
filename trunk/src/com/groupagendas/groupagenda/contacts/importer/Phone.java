package com.groupagendas.groupagenda.contacts.importer;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.importer.phone.Importer;

public class Phone {

	public void importContactsFromPhone(ImportActivity activity) {
		//import sim contacts
		Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		phones.moveToFirst();
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
			phoneContact.name = firstName;
			System.out.println(firstName);
			phoneContact.lastname = lastName;
			System.out.println(lastName + "\n");
			phoneContact.phone1 = phoneNumber;
			Object[] array = { activity, phoneContact };
			new Importer().execute(array);
		}
		phones.close();
		//import phone contacts
		Cursor phoneContacts = activity.getContentResolver().query(RawContacts.CONTENT_URI,
				new String[] { RawContacts._ID, RawContacts.ACCOUNT_TYPE },
				RawContacts.ACCOUNT_TYPE + " <> 'com.anddroid.contacts.sim' " + " AND " + RawContacts.ACCOUNT_TYPE + " <> 'com.google' ",
				null, null);
		phoneContacts.moveToFirst();
		while(phoneContacts.moveToNext()){
			
		}
		phoneContacts.close();
	}
}
