package com.groupagendas.groupagenda;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

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
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.data.OfflineData;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.https.WebService;

public class ConnectReceiver extends BroadcastReceiver {

	private static Context context;

	@SuppressWarnings("unchecked")
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
			new ExecuteOfflineChats().execute();
			boolean success = false;
			
			
			if (DataManagement.networkAvailable
					&& (Data.getUnuploadedData().size() > 0)) {
				new ExecuteOfflineChanges().execute(Data.getUnuploadedData());
			}

			if (success) {

			} else {

			}
		} else {
			// reikes account'a long'a uzsetinti dabartini momenta

			account.setLastTimeConnectedToWeb(Calendar.getInstance());
			DataManagement.networkAvailable = false;
			Log.i("app", "No connection to network!");
		}

	}
	//sita klasse exucutina metodus, kurie update visus duomenys
	private class ExecuteOfflineChats extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Account account = new Account(context);
			EventManagement.uploadOfflineEvents(context);
			ContactManagement.uploadOfflineContact(context);
			ChatManagement.uploadUnploaded(context, ChatManagement.getChatMessagesCreatedOffline(context)); //grazina arraylista, kuris uplaudina kiekviena message
			//account.clearLastTimeConnectedToweb();
			DataManagement.synchronizeWithServer(context, null, account.getLatestUpdateUnixTimestamp());
			return null;
		}
		
	}

	private class ExecuteOfflineChanges extends
			AsyncTask<ArrayList<OfflineData>, Void, Boolean> {

		@Override
		protected Boolean doInBackground(ArrayList<OfflineData>... params) {
			ArrayList<OfflineData> requests = params[0];
			boolean success = false;
			WebService webService = new WebService();

			try {
				for (OfflineData request : requests) {
					HttpPost post = new HttpPost(Data.getServerUrl()
							+ request.getLocation());
					post.setEntity(request.getRequest());
					if (DataManagement.networkAvailable) {
						HttpResponse rp = webService.getResponseFromHttpPost(post);

						if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							String resp = EntityUtils.toString(rp.getEntity());
							if (resp != null) {
								JSONObject object = new JSONObject(resp);
								success = object.getBoolean("success");

								if (success == false) {
									Log.e("Create event error",
											object.getJSONObject("error")
													.getString("reason"));
								}
							}
						} else {
							Log.e("createEvent - status", rp.getStatusLine()
									.getStatusCode() + "");
						}
					}
				}
				if (success == true) {
					Data.setUnuploadedData(new ArrayList<OfflineData>());
				}
			} catch (Exception ex) {
				Reporter.reportError(context, this.getClass().toString(),
						Thread.currentThread().getStackTrace()[2]
								.getMethodName().toString(), ex.getMessage());
			}
			// this.success = success;
			return success;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				Data.setUnuploadedData(new ArrayList<OfflineData>());
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}
