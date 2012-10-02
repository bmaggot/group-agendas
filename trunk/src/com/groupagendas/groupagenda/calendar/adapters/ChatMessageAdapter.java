package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.data.ChatManagement;

public class ChatMessageAdapter extends AbstractAdapter<ChatMessageObject> {

	public ChatMessageAdapter(Context context, List<ChatMessageObject> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, null);
		}

		final ChatMessageObject chatMessage = (ChatMessageObject) this.getItem(i);
		if (!chatMessage.isDeleted()) {
			Account account = new Account();
			TextView messageBody = (TextView) view.findViewById(R.id.chat_message_body);
			messageBody.setText(chatMessage.getMessage());
			TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(chatMessage.getCreated());
			chatTime.setText(calendar.getTime().toString());
			if (chatMessage.getUserId() == account.getUser_id()) {
				view.findViewById(R.id.kubiks).setVisibility(View.VISIBLE);

				TextView iksiuks = (TextView) view.findViewById(R.id.chat_message_delete);
				iksiuks.setVisibility(View.VISIBLE);
				iksiuks.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getContext(), R.string.delete_chat_message, Toast.LENGTH_SHORT).show();
						ChatManagement.removeChatMessageFromRemoteDb(getContext(), chatMessage.getMessageId());
					}
				});
			}
		}

		return view;
	}

}
