package com.groupagendas.groupagenda.events;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.account.AccountActivity;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.contacts.ContactInfoActivity;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.utils.InviteDialog;

public class InvitedAdapter extends AbstractAdapter<Invited> {
	int listSize;
	int myID = 0;
	String myFullname = "";
	View view;
	int event_id = 0;

	public InvitedAdapter(Context context, List<Invited> list, int event_id) {
		super(context, list);
		this.event_id = event_id;
		listSize = list.size();
		Account account = new Account(context);
		myFullname = account.getFullname();
		myID = account.getUser_id();
	}

	@Override
	public View getView(final int i, View view, final ViewGroup viewGroup) {
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
			if (invited.getGuid() != new Account(context).getUser_id()) {
				nameView.setText(temp);
			} else {
				nameView.setText(context.getResources().getString(R.string.you));
			}
			if (invited.getMy_contact_id() == 1) {
				emailView.setText(getContext().getResources().getString(R.string.adding));
			}
			if (invited.getMy_contact_id() > 1 && ContactManagement.getContactFromLocalDb(context, invited.getMy_contact_id(), 0) != null) {
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

		if ((((invited.getGuid() != myID) && (invited.getMy_contact_id() < 1)) || ((invited.getGcid() > 0) && invited.getMy_contact_id() < 1))
				&& ContactManagement.getContactFromLocalDb(context, invited.getMy_contact_id(), 0) == null) {
			addToContactView.setVisibility(View.VISIBLE);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					InviteDialog dia = new InviteDialog(context, 0, invited, event_id);
					dia.show();
				}
			});
		} else if (ContactManagement.getContactFromLocalDb(context, invited.getMy_contact_id(), 0) != null){
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent contactIntent = new Intent(context, ContactInfoActivity.class);
					contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					contactIntent.putExtra("contactId", invited.getMy_contact_id());
					context.startActivity(contactIntent);
				}
			});
		} else if (invited.getGuid() == new Account(context).getUser_id()) {
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent contactIntent = new Intent(context, AccountActivity.class);
					contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(contactIntent);
				}
			});
		}

		return view;
	}
}
