package com.groupagendas.groupagenda.chat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
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
	ArrayList<ChatMessageObject> chatMessages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.chat);

		event_id = getIntent().getIntExtra("event_id", 0);

		Object[] params = { this, event_id };
		new GetChatMessagesForEventFromRemoteDb().execute(params);

		adapter = new ChatMessageAdapter(ChatMessageActivity.this, chatMessages);
		chat_message_list = (ListView) findViewById(R.id.chat_message_list);
		chat_message_list.setAdapter(adapter);
		chatInput = (EditText) findViewById(R.id.chat_input);
		chatSend = (Button) findViewById(R.id.chat_send);

		chatSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String message = chatInput.getText().toString();
				Object[] params = { ChatMessageActivity.this, message, event_id };
				new PostChatMessage().execute(params);
				chatInput.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
				chat_message_list.setSelection(adapter.getCount() - 1);

			}
		});
	}

	private class GetChatMessagesForEventFromRemoteDb extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			Context context = (Context) params[0];
			int eventId = (Integer) params[1];
			chatMessages = ChatManagement.getChatMessagesForEventFromRemoteDb(eventId, chatMessages, context);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			adapter.setList(chatMessages);
			adapter.notifyDataSetChanged();
		}

	}

	private class PostChatMessage extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			Context context = (Context) params[0];
			String message = (String) params[1];
			int eventId = (Integer) params[2];
			chatMessages.add(ChatManagement.postChatMessage(eventId, message, context));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			adapter.notifyDataSetChanged();
		}

	}

	public class RemoveChatMessageFromRemoteDb extends AsyncTask<Object, ChatMessageObject, ChatMessageObject> {

		@Override
		protected ChatMessageObject doInBackground(Object... params) {
			Context context = (Context) params[0];
			int messageId = (Integer) params[1];
			ChatMessageObject chatMessageObject = (ChatMessageObject) params[2];
			ChatManagement.removeChatMessage(context, messageId);
			return chatMessageObject;
		}

		@Override
		protected void onPostExecute(ChatMessageObject result) {
			result.setDeleted(true);
			adapter.notifyDataSetChanged();
		}

	}

	public void deleteMessage(int message_id, ChatMessageObject chatMessageObject) {
		Object[] params = {ChatMessageActivity.this, message_id, chatMessageObject};
		new RemoveChatMessageFromRemoteDb().execute(params);
		
	}

}
