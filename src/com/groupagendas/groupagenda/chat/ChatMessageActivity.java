package com.groupagendas.groupagenda.chat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatMessageAdapter;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.OfflineData;

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
		Object[] params = { this, event_id };
		new GetChatMessagesForEventFromRemoteDb().execute(params);
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
				Object[] params = { this, message, event_id };
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
			boolean success = false;
			HttpClient hc = new DefaultHttpClient();
			HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			try {
				reqEntity.addPart(ChatManagement.TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(eventId)));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			post.setEntity(reqEntity);
			try {
				HttpResponse rp = hc.execute(post);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String resp = EntityUtils.toString(rp.getEntity());
					if (resp != null) {
						JSONObject object = new JSONObject(resp);
						success = object.getBoolean("success");
						if (success) {
							JSONArray chatMessages = object.getJSONArray("items");
							for (int i = 0, l = chatMessages.length(); i < l; i++) {
								ChatManagement.insertChatMessageContentValueToLocalDb(context,
										ChatManagement.makeChatMessageObjectContentValueFromJSON(chatMessages.getJSONObject(i)));
							}
						} else {
							Toast.makeText(context, object.getString("error"), Toast.LENGTH_LONG);
						}
					}
				}
			} catch (Exception e) {
				Log.e("getChatMessagesForEventFromRemoteDb(Context context, int eventId " + eventId + ")", e.getMessage());
			}
			return null;
		}

	}

	private class PostChatMessage extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			Context context = (Context) params[0];
			String message = (String) params[1];
			int eventId = (Integer) params[2];
			try {
				boolean success = false;
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_post");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart(ChatManagement.TOKEN, new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(eventId)));
				if (message == null) {
					reqEntity.addPart("message", new StringBody(String.valueOf("")));
				} else {
					reqEntity.addPart("message", new StringBody(String.valueOf(message)));
				}

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if (DataManagement.networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							success = object.getBoolean("success");
							if (success) {
								ChatManagement.insertChatMessageContentValueToLocalDb(context,
										ChatManagement.makeChatMessageObjectContentValueFromJSON(object.getJSONObject("message")));
							} else {
								Toast.makeText(context, object.getString("error"), Toast.LENGTH_LONG);
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_post", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
				Log.e("postChatMessage(Context context, message " + message + ", event id " + eventId + ")", e.getMessage());
			}
			return null;
		}

	}

}
