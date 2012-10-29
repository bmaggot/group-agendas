package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.chat.ChatThreadObject;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.utils.Utils;

public class ChatThreadAdapter extends AbstractAdapter<ChatThreadObject> {

	public ChatThreadAdapter(Context context, List<ChatThreadObject> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_thread, null);
		}
		// TODO sukeisti last_msg ir time field'u id vietomis + investigate.
		
		ChatThreadObject chatThread = (ChatThreadObject) this.getItem(i);
		TextView title = (TextView) view.findViewById(R.id.chat_thread_title);
		title.setText(chatThread.getTitle());
		TextView chatTime = (TextView) view.findViewById(R.id.chat_thread_last_message);
		Calendar timeStart = Calendar.getInstance();
		timeStart.setTimeInMillis(Utils.unixTimestampToMilis(chatThread.getTimeStart()));
		chatTime.setText(DateUtils.getRelativeTimeSpanString(timeStart.getTimeInMillis()));
		TextView chatLastMsg = (TextView) view.findViewById(R.id.chat_thread_time);
		chatLastMsg.setText(ChatManagement.getLastMessageForEventFromLocalDb(context, chatThread.getEvent_id()).getMessage());
		return view;
	}

}
