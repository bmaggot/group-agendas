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
	int listSize;

	public InvitedAdapter(Context context, List<Invited> list) {
		super(context, list);
		listSize = list.size();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		Invited invited;
		Cursor cur;
		Account account = new Account();
		String temp = "";

		if (view == null) {
			view = mInflater.inflate(R.layout.event_invited_person_entry, null);
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

			switch (invited.getStatus()) {
				case Invited.REJECTED:
					temp = getContext().getResources().getString(R.string.status_not_attending);
					break;
				case Invited.ACCEPTED:
					temp = getContext().getResources().getString(R.string.status_attending);
					break;
				case Invited.MAYBE:
					temp = getContext().getResources().getString(R.string.status_maybe);
					break;
				case Invited.PENDING:
					temp = getContext().getResources().getString(R.string.status_pending);
					break;
				default:
					temp = "";
					break;
			}
			statusView.setText(temp);

//			if (invited.getGuid() > 0) {
//				if (invited.getGuid() == account.getUser_id())
//					emailView.setText(account.getEmail());
//			} else if (invited.getMy_contact_id() > 0) {
//				String[] projection = { ContactsProvider.CMetaData.ContactsMetaData.EMAIL };
//				temp = ContactsProvider.CMetaData.ContactsMetaData.C_ID + "=" + invited.getMy_contact_id();
//				cur = context.getContentResolver().query(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, projection, temp, null,
//						null);
//				if (cur.moveToFirst()) {
//					temp = cur.getString(cur.getColumnIndex(ContactsProvider.CMetaData.ContactsMetaData.EMAIL));
//					emailView.setText(temp);
//				} else {
//					emailView.setText("");
//					Log.e("InvitedAdapter.getView()", "Failed getting own contact's email.");
//				}
//			}
		}
		
		if (i == listSize - 1)
			view.setBackgroundResource(R.drawable.event_invited_entry_last_background);

		return view;
	}
}
