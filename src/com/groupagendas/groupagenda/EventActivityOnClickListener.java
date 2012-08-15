package com.groupagendas.groupagenda;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;

public class EventActivityOnClickListener implements View.OnClickListener{
	Event event;
	Context context;
	public EventActivityOnClickListener (Context context, Event event){
		this.event = event;
		this.context = context;
	}
	@Override
  public void onClick(View view) {
		Intent intent = new Intent(context, EventActivity.class);
		intent.putExtra("event_id", event.event_id);
		intent.putExtra("type", event.type);
		intent.putExtra("isNative", event.isNative);
		context.startActivity(intent);
	}
  	
}