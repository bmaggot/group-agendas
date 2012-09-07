package com.groupagendas.groupagenda.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;

import com.groupagendas.groupagenda.R;

public class InviteDialog extends Dialog {

	private Activity context;
	private Button inviteAndRequest;
	private Button justInvite;
	private Button dontInvite;

	public InviteDialog(Activity context, int styleResId, String name) {
		super(context, styleResId);
		this.context = context;
		this.setContentView(R.layout.contacts_invite);
		this.setTitle(context.getResources().getString(R.string.do_you_want_to_save_contact) + " " + name + " "
				+ context.getResources().getString(R.string.to_your_list));

		inviteAndRequest = (Button) findViewById(R.id.contact_invite_and_request);
		justInvite = (Button) findViewById(R.id.contact_only_invite);
		dontInvite = (Button) findViewById(R.id.contact_dont_invite);

		dontInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
