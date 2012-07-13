package com.groupagendas.groupagenda.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
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
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.Utils;

public class MoreEmailsActivity extends Activity{
	
	private Button saveButton;
	
	private EditText mail1View;
	private EditText mail2View;
	private EditText mail3View;
	
	
	private DataManagement dm;
	private ProgressBar pb;
	
	private Account mAccount;
	
	private String errorStr = "";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.more_emails);
		
		dm = DataManagement.getInstance(this);
		pb = (ProgressBar) findViewById(R.id.progress);
		
		new GetAccountFromDBTask().execute();
		
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
		mAccount = account;
		
		if(mAccount.email2 != null && !mAccount.email2.equals("null"))
			mail1View.setText(mAccount.email2);
		if(mAccount.email3 != null && !mAccount.email3.equals("null"))
			mail2View.setText(mAccount.email3);
		if(mAccount.email4 != null && !mAccount.email4.equals("null"))
			mail3View.setText(mAccount.email4);
	}
	
	class ChangeEmailTask extends AsyncTask<Void, Boolean, Boolean>{
		
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			saveButton.setText(getString(R.string.saving));
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean check = true;
			String email="";
			
			//2
			email = mail1View.getText().toString();
			if(email.length() > 0){
				if(!Utils.checkEmail(email)){
					check = false;
					errorStr = getString(R.string.invalid_email, "1");
				}else{
					if(!email.equals(mAccount.email2)){
						if(dm.changeEmail(email, 2)){
							saveInDatabase(AccountProvider.AMetaData.AccountMetaData.EMAIL2, email);
						}
					}
				}
			}
			
			//3
			email = mail2View.getText().toString();
			if(email.length() > 0){
				if(!Utils.checkEmail(email)){
					check = false;
					errorStr = getString(R.string.invalid_email, "2");
				}else{
					if(!email.equals(mAccount.email3)){
						if(dm.changeEmail(email, 3)){
							saveInDatabase(AccountProvider.AMetaData.AccountMetaData.EMAIL3, email);
						}
					}
				}
			}
			
			//4
			email = mail3View.getText().toString();
			if(email.length() > 0){
				if(!Utils.checkEmail(email)){
					check = false;
					errorStr = getString(R.string.invalid_email, "3");
				}else{
					if(!email.equals(mAccount.email4)){
						if(dm.changeEmail(email, 4)){
							saveInDatabase(AccountProvider.AMetaData.AccountMetaData.EMAIL4, email);
						}
					}
				}
			}

			return check;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				finish();
			}else{
				showDialog(0);
				pb.setVisibility(View.GONE);
				saveButton.setText(getString(R.string.save));
			}
			super.onPostExecute(result);
		}
		
	}
	
	private void saveInDatabase(String key, String email){
		ContentValues values = new ContentValues();
		values.put(key, email);
		String where = AccountProvider.AMetaData.AccountMetaData.A_ID+"="+mAccount.user_id;
		
		getContentResolver().update(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, values, where, null);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(errorStr)
		       .setCancelable(false)
		       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });
		return builder.create();
	}
	
	class GetAccountFromDBTask extends AsyncTask<Void, Account, Account> {

		protected void onPreExecute() {

			pb.setVisibility(View.VISIBLE);

			super.onPreExecute();
		}

		protected Account doInBackground(Void... args) {
			return dm.getAccountFromLocalDb();
		}

		protected void onPostExecute(Account account) {
			if (account != null)
				feelFields(account);
			new GetAccountTask().execute();
			super.onPostExecute(account);
		}

	}
	
	class GetAccountTask extends AsyncTask<Void, Account, Account> {

		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		protected Account doInBackground(Void... args) {
			return dm.getAccountFromRemoteDb();
		}

		protected void onPostExecute(Account account) {
			if (account != null) {
				feelFields(account);
			}

			pb.setVisibility(View.GONE);
			super.onPostExecute(account);
		}

	}
}
