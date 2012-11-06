package com.groupagendas.groupagenda;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.groupagendas.groupagenda.contacts.ContactInfoActivity;
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
		if(event.isBirthday()){
			Intent intent = new Intent(context, ContactInfoActivity.class);
			intent.putExtra("contactId", Integer.valueOf(""+event.getInternalID()));
			context.startActivity(intent);
		}else{
			Intent intent = new Intent(context, EventEditActivity.class);
			intent.putExtra("event_id", event.getInternalID());
			intent.putExtra("type", event.getType());
			intent.putExtra("isNative", event.isNative());
			context.startActivity(intent);
		}
		
	}
  	
}