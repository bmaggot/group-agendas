package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.chat.ChatThreadObject;

public class ChatThreadAdapter extends AbstractAdapter<ChatThreadObject> {

	public ChatThreadAdapter(Context context, List<ChatThreadObject> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, null);
		}
		ChatThreadObject chatThread = (ChatThreadObject) this.getItem(i);
		TextView title = (TextView) view.findViewById(R.id.chat_message_body);
		title.setText(chatThread.title);
		TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
		chatTime.setText(chatThread.message_last);
		return view;
	}

}
