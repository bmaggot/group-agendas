package com.groupagendas.groupagenda.contacts.importer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.ContactManagement;

public class PhoneContactImport {
	public static void importPhoneContacts(Context context){
	String id = "";

	ContentResolver cr = context.getContentResolver();
	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

	if (cur.moveToFirst()) {
		while (!cur.isAfterLast()) {
			Contact phoneContact2 = new Contact();
			id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			Cursor cur1 = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);

			if (cur1.moveToFirst()) {
				while (!cur1.isAfterLast()) {
					String displayName = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
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
					if (phone.moveToFirst()) {
						while (!phone.isAfterLast()) {
							phoneContact2.phone1 = phone.getString(phone
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							break;
						}
					}
					phone.close();

					phoneContact2.email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					break;
				}
				cur1.close();
			}

			cur1 = context.getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + id, null, null);
			if (cur1.moveToFirst()) {
				while (!cur1.isAfterLast()) {
					phoneContact2.city = cur1
							.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
					phoneContact2.street = cur1.getString(cur1
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
					phoneContact2.zip = cur1.getString(cur1
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
					break;
				}
				cur1.close();
			}
			ContactManagement.insertContact(context, phoneContact2);
			cur.moveToNext();
		}
	}
	cur.close();
}

}
