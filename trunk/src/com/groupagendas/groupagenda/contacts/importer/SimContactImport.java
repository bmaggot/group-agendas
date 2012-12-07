package com.groupagendas.groupagenda.contacts.importer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.ContactManagement;

public class SimContactImport {
	public static final String DISPLAY_NAME = "name";
	public static final String NUMBER = "number";
	public static final String URI = "content://icc/adn";

	public static int[] importSimContacts(Context context) {

		String simPhoneName = "";
		String simPhoneNo = "";
		int[] importStats = new int[3];
		int importedContactAmount = 0;
		int unimportedContactAmount = 0;
		int totalEntries = 0;

		Uri simUri = Uri.parse(URI);
		Cursor cursorSim = context.getContentResolver().query(simUri, null, null, null, null);
		totalEntries = cursorSim.getCount();

		if (cursorSim.moveToFirst()) {
			while (!cursorSim.isAfterLast()) {
				Contact simContact = new Contact();
				simPhoneName = cursorSim.getString(cursorSim.getColumnIndex(DISPLAY_NAME));
				String[] nameArray;
				String firstName = "";
				String lastName = "";

				if (simPhoneName.matches("[0-9,A-Z,a-z]*\\s[0-9,A-Z,a-z]*")) {
					nameArray = simPhoneName.split("\\s");
					firstName = nameArray[0];
					if (nameArray.length <= 2) {
						lastName = nameArray[1];
					} else if (nameArray.length > 2) {
						for (int i = 1; i < nameArray.length; i++) {
							lastName = lastName + nameArray[i];
						}
					}
				} else {
					firstName = simPhoneName;
					lastName = "";
				}

				simPhoneNo = cursorSim.getString(cursorSim.getColumnIndex(NUMBER));
				simPhoneNo.replaceAll("\\D", "");
				simPhoneNo.replaceAll("&", "");
				simPhoneName = simPhoneName.replace("|", "");
				simContact.name = firstName;
				simContact.lastname = lastName;
				simContact.phone1 = simPhoneNo;
				Log.i("SimContacts", simPhoneName);
				Log.i("SimContactsNo", simPhoneNo);
				
				if(ContactManagement.insertContact(context, simContact, false)){
					importedContactAmount++;
				} else {
					unimportedContactAmount++;
				}
				cursorSim.moveToNext();
			}
		}
		cursorSim.close();
		importStats[0] = importedContactAmount;
		importStats[1] = unimportedContactAmount;
		importStats[2] = totalEntries;
		
		return importStats;
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
