package com.groupagendas.groupagenda.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.Utils;

public class MoreEmailsActivity extends Activity {

	private Button saveButton;

	private EditText mail1View;
	private EditText mail2View;
	private EditText mail3View;

	private DataManagement dm;
	private ProgressBar pb;

	private String errorStr = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.more_emails);

		dm = DataManagement.getInstance(this);
		pb = (ProgressBar) findViewById(R.id.progress);

		mail1View = (EditText) findViewById(R.id.email1);
		mail2View = (EditText) findViewById(R.id.email2);
		mail3View = (EditText) findViewById(R.id.email3);

		saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new ChangeEmailTask().execute();
			}
		});
	}

	private void feelFields(Account account) {
		Account mAccount = account;

		if (!mAccount.getEmail2().equals("null"))
			mail1View.setText(mAccount.getEmail2());
		if (!mAccount.getEmail3().equals("null"))
			mail2View.setText(mAccount.getEmail3());
		if (!mAccount.getEmail4().equals("null"))
			mail3View.setText(mAccount.getEmail4());
	}

	class ChangeEmailTask extends AsyncTask<Void, Boolean, Boolean> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean check = true;
			Account mAccount = new Account();
			String email = "";

			// 2
			email = mail1View.getText().toString();
			if (email.length() > 0) {
				if (!Utils.checkEmail(email)) {
					check = false;
					errorStr = getString(R.string.invalid_email, "1");
				} else {
					if (!email.equals(mAccount.getEmail2())) {
						if (dm.changeEmail(email, 2)) {
							mAccount.setEmail(email, 2);
						}
					}
				}
			}

			// 3
			email = mail2View.getText().toString();
			if (email.length() > 0) {
				if (!Utils.checkEmail(email)) {
					check = false;
					errorStr = getString(R.string.invalid_email, "2");
				} else {
					if (!email.equals(mAccount.getEmail3())) {
						if (dm.changeEmail(email, 3)) {
							mAccount.setEmail(email, 3);
						}
					}
				}
			}

			// 4
			email = mail3View.getText().toString();
			if (email.length() > 0) {
				if (!Utils.checkEmail(email)) {
					check = false;
					errorStr = getString(R.string.invalid_email, "3");
				} else {
					if (!email.equals(mAccount.getEmail4())) {
						if (dm.changeEmail(email, 4)) {
							mAccount.setEmail(email, 4);
						}
					}
				}
			}

			return check;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish();
			} else {
				showDialog(0);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(errorStr).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	class GetAccountTask extends AsyncTask<Void, Account, Account> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Account doInBackground(Void... args) {
			return dm.getAccountFromRemoteDb();
		}

		@Override
		protected void onPostExecute(Account account) {
			if (account != null) {
				feelFields(account);
			}

			pb.setVisibility(View.GONE);
			super.onPostExecute(account);
		}

	}
}
