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
	TestCondition test;
	public EventActivityOnClickListener (Context context, Event event){
		this(context, event, null);
	}
	public EventActivityOnClickListener (Context context, Event event, TestCondition test){
		this.event = event;
		this.context = context;
		this.test = test;
	}
	
	@Override
  public void onClick(View view) {
		if (test != null && !test.test())
			return;
		
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
  	
	public interface TestCondition {
		boolean test();
	}
}