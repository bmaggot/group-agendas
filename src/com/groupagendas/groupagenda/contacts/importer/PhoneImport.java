package com.groupagendas.groupagenda.contacts.importer;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.importer.phone.Importer;
import com.groupagendas.groupagenda.data.DataManagement;

public class PhoneImport {

	public void importContactsFromPhone(ImportActivity activity) {
		// import sim contacts
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

			phoneContact.name = firstName;
			phoneContact.lastname = lastName;
			phoneContact.phone1 = phoneNumber;
			Object[] array = { activity, phoneContact };
			new Importer().execute(array);
		}
		phones.close();
		// import phone contacts
		Cursor phoneContacts = activity.getContentResolver().query(
				RawContacts.CONTENT_URI,
				new String[] { RawContacts._ID, RawContacts.ACCOUNT_TYPE, ContactsContract.Contacts.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Email._ID },
				RawContacts.ACCOUNT_TYPE + " <> 'com.android.contacts.sim' " + " AND " + RawContacts.ACCOUNT_TYPE + " == 'com.google' ",
				null, null);
		int in = 0;
		while (phoneContacts.moveToNext()) {
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
			if(!displayName.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})")){
				
			}
			phoneContact.email = getContactEmail(rawContactId, activity, phoneContact);
			phoneContact.phone1 = getContactPhoneNumber(rawContactId, activity, phoneContact);
			phoneContact.lastname = lastName;
			Object[] array = { activity, phoneContact };
			new Importer().execute(array);
		}
		phoneContacts.close();
		DataManagement.getInstance(activity).getContactsFromRemoteDb(null);
		activity.onBackPressed();
	}

	public String getContactEmail(String id, ImportActivity activity, Contact contact) {
		String email = "";
		Cursor emailCur = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID + " = ?", new String[] { id }, null);
		while (emailCur.moveToNext()) {
			email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		}
		emailCur.close();
		return email;
	}

	public String getContactPhoneNumber(String id, ImportActivity activity, Contact contact) {
		String phone = "";
		Cursor pCur = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = ?", new String[] { id }, null);
		while (pCur.moveToNext()) {
			phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
		}
		pCur.close();
		return phone;
	}
	
}
