package com.groupagendas.groupagenda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	     if(intent.getExtras()!=null) {
	         NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
	         if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
	             Log.i("app","Network "+ni.getTypeName()+" connected");
	         }
	      }
		
		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED || connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
			
			Intent serviceIntent = new Intent();
			serviceIntent.setAction("com.groupagendas.groupagenda.UpdateService");
			context.startService(serviceIntent);
		}
		
	}
}
