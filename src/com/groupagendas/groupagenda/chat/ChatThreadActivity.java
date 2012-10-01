package com.groupagendas.groupagenda.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatThreadAdapter;
import com.groupagendas.groupagenda.data.Data;

public class ChatThreadActivity extends Activity {
	ChatThreadAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		LinearLayout chatInputBlock = (LinearLayout) findViewById(R.id.chat_inputBlock);
		chatInputBlock.setVisibility(View.INVISIBLE);
		adapter = new ChatThreadAdapter(this, Data.getChatThreads());
		ListView chat_message_list = (ListView) findViewById(R.id.chat_message_list);
		chat_message_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(ChatThreadActivity.this, ChatMessageActivity.class);
//				intent.putExtra("event_id", Data.getChatThreads().get(arg2).event_id);
				startActivity(intent);
			}
			
		});
		chat_message_list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
}
