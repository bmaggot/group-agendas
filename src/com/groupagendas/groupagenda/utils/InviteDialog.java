package com.groupagendas.groupagenda.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Invited;

public class InviteDialog extends Dialog {

	private Invited source;
	private Button inviteAndRequest;
	private Button justInvite;
	private Button dontInvite;
	private View view;
	private boolean req = false;
	int event_id = 0;

	public InviteDialog(Context context, int styleResId, Invited invited, View view, int event_id) {
		super(context, styleResId);
		this.view = view;
		this.setContentView(R.layout.contacts_invite);
		this.source = invited;
		this.event_id = event_id;
		this.setTitle(context.getResources().getString(R.string.do_you_want_to_save_contact) + " " + invited.getName() + " "
				+ context.getResources().getString(R.string.to_your_list));
		inviteAndRequest = (Button) findViewById(R.id.contact_invite_and_request);
		inviteAndRequest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				req = true;
				TextView emailTextView = (TextView) InviteDialog.this.view.findViewById(R.id.invited_available_email);
				emailTextView.setText(getContext().getResources().getString(R.string.adding));
				TextView addToContactView = (TextView) InviteDialog.this.view.findViewById(R.id.add_to_contact);
				addToContactView.setVisibility(View.INVISIBLE);
				new Invite().execute(InviteDialog.this.source);
				dismiss();
			}
		});
		justInvite = (Button) findViewById(R.id.contact_only_invite);
		justInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				req = false;
				TextView emailTextView = (TextView) InviteDialog.this.view.findViewById(R.id.invited_available_email);
				emailTextView.setText(getContext().getResources().getString(R.string.adding));
				TextView addToContactView = (TextView) InviteDialog.this.view.findViewById(R.id.add_to_contact);
				addToContactView.setVisibility(View.INVISIBLE);
				new Invite().execute(InviteDialog.this.source);
				dismiss();
			}
		});
		dontInvite = (Button) findViewById(R.id.contact_dont_invite);

		dontInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public class Invite extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			Invited i = (Invited) params[0];
			ContactManagement.requestContactCopy(getContext(), i.getGuid(), i.getGcid(), InviteDialog.this.req);
			DataManagement.synchronizeWithServer(getContext(), null, new Account(getContext()).getLatestUpdateUnixTimestamp());
			EventManagement.updateEventByIdFromRemoteDb(getContext(), event_id + "");
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			Intent intent = new Intent(C2DMReceiver.REFRESH_EVENT_EDIT_ACTIVITY);
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
		}
		
	}
}
