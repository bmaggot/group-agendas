package com.groupagendas.groupagenda.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;

public class ChatThreadActivity extends Activity {
	private DataManagement dm;
	ListAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		LinearLayout chatInputBlock = (LinearLayout) findViewById(R.id.chat_inputBlock);
		chatInputBlock.setVisibility(View.INVISIBLE);
		dm = DataManagement.getInstance(this);
		if(!Data.getChatThreads().isEmpty()){
			adapter = new ArrayAdapter<ChatThreadObject>(this, R.layout.chat_message, Data.getChatThreads());
		} else {
			
		}
		ListView chat_message_list = (ListView) findViewById(R.id.chat_message_list);
//		chat_message_list.setAdapter(adapter);
	}
}
