package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
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
		title.setText(chatThread.getTitle());
		TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
		Calendar timeStart = Calendar.getInstance();
		timeStart.setTimeInMillis(chatThread.getTimeStart());
		chatTime.setText(timeStart.getTime().toString());
		return view;
	}

}
