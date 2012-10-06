package com.groupagendas.groupagenda;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.registration.RegistrationActivity;
import com.pass_retrieve.forgot_pass1;

public class LoginActivity extends Activity {

	private DataManagement dm;
	private EditText loginText, passwordText;
	private CheckBox stayCheck;
	private ProgressBar pb;
	private String error;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		ConnectivityManager conn = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conn.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED || conn.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
			DataManagement.networkAvailable = true;
		} else {
			DataManagement.networkAvailable = false;
			Data.needToClearData = false;
		}
      
        findViewById(R.id.forgot_pass).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), forgot_pass1.class ));
//                LoginActivity.this.finish();
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
				startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
			}
		});

		SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);
		stayCheck = (CheckBox) findViewById(R.id.login_stayLoggedIn);
		
		if (prefs.getBoolean("stay_logged_in", false)) {
			loginText.setText(prefs.getString("email", ""));
			passwordText.setText(prefs.getString("password", ""));

			new LoginTask().execute();
		}
	}
	
	public class LoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			boolean success = false;
			SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);
			Editor editor = prefs.edit();
			String login = loginText.getText().toString();
			String password = passwordText.getText().toString();
			Boolean stay = stayCheck.isChecked();

			success = dm.login(login, password);

			if (success) {
				editor.putString("email", login);
				editor.putString("password", password);
				editor.putBoolean("stay_logged_in", stay);
				editor.putBoolean("logged", true);
				// TODO overview if instance should be retrieved with locale attribute.
				editor.putLong("latest_update", Calendar.getInstance().getTimeInMillis());
				success = editor.commit();
			}

			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pb.setVisibility(View.GONE);
			if (result == true) {
				getApplicationContext();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);
				Intent intent = new Intent(LoginActivity.this, NavbarActivity.class);
				startActivity(intent);
				finish();
			} else {
				error = dm.getError();
				showDialog(0);
			}

			super.onPostExecute(result);
		}

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(error).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
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
