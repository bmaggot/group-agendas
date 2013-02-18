package com.groupagendas.groupagenda.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.groupagendas.groupagenda.GroupAgendasActivity;

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