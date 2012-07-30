package com.groupagendas.groupagenda;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	     if(intent.getExtras()!=null) {
	         NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
	         if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
	             Log.i("app","Network "+ni.getTypeName()+" connected");
	         }
	      }
		
		if (conn.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED || conn.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
			DataManagement.networkAvailable = true;
//			Intent serviceIntent = new Intent();
//			serviceIntent.setAction("com.groupagendas.groupagenda.UpdateService");
//			context.startService(serviceIntent);
		} else {
			DataManagement.networkAvailable = false;
		}
		
	}
}
