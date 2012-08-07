package com.groupagendas.groupagenda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.registration.RegisterationActivity;
import com.groupagendas.groupagenda.utils.PrefixReceiver;
import com.pass_retrieve.forgot_pass1;

public class LoginActivity extends Activity {

	private DataManagement dm;
	private EditText loginText, passwordText;
	private ProgressBar pb;
	private String error;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

        final Activity con=this;
        findViewById(R.id.forgot_pass).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), forgot_pass1.class ));
               // con.finish();
            }
        });

		dm = DataManagement.getInstance(this);


		pb = (ProgressBar) findViewById(R.id.progress);
		loginText = (EditText) this.findViewById(R.id.login_emailText);
		passwordText = (EditText) this.findViewById(R.id.login_passwordText);
		passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                new LoginTask().execute();
                return true;
            }
            return false;
			}
		});
		
		Button loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginTask().execute();
			}
		});
		
		Button regButton = (Button) findViewById(R.id.registerButton);
		regButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterationActivity.class));
			}
		});
	}

	public class LoginTask extends AsyncTask<Void, Void, Boolean> {
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		protected Boolean doInBackground(Void... arg0) {
			boolean success = false;

			String login = loginText.getText().toString();
			String password = passwordText.getText().toString();

			success = dm.login(login, password);

			return success;
		}

		protected void onPostExecute(Boolean result) {
			pb.setVisibility(View.GONE);
			if (result == true) {
				getApplicationContext();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);
				Intent intent = new Intent(LoginActivity.this, NavbarActivity.class);
				intent.putExtra("load_data", true);
				startActivity(intent);
				finish();
			} else {
				error = dm.getError();
//				showDialog(0);
			}

			super.onPostExecute(result);
		}

	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(error).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
}
