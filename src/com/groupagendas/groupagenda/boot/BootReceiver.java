package com.groupagendas.groupagenda.boot;

import com.groupagendas.groupagenda.GroupAgendasActivity;
import com.groupagendas.groupagenda.LoginActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        	Toast.makeText(context, "GA loading", Toast.LENGTH_LONG).show();
            Intent serviceIntent = new Intent(context, GroupAgendasActivity.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(serviceIntent);
        }
    }
}