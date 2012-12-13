package com.groupagendas.groupagenda.account.verification;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;

public class SmsVerificationCodeActivity extends Activity {

	private static EditText confirmCode;
	private JSONObject object;
	private ProgressDialog pd;

	@Override
	public void onResume() {
		super.onResume();
		this.setContentView(R.layout.sms_verification_enter_cofirm_code);
		pd = new ProgressDialog(SmsVerificationCodeActivity.this);
		Button confirmBut = (Button) findViewById(R.id.confirmButton);
		confirmCode = (EditText) findViewById(R.id.confirm_code);
		new ResendConfirmationSyncTask().execute();
		confirmBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ConfirmationSyncTask().execute();

				Log.e("confirm code", "" + confirmCode.getText().toString());
				// finish();
			}
		});
		Log.e("phone number", "" + getIntent().getStringExtra(NavbarActivity.newPhoneNumber));

	}

	public void dialog(Context context, String message, boolean success, final boolean finishActivity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.phone_number_verification);
		builder.setMessage(message);
		if (success) {
			builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(finishActivity){
						finish();
					}
					dialog.cancel();
				}
			});
		} else {
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new ResendConfirmationSyncTask().execute();

				}
			});
		}
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	private class ConfirmationSyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			pd.setMessage(getResources().getString(R.string.sending_confirmation));
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			object = DataManagement.confirmPhoneNumber(SmsVerificationCodeActivity.this,
					getIntent().getStringExtra(NavbarActivity.newPhoneNumber), confirmCode.getText().toString());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			if (object != null) {
				try {
					boolean success = object.getBoolean("success");
					if (success) {
						dialog(SmsVerificationCodeActivity.this, getString(R.string.verification_success), success, true);
					} else {
						dialog(SmsVerificationCodeActivity.this, getString(R.string.verification_unsuccess) + "\n\n"
								+ getString(R.string.reason) + " " + object.getString("reason") + "\n\n"
								+ getString(R.string.send_question), success, false);
					}
				} catch (Exception ex) {
					Log.e("SMS", "exception");
				}
			}
		}

	}

	public class ResendConfirmationSyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			pd.setMessage(getResources().getString(R.string.sending_confirmation));
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			object = DataManagement.resendConfirmPhoneNumberCode(SmsVerificationCodeActivity.this, getIntent().getStringExtra(NavbarActivity.newPhoneNumber));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			if (object != null) {
				try {
					boolean success = object.getBoolean("success");
					if (success) {
						dialog(SmsVerificationCodeActivity.this, getString(R.string.send_success), success, false);
					} else {
						dialog(SmsVerificationCodeActivity.this, getString(R.string.send_unsuccess) + "\n\n" + getString(R.string.reason)
								+ " " + object.getString("reason") + "\n\n" + getString(R.string.send_question), success, false);
					}
				} catch (Exception ex) {
					Log.e("SMS", "exception");
				}
			}
		}

	}

}
