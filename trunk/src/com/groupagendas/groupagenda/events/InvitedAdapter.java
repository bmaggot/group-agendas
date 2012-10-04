package com.groupagendas.groupagenda.events;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.contacts.ContactsProvider;

public class InvitedAdapter extends AbstractAdapter<Invited> {

	public InvitedAdapter(Context context, List<Invited> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		Invited invited;
		Cursor cur;
		Account account = new Account();
		String temp = "";
		
		if (view == null) {
			view = mInflater.inflate(R.layout.invited_item, null);
		}
		
		TextView nameView = (TextView) view.findViewById(R.id.invited_fullname);
		TextView statusView = (TextView) view.findViewById(R.id.invited_status);
		TextView emailView = (TextView) view.findViewById(R.id.invited_available_email);

		invited = list.get(i);
		if (invited != null) {
			temp = invited.getName();
			nameView.setText(temp);
			
			if (temp.equals("You"))
				nameView.setTag(Invited.OWN_INVITATION_ENTRY);
			
			statusView.setText(invited.getStatus());
			
			if (invited.getGuid() > 0) {
				if (invited.getGuid() == account.getUser_id())
					emailView.setText(account.getEmail());
			} else if (invited.getMy_contact_id() > 0) {
				String[] projection = {ContactsProvider.CMetaData.ContactsMetaData.EMAIL};
				temp = EventsProvider.EMetaData.InvitedMetaData.MY_CONTACT_ID + "=" + invited.getMy_contact_id();
				cur = context.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, projection, temp, null, null);
				if (cur.moveToFirst()) {
					temp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
					emailView.setText(temp);
				} else {
					emailView.setText("");
					Log.e("InvitedAdapter.getView()", "Failed getting own contact's email.");
				}
			}
		}
		
		return view;
	}
}
