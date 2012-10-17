package com.groupagendas.groupagenda.events;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;

public class InvitedAdapter extends AbstractAdapter<Invited> {
	int listSize;
	String myFullname = "";

	public InvitedAdapter(Context context, List<Invited> list) {
		super(context, list);
		listSize = list.size();
		Account account = new Account(context);
		myFullname = account.getFullname();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		Invited invited;
		String temp = "";

		if (view == null) {
			view = mInflater.inflate(R.layout.event_invited_person_entry, null);
		}

		TextView nameView = (TextView) view.findViewById(R.id.invited_fullname);
		TextView statusView = (TextView) view.findViewById(R.id.invited_status);
		@SuppressWarnings("unused")
		TextView emailView = (TextView) view.findViewById(R.id.invited_available_email);

		invited = list.get(i);
		if (invited != null) {
			temp = invited.getName();
			nameView.setText(temp);

			if (temp.equals("You") || temp.equals(myFullname))
				view.setTag(Invited.OWN_INVITATION_ENTRY);

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
		}
		
		if (i == listSize - 1)
			view.setBackgroundResource(R.drawable.event_invited_entry_last_background);

		return view;
	}
}
