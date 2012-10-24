package com.groupagendas.groupagenda.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.events.Invited;

public class InviteDialog extends Dialog {

	private Invited source;
	private Button inviteAndRequest;
	private Button justInvite;
	private Button dontInvite;
	private boolean req = false;

	public InviteDialog(Context context, int styleResId, Invited invited) {
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
				new Invite().execute(InviteDialog.this.source);
				dismiss();
			}
		});
		justInvite = (Button) findViewById(R.id.contact_only_invite);
		justInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				req = false;
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
	
	public class Invite extends AsyncTask<Invited, Void, Void> {

		@Override
		protected Void doInBackground(Invited... params) {
			Invited i = params[0];
			ContactManagement.requestContactCopy(getContext(), i.getGuid(), InviteDialog.this.req);
			return null;
		}
		
	}
}
