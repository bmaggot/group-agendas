package com.groupagendas.groupagenda.events;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.utils.InviteDialog;

public class InvitedAdapter extends AbstractAdapter<Invited> {
	int listSize;
	int myID = 0;
	String myFullname = "";

	public InvitedAdapter(Context context, List<Invited> list) {
		super(context, list);
		listSize = list.size();
		Account account = new Account(context);
		myFullname = account.getFullname();
		myID = account.getUser_id();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final Invited invited;
		String temp = "";

		if (view == null) {
			view = mInflater.inflate(R.layout.event_invited_person_entry, null);
		}

		TextView nameView = (TextView) view.findViewById(R.id.invited_fullname);
		TextView statusView = (TextView) view.findViewById(R.id.invited_status);
		TextView emailView = (TextView) view.findViewById(R.id.invited_available_email);
		TextView addToContactView = (TextView) view.findViewById(R.id.add_to_contact);
		
		int statusBackground = 0; 

		invited = list.get(i);
		if (invited != null) {
			temp = invited.getName();
			nameView.setText(temp);
			if(invited.getMy_contact_id() > 0){
				emailView.setText(ContactManagement.getContactFromLocalDb(context, invited.getMy_contact_id(), 0).email);
			}

			if (temp.equals("You") || temp.equals(myFullname))
				view.setTag(Invited.OWN_INVITATION_ENTRY);

			switch (invited.getStatus()) {
				case Invited.REJECTED:
					temp = getContext().getResources().getString(R.string.status_not_attending);
					statusBackground = getContext().getResources().getColor(R.color.darker_gray);
					break;
				case Invited.ACCEPTED:
					temp = getContext().getResources().getString(R.string.status_attending);
					statusBackground = Color.parseColor("#26b2d8");
					break;
				case Invited.MAYBE:
					temp = getContext().getResources().getString(R.string.status_maybe);
					statusBackground = getContext().getResources().getColor(R.color.lighter_gray);
					break;
				case Invited.PENDING:
					temp = getContext().getResources().getString(R.string.status_pending);
					statusBackground = getContext().getResources().getColor(R.color.lighter_gray);
					break;
				default:
					temp = "";
					break;
			}
			statusView.setText(temp);
			statusView.setBackgroundColor(statusBackground);
		}
		
		if (i == listSize - 1)
			view.setBackgroundResource(R.drawable.event_invited_entry_last_background);
		
		if (((invited.getGuid() != myID) && (invited.getMy_contact_id() < 1)) || ((invited.getGuid() > 0) && invited.getMy_contact_id() < 1)) {
			addToContactView.setVisibility(View.VISIBLE);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					InviteDialog dia = new InviteDialog(context, 0, invited);
					dia.show();
				}
			});
		}

		return view;
	}
}
