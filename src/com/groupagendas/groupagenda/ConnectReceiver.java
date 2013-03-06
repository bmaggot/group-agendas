package com.groupagendas.groupagenda;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.address.AddressManagement;
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

		Account account = new Account(context);

		if (conn.getActiveNetworkInfo() != null && conn.getActiveNetworkInfo().isConnected()) {
			Log.i("app", "Network " + conn.getActiveNetworkInfo().getTypeName() + " connected");
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
			ContactManagement.uploadOfflineContact(context);
			ContactManagement.uploadOfflineCreatedGroups(context);
			EventManagement.uploadOfflineEvents(context);
			ChatManagement.uploadUnploaded(context, ChatManagement.getChatMessagesCreatedOffline(context));
			AddressManagement.uploadOfflineAddresses(context);
			DataManagement.synchronizeWithServer(context, null, account.getLatestUpdateUnixTimestamp());
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			Account account = new Account(context);
			account.clearLastTimeConnectedToweb();
		}
		
	}
}
