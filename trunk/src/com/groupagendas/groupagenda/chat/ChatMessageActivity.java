package com.groupagendas.groupagenda.chat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatMessageAdapter;
import com.groupagendas.groupagenda.data.ChatManagement;

public class ChatMessageActivity extends Activity {
	private int event_id;

	EditText chatInput;
	Button chatSend;
	ChatMessageAdapter adapter;
	ListView chat_message_list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.chat);

		event_id = getIntent().getIntExtra("event_id", 0);

		ArrayList<ChatMessageObject> chatMessages = new ArrayList<ChatMessageObject>();
		if (ChatManagement.getChatMessagesForEventFromRemoteDb(ChatMessageActivity.this, event_id)) {
			chatMessages = ChatManagement.getChatMessagesForEventFromLocalDb(ChatMessageActivity.this, event_id);
		}
		adapter = new ChatMessageAdapter(ChatMessageActivity.this, chatMessages);
		adapter.setList(chatMessages);
		adapter.notifyDataSetChanged();

		chat_message_list = (ListView) findViewById(R.id.chat_message_list);
		chatInput = (EditText) findViewById(R.id.chat_input);
		chatSend = (Button) findViewById(R.id.chat_send);

		chatSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String message = chatInput.getText().toString();
				ChatManagement.postChatMessage(ChatMessageActivity.this, message, event_id);
				chatInput.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
				chat_message_list.setSelection(adapter.getCount() - 1);

			}
		});
	}

}
