package com.groupagendas.groupagenda.utils;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventsProvider;

public class EventStatusUpdater extends AsyncTask<Object, Void, Void>{

	@Override
	protected Void doInBackground(Object... params) {
		int event_id = (Integer) params[0];
		int status = (Integer) params[1]; 
		boolean success = (Boolean) params[2];
		DataManagement dm = (DataManagement) params[3];
		ContentValues values = new ContentValues();
		values.put(EventsProvider.EMetaData.EventsMetaData.STATUS, status);
		if(!success){
			values.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 4);
		}
		String where = EventsProvider.EMetaData.EventsMetaData.E_ID+"="+event_id;
		dm.getmContext().getContentResolver().update(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, values, where, null);
		return null;
	}
}
