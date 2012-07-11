package com.groupagendas.groupagenda;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;

public class UpdateService extends Service{
	private Context mContext;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ContentValues cv = new ContentValues();
		cv.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 0);
		
		mContext = this.getApplicationContext();
		DataManagement dm = DataManagement.getInstance(mContext);
		
		// events
			//edit
		Event event;
		String where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE+"=1";
		Cursor result = mContext.getContentResolver().query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int event_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID));
			event = dm.getEventFromDb(event_id);
			final boolean success = dm.editEvent(event);
			if(success){
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+"/"+event_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// create
		where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE+"=2";
		result = mContext.getContentResolver().query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int event_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID));
			event = dm.getEventFromDb(event_id);
			final boolean success = dm.createEvent(event);
			if(success){
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+"/"+event_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// remove
		where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE+"=3";
		result = mContext.getContentResolver().query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int event_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID));
			event = dm.getEventFromDb(event_id);
			final boolean success = dm.removeEvent(event_id);
			if(success){
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+"/"+event_id);
				mContext.getContentResolver().delete(uri, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// change status
		where = EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE+"=4";
		result = mContext.getContentResolver().query(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			int event_id = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.E_ID));
			int status   = result.getInt(result.getColumnIndex(EventsProvider.EMetaData.EventsMetaData.STATUS));
			final boolean success = dm.changeEventStatus(event_id, String.valueOf(status));
			if(success){
				Uri uri = Uri.parse(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI+"/"+event_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
		
		
		// groups
			//edit
		Group group;
		where = ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE+"=1";
		result = mContext.getContentResolver().query(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int group_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
			group = dm.getGroup(mContext, group_id);
			final boolean success = dm.editGroup(group);
			if(success){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI+"/"+group_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// create
		where = ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE+"=2";
		result = mContext.getContentResolver().query(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int group_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
			group = dm.getGroup(mContext, group_id);
			final boolean success = dm.createGroup(group);
			if(success){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI+"/"+group_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// delete
		where = ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE+"=3";
		result = mContext.getContentResolver().query(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int group_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.GroupsMetaData.G_ID));
			group = dm.getGroup(mContext, group_id);
			final boolean success = dm.removeGroup(group_id);
			if(success){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI+"/"+group_id);
				mContext.getContentResolver().delete(uri, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
		
		
		
		
		
		// contact
			// edit
		Contact contact;
		where = ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE+"=1";
		result = mContext.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int contact_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
			contact = dm.getContact(contact_id);
			final boolean success = dm.editContact(contact);
			if(success){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI+"/"+contact_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// create
		where = ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE+"=2";
		result = mContext.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int contact_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
			contact = dm.getContact(contact_id);
			final boolean success = dm.createContact(contact);
			if(success){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI+"/"+contact_id);
				mContext.getContentResolver().update(uri, cv, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
			// remove
		where = ContactsProvider.CMetaData.ContactsMetaData.NEED_UPDATE+"=3";
		result = mContext.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, null, where, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			final int contact_id = result.getInt(result.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.C_ID));
			contact = dm.getContact(contact_id);
			final boolean success = dm.removeContact(contact.contact_id);
			if(success){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI+"/"+contact_id);
				mContext.getContentResolver().delete(uri, null, null);
			}
			
			result.moveToNext();
		}
		result.close();
		
		// Account
		result = mContext.getContentResolver().query(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, null, null, null, null);
		if(result.moveToFirst()){
			int needUpdate = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.NEED_UPDATE));
			
			switch(needUpdate){
			case 1:
				 Account acc = dm.getAccountFromDb();
				 boolean removeImage = (acc.remove_image == 1)?true:false;
				 dm.updateAccount(acc, removeImage);
				break;
			// update settings
			case 2:
				int am_pm = result.getInt(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_AMPM));
				String dateformat = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_DATE_FORMAT));
				String defaultview = result.getString(result.getColumnIndex(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW));
				final boolean success = dm.changeCalendarSettings(am_pm, defaultview, dateformat);
				if(success){
					mContext.getContentResolver().update(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, cv, null, null);
				}
				break;
			}
		}
			// Autoicon
			where = AccountProvider.AMetaData.AutoiconMetaData.NEED_UPDATE+"=1";
			result = mContext.getContentResolver().query(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, null, where, null, null);
			if(result.moveToFirst()){
				final boolean success = dm.setAutoIcons();
				if(success){
					getContentResolver().update(AccountProvider.AMetaData.AutoiconMetaData.CONTENT_URI, cv, "", null);
				}
			}
			// Autocolor
			where = AccountProvider.AMetaData.AutocolorMetaData.NEED_UPDATE+"=1";
			result = mContext.getContentResolver().query(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, null, where, null, null);
			if(result.moveToFirst()){
				final boolean success = dm.setAutoColors();
				if(success){
					getContentResolver().update(AccountProvider.AMetaData.AutocolorMetaData.CONTENT_URI, cv, "", null);
				}
			}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		stopSelf();
	}

}
