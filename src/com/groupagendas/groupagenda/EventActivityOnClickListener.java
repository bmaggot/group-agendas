package com.groupagendas.groupagenda;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventEditActivity;

public class EventActivityOnClickListener implements View.OnClickListener{
	Event event;
	Context context;
	public EventActivityOnClickListener (Context context, Event event){
		this.event = event;
		this.context = context;
	}
	@Override
  public void onClick(View view) {
		Intent intent = new Intent(context, EventEditActivity.class);
		intent.putExtra("event_id", event.getEvent_id());
		intent.putExtra("type", event.getType());
		intent.putExtra("isNative", event.isNative());
		context.startActivity(intent);
	}
  	
}