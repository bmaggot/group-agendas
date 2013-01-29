package com.groupagendas.groupagenda.events;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;

public class NavigationDialog extends Dialog {
	private Button location;
	private Button fromHomeToDestination;
	private Button fromLocationToDestination;
	
	public NavigationDialog(final Activity context, int styleResId, final String startAddress, final String endAddress) {
		super(context, styleResId);

		this.setContentView(R.layout.navigation);
		this.setTitle(R.string.navigation_settings);

		location = (Button) findViewById(R.id.location);
		fromHomeToDestination = (Button) findViewById(R.id.fromHomeToDestination);
		fromLocationToDestination = (Button) findViewById(R.id.fromLocationToDestination);

		location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEventLocation(context, endAddress);
				dismiss();
			}
		});

		fromHomeToDestination.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(startAddress.contentEquals("")){
					Toast.makeText(context, context.getString(R.string.no_home_address), Toast.LENGTH_SHORT).show();
				} else {
					showEventDirection(context, startAddress, endAddress);
					dismiss();
				}
			}
		});

		fromLocationToDestination.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEventDirection(context, "", endAddress);
				dismiss();
			}
		});
	}	
	
	private void showEventLocation(Context context, String address){
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("http://maps.google.com/maps?&q="+address));
		context.startActivity(intent);
	}
	
	private void showEventDirection(Context context, String startAddress, String endAddress){
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("http://maps.google.com/maps?&saddr="+ startAddress + "&daddr=" 
		   + endAddress));
		context.startActivity(intent);
	}
}
