package com.groupagendas.groupagenda;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;

public class ConnectReceiver extends BroadcastReceiver {

	private static Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectReceiver.context = context;
		ConnectivityManager conn = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (intent.getExtras() != null) {
			NetworkInfo ni = (NetworkInfo) intent.getExtras().get(
					ConnectivityManager.EXTRA_NETWORK_INFO);
			if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
				Log.i("app", "Network " + ni.getTypeName() + " connected");
			}
		}

		Account account = new Account(context);

		if (conn.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
				|| conn.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
			DataManagement.networkAvailable = true;
			if(account.getLastTimeConnectedToWeb() != 0){
				new ExecuteOfflineChats().execute();
			}
		} else {
			account.setLastTimeConnectedToWeb(Calendar.getInstance());
			DataManagement.networkAvailable = false;
			Log.i("app", "No connection to network!");
		}

	}
	private class ExecuteOfflineChats extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Account account = new Account(context);
			DataManagement.synchronizeWithServer(context, null, account.getLatestUpdateUnixTimestamp());
			ContactManagement.uploadOfflineContact(context);
			ContactManagement.uploadOfflineCreatedGroups(context);
			EventManagement.uploadOfflineEvents(context);
			ChatManagement.uploadUnploaded(context, ChatManagement.getChatMessagesCreatedOffline(context));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			Account account = new Account(context);
			account.clearLastTimeConnectedToweb();
		}
		
	}
}
