package com.groupagendas.groupagenda.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.OfflineData;
import com.groupagendas.groupagenda.events.Invited;

public class InviteDialog extends Dialog {

//	private Activity context;
	private Button inviteAndRequest;
	private Button justInvite;
	private Button dontInvite;
	private Invited invited;
	private boolean req = false;

	public InviteDialog(Activity context, int styleResId, final Invited invited) {
		super(context, styleResId);
//		this.context = context;
		this.invited = invited;
		this.setContentView(R.layout.contacts_invite);
		this.setTitle(context.getResources().getString(R.string.do_you_want_to_save_contact) + " " + invited.getName() + " "
				+ context.getResources().getString(R.string.to_your_list));

		inviteAndRequest = (Button) findViewById(R.id.contact_invite_and_request);
		inviteAndRequest.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				req = true;
				new Invite().execute();
				dismiss();
			}
		});
		justInvite = (Button) findViewById(R.id.contact_only_invite);
		justInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				req = false;
				new Invite().execute();
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
	public class Invite extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/contact_copy");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("guid", new StringBody(String.valueOf(invited.getGuid())));
				if(req){
					reqEntity.addPart("req_details", new StringBody("1"));
				}
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					@SuppressWarnings("unused")
					HttpResponse rp = hc.execute(post);
				} else {
					OfflineData uplooad = new OfflineData("mobile/contact_copy", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {

			}
			return null;
		}
		
	}
}
