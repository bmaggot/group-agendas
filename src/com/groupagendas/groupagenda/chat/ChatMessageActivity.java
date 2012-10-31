package com.groupagendas.groupagenda.chat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.groupagendas.groupagenda.C2DMReceiver;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatMessageAdapter;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.data.DataManagement;

public class ChatMessageActivity extends Activity {
	private int event_id;

	EditText chatInput;
	Button chatSend;
	static ChatMessageAdapter adapter;
	ListView chat_message_list;
	ArrayList<ChatMessageObject> chatMessages;
	ChatMessageObject chatMessageObject;
	ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.chat);
		pb = (ProgressBar) findViewById(R.id.progress);

		event_id = getIntent().getIntExtra("event_id", 0);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter(C2DMReceiver.REFRESH_MESSAGES_LIST + event_id));

		Object[] params = { this, event_id, false };

		new GetChatMessagesForEventDb().execute(params);

		adapter = new ChatMessageAdapter(ChatMessageActivity.this, chatMessages);
		chat_message_list = (ListView) findViewById(R.id.chat_message_list);
		chat_message_list.setAdapter(adapter);
		if (DataManagement.networkAvailable) {
			chatInput = (EditText) findViewById(R.id.chat_input);
			chatSend = (Button) findViewById(R.id.chat_send);

			chatSend.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String message = chatInput.getText().toString();
					chatMessageObject = ChatManagement.makeChatMessageObjectNow(ChatMessageActivity.this, message, event_id);
					chatMessages.add(chatMessageObject);
					adapter.notifyDataSetChanged();
					Object[] params = { ChatMessageActivity.this, message, event_id };
					new PostChatMessage().execute(params);
					chatInput.setText("");
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
					chat_message_list.setSelection(adapter.getCount() - 1);

				}
			});
		} else {
			LinearLayout chatInputBlock = (LinearLayout) findViewById(R.id.chat_inputBlock);
			chatInputBlock.setVisibility(View.GONE);
		}
	}

	private class GetChatMessagesForEventDb extends AsyncTask<Object, Void, Void> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Object... params) {
			Context context = (Context) params[0];
			int eventId = (Integer) params[1];
			boolean refreshMessagesList = (Boolean) params[2];
			chatMessages = new ArrayList<ChatMessageObject>();
			chatMessages = ChatManagement.getChatMessagesForEventFromLocalDb(context, eventId);
			if (DataManagement.networkAvailable && !refreshMessagesList && chatMessages.isEmpty()) {
				chatMessages = ChatManagement.getChatMessagesForEventFromRemoteDb(eventId, context, true, 0);
			}
			if (refreshMessagesList) {
				refreshMessagesList = false;
				ChatManagement.getChatMessagesForEventFromRemoteDb(eventId, context, true, ChatManagement.getLastMessageTimeStamp(context, eventId));
				chatMessages.clear();
				chatMessages = ChatManagement.getChatMessagesForEventFromLocalDb(context, eventId);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			adapter.setList(chatMessages);
			adapter.notifyDataSetChanged();
			chat_message_list.setSelection(adapter.getCount() - 1);
			pb.setVisibility(View.INVISIBLE);
		}

	}

	private class PostChatMessage extends AsyncTask<Object, Void, Void> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Object... params) {
			Context context = (Context) params[0];
			String message = (String) params[1];
			int eventId = (Integer) params[2];
			ChatMessageObject newChatMessageObject = ChatManagement.postChatMessage(eventId, message, context);
			if (newChatMessageObject == null) {
				chatMessages.remove(chatMessageObject);
			} else {
				chatMessages.remove(chatMessageObject);
				chatMessages.add(newChatMessageObject);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			adapter.notifyDataSetChanged();
			chat_message_list.setSelection(adapter.getCount() - 1);
			pb.setVisibility(View.INVISIBLE);
		}

	}

	public class RemoveChatMessageFromRemoteDb extends AsyncTask<Object, ChatMessageObject, ChatMessageObject> {

		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
		}

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
			chat_message_list.setSelection(adapter.getCount() - 1);
			pb.setVisibility(View.INVISIBLE);
		}

	}

	public void deleteMessage(int message_id, ChatMessageObject chatMessageObject) {
		Object[] params = { ChatMessageActivity.this, message_id, chatMessageObject };
		new RemoveChatMessageFromRemoteDb().execute(params);

	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Object[] params = { context, event_id, true };
			new GetChatMessagesForEventDb().execute(params);
		}
	};
}
