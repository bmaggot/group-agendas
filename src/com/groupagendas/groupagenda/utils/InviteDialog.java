package com.groupagendas.groupagenda.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;

import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventEditActivity;
import com.groupagendas.groupagenda.events.Invited;

public class InviteDialog extends Dialog {

	private Invited source;
	private Button inviteAndRequest;
	private Button justInvite;
	private Button dontInvite;
	private boolean req = false;

	public InviteDialog(final Context context, int styleResId, Invited invited) {
		super(context, styleResId);
		this.setContentView(R.layout.contacts_invite);
		this.source = invited;
		this.setTitle(context.getResources().getString(R.string.do_you_want_to_save_contact) + " " + invited.getName() + " "
				+ context.getResources().getString(R.string.to_your_list));

		inviteAndRequest = (Button) findViewById(R.id.contact_invite_and_request);
		inviteAndRequest.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				req = true;
				new Invite().execute(new Object[]{InviteDialog.this.source, context});
				dismiss();
			}
		});
		justInvite = (Button) findViewById(R.id.contact_only_invite);
		justInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				req = false;
				new Invite().execute(new Object[]{InviteDialog.this.source, context});
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
			Context context = (Context) params[1];
			ContactManagement.requestContactCopy(getContext(), i.getGuid(), i.getGcid(), InviteDialog.this.req);
			DataManagement.synchronizeWithServer(getContext(), null, new Account(getContext()).getLatestUpdateUnixTimestamp());
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			Intent intent = new Intent(C2DMReceiver.REFRESH_EVENT_EDIT_ACTIVITY);
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
		}
		
	}
}
