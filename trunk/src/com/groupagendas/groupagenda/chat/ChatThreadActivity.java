package com.groupagendas.groupagenda.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatThreadAdapter;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;

public class ChatThreadActivity extends Activity {
	private DataManagement dm;
	ChatThreadAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		LinearLayout chatInputBlock = (LinearLayout) findViewById(R.id.chat_inputBlock);
		chatInputBlock.setVisibility(View.INVISIBLE);
		dm = DataManagement.getInstance(this);
		adapter = new ChatThreadAdapter(this, Data.getChatThreads());
		ListView chat_message_list = (ListView) findViewById(R.id.chat_message_list);
		chat_message_list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
}
