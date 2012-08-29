package com.groupagendas.groupagenda.contacts.importer.phone;

import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;

import android.app.Activity;
import android.os.AsyncTask;

public class Importer extends AsyncTask<Object, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Object... params) {
		return DataManagement.getInstance((Activity) params[0]).createContact((Contact) params[1]);
	}

}
