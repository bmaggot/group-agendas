package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.data.Data;

public class ChatMessageAdapter extends AbstractAdapter<ChatMessageObject> {

	public ChatMessageAdapter(Context context, List<ChatMessageObject> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, null);
		}

		ChatMessageObject chatMessage = (ChatMessageObject) this.getItem(i);
		if (!chatMessage.deleted) {
			TextView messageBody = (TextView) view.findViewById(R.id.chat_message_body);
			messageBody.setText(chatMessage.message);
			TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
			chatTime.setText(chatMessage.dateTime);
			if (chatMessage.userId == Data.getAccount().user_id) {
				view.findViewById(R.id.kubiks).setVisibility(View.VISIBLE);
			}
		}
		
		return view;
	}

}
