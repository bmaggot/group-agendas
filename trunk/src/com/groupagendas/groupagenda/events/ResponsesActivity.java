package com.groupagendas.groupagenda.events;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ResponsesAdapter;
import com.groupagendas.groupagenda.data.EventManagement;

public class ResponsesActivity extends Activity {
	
	ResponsesAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.responses_threads);
		
		ListView responses_thread_list = (ListView) findViewById(R.id.responses_list);
		adapter = new ResponsesAdapter(ResponsesActivity.this, EventManagement.getResponsesFromRemoteDb(getApplicationContext()));
		responses_thread_list.setAdapter(adapter);
		
	}

}
