package com.groupagendas.groupagenda.chat;

import java.util.concurrent.ExecutionException;

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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatMessageAdapter;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.OfflineData;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.utils.Utils;

public class ChatMessageActivity extends Activity {
	private int event_id;

	EditText chatInput;
	Button chatSend;
	ChatMessageAdapter adapter;
	ListView chat_message_list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		event_id = getIntent().getIntExtra("event_id", 0);
		setContentView(R.layout.chat);
		Object[] executeArray = { event_id, null };
		try {
			new GetChatMessages().execute(executeArray).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		adapter = new ChatMessageAdapter(this, Data.getChatMessages());
		chat_message_list = (ListView) findViewById(R.id.chat_message_list);
		chat_message_list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		chatInput = (EditText) findViewById(R.id.chat_input);
		chatSend = (Button) findViewById(R.id.chat_send);
	}

	@Override
	protected void onResume() {
		super.onResume();
		chatSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String message = chatInput.getText().toString();
				Object[] executePostArray = { event_id, message };
				new PostChatMessage().execute(executePostArray);
				Object[] executeArray = { event_id, null };
				new GetChatMessages().execute(executeArray);
				chatInput.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
				chat_message_list.setSelection(adapter.getCount()-1);
				
			}
		});
	}

	private class GetChatMessages extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int event_id = (Integer) params[0];
				String from = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_get");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
				if (from == null) {
					reqEntity.addPart("from_datetime", new StringBody(String.valueOf("")));
				} else {
					reqEntity.addPart("from_datetime", new StringBody(String.valueOf(from)));
				}

				post.setEntity(reqEntity);
				HttpResponse rp = null;
				if (DataManagement.networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						if (resp != null) {
							JSONObject object = new JSONObject(resp);
							boolean success = object.getBoolean("success");
							if (!success) {
								Log.e("Change account ERROR", object.getJSONObject("error").getString("reason"));
							} else {
								JSONArray chatMessages = object.getJSONArray("items");
								Data.getChatMessages().clear();
								for (int i = 0, l = chatMessages.length(); i < l; i++) {
									final JSONObject chatMessage = chatMessages.getJSONObject(i);
									ChatMessageObject message = new ChatMessageObject();
									message.messageId = chatMessage.getInt("message_id");
									message.eventId = chatMessage.getInt("event_id");
									message.dateTime = chatMessage.getString("datetime");
									message.dateTimeCalendar = Utils.stringToCalendar(message.dateTime,
											DataManagement.SERVER_TIMESTAMP_FORMAT);
									message.userId = chatMessage.getInt("user_id");
									message.message = chatMessage.getString("message");
									String deleted = chatMessage.getString("deleted");
									message.deleted = !deleted.equals("null");
									message.updated = chatMessage.getString("updated");
									message.updatedCalendar = Utils.stringToCalendar(message.updated,
											DataManagement.SERVER_TIMESTAMP_FORMAT);
									message.fullname = chatMessage.getString("fullname");
									message.contactId = chatMessage.getString("contact_id");
									message.dateTimeConverted = chatMessage.getString("datetime_conv");
									message.dateTimeConvertedCalendar = Utils.stringToCalendar(message.dateTimeConverted,
											DataManagement.SERVER_TIMESTAMP_FORMAT);
									message.formatedDateTime = chatMessage.getString("formatted_datetime");
									Data.getChatMessages().add(message);
								}
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_get", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			if(adapter != null){
				adapter.notifyDataSetChanged();
			}
		}

	}

	public class PostChatMessage extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			try {
				int event_id = (Integer) params[0];
				String message = (String) params[1];
				HttpClient hc = new DefaultHttpClient();
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/chat_post");

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				reqEntity.addPart("token", new StringBody(Data.getToken()));
				reqEntity.addPart("event_id", new StringBody(String.valueOf(event_id)));
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
							boolean success = object.getBoolean("success");
							if (success) {
							}
						}
					}
				} else {
					OfflineData uplooad = new OfflineData("mobile/chat_post", reqEntity);
					Data.getUnuploadedData().add(uplooad);
				}
			} catch (Exception e) {
				Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(),
						e.getMessage());
			}
			return null;
		}
	}
}
